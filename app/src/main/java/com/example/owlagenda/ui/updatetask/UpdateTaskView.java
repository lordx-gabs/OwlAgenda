package com.example.owlagenda.ui.updatetask;

import android.Manifest;
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
import com.example.owlagenda.data.models.School;
import com.example.owlagenda.data.models.SchoolClass;
import com.example.owlagenda.data.models.Task;
import com.example.owlagenda.data.models.TaskAttachments;
import com.example.owlagenda.databinding.ActivityUpdateTaskViewBinding;
import com.example.owlagenda.ui.task.DocumentAdapter;
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

public class UpdateTaskView extends AppCompatActivity {
    private String taskId;
    private static final int REQUEST_CODE_NOTIFICATION = 501;
    private ActivityResultLauncher<Intent> pickDocumentLauncher;
    private UpdateTaskViewModel viewModel;
    private ActivityUpdateTaskViewBinding binding;
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
        binding = ActivityUpdateTaskViewBinding.inflate(getLayoutInflater());
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        viewModel = new ViewModelProvider(this).get(UpdateTaskViewModel.class);

        viewModel.isLoading().observe(this, aBoolean -> {
            if (aBoolean) {
                binding.btnUpdateTask.setEnabled(false);
                binding.loadingTaskUpdate.setVisibility(View.VISIBLE);
            } else {
                binding.btnUpdateTask.setEnabled(true);
                binding.loadingTaskUpdate.setVisibility(View.GONE);
            }
        });

        viewModel.getErrorMessage().observe(this, s ->
                Toast.makeText(this, s, Toast.LENGTH_SHORT).show());

        setContentView(binding.getRoot());

        taskId = getIntent().getStringExtra("taskId");

