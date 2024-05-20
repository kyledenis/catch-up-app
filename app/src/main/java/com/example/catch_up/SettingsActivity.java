package com.example.catch_up;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class SettingsActivity extends AppCompatActivity {
    FirebaseAuth auth;
    Button Logout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        auth = FirebaseAuth.getInstance();
        Logout = findViewById(R.id.Logout);

        Logout.setOnClickListener(v -> {
            auth.signOut();
            Intent intent = new Intent(SettingsActivity.this, Login.class);
            startActivity(intent);
            finish();
        });
    }
}