package com.example.owlagenda.ui.telaprincipal;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.owlagenda.R;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;
import androidx.appcompat.app.AppCompatActivity;

import com.example.owlagenda.data.models.Task;
import com.example.owlagenda.data.models.TaskViewModel;
import com.example.owlagenda.data.models.User;
import com.example.owlagenda.data.models.UserViewModel;
import com.example.owlagenda.databinding.ActivityTelaPrincipalBinding;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class TelaPrincipalView extends AppCompatActivity {
    public ActivityTelaPrincipalBinding binding;
    private NavController navController;
    private TelaPrincipalViewModel viewModel;
    private UserViewModel userViewModel;
    private TaskViewModel taskViewModel;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = new ViewModelProvider(this).get(TelaPrincipalViewModel.class);
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        taskViewModel = new ViewModelProvider(this).get(TaskViewModel.class);

        viewModel.getUser(FirebaseAuth.getInstance().getCurrentUser().getUid()).observe(this, user -> {
            userViewModel.setUser(user);
            currentUser = user;
        });
        viewModel.getMessages(FirebaseAuth.getInstance().getCurrentUser().getUid()).observe(this,
                messages -> userViewModel.setMessages(messages));

        viewModel.getErrorMessage().observe(this, s ->
                runOnUiThread(() -> Toast.makeText(this, s, Toast.LENGTH_SHORT).show()));

        viewModel.getTasks(FirebaseAuth.getInstance().getCurrentUser().getUid()).observe(this, tasks -> {
            taskViewModel.setTask(tasks);
            Log.e("teste", tasks.toString());
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


