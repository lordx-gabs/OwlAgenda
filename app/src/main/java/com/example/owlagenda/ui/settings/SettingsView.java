package com.example.owlagenda.ui.settings;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.owlagenda.R;
import com.example.owlagenda.util.SharedPreferencesUtil;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class SettingsView extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_view);

        // Referência ao botão "Tema" (LinearLayout)
        LinearLayout temaButton = findViewById(R.id.button4);

        SharedPreferencesUtil.init(this);

        // Definindo o OnClickListener para mostrar o MaterialAlertDialog
        temaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Opções de tema
                String[] temas = {"Claro", "Escuro"};

                // Cria o Material AlertDialog simples
                new MaterialAlertDialogBuilder(SettingsView.this)
                        .setTitle("Escolha o tema")
                        .setItems(temas, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Responder à escolha do item
                                if (which == 0) {

                                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                                    onThemeChange();
                                    Toast.makeText(getApplicationContext(), "Tema Claro selecionado", Toast.LENGTH_SHORT).show();

                                } else if (which == 1) {

                                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                                    onThemeChange();
                                    Toast.makeText(getApplicationContext(), "Tema Escuro selecionado", Toast.LENGTH_SHORT).show();

                                }
                            }
                        })
                        .show();
            }
        });
    }

    public void onThemeChange(){
            recreate();
    }
}