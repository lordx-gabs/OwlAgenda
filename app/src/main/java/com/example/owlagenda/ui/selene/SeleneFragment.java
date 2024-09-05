package com.example.owlagenda.ui.selene;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.graphics.LinearGradient;
import android.graphics.Rect;
import android.graphics.Shader;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Toast;

import com.example.owlagenda.R;
import com.example.owlagenda.data.models.User;
import com.example.owlagenda.data.models.UserViewModel;
import com.example.owlagenda.databinding.FragmentCorubotBinding;

import java.util.ArrayList;

public class SeleneFragment extends Fragment {

    private SeleneViewModel viewModel;
    private UserViewModel userViewModel;
    private FragmentCorubotBinding binding;
    private ArrayList<Message> messages;
    private User currentUser;
    private ValueAnimator animatorText;
    private ViewTreeObserver.OnGlobalLayoutListener keyboardListener;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentCorubotBinding.inflate(inflater, container, false);

        binding.appBarTelaPrincipal.toolbarSofia.inflateMenu(R.menu.menu_overflow);
        binding.recycleBalloons.setLayoutManager(new LinearLayoutManager(requireContext(),
                RecyclerView.VERTICAL,
                false));

        viewModel = new ViewModelProvider(this).get(SeleneViewModel.class);
        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);

        userViewModel.getUser().observe(getViewLifecycleOwner(), user -> {
            if(user != null) {
                currentUser = user;
                this.messages = new ArrayList<>();
                binding.recycleBalloons.setAdapter(new MessageAdapter(this.messages, currentUser.getUrlProfilePhoto()));
            } else {
                Toast.makeText(getActivity(), "Erro ao carregar foto de perfil.", Toast.LENGTH_SHORT).show();
            }
        });

        userViewModel.getMessages().observe(getViewLifecycleOwner(), messages -> {
            this.messages = new ArrayList<>();
            if (messages != null) {
                this.messages = messages;
            }

            binding.recycleBalloons.setAdapter(new MessageAdapter(this.messages, currentUser.getUrlProfilePhoto()));
            binding.recycleBalloons.scrollToPosition(this.messages.size() - 1);
        });

        binding.btnSendMessage.setOnClickListener(v -> {
            if (binding.etMessageUser.getText().toString().isEmpty()) {
                Toast.makeText(getContext(), "Selene não pode responder mensagens vazias.", Toast.LENGTH_SHORT).show();
            } else {
                binding.pointsAnimationView.setVisibility(View.VISIBLE);
                binding.btnSendMessage.setVisibility(View.GONE);
                messages.add(new Message(binding.etMessageUser.getText().toString(), Message.TYPE_USER_MESSAGE));
                binding.recycleBalloons.getAdapter().notifyItemInserted(messages.size() - 1);
                binding.recycleBalloons.scrollToPosition(messages.size() - 1);

                viewModel.sendMessage(binding.etMessageUser.getText().toString()).observe(getViewLifecycleOwner(), s -> {
                    if (s != null) {
                        messages.add(new Message(s, Message.TYPE_SELENE_MESSAGE));
                        binding.recycleBalloons.getAdapter().notifyItemInserted(messages.size() - 1);
                        binding.recycleBalloons.scrollToPosition(messages.size() - 1);
                        animatedText();
                    } else {
                        // vermelho mensagem user
                        Toast.makeText(getActivity(), "Erro ao enviar mensagem.", Toast.LENGTH_SHORT).show();
                    }
                });
                binding.etMessageUser.setText("");
            }
        });

        binding.appBarTelaPrincipal.textGemini.post(() -> {
            float width = binding.appBarTelaPrincipal.textGemini.getPaint()
                    .measureText(binding.appBarTelaPrincipal.textGemini.getText().toString());
            Shader textShader = new LinearGradient(0, 0, width, 0,
                    new int[]{
                            0xFF4285F4,
                            0xFF9B72CB,
                            0xFFD96570,
                            0xFFD96570,
                            0xFF9B72CB,
                            0xFF4285F4,
                            0xFF9B72CB,
                            0xFFD96570
                    }, null, Shader.TileMode.CLAMP);

            binding.appBarTelaPrincipal.textGemini.getPaint().setShader(textShader);
            binding.appBarTelaPrincipal.textGemini.invalidate();
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), s ->
                Toast.makeText(getActivity(), s, Toast.LENGTH_SHORT).show());

        keyboardListener = () -> {
            if (getActivity() != null) {
                Rect r = new Rect();
                binding.getRoot().getWindowVisibleDisplayFrame(r);
                int screenHeight = binding.getRoot().getRootView().getHeight();
                int keypadHeight = screenHeight - r.bottom;

                if (keypadHeight > screenHeight * 0.15) {
                    onHideKeyboard();
                } else {
                    onShowKeyboard();
                }
            }
        };

        binding.getRoot().getViewTreeObserver().addOnGlobalLayoutListener(keyboardListener);

        binding.appBarTelaPrincipal.toolbarSofia.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.menu_clear_history_message) {
                int index = messages.size();
                messages.clear();
                viewModel.deleteHistoryMessageUser(currentUser.getId());
                binding.recycleBalloons.getAdapter().notifyItemRangeRemoved(0, index);
            } else {
                viewModel.saveHistoryMessageUser(currentUser, messages);
            }
            return false;
        });

        return binding.getRoot();
    }

    private void animatedText() {
        binding.recycleBalloons.post(() -> {
            SeleneMessageViewHolder viewHolder = (SeleneMessageViewHolder) binding.recycleBalloons.findViewHolderForAdapterPosition(messages.size() - 1);

            String fullTextSelene = viewHolder.textMessage.getText().toString();
            animatorText = ValueAnimator.ofInt(0, fullTextSelene.length());
            animatorText.setDuration(3000);
//            int heightInDp = 2;
//            int heightInPx = (int) (heightInDp * getResources().getDisplayMetrics().density); // Converter dp para px
            animatorText.addUpdateListener(animation -> {
                int animatedValue = (int) animation.getAnimatedValue();
//                viewHolder.textMessage.setHeight(viewHolder.textMessage.getHeight() + heightInPx);
                viewHolder.textMessage.setText(fullTextSelene.substring(0, animatedValue));
                binding.recycleBalloons.scrollToPosition(messages.size() - 1);
            });
            animatorText.start();

            animatorText.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(@NonNull Animator animation) {}

                @Override
                public void onAnimationEnd(@NonNull Animator animation) {
                    binding.pointsAnimationView.setVisibility(View.GONE);
                    binding.btnSendMessage.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationCancel(@NonNull Animator animation) {}

                @Override
                public void onAnimationRepeat(@NonNull Animator animation) {}
            });
        });
    }

    private void onHideKeyboard() {
        getActivity().findViewById(R.id.bottomNavigationView).setVisibility(View.GONE);
        binding.bottomNavigationViewSelene.bottomNavigationView.setVisibility(View.GONE);
    }

    private void onShowKeyboard() {
        getActivity().findViewById(R.id.bottomNavigationView).setVisibility(View.VISIBLE);
        binding.bottomNavigationViewSelene.bottomNavigationView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (binding != null) {
            binding.getRoot().getViewTreeObserver().removeOnGlobalLayoutListener(keyboardListener);
        }

        if (animatorText != null && animatorText.isRunning()) {
            animatorText.cancel();
        }

        binding = null;
    }
}