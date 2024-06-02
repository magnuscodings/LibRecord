package com.example.librecord;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageButton;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.example.librecord.Connect;
import com.example.librecord.R;
import com.example.librecord.ReservationAdapter;
import com.example.librecord.ReservationRecord;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReservationFragment extends Fragment {
    View view;
    ImageButton profileButton1;
    GridView gridView;
    ReservationAdapter reservationAdapter;
    List<ReservationRecord> reservationList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_reservation, container, false);

        gridView = view.findViewById(R.id.gridView);
        profileButton1 = view.findViewById(R.id.reservation_profileButton);
        profileButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DrawerLayout drawerLayout = view.getRootView().findViewById(R.id.drawerLayout);
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        reservationAdapter = new ReservationAdapter(getContext(), reservationList);
        gridView.setAdapter(reservationAdapter);

        fetchReservations();

        return view;
    }

    private void fetchReservations() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            try {
                Connection conn = Connect.CONN(); // Adjust your connection method
                String query = "SELECT * FROM `reservationrecord`";
                PreparedStatement preparedStatement = conn.prepareStatement(query);
                ResultSet rs = preparedStatement.executeQuery();

                while (rs.next()) {
                    int id = rs.getInt("record_id");
                    String name = rs.getString("username");
                    String details = rs.getString("title");
                    String status = rs.getString("status");
                    String date = rs.getString("date");

                    reservationList.add(new ReservationRecord(id, name, details,status,date));
                }

                getActivity().runOnUiThread(() -> reservationAdapter.notifyDataSetChanged());

            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }
}
