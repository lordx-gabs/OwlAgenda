package com.example.owlagenda.ui.inicio;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.owlagenda.R;
import com.example.owlagenda.data.models.Task;
import com.example.owlagenda.data.models.TaskDay;
import com.example.owlagenda.data.models.User;
import com.example.owlagenda.data.models.UserViewModel;
import com.example.owlagenda.databinding.FragmentInicioBinding;
import com.example.owlagenda.ui.settings.SettingsView;
import com.example.owlagenda.ui.task.TaskView;
import com.example.owlagenda.util.NotificationUtil;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class InicioFragment extends Fragment {
    private InicioViewModel inicioViewModel;
    private FragmentInicioBinding binding;
    private UserViewModel userViewModel;
    private ArrayList<TaskDay> tasksDay;
    private ArrayList<Task> tasks;
    private User currentUser;
    private TaskDayAdapter adapter;
    List<com.google.android.gms.tasks.Task<Void>> tasksSchool = new ArrayList<>(); // Lista de tarefas Firestore para controle
    List<com.google.android.gms.tasks.Task<Void>> firestoreTasks = new ArrayList<>(); // Lista de tarefas Firestore para controle

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        inicioViewModel = new ViewModelProvider(this).get(InicioViewModel.class);

        binding = FragmentInicioBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
        userViewModel.getUser().observe(getViewLifecycleOwner(), user -> {
            currentUser = user;
            binding.tvNameTeacher.setText("Olá professor(a) " + currentUser.getName() + "!");
        });

        tasksDay = new ArrayList<>();
        binding.recycleTaskDay.setLayoutManager(new LinearLayoutManager(requireContext(),
                LinearLayoutManager.VERTICAL, false));

        inicioViewModel.isLoading().observe(getViewLifecycleOwner(), aBoolean -> {
            if (aBoolean) {
                binding.loadingHome.setVisibility(View.VISIBLE);
            } else {
                binding.loadingHome.setVisibility(View.GONE);
            }
        });


        inicioViewModel.getTasksByNotCompleted(FirebaseAuth.getInstance()
                .getCurrentUser().getUid()).observe(getViewLifecycleOwner(), tasks -> {
            if (tasks != null) {
                tasksDay.clear();
                tasks.sort(Comparator.comparing(task ->
                        LocalDate.parse(task.getDate(), DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                ));
                if (!tasks.isEmpty()) {
                    binding.recycleTaskDay.setVisibility(View.VISIBLE);
                    binding.tvMessageNoTask.setVisibility(View.GONE);
                    binding.tvTaskDayTitle.setVisibility(View.VISIBLE);
                    binding.loadingHome.setVisibility(View.VISIBLE);
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
                                                                    Toast.makeText(getContext(), "Erro ao carregar escola", Toast.LENGTH_SHORT).show();
                                                                }
                                                            }
                                                    );
                                        } else {
                                            Toast.makeText(getContext(), "Erro ao carregar escolas", Toast.LENGTH_SHORT).show();
                                        }
                                        return null;
                                    }));
                                } else {
                                    Toast.makeText(getContext(), "Erro ao carregar classes", Toast.LENGTH_SHORT).show();
                                }
                            }
                            return null;
                        }));

                        Tasks.whenAllComplete(firestoreTasks).addOnCompleteListener(task7 -> {
                            if (task7.isSuccessful()) {
                                Tasks.whenAllComplete(taskSchool).addOnCompleteListener(task9 -> {
                                    binding.loadingHome.setVisibility(View.GONE);
                                    if (task9.isSuccessful()) {
                                        adapter = new TaskDayAdapter(tasksDay, new TaskDayViewHolder.onClickListener() {
                                            @Override
                                            public void onClickCheck(int position) {
                                                //marcar tarefa como concluida

                                                Optional<Task> taskActually = tasks.stream().filter(task -> task.getId()
                                                        .equalsIgnoreCase(tasksDay.get(position).getIdTaskDay())).findFirst();
                                                taskActually.orElse(null).setCompleted(true);

                                                inicioViewModel.setTaskIsCompleted(taskActually.orElse(null)).observe(getViewLifecycleOwner()
                                                        , aBoolean -> {
                                                            if (aBoolean) {
                                                                Snackbar snackbar = Snackbar.make(binding.getRoot(), "Tarefa marcada como concluida!",
                                                                                Snackbar.LENGTH_SHORT)
                                                                        .setAction("Desfazer", v -> {
                                                                            taskActually.orElse(null).setCompleted(false);
                                                                            inicioViewModel.setTaskIsCompleted(taskActually.orElse(null));
                                                                        }).setAnchorView(binding.appFab.getRoot());

                                                                snackbar.addCallback(new Snackbar.Callback() {
                                                                    @Override
                                                                    public void onDismissed(Snackbar snackbar, int event) {
                                                                        if (event == Snackbar.Callback.DISMISS_EVENT_TIMEOUT) {
                                                                            int notificationId = 0;
                                                                            try {
                                                                                notificationId = Integer.parseInt(taskActually.orElse(null).getId().replaceAll("[^0-9]", ""));
                                                                            } catch (NumberFormatException ignored) {}

                                                                            Log.d("teste", "" + notificationId);
                                                                            if (NotificationUtil.scheduleNotificationApp.isAlarmSet(InicioFragment.this.getActivity().getApplicationContext(), taskActually.orElse(null).getTitle(),
                                                                                    notificationId)) {
                                                                                NotificationUtil.scheduleNotificationApp.cancelNotification(InicioFragment.this.getActivity().getApplicationContext(), taskActually.orElse(null).getTitle(),
                                                                                        notificationId);
                                                                                Log.d("testeee", "chegouu");
                                                                            }
                                                                        }
                                                                    }
                                                                });

                                                                snackbar.show();
                                                            } else {
                                                                Toast.makeText(getContext(), "Erro ao marcar tarefa como concluida!",
                                                                        Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                            }

                                            @Override
                                            public void onClickTask(int position) {
                                                // detalhes da tarefa

                                            }
                                        });
                                        if (isAdded()) {
                                            binding.recycleTaskDay.setAdapter(adapter);
                                            binding.recycleTaskDay.getAdapter().notifyDataSetChanged();
                                        }
                                    } else {
                                        Toast.makeText(getContext(), "Erro ao carregar tarefas", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } else {
                                Toast.makeText(getContext(), "Erro ao carregar tarefas", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } else {
                    if (binding.recycleTaskDay.getAdapter() != null) {
                        binding.recycleTaskDay.getAdapter().notifyDataSetChanged();
                    }
                    binding.recycleTaskDay.setVisibility(View.GONE);
                    binding.tvMessageNoTask.setVisibility(View.VISIBLE);
                    binding.tvTaskDayTitle.setVisibility(View.GONE);
                }
            } else {
                Toast.makeText(getContext(), "Erro ao carregar tarefas", Toast.LENGTH_SHORT).show();
            }
        });

        binding.appFab.fab.setOnClickListener(v ->
                startActivity(new Intent(getActivity(), TaskView.class)));

        binding.appBarTelaPrincipal.toolbar.inflateMenu(R.menu.menu_overflow);

//        binding.btnTestee.setOnClickListener(v ->
//                startActivity(new Intent(getActivity(), Prova.class)));

        inicioViewModel.getErrorMessage().observe(getViewLifecycleOwner(), s ->
                Toast.makeText(getContext(), s, Toast.LENGTH_SHORT).show());

        binding.appBarTelaPrincipal.toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_settings) {
                startActivity(new Intent(getContext(), SettingsView.class));
                requireActivity().finish();
                return true;
            }
            return false;

        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}