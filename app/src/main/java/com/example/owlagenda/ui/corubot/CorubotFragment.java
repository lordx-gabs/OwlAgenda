package com.example.owlagenda.ui.corubot;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.owlagenda.R;
import com.example.owlagenda.databinding.FragmentCorubotBinding;
import com.example.owlagenda.ui.register.RegisterView;

public class CorubotFragment extends Fragment {

    private CorubotViewModel viewModel;
    private FragmentCorubotBinding binding;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentCorubotBinding.inflate(inflater, container, false);

        binding.appBarTelaPrincipal.toolbar.inflateMenu(R.menu.menu_overflow); // Define o menu overflow na fragment

        viewModel = new ViewModelProvider(this).get(CorubotViewModel.class);

        binding.button.setOnClickListener(v -> {
            viewModel.sendMessage(binding.etMessageUser.getText().toString()).observe(getViewLifecycleOwner(), s -> {
                if (s != null) {
                    binding.tvMessageGemini.setText(s);
                } else {
                    Toast.makeText(getActivity(), "Erro ao enviar mensagem.", Toast.LENGTH_SHORT).show();
                }
            });
        });

        binding.appFab.fab.setOnClickListener(v -> startActivity(new Intent(getActivity(), RegisterView.class)));

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}