package com.example.owlagenda.ui.task;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.owlagenda.data.models.SchoolClass;
import com.example.owlagenda.data.models.School;
import com.example.owlagenda.data.models.Task;
import com.example.owlagenda.data.models.TaskAttachments;
import com.example.owlagenda.data.repository.ClassRepository;
import com.example.owlagenda.data.repository.SchoolRepository;
import com.example.owlagenda.data.repository.TaskRepository;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class TaskViewModel extends ViewModel {
    private final TaskRepository repository;
    private final SchoolRepository schoolRepository;
    private final ClassRepository classRepository;
    private MutableLiveData<ArrayList<Task>> tasksLiveData;
    private final MutableLiveData<Boolean> isLoading;
    private MutableLiveData<Boolean> isSuccessful;
    private final MutableLiveData<String> errorMessage;
    private MutableLiveData<ArrayList<School>> schoolsLiveData;
    private MutableLiveData<ArrayList<SchoolClass>> classesLiveData;
    private FirebaseUser firebaseUser;

    public TaskViewModel() {
        repository = new TaskRepository();
        isLoading = new MutableLiveData<>();
        errorMessage = new MutableLiveData<>();
        schoolRepository = new SchoolRepository();
        classRepository = new ClassRepository();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    public LiveData<ArrayList<Task>> getTasks() {
        isLoading.postValue(true);
        tasksLiveData = new MutableLiveData<>();
        repository.getTasks(firebaseUser.getUid(), (value, error) -> {
            if (error != null) {
                if (error.getCode() == FirebaseFirestoreException.Code.UNAVAILABLE) {
                    errorMessage.postValue("Erro de conexão. Verifique sua conexão e tente novamente.");
                } else {
                    tasksLiveData.postValue(null);
                }
                isLoading.postValue(false);
                return;
            }
            if (!value.isEmpty()) {
                ArrayList<Task> tasks = new ArrayList<>();

                tasks.addAll(value.toObjects(Task.class));

                tasksLiveData.postValue(tasks);
                isLoading.postValue(false);
            } else {
                tasksLiveData.postValue(new ArrayList<>());
                isLoading.postValue(false);
            }

        });
        return tasksLiveData;
    }

    public LiveData<Boolean> addTask(Task task) {
        isLoading.postValue(true);
        isSuccessful = new MutableLiveData<>();

        // Cria uma lista para armazenar as URLs de download
        ArrayList<String> downloadUrls = new ArrayList<>();
        int totalDocuments = task.getTaskDocuments().size();
        AtomicInteger completedDocuments = new AtomicInteger(0);

        repository.saveAttachmentsStorage(task.getTaskDocuments(), task12 -> {
            if (task12.isSuccessful()) {
                for (TaskAttachments document : task.getTaskDocuments()) {
                    FirebaseStorage.getInstance().getReference()
                            .child("usuarios")
                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .child("document")
                            .child(document.getName())
                            .getDownloadUrl()
                            .addOnCompleteListener(task13 -> {
                                if (task13.isSuccessful()) {
                                    downloadUrls.add(task13.getResult().toString());
                                    completedDocuments.incrementAndGet();

                                    // Verifica se todos os documentos foram processados
                                    if (completedDocuments.get() == totalDocuments) {
                                        // Define as URLs no objeto Task
                                        for (int i = 0; i < downloadUrls.size(); i++) {
                                            task.getTaskDocuments().get(i).setUrl(downloadUrls.get(i));
                                        }

                                        // Adiciona a tarefa
                                        repository.addTask(task, task1 -> {
                                            if (task1.isSuccessful()) {
                                                isSuccessful.postValue(true);
                                            } else {
                                                isSuccessful.postValue(false);
                                            }
                                            isLoading.postValue(false);
                                        });
                                    }
                                } else {
                                    // Tratar erro ao obter URL de download
                                    if (task13.getException() != null) {
                                        if (task13.getException() instanceof FirebaseNetworkException) {
                                            errorMessage.postValue("Erro de conexão. Verifique sua conexão e tente novamente.");
                                        } else {
                                            errorMessage.postValue("Erro ao obter URL de download. Tente novamente.");
                                        }
                                    }
                                }
                            });
                }
            } else {
                // Tratar erro ao salvar anexos
                Log.e("teste", task12.getException().getMessage());
                isLoading.postValue(false);
                isSuccessful.postValue(false);
            }
        });

        return isSuccessful;
    }


    public LiveData<ArrayList<School>> getSchools() {
        isLoading.postValue(true);
        schoolsLiveData = new MutableLiveData<>();
        schoolRepository.getSchoolsByUserId(firebaseUser.getUid(), (value, error) -> {
            if(error != null) {
                if(error.getCode() == FirebaseFirestoreException.Code.UNAVAILABLE) {
                    errorMessage.postValue("Erro de conexão. Verifique sua conexão e tente novamente.");
                } else {
                    schoolsLiveData.postValue(null);
                }
                isLoading.postValue(false);
                return;
            }

            if (value != null && !value.isEmpty()) {
                List<School> schools = value.toObjects(School.class);
                schoolsLiveData.postValue(new ArrayList<>(schools));
                isLoading.postValue(false);
            } else {
                schoolsLiveData.postValue(new ArrayList<>());
                isLoading.postValue(false);
            }
        });

        return schoolsLiveData;
    }

    public LiveData<Boolean> saveSchool(School school) {
        isSuccessful = new MutableLiveData<>();
        schoolRepository.addSchool(school, task -> {
            if(task.isSuccessful()) {
                isSuccessful.postValue(true);
            } else {
                isSuccessful.postValue(false);
            }
        });
        return isSuccessful;
    }

    public LiveData<ArrayList<SchoolClass>> getClasses() {
        classesLiveData = new MutableLiveData<>();
        classRepository.getClassesByUserId(firebaseUser.getUid(), (value, error) -> {
            if(error != null) {
                if(error.getCode() == FirebaseFirestoreException.Code.UNAVAILABLE) {
                    errorMessage.postValue("Erro de conexão. Verifique sua conexão e tente novamente.");
                } else {
                    Log.e("teste", error.getMessage());
                    classesLiveData.postValue(null);
                }
                return;
            }

            if(value != null && !value.isEmpty()) {
                List<SchoolClass> schoolClasses = value.toObjects(SchoolClass.class);
                classesLiveData.postValue(new ArrayList<>(schoolClasses));
                isLoading.postValue(false);

            } else {
                classesLiveData.postValue(new ArrayList<>());
                isLoading.postValue(false);
            }

        });

        return classesLiveData;
    }

    public LiveData<Boolean> saveClass(SchoolClass dataSchoolClass) {
        isSuccessful = new MutableLiveData<>();

        classRepository.addClass(dataSchoolClass, task -> {
            if(task.isSuccessful()) {
                isSuccessful.postValue(true);
            } else {
                isSuccessful.postValue(false);
            }
        });

        return isSuccessful;
    }

    public String getFileName(Uri uri, Context context) {
        String fileName = null;
        if (uri.getScheme() != null && uri.getScheme().equals("content")) {
            try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {

                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) {
                        fileName = cursor.getString(nameIndex);
                    } else {
                        fileName = uri.getLastPathSegment();
                    }
                }
            }
        }

        return fileName;
    }

    public LiveData<Boolean> isLoading() {
        return isLoading;
    }

}
