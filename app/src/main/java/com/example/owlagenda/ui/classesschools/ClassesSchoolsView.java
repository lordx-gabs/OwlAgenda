package com.example.owlagenda.ui.classesschools;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.owlagenda.R;
import com.example.owlagenda.data.models.School;
import com.example.owlagenda.data.models.SchoolClass;
import com.example.owlagenda.databinding.ActivityClassesSchoolsViewBinding;
import com.example.owlagenda.ui.task.TaskView;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ClassesSchoolsView extends AppCompatActivity {
    private ActivityClassesSchoolsViewBinding binding;
    private ClassesSchoolsViewModel viewModel;
    private ArrayList<ClassModel> classesModel = new ArrayList<>();
    private ArrayList<SchoolModel> schoolsModel = new ArrayList<>();
    private List<Task<Void>> tasksSchool = new ArrayList<>(); // Lista de tarefas Firestore para controle
    private ClassAdapter classAdapter;
    private SchoolAdapter schoolAdapter;
    private BottomSheetDialog bottomSheetDialogClass;
    private ArrayAdapter<String> adapterSchoolName;
    private String schoolIdSelected = "";
    private SchoolModel schoolSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityClassesSchoolsViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        viewModel = new ViewModelProvider(this).get(ClassesSchoolsViewModel.class);

        viewModel.getErrorMessage().observe(this, s ->
                Toast.makeText(this, s, Toast.LENGTH_SHORT).show());

        viewModel.getIsLoading().observe(this, aBoolean -> {
            if (aBoolean) {
                binding.loadingClassesSchools.setVisibility(View.VISIBLE);
            } else {
                binding.loadingClassesSchools.setVisibility(View.GONE);
            }
        });

        binding.toolbarClassesSchools.setNavigationOnClickListener(v ->
                getOnBackPressedDispatcher().onBackPressed());

        binding.recycleClasses.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false));

        binding.recycleSchools.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,
                false));

        viewModel.getSchools(FirebaseAuth.getInstance().getCurrentUser().getUid()).observe(this, schools -> {
            if(schools != null) {
                schoolsModel.clear();
                schools.forEach(school -> schoolsModel.add(new SchoolModel(school.getId()
                        ,school.getSchoolName())));

                schoolAdapter = new SchoolAdapter(schoolsModel, new SchoolViewHolder.onSchoolClickListener() {
                    @Override
                    public void onEditSchoolClick(int position) {
                        BottomSheetDialog bottomSheetDialogSchool = new BottomSheetDialog(ClassesSchoolsView.this);
                        View view9 = LayoutInflater.from(ClassesSchoolsView.this).inflate(R.layout.bottom_sheet_add_school, (ViewGroup) ClassesSchoolsView.this.getWindow().getDecorView(), false);
                        bottomSheetDialogSchool.setContentView(view9);
                        bottomSheetDialogSchool.show();

                        TextView tvTitle = view9.findViewById(R.id.tv_title_bottom_add_school);
                        tvTitle.setText("Editar Escola");
                        TextInputEditText etNameSchool = view9.findViewById(R.id.edt_nome_escola);
                        etNameSchool.setText(schoolsModel.get(position).getSchoolName());
                        MaterialButton btnUpdateSchool = view9.findViewById(R.id.btn_add_escola);
                        btnUpdateSchool.setText("Editar Escola");

                        btnUpdateSchool.setOnClickListener(v -> {
                            School school = new School();
                            school.setSchoolName(etNameSchool.getText().toString());
                            school.setUserId(FirebaseFirestore.getInstance()
                                    .collection("usuario").document(
                                            FirebaseAuth.getInstance().getCurrentUser().getUid()
                                    ));
                            school.setSchoolNameSearch(etNameSchool.getText().toString().toUpperCase());
                            school.setId(schoolsModel.get(position).getIdSchool());
                            viewModel.updateSchool(school).observe(ClassesSchoolsView.this, aBoolean -> {
                                if (aBoolean) {
                                    bottomSheetDialogSchool.dismiss();
                                    Toast.makeText(ClassesSchoolsView.this, "Escola editada com sucesso.", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(ClassesSchoolsView.this, "Erro ao editar escola.", Toast.LENGTH_SHORT).show();
                                }
                            });
                        });

                    }

                    @Override
                    public void onDeleteSchoolClick(int position) {
                        new MaterialAlertDialogBuilder(ClassesSchoolsView.this)
                                .setTitle("Tem certeza que deseja excluir essa escola?")
                                .setMessage("Todas suas tarefas e turmas atrelhadas a essa escola serão " +
                                        "deletadas. Essa ação não pode ser desfeita")
                                .setPositiveButton("Sim", (dialogInterface, i) -> {
                                    viewModel.deleteSchool(schoolsModel.get(position).getIdSchool(),
                                            ClassesSchoolsView.this).observe(ClassesSchoolsView.this,
                                            aBoolean -> {
                                                if(aBoolean) {
                                                    Toast.makeText(ClassesSchoolsView.this,
                                                            "Escola deletada com sucesso", Toast.LENGTH_SHORT).show();
                                                } else {
                                                    Toast.makeText(ClassesSchoolsView.this,
                                                            "Erro ao deletar a escola", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                }).setNegativeButton("Não", (dialogInterface, i) -> {
                                    dialogInterface.dismiss();
                                }).show();
                    }
                });
                binding.recycleSchools.setAdapter(schoolAdapter);
                schoolAdapter.notifyDataSetChanged();
            } else {
                Toast.makeText(this, "Erro ao carregar escolas", Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getClasses(FirebaseAuth.getInstance().getCurrentUser().getUid()).observe(this,
                schoolClasses -> {
                    classesModel.clear();
                    if (schoolClasses != null) {
                        if (!schoolClasses.isEmpty()) {
                            binding.tvMessageNoClasses.setVisibility(View.GONE);
                            binding.recycleClasses.setVisibility(View.VISIBLE);
                            binding.tvMessageClassesFound.setVisibility(View.VISIBLE);

                            for (SchoolClass classes : schoolClasses) {
                                com.google.android.gms.tasks.Task<DocumentSnapshot> taskSchool = classes.getSchoolId().get();

                                // Adicionar à lista de tarefas Firestore para esperar todas

                                tasksSchool.add(taskSchool.continueWith(task4 -> {
                                    if (task4.isSuccessful()) {
                                        classesModel.add(new ClassModel(classes.getId(), classes.getClassName(), classes.getPeriod(),
                                                classes.getNumberOfStudents(), task4.getResult().getString("schoolName")));
                                        classAdapter.notifyDataSetChanged();
                                    } else {
                                        Toast.makeText(this, "Erro ao carregar escolas", Toast.LENGTH_SHORT).show();
                                    }
                                    return null;
                                }));

                            }

                            Tasks.whenAllComplete(tasksSchool).addOnCompleteListener(task7 -> {
                                binding.loadingClassesSchools.setVisibility(View.GONE);
                                if (task7.isSuccessful()) {
                                    classAdapter = new ClassAdapter(classesModel, new ClassViewHolder.onClickClassListener() {
                                        @Override
                                        public void onEditClassClick(int position) {
                                            ClassModel classModel = classesModel.get(position);
                                            bottomSheetDialogClass = new BottomSheetDialog(ClassesSchoolsView.this);
                                            View view5 = LayoutInflater.from(ClassesSchoolsView.this)
                                                    .inflate(R.layout.bottom_sheet_add_class, (ViewGroup)
                                                            ClassesSchoolsView.this.getWindow().getDecorView(), false);
                                            bottomSheetDialogClass.setContentView(view5);
                                            bottomSheetDialogClass.show();


                                            TextInputEditText etNameClass = view5.findViewById(R.id.edt_name_class);
                                            TextInputEditText etNumberOfStudents = view5.findViewById(R.id.edt_number_students_class);
                                            MaterialButton btnEditClass = view5.findViewById(R.id.btn_add_class);
                                            TextView tvTitleBottomClass = view5.findViewById(R.id.tv_title_bottom_class);
                                            tvTitleBottomClass.setText("Editar Classe");

                                            AutoCompleteTextView autoCompletePeriod = view5.findViewById(R.id.auto_complete_period_class);
                                            String[] periods = new String[]{"Manhã", "Tarde", "Noite"};
                                            ArrayAdapter<String> adapter = new ArrayAdapter<>(ClassesSchoolsView.this,
                                                    android.R.layout.simple_dropdown_item_1line, periods);
                                            autoCompletePeriod.setAdapter(adapter);
                                            etNameClass.setText(classModel.getNameClass());
                                            etNumberOfStudents.setText(String.valueOf(classModel.getNumberStudents()));
                                            autoCompletePeriod.setText(classModel.getPeriod(), false);
                                            autoCompletePeriod.setOnClickListener(v -> autoCompletePeriod.showDropDown());

                                            ArrayList<String> schoolsName = new ArrayList<>();
                                            schoolsModel.forEach(school -> schoolsName.add(school.getSchoolName()));

                                            AutoCompleteTextView autoCompleteSchool = view5.findViewById(R.id.auto_complete_school_class);
                                            autoCompleteSchool.setOnClickListener(v -> autoCompleteSchool.showDropDown());
                                            adapterSchoolName = new ArrayAdapter<>(ClassesSchoolsView.this,
                                                    android.R.layout.simple_dropdown_item_1line, schoolsName);
                                            autoCompleteSchool.setAdapter(adapterSchoolName);
                                            adapterSchoolName.notifyDataSetChanged();

                                            autoCompleteSchool.setOnItemClickListener((adapterView, view, i, l) -> {
                                                schoolIdSelected = schoolsModel.get(i).getIdSchool();
                                            });
                                            autoCompleteSchool.setText(classModel.getSchoolName(), false);
                                            Optional<SchoolModel> schoolModelOptional = schoolsModel.stream().filter(school -> school.getSchoolName()
                                                            .equalsIgnoreCase(classModel.getSchoolName())).findFirst();
                                            if(schoolModelOptional.isPresent()) {
                                                schoolIdSelected = schoolModelOptional.get().getIdSchool();
                                            } else {
                                                Toast.makeText(ClassesSchoolsView.this, "Erro, contate o suporte", Toast.LENGTH_SHORT).show();
                                                bottomSheetDialogClass.dismiss();
                                            }

                                            btnEditClass.setText("Editar");
                                            btnEditClass.setOnClickListener(v -> {
                                                if(!etNameClass.getText().toString().isEmpty() && !etNumberOfStudents.getText().toString().isEmpty()
                                                && !autoCompletePeriod.getText().toString().isEmpty() && !schoolIdSelected.isEmpty()) {
                                                    // deixar schollId empty
                                                    SchoolClass schoolClass = new SchoolClass(classModel.getIdClass(),
                                                            FirebaseFirestore.getInstance().collection("usuario").document(
                                                                    FirebaseAuth.getInstance().getCurrentUser().getUid()),
                                                            FirebaseFirestore.getInstance().collection("escola").document(
                                                                    schoolIdSelected),
                                                            etNameClass.getText().toString(), etNameClass.getText().toString().toUpperCase(),
                                                            autoCompletePeriod.getText().toString(), Integer.parseInt(etNumberOfStudents.getText().toString()));

                                                    viewModel.updateClass(schoolClass).observe(ClassesSchoolsView.this, aBoolean -> {
                                                        schoolIdSelected = "";
                                                        if(aBoolean) {
                                                            Toast.makeText(ClassesSchoolsView.this, "Classe editada com sucesso", Toast.LENGTH_SHORT).show();
                                                            bottomSheetDialogClass.dismiss();
                                                        } else {
                                                            Toast.makeText(ClassesSchoolsView.this, "Erro ao editar classe", Toast.LENGTH_SHORT).show();
                                                            bottomSheetDialogClass.dismiss();
                                                        }
                                                    });

                                                } else {
                                                    Toast.makeText(ClassesSchoolsView.this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }

                                        @Override
                                        public void onDeleteClassClick(int position) {
                                            new MaterialAlertDialogBuilder(ClassesSchoolsView.this)
                                                    .setTitle("Tem certeza que deseja excluir essa turma?")
                                                    .setMessage("Todas suas tarefas atrelhadas a essa turma serão " +
                                                            "deletadas. Essa ação não pode ser desfeita")
                                                    .setPositiveButton("Sim", (dialogInterface, i) -> {
                                                        viewModel.deleteClass(classesModel.get(position).getIdClass(),
                                                                ClassesSchoolsView.this).observe(ClassesSchoolsView.this,
                                                                aBoolean -> {
                                                                    if(aBoolean) {
                                                                        Toast.makeText(ClassesSchoolsView.this,
                                                                                "Classe deletada com sucesso", Toast.LENGTH_SHORT).show();
                                                                    } else {
                                                                        Toast.makeText(ClassesSchoolsView.this,
                                                                                "Erro ao deletar a classe", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                });
                                                    }).setNegativeButton("Não", (dialogInterface, i) -> {
                                                        dialogInterface.dismiss();
                                                    }).show();
                                        }
                                    });
                                    binding.recycleClasses.setAdapter(classAdapter);
                                } else {
                                    Toast.makeText(this, "Erro ao carregar classes", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            binding.tvMessageNoClasses.setVisibility(View.VISIBLE);
                            binding.recycleClasses.setVisibility(View.GONE);
                            binding.tvMessageClassesFound.setVisibility(View.GONE);
                        }
                    }
                });
        binding.btnClassAdd.setOnClickListener(v -> {
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
            autoCompletePeriod.setOnClickListener(v2 -> autoCompletePeriod.showDropDown());

            ArrayList<String> schoolsName = new ArrayList<>();
            schoolsModel.forEach(school -> schoolsName.add(school.getSchoolName()));

            AutoCompleteTextView autoCompleteSchool = view5.findViewById(R.id.auto_complete_school_class);
            autoCompleteSchool.setOnClickListener(v4 -> autoCompleteSchool.showDropDown());
            adapterSchoolName = new ArrayAdapter<>(ClassesSchoolsView.this,
                    android.R.layout.simple_dropdown_item_1line, schoolsName);
            autoCompleteSchool.setAdapter(adapterSchoolName);
            adapterSchoolName.notifyDataSetChanged();

            autoCompleteSchool.setAdapter(adapterSchoolName);
            autoCompleteSchool.setOnClickListener(v4 -> autoCompleteSchool.showDropDown());

            autoCompleteSchool.setOnItemClickListener((adapterView, view, i, l) -> {
                schoolSelected = schoolsModel.get(i);
            });
            btnAddClass.setOnClickListener(v6 -> {
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
                            schoolSelected.getIdSchool()));
                    dataSchoolClass.setId(FirebaseFirestore.getInstance().collection("turma").document().getId());
                    viewModel.saveClass(dataSchoolClass).observe(this, aBoolean -> {
                        schoolSelected = null;
                        if (aBoolean) {
                            bottomSheetDialogClass.dismiss();
                            Toast.makeText(ClassesSchoolsView.this, "Classe adicionada com sucesso.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(ClassesSchoolsView.this, "Erro ao adicionar classe.", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(this, "Preencha todos os campos.", Toast.LENGTH_SHORT).show();
                }
            });
        });

        binding.btnAddSchools.setOnClickListener(v10 -> {
            BottomSheetDialog bottomSheetDialogSchool = new BottomSheetDialog(this);
            View view9 = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_add_school, (ViewGroup) this.getWindow().getDecorView(), false);
            bottomSheetDialogSchool.setContentView(view9);
            bottomSheetDialogSchool.show();

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
                        Toast.makeText(this, "Escola adicionada com sucesso.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Erro ao adicionar escola.", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        });

    }
}