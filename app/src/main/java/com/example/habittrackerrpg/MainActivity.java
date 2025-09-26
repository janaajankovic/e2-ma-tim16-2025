package com.example.habittrackerrpg;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.habittrackerrpg.ui.auth.AuthViewModel;
import com.example.habittrackerrpg.ui.auth.AuthenticationActivity;

public class MainActivity extends AppCompatActivity {

    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        Button logoutBtn = findViewById(R.id.button_logout);
        logoutBtn.setOnClickListener(v -> {
            authViewModel.logoutUser();
            startActivity(new Intent(MainActivity.this, AuthenticationActivity.class));
            finish();
        });
    }
}