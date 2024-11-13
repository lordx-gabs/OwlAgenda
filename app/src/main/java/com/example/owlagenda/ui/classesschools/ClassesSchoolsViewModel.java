package com.example.owlagenda.ui.classesschools;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.owlagenda.data.models.School;
import com.example.owlagenda.data.models.SchoolClass;
import com.example.owlagenda.data.repository.ClassRepository;
import com.example.owlagenda.data.repository.SchoolRepository;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class ClassesSchoolsViewModel extends ViewModel {
    private final ClassRepository classRepository;
    private final SchoolRepository schoolRepository;
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private MutableLiveData<Boolean> isSuccessfully;
    private MutableLiveData<ArrayList<School>> schoolsLiveData;
    private MutableLiveData<ArrayList<SchoolClass>> classesLiveData;

    public ClassesSchoolsViewModel() {
        classRepository = new ClassRepository();
        schoolRepository = new SchoolRepository();
    }

    public LiveData<ArrayList<School>> getSchools(String idUser) {
        schoolsLiveData = new MutableLiveData<>();

        schoolRepository.getSchoolsByUserId(idUser, (value, error) -> {
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
                Log.d("teste33", "voltou vazio");
                schoolsLiveData.postValue(new ArrayList<>());
                isLoading.postValue(false);
            }
        });

        return schoolsLiveData;
    }

    public LiveData<Boolean> saveSchool(School school) {
        isLoading.postValue(true);
        isSuccessfully = new MutableLiveData<>();
        schoolRepository.addSchool(school, task -> {
            if (task.isSuccessful()) {
                isSuccessfully.postValue(true);
                isLoading.postValue(false);
            } else {
                isSuccessfully.postValue(false);
                isLoading.postValue(false);
            }
        });
        return isSuccessfully;
    }

    public LiveData<Boolean> updateSchool(School school) {
        isLoading.postValue(true);
        isSuccessfully = new MutableLiveData<>();

        schoolRepository.updateSchool(school, task -> {
            isLoading.postValue(false);
            if(task.isSuccessful()) {
                isSuccessfully.postValue(true);
            } else {
                isSuccessfully.postValue(false);
                Log.d("teste", task.getException().getMessage());
            }
        });

        return isSuccessfully;
    }

    public LiveData<ArrayList<SchoolClass>> getClasses(String userId) {
        classesLiveData = new MutableLiveData<>();
        classRepository.getClassesByUserId(userId, (value, error) -> {
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

    public LiveData<Boolean> updateClass(SchoolClass classData) {
        isSuccessfully = new MutableLiveData<>();
        isLoading.setValue(true);
        classRepository.updateClass(classData, task -> {
            isLoading.setValue(false);
            if(task.isSuccessful()) {
                isSuccessfully.postValue(true);
            } else {
                isSuccessfully.postValue(false);
                Log.d("teste", task.getException().getMessage());
            }
        });

        return isSuccessfully;
    }

    public LiveData<Boolean> deleteClass(String id, Context context) {
        isSuccessfully = new MutableLiveData<>();
        isLoading.setValue(true);
        classRepository.deleteClass(id, task -> {
            isLoading.setValue(false);
            if(task.isSuccessful()) {
                isSuccessfully.postValue(true);
            } else {
                Log.d("teste", task.getException().getMessage());
                isSuccessfully.postValue(false);
            }
        }, context);

        return isSuccessfully;
    }

    public LiveData<Boolean> deleteSchool(String id, Context context) {
        isSuccessfully = new MutableLiveData<>();
        isLoading.setValue(true);
        schoolRepository.deleteSchool(id, task -> {
            isLoading.setValue(false);
            if(task.isSuccessful()) {
                isSuccessfully.postValue(true);
            } else {
                Log.d("teste", task.getException().getMessage());
                isSuccessfully.postValue(false);
            }
        }, context);

        return isSuccessfully;
    }

    public LiveData<Boolean> saveClass(SchoolClass dataSchoolClass) {
        isSuccessfully = new MutableLiveData<>();
        isLoading.postValue(true);
        classRepository.addClass(dataSchoolClass, task -> {
            if (task.isSuccessful()) {
                isSuccessfully.postValue(true);
                isLoading.postValue(false);
            } else {
                isSuccessfully.postValue(false);
                isLoading.postValue(false);
            }
        });

        return isSuccessfully;
    }


    public MutableLiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public MutableLiveData<String> getErrorMessage() {
        return errorMessage;
    }
}
