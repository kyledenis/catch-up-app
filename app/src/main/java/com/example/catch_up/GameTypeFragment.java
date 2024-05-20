package com.example.catch_up;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

public class GameTypeFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_game_type, container, false);

        Button listOptionButton = view.findViewById(R.id.list_option_button);
        listOptionButton.setOnClickListener(v -> {
            // Handle list option selection
        });

        Button locationOptionButton = view.findViewById(R.id.location_option_button);
        locationOptionButton.setOnClickListener(v -> {
            // Handle location option selection
        });

        Button doneButton = view.findViewById(R.id.done_button);
        doneButton.setOnClickListener(v -> {
            // Handle done button click
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        return view;
    }
}
