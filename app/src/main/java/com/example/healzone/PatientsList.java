package com.example.healzone;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.Query;

public class PatientsList extends AppCompatActivity {

    FloatingActionButton addPatientBtn;
    RecyclerView recyclerView;
    PatientAdapter patientAdapter;
    ImageButton menuBtn, calendarBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patients_list);

        checkForPatients();

        recyclerView = findViewById(R.id.patients_recycler_view);
        setUpRecyclerView();

        addPatientBtn = findViewById(R.id.add_patient_btn);
        addPatientBtn.setOnClickListener(v-> startActivity(new Intent(PatientsList.this, PatientDetailsActivity.class)));

        menuBtn = findViewById(R.id.menu_btn);
        menuBtn.setOnClickListener(v->showMenu());

        calendarBtn = findViewById(R.id.calendar_btn);
        calendarBtn.setOnClickListener(v->{
            Intent intent = new Intent(PatientsList.this, CalendarActivity.class);
            startActivity(intent);
        });


    }

    void showMenu(){
        PopupMenu popupMenu = new PopupMenu(PatientsList.this, menuBtn);
        popupMenu.getMenu().add("Wyloguj");
        popupMenu.getMenu().add("Usuń konto");
        popupMenu.show();
        popupMenu.setOnMenuItemClickListener(menuItem -> {
            if(menuItem.getTitle()=="Wyloguj"){
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(PatientsList.this, ChooseLoginActivity.class));
                finish();
                return true;
            } else if (menuItem.getTitle()=="Usuń konto") {
                deleteFirebaseAccount();
                return true;}
            return false;
        });
    }

    private void deleteFirebaseAccount() {
        AlertDialog.Builder builder = new AlertDialog.Builder(PatientsList.this);
        builder.setMessage("Czy na pewno chcesz trwale usunąć konto i wszystkie związane z nim dane?");
        builder.setPositiveButton("Tak", (dialog, which) -> {
            deleteFromSpecialists();
            FirebaseAuth.getInstance().getCurrentUser().delete()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(PatientsList.this, "Konto usunięte", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(PatientsList.this, ChooseLoginActivity.class));
                            finish();
                        } else {
                            Toast.makeText(PatientsList.this, "Błąd podczas usuwania konta", Toast.LENGTH_SHORT).show();
                        }
                    });
        });
        builder.setNegativeButton("Nie", (dialog, which) -> {
            dialog.dismiss();
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    void deleteFromSpecialists(){
        DocumentReference documentReference;
        documentReference = Utility.getCollectionReferenceForSpecialist().document(FirebaseAuth.getInstance().getCurrentUser().getUid());
        documentReference.delete();
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
}