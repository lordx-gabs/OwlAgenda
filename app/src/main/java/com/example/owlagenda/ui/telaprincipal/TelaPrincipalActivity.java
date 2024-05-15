package com.example.owlagenda.ui.telaprincipal;

import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

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
        // Encontre o NavController
        navController = Navigation.findNavController(this, R.id.nav_host_fragment_tela_principal);

        // Configure a visualização de navegação inferior com o NavController
        NavigationUI.setupWithNavController(binding.bottomNavigationView.bottomNavigationView, navController);

        binding.appFab.fab.setOnClickListener(v -> {
            int areItemsVisible = binding.menuFab.layoutMenuOptions.getVisibility();

            // Animar o FAB para baixo se os itens estiverem visíveis
            if (areItemsVisible == View.VISIBLE) {
                // Para fechar o menu
                Animation closeMenuAnim = AnimationUtils.loadAnimation(this, R.anim.slide_up);
                binding.menuFab.layoutMenuOptions.startAnimation(closeMenuAnim);
                binding.menuFab.layoutMenuOptions.setVisibility(View.GONE);
            } else {
                // Animar o FAB para cima se os itens não estiverem visíveis
                Animation openMenuAnim = AnimationUtils.loadAnimation(this, R.anim.slide_down);
                binding.menuFab.layoutMenuOptions.startAnimation(openMenuAnim);
                binding.menuFab.layoutMenuOptions.setVisibility(View.VISIBLE);
            }

        });
    }
}

