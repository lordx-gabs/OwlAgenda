package com.example.owlagenda.ui.profile;

import static android.app.Activity.RESULT_OK;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.owlagenda.R;
import com.example.owlagenda.data.models.User;
import com.example.owlagenda.data.models.UserViewModel;
import com.example.owlagenda.databinding.FragmentProfileBinding;
import com.example.owlagenda.ui.aboutus.AboutUsView;
import com.example.owlagenda.ui.homescreen.HomeScreenView;
import com.example.owlagenda.ui.updateemail.UpdateEmail;
import com.example.owlagenda.util.FormatPhoneNumber;
import com.example.owlagenda.util.NetworkUtil;
import com.example.owlagenda.util.SharedPreferencesUtil;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointBackward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class ProfileFragment extends Fragment {
    private FragmentProfileBinding binding;
    private UserViewModel userViewModel;
    private ProfileViewModel profileViewModel;
    private MaterialDatePicker<Long> materialDatePicker;
    private User oldUser;
    private ActivityResultLauncher<Intent> pickImageLauncher, takePhotoLauncher;
    private BottomSheetDialog bottomSheetDialog;
    private Uri fileDestination;
    private Bitmap imageProfileBitmap = null;
    private ViewTreeObserver.OnGlobalLayoutListener keyboardListener;
    private final String[] genderOptions = {"Masculino", "Feminino", "Outros", "Prefiro não informar"};
    private ActivityResultLauncher<Intent> cropActivityResultLauncher;
    private ActivityResultLauncher<String> requestStoragePermissionLauncher;
    private ActivityResultLauncher<String> requestCameraPermissionLauncher;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
        binding = FragmentProfileBinding.inflate(inflater, container, false);

        Calendar calendarEnd = Calendar.getInstance();
        calendarEnd.set(1940, Calendar.JANUARY, 1);
        long dateStart = calendarEnd.getTimeInMillis();

        Calendar calendarStart = Calendar.getInstance();
        calendarStart.add(Calendar.YEAR, -16);
        long dateEnd = calendarStart.getTimeInMillis();

        CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder()
                .setStart(dateStart)
                .setEnd(dateEnd)
                .setValidator(DateValidatorPointBackward.before(dateEnd));

        binding.etDataNascimentoProfile.setOnClickListener(v -> {
            Calendar calendarInitialed = Calendar.getInstance();
            if (binding.etDataNascimentoProfile.getText().toString().isEmpty()) {
                calendarInitialed.setTimeInMillis(dateEnd);
            } else {
                try {
                    calendarInitialed.setTime(new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            .parse(binding.etDataNascimentoProfile.getText().toString()));
                } catch (ParseException e) {
                    Toast.makeText(getActivity().getBaseContext(), "Erro ao formatar a data.", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            materialDatePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Selecione uma data")
                    .setCalendarConstraints(constraintsBuilder.build())
                    .setSelection(calendarInitialed.getTimeInMillis())
                    .build();

            materialDatePicker.addOnPositiveButtonClickListener(selection -> {
                Calendar dateSelected = Calendar.getInstance();
                dateSelected.setTimeInMillis(selection);
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", new Locale("pt", "BR"));
                dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

                binding.etDataNascimentoProfile.setText(dateFormat.format(dateSelected.getTime()));
            });

            if (!materialDatePicker.isAdded()) {
                materialDatePicker.show(requireActivity().getSupportFragmentManager(), "material_date_picker");
            }
        });

        profileViewModel.getErrorMessage().observe(getViewLifecycleOwner(), s ->
                Toast.makeText(getActivity(), s, Toast.LENGTH_SHORT).show());

        profileViewModel.isLoading.setValue(false);

        profileViewModel.isLoading().observe(getViewLifecycleOwner(), aBoolean -> {
            if (aBoolean) {
                binding.barraCarregandoProfile.setVisibility(View.VISIBLE);
            } else {
                binding.barraCarregandoProfile.setVisibility(View.GONE);
            }
        });
        binding.imagePhotoProfile.setVisibility(View.INVISIBLE);
        binding.imageLoadingAnimationView.setVisibility(View.VISIBLE);

        NetworkUtil.registerNetworkCallback(getContext(), new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NonNull Network network) {
                super.onAvailable(network);
                if (isAdded()) {
                    requireActivity().runOnUiThread(() ->
                            userViewModel.getUser().observe(getViewLifecycleOwner(), user -> {
                                if (user != null) {
                                    ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, genderOptions);
                                    binding.etGenderProfile.setAdapter(adapter);
                                    oldUser = user;
                                    binding.etNomeProfile.setText(user.getName());
                                    binding.etDataNascimentoProfile.setText(user.getBirthdate());
                                    if (user.getPhoneNumber() != null) {
                                        binding.etTelefoneProfile.setText(String.valueOf(user.getPhoneNumber()));
                                    }

                                    binding.etTelefoneProfile.setText(String.valueOf(user.getPhoneNumber()));
                                    binding.etGenderProfile.setText(user.getGender(), false);
                                    binding.etSobrenomeProfile.setText(user.getSurname());

                                    Glide.with(getContext())
                                            .load(user.getUrlProfilePhoto())
                                            .circleCrop()
                                            .listener(new RequestListener<>() {
                                                @Override
                                                public boolean onLoadFailed(@Nullable GlideException e, @Nullable Object model, @NonNull Target<Drawable> target, boolean isFirstResource) {
                                                    Toast.makeText(getContext(), "Erro ao carregar imagem de perfil.", Toast.LENGTH_SHORT).show();
                                                    return false;
                                                }

                                                @Override
                                                public boolean onResourceReady(@NonNull Drawable resource, @NonNull Object model, Target<Drawable> target, @NonNull DataSource dataSource, boolean isFirstResource) {
                                                    binding.imagePhotoProfile.setVisibility(View.VISIBLE);
                                                    binding.imageLoadingAnimationView.setVisibility(View.GONE);
                                                    return false;
                                                }
                                            })
                                            .into(binding.imagePhotoProfile);

                                }
                            })
                    );
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

        binding.appBarTelaPrincipal.toolbar.setTitle("Perfil");
        binding.appBarTelaPrincipal.titleOwl.setVisibility(View.GONE);

        binding.btnUserUndo.setOnClickListener(v -> {
            if (NetworkUtil.isInternetAvailable(getContext())) {
                binding.etNomeProfile.setText(oldUser.getName());
                binding.etDataNascimentoProfile.setText(oldUser.getBirthdate());
                binding.etGenderProfile.setText(oldUser.getGender(), false);
                binding.etSobrenomeProfile.setText(oldUser.getSurname());
                binding.etTelefoneProfile.setText(String.valueOf(oldUser.getPhoneNumber()));
                Glide.with(getContext())
                        .load(oldUser.getUrlProfilePhoto())
                        .placeholder(R.drawable.photo_default)
                        .circleCrop()
                        .into(binding.imagePhotoProfile);
                imageProfileBitmap = null;
            } else {
                Toast.makeText(getContext(), "Sem conexão com a internet.", Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnUpdateEmail.setOnClickListener(v -> startActivity(new Intent(getActivity(), UpdateEmail.class)));

        binding.etTelefoneProfile.addTextChangedListener(new FormatPhoneNumber(binding.etTelefoneProfile));

        binding.btnSaveUser.setOnClickListener(v -> {
            if (NetworkUtil.isInternetAvailable(getContext())) {
                if (binding.etTelefoneProfile.getText().toString().isEmpty() || binding.etTelefoneProfile.getText().toString().replaceAll("[()\\s-]", "")
                        .matches("^[1-9]{2}9[0-9]{7,8}$")) {
                    hideKeyboard();

                    User user = new User();

                    String date = binding.etDataNascimentoProfile.getText().toString();
                    String gender = binding.etGenderProfile.getText().toString();
                    String name = binding.etNomeProfile.getText().toString().trim();
                    String phone = binding.etTelefoneProfile.getText().toString();
                    String surname = binding.etSobrenomeProfile.getText().toString().trim();

                    user.setId(oldUser.getId());
                    user.setHistoryMessage(oldUser.getHistoryMessage());

                    if (!date.equals(oldUser.getBirthdate())) {
                        user.setBirthdate(date);
                    } else {
                        user.setBirthdate(oldUser.getBirthdate());
                    }

                    if (!surname.equals(oldUser.getSurname())) {
                        user.setSurname(surname);
                    } else {
                        user.setSurname(oldUser.getSurname());
                    }

                    if (!gender.equals(oldUser.getGender())) {
                        user.setGender(gender);
                    } else {
                        user.setGender(oldUser.getGender());
                    }

                    if (!name.equals(oldUser.getName())) {
                        user.setName(name);
                    } else {
                        user.setName(oldUser.getName());
                    }

                    if (!phone.equals(String.valueOf(oldUser.getPhoneNumber()))) {
                        if(!phone.isEmpty()) {
                            user.setPhoneNumber(Long.valueOf(phone.replaceAll("[()\\s-]", "")));
                        }
                    } else {
                        user.setPhoneNumber(oldUser.getPhoneNumber());
                    }

                    if (imageProfileBitmap != null) {
                        profileViewModel.saveProfilePhoto(imageProfileBitmap).observe(getViewLifecycleOwner(), url -> {
                            if (url != null) {
                                user.setUrlProfilePhoto(url);
                                user.setEmail(oldUser.getEmail());
                                profileViewModel.updateUser(user).observe(getViewLifecycleOwner(), aBoolean1 -> {
                                    if (aBoolean1) {
                                        Toast.makeText(getContext(), "Usuario salvo", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(getContext(), "Usuario não salvo", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } else {
                                Toast.makeText(getContext(), "Foto de perfil não salva", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        user.setUrlProfilePhoto(oldUser.getUrlProfilePhoto());
                        user.setEmail(oldUser.getEmail());
                        profileViewModel.updateUser(user).observe(getViewLifecycleOwner(), aBoolean1 -> {
                            if (aBoolean1) {
                                Toast.makeText(getContext(), "Usuario salvo", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getContext(), "Usuario não salvo", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } else {
                    Toast.makeText(getContext(), "Número de telefone inválido.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), "Sem conexão com a internet.", Toast.LENGTH_SHORT).show();
            }
        });

        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri uriImageSelected = result.getData().getData();
                        if (uriImageSelected != null) {
                            cutImage(uriImageSelected);
                        }

                    }
                });

        takePhotoLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        cutImage(fileDestination);
                    } else {
                        Toast.makeText(getContext(), "Erro ao tirar foto.", Toast.LENGTH_SHORT).show();
                    }
                });

        cropActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        final Uri resultUri = UCrop.getOutput(result.getData());
                        if (resultUri != null) {
                            Glide.with(getContext())
                                    .load(resultUri)
                                    .placeholder(R.drawable.photo_default)
                                    .circleCrop()
                                    .into(binding.imagePhotoProfile);
                            imageProfileBitmap = BitmapFactory.decodeFile(resultUri.getPath());
                        }
                    } else if (result.getResultCode() == UCrop.RESULT_ERROR && result.getData() != null) {
                        final Throwable cropError = UCrop.getError(result.getData());
                        if (cropError != null) {
                            Toast.makeText(getContext(), "Erro ao recortar a imagem. Tente novamente.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        binding.imagePhotoProfile.setOnClickListener(v -> {
            bottomSheetDialog = new BottomSheetDialog(getContext());
            View view = LayoutInflater.from(getContext()).inflate(R.layout.bottom_sheet_select_picture_profile, (ViewGroup) getActivity().getWindow().getDecorView(), false);
            bottomSheetDialog.setContentView(view);
            bottomSheetDialog.show();

            MaterialButton btnChangeImage = view.findViewById(R.id.btn_edit_image),
                    btnDeleteImage = view.findViewById(R.id.btn_delete_image),
                    btnTakePhoto = view.findViewById(R.id.btn_take_photo),
                    btnSetDefaultPhoto = view.findViewById(R.id.btn_set_default_photo);

            btnTakePhoto.setOnClickListener(v13 -> takePhoto());

            btnChangeImage.setOnClickListener(v1 -> this.pickImage());

            btnDeleteImage.setOnClickListener(v12 -> {
                Glide.with(getContext())
                        .load(oldUser.getUrlProfilePhoto())
                        .placeholder(R.drawable.photo_default)
                        .circleCrop()
                        .into(binding.imagePhotoProfile);
                imageProfileBitmap = null;
                bottomSheetDialog.dismiss();
            });

            btnSetDefaultPhoto.setOnClickListener(v5 -> {
                Glide.with(getContext())
                        .load(R.drawable.photo_default)
                        .circleCrop()
                        .into(binding.imagePhotoProfile);

                Drawable drawable = ContextCompat.getDrawable(requireContext(), R.drawable.photo_default);
                Bitmap bitmap = Bitmap.createBitmap(
                        drawable.getIntrinsicWidth(),
                        drawable.getIntrinsicHeight(),
                        Bitmap.Config.ARGB_8888
                );

                Canvas canvas = new Canvas(bitmap);
                drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                drawable.draw(canvas);

                imageProfileBitmap = bitmap;
                bottomSheetDialog.dismiss();
            });
        });

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

        binding.appBarTelaPrincipal.toolbar.inflateMenu(R.menu.menu_profile);

        int currentNightMode = getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK;

        MenuItem themeItem = binding.appBarTelaPrincipal.toolbar.getMenu().findItem(R.id.action_theme);
        if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
            themeItem.setIcon(R.drawable.ic_theme_light);  // Ícone para o tema claro
        } else {
            themeItem.setIcon(R.drawable.ic_theme_dark);   // Ícone para o tema escuro
        }

        binding.appBarTelaPrincipal.toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_about_us) {
                startActivity(new Intent(getActivity(), AboutUsView.class));
                return true;
            } else if (item.getItemId() == R.id.action_logout) {
                profileViewModel.logout();
                SharedPreferencesUtil.init(getContext());
                SharedPreferencesUtil.clearAll();
                startActivity(new Intent(getActivity(), HomeScreenView.class));
                requireActivity().finishAffinity();
                return true;
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
            }
            return false;
        });

        requestStoragePermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        pickImage();
                    } else {
                        Toast.makeText(getContext(), "Permissão necessária para acessar o armazenamento.", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // Inicialize o launcher para a permissão da câmera
        requestCameraPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        takePhoto();
                    } else {
                        Toast.makeText(getContext(), "Permissão necessária para tirar foto.", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        return binding.getRoot();
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager)
                getView().getContext().getSystemService(Context.INPUT_METHOD_SERVICE);

        if (imm != null) {
            imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
        }
    }

    private void pickImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            pickImageLauncher.launch(new Intent(Intent.ACTION_GET_CONTENT).setType("image/*"));
        } else {
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestStoragePermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            } else {
                pickImageLauncher.launch(new Intent(Intent.ACTION_PICK).setType("image/*"));
            }
        }
    }

    private void takePhoto() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) != PERMISSION_GRANTED) {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        } else {
            fileDestination = profileViewModel.getImageFile(getContext());
            if (fileDestination == null) {
                Toast.makeText(getContext(), "Erro ao tirar foto.", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileDestination);
                takePhotoLauncher.launch(takePictureIntent);
            }
        }
    }

    private void cutImage(Uri imagePath) {
        UCrop.Options options = new UCrop.Options();
        options.setToolbarColor(ContextCompat.getColor(getContext(), R.color.white));
        options.setStatusBarColor(ContextCompat.getColor(getContext(), R.color.white));
        options.setCircleDimmedLayer(true);

        UCrop uCrop = UCrop.of(imagePath, Uri.fromFile(new File(getActivity().getCacheDir(), imagePath.getLastPathSegment())))
                .withOptions(options)
                .withAspectRatio(1, 1);
        Intent intent = uCrop.getIntent(getContext());
        cropActivityResultLauncher.launch(intent);
        bottomSheetDialog.dismiss();
    }

    private void onHideKeyboard() {
        getActivity().findViewById(R.id.bottomNavigationView).setVisibility(View.GONE);
        binding.bottomNavigationView.bottomNavigationView.setVisibility(View.GONE);
    }

    private void onShowKeyboard() {
        getActivity().findViewById(R.id.bottomNavigationView).setVisibility(View.VISIBLE);
        binding.bottomNavigationView.bottomNavigationView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (keyboardListener != null) {
            binding.getRoot().getViewTreeObserver().removeOnGlobalLayoutListener(keyboardListener);
        }
        binding = null;
    }

}