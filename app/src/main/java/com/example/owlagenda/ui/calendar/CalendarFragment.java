package com.example.owlagenda.ui.calendar;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.owlagenda.R;
import com.example.owlagenda.data.models.Task;
import com.example.owlagenda.data.models.UserViewModel;
import com.example.owlagenda.databinding.CalendarDayBinding;
import com.example.owlagenda.databinding.CalendarHeaderBinding;
import com.example.owlagenda.databinding.FragmentCalendarBinding;
import com.example.owlagenda.ui.task.TaskView;
import com.example.owlagenda.util.SharedPreferencesUtil;
import com.example.owlagenda.util.TaskTypeColor;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.kizitonwose.calendar.core.CalendarDay;
import com.kizitonwose.calendar.core.CalendarMonth;
import com.kizitonwose.calendar.core.DayPosition;
import com.kizitonwose.calendar.core.ExtensionsKt;
import com.kizitonwose.calendar.view.MonthDayBinder;
import com.kizitonwose.calendar.view.MonthHeaderFooterBinder;
import com.kizitonwose.calendar.view.ViewContainer;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


import jp.wasabeef.recyclerview.animators.FadeInUpAnimator;

public class CalendarFragment extends Fragment {

    private FragmentCalendarBinding binding;
    private LocalDate selectedDate;
    private TaskAdapter taskAdapter;
    private Map<LocalDate, List<TaskCalendar>> tasks;
    private UserViewModel userViewModel;
    private CalendarViewModel viewModel;
    private ArrayList<TaskCalendar> tasksCalendar;
    private ArrayList<Task> tasksObject;
    List<com.google.android.gms.tasks.Task<Void>> firestoreTasks = new ArrayList<>(); // Lista de tarefas Firestore para controle


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

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

        List<DayOfWeek> daysOfWeek = ExtensionsKt.daysOfWeek();
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

            }

            @Override
            public void onClickBtnDelete(int position) {
                List<TaskCalendar> task = tasks.get(selectedDate);
                Optional<Task> taskOptional = tasksObject.stream()
                        .filter(task7 -> task7.getTitle().equalsIgnoreCase(task.get(position).getNameTask()))
                        .findFirst();

                if (taskOptional.isPresent()) {
                    // Tarefa encontrada
                    viewModel.deleteTask(taskOptional.get(), getActivity().getApplicationContext())
                            .observe(getViewLifecycleOwner(), aBoolean -> {
                                if(aBoolean) {
                                    Toast.makeText(requireContext(), "Tarefa deletada com sucesso", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(requireContext(), "Erro ao deletar tarefa", Toast.LENGTH_SHORT).show();
                                }
                            });
                } else {
                    // Tarefa não encontrada
                   Log.d("teste","Tarefa não encontrada.");
                }
            }

            @Override
            public void onClickBtnDetails(int position) {

            }
        });
        binding.recycleCalendar.setItemAnimator(new FadeInUpAnimator());
        binding.recycleCalendar.setAdapter(taskAdapter);
        if(isAdded()) {
            configureBinders(daysOfWeek);
            binding.calendar.setup(startMonth, endMonth, daysOfWeek.get(0));
            binding.calendar.scrollToMonth(YearMonth.now());
        }

        viewModel.getTasks().observe(getViewLifecycleOwner(), tasks -> {
            binding.loadingCalendar.setVisibility(View.VISIBLE);
            if(tasks != null) {
                if(!tasks.isEmpty()) {
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
                                            task.getTag()
                                    ));
                                }
                            }
                            return null;
                        }));

                        Tasks.whenAllComplete(firestoreTasks).addOnCompleteListener(task7 -> {
                            if (task7.isSuccessful()) {
                                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                                this.tasks.clear();
                                this.tasks.putAll(
                                        tasksCalendar.stream().collect(Collectors.groupingBy(task5 ->
                                                        LocalDate.parse(task5.getDate(), formatter),
                                                Collectors.toList()))
                                );
                                Log.e("teste", "" + this.tasks.size());
                                if(isAdded()) {
                                    if(selectedDate != null) {
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
                    this.tasks.clear();
                    if(isAdded()) {
                        if(selectedDate != null) {
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
            if(isAdded()) {
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

        int currentNightMode = getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK;

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
            }
            return false;
        });

        return binding.getRoot();
    }

    private void updateAdapterForDate(LocalDate date) {
        List<TaskCalendar> taskBefore = new ArrayList<>(taskAdapter.getTasks());
        taskAdapter.getTasks().clear();
        taskAdapter.getTasks().addAll(tasks.getOrDefault(date, List.of()));

        if (taskAdapter.getTasks().size() > taskBefore.size() ) {
            int index = taskAdapter.getTasks().size() - taskBefore.size();
            taskAdapter.notifyItemRangeInserted(taskAdapter.getTasks().size(), index);
            taskAdapter.notifyItemRangeChanged( 0, taskAdapter.getTasks().size() - index);

        } else if(taskAdapter.getTasks().size() < taskBefore.size()) {
            int index = taskBefore.size() - taskAdapter.getTasks().size();
            taskAdapter.notifyItemRangeRemoved(taskAdapter.getTasks().size(), index);
            taskAdapter.notifyItemRangeChanged( 0, taskBefore.size() - index);
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
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}