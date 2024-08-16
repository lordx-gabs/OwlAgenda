package com.example.owlagenda.ui.homescreen;

import android.content.Intent;
import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.owlagenda.R;
import com.example.owlagenda.databinding.ActivityHomeScreenBinding;
import com.example.owlagenda.ui.login.LoginView;
import com.example.owlagenda.ui.register.RegisterView;

public class HomeScreenView extends AppCompatActivity {
    private ActivityHomeScreenBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityHomeScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnRegisterHomescreen.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterView.class));
            finish();
        });

        binding.btnLoginHomescreen.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginView.class));
            finish();
        });
    }
}