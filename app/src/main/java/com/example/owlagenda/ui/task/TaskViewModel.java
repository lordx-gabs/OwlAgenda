package com.example.owlagenda.ui.task;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Log;

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
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.ArrayList;
import java.util.List;

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
    private final FirebaseUser firebaseUser;

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

                ArrayList<Task> tasks = new ArrayList<>(value.toObjects(Task.class));

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

        ArrayList<String> downloadUrls = new ArrayList<>();

        task.setId(FirebaseFirestore.getInstance().collection("tarefa").document().getId());

        com.google.android.gms.tasks.Task<Void> lastTask = Tasks.forResult(null);

        lastTask = lastTask.continueWithTask(task1 -> repository.saveAttachmentsStorage(task.getTaskDocuments()))
                .addOnFailureListener(e -> {
                    if (e instanceof FirebaseNetworkException) {
                        errorMessage.postValue("Erro de conexão. Verifique sua conexão e tente novamente.");
                    } else {
                        isSuccessful.postValue(false);
                    }
                    isLoading.postValue(false);
                });

        lastTask.continueWithTask(task1 -> repository.getAttachmentsUrls(task, downloadUrls))
                .addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        for (int i = 0; i < downloadUrls.size(); i++) {
                            task.getTaskDocuments().get(i).setUrl(downloadUrls.get(i));
                        }

                        repository.addTask(task, task2 -> {
                            if (task2.isSuccessful()) {
                                isSuccessful.postValue(true);
                            } else {
                                handleErrorCreateTask(task.getTaskDocuments(), task2.getException());
                            }
                        });
                    } else {
                        handleErrorCreateTask(task.getTaskDocuments(), task1.getException());
                    }
                });

        return isSuccessful;
    }

    private void handleErrorCreateTask(ArrayList<TaskAttachments> documents, Exception exception) {
        if (exception instanceof FirebaseNetworkException) {
            errorMessage.postValue("Erro de conexão. Verifique sua conexão e tente novamente.");
        }
        repository.deleteAttachmentsStorage(documents, task -> {
            if (!task.isSuccessful()) {
                errorMessage.postValue("Erro ao interno, tente novamente. " + exception.getMessage());
            }
            isLoading.postValue(false);
        });
    }


    public LiveData<ArrayList<School>> getSchools() {
        isLoading.postValue(true);
        schoolsLiveData = new MutableLiveData<>();
        schoolRepository.getSchoolsByUserId(firebaseUser.getUid(), (value, error) -> {
            if (error != null) {
                if (error.getCode() == FirebaseFirestoreException.Code.UNAVAILABLE) {
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
        isLoading.postValue(true);
        isSuccessful = new MutableLiveData<>();
        schoolRepository.addSchool(school, task -> {
            if (task.isSuccessful()) {
                isSuccessful.postValue(true);
                isLoading.postValue(false);
            } else {
                isSuccessful.postValue(false);
                isLoading.postValue(false);
            }
        });
        return isSuccessful;
    }

    public LiveData<ArrayList<SchoolClass>> getClasses() {
        classesLiveData = new MutableLiveData<>();
        classRepository.getClassesByUserId(firebaseUser.getUid(), (value, error) -> {
            if (error != null) {
                if (error.getCode() == FirebaseFirestoreException.Code.UNAVAILABLE) {
                    errorMessage.postValue("Erro de conexão. Verifique sua conexão e tente novamente.");
                } else {
                    classesLiveData.postValue(null);
                }
                return;
            }

            if (value != null && !value.isEmpty()) {
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
        isLoading.postValue(true);
        classRepository.addClass(dataSchoolClass, task -> {
            if (task.isSuccessful()) {
                isSuccessful.postValue(true);
                isLoading.postValue(false);
            } else {
                isSuccessful.postValue(false);
                isLoading.postValue(false);
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

    public double getFileMbSize(Context context, Uri uri) {
        long fileSize = 0;

        if (uri != null) {
            Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                if (sizeIndex != -1) {
                    fileSize = cursor.getLong(sizeIndex);
                }
                cursor.close();
            }
        }

        return (fileSize / (1024.0 * 1024.0));
    }

    public LiveData<Boolean> isLoading() {
        return isLoading;
    }

}
