package com.example.owlagenda.ui.telaprincipal;

import android.os.Bundle;
import android.widget.PopupMenu;

import com.example.owlagenda.R;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;
import androidx.appcompat.app.AppCompatActivity;

import com.example.owlagenda.databinding.ActivityTelaPrincipalBinding;

public class TelaPrincipalActivity extends AppCompatActivity {

    private ActivityTelaPrincipalBinding binding;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityTelaPrincipalBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.appFab.fab.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(this, binding.appFab.fab);
            popupMenu.getMenuInflater().inflate(R.menu.menu_fab, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.action_option1) {
                    // Lógica para a opção 1
                    return true;
                } else if (id == R.id.action_option2) {
                    // Lógica para a opção 2
                    return true;
                } else {
                    return false;
                }
            });

            popupMenu.show();
        });

        // Encontre o NavController
        navController = Navigation.findNavController(this, R.id.nav_host_fragment_tela_principal);

        // Configure a visualização de navegação inferior com o NavController
        NavigationUI.setupWithNavController(binding.bottomNavigationView.bottomNavigationView, navController);

    }



}