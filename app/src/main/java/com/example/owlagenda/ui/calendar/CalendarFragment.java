package com.example.owlagenda.ui.calendar;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.owlagenda.R;
import com.example.owlagenda.databinding.CalendarDayBinding;
import com.example.owlagenda.databinding.CalendarHeaderBinding;
import com.example.owlagenda.databinding.FragmentCalendarBinding;
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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class CalendarFragment extends Fragment {

    private FragmentCalendarBinding binding;
    private LocalDate selectedDate;
    private TaskAdapter calendarAdapter;
    private Map<LocalDate, List<Task>> tasks;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentCalendarBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        binding.appBarTelaPrincipal.toolbar.inflateMenu(R.menu.menu_overflow);

        binding.recycleCalendar.setLayoutManager(new LinearLayoutManager(requireContext(),
                RecyclerView.VERTICAL,
                false));

        tasks = Task.generateFlights().stream()
                .collect(Collectors.groupingBy(flight -> flight.getTime().toLocalDate()));

        calendarAdapter = new TaskAdapter();
        binding.recycleCalendar.setAdapter(calendarAdapter);
        calendarAdapter.notifyDataSetChanged();

        List<DayOfWeek> daysOfWeek = ExtensionsKt.daysOfWeek();
        YearMonth currentMonth = YearMonth.now();
        YearMonth startMonth = currentMonth.minusMonths(12);
        YearMonth endMonth = currentMonth.plusMonths(12);
        configureBinders(daysOfWeek);
        binding.calendar.setup(startMonth, endMonth, daysOfWeek.get(0));
        binding.calendar.scrollToMonth(currentMonth);

        binding.calendar.setMonthScrollListener(calendarMonth -> {
            binding.monthYearText.setText( DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault()).format(calendarMonth.getYearMonth()));
            if (selectedDate != null) {
                binding.calendar.notifyDateChanged(selectedDate);
                selectedDate = null;
                updateAdapterForDate(null);
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

        return root;
    }

    private void updateAdapterForDate(LocalDate date) {
        calendarAdapter.getTasks().clear();
        calendarAdapter.getTasks().addAll(tasks.getOrDefault(date, List.of()));
        calendarAdapter.notifyDataSetChanged();
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

                if(calendarDay.getPosition() == DayPosition.MonthDate){
                    textView.setTextColor(context.getColor(R.color.example_5_text_grey));


                    List<Task> flights = CalendarFragment.this.tasks.get(calendarDay.getDate());

                    if (flights != null) {
                        if (flights.size() == 1) {
                            flightBottomView.setBackgroundColor(context.getColor(flights.get(0).getColor()));
                        } else {
                            flightTopView.setBackgroundColor(context.getColor(flights.get(0).getColor()));
                            flightBottomView.setBackgroundColor(context.getColor(flights.get(1).getColor()));
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

                if(monthViewContainer.legendLayout.getTag() == null){
                    monthViewContainer.legendLayout.setTag(true);

                    int childCount = monthViewContainer.legendLayout.getChildCount();

                    for (int index = 0; index < childCount; index++) {
                        View view = monthViewContainer.legendLayout.getChildAt(index);
                        if (view instanceof TextView tv) {
                            tv.setText(daysOfWeek.get(index).getDisplayName(TextStyle.SHORT, Locale.getDefault()).toUpperCase());
                            tv.setTextColor(monthViewContainer.getView().getContext().getColor(R.color.white)); // setTextColorRes equivalente
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