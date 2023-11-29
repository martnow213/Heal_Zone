package com.example.healzone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;


public class PatientLoginActivity extends AppCompatActivity {

    EditText emailEditText, generatedPasswordEditText;
    Button patientLoginBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_login);

        emailEditText = findViewById(R.id.email_edit_text);
        generatedPasswordEditText = findViewById(R.id.password_edit_text);

        patientLoginBtn = findViewById(R.id.login_btn);

        patientLoginBtn.setOnClickListener(v->loginUser());

        TextView learnMoreBtn = findViewById(R.id.learn_more_text_view_btn);

        learnMoreBtn.setOnClickListener(view -> {
            Intent intent = new Intent(PatientLoginActivity.this, InfoActivity.class);
            startActivity(intent);
        });

    }

    void loginUser(){

        String email = emailEditText.getText().toString();
        String password = generatedPasswordEditText.getText().toString();

        boolean isValidated = validateData(email, password);
        if(isValidated){
            Intent intent = new Intent(PatientLoginActivity.this, PatientsMainActivity.class);
            startActivity(intent);
        }
    }

    boolean validateData(String email, String password){

        //Poprawny format maila
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            emailEditText.setError("Email nieprawidłowy");
            return false;
        }
        //Dlugosc hasla
        if (password.length()<8){
            generatedPasswordEditText.setError("Hasło musi mieć min. 8 znaków");
            return false;
        }
        //Min. jedna duza litera w hasle
        if (!hasUpperCaseLetter(password)) {
            generatedPasswordEditText.setError("Hasło musi zawierać min. jedną dużą literę");
            return false;
        }
        //Min. jedna cyfra w hasle
        boolean containsDigit = false;

        for (int i = 0; i < password.length(); i++) {
            char c = password.charAt(i);
            if (Character.isDigit(c)) {
                containsDigit = true;
                break;
            }
        }

        if (!containsDigit) {
            generatedPasswordEditText.setError("Hasło musi zawierać min. jedną cyfrę");
            return false;
        }

        if(!isCreated(email, password)){
            return false;
        }

        return true; //Wszystkie warunki spełnione
    }

    boolean isCreated(String email, String password) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Query query = db.collection("patientsData")
                .whereEqualTo("patientEmail", email)
                .whereEqualTo("patientPassword", password);

        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (DocumentSnapshot document : task.getResult().getDocuments()) {

                    String docId = document.getString("docId");
                    String patientName = document.getString("patientName");
                    String patientSurname = document.getString("patientSurname");
                    String patientNumber = document.getString("patientNumber");
                    String patientEmail = document.getString("patientEmail");
                    String patientPassword = document.getString("patientPassword");
                    String specialistName = document.getString("specialistName");
                    String specialistNumber = document.getString("specialistNumber");
                    String specialistEmail = document.getString("specialistEmail");
                    String nextVisit = document.getString("nextVisit");

                    Intent intent = new Intent(PatientLoginActivity.this, PatientsMainActivity.class);
                    intent.putExtra("docId", docId);
                    intent.putExtra("patientName", patientName);
                    intent.putExtra("patientSurname", patientSurname);
                    intent.putExtra("patientEmail", patientEmail);
                    intent.putExtra("patientNumber", patientNumber);
                    intent.putExtra("patientPassword", patientPassword);
                    intent.putExtra("specialistName",specialistName);
                    intent.putExtra("specialistNumber",specialistNumber);
                    intent.putExtra("specialistEmail",specialistEmail);
                    //intent.putExtra("nextVisit", nextVisit);

                    startActivity(intent);
                    return;
                }

                // Nie znaleziono użytkownika o podanym emailu i haśle
                emailEditText.setError("Błędny email lub hasło");
            } else {
                Log.e("Firestore", "Błąd podczas odczytu z Firestore: " + task.getException().getMessage());
            }
        });

        return false;
    }

    public static boolean hasUpperCaseLetter(String password) {
        for (int i = 0; i < password.length(); i++) {
            char c = password.charAt(i);
            if (Character.isUpperCase(c)) {
                return true; // Znaleziono dużą literę
            }
        }
        return false; // Nie znaleziono dużej litery
    }

}
