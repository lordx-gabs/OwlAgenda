package com.example.owlagenda.ui.calendario;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.owlagenda.R;
import com.example.owlagenda.databinding.FragmentCalendarioBinding;

public class CalendarioFragment extends Fragment {

    private FragmentCalendarioBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentCalendarioBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        binding.appBarTelaPrincipal.toolbar.inflateMenu(R.menu.menu_overflow); // Define o menu overflow na fragment

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}