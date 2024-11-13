package com.example.owlagenda.ui.homepage;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.owlagenda.R;

import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;
import androidx.appcompat.app.AppCompatActivity;

import com.example.owlagenda.data.models.TaskViewModel;
import com.example.owlagenda.data.models.UserViewModel;
import com.example.owlagenda.databinding.ActivityHomePageBinding;
import com.example.owlagenda.ui.homescreen.HomeScreenView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class HomePageView extends AppCompatActivity {
    public ActivityHomePageBinding binding;
    private HomePageViewModel viewModel;
    private UserViewModel userViewModel;
    private TaskViewModel taskViewModel;
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = new ViewModelProvider(this).get(HomePageViewModel.class);
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        taskViewModel = new ViewModelProvider(this).get(TaskViewModel.class);

        binding = ActivityHomePageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel.getUser(FirebaseAuth.getInstance().getCurrentUser().getUid()).observe(this, user -> {
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                FirebaseAuth.getInstance().getCurrentUser().reload().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        userViewModel.setUser(user);
                        if (FirebaseAuth.getInstance().getCurrentUser() != null
                                && FirebaseAuth.getInstance().getCurrentUser().getEmail() != null) {
                            if (!FirebaseAuth.getInstance().getCurrentUser().getEmail().equalsIgnoreCase(user.getEmail())) {
                                viewModel.updateEmail(FirebaseAuth.getInstance().getCurrentUser().getUid(), FirebaseAuth
                                        .getInstance().getCurrentUser().getEmail()).observe(this, aBoolean -> {
                                    if (aBoolean) {
                                        Toast.makeText(this, "Email atualizado com sucesso.", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(this, "Erro ao atualizar o email.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }
                    } else {
                        Toast.makeText(this, "Erro ao carregar informações do usuário.", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                goHomeScreen();
            }
        });

        viewModel.getMessages(FirebaseAuth.getInstance().getCurrentUser().getUid()).observe(this,
                messages -> {
                    if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                        FirebaseAuth.getInstance().getCurrentUser().reload().addOnCompleteListener(task ->
                                userViewModel.setMessages(messages));
                    } else {
                        goHomeScreen();
                    }
                });

        viewModel.getErrorMessage().observe(this, s ->
                runOnUiThread(() -> Toast.makeText(this, s, Toast.LENGTH_SHORT).show()));

        viewModel.getTasks(FirebaseAuth.getInstance().getCurrentUser().getUid()).observe(this, tasks -> {
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                FirebaseAuth.getInstance().getCurrentUser().reload().addOnCompleteListener(task ->
                        taskViewModel.setTask(tasks));
            } else {
                goHomeScreen();
            }
        });

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_tela_principal);
        NavigationUI.setupWithNavController(binding.bottomNavigationView.bottomNavigationView, navController);

        auth = FirebaseAuth.getInstance();

        authStateListener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user != null) {
                Log.d("AuthState", "Usuário logado.");
            } else {
                Log.d("AuthState", "Usuário deslogado. O email pode ter mudado.");
                goHomeScreen();
            }
        };
        auth.addAuthStateListener(authStateListener);
    }

    private void goHomeScreen() {
        Intent intent = new Intent(this, HomeScreenView.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Limpa a pilha
        startActivity(intent);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        auth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        auth.removeAuthStateListener(authStateListener);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}


