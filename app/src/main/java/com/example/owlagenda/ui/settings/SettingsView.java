package com.example.owlagenda.ui.settings;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.owlagenda.R;
import com.example.owlagenda.databinding.ActivitySettingsViewBinding;
import com.example.owlagenda.ui.telaprincipal.TelaPrincipalView;
import com.example.owlagenda.util.SharedPreferencesUtil;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class SettingsView extends AppCompatActivity {
    private final String[] themes = {"Claro", "Escuro"};
    private ActivitySettingsViewBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SharedPreferencesUtil.init(this);

        binding.linearLayoutTheme.setOnClickListener(v ->
                new MaterialAlertDialogBuilder(SettingsView.this)
                .setTitle("Escolha o tema")
                .setItems(themes, (dialog, which) -> {
                    if (which == 0) {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                        onThemeChange();
                        SharedPreferencesUtil.saveInt(SharedPreferencesUtil.KEY_USER_THEME,
                                AppCompatDelegate.MODE_NIGHT_NO);
                        Toast.makeText(getApplicationContext(), "Tema Claro selecionado",
                                Toast.LENGTH_SHORT).show();

                    } else if (which == 1) {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                        onThemeChange();
                        SharedPreferencesUtil.saveInt(SharedPreferencesUtil.KEY_USER_THEME,
                                AppCompatDelegate.MODE_NIGHT_YES);
                        Toast.makeText(getApplicationContext(), "Tema Escuro selecionado",
                                Toast.LENGTH_SHORT).show();

                    }
                })
                .show());

        binding.materialToolbar.setNavigationOnClickListener(v ->
                getOnBackPressedDispatcher().onBackPressed());

        getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                startActivity(new Intent(getApplicationContext(), TelaPrincipalView.class));
                finish();
            }
        });
    }

    public void onThemeChange() {
        recreate();
    }

}