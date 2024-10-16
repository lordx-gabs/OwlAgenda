package com.example.owlagenda.ui.resetemail;

import android.os.Bundle;
import android.os.PatternMatcher;
import android.util.Patterns;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.owlagenda.R;
import com.example.owlagenda.data.models.User;
import com.example.owlagenda.data.models.UserViewModel;
import com.example.owlagenda.databinding.ActivityResetEmailBinding;

public class ResetEmail extends AppCompatActivity {
    private UserViewModel userViewModel;
    private UpdateEmailViewModel viewModel;
    private ActivityResetEmailBinding binding;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityResetEmailBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        viewModel = new ViewModelProvider(this).get(UpdateEmailViewModel.class);

        userViewModel.getUser().observe(this, user -> {
            binding.etResetCurrentEmail.setText(user.getEmail());
            currentUser = user;
        });

        binding.btnResetEmail.setOnClickListener(v -> {
            if(binding.etResetNewEmail.getText().toString().isEmpty()) {
                Toast.makeText(this, "Insira um email novo.", Toast.LENGTH_SHORT).show();
            } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.etResetNewEmail.getText()).matches()) {
                Toast.makeText(this, "Insira um email válido.", Toast.LENGTH_SHORT).show();
            } else {
                viewModel.updateEmail(binding.etResetNewEmail.getText().toString()).observe(this, aBoolean -> {
                    if(aBoolean) {
                        binding.textResetEmail.setText("Email de verificação enviado para esse email, siga as intruções do email para" +
                                "atualizar seu email.");
                    } else {
                        Toast.makeText(this, "Erro ao enviar email de verificação", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }


}