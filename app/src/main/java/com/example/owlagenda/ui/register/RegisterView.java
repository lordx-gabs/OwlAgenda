package com.example.owlagenda.ui.register;

import android.Manifest;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.owlagenda.R;
import com.example.owlagenda.data.models.User;
import com.example.owlagenda.util.FormatPhoneNumber;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointBackward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Locale;
import java.util.TimeZone;

public class RegisterView extends AppCompatActivity {
    private RegisterViewModel registerViewModel;
    private static final int PICK_IMAGE_REQUEST = 1;
    private final int REQUEST_IMAGE_CAPTURE = 2;
    private User user;
    private ImageView userProfileImage;
    private TextInputEditText nameEditText, surnameEditText, emailEditText, passwordEditText, birthdateEditText, phoneNumberEditText, confirmPasswordEditText;
    private Uri imageProfileUri;
    private MaterialDatePicker<Long> materialDatePicker;
    private AutoCompleteTextView genderAutoCompleteTextView;
    private final String[] genderOptions = {"Masculino", "Feminino", "Outros", "Prefiro não informar"};
    private int genderSelected = -1;
    private LinearProgressIndicator loadingProgress;
    private ActivityResultLauncher<Intent> pickImageLauncher, takePhotoLauncher;
    private ActivityResultLauncher<IntentSenderRequest> requestWriteAccessLauncher;
    private BottomSheetDialog bottomSheetDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        this.setContentView(R.layout.activity_cadastro_view);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        registerViewModel = new ViewModelProvider(this).get(RegisterViewModel.class);

