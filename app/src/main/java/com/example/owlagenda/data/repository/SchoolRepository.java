package com.example.owlagenda.data.repository;

import com.example.owlagenda.data.models.School;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

public class SchoolRepository {
    private final CollectionReference schoolCollection;

    public SchoolRepository() {
        schoolCollection = FirebaseFirestore.getInstance().collection("escola");
    }

    public void addSchool(School school, OnCompleteListener<Void> eventListener) {
        schoolCollection.document(school.getId()).set(school)
                .addOnCompleteListener(eventListener);
    }

    public void deleteSchool(String id, OnCompleteListener<Void> completeListener) {
        schoolCollection.document(id).delete().addOnCompleteListener(completeListener);
    }

    public void updateSchool(School school, OnCompleteListener<Void> completeListener) {
        schoolCollection.document(String.valueOf(school.getId())).set(school, SetOptions.merge())
                .addOnCompleteListener(completeListener);
    }

    public void getSchoolsByUserId(String userId, EventListener<QuerySnapshot> eventListener) {
        DocumentReference userRef = FirebaseFirestore.getInstance()
                .collection("usuario")
                .document(userId);
        schoolCollection.whereEqualTo("userId", userRef).addSnapshotListener(eventListener);
    }

    public void getSchoolByName(String schoolName, OnCompleteListener<QuerySnapshot> eventListener) {
        schoolCollection.whereEqualTo("schoolName", schoolName).get().addOnCompleteListener(eventListener);
    }

    public void getSchoolById(String schoolId, EventListener<DocumentSnapshot> eventListener) {

    }

}
