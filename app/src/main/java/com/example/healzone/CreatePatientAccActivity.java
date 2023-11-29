package com.example.healzone;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class CreatePatientAccActivity extends AppCompatActivity {

    Button generatePasswordBtn, createAccBtn;
    TextView emailTextView, generatedPswTextView;
    EditText confirmPswEditText;
    LinearLayout passwordLayout;
    String email, docId, imie, nazwisko, numer, nextVisit;
    boolean isAccountCreated = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_patient_acc);

        passwordLayout = findViewById(R.id.patients_password_view);

        emailTextView = findViewById(R.id.patient_acc_email_textview);

        email=getIntent().getStringExtra("email");
        emailTextView.setText(email);

        docId=getIntent().getStringExtra("docId");
        imie=getIntent().getStringExtra("imie");
        nazwisko=getIntent().getStringExtra("nazwisko");
        numer=getIntent().getStringExtra("numer");

        generatedPswTextView = findViewById(R.id.patient_acc_password_textview);

        confirmPswEditText = findViewById(R.id.patient_acc_confirmpw_edit_text);

        generatePasswordBtn = findViewById(R.id.generate_password_btn);


        generatePasswordBtn.setOnClickListener(v->{
            String randomPsw = Utility.generateRandomPassword(8);
            generatedPswTextView.setText(randomPsw);

            if (generatedPswTextView.getText().toString().matches("Hasło")){
                passwordLayout.setVisibility(View.GONE);
                createAccBtn.setVisibility(View.GONE);
            }else {
                passwordLayout.setVisibility(View.VISIBLE);
                createAccBtn.setVisibility(View.VISIBLE);
                generatePasswordBtn.setVisibility(View.GONE);
            }
        });


        createAccBtn = findViewById(R.id.create_acc_btn);

        createAccBtn.setOnClickListener(v->{
            createPatientAccount();
        });
    }

    void createPatientAccount(){

        String email = emailTextView.getText().toString();
        String password = confirmPswEditText.getText().toString();


        if (validatePassword()) {
            savePasswordInFirebase(password);

            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            DocumentReference dr = Utility.getCollectionReferenceForSpecialist().document(currentUser.getUid());

            dr.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String name = documentSnapshot.getString("specialistName");
                    String surname = documentSnapshot.getString("specialistSurname");
                    String mail = documentSnapshot.getString("specialistEmail");
                    String phoneNumber = documentSnapshot.getString("specialistNumber");

                    PatientsData patientsData = new PatientsData();
                    patientsData.setPatientEmail(email);
                    patientsData.setPatientPassword(password);
                    patientsData.setPatientName(imie);
                    patientsData.setPatientSurname(nazwisko);
                    patientsData.setPatientNumber(numer);
                    patientsData.setDocId(docId);
                    patientsData.setSpecialistName(name + " " + surname);
                    patientsData.setSpecialistEmail(mail);
                    patientsData.setSpecialistNumber(phoneNumber);

                    addPatientsDataToFirebase(patientsData);

                    Log.d("TAG", "Specialist Name: " + name);
                } else {
                    Log.d("TAG", "Document does not exist");
                }
            }).addOnFailureListener(e -> {
                Log.e("TAG", "Error getting document: " + e.getMessage());
            });




        } else {
            confirmPswEditText.setError("Hasło nieprawidłowe");
        }
    }

    void addPatientsDataToFirebase(PatientsData patientsData) {
        DocumentReference documentReference;
        documentReference = Utility.getCollectionReferenceForPatientsData().document(docId);

        documentReference.set(patientsData).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(CreatePatientAccActivity.this, "Dane dodane pomyślnie", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(CreatePatientAccActivity.this, "Błąd podczas dodawania danych", Toast.LENGTH_SHORT).show();
            }
        });
    }

    void savePasswordInFirebase(String password) {

        DocumentReference patientRef = Utility.getCollectionReferenceForPatient().document(docId);

        Map<String, Object> data = new HashMap<>();
        data.put("patientGeneratedPassword", password);

        patientRef.update(data)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(CreatePatientAccActivity.this, "Hasło zapisane w bazie danych Firebase.", Toast.LENGTH_SHORT).show();
                    isAccountCreated = true;
                    Intent intent = new Intent(CreatePatientAccActivity.this, PatientFullView.class);
                    intent.putExtra("patientGeneratedPassword",password);
                    Log.d("CreatePatientAccActivity", "Przekazywane hasło: " + password);
                    startActivity(intent);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(CreatePatientAccActivity.this, "Wystąpił błąd podczas zapisywania hasła w bazie danych Firebase.", Toast.LENGTH_SHORT).show();
                    isAccountCreated = false;
                });

    }



    boolean validatePassword(){
        String generatedPassword = generatedPswTextView.getText().toString();
        String confirmPassword = confirmPswEditText.getText().toString();
        return generatedPassword.equals(confirmPassword);
    }


}