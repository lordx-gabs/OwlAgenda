package com.example.owlagenda.ui.corubot;

import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.owlagenda.R;
import com.example.owlagenda.databinding.FragmentCorubotBinding;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class CorubotFragment extends Fragment {

    private CorubotViewModel viewModel;
    private FragmentCorubotBinding binding;
    private ArrayList<Message> messages;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentCorubotBinding.inflate(inflater, container, false);

        binding.appBarTelaPrincipal.toolbar.inflateMenu(R.menu.menu_overflow); // Define o menu overflow na fragment

        messages = new ArrayList<>();
        binding.recycleBalloons.setAdapter(new MessageAdapter(messages, FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl().toString()));
        binding.recycleBalloons.setLayoutManager(new LinearLayoutManager(requireContext(),
                RecyclerView.VERTICAL,
                false));

        viewModel = new ViewModelProvider(this).get(CorubotViewModel.class);

        binding.btnSendMessage.setOnClickListener(v -> {
            messages.add(new Message(binding.etMessageUser.getText().toString(), Message.TYPE_USER_MESSAGE));
            binding.recycleBalloons.getAdapter().notifyItemInserted(messages.size() - 1);
            binding.recycleBalloons.scrollToPosition(messages.size() - 1);
            viewModel.sendMessage(binding.etMessageUser.getText().toString()).observe(getViewLifecycleOwner(), s -> {
                if (s != null) {
                    messages.add(new Message(s, Message.TYPE_SELENE_MESSAGE));
                    binding.recycleBalloons.getAdapter().notifyItemInserted(messages.size() - 1);
                    binding.recycleBalloons.scrollToPosition(messages.size() - 1);
                } else {
                    Toast.makeText(getActivity(), "Erro ao enviar mensagem.", Toast.LENGTH_SHORT).show();
                }
            });
        });


        binding.appBarTelaPrincipal.textGemini.post(() -> {
            float width = binding.appBarTelaPrincipal.textGemini.getPaint().measureText(binding.appBarTelaPrincipal.textGemini.getText().toString());

            Shader textShader = new LinearGradient(0, 0, width, 0,
                    new int[]{
                            0xFF4285F4,  // Azul
                            0xFF9B72CB,  // Roxo
                            0xFFD96570,  // Vermelho
                            0xFFD96570,  // Vermelho (repetido para a posição 24%)
                            0xFF9B72CB,  // Roxo (repetido para a posição 35%)
                            0xFF4285F4,  // Azul (repetido para a posição 44%)
                            0xFF9B72CB,  // Roxo (repetido para a posição 50%)
                            0xFFD96570   // Vermelho (repetido para a posição 56%)
                    }, null, Shader.TileMode.CLAMP);

            // Aplicar o shader ao Paint do TextView
            binding.appBarTelaPrincipal.textGemini.getPaint().setShader(textShader);

            // Invalidar o TextView para redesenhar com o shader
            binding.appBarTelaPrincipal.textGemini.invalidate();

        });



        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}