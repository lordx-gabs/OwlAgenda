package com.example.owlagenda.ui.telaprincipal;

import android.os.Bundle;

import com.example.owlagenda.R;

import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;
import androidx.appcompat.app.AppCompatActivity;

import com.example.owlagenda.data.models.User;
import com.example.owlagenda.data.models.UserViewModel;
import com.example.owlagenda.databinding.ActivityTelaPrincipalBinding;
import com.google.firebase.auth.FirebaseAuth;

public class TelaPrincipalView extends AppCompatActivity {
    public ActivityTelaPrincipalBinding binding;
    private NavController navController;
    private TelaPrincipalViewModel viewModel;
    private UserViewModel userViewModel;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = new ViewModelProvider(this).get(TelaPrincipalViewModel.class);
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        viewModel.getUser(FirebaseAuth.getInstance().getCurrentUser().getUid()).observe(this, user -> {
                userViewModel.setUser(user);
                currentUser = user;
        });
        viewModel.getMessages(FirebaseAuth.getInstance().getCurrentUser().getUid()).observe(this, messages -> {
            userViewModel.setMessages(messages);
        });

        binding = ActivityTelaPrincipalBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        navController = Navigation.findNavController(this, R.id.nav_host_fragment_tela_principal);
        NavigationUI.setupWithNavController(binding.bottomNavigationView.bottomNavigationView, navController);


        // finishAffinity();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}