        viewModel.getSchools().observe(this, schools -> {
            if (schools == null) {
                finish();
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
                        finish();
                        Toast.makeText(this, "Não foi possivel carregar as classes.", Toast.LENGTH_SHORT).show();
                    } else {
                        this.schoolClasses = classes;
                        classesName = new ArrayList<>();
                        classesName.add("Adicionar nova classe");
                        // Mapeia cada classe para o nome da escola correspondente e coleta os nomes em uma lista
                        List<String> schoolNames = classes.stream()
                                .map(classItem -> {
                                    // Obtém o DocumentReference da escola da classe
                                    String classSchoolRef = classItem.getSchoolId().getId();

                                    // Encontra a escola correspondente
                                    return schools.stream()
                                            .filter(school -> classSchoolRef.equals(school.getId())) // Compara DocumentReferences
                                            .map(School::getSchoolName) // Mapeia para o nome da escola
                                            .findFirst() // Pega a primeira ocorrência
                                            .orElse("Escola Desconhecida"); // Valor padrão caso não encontre
                                })
                                .collect(Collectors.toList()); // Coleta os nomes das escolas em uma lista

                        for (int i = 0; i < schoolNames.size(); i++) {
                            classesName.add(schoolNames.get(i) + " - " + classes.get(i).getClassName());
                        }
                        adapterClass = new ArrayAdapter<>(this, R.layout.dropdown_layout, classesName);
                        binding.autoCompleteClassTaskUpdate.setAdapter(adapterClass);
                        adapterClass.notifyDataSetChanged();
                    }
                });
            }
        });

        binding.autoCompleteClassTaskUpdate.setOnItemClickListener((parent, view, position, id) -> {
            List<String> filteredItems = new ArrayList<>();
            for (int i = 0; i < binding.autoCompleteClassTaskUpdate.getAdapter().getCount(); i++) {
                if (binding.autoCompleteClassTaskUpdate.getAdapter().getItem(i).toString().contains(" - ")) {
                    filteredItems.add(binding.autoCompleteClassTaskUpdate.getAdapter().getItem(i).toString()
                            .split(" - ")[1]);
                } else {
                    filteredItems.add(binding.autoCompleteClassTaskUpdate.getAdapter().getItem(i).toString());
                }
            }
            schoolSelected = null;
            if (filteredItems.get(position).equalsIgnoreCase("Adicionar nova classe")) {
                binding.autoCompleteClassTaskUpdate.setText("");
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

                bottomSheetDialogClass.setOnDismissListener(dialog -> {
                    nameClass = etNameClass.getText().toString();
                    numberOfStudents = etNumberOfStudents.getText().toString();
                    period = autoCompletePeriod.getText().toString();
                });

                AutoCompleteTextView autoCompleteSchool = view5.findViewById(R.id.auto_complete_school_class);
                autoCompleteSchool.setAdapter(adapterSchool);
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
                            school.setSchoolName(etNameSchool.getText().toString());
                            school.setUserId(FirebaseFirestore.getInstance()
                                    .collection("usuario").document(
                                            FirebaseAuth.getInstance().getCurrentUser().getUid()
                                    ));
                            school.setSchoolNameSearch(etNameSchool.getText().toString().toUpperCase());
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
                    if (!etNameClass.getText().toString().isEmpty() && !etNumberOfStudents.getText().toString().isEmpty()
                            && !autoCompletePeriod.getText().toString().isEmpty() && schoolSelected != null) {
                        SchoolClass dataSchoolClass = new SchoolClass();
                        dataSchoolClass.setClassNameSearch(etNameClass.getText().toString().toUpperCase());
                        dataSchoolClass.setNumberOfStudents(Integer.parseInt(etNumberOfStudents.getText().toString()));
                        dataSchoolClass.setClassName(etNameClass.getText().toString());
                        dataSchoolClass.setPeriod(autoCompletePeriod.getText().toString());
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
                                binding.autoCompleteClassTaskUpdate.setText(schoolClassSelected.getClassName(), false);
                                Toast.makeText(UpdateTaskView.this, "Classe adicionada com sucesso.", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(UpdateTaskView.this, "Erro ao adicionar classe.", Toast.LENGTH_SHORT).show();
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
        binding.autoCompleteTagTaskUpdate.setAdapter(adapter);

        pickDocumentLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        if (result.getData().getData() != null) {
                            if (documentAdapter.getDocuments().size() < 10) {
                                if (viewModel.getFileMbSize(this, result.getData().getData()) < 5) {
                                    String fileName = viewModel.getFileName(result.getData().getData(), this);
                                    if (documentAdapter.getDocuments().stream().noneMatch(taskAttachments ->
                                            taskAttachments.getName().equals(fileName))) {
                                        documentAdapter.getDocuments().add(new TaskAttachments(
                                                fileName, result.getData().getData().toString()));
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

        binding.recycleDocumentTaskUpdate.setLayoutManager(new LinearLayoutManager(this
                , LinearLayoutManager.HORIZONTAL, false));
        binding.recycleDocumentTaskUpdate.setAdapter(documentAdapter);
        binding.autoCompleteNotificationsTaskUpdate.setOnItemClickListener((parent, view, position, id) ->
                indexNotifications = position);

        binding.btnUpdateTask.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && indexNotifications != 0) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_CODE_NOTIFICATION);
                    return;
                } else {
                    updateTask();
                }
            }
            updateTask();
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

        binding.etDateTaskUpdate.setOnClickListener(v -> {
            Calendar calendarInitialed = Calendar.getInstance();
            if (binding.etDateTaskUpdate.getText().toString().isEmpty()) {
                calendarInitialed.setTimeInMillis(Calendar.getInstance(TimeZone
                        .getTimeZone("America/Sao_Paulo")).getTimeInMillis());
            } else {
                try {
                    calendarInitialed.setTime(new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            .parse(binding.etDateTaskUpdate.getText().toString()));
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

                binding.etDateTaskUpdate.setText(dateFormat.format(dateSelected.getTime()));
            });

            if (!materialDatePicker.isAdded()) {
                materialDatePicker.show(getSupportFragmentManager(), "material_date_picker");
            }
        });

        adapterNotification = new ArrayAdapter<>(this, R.layout.dropdown_layout, notificationsMinutes);
        binding.autoCompleteNotificationsTaskUpdate.setAdapter(adapterNotification);
        binding.autoCompleteNotificationsTaskUpdate.setText(notificationsMinutes[1], false);
        binding.autoCompleteNotificationsTaskUpdate.setOnClickListener(v ->
                binding.autoCompleteNotificationsTaskUpdate.showDropDown());

        binding.toolbarTaskUpdate.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        viewModel.getTaskById(taskId).observe(this, task -> {
            if (task != null) {
                task.getSchoolClass().get().addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        schoolClassSelected = task1.getResult().toObject(SchoolClass.class);
                        binding.autoCompleteClassTaskUpdate.setText(schoolClassSelected.getClassName(), false);
                        binding.etNameTaskUpdate.setText(task.getTitle());
                        binding.etDescriptionTaskUpdate.setText(task.getDescription());
                        binding.etDateTaskUpdate.setText(task.getDate());
                        binding.autoCompleteTagTaskUpdate.setText(task.getTag(), false);
                        binding.switchCompleteUpdateTask.setChecked(task.isCompleted());
                        documentAdapter.getDocuments().clear();
                        if(task.getTaskDocuments() != null) {
                            documentAdapter.getDocuments().addAll(task.getTaskDocuments());
                            documentAdapter.notifyDataSetChanged();
                        }
                    } else {
                        finish();
                        Toast.makeText(this, "Erro ao carregar a tarefa.", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                finish();
                Toast.makeText(this, "Não foi possivel carregar a tarefa.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateTask() {
        String txtNameTask = binding.etNameTaskUpdate.getText().toString().trim();
        String txtDescriptionTask = binding.etDescriptionTaskUpdate.getText().toString().trim();
        String txtDateTask = binding.etDateTaskUpdate.getText().toString();
        String txtTagTask = binding.autoCompleteTagTaskUpdate.getText().toString();
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
            task.setCompleted(binding.switchCompleteUpdateTask.isChecked());
            task.setId(taskId);
            Calendar calendar;
            if (notificationBefore != null) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                dateFormat.setTimeZone(TimeZone.getTimeZone("America/Sao_Paulo")); // Definir o fuso horário
                calendar = Calendar.getInstance();
                try {
                    // Faz o parse da data
                    calendar.setTime(dateFormat.parse(txtDateTask));
                    calendar.setTimeZone(TimeZone.getTimeZone("America/Sao_Paulo"));

                    Calendar currentCalendar = Calendar.getInstance(TimeZone.getTimeZone("America/Sao_Paulo"));
                    currentCalendar.setTimeZone(TimeZone.getTimeZone("America/Sao_Paulo"));
                    calendar.set(Calendar.HOUR_OF_DAY, currentCalendar.get(Calendar.HOUR_OF_DAY));
                    calendar.set(Calendar.MINUTE, currentCalendar.get(Calendar.MINUTE));
                    calendar.set(Calendar.SECOND, currentCalendar.get(Calendar.SECOND));

                    calendar.add(Calendar.MINUTE, -notificationBefore); // Subtrai o tempo de notificação
                } catch (ParseException e) {
                    Toast.makeText(this, "Erro ao converter data", Toast.LENGTH_SHORT).show();
                }
                if (isNotificationDateInFuture(calendar)) {
                    notificationBefore = null;
                }
            } else {
                calendar = null;
            }

            Integer finalNotificationBefore = notificationBefore;
            viewModel.updateTask(task).observe(this, aBoolean -> {
                int idNotification = 0;
                try {
                    idNotification = Integer.parseInt(taskId.replaceAll("[^0-9]", ""));
                } catch (Exception ignored) {
                }
                if (aBoolean) {
                    if (NotificationUtil.scheduleNotificationApp.isAlarmSet(getApplicationContext(),
                            task.getTitle(), idNotification)) {
                        NotificationUtil.scheduleNotificationApp.cancelNotification(getApplicationContext(),
                                        task.getTitle(), idNotification);
                        Log.d("testeee", "cancelou");
                    }
                    if (finalNotificationBefore != null && !binding.switchCompleteUpdateTask.isChecked()) {
                        NotificationUtil.scheduleNotificationApp.scheduleNotification(getApplicationContext(),
                                calendar.getTimeInMillis(),
                                txtNameTask,
                                idNotification);
                        Log.d("testeee", "salvou");
                    }
                    Toast.makeText(this, "Tarefa alterada com sucesso.", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(this, "Erro ao alterar tarefa.", Toast.LENGTH_SHORT).show();
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
                updateTask();
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