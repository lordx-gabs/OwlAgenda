package com.example.owlagenda.ui.task;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
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
import com.example.owlagenda.util.NotificationUtil;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.TimeZone;
import java.util.stream.Collectors;

public class TaskView extends AppCompatActivity {
    private static final int REQUEST_CODE_NOTIFICATION = 501;
    private ActivityResultLauncher<Intent> pickDocumentLauncher;
    private TaskViewModel viewModel;
    private ActivityTaskBinding binding;
    private ArrayList<School> schools;
    private ArrayAdapter<String> adapterSchool;
    private School schoolSelected;
    private ArrayList<SchoolClass> schoolClasses;
    private ArrayAdapter<String> adapterClass;
    private SchoolClass schoolClassSelected;
    private DocumentAdapter documentAdapter;
    private BottomSheetDialog bottomSheetDialogClass;
    private MaterialDatePicker<Long> materialDatePicker;
    private String nameClass, numberOfStudents, period;
    private ArrayAdapter<String> adapterNotification;
    private int indexNotifications = 1;
    private ArrayList<String> classesName;
    private final String[] notificationsMinutes = new String[]{"Sem notificação",
            "12 Horas antes",
            "1 Dia antes",
            "2 Dias antes",
            "3 Dias antes"};
    private final String[] tagsTask = new String[]{"Prova",
            "Atividade Avaliativa",
            "Trabalho de Casa",
            "Relatório",
            "Projeto",
            "Atividade Extra-Curricular",
            "Evento Escolar",
            "Reunião com Pais",
            "Preparação de Aula",
            "Atividade de Desenvolvimento Profissional",
            "Outros"};

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

        viewModel.isLoading().observe(this, aBoolean -> {
            if (aBoolean) {
                binding.loadingTaskView.setVisibility(View.VISIBLE);
            } else {
                binding.loadingTaskView.setVisibility(View.GONE);
            }
        });

        viewModel.getErrorMessage().observe(this, s ->
                Toast.makeText(this, s, Toast.LENGTH_SHORT).show());

        setContentView(binding.getRoot());

