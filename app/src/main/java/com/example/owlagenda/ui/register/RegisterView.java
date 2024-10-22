package com.example.owlagenda.ui.register;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.owlagenda.R;
import com.example.owlagenda.data.models.User;
import com.example.owlagenda.ui.login.LoginView;
import com.example.owlagenda.util.FormatPhoneNumber;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointBackward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class RegisterView extends AppCompatActivity {
    private RegisterViewModel registerViewModel;
    public static final int PICK_IMAGE_REQUEST = 1;
    private final int REQUEST_IMAGE_CAPTURE = 2;
    private User user;
    private ImageView userProfileImage;
    private TextInputEditText nameEditText, surnameEditText, emailEditText, passwordEditText, birthdateEditText, phoneNumberEditText, confirmPasswordEditText;
    private Bitmap imageProfileBitmap;
    private MaterialDatePicker<Long> materialDatePicker;
    private AutoCompleteTextView genderAutoCompleteTextView;
    private final String[] genderOptions = {"Masculino", "Feminino", "Outros", "Prefiro não informar"};
    private int genderSelected = -1;
    private LinearProgressIndicator loadingProgress;
    private ActivityResultLauncher<Intent> pickImageLauncher, takePhotoLauncher;
    private BottomSheetDialog bottomSheetDialog;
    private MaterialToolbar toolbar;
    private Uri fileDestination;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        this.setContentView(R.layout.activity_register);

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
        toolbar = findViewById(R.id.toolbar_register);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, genderOptions);
        genderAutoCompleteTextView.setAdapter(adapter);
        genderAutoCompleteTextView.setOnItemClickListener((parent, view, position, id) ->
                genderSelected = position);

        TimeZone timeZone = TimeZone.getTimeZone("America/Sao_Paulo");

        Calendar calendarEnd = Calendar.getInstance(timeZone);
        calendarEnd.set(1940, Calendar.JANUARY, 1);
        long dateStart = calendarEnd.getTimeInMillis();

        Calendar calendarStart = Calendar.getInstance(timeZone);
        calendarStart.add(Calendar.YEAR, -16);
        long dateEnd = calendarStart.getTimeInMillis();

        CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder()
                .setStart(dateStart)
                .setEnd(dateEnd)
                .setValidator(DateValidatorPointBackward.before(dateEnd));

        birthdateEditText.setOnClickListener(v -> {
            Calendar calendarInitialed = Calendar.getInstance(timeZone);
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
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

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
                        cutImage(fileDestination);
                    } else {
                        Toast.makeText(this, "Erro ao tirar foto.", Toast.LENGTH_SHORT).show();
                    }
                });

        MaterialButton btnRegister = findViewById(R.id.btn_cadastrar);
        registerViewModel.isLoading().observe(this, aBoolean -> {
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


        toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
    }

    public void registerUser(View view) {
        String name = nameEditText.getText().toString().trim();
        String surname = surnameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString();
        String confirmPassword = confirmPasswordEditText.getText().toString();
        String birthdate = null;
        String gender = null;
        Long phoneNumber = null;

        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);

        if (!(birthdateEditText.getText()).toString().isEmpty()) {
            birthdate = birthdateEditText.getText().toString();
        }

        if (!(phoneNumberEditText.getText()).toString().isEmpty()) {
            phoneNumber = Long.valueOf(phoneNumberEditText.getText().toString().replaceAll("\\D", ""));
        }

        if (genderSelected > -1) {
            gender = genderAutoCompleteTextView.getText().toString();
        }
        if (password.length() < 6 || confirmPassword.length() < 6) {
            Toast.makeText(this, "A senha precisa ter no mínimo 6 caracteres.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!name.isEmpty() && !surname.isEmpty() && !email.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            if (phoneNumberEditText.getText().toString().isEmpty() || phoneNumberEditText.getText().toString()
                    .replaceAll("[()\\s-]", "").matches("^[1-9]{2}9[0-9]{8}$")) {
                if (password.equals(confirmPassword)) {
                    user.setName(name);
                    user.setSurname(surname);
                    user.setEmail(email);
                    user.setPassword(password);
                    user.setBirthdate(birthdate);
                    user.setGender(gender);
                    user.setPhoneNumber(phoneNumber);
                    user.setHistoryMessage(new ArrayList<>());

                    if (imageProfileBitmap == null) {
                        imageProfileBitmap = registerViewModel.getImageProfileDefaultBitmap(this);
                        if (imageProfileBitmap == null) {
                            Toast.makeText(this, "Erro ao carregar imagem padrão.", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }

                    registerViewModel.registerUser(user, imageProfileBitmap).observe(this, success -> {
                        if (success) {
                            Toast.makeText(this, "Cadastro realizado com success!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(this, LoginView.class)
                                    .putExtra("emailUser", user.getEmail())
                                    .putExtra("firstNameUser", user.getName()));
                            FirebaseAuth.getInstance().signOut();
                            finish();
                        } else {
                            Toast.makeText(this, "Erro ao cadastrar o usuário. Por favor, tente novamente.", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(this, "As senhas precisam ser iguais.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "O número de telefone precisa ser válido.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Todos os campos obrigatorios precisam ser preenchidos.", Toast.LENGTH_SHORT).show();
        }
    }

    private void pickImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            pickImageLauncher.launch(new Intent(Intent.ACTION_GET_CONTENT).setType("image/*"));
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PICK_IMAGE_REQUEST);
            } else {
                pickImageLauncher.launch(new Intent(Intent.ACTION_PICK).setType("image/*"));
            }
        }
    }

    private void takePhoto() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_IMAGE_CAPTURE);
        } else {
            fileDestination = registerViewModel.getImageFile(this);
            if (fileDestination == null) {
                Toast.makeText(this, "Erro ao tirar foto.", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileDestination);
                takePhotoLauncher.launch(takePictureIntent);
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
            imageProfileBitmap = null;
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
                imageProfileBitmap = BitmapFactory.decodeFile(resultadoUri.getPath());
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
            if (grantResults.length > 0 && grantResults[0] == PERMISSION_GRANTED) {
                pickImage();
            } else {
                Toast.makeText(this, "Permissão necessária para acessar o armazenamento.", Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            if (grantResults.length > 0 && grantResults[0] == PERMISSION_GRANTED) {
                takePhoto();
            } else {
                Toast.makeText(this, "Permissão necessária para tirar foto.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("imageUri", imageProfileBitmap);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Bitmap imageBitmapRestored = savedInstanceState.getParcelable("imageUri");
        if (imageBitmapRestored != null) {
            userProfileImage.setImageBitmap(imageBitmapRestored);
            imageProfileBitmap = imageBitmapRestored;
        }
    }

}
