package com.example.owlagenda.ui.calendar;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.owlagenda.R;
import com.example.owlagenda.data.models.Task;
import com.example.owlagenda.data.models.User;
import com.example.owlagenda.data.models.UserViewModel;
import com.example.owlagenda.databinding.CalendarDayBinding;
import com.example.owlagenda.databinding.CalendarHeaderBinding;
import com.example.owlagenda.databinding.FragmentCalendarBinding;
import com.example.owlagenda.ui.aboutus.AboutUsView;
import com.example.owlagenda.ui.task.TaskView;
import com.example.owlagenda.ui.taskdetails.TaskDetailsView;
import com.example.owlagenda.ui.updatetask.UpdateTaskView;
import com.example.owlagenda.util.NotificationUtil;
import com.example.owlagenda.util.SharedPreferencesUtil;
import com.example.owlagenda.util.TaskTypeColor;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentSnapshot;
import com.kizitonwose.calendar.core.CalendarDay;
import com.kizitonwose.calendar.core.CalendarMonth;
import com.kizitonwose.calendar.core.DayPosition;
import com.kizitonwose.calendar.core.ExtensionsKt;
import com.kizitonwose.calendar.view.MonthDayBinder;
import com.kizitonwose.calendar.view.MonthHeaderFooterBinder;
import com.kizitonwose.calendar.view.ViewContainer;

import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.TimeZone;
import java.util.stream.Collectors;


import jp.wasabeef.recyclerview.animators.FadeInUpAnimator;

public class CalendarFragment extends Fragment {

    private FragmentCalendarBinding binding;
    private LocalDate selectedDate;
    private TaskAdapter taskAdapter;
    private HashMap<LocalDate, List<TaskCalendar>> tasks;
    private UserViewModel userViewModel;
    private CalendarViewModel viewModel;
    private ArrayList<TaskCalendar> tasksCalendar;
    private ArrayList<Task> tasksObject;
    private ActivityResultLauncher<String> requestCalendarPermissionLauncher;
    private final List<com.google.android.gms.tasks.Task<Void>> firestoreTasks = new ArrayList<>(); // Lista de tarefas Firestore para controle
    private User currentUser;
    private List<DayOfWeek> daysOfWeek;
    private ArrayList<TaskCalendar> taskCalendarUserCopy = new ArrayList<>();
    private Snackbar snackbar;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentCalendarBinding.inflate(inflater, container, false);

