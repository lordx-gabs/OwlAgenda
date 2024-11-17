package com.example.owlagenda.ui.selene;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.LinearGradient;
import android.graphics.Rect;
import android.graphics.Shader;
import android.net.ConnectivityManager;
import android.net.Network;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Toast;

import com.example.owlagenda.R;
import com.example.owlagenda.data.models.User;
import com.example.owlagenda.data.models.UserViewModel;
import com.example.owlagenda.databinding.FragmentSeleneBinding;
import com.example.owlagenda.ui.aboutus.AboutUsView;
import com.example.owlagenda.util.NetworkUtil;
import com.example.owlagenda.util.SharedPreferencesUtil;
import com.google.ai.client.generativeai.type.Content;

import java.util.ArrayList;
import java.util.List;

public class SeleneFragment extends Fragment {

    private SeleneViewModel viewModel;
    private UserViewModel userViewModel;
    private FragmentSeleneBinding binding;
    private ArrayList<Message> messages;
    private User currentUser;
    private ValueAnimator animatorText;
    private ViewTreeObserver.OnGlobalLayoutListener keyboardListener;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSeleneBinding.inflate(inflater, container, false);

        // mudar aqui a cor do selene filled

        binding.appBarTelaPrincipal.toolbarSofia.inflateMenu(R.menu.menu_overflow);
        binding.recycleBalloons.setLayoutManager(new LinearLayoutManager(requireContext(),
                RecyclerView.VERTICAL,
                false));

