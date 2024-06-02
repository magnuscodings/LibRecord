package com.example.librecord;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReservationHistoryFragment extends Fragment {

    private LinearLayout reservationContainer;
    private ConstraintLayout constraintLayout;
    private ExecutorService executorService;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy");

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reservation, container, false);

        reservationContainer = view.findViewById(R.id.constraintLayout);

        executorService = Executors.newSingleThreadExecutor();

        loadReservations();

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }

    private void loadReservations() {
        executorService.execute(() -> {
            List<Reservation> reservations = fetchReservationsFromDatabase();

            if (!reservations.isEmpty() && getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    for (Reservation reservation : reservations) {
                        View reservationView = createReservationView(reservation);
                        reservationContainer.addView(reservationView);
                    }
                });
            }
        });
    }

    private List<Reservation> fetchReservationsFromDatabase() {
        List<Reservation> reservations = new ArrayList<>();
        try (Connection connection = Connect.CONN();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT bookreserved, date FROM reservationhistory")) {

            while (resultSet.next()) {
                String bookReserved = resultSet.getString("bookreserved");
                Date reservationDate = resultSet.getDate("date");
                Date returnDate = calculateReturnDate(reservationDate);
                reservations.add(new Reservation(bookReserved, reservationDate, returnDate));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle the exception or log it
        }
        return reservations;
    }

    private Date calculateReturnDate(Date reservationDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(reservationDate);
        calendar.add(Calendar.DAY_OF_YEAR, 3);
        return calendar.getTime();
    }

    private View createReservationView(Reservation reservation) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.item_reservation, reservationContainer, false);

        TextView bookReservedTextView = view.findViewById(R.id.book_reserved);
        TextView reservationDateTextView = view.findViewById(R.id.reservation_date);
        TextView returnDateTextView = view.findViewById(R.id.return_date);

        bookReservedTextView.setText(reservation.getBookReserved());
        reservationDateTextView.setText(dateFormat.format(reservation.getReservationDate()));
        returnDateTextView.setText(dateFormat.format(reservation.getReturnDate()));

        return view;
    }

    private static class Reservation {
        private final String bookReserved;
        private final Date reservationDate;
        private final Date returnDate;

        public Reservation(String bookReserved, Date reservationDate, Date returnDate) {
            this.bookReserved = bookReserved;
            this.reservationDate = reservationDate;
            this.returnDate = returnDate;
        }

        public String getBookReserved() {
            return bookReserved;
        }

        public Date getReservationDate() {
            return reservationDate;
        }

        public Date getReturnDate() {
            return returnDate;
        }
    }
}

