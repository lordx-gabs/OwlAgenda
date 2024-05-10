package com.example.owlagenda.ui.corubot;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.owlagenda.R;
import com.example.owlagenda.databinding.FragmentCorubotBinding;

public class CorubotFragment extends Fragment {

    private CorubotViewModel mViewModel;
    private FragmentCorubotBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentCorubotBinding.inflate(inflater, container, false);

        binding.appBarTelaPrincipal.toolbar.inflateMenu(R.menu.menu_overflow); // Define o menu overflow na fragment

        return binding.getRoot();
    }

}