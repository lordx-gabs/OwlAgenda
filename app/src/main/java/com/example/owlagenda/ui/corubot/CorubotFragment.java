package com.example.owlagenda.ui.corubot;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.owlagenda.R;
import com.example.owlagenda.databinding.FragmentCorubotBinding;
import com.example.owlagenda.ui.registration.RegistrationView;
import com.example.owlagenda.util.ChatBot;

public class CorubotFragment extends Fragment {

    private CorubotViewModel mViewModel;
    private FragmentCorubotBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentCorubotBinding.inflate(inflater, container, false);

        binding.appBarTelaPrincipal.toolbar.inflateMenu(R.menu.menu_overflow); // Define o menu overflow na fragment

        binding.button.setOnClickListener(v -> {
            ChatBot chatBot = new ChatBot();
            chatBot.sendMessage(binding.etMessageUser.getText().toString(), new ChatBot.Callback<>() {
                @Override
                public void onSuccess(String response) {
                    // Do something with the response
                    binding.tvMessageGemini.setText(response);
                }

                @Override
                public void onFailure(Throwable t) {
                    // Handle the error
                }
            });

        });

        binding.appFab.fab.setOnClickListener(v ->
                startActivity(new Intent(getActivity(), RegistrationView.class)));

        return binding.getRoot();

    }

}