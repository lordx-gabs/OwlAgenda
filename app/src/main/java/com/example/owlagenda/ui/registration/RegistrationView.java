package com.example.owlagenda.ui.registration;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.example.owlagenda.util.FormataTelefone;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegistrationView extends AppCompatActivity {
    private RegistrationViewModel registrationViewModel;
    private static final int PICK_IMAGE_REQUEST = 1;
    private final int REQUEST_IMAGE_CAPTURE = 5;
    private User user;
    private ImageView userProfileImage;
    private TextInputEditText nameEditText, surnameEditText, emailEditText, passwordEditText, birthdateEditText, phoneNumberEditText, confirmPasswordEditText;
    private Uri imageUri;
    private Calendar calendar;
    private DatePickerDialog datePickerDialog;
    private AutoCompleteTextView genderAutoCompleteTextView;
    private String[] genderOptions = {"", "Masculino", "Feminino", "Outros", "Prefiro não informar"};
    private LinearProgressIndicator loadingProgressBar;
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

        calendar = Calendar.getInstance();

        registrationViewModel = new ViewModelProvider(this).get(RegistrationViewModel.class);

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
        loadingProgressBar = findViewById(R.id.barra_carregando);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, genderOptions);
        genderAutoCompleteTextView.setAdapter(adapter);

        datePickerDialog = new DatePickerDialog(RegistrationView.this, (view, year, month, dayOfMonth) -> {
            Calendar dataSelecionado = Calendar.getInstance();
            dataSelecionado.set(year, month, dayOfMonth);
            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy", new Locale("pt", "BR"));
            birthdateEditText.setText(format.format(dataSelecionado.getTime()));

        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        Calendar dateStart = Calendar.getInstance();
        dateStart.set(1930, Calendar.JANUARY, 1);
        datePickerDialog.getDatePicker().setMinDate(dateStart.getTimeInMillis());

        Calendar dateFinal = Calendar.getInstance();
        dateFinal.add(Calendar.YEAR, -16);
        datePickerDialog.getDatePicker().setMaxDate(dateFinal.getTimeInMillis());

        birthdateEditText.setOnClickListener(v -> datePickerDialog.show());

        phoneNumberEditText.addTextChangedListener(new FormataTelefone(phoneNumberEditText));

        emailEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                TextInputLayout emailLayout = findViewById(R.id.et_email_layout);
                if (isEmailValid(s.toString())) {
                    int cor = getColor(R.color.botao_cor);
                    emailLayout.setBoxStrokeColor(cor);
                } else {
                    int cor = getColor(R.color.cor_primaria);
                    emailLayout.setBoxStrokeColor(cor);
                }
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
                new ActivityResultContracts.StartActivityForResult(), resul -> {
                    if (resul.getResultCode() == RESULT_OK) {
                        cutImage(imageUri);
                    } else {
                        Toast.makeText(this, "Erro ao tirar foto.", Toast.LENGTH_SHORT).show();
                    }
                });

        requestWriteAccessLauncher = registerForActivityResult(
                new ActivityResultContracts.StartIntentSenderForResult(), result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                            takePhotoLauncher.launch(takePictureIntent);
                        }
                    } else {
                        Toast.makeText(this, "Permissão necessária para tirar fotos.", Toast.LENGTH_SHORT).show();
                    }
                });

        registrationViewModel.isLoading().observe(this, aBoolean -> {
            MaterialButton btnRegister = findViewById(R.id.btn_cadastrar);
            if (aBoolean) {
                btnRegister.setEnabled(false);
                loadingProgressBar.setVisibility(View.VISIBLE);
            } else {
                loadingProgressBar.setVisibility(View.GONE);
                btnRegister.setEnabled(true);
            }
        });

        registrationViewModel.getErrorMessageLiveData().observe(this, s ->
                Toast.makeText(this, s, Toast.LENGTH_SHORT).show());
    }

    public void registerUser(View view) {
        String name = (nameEditText.getText()).toString();
        String surname = (surnameEditText.getText()).toString();
        String email = (emailEditText.getText()).toString();
        String password = (passwordEditText.getText()).toString();
        String birthdate = null;
        String gender = genderAutoCompleteTextView.getText().toString();
        String confirmPassword = (confirmPasswordEditText.getText()).toString();
        long phoneNumber = 0;

        if (!(birthdateEditText.getText()).toString().isEmpty()) {
            birthdate = birthdateEditText.getText().toString();
        }

        if (!(phoneNumberEditText.getText()).toString().isEmpty()) {
            phoneNumber = Long.parseLong(phoneNumberEditText.getText().toString().replaceAll("\\D", ""));
        }

        if (!name.isEmpty() && !surname.isEmpty() && !email.isEmpty() && !password.isEmpty() && !confirmPassword.isEmpty() && isEmailValid(email) && !gender.isEmpty()) {
            if (password.equals(confirmPassword)) {
                user.setNome(name);
                user.setSobrenome(surname);
                user.setEmail(email);
                user.setSenha(password);
                user.setData_aniversario(birthdate);
                user.setSexo(gender);
                user.setNumeroTelefone(phoneNumber);

                registrationViewModel.storeImageInStorage(imageUri).observe(this, s -> {
                    if (s != null) {
                        user.setUrl_foto_perfil(s);
                        registerUserInDatabase(user);
                    } else {
                        Toast.makeText(RegistrationView.this, "Erro ao cadastra a foto de perfil, tente novamente.", Toast.LENGTH_SHORT).show();
                    }
                });

            } else {
                Toast.makeText(this, "As senhas precisam ser iguais.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Todos os campos obrigatorios precisam ser preenchidos.", Toast.LENGTH_SHORT).show();
        }
    }

    private void registerUserInDatabase(User user) {
        registrationViewModel.registrationUserInDatabase(user).observe(this, success -> {
            if (success) {
                Toast.makeText(this, "Cadastro realizado com success!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Erro ao cadastrar o usuário. Por favor, tente novamente.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void pickImage() {
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

    public void takePhoto() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_IMAGE_CAPTURE);
        } else {
            File photoFile = createImageFileAvatar();
            imageUri = FileProvider.getUriForFile(this,
                    "com.example.owlagenda.fileprovider",
                    photoFile);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                long mediaID = getFilePathToPhotoID(photoFile.getAbsolutePath(), getApplicationContext());
                Uri uriImageCamera = ContentUris.withAppendedId(MediaStore.Images.Media.getContentUri("external"), mediaID);

                IntentSender intentSender = MediaStore.createWriteRequest(getContentResolver(), Collections.singletonList(uriImageCamera)).getIntentSender();
                requestWriteAccessLauncher.launch(new IntentSenderRequest.Builder(intentSender).build());
            } else {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                    takePhotoLauncher.launch(takePictureIntent);
                }

            }
        }
    }

    public long getFilePathToPhotoID(String imagePath, Context context) {
        long id = 0;
        ContentResolver cr = context.getContentResolver();

        Uri uri = MediaStore.Files.getContentUri("external");
        String selection = MediaStore.Images.Media.DATA;
        String[] selectionArgs = {imagePath};
        String[] projection = {MediaStore.Images.Media._ID};
        String sortOrder = MediaStore.Images.Media.TITLE + " ASC";

        Cursor cursor = cr.query(uri, projection, selection + "=?", selectionArgs, sortOrder);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                int idIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
                id = Long.parseLong(cursor.getString(idIndex));
            }
        }

        cursor.close();
        return id;
    }

    private File createImageFileAvatar() {
        Bitmap bitmap = ((BitmapDrawable) ContextCompat.getDrawable(this, R.drawable.ic_calendario))
                .getBitmap();

        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File outputFile = null;
        try {
            outputFile = File.createTempFile("avatar", ".png", storageDir);
            OutputStream outputStream = new FileOutputStream(outputFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.close();
        } catch (IOException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        return outputFile;
    }

    public void clipsClick(View v) {
        bottomSheetDialog = new BottomSheetDialog(this);
        // pode não rodar
        View view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_login, (ViewGroup) this.getWindow().getDecorView(), false);
        bottomSheetDialog.setContentView(view);
        bottomSheetDialog.show();

        MaterialButton btnChangeImage = view.findViewById(R.id.btn_alterar_imagem),
                btnDeleteImage = view.findViewById(R.id.btn_excluir_imagem),
                btnTakePhoto = view.findViewById(R.id.btn_tirar_foto);

        btnTakePhoto.setOnClickListener(v13 -> takePhoto());

        btnChangeImage.setOnClickListener(v1 -> this.pickImage());

        btnDeleteImage.setOnClickListener(v12 -> {
            userProfileImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.avatar_1));
            imageUri = null;
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

    public boolean isEmailValid(String email) {
        String regex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            final Uri resultadoUri = UCrop.getOutput(data);
            if (resultadoUri != null) {
                userProfileImage.setImageURI(null);
                userProfileImage.setImageURI(resultadoUri);
                imageUri = resultadoUri;
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
        outState.putParcelable("imageUri", imageUri);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Uri imageUriRestored = savedInstanceState.getParcelable("imageUri");
        if (imageUriRestored != null) {
            userProfileImage.setImageURI(imageUriRestored);
            imageUri = imageUriRestored;
        }
    }
}