        user = new User();
        nameEditText = findViewById(R.id.et_nome);
        surnameEditText = findViewById(R.id.et_sobrenome);
        emailEditText = findViewById(R.id.et_email);
        passwordEditText = findViewById(R.id.et_senha);
        genderAutoCompleteTextView = findViewById(R.id.auto_complete_text_view);
        birthdateEditText = findViewById(R.id.et_data_nascimento);
        phoneNumberEditText = findViewById(R.id.et_telefone);
        userProfileImage = findViewById(R.id.foto_usuario);
        confirmPasswordEditText = findViewById(R.id.et_confirma_senha);
        loadingProgress = findViewById(R.id.barra_carregando);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, genderOptions);
        genderAutoCompleteTextView.setAdapter(adapter);
        genderAutoCompleteTextView.setOnItemClickListener((parent, view, position, id) ->
                genderSelected = position);

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

        birthdateEditText.setOnClickListener(v -> {
            Calendar calendarInitialed = Calendar.getInstance();
            if (birthdateEditText.getText().toString().isEmpty()) {
                calendarInitialed.setTimeInMillis(dateEnd);
            } else {
                try {
                    calendarInitialed.setTime(new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            .parse(birthdateEditText.getText().toString()));
                } catch (ParseException e) {
                    Toast.makeText(this, "Erro ao formatar a data.", Toast.LENGTH_SHORT).show();
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

                birthdateEditText.setText(dateFormat.format(dateSelected.getTime()));
            });

            if (!materialDatePicker.isAdded()) {
                materialDatePicker.show(getSupportFragmentManager(), "material_date_picker");
            }
        });

        phoneNumberEditText.addTextChangedListener(new FormatPhoneNumber(phoneNumberEditText));

        emailEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                TextInputLayout textInputLayout = findViewById(R.id.et_email_layout_register);
                int boxStrokeColor;
                if (Patterns.EMAIL_ADDRESS.matcher(emailEditText.getText().toString()).matches()) {
                    boxStrokeColor = getColor(R.color.botao_cor);
                } else {
                    boxStrokeColor = getColor(R.color.cor_primaria);
                }
                textInputLayout.setBoxStrokeColor(boxStrokeColor);
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
                        cutImage(imageProfileUri);
                    } else {
                        Toast.makeText(this, "Erro ao tirar foto.", Toast.LENGTH_SHORT).show();
                    }
                });

        requestWriteAccessLauncher = registerForActivityResult(
                new ActivityResultContracts.StartIntentSenderForResult(), result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageProfileUri);
                            takePhotoLauncher.launch(takePictureIntent);
                        }
                    } else {
                        Toast.makeText(this, "Permissão necessária para tirar fotos.", Toast.LENGTH_SHORT).show();
                    }
                });

        registerViewModel.isLoading().observe(this, aBoolean -> {
            MaterialButton btnRegister = findViewById(R.id.btn_cadastrar);
            if (aBoolean) {
                btnRegister.setEnabled(false);
                loadingProgress.setVisibility(View.VISIBLE);
            } else {
                loadingProgress.setVisibility(View.GONE);
                btnRegister.setEnabled(true);
            }
        });

        registerViewModel.getErrorMessageLiveData().observe(this, s ->
                Toast.makeText(this, s, Toast.LENGTH_SHORT).show());


    }

    public void registerUser(View view) {
        String name = (nameEditText.getText()).toString();
        String surname = (surnameEditText.getText()).toString();
        String email = (emailEditText.getText()).toString();
        String password = (passwordEditText.getText()).toString();
        String confirmPassword = (confirmPasswordEditText.getText()).toString();
        String birthdate = null;
        String gender = null;
        long phoneNumber = 0;

        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);

        if (!(birthdateEditText.getText()).toString().isEmpty()) {
            birthdate = birthdateEditText.getText().toString();
        }

        if (!(phoneNumberEditText.getText()).toString().isEmpty()) {
            phoneNumber = Long.parseLong(phoneNumberEditText.getText().toString().replaceAll("\\D", ""));
        }

        if (genderSelected > -1) {
            gender = genderAutoCompleteTextView.getText().toString();
        }

        if (!name.isEmpty() && !surname.isEmpty() && !email.isEmpty() && !password.isEmpty() && !confirmPassword.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            if (password.equals(confirmPassword)) {
                user.setName(name);
                user.setSurname(surname);
                user.setEmail(email);
                user.setPassword(password);
                user.setData_aniversario(birthdate);
                user.setGender(gender);
                user.setPhoneNumber(phoneNumber);

                if (imageProfileUri == null) {
                    try {
                        imageProfileUri = Uri.fromFile(registerViewModel.createImageProfileDefaultFile(this));
                    } catch (IOException e) {
                        Toast.makeText(this, "Erro: " + e, Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                registerViewModel.registerUser(user, imageProfileUri).observe(this, success -> {
                    if (success) {
                        //colocar que o email será enviado
                        Toast.makeText(this, "Cadastro realizado com success!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Erro ao cadastrar o usuário. Por favor, tente novamente.", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(this, "As senhas precisam ser iguais.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Todos os campos obrigatorios precisam ser preenchidos.", Toast.LENGTH_SHORT).show();
        }
    }

    private void pickImage() {
        Intent pickImageIntent = new Intent(Intent.ACTION_PICK);
        pickImageIntent.setType("image/*");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, PICK_IMAGE_REQUEST);
            } else {
                pickImageLauncher.launch(pickImageIntent);
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PICK_IMAGE_REQUEST);
            } else {
                pickImageLauncher.launch(pickImageIntent);
            }
        }
    }

    private void takePhoto() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_IMAGE_CAPTURE);
        } else {
            File photoFile;
            try {
                photoFile = registerViewModel.createImageProfileDefaultFile(getApplicationContext());
            } catch (IOException e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                long mediaID = registerViewModel.getFilePathToPhotoID(photoFile.getAbsolutePath()
                        ,getApplicationContext());
                Uri uriImageCamera = ContentUris.withAppendedId(MediaStore.Images.Media
                        .getContentUri("external"), mediaID);

                IntentSender intentSender = MediaStore.createWriteRequest(getContentResolver()
                        ,Collections.singletonList(uriImageCamera)).getIntentSender();
                requestWriteAccessLauncher.launch(new IntentSenderRequest.Builder(intentSender).build());
            } else {
                imageProfileUri = FileProvider.getUriForFile(this,
                        "com.example.owlagenda.fileprovider",
                        photoFile);
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageProfileUri);
                    takePhotoLauncher.launch(takePictureIntent);
                }

            }
        }
    }

    public void clipsClick(View v) {
        bottomSheetDialog = new BottomSheetDialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_select_picture, (ViewGroup) this.getWindow().getDecorView(), false);
        bottomSheetDialog.setContentView(view);
        bottomSheetDialog.show();

        MaterialButton btnChangeImage = view.findViewById(R.id.btn_alterar_imagem),
                btnDeleteImage = view.findViewById(R.id.btn_excluir_imagem),
                btnTakePhoto = view.findViewById(R.id.btn_tirar_foto);

        btnTakePhoto.setOnClickListener(v13 -> takePhoto());

        btnChangeImage.setOnClickListener(v1 -> this.pickImage());

        btnDeleteImage.setOnClickListener(v12 -> {
            userProfileImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.avatar_1));
            imageProfileUri = null;
            bottomSheetDialog.dismiss();
        });
    }

    private void cutImage(Uri imagePath) {
        UCrop.Options options = new UCrop.Options();
        options.setToolbarColor(ContextCompat.getColor(this, R.color.white));
        options.setStatusBarColor(ContextCompat.getColor(this, R.color.white));
        options.setCircleDimmedLayer(true);

        UCrop uCrop = UCrop.of(imagePath, Uri.fromFile(new File(getCacheDir(), imagePath.getLastPathSegment())))
                .withOptions(options)
                .withAspectRatio(1, 1);
        uCrop.start(this);
        bottomSheetDialog.dismiss();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            final Uri resultadoUri = UCrop.getOutput(data);
            if (resultadoUri != null) {
                userProfileImage.setImageURI(null);
                userProfileImage.setImageURI(resultadoUri);
                imageProfileUri = resultadoUri;
            }
        } else if (resultCode == UCrop.RESULT_ERROR) {
            final Throwable cropError = UCrop.getError(data);
            if (cropError != null) {
                Toast.makeText(this, "Erro ao recortar a imagem. Tente novamente.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PICK_IMAGE_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pickImage();
            } else {
                Toast.makeText(this, "Permissão necessária para acessar o armazenamento.", Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                takePhoto();
            } else {
                Toast.makeText(this, "Permissão necessária para tirar foto.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("imageUri", imageProfileUri);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Uri imageUriRestored = savedInstanceState.getParcelable("imageUri");
        if (imageUriRestored != null) {
            userProfileImage.setImageURI(imageUriRestored);
            imageProfileUri = imageUriRestored;
        }
    }
}