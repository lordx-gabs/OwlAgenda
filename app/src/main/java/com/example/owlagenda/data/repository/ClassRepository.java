package com.example.owlagenda.data.repository;

import com.example.owlagenda.data.models.SchoolClass;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

public class ClassRepository {
    private CollectionReference classCollection;

    public ClassRepository() {
        classCollection = FirebaseFirestore.getInstance().collection("turma");
    }

    public void addClass(SchoolClass schoolClassData, OnCompleteListener<Void> completeListener) {
        classCollection.document(schoolClassData.getId()).set(schoolClassData)
                .addOnCompleteListener(completeListener);
    }

    public void deleteClass(String id, OnCompleteListener<Void> completeListener) {
        classCollection.document(id).delete().addOnCompleteListener(completeListener);
    }

    public void updateClass(SchoolClass schoolClassData, OnCompleteListener<Void> completeListener) {
        classCollection.document(String.valueOf(schoolClassData.getId())).set(schoolClassData, SetOptions.merge())
                .addOnCompleteListener(completeListener);
    }

    public void getClassesByUserId(String userId, EventListener<QuerySnapshot> eventListener) {
        DocumentReference userRef = FirebaseFirestore.getInstance()
                .collection("usuario")
                .document(userId);

        classCollection.whereEqualTo("userId", userRef).addSnapshotListener(eventListener);
    }

    public void getClassesBySchoolId(String schoolId, EventListener<QuerySnapshot> eventListener) {
        classCollection.whereEqualTo("schoolId", schoolId).addSnapshotListener(eventListener);
    }

}
