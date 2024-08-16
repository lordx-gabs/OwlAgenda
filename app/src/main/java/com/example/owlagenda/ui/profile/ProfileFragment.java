package com.example.owlagenda.ui.profile;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.owlagenda.R;
import com.example.owlagenda.databinding.FragmentPerfilBinding;
import com.example.owlagenda.ui.prova.ImageAdapter;
import com.mig35.carousellayoutmanager.CarouselLayoutManager;
import com.mig35.carousellayoutmanager.CarouselZoomPostLayoutListener;
import com.mig35.carousellayoutmanager.CenterScrollListener;

import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends Fragment {

    private FragmentPerfilBinding binding;
    private List<Uri> imageUris;
    private ActivityResultLauncher<Intent> pickImageLauncher;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        ProfileViewModel profileViewModel =
                new ViewModelProvider(this).get(ProfileViewModel.class);

        binding = FragmentPerfilBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        binding.appBarTelaPrincipal.toolbar.inflateMenu(R.menu.menu_overflow); // Define o menu overflow na fragment

        imageUris = new ArrayList<>();
        CarouselLayoutManager layoutManager = new CarouselLayoutManager(CarouselLayoutManager.HORIZONTAL);
        layoutManager.setPostLayoutListener(new CarouselZoomPostLayoutListener());
        binding.recylerView.setLayoutManager(layoutManager);
        binding.recylerView.setHasFixedSize(true);
        binding.recylerView.addOnScrollListener(new CenterScrollListener());

        binding.recylerView.setAdapter(new ImageAdapter(getContext(), imageUris));

        binding.button6.setOnClickListener(v -> pickImage());

        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Intent data = result.getData();
                        if (data.getClipData() != null) {
                            int count = data.getClipData().getItemCount();
                            for (int i = 0; i < count; i++) {
                                Uri imageUri = data.getClipData().getItemAt(i).getUri();
                                imageUris.add(imageUri);
                            }
                            binding.recylerView.getAdapter().notifyItemRangeInserted(imageUris.size() - 1, data.getClipData().getItemCount());
                        } else if (data.getData() != null) {
                            imageUris.add(data.getData());
                            binding.recylerView.getAdapter().notifyItemChanged(imageUris.size() - 1);
                        }
                    }
                }
        );


        return root;
    }

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        pickImageLauncher.launch(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}