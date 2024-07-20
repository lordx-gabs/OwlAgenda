package com.example.owlagenda.ui.calendar;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.applandeo.materialcalendarview.CalendarView;
import com.example.owlagenda.R;
import com.example.owlagenda.databinding.FragmentCalendarBinding;

import java.util.Calendar;

public class CalendarFragment extends Fragment {

    private @NonNull FragmentCalendarBinding binding;
    private CalendarView calendarView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentCalendarBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        binding.appBarTelaPrincipal.toolbar.inflateMenu(R.menu.menu_overflow); // Define o menu overflow na fragment
        calendarView = binding.calendarView;

        Calendar calendarMin = Calendar.getInstance();
        calendarMin.set(Calendar.MONTH, Calendar.JANUARY);
        calendarMin.set(Calendar.DAY_OF_MONTH, 1);

        Calendar calendarMax = Calendar.getInstance();
        calendarMax.set(Calendar.MONTH, Calendar.DECEMBER);
        calendarMax.set(Calendar.DAY_OF_MONTH, 31);

        calendarView.setMinimumDate(calendarMin);
        calendarView.setMaximumDate(calendarMax);


        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}