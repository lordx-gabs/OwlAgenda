package com.example.owlagenda.ui.telaprincipal;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.Menu;
import android.widget.PopupMenu;

import com.example.owlagenda.R;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;
import androidx.appcompat.app.AppCompatActivity;

import com.example.owlagenda.databinding.ActivityTelaPrincipalBinding;

public class TelaPrincipalActivity extends AppCompatActivity {

    private ActivityTelaPrincipalBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityTelaPrincipalBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarTelaPrincipal.toolbar);
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
                } else if (id == R.id.action_option3) {
                    // Lógica para a opção 3
                    return true;
                } else {
                    return false;
                }
            });

            popupMenu.show();
        });

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_tela_principal);
        NavigationUI.setupWithNavController(binding.bottomNavigationView.bottomNavigationView, navController);

        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.tela_principal, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            // Lógica para ação de configurações
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}