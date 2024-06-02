package com.example.librecord;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.DatePickerDialog;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    private String userId,name,email;

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
        Log.d("InfoActivitiy","bookTitle  "+bookTitle);

// Query database for author name based on book title
        ExecutorService executorService1 = Executors.newSingleThreadExecutor();
        executorService1.execute(() -> {
            try (Connection conn = Connect.CONN()) {
                String query = "SELECT * FROM books WHERE BookName = ?";
                PreparedStatement preparedStatement = conn.prepareStatement(query);
                preparedStatement.setString(1, bookTitle);
                ResultSet rs = preparedStatement.executeQuery();

                if (rs.next()) {
                    String authorName = rs.getString("AuthorName");
                    Log.d("InfoActivity", "Author: " + authorName);
                    title.setText(rs.getString("BookName"));
                    author.setText(rs.getString("AuthorName"));
                    year.setText(rs.getString("Year"));
                    category.setText(rs.getString("Category"));
                    publisher.setText(rs.getString("Publisher"));
                    isbn.setText(rs.getString("ISBN"));
                    language.setText(rs.getString("Language"));
                }
            } catch (SQLException e) {
                e.printStackTrace();
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
        Log.d("InfoActivitiy","username  "+username);

        reserve.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calendar = Calendar.getInstance();
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                int month = calendar.get(Calendar.MONTH);
                int year = calendar.get(Calendar.YEAR);
                datePickerDialog = new DatePickerDialog(InfoActivity.this, R.style.my_dialog_theme, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        Log.d("Resource: ", "" + user_id);
                        runOnUiThread(() -> {
                            try {
                                Thread.sleep(3000);
                                Toast.makeText(InfoActivity.this, "Please Wait", Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            processReservation(
                                    username,
                                    intent.getStringExtra("title"),
                                    year,
                                    month,
                                    dayOfMonth
                            );
                            Intent reserveActivity = new Intent(InfoActivity.this, ReservationCompleteActivity.class);
                            startActivity(reserveActivity);
                        });
                    }
                }, year, month, day);
                datePickerDialog.show();
            }
        });

        bookmarkButton.setOnClickListener(this::onBookmarkClick);
    }

    public void processReservation(String name, String bookTitle, int year, int month, int day) {
        String date = day + "," + month + "," + year;
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            try {
                conn = connection.CONN();
                String query = "INSERT INTO ReservationRecord (username, title, date) VALUES (\"" +
                        name + "\", \"" + bookTitle + "\", STR_TO_DATE(\"" + date + "\",\"%d,%m,%Y\"))";
                Log.d("Connection Error", query);
                PreparedStatement preparedStatement = conn.prepareStatement(query);
                preparedStatement.execute();
            } catch (SQLException e) {
                Log.d("Connection Error", "error");
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
                MySQLHelperBookmark.addBookmark(user_id, bookTitle,name);
                runOnUiThread(() -> Toast.makeText(InfoActivity.this, "Bookmark Saved", Toast.LENGTH_SHORT).show());
            } catch (SQLException e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(InfoActivity.this, "Error Saving Bookmark", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void openBookmarkFragment() {

            BookmarkFragment fragment = new BookmarkFragment();
            FragmentManager fragmentManager = getSupportFragmentManager(); // Or getFragmentManager() if not using support library
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.activityInfo, fragment); // R.id.fragment_container is the ID of the layout container where the fragment should be placed
            transaction.addToBackStack(null); // Optional, adds the transaction to the back stack
            transaction.commit();

    }
}