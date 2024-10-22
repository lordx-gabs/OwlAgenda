package com.example.owlagenda.ui.telaprincipal;

import android.os.Bundle;
import android.widget.Toast;

import com.example.owlagenda.R;

import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;
import androidx.appcompat.app.AppCompatActivity;

import com.example.owlagenda.data.models.TaskViewModel;
import com.example.owlagenda.data.models.UserViewModel;
import com.example.owlagenda.databinding.ActivityTelaPrincipalBinding;
import com.google.firebase.auth.FirebaseAuth;

public class TelaPrincipalView extends AppCompatActivity {
    public ActivityTelaPrincipalBinding binding;
    private NavController navController;
    private TelaPrincipalViewModel viewModel;
    private UserViewModel userViewModel;
    private TaskViewModel taskViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = new ViewModelProvider(this).get(TelaPrincipalViewModel.class);
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        taskViewModel = new ViewModelProvider(this).get(TaskViewModel.class);

        viewModel.getUser(FirebaseAuth.getInstance().getCurrentUser().getUid()).observe(this, user ->
                userViewModel.setUser(user));

        viewModel.getMessages(FirebaseAuth.getInstance().getCurrentUser().getUid()).observe(this,
                messages -> userViewModel.setMessages(messages));

        viewModel.getErrorMessage().observe(this, s ->
                runOnUiThread(() -> Toast.makeText(this, s, Toast.LENGTH_SHORT).show()));

        viewModel.getTasks(FirebaseAuth.getInstance().getCurrentUser().getUid()).observe(this, tasks ->
                taskViewModel.setTask(tasks));

        binding = ActivityTelaPrincipalBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        navController = Navigation.findNavController(this, R.id.nav_host_fragment_tela_principal);
        NavigationUI.setupWithNavController(binding.bottomNavigationView.bottomNavigationView, navController);


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}