        viewModel.getSchools().observe(this, schools -> {
            if (schools == null) {
                Toast.makeText(this, "Não foi possivel carregar as escolas.", Toast.LENGTH_SHORT).show();
            } else {
                this.schools = schools;
                ArrayList<String> schoolsName = new ArrayList<>();
                schoolsName.add("Nova escola");

                schools.forEach(school -> schoolsName.add(school.getSchoolName()));
                adapterSchool = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, schoolsName);
                adapterSchool.notifyDataSetChanged();

                viewModel.getClasses().observe(this, classes -> {
                    if (classes == null) {
                        Toast.makeText(this, "Não foi possivel carregar as classes.", Toast.LENGTH_SHORT).show();
                    } else {
                        this.schoolClasses = classes;
                        classesName = new ArrayList<>();
                        classesName.add("Adicionar nova classe");
                        // Mapeia cada classe para o nome da escola correspondente e coleta os nomes em uma lista
                        List<String> schoolNames = classes.stream()
                                .map(classItem -> {
                                    String classSchoolRef = classItem.getSchoolId().getId();

                                    // Encontra a escola correspondente
                                    return schools.stream()
                                            .filter(school -> classSchoolRef.equals(school.getId()))
                                            .map(School::getSchoolName)
                                            .findFirst()
                                            .orElse("Escola Desconhecida");
                                })
                                .collect(Collectors.toList()); 

                        for (int i = 0; i < schoolNames.size(); i++) {
                            classesName.add(schoolNames.get(i) + " - " + classes.get(i).getClassName());
                        }
                        adapterClass = new ArrayAdapter<>(this, R.layout.dropdown_layout, classesName);
                        binding.autoCompleteClass.setAdapter(adapterClass);
                        adapterClass.notifyDataSetChanged();
                    }
                });
            }
        });

        binding.autoCompleteClass.setOnItemClickListener((parent, view, position, id) -> {
            List<String> filteredItems = new ArrayList<>();
            for (int i = 0; i < binding.autoCompleteClass.getAdapter().getCount(); i++) {
                if (binding.autoCompleteClass.getAdapter().getItem(i).toString().contains(" - ")) {
                    filteredItems.add(binding.autoCompleteClass.getAdapter().getItem(i).toString()
                            .split(" - ")[1]);
                } else {
                    filteredItems.add(binding.autoCompleteClass.getAdapter().getItem(i).toString());
                }
            }
            schoolSelected = null;
            if (filteredItems.get(position).equalsIgnoreCase("Adicionar nova classe")) {
                binding.autoCompleteClass.setText("");
                bottomSheetDialogClass = new BottomSheetDialog(this);
                View view5 = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_add_class, (ViewGroup) this.getWindow().getDecorView(), false);
                bottomSheetDialogClass.setContentView(view5);
                bottomSheetDialogClass.show();

                TextInputEditText etNameClass = view5.findViewById(R.id.edt_name_class);
                TextInputEditText etNumberOfStudents = view5.findViewById(R.id.edt_number_students_class);
                MaterialButton btnAddClass = view5.findViewById(R.id.btn_add_class);

                AutoCompleteTextView autoCompletePeriod = view5.findViewById(R.id.auto_complete_period_class);
                String[] periods = new String[]{"Manhã", "Tarde", "Noite"};
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, periods);
                autoCompletePeriod.setAdapter(adapter);
                autoCompletePeriod.setOnClickListener(v -> autoCompletePeriod.showDropDown());

                bottomSheetDialogClass.setOnDismissListener(dialog -> {
                    nameClass = etNameClass.getText().toString().trim();
                    numberOfStudents = etNumberOfStudents.getText().toString().trim();
                    period = autoCompletePeriod.getText().toString().trim();
                });

                AutoCompleteTextView autoCompleteSchool = view5.findViewById(R.id.auto_complete_school_class);
                autoCompleteSchool.setAdapter(adapterSchool);
                autoCompleteSchool.setOnClickListener(v -> autoCompleteSchool.showDropDown());

                autoCompleteSchool.setOnItemClickListener((parent1, view1, position1, id1) -> {
                    if (position1 == 0) {
                        this.bottomSheetDialogClass.dismiss();
                        autoCompleteSchool.setText("");
                        autoCompleteSchool.clearFocus();
                        BottomSheetDialog bottomSheetDialogSchool = new BottomSheetDialog(this);
                        View view9 = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_add_school, (ViewGroup) this.getWindow().getDecorView(), false);
                        bottomSheetDialogSchool.setContentView(view9);
                        bottomSheetDialogSchool.show();
                        bottomSheetDialogSchool.setOnDismissListener(dialog -> {
                            this.bottomSheetDialogClass.show();
                            etNameClass.setText(nameClass);
                            etNumberOfStudents.setText(numberOfStudents);
                            autoCompletePeriod.setText(period);
                        });

                        TextInputEditText etNameSchool = view9.findViewById(R.id.edt_nome_escola);
                        MaterialButton btnAddSchool = view9.findViewById(R.id.btn_add_escola);

                        btnAddSchool.setOnClickListener(v -> {
                            School school = new School();
                            school.setSchoolName(etNameSchool.getText().toString().trim());
                            school.setUserId(FirebaseFirestore.getInstance()
                                    .collection("usuario").document(
                                            FirebaseAuth.getInstance().getCurrentUser().getUid()
                                    ));
                            school.setSchoolNameSearch(etNameSchool.getText().toString().trim().toUpperCase());
                            school.setId(FirebaseFirestore.getInstance().collection("escola").document().getId());
                            viewModel.saveSchool(school).observe(this, aBoolean -> {
                                if (aBoolean) {
                                    bottomSheetDialogSchool.dismiss();
                                    autoCompleteSchool.setAdapter(adapterSchool);
                                    this.schoolSelected = school;
                                    autoCompleteSchool.setText(schoolSelected.getSchoolName(), false);
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
                    if (!etNameClass.getText().toString().trim().isEmpty() && !etNumberOfStudents.getText().toString().trim().isEmpty()
                            && !autoCompletePeriod.getText().toString().trim().isEmpty() && schoolSelected != null) {
                        SchoolClass dataSchoolClass = new SchoolClass();
                        dataSchoolClass.setClassNameSearch(etNameClass.getText().toString().trim().toUpperCase());
                        dataSchoolClass.setNumberOfStudents(Integer.parseInt(etNumberOfStudents.getText().toString().trim()));
                        dataSchoolClass.setClassName(etNameClass.getText().toString().trim());
                        dataSchoolClass.setPeriod(autoCompletePeriod.getText().toString().trim());
                        dataSchoolClass.setUserId(FirebaseFirestore.getInstance().collection("usuario").document(
                                FirebaseAuth.getInstance().getCurrentUser().getUid())
                        );
                        dataSchoolClass.setSchoolId(FirebaseFirestore.getInstance().collection("escola").document(
                                schoolSelected.getId()));
                        dataSchoolClass.setId(FirebaseFirestore.getInstance().collection("turma").document().getId());
                        viewModel.saveClass(dataSchoolClass).observe(this, aBoolean -> {
                            if (aBoolean) {
                                bottomSheetDialogClass.dismiss();
                                this.schoolClassSelected = dataSchoolClass;
                                binding.autoCompleteClass.setText(schoolClassSelected.getClassName(), false);
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
                Optional<SchoolClass> schoolClassSelected = schoolClasses.stream()
                        .filter(schoolClass -> schoolClass.getClassName()
                                .equalsIgnoreCase(filteredItems.get(position)))
                        .findFirst();
                this.schoolClassSelected = schoolClassSelected.orElse(null);
            }
        });

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.dropdown_layout, tagsTask);
        binding.autoCompleteTag.setAdapter(adapter);

        pickDocumentLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri documentUri = result.getData().getData();
                        if (documentUri != null) {
                            if (documentAdapter.getDocuments().size() < 10) {
                                if (viewModel.getFileMbSize(this, documentUri) < 5) {
                                    String fileName = viewModel.getFileName(documentUri, this);
                                    if (documentAdapter.getDocuments().stream().noneMatch(taskAttachments ->
                                            taskAttachments.getName().equals(fileName))) {
                                        final int takeFlags = (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                                        getContentResolver().takePersistableUriPermission(documentUri, takeFlags);

                                        documentAdapter.getDocuments().add(new TaskAttachments(
                                                fileName, documentUri.toString()));
                                        documentAdapter.notifyItemInserted(documentAdapter.getItemCount() - 1);
                                    } else {
                                        Toast.makeText(this, "Esse arquivo já esta anexado a tarefa", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toast.makeText(this, "O arquivo deve ter o tamanho de até 5MB", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(this, "O número maximo de arquivos é 10", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(this, "Nenhum arquivo selecionado", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        documentAdapter = new DocumentAdapter(position ->
                pickDocumentLauncher.launch(Intent.createChooser(
                        new Intent(Intent.ACTION_OPEN_DOCUMENT).setType("*/*"), "Selecione um arquivo")
                ), position -> {
            bottomSheetDialogClass = new BottomSheetDialog(this);
            View view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_delete_document, (ViewGroup) this.getWindow().getDecorView(), false);
            bottomSheetDialogClass.setContentView(view);
            bottomSheetDialogClass.show();

            MaterialButton btnDeleteDocument = view.findViewById(R.id.btn_delete_document);
            btnDeleteDocument.setOnClickListener(v -> {
                documentAdapter.getDocuments().remove(position);
                documentAdapter.notifyItemRemoved(position);
                bottomSheetDialogClass.dismiss();
            });
        });

        binding.recycleDocument.setLayoutManager(new LinearLayoutManager(this
                , LinearLayoutManager.HORIZONTAL, false));
        binding.recycleDocument.setAdapter(documentAdapter);
        binding.autoCompleteNotifications.setOnItemClickListener((parent, view, position, id) ->
                indexNotifications = position);

        binding.btnSaveTask.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && indexNotifications != 0) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_CODE_NOTIFICATION);
                    return;
                }
            }
            saveTask();
        });

        Calendar calendarStart = Calendar.getInstance(TimeZone.getTimeZone("America/Sao_Paulo"));
        calendarStart.add(Calendar.YEAR, -1);
        long dateStart = calendarStart.getTimeInMillis();

        Calendar calendarEnd = Calendar.getInstance(TimeZone.getTimeZone("America/Sao_Paulo"));
        calendarEnd.add(Calendar.YEAR, 1);
        long dateEnd = calendarEnd.getTimeInMillis();

        CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder()
                .setStart(dateStart)
                .setEnd(dateEnd);

        binding.etDateTask.setOnClickListener(v -> {
            Calendar calendarInitialed = Calendar.getInstance();
            if (binding.etDateTask.getText().toString().trim().isEmpty()) {
                calendarInitialed.setTimeInMillis(Calendar.getInstance(TimeZone
                                .getTimeZone("America/Sao_Paulo")).getTimeInMillis());
            } else {
                try {
                    calendarInitialed.setTime(new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            .parse(binding.etDateTask.getText().toString().trim()));
                } catch (ParseException e) {
                    Toast.makeText(this, "Erro ao formatar a data.", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            materialDatePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Selecione a data da sua tarefa")
                    .setSelection(calendarInitialed.getTimeInMillis())
                    .setCalendarConstraints(constraintsBuilder.build())
                    .build();

            materialDatePicker.addOnPositiveButtonClickListener(selection -> {
                Calendar dateSelected = Calendar.getInstance();
                dateSelected.setTimeInMillis(selection);
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy"
                        , new Locale("pt", "BR"));
                dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

                binding.etDateTask.setText(dateFormat.format(dateSelected.getTime()));
            });

            if (!materialDatePicker.isAdded()) {
                materialDatePicker.show(getSupportFragmentManager(), "material_date_picker");
            }
        });

        adapterNotification = new ArrayAdapter<>(this, R.layout.dropdown_layout, notificationsMinutes);
        binding.autoCompleteNotifications.setAdapter(adapterNotification);
        binding.autoCompleteNotifications.setText(notificationsMinutes[1], false);
        binding.autoCompleteNotifications.setOnClickListener(v ->
                binding.autoCompleteNotifications.showDropDown());

        binding.toolbarTask.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
    }

    private void saveTask() {
        String txtNameTask = binding.etNameTask.getText().toString().trim();
        String txtDescriptionTask = binding.etDescriptionTask.getText().toString().trim();
        String txtDateTask = binding.etDateTask.getText().toString();
        String txtTagTask = binding.autoCompleteTag.getText().toString();
        if (txtNameTask.isEmpty() || txtDateTask.isEmpty() || schoolClassSelected == null
                || txtTagTask.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos obrigátorios.", Toast.LENGTH_SHORT).show();
        } else {
            DocumentReference userRef = FirebaseFirestore.getInstance().collection("usuario")
                    .document(FirebaseAuth.getInstance().getCurrentUser().getUid());
            DocumentReference schoolRef = schoolClassSelected.getSchoolId();
            DocumentReference classRef = FirebaseFirestore.getInstance().collection("turma")
                    .document(schoolClassSelected.getId());

            Integer notificationBefore = switch (indexNotifications) {
                case 1 -> 720;
                case 2 -> 1440;
                case 3 -> 2880;
                case 4 -> 4320;
                default -> null;
            };

            Task task = new Task(userRef,
                    txtNameTask,
                    txtNameTask.toUpperCase(),
                    txtDescriptionTask,
                    txtDateTask,
                    schoolRef,
                    classRef,
                    txtTagTask,
                    documentAdapter.getDocuments(),
                    notificationBefore
            );
            Calendar calendar = Calendar.getInstance();
            if (notificationBefore != null) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                dateFormat.setTimeZone(TimeZone.getTimeZone("America/Sao_Paulo")); // Definir o fuso horário
                try {
                    // Faz o parse da data
                    calendar.setTime(dateFormat.parse(txtDateTask));
                    calendar.setTimeZone(TimeZone.getTimeZone("America/Sao_Paulo"));

                    Calendar currentCalendar = Calendar.getInstance(TimeZone.getTimeZone("America/Sao_Paulo"));
                    currentCalendar.setTimeZone(TimeZone.getTimeZone("America/Sao_Paulo"));
                    calendar.set(Calendar.HOUR_OF_DAY, currentCalendar.get(Calendar.HOUR_OF_DAY));
                    calendar.set(Calendar.MINUTE, currentCalendar.get(Calendar.MINUTE));
                    calendar.set(Calendar.SECOND, currentCalendar.get(Calendar.SECOND));

                    calendar.add(Calendar.MINUTE, +notificationBefore); // Subtrai o tempo de notificação
                } catch (ParseException e) {
                    Toast.makeText(this, "Erro ao converter data", Toast.LENGTH_SHORT).show();
                }
                if (isNotificationDateInFuture(calendar)) {
                    notificationBefore = null;
                }
            }

            Integer finalNotificationBefore = notificationBefore;
            viewModel.addTask(task).observe(this, aBoolean -> {
                if (aBoolean) {
                    if (finalNotificationBefore != null) {

                        int idNotification = 0;
                        try {
                            idNotification = Integer.parseInt(task.getId().replaceAll("[^0-9]", ""));
                        } catch (Exception ignored) {
                        }

                        NotificationUtil.scheduleNotificationApp.scheduleNotification(getApplicationContext(),
                                calendar.getTimeInMillis(), txtNameTask, idNotification, task.getId());
                    }
                    Toast.makeText(this, "Tarefa adicionada com sucesso.", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(this, "Erro ao adicionar tarefa.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private boolean isNotificationDateInFuture(Calendar calendar) {
        Calendar currentDate = Calendar.getInstance();
        return !calendar.getTime().after(currentDate.getTime());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_NOTIFICATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                saveTask();
            } else {
                Toast.makeText(this, "Permissão necessária, coloque 'Sem Notificação' no campo de notificação.", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
