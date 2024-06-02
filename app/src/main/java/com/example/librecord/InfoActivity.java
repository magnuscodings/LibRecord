package com.example.librecord;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class InfoActivity extends AppCompatActivity {
    TextView title, author, year, category, publisher, isbn, language;
    ImageView book;
    Button reserve;
    ImageButton bookmarkButton;
    private String user_id;
    DatePickerDialog datePickerDialog;
    Calendar calendar;
    Connect connection;
    Connection conn;
    String username;

    private String userId, name, email, checkerErr = "good";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        connection = new Connect();

        SharedPreferences sharedPreferences = getSharedPreferences("userDetails", MODE_PRIVATE);
        userId = sharedPreferences.getString("userId", "");
        email = sharedPreferences.getString("email", "");
        name = sharedPreferences.getString("name", "");

        user_id = userId;

        book = findViewById(R.id.bookimage);
        title = findViewById(R.id.title);
        author = findViewById(R.id.author);
        year = findViewById(R.id.year);
        category = findViewById(R.id.category);
        publisher = findViewById(R.id.publisher);
        isbn = findViewById(R.id.isbn);
        language = findViewById(R.id.language);
        reserve = findViewById(R.id.reserve);
        bookmarkButton = findViewById(R.id.bookmark);

        int[] bookImages = {
                R.drawable.histo1, R.drawable.histo2, R.drawable.prog3, R.drawable.calcu4, R.drawable.histo5,
                R.drawable.histo6, R.drawable.histo7, R.drawable.histo8, R.drawable.histo9, R.drawable.histo10,
                R.drawable.histo11, R.drawable.calcu12, R.drawable.calcu13, R.drawable.calcu14, R.drawable.calcu15,
                R.drawable.calcu16, R.drawable.sci17, R.drawable.sci18, R.drawable.sci19, R.drawable.sci20,
                R.drawable.sci22, R.drawable.prog23, R.drawable.prog24, R.drawable.prog25, R.drawable.prog26,
                R.drawable.prog27
        };

        Intent intent = getIntent();
        book.setImageResource(bookImages[intent.getIntExtra("id", 0)]);

        String bookTitle = intent.getStringExtra("bookTitle");
        Log.d("InfoActivity", "bookTitle " + bookTitle);

        ExecutorService executorService1 = Executors.newSingleThreadExecutor();
        executorService1.execute(() -> {
            String query = "SELECT * FROM books WHERE BookName = ?";
            try (Connection conn = Connect.CONN();
                 PreparedStatement preparedStatement = conn.prepareStatement(query)) {

                if (bookTitle == null) {
                    Log.e("InfoActivity", "Book title is null");
                    return;
                }

                preparedStatement.setString(1, bookTitle);
                try (ResultSet rs = preparedStatement.executeQuery()) {
                    if (rs.next()) {
                        String rsbookName = rs.getString("BookName");
                        String rsauthorName = rs.getString("AuthorName");
                        String rsyear = rs.getString("Year");
                        String rscategory = rs.getString("Category");
                        String rspublisher = rs.getString("Publisher");
                        String rsisbn = rs.getString("ISBN");
                        String rslanguage = rs.getString("Language");

                        Log.d("InfoActivity", "Author: " + rsauthorName);

                        runOnUiThread(() -> {
                            title.setText(rsbookName);
                            author.setText(rsauthorName);
                            year.setText(rsyear);
                            category.setText(rscategory);
                            publisher.setText(rspublisher);
                            isbn.setText(rsisbn);
                            language.setText(rslanguage);
                        });
                    }
                }
            } catch (SQLException e) {
                Log.e("InfoActivity", "Database error: ", e);
            } finally {
                executorService1.shutdown();
            }
        });

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            try {
                conn = connection.CONN();
                String query = "SELECT * FROM accountdata WHERE id = " + user_id;
                PreparedStatement preparedStatement = conn.prepareStatement(query);
                ResultSet rs = preparedStatement.executeQuery();

                if (rs.next()) {
                    username = rs.getString("username");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
        Log.d("InfoActivity", "username " + username);

        reserve.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCustomDatePickerDialog(intent);
            }
        });

        bookmarkButton.setOnClickListener(this::onBookmarkClick);
    }

    private void showCustomDatePickerDialog(Intent intent) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_date_picker);

        DatePicker datePicker = dialog.findViewById(R.id.datePicker);
        Button confirmButton = dialog.findViewById(R.id.confirmButton);
        Button cancelButton = dialog.findViewById(R.id.cancelButton);

        confirmButton.setOnClickListener(v -> {
            int day = datePicker.getDayOfMonth();
            int month = datePicker.getMonth();
            int year = datePicker.getYear();

            try {
                Toast.makeText(InfoActivity.this, "Please Wait", Toast.LENGTH_SHORT).show();
                Thread.sleep(3000);
            } catch (Exception e) {
                e.printStackTrace();
            }

            String bookTitle = title.getText().toString();
            if(checkReservation()){
                if(checkDateReservation(bookTitle,year, month, day)){
                    processReservation(username, bookTitle, year, month, day);
                }else{
                    Toast.makeText(this, "Please Select Other Date", Toast.LENGTH_SHORT).show();
                    return;
                }
            }else{
                Toast.makeText(this, "Please Return a book first to make a reservation", Toast.LENGTH_SHORT).show();
                return;
            }

            // Wait for the reservation process to complete before proceeding
            Executors.newSingleThreadExecutor().execute(() -> {
                try {
                    Thread.sleep(3000); // Adjust the sleep time as needed for the reservation process to complete
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                runOnUiThread(() -> {
                    if (checkerErr.equals("good")) {
                        Intent reserveActivity = new Intent(InfoActivity.this, ReservationCompleteActivity.class);
                        startActivity(reserveActivity);
                    }

                    dialog.dismiss();
                });
            });
        });

        cancelButton.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    public boolean checkReservation() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<Boolean> future = executorService.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() {
                try {
                    conn = connection.CONN();
                    String query = "SELECT COUNT(*) as count FROM reservationrecord WHERE status = 'reserve'";
                    PreparedStatement preparedStatement = conn.prepareStatement(query);

                    ResultSet rs = preparedStatement.executeQuery();
                    if (rs.next()) {
                        int count = rs.getInt("count");
                        return count < 3;
                    }
                } catch (SQLException e) {
                    Log.d("Connection Error", "error", e);
                }
                return false;
            }
        });

        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return false;
        } finally {
            executorService.shutdown();
        }
    }

    public boolean checkDateReservation(String title, int year, int month, int day) {
        String date = String.format("%04d-%02d-%02d", year, month, day);

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<Boolean> future = executorService.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() {
                try {
                    conn = connection.CONN();
                    String query = "SELECT COUNT(*) as count FROM reservationrecord WHERE title = ? AND date = ?";
                    PreparedStatement preparedStatement = conn.prepareStatement(query);
                    preparedStatement.setString(1, title);
                    preparedStatement.setString(2, date);
                    ResultSet rs = preparedStatement.executeQuery();

                    // Log the query with parameters
                    String loggedQuery = String.format("Executing query: SELECT COUNT(*) as count FROM reservationrecord WHERE title = '%s' AND date = '%s'", title, date);

                    Log.d("infoActivity","p:"+loggedQuery);
                    Log.d("infoActivity","p:"+rs);
                    if (rs.next()) {
                        int count = rs.getInt("count");
                        return count == 0;
                    }
                } catch (SQLException e) {
                    Log.d("Connection Error", "error", e);
                }
                return false;
            }
        });

        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return false;
        } finally {
            executorService.shutdown();
        }
    }


    public void processReservation(String name, String bookTitle, int year, int month, int day) {
        String date = day + "," + month + "," + year;
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            try {
                conn = connection.CONN();
                String query = "INSERT INTO reservationrecord (username, title, date) VALUES (?, ?, STR_TO_DATE(?, '%d,%m,%Y'))";
                try (PreparedStatement preparedStatement = conn.prepareStatement(query)) {
                    preparedStatement.setString(1, name);
                    preparedStatement.setString(2, bookTitle);
                    preparedStatement.setString(3, date);
                    preparedStatement.execute();
                    checkerErr = "good";
                }
            } catch (SQLException e) {
                checkerErr = "error";
                Log.d("Connection Error", "error", e);
            }
        });
    }

    public void onBookmarkClick(View view) {
        saveBookmark();
        openBookmarkFragment();
    }

    private void saveBookmark() {
        String bookTitle = title.getText().toString();
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            try {
                MySQLHelperBookmark.addBookmark(user_id, bookTitle, name);
                runOnUiThread(() -> Toast.makeText(InfoActivity.this, "Bookmark Saved", Toast.LENGTH_SHORT).show());
            } catch (SQLException e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(InfoActivity.this, "Error Saving Bookmark", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void openBookmarkFragment() {
        BookmarkFragment fragment = new BookmarkFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.activityInfo, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
