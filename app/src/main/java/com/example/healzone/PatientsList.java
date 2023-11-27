package com.example.healzone;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.Query;

public class PatientsList extends AppCompatActivity {

    FloatingActionButton addPatientBtn;
    RecyclerView recyclerView;
    PatientAdapter patientAdapter;
    EditText searchPatientEditText;
    ImageButton searchButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patients_list);

        checkForPatients();

        recyclerView = findViewById(R.id.patients_recycler_view);
        setUpRecyclerView();

        addPatientBtn = findViewById(R.id.add_patient_btn);
        addPatientBtn.setOnClickListener(v-> startActivity(new Intent(PatientsList.this, PatientDetailsActivity.class)));

        searchPatientEditText = findViewById(R.id.search_patient);
        searchButton = findViewById(R.id.search_btn);

        searchButton.setOnClickListener(v -> searchPatients());
    }

    void searchPatients() {
        //TODO: Logika wyszukiwania pacjentów
    }


    void setUpRecyclerView(){
        Query query = Utility.getCollectionReferenceForPatient().orderBy("timestamp",Query.Direction.DESCENDING);
        FirestoreRecyclerOptions<Patient> options = new FirestoreRecyclerOptions.Builder<Patient>()
                .setQuery(query,Patient.class).build();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        patientAdapter = new PatientAdapter(options,this);
        recyclerView.setAdapter(patientAdapter);
    }

    private void checkForPatients() {
        CollectionReference patientsCollection = Utility.getCollectionReferenceForPatient();
        patientsCollection.get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (queryDocumentSnapshots.isEmpty()) {
                findViewById(R.id.no_patients_message).setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                findViewById(R.id.no_patients_message).setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Błąd podczas sprawdzania pacjentów", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        patientAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        patientAdapter.stopListening();
    }

    @Override
    protected void onResume() {
        super.onResume();
        patientAdapter.notifyDataSetChanged();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(PatientsList.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}