        viewModel = new ViewModelProvider(this).get(SeleneViewModel.class);
        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);

        userViewModel.getUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                currentUser = user;
                this.messages = new ArrayList<>();
                if (user.getHistoryMessage() != null && !user.getHistoryMessage().isEmpty()) {
                    List<Content> historyMessage = new ArrayList<>();

                    for (Message message : user.getHistoryMessage()) {
                        Content.Builder contentBuilder = new Content.Builder();

                        if (message.getMessageType() == Message.TYPE_USER_MESSAGE) {
                            contentBuilder.setRole("user");
                        } else if (message.getMessageType() == Message.TYPE_SELENE_MESSAGE) {
                            contentBuilder.setRole("model");
                        } else {
                            continue; // Pular tipos de mensagem desconhecidos
                        }

                        contentBuilder.addText(message.getText());
                        historyMessage.add(contentBuilder.build());
                    }

                    viewModel.setChatBotSelene(historyMessage);
                } else {
                    viewModel.setChatBotSelene(new ArrayList<>());
                }

                binding.recycleBalloons.setAdapter(new MessageAdapter(this.messages, currentUser.getUrlProfilePhoto()));
            } else {
                Toast.makeText(getActivity(), "Erro ao carregar foto de perfil.", Toast.LENGTH_SHORT).show();
            }
        });

        NetworkUtil.registerNetworkCallback(getContext(), new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NonNull Network network) {
                super.onAvailable(network);
                if (isAdded()) {
                    requireActivity().runOnUiThread(() ->
                            userViewModel.getUser().observe(getViewLifecycleOwner(), user -> {
                                if (user != null) {
                                    currentUser = user;
                                    userViewModel.getMessages().observe(getViewLifecycleOwner(), messages -> {
                                        SeleneFragment.this.messages = new ArrayList<>();
                                        if (messages != null) {
                                            SeleneFragment.this.messages.addAll(messages);

                                        } else {
                                            Toast.makeText(getContext(), "Erro ao carregar histórico de mensagens.", Toast.LENGTH_SHORT).show();
                                        }

                                        binding.recycleBalloons.setAdapter(new MessageAdapter(SeleneFragment.this.messages, currentUser.getUrlProfilePhoto()));
                                        binding.recycleBalloons.scrollToPosition(SeleneFragment.this.messages.size() - 1);
                                    });

                                } else {
                                    Toast.makeText(getContext(), "Erro ao carregar foto de perfil.", Toast.LENGTH_SHORT).show();
                                }
                            }));
                }
            }

            @Override
            public void onLost(@NonNull Network network) {
                super.onLost(network);
                if (isAdded()) {
                    Toast.makeText(getContext(), "Sem conexão com a internet.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        binding.btnSendMessage.setOnClickListener(v -> {
            if (NetworkUtil.isInternetAvailable(getContext())) {
                if (binding.etMessageUser.getText().toString().isEmpty()) {
                    Toast.makeText(getContext(), "Selene não pode responder mensagens vazias.", Toast.LENGTH_SHORT).show();
                } else {
                    binding.pointsAnimationView.setVisibility(View.VISIBLE);
                    binding.btnSendMessage.setVisibility(View.GONE);
                    messages.add(new Message(binding.etMessageUser.getText().toString(), Message.TYPE_USER_MESSAGE));
                    binding.recycleBalloons.getAdapter().notifyItemInserted(messages.size() - 1);
                    binding.recycleBalloons.scrollToPosition(messages.size() - 1);

                    viewModel.sendMessage(binding.etMessageUser.getText().toString().trim(), getContext()).observe(getViewLifecycleOwner(), s -> {
                        if (s != null) {
                            messages.add(new Message(s, Message.TYPE_SELENE_MESSAGE));
                            binding.recycleBalloons.getAdapter().notifyItemInserted(messages.size() - 1);
                            binding.recycleBalloons.scrollToPosition(messages.size() - 1);
                            animatedText();
                        } else {
                            // vermelho mensagem user
                            messages.remove(messages.size() - 1);
                            binding.recycleBalloons.getAdapter().notifyItemRemoved(messages.size() - 1);
                            Toast.makeText(getActivity(), "Erro ao enviar mensagem.", Toast.LENGTH_SHORT).show();
                        }
                    });
                    binding.etMessageUser.setText("");
                }
            } else {
                Toast.makeText(getActivity(), "Sem conexão com a internet.", Toast.LENGTH_SHORT).show();
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

        int currentNightMode = getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK;

        MenuItem themeItem = binding.appBarTelaPrincipal.toolbarSofia.getMenu().findItem(R.id.action_theme);
        if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
            themeItem.setIcon(R.drawable.ic_theme_light);  // Ícone para o tema claro
        } else {
            themeItem.setIcon(R.drawable.ic_theme_dark);   // Ícone para o tema escuro
        }

        binding.appBarTelaPrincipal.toolbarSofia.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.menu_clear_history_message) {
                int index = messages.size();
                messages.clear();
                viewModel.deleteHistoryMessageUser(currentUser.getId());
                binding.recycleBalloons.getAdapter().notifyItemRangeRemoved(0, index);
            } else if (item.getItemId() == R.id.action_theme) {
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
            } else if (item.getItemId() == R.id.action_about_us) {
                startActivity(new Intent(getActivity(), AboutUsView.class));
                return true;
            }
            return false;
        });

        return binding.getRoot();
    }

    private void animatedText() {
        binding.recycleBalloons.post(() -> {
            RecyclerView.ViewHolder holder = binding.recycleBalloons.findViewHolderForAdapterPosition(messages.size() - 1);
            if (holder instanceof SeleneMessageViewHolder viewHolder) {

                String fullTextSelene = viewHolder.textMessage.getText().toString();

                int durationPerCharacter = 50;
                int minDuration = 500;
                int maxDuration = 10000;
                int totalDuration = fullTextSelene.length() * durationPerCharacter;
                totalDuration = Math.max(minDuration, Math.min(totalDuration, maxDuration));

                animatorText = ValueAnimator.ofInt(0, fullTextSelene.length());
                animatorText.setDuration(totalDuration);

                animatorText.addUpdateListener(animation -> {
                    int animatedValue = (int) animation.getAnimatedValue();
                    viewHolder.textMessage.setText(fullTextSelene.substring(0, animatedValue));
                    binding.recycleBalloons.scrollToPosition(messages.size() - 1);
                });

                animatorText.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        binding.pointsAnimationView.setVisibility(View.GONE);
                        binding.btnSendMessage.setVisibility(View.VISIBLE);
                    }
                });

                animatorText.start();
            }
        });
    }

    private void onHideKeyboard() {
        getActivity().findViewById(R.id.bottomNavigationView).setVisibility(View.GONE);
        binding.bottomNavigationViewSelene.bottomNavigationView.setVisibility(View.GONE);
    }

    private void onShowKeyboard() {
        if(binding.bottomNavigationViewSelene.bottomNavigationView.getVisibility() == View.GONE) {
            binding.recycleBalloons.scrollToPosition(SeleneFragment.this.messages.size() - 1);
        }
        getActivity().findViewById(R.id.bottomNavigationView).setVisibility(View.VISIBLE);
        binding.bottomNavigationViewSelene.bottomNavigationView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (NetworkUtil.isInternetAvailable(getContext())) {
            viewModel.saveHistoryMessageUser(currentUser, messages);

        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding.getRoot().getViewTreeObserver().removeOnGlobalLayoutListener(keyboardListener);

        if (animatorText != null && animatorText.isRunning()) {
            animatorText.cancel();
        }

        binding = null;
    }
}