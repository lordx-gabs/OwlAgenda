package com.example.owlagenda.ui.inicio;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.owlagenda.R;
import com.example.owlagenda.data.models.Task;
import com.example.owlagenda.data.models.UserViewModel;
import com.example.owlagenda.databinding.FragmentInicioBinding;
import com.example.owlagenda.ui.prova.Prova;
import com.example.owlagenda.ui.task.TaskView;

public class InicioFragment extends Fragment {
    private InicioViewModel inicioViewModel;
    private FragmentInicioBinding binding;
    private UserViewModel userViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        inicioViewModel = new ViewModelProvider(this).get(InicioViewModel.class);

        binding = FragmentInicioBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);

        binding.appFab.fab.setOnClickListener(v ->
                startActivity(new Intent(getActivity(), TaskView.class)));
        binding.appBarTelaPrincipal.toolbar.inflateMenu(R.menu.menu_overflow); // Define o menu overflow na fragment

        binding.btnTestee.setOnClickListener(v ->
                startActivity(new Intent(getActivity(), Prova.class)));

//        final String fullText = "Texto que vai aparecendo aos poucoswdadmjidaiduajnusdjasjhdnsadmsd adnzdjuzndzhsjfzhduwseytqwieqwkleqkoerqwklerkojraewadjajnudajhdaawdajdjiadjauwyywyernjasdjhfzsn scbzbycwdqkodj zm";
//
//        ValueAnimator animator = ValueAnimator.ofInt(0, fullText.length());
//        animator.setDuration(10000); // Duração de 3 segundos
//        animator.addUpdateListener(animation -> {
//            int animatedValue = (int) animation.getAnimatedValue();
//            binding.textView30.setText(fullText.substring(0, animatedValue));
//        });
//        animator.start();


        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}