package com.example.librecord;

import android.os.Bundle;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;


public class ReservationFragment extends Fragment {
    View view;
    ImageButton profileButton1;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_reservation, container, false);

        profileButton1 = (ImageButton) view.findViewById(R.id.reservation_profileButton);
        profileButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DrawerLayout drawerLayout = view.getRootView().findViewById(R.id.drawerLayout);

                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
        return view;
    }
}
