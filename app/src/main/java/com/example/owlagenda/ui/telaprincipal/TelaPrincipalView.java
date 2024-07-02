package com.example.owlagenda.ui.telaprincipal;

import android.os.Bundle;

import com.example.owlagenda.R;

import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;
import androidx.appcompat.app.AppCompatActivity;

import com.example.owlagenda.databinding.ActivityTelaPrincipalBinding;

public class TelaPrincipalView extends AppCompatActivity {
    private ActivityTelaPrincipalBinding binding;
    private NavController navController;
    private TelaPrincipalViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = new ViewModelProvider(this).get(TelaPrincipalViewModel.class);

        binding = ActivityTelaPrincipalBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // Encontre o NavController
        navController = Navigation.findNavController(this, R.id.nav_host_fragment_tela_principal);

        // Configure a visualização de navegação inferior com o NavController
        NavigationUI.setupWithNavController(binding.bottomNavigationView.bottomNavigationView, navController);

        binding.appFab.fab.setOnClickListener(v -> {

        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}