        binding.appBarTelaPrincipal.toolbar.inflateMenu(R.menu.menu_overflow);

        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);

        viewModel = new ViewModelProvider(this).get(CalendarViewModel.class);

        binding.recycleCalendar.setLayoutManager(new LinearLayoutManager(requireContext(),
                RecyclerView.VERTICAL,
                false));

        binding.appBarTelaPrincipal.toolbar.setTitle("Calendário");
        binding.appBarTelaPrincipal.titleOwl.setVisibility(View.GONE);

        binding.appFab.fab.setOnClickListener(v -> startActivity(new Intent(getActivity(), TaskView.class)));

        requestCalendarPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        loadCalendars();
                    } else {
                        Toast.makeText(getContext(), "Permissão necessária para acessar o armazenamento.", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        daysOfWeek = ExtensionsKt.daysOfWeek();
        YearMonth currentMonth = YearMonth.now();
        YearMonth startMonth = currentMonth.minusMonths(12);
        YearMonth endMonth = currentMonth.plusMonths(12);
        // chamar apos pegar as Tasks
        tasks = new HashMap<>();
        tasksCalendar = new ArrayList<>();
        tasksObject = new ArrayList<>();

        taskAdapter = new TaskAdapter(new TaskViewHolder.OnClickTask() {
            @Override
            public void onClickBtnEdit(int position) {
                List<TaskCalendar> task = tasks.get(selectedDate);
                Optional<Task> taskOptional = tasksObject.stream()
                        .filter(task7 -> task7.getId().equalsIgnoreCase(task.get(position).getId()))
                        .findFirst();
                if (taskOptional.isPresent()) {
                    Intent intentUpdateTask = new Intent(getActivity(), UpdateTaskView.class);
                    intentUpdateTask.putExtra("taskId", taskOptional.get().getId());
                    startActivity(intentUpdateTask);
                } else {
                    Toast.makeText(requireContext(), "Erro interno. Contante o suporte", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onClickBtnDelete(int position) {
                List<TaskCalendar> task = tasks.get(selectedDate);
                Optional<Task> taskOptional = tasksObject.stream()
                        .filter(task7 -> task7.getId().equalsIgnoreCase(task.get(position).getId()))
                        .findFirst();

                if (taskOptional.isPresent()) {
                    // Tarefa encontrada
                    viewModel.deleteTask(taskOptional.get())
                            .observe(getViewLifecycleOwner(), aBoolean -> {
                                if (aBoolean) {
                                    snackbar = Snackbar.make(binding.appFab.fab, "Tarefa Excluída",
                                            Snackbar.LENGTH_SHORT).setAction("Desfazer", v3 ->
                                            viewModel.addTask(taskOptional.get()).observe(getViewLifecycleOwner(),
                                                    aBoolean1 -> {
                                                        if (aBoolean1) {
                                                            Toast.makeText(getContext(), "Tarefa restaurada com sucesso", Toast.LENGTH_SHORT).show();
                                                        } else {
                                                            Toast.makeText(getContext(), "Falha ao restaurar tarefa", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                            )
                                    );

                                    snackbar.addCallback(new Snackbar.Callback() {
                                        @Override
                                        public void onDismissed(Snackbar snackbar, int event) {
                                            if (event == Snackbar.Callback.DISMISS_EVENT_TIMEOUT ||
                                                    event == Snackbar.Callback.DISMISS_EVENT_MANUAL ||
                                                    event == Snackbar.Callback.DISMISS_EVENT_SWIPE) {
                                                int notificationId = 0;
                                                try {
                                                    notificationId = Integer.parseInt(taskOptional.get().getId()
                                                            .replaceAll("[^0-9]", ""));
                                                } catch (NumberFormatException ignored) {
                                                }

                                                Log.d("teste", "" + notificationId);
                                                if (NotificationUtil.scheduleNotificationApp.isAlarmSet(requireActivity().getApplicationContext(),
                                                        taskOptional.get().getTitle(), notificationId)) {
                                                    NotificationUtil.scheduleNotificationApp
                                                            .cancelNotification(requireActivity().getApplicationContext(),
                                                                    taskOptional.get().getTitle(), notificationId);
                                                    Log.d("testeee", "chegouu");
                                                }
                                            }
                                            binding.loadingCalendar.setVisibility(View.GONE);
                                        }
                                    });

                                    snackbar.show();
                                } else {
                                    Toast.makeText(requireContext(), "Erro ao deletar tarefa", Toast.LENGTH_SHORT).show();
                                }
                                binding.loadingCalendar.setVisibility(View.GONE);
                            });
                } else {
                    // Tarefa não encontrada
                    Toast.makeText(requireContext(), "Erro interno. Contante o suporte", Toast.LENGTH_SHORT).show();
                    Log.d("teste", "Tarefa não encontrada.");
                }
            }

            @Override
            public void onClickBtnDetails(int position) {
                Intent intent = new Intent(getActivity(), TaskDetailsView.class);
                List<TaskCalendar> task = tasks.get(selectedDate);
                Optional<Task> taskOptional = tasksObject.stream()
                        .filter(task7 -> task7.getId().equalsIgnoreCase(task.get(position).getId()))
                        .findFirst();
                if (taskOptional.isPresent()) {
                    intent.putExtra("taskId", taskOptional.get().getId());
                    startActivity(intent);
                } else {
                    Log.d("teste", "Tarefa não encontrada.");
                }
            }
        });
        binding.recycleCalendar.setItemAnimator(new FadeInUpAnimator());
        binding.recycleCalendar.setAdapter(taskAdapter);
        if (isAdded()) {
            configureBinders(daysOfWeek);
            binding.calendar.setup(startMonth, endMonth, daysOfWeek.get(0));
            binding.calendar.scrollToMonth(YearMonth.now());
        }

        viewModel.getTasks().observe(getViewLifecycleOwner(), tasks -> {
            binding.loadingCalendar.setVisibility(View.VISIBLE);
            if (tasks != null) {
                if (!tasks.isEmpty()) {
                    Log.d("teste", "" + tasks.size());
                    tasksObject.clear();
                    tasksObject.addAll(tasks);
                    for (Task task : tasks) {
                        com.google.android.gms.tasks.Task<DocumentSnapshot> schoolTask = task.getSchoolClass().get();
                        tasksCalendar.clear();
                        // Adicionar à lista de tarefas Firestore para esperar todas
                        firestoreTasks.add(schoolTask.continueWith(task1 -> {
                            if (task1.isSuccessful()) {
                                DocumentSnapshot document = task1.getResult();
                                if (document.exists()) {
                                    tasksCalendar.add(new TaskCalendar(
                                            task.getId(),
                                            task.getTitle(),
                                            document.getString("className"),
                                            task.getDate(),
                                            task.getTag(),
                                            task.isCompleted()
                                    ));
                                }
                            }
                            return null;
                        }));

                        Tasks.whenAllComplete(firestoreTasks).addOnCompleteListener(task7 -> {
                            if (task7.isSuccessful()) {
                                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                                tasksCalendar.removeIf(TaskCalendar::isTypeCalendarUser);
                                tasksCalendar.addAll(taskCalendarUserCopy);
                                this.tasks.clear();
                                this.tasks.putAll(
                                        tasksCalendar.stream().collect(Collectors.groupingBy(task5 ->
                                                        LocalDate.parse(task5.getDate(), formatter),
                                                Collectors.toList()))
                                );
                                Log.e("teste", "" + this.tasks.size());

                                if (isAdded()) {
                                    if (selectedDate != null) {
                                        updateAdapterForDate(selectedDate);
                                    } else {
                                        updateAdapterForDate(null);
                                    }
                                    configureBinders(daysOfWeek);
                                    binding.loadingCalendar.setVisibility(View.GONE);
                                }
                            } else {
                                binding.loadingCalendar.setVisibility(View.GONE);
                                Toast.makeText(requireContext(), "Erro ao carregar as tarefas", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } else {
                    binding.loadingCalendar.setVisibility(View.GONE);
                    this.tasks.entrySet().removeIf(entry -> entry.getValue().stream().anyMatch(task -> !task.isTypeCalendarUser()));
                    if (isAdded()) {
                        if (selectedDate != null) {
                            updateAdapterForDate(selectedDate);
                        } else {
                            updateAdapterForDate(null);
                        }
                        configureBinders(daysOfWeek);
                    }
                }

            } else {
                binding.loadingCalendar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "Erro ao carregar as tarefas", Toast.LENGTH_SHORT).show();
            }
        });

        binding.calendar.setMonthScrollListener(calendarMonth -> {
            if (isAdded()) {
                binding.monthYearText.setText(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault()).format(calendarMonth.getYearMonth()));
                if (selectedDate != null) {
                    binding.calendar.notifyDateChanged(selectedDate);
                    selectedDate = null;
                    updateAdapterForDate(null);
                }

                return null;
            }
            return null;
        });

        binding.nextMonthImage.setOnClickListener(v -> {
            CalendarMonth firstVisibleMonth = binding.calendar.findFirstVisibleMonth();
            if (firstVisibleMonth != null) {
                binding.calendar.smoothScrollToMonth(ExtensionsKt.getNextMonth(firstVisibleMonth.getYearMonth()));
            }
        });

        binding.previousMouthImage.setOnClickListener(v -> {
            CalendarMonth firstVisibleMonth = binding.calendar.findFirstVisibleMonth();
            if (firstVisibleMonth != null) {
                binding.calendar.smoothScrollToMonth(ExtensionsKt.getPreviousMonth(firstVisibleMonth.getYearMonth()));
            }
        });

        int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;

        MenuItem themeItem = binding.appBarTelaPrincipal.toolbar.getMenu().findItem(R.id.action_theme);
        if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
            themeItem.setIcon(R.drawable.ic_theme_light);  // Ícone para o tema claro
        } else {
            themeItem.setIcon(R.drawable.ic_theme_dark);   // Ícone para o tema escuro
        }

        binding.appBarTelaPrincipal.toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_theme) {
                if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
                    // Mudar para o tema claro
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    item.setIcon(R.drawable.ic_theme_dark);  // Atualizar o ícone para tema escuro
                    SharedPreferencesUtil.saveInt(SharedPreferencesUtil.KEY_USER_THEME, AppCompatDelegate.MODE_NIGHT_NO);
                } else {
                    // Mudar para o tema escuro
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    item.setIcon(R.drawable.ic_theme_light);  // Atualizar o ícone para tema claro
                    SharedPreferencesUtil.saveInt(SharedPreferencesUtil.KEY_USER_THEME, AppCompatDelegate.MODE_NIGHT_YES);
                }

                requireActivity().recreate();
                return true;
            } else if (item.getItemId() == R.id.action_about_us) {
                startActivity(new Intent(getActivity(), AboutUsView.class));
                return true;
            }
            return false;
        });

        binding.btnCalendarAdd.setOnClickListener(v ->
                new MaterialAlertDialogBuilder(getContext())
                        .setTitle("Deseja adicionar as tarefas do seu calendário?")
                        .setMessage("Todas as suas tarefas do seu calendário serão mostradas aqui no Owl.")
                        .setPositiveButton("Sim", (dialogInterface, i) -> {
                            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
                                requestCalendarPermissionLauncher.launch(Manifest.permission.READ_CALENDAR);
                            } else {
                                loadCalendars();
                            }
                        })
                        .setNegativeButton("Não", (dialogInterface, i) -> dialogInterface.dismiss())
                        .show());


        userViewModel.getUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                currentUser = user;
                if (user.getTaskCalendarUser() != null && !user.getTaskCalendarUser().isEmpty()) {
                    tasksCalendar.removeIf(TaskCalendar::isTypeCalendarUser);
                    tasks.entrySet().removeIf(entry -> entry.getValue().stream().anyMatch(TaskCalendar::isTypeCalendarUser));

                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                    tasksCalendar.addAll(user.getTaskCalendarUser());
                    this.tasks.putAll(
                            tasksCalendar.stream().collect(Collectors.groupingBy(task5 ->
                                            LocalDate.parse(task5.getDate(), formatter),
                                    Collectors.toList()))
                    );
                    taskCalendarUserCopy = new ArrayList<>(user.getTaskCalendarUser());
                } else {
                    taskCalendarUserCopy.clear();
                    tasksCalendar.removeIf(TaskCalendar::isTypeCalendarUser);
                    tasks.entrySet().removeIf(entry -> entry.getValue().stream().anyMatch(TaskCalendar::isTypeCalendarUser));
                }
                Log.e("testeSizeTasks", "" + this.tasks.size());
                if (isAdded()) {
                    if (selectedDate != null) {
                        updateAdapterForDate(selectedDate);
                    } else {
                        updateAdapterForDate(null);
                    }
                    configureBinders(daysOfWeek);
                    binding.loadingCalendar.setVisibility(View.GONE);
                }
            } else {
                Toast.makeText(getContext(), "Erro ao carregar usuário", Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnRemoveCalendarUser.setOnClickListener(v -> {
            binding.loadingCalendar.setVisibility(View.VISIBLE);
            binding.btnRemoveCalendarUser.setEnabled(false);
            currentUser.setTaskCalendarUser(null);
            viewModel.updateUserTaskCalendar(currentUser).observe(getViewLifecycleOwner(), aBoolean -> {
                binding.btnRemoveCalendarUser.setEnabled(true);
                binding.loadingCalendar.setVisibility(View.GONE);
                if (aBoolean) {
                    Toast.makeText(getContext(), "Tarefas removidas com sucesso", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Erro ao remover tarefas", Toast.LENGTH_SHORT).show();
                }
            });
        });

        return binding.getRoot();
    }

    private void loadCalendars() {
        if (!taskCalendarUserCopy.isEmpty()) {
            Toast.makeText(getContext(), "Calendário já carregado", Toast.LENGTH_SHORT).show();
            return;
        }
        List<CalendarModel> calendars = new ArrayList<>();

        Uri uri = CalendarContract.Calendars.CONTENT_URI;
        String[] projection = new String[]{
                CalendarContract.Calendars._ID,
                CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
                CalendarContract.Calendars.ACCOUNT_NAME,
                CalendarContract.Calendars.ACCOUNT_TYPE
        };

        Cursor cursor = requireActivity().getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(CalendarContract.Calendars._ID));
                String displayName = cursor.getString(cursor.getColumnIndexOrThrow(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME));
                String accountName = cursor.getString(cursor.getColumnIndexOrThrow(CalendarContract.Calendars.ACCOUNT_NAME));
                String accountType = cursor.getString(cursor.getColumnIndexOrThrow(CalendarContract.Calendars.ACCOUNT_TYPE));

                calendars.add(new CalendarModel(id, displayName, accountName, accountType));
                Log.d("CalendarInfo", "ID: " + id + ", Nome: " + displayName + ", Conta: " + accountName + ", Tipo: " + accountType);
            }
            cursor.close();
        }

        String[] names = new String[calendars.size()];
        for (int i = 0; i < calendars.size(); i++) {
            names[i] = calendars.get(i).getDisplayName();
        }

        new MaterialAlertDialogBuilder(getContext())
                .setTitle("Selecione um calendário")
                .setItems(names, (dialogInterface, i) -> {
                    CalendarModel selectedCalendar = calendars.get(i);
                    Log.d("CalendarInfo", "ID: " + selectedCalendar.getId() + "nome" + selectedCalendar.getAccountName());
                    getCalendarTask(selectedCalendar.getId());
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void getCalendarTask(long calendarId) {
        binding.loadingCalendar.setVisibility(View.VISIBLE);
        binding.btnCalendarAdd.setEnabled(false);
        Uri uri = CalendarContract.Events.CONTENT_URI;

        String[] projection = new String[]{
                CalendarContract.Events.TITLE,
                CalendarContract.Events.DTSTART,
                CalendarContract.Events.EVENT_TIMEZONE,
                CalendarContract.Events.ALL_DAY
        };

        long currentTime = System.currentTimeMillis();
        long oneYearFromNow = currentTime + (365L * 24 * 60 * 60 * 1000); // One year in milliseconds

        String selection = CalendarContract.Events.CALENDAR_ID + " = ? AND " +
                CalendarContract.Events.DTSTART + " >= ? AND " +
                CalendarContract.Events.DTSTART + " <= ?";
        String[] selectionArgs = new String[]{
                String.valueOf(calendarId),
                String.valueOf(currentTime),
                String.valueOf(oneYearFromNow)
        };

        String sortOrder = CalendarContract.Events.DTSTART + " ASC";

        ContentResolver contentResolver = requireActivity().getContentResolver();

        try (Cursor cursor = contentResolver.query(uri, projection, selection, selectionArgs, sortOrder)) {
            if (cursor != null && cursor.moveToFirst()) {
                ArrayList<TaskCalendar> taskCalendarsUser = new ArrayList<>();
                do {
                    String title = cursor.getString(cursor.getColumnIndexOrThrow(CalendarContract.Events.TITLE));
                    long dtStart = cursor.getLong(cursor.getColumnIndexOrThrow(CalendarContract.Events.DTSTART));
                    String eventTimeZone = cursor.getString(cursor.getColumnIndexOrThrow(CalendarContract.Events.EVENT_TIMEZONE));
                    int allDay = cursor.getInt(cursor.getColumnIndexOrThrow(CalendarContract.Events.ALL_DAY));

                    String dateFormatted;

                    SimpleDateFormat dateFormat;
                    if (allDay == 1) {
                        // Handle all-day events
                        dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                    } else {
                        // Handle events with specific times
                        dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                        if (eventTimeZone != null) {
                            dateFormat.setTimeZone(TimeZone.getTimeZone(eventTimeZone));
                        } else {
                            dateFormat.setTimeZone(TimeZone.getDefault());
                        }
                    }
                    dateFormatted = dateFormat.format(new Date(dtStart));

                    // Create the task
                    TaskCalendar taskCalendar = new TaskCalendar(title, dateFormatted, "TaskCalendarUser", true);
                    taskAdapter.getTasks().add(taskCalendar);
                    taskCalendarsUser.add(taskCalendar);

                    Log.d("CalendarEvent", "Title: " + title + ", Date: " + dateFormatted);
                } while (cursor.moveToNext());

                if (!taskCalendarsUser.isEmpty()) {
                    currentUser.setTaskCalendarUser(taskCalendarsUser);
                    viewModel.updateUserTaskCalendar(currentUser).observe(getViewLifecycleOwner(), aBoolean -> {
                        binding.loadingCalendar.setVisibility(View.GONE);
                        binding.btnCalendarAdd.setEnabled(true);
                        if (aBoolean) {
                            Toast.makeText(getContext(), "Tarefas do calendário salvas", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "Erro ao salvar tarefas do calendário", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    binding.loadingCalendar.setVisibility(View.GONE);
                    binding.btnCalendarAdd.setEnabled(true);
                    Toast.makeText(getContext(), "Nenhuma tarefa encontrada neste calendário.", Toast.LENGTH_SHORT).show();
                }
            } else {
                binding.loadingCalendar.setVisibility(View.GONE);
                binding.btnCalendarAdd.setEnabled(true);
                Toast.makeText(getContext(), "Nenhuma tarefa encontrada neste calendário.", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            binding.loadingCalendar.setVisibility(View.GONE);
            binding.btnCalendarAdd.setEnabled(true);
            Toast.makeText(getContext(), "Erro ao acessar o calendário: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e("CalendarError", "Erro ao acessar o calendário", e);
        }
    }

    private void updateAdapterForDate(LocalDate date) {
        List<TaskCalendar> taskBefore = new ArrayList<>(taskAdapter.getTasks());
        taskAdapter.getTasks().clear();
        taskAdapter.getTasks().addAll(tasks.getOrDefault(date, List.of()));

        if (taskAdapter.getTasks().size() > taskBefore.size()) {
            int index = taskAdapter.getTasks().size() - taskBefore.size();
            taskAdapter.notifyItemRangeInserted(taskAdapter.getTasks().size(), index);
            taskAdapter.notifyItemRangeChanged(0, taskAdapter.getTasks().size() - index);

        } else if (taskAdapter.getTasks().size() < taskBefore.size()) {
            int index = taskBefore.size() - taskAdapter.getTasks().size();
            taskAdapter.notifyItemRangeRemoved(taskAdapter.getTasks().size(), index);
            taskAdapter.notifyItemRangeChanged(0, taskBefore.size() - index);
        } else {
            taskAdapter.notifyItemRangeChanged(0, taskAdapter.getTasks().size());
        }
    }

    private void configureBinders(List<DayOfWeek> daysOfWeek) {
        class DayViewContainer extends ViewContainer {
            private CalendarDay day;
            final CalendarDayBinding binding;

            public DayViewContainer(@NonNull View view) {
                super(view);
                binding = CalendarDayBinding.bind(view);
                view.setOnClickListener(v -> {
                    if (day != null && day.getPosition() == DayPosition.MonthDate) {
                        if (selectedDate != day.getDate()) {
                            LocalDate oldDate = selectedDate;
                            selectedDate = day.getDate();
                            CalendarFragment.this.binding.calendar.notifyDateChanged(day.getDate());
                            if (oldDate != null) {
                                CalendarFragment.this.binding.calendar.notifyDateChanged(oldDate);
                            }
                            updateAdapterForDate(day.getDate());
                        }
                    }
                });
            }

            public void bind(CalendarDay calendarDay) {
                this.day = calendarDay;
            }

        }
        binding.calendar.setDayBinder(new MonthDayBinder<>() {
            @NonNull
            @Override
            public ViewContainer create(@NonNull View view) {
                return new DayViewContainer(view);
            }

            @Override
            public void bind(@NonNull ViewContainer container, CalendarDay calendarDay) {
                DayViewContainer dayViewContainer = (DayViewContainer) container;
                dayViewContainer.bind(calendarDay);
                Context context = dayViewContainer.binding.getRoot().getContext();
                TextView textView = dayViewContainer.binding.exFiveDayText;

                textView.setText(String.valueOf(calendarDay.getDate().getDayOfMonth()));

                View flightTopView = dayViewContainer.binding.exFiveDayFlightTop;
                View flightBottomView = dayViewContainer.binding.exFiveDayFlightBottom;
                flightTopView.setBackground(null);
                flightBottomView.setBackground(null);

                if (calendarDay.getPosition() == DayPosition.MonthDate) {

                    List<TaskCalendar> task = CalendarFragment.this.tasks.get(calendarDay.getDate());

                    if (task != null) {
                        if (task.size() == 1) {
                            flightBottomView.setBackgroundColor(context.getColor(TaskTypeColor
                                    .fromTagName(task.get(0).getTag()).getColorHex()));
                        } else {
                            flightTopView.setBackgroundColor(context.getColor(TaskTypeColor
                                    .fromTagName(task.get(0).getTag()).getColorHex()));
                            flightBottomView.setBackgroundColor(context.getColor(TaskTypeColor
                                    .fromTagName(task.get(1).getTag()).getColorHex()));
                        }
                    }

                }
            }
        });

        class MonthViewContainer extends ViewContainer {
            final LinearLayout legendLayout;

            public MonthViewContainer(@NonNull View view) {
                super(view);
                legendLayout = CalendarHeaderBinding.bind(view).legendLayout.getRoot();
            }
        }

        Typeface typeFace = Typeface.create("sans-serif-light", Typeface.NORMAL);
        binding.calendar.setMonthHeaderBinder(new MonthHeaderFooterBinder<>() {
            @NonNull
            @Override
            public ViewContainer create(@NonNull View view) {
                return new MonthViewContainer(view);
            }

            @Override
            public void bind(@NonNull ViewContainer container, CalendarMonth calendarMonth) {
                MonthViewContainer monthViewContainer = (MonthViewContainer) container;

                if (monthViewContainer.legendLayout.getTag() == null) {
                    monthViewContainer.legendLayout.setTag(true);

                    int childCount = monthViewContainer.legendLayout.getChildCount();

                    for (int index = 0; index < childCount; index++) {
                        View view = monthViewContainer.legendLayout.getChildAt(index);
                        if (view instanceof TextView tv) {
                            tv.setText(daysOfWeek.get(index).getDisplayName(TextStyle.SHORT, Locale.getDefault()).toUpperCase());
                            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
                            tv.setTypeface(typeFace);
                        }
                    }
                }
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        if (snackbar != null && snackbar.isShown()) {
            snackbar.dismiss();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}