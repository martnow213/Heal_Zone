package com.example.healzone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class VisitFullView extends AppCompatActivity {

    TextView dateTextView, timeTextView, patientTextView, patient1Field;
    String date, time, patient, docId, thisDocId;
    ImageButton deleteVisitBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visit_full_view);

        deleteVisitBtn = findViewById(R.id.delete_visit_btn);

        dateTextView = findViewById(R.id.date_field);
        timeTextView = findViewById(R.id.time_field);
        patientTextView =findViewById(R.id.patient_field);
        patient1Field = findViewById(R.id.patient1_field);

        date = getIntent().getStringExtra("date");
        time = getIntent().getStringExtra("time");
        patient = getIntent().getStringExtra("patient");
        docId = getIntent().getStringExtra("docId");
        thisDocId = getIntent().getStringExtra("thisDocId");

        patientTextView.setText(patient);


        deleteVisitBtn.setOnClickListener(v->{
            deleteVisit(date,time);
        });


        dateTextView.setText(date);
        timeTextView.setText(time);


    }

    void deleteVisit(String date, String time){

        AlertDialog.Builder builder = new AlertDialog.Builder(VisitFullView.this);
        builder.setMessage("Czy na pewno chcesz usunąć tę wizytę?");
        builder.setPositiveButton("Tak", (dialog, which) -> {

            DocumentReference documentReference;
            documentReference = Utility.getCollectionReferenceForVisit(docId).document(thisDocId);
            documentReference.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(VisitFullView.this, "Wizyta usunięta pomyślnie", Toast.LENGTH_SHORT).show();
                        deleteVisitFromFirebase(date, time);
                        Intent intent = new Intent(VisitFullView.this, CalendarActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(VisitFullView.this, "Błąd podczas usuwania wizyty", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            DocumentReference documentReference1;
            documentReference1 = Utility.getCollectionReferenceForAllVisits().document(thisDocId);
            documentReference1.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(VisitFullView.this, "Wizyta usunięta pomyślnie", Toast.LENGTH_SHORT).show();
                        deleteVisitFromFirebase(date, time);
                        Intent intent = new Intent(VisitFullView.this, CalendarActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(VisitFullView.this, "Błąd podczas usuwania wizyty", Toast.LENGTH_SHORT).show();
                    }
                }
            });

        });builder.setNegativeButton("Nie", (dialog, which) -> {
            dialog.dismiss();
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    void deleteVisitFromFirebase(String date, String time){
        DocumentReference documentReference;
        documentReference = Utility.getCollectionReferenceForVisit(docId).document(thisDocId);
        documentReference.delete();

        DocumentReference documentReference1;
        documentReference1 = Utility.getCollectionReferenceForAllVisits().document(thisDocId);
        documentReference1.delete();

        DocumentReference documentReference2;
        documentReference2 = Utility.getCollectionReferenceForPatientAllVisits(docId).document(thisDocId);
        documentReference2.delete();

        updateHourState(date,time);
        Log.d("debug","Date and time: " + date + " " + time);
    }

    private void updateHourState(String selectedDate, String selectedHour) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String specialistId = currentUser.getUid();

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference selectedDayHours = db.collection("availableHours").document(specialistId).collection("my_hours").document(selectedDate);

            Map<String, Object> updateMap = new HashMap<>();
            updateMap.put(selectedHour, true);

            selectedDayHours.update(updateMap)
                    .addOnSuccessListener(aVoid -> Toast.makeText(VisitFullView.this, "Godzina zaktualizowana na true", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(VisitFullView.this, "Błąd podczas aktualizacji godziny", Toast.LENGTH_SHORT).show());
        }
    }
}