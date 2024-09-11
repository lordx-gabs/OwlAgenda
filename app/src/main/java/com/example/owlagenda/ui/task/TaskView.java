package com.example.owlagenda.ui.task;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.owlagenda.R;
import com.example.owlagenda.data.models.SchoolClass;
import com.example.owlagenda.data.models.School;
import com.example.owlagenda.data.models.Task;
import com.example.owlagenda.data.models.TaskAttachments;
import com.example.owlagenda.databinding.ActivityTaskBinding;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointBackward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class TaskView extends AppCompatActivity {
    private final int PICK_IMAGE_REQUEST = 78;
    private ActivityResultLauncher<Intent> pickImageLauncher;
    private TaskViewModel viewModel;
    private ActivityTaskBinding binding;
    private ArrayList<School> schools;
    private ArrayAdapter<String> adapterSchool;
    private School schoolSelected;
    private ArrayList<SchoolClass> schoolClasses;
    private ArrayAdapter<String> adapterClass;
    private SchoolClass schoolClassSelected;
    private ArrayList<TaskAttachments> documents;
    private DocumentAdapter documentAdapter;
    private BottomSheetDialog bottomSheetDialog;
    private MaterialDatePicker<Long> materialDatePicker;
    int index = -1;
    private String nameClass, numberOfStudents, period;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityTaskBinding.inflate(getLayoutInflater());
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        viewModel = new ViewModelProvider(this).get(TaskViewModel.class);

        setContentView(binding.getRoot());
        documents = new ArrayList<>();

        viewModel.getSchools().observe(this, schools -> {
            if (schools == null) {
                Toast.makeText(this, "Não foi possivel carregar as escolas.", Toast.LENGTH_SHORT).show();
            } else {
                this.schools = schools;
                ArrayList<String> schoolsName = new ArrayList<>();
                schoolsName.add("Nova escola");

                schools.forEach(school -> {
                    schoolsName.add(school.getSchoolName());
                    Log.e("teste", school.getSchoolName());
                });

                adapterSchool = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, schoolsName);
                adapterSchool.notifyDataSetChanged();
            }
        });

        viewModel.getClasses().observe(this, classes -> {
            if (classes == null) {
                Toast.makeText(this, "Não foi possivel carregar as classes.", Toast.LENGTH_SHORT).show();
            } else {
                this.schoolClasses = classes;
                ArrayList<String> classesName = new ArrayList<>();
                classesName.add("Adicionar nova classe");
                classes.forEach(schoolClassData -> classesName.add(schoolClassData.getClassName()));
                adapterClass = new ArrayAdapter<>(this, R.layout.dropdown_layout, classesName);
                binding.autoCompleteClass.setAdapter(adapterClass);
                adapterClass.notifyDataSetChanged();
            }
        });


        binding.autoCompleteClass.setOnItemClickListener((parent, view, position, id) -> {
                    if (position == 0) {
                        binding.autoCompleteClass.setText("");
                        index = -1;
                        bottomSheetDialog = new BottomSheetDialog(this);
                        View view5 = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_add_class, (ViewGroup) this.getWindow().getDecorView(), false);
                        bottomSheetDialog.setContentView(view5);
                        bottomSheetDialog.show();

                        TextInputEditText etNameClass = view5.findViewById(R.id.edt_name_class);
                        TextInputEditText etNumberOfStudents = view5.findViewById(R.id.edt_number_students_class);
                        MaterialButton btnAddClass = view5.findViewById(R.id.btn_add_class);

                        AutoCompleteTextView autoCompletePeriod = view5.findViewById(R.id.auto_complete_period_class);
                        String[] periods = new String[]{"Manhã", "Tarde", "Noite"};
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, periods);
                        autoCompletePeriod.setAdapter(adapter);

                        nameClass = "";
                        numberOfStudents = "";
                        period = "";
                        bottomSheetDialog.setOnDismissListener(dialog -> {
                            nameClass = etNameClass.getText().toString();
                            numberOfStudents = etNumberOfStudents.getText().toString();
                            period = autoCompletePeriod.getText().toString();
                        });

                        AutoCompleteTextView autoCompleteSchool = view5.findViewById(R.id.auto_complete_school_class);
                        autoCompleteSchool.setAdapter(adapterSchool);
                        autoCompleteSchool.setOnItemClickListener((parent1, view1, position1, id1) -> {
                            if (position1 == 0) {
                                this.bottomSheetDialog.dismiss();
                                autoCompleteSchool.setText("");
                                autoCompleteSchool.clearFocus();
                                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
                                View view9 = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_add_school, (ViewGroup) this.getWindow().getDecorView(), false);
                                bottomSheetDialog.setContentView(view9);
                                bottomSheetDialog.show();
                                bottomSheetDialog.setOnDismissListener(dialog -> {
                                    this.bottomSheetDialog.show();
                                    etNameClass.setText(nameClass);
                                    etNumberOfStudents.setText(numberOfStudents);
                                    autoCompletePeriod.setText(period);
                                });

                                TextInputEditText etNameSchool = view9.findViewById(R.id.edt_nome_escola);
                                MaterialButton btnAddSchool = view9.findViewById(R.id.btn_add_escola);

                                btnAddSchool.setOnClickListener(v -> {
                                    School school = new School();
                                    school.setSchoolName(etNameSchool.getText().toString());
                                    school.setUserId(FirebaseFirestore.getInstance().collection("usuario").document(
                                            FirebaseAuth.getInstance().getCurrentUser().getUid()
                                    ));
                                    school.setId(FirebaseFirestore.getInstance().collection("escola").document().getId());
                                    viewModel.saveSchool(school).observe(this, aBoolean -> {
                                        if (aBoolean) {
                                            bottomSheetDialog.dismiss();
                                            Toast.makeText(this, "Escola adicionada com sucesso.", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(this, "Erro ao adicionar escola.", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                });

                            } else {
                                this.schoolSelected = schools.get(position1 - 1);
                            }
                        });

                        btnAddClass.setOnClickListener(v -> {
                            if (!etNameClass.getText().toString().isEmpty() && !etNumberOfStudents.getText().toString().isEmpty()
                                    && !autoCompletePeriod.getText().toString().isEmpty() && index != -1) {
                                SchoolClass dataSchoolClass = new SchoolClass();
                                dataSchoolClass.setNumberOfStudents(Integer.parseInt(etNumberOfStudents.getText().toString()));
                                dataSchoolClass.setClassName(etNameClass.getText().toString());
                                dataSchoolClass.setPeriod(autoCompletePeriod.getText().toString());
                                dataSchoolClass.setUserId(FirebaseFirestore.getInstance().collection("usuario").document(
                                        FirebaseAuth.getInstance().getCurrentUser().getUid())
                                );
                                dataSchoolClass.setSchoolId(FirebaseFirestore.getInstance().collection("escola").document(
                                        schools.get(index).getId())
                                );
                                dataSchoolClass.setId(FirebaseFirestore.getInstance().collection("turma").document().getId());
                                viewModel.saveClass(dataSchoolClass).observe(this, aBoolean -> {
                                    if (aBoolean) {
                                        bottomSheetDialog.dismiss();
                                        Toast.makeText(TaskView.this, "Classe adicionada com sucesso.", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(TaskView.this, "Erro ao adicionar classe.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } else {
                                Toast.makeText(this, "Preencha todos os campos.", Toast.LENGTH_SHORT).show();
                            }
                        });

                    } else {
                        this.schoolClassSelected = schoolClasses.get(position - 1);
                    }
                }
        );

        pickImageLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        if (result.getData().getData() != null) {
                            documentAdapter.getDocuments().add(new TaskAttachments(
                                    viewModel.getFileName(result.getData().getData(), this),
                                    result.getData().getData().toString()
                            ));
                            documentAdapter.notifyItemInserted(documentAdapter.getItemCount() - 1);
                        } else {
                            Toast.makeText(this, "Nenhum arquivo selecionado", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        documentAdapter = new DocumentAdapter(position -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                pickImageLauncher.launch(Intent.createChooser(
                        new Intent(Intent.ACTION_GET_CONTENT).setType("*/*")
                        , "Selecione um arquivo")
                );
            } else {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PICK_IMAGE_REQUEST);
                } else {
                    pickImageLauncher.launch(Intent.createChooser(
                            new Intent(Intent.ACTION_PICK).setType("*/*")
                            , "Selecione um arquivo")
                    );
                }
            }
        }, position -> {
            bottomSheetDialog = new BottomSheetDialog(this);
            View view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_delete_document, (ViewGroup) this.getWindow().getDecorView(), false);
            bottomSheetDialog.setContentView(view);
            bottomSheetDialog.show();

            MaterialButton btnDeleteDocument = view.findViewById(R.id.btn_delete_document);
            btnDeleteDocument.setOnClickListener(v -> {
                documentAdapter.getDocuments().remove(position);
                documentAdapter.notifyItemRemoved(position);
                bottomSheetDialog.dismiss();
            });
        });

        binding.recycleDocument.setLayoutManager(new LinearLayoutManager(this
                , LinearLayoutManager.HORIZONTAL, false));
        binding.recycleDocument.setAdapter(documentAdapter);

        binding.btnSaveTask.setOnClickListener(v -> {
            //TODO:Add notifacção
            String txtNameTask = binding.etNameTask.getText().toString();
            String txtDescriptionTask = binding.etDescriptionTask.getText().toString();
            String txtDateTask = binding.etDateTask.getText().toString();
            String txtTagTask = binding.autoCompleteTag.getText().toString();
            if (txtNameTask.isEmpty() && txtDateTask.isEmpty()
                    && schoolSelected == null && schoolClassSelected == null) {
                Toast.makeText(this, "Preencha todos os campos.", Toast.LENGTH_SHORT).show();
            } else {
                DocumentReference userRef = FirebaseFirestore.getInstance().collection("usuario")
                        .document(FirebaseAuth.getInstance().getCurrentUser().getUid());
                DocumentReference schoolRef = FirebaseFirestore.getInstance().collection("escola")
                        .document(schoolSelected.getId());
                DocumentReference classRef = FirebaseFirestore.getInstance().collection("turma")
                        .document(schoolClassSelected.getId());

                Task task = new Task(userRef,
                        txtNameTask,
                        txtDescriptionTask,
                        txtDateTask,
                        schoolRef,
                        classRef,
                        txtTagTask,
                        documentAdapter.getDocuments()
                );
                viewModel.addTask(task).observe(this, aBoolean -> {
                    if (aBoolean) {
                        Toast.makeText(this, "Tarefa adicionada com sucesso.", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Erro ao adicionar tarefa.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

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


        binding.etDateTask.setOnClickListener(v -> {
            Calendar calendarInitialed = Calendar.getInstance();
            if (binding.etDateTask.getText().toString().isEmpty()) {
                calendarInitialed.setTimeInMillis(dateEnd);
            } else {
                try {
                    calendarInitialed.setTime(new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            .parse(binding.etDateTask.getText().toString()));
                } catch (ParseException e) {
                    Toast.makeText(this, "Erro ao formatar a data.", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            materialDatePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Selecione uma data da sua tarefa")
                    .setCalendarConstraints(constraintsBuilder.build())
                    .setSelection(calendarInitialed.getTimeInMillis())
                    .build();

            materialDatePicker.addOnPositiveButtonClickListener(selection -> {
                Calendar dateSelected = Calendar.getInstance();
                dateSelected.setTimeInMillis(selection);
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", new Locale("pt", "BR"));
                dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

                binding.etDateTask.setText(dateFormat.format(dateSelected.getTime()));
            });

            if (!materialDatePicker.isAdded()) {
                materialDatePicker.show(getSupportFragmentManager(), "material_date_picker");
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
