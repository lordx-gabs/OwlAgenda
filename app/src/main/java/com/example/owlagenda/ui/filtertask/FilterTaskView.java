package com.example.owlagenda.ui.filtertask;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.owlagenda.data.models.Task;
import com.example.owlagenda.data.models.TaskDay;
import com.example.owlagenda.databinding.ActivityFilterTaskViewBinding;
import com.example.owlagenda.ui.taskdetails.TaskDetailsView;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.search.SearchView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class FilterTaskView extends AppCompatActivity {

    private ActivityFilterTaskViewBinding binding;
    private FilterTaskViewModel viewModel;
    List<com.google.android.gms.tasks.Task<Void>> tasksSchool = new ArrayList<>(); // Lista de tarefas Firestore para controle
    List<com.google.android.gms.tasks.Task<Void>> firestoreTasks = new ArrayList<>(); // Lista de tarefas Firestore para controle
    private FilterTaskAdapter adapter;
    private FilterTaskAdapter adapterSuggestions;
    private ArrayList<TaskDay> tasksSuggestions;
    private ArrayList<TaskDay> tasksDay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityFilterTaskViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        viewModel = new ViewModelProvider(this).get(FilterTaskViewModel.class);

        viewModel.getIsLoading().observe(this, aBoolean -> {
            if (aBoolean) {
                binding.loadingFilterTask.setVisibility(View.VISIBLE);
            } else {
                binding.loadingFilterTask.setVisibility(View.GONE);
            }
        });

        tasksDay = new ArrayList<>();
        binding.recycleFilterTask.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false));

        viewModel.getAllTask(FirebaseAuth.getInstance()
                .getCurrentUser().getUid()).observe(this, tasks -> {
            if (tasks != null) {
                tasksDay.clear();
                tasks.sort(Comparator.comparing(task ->
                        LocalDate.parse(task.getDate(), DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                ));
                if (!tasks.isEmpty()) {
                    binding.tvFilterTaskMessageNoTask.setVisibility(View.GONE);
                    binding.loadingFilterTask.setVisibility(View.VISIBLE);
                    for (Task task : tasks) {
                        com.google.android.gms.tasks.Task<DocumentSnapshot> taskSchool = task.getSchool().get();
                        com.google.android.gms.tasks.Task<DocumentSnapshot> classTasks = task.getSchoolClass().get();

                        // Adicionar à lista de tarefas Firestore para esperar todas
                        firestoreTasks.add(classTasks.continueWith(task1 -> {
                            if (task1.isSuccessful()) {
                                DocumentSnapshot document = task1.getResult();
                                if (document.exists()) {
                                    // Adicionar uma nova TaskDay à lista
                                    tasksSchool.add(taskSchool.continueWith(task4 -> {
                                        if (task4.isSuccessful()) {
                                            document.getDocumentReference("schoolId").get()
                                                    .addOnCompleteListener(task2 -> {
                                                                if (task2.isSuccessful()) {
                                                                    tasksDay.add(new TaskDay(
                                                                            task.getId(),
                                                                            task.getTitle(),
                                                                            document.getString("className"),
                                                                            task.getTag(),
                                                                            task.getDate(),
                                                                            task.isCompleted(),
                                                                            task2.getResult().getString("schoolName")
                                                                    ));
                                                                    adapter.notifyDataSetChanged();
                                                                } else {
                                                                    Toast.makeText(this, "Erro ao carregar escola", Toast.LENGTH_SHORT).show();
                                                                }
                                                            }
                                                    );
                                        } else {
                                            Toast.makeText(this, "Erro ao carregar escolas", Toast.LENGTH_SHORT).show();
                                        }
                                        return null;
                                    }));
                                } else {
                                    Toast.makeText(this, "Erro ao carregar classes", Toast.LENGTH_SHORT).show();
                                }
                            }
                            return null;
                        }));

                        Tasks.whenAllComplete(firestoreTasks).addOnCompleteListener(task7 -> {
                            if (task7.isSuccessful()) {
                                Tasks.whenAllComplete(taskSchool).addOnCompleteListener(task9 -> {
                                    binding.loadingFilterTask.setVisibility(View.GONE);
                                    if (task9.isSuccessful()) {
                                        adapter = new FilterTaskAdapter(tasksDay, position -> {
                                            Intent intent = new Intent(FilterTaskView.this, TaskDetailsView.class);
                                            intent.putExtra("taskId", tasksDay.get(position).getIdTaskDay());
                                            startActivity(intent);
                                        });

                                        binding.recycleFilterTask.setAdapter(adapter);
                                        binding.recycleFilterTask.getAdapter().notifyDataSetChanged();

                                    } else {
                                        Toast.makeText(this, "Erro ao carregar tarefas", Toast.LENGTH_SHORT).show();
                                    }

                                });
                            } else {
                                Toast.makeText(this, "Erro ao carregar tarefas", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } else {
                    if (binding.recycleFilterTask.getAdapter() != null) {
                        binding.recycleFilterTask.getAdapter().notifyDataSetChanged();
                    }
                    binding.recycleFilterTask.setVisibility(View.GONE);
                    binding.tvFilterTaskMessageNoTask.setVisibility(View.VISIBLE);
                    binding.tvFilterTaskTitle.setVisibility(View.GONE);
                }
            } else {
                Toast.makeText(this, "Erro ao carregar tarefas", Toast.LENGTH_SHORT).show();
            }
        });

        binding.materialToolbar2.setNavigationOnClickListener(v ->
                getOnBackPressedDispatcher().onBackPressed());

        binding.searchBar.setOnClickListener(v -> binding.searchView.show());

        tasksSuggestions = new ArrayList<>();
        adapterSuggestions = new FilterTaskAdapter(tasksSuggestions, position -> {
            Intent intent = new Intent(FilterTaskView.this, TaskDetailsView.class);
            intent.putExtra("taskId", tasksSuggestions.get(position).getIdTaskDay());
            startActivity(intent);
            binding.searchView.hide();
        });

        binding.recyclerSuggestions.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false));

        binding.recyclerSuggestions.setAdapter(adapterSuggestions);

        // Configura o listener de busca
        binding.searchView.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                tasksSuggestions.clear();
                Log.d("teste", binding.searchView.getText().toString());
                if (!s.toString().isEmpty()) {
                    tasksDay.stream().filter(task -> task.getTitleTaskDay().toLowerCase().contains(binding.searchView
                            .getText().toString().toLowerCase())).forEach(tasksSuggestions::add);
                }
                if (tasksSuggestions.isEmpty()) {
                    binding.recyclerSuggestions.setVisibility(View.GONE);
                    binding.tvNoSuggestions.setVisibility(View.VISIBLE);
                } else {
                    binding.recyclerSuggestions.setVisibility(View.VISIBLE);
                    binding.tvNoSuggestions.setVisibility(View.GONE);
                }
                adapterSuggestions.notifyDataSetChanged();
            }
        });

        // Fecha o SearchView ao clicar no botão de navegação
        binding.searchBar.setNavigationOnClickListener(v -> binding.searchView.show());

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
