package com.example.owlagenda.ui.updatetask;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.owlagenda.R;
import com.example.owlagenda.databinding.ActivityUpdateTaskViewBinding;

public class UpdateTaskView extends AppCompatActivity {
    private ActivityUpdateTaskViewBinding binding;
    private String taskId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityUpdateTaskViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        taskId = getIntent().getStringExtra("taskId");


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}