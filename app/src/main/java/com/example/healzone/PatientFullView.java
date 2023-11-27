package com.example.healzone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;


import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;


public class PatientFullView extends AppCompatActivity {

    String imie, nazwisko, pesel, data_ur, plec, diagnoza, notatki, docId, email, numer, nextVisitValue;
    TextView nameField, surnameField, peselField, birthDateField, genderField, diagnosisField, notesField, emailField, numberField, notes1Field, diagnosis1Field, generatedPswField;
    Button progressBtn, calendarBtn, patientAccBtn;
    ImageButton editBtn, deleteBtn, pdfBtn;
    LinearLayout generatedPswLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_full_view);

        generatedPswLayout = findViewById(R.id.generatedPswLayout);
        generatedPswField = findViewById(R.id.generated_psw_field);

        deleteBtn = findViewById(R.id.delete_patient_btn);

        deleteBtn.setOnClickListener(v->{
            deletePatient();
        });


        editBtn = findViewById(R.id.edit_patient_btn);

        editBtn.setOnClickListener(v -> {
            Intent intent = new Intent(PatientFullView.this, PatientDetailsActivity.class);
            intent.putExtra("imie", imie);
            intent.putExtra("nazwisko", nazwisko);
            intent.putExtra("pesel", pesel);
            intent.putExtra("data_ur", data_ur);
            intent.putExtra("plec", plec);
            intent.putExtra("diagnoza", diagnoza);
            intent.putExtra("notatki", notatki);
            intent.putExtra("docId", docId);
            intent.putExtra("email", email);
            intent.putExtra("numer", numer);

            startActivity(intent);
        });


        patientAccBtn = findViewById(R.id.create_patient_acc_btn);

        patientAccBtn.setOnClickListener(v->{
            Intent intent = new Intent(PatientFullView.this, CreatePatientAccActivity.class);
            intent.putExtra("email",email);
            intent.putExtra("docId", docId);
            intent.putExtra("imie", imie);
            intent.putExtra("nazwisko", nazwisko);
            intent.putExtra("numer", numer);

            startActivity(intent);
        });

        progressBtn = findViewById(R.id.progress_btn);

        progressBtn.setOnClickListener(view -> {
            Intent intent = new Intent(PatientFullView.this, PatientsNotesActivity.class);
            intent.putExtra("docId",docId);
            startActivity(intent);
        });

        calendarBtn = findViewById(R.id.calendar_btn);

        calendarBtn.setOnClickListener(view->{
            Intent intent = new Intent(PatientFullView.this, PatientVisitsActivity.class);
            intent.putExtra("docId",docId);
            startActivity(intent);
        });

        nameField = findViewById(R.id.name_field);
        surnameField = findViewById(R.id.surname_field);
        peselField = findViewById(R.id.pesel_field);
        genderField = findViewById(R.id.gender_field);
        birthDateField = findViewById(R.id.birthdate_field);
        diagnosisField = findViewById(R.id.diagnosis_field);
        notesField = findViewById(R.id.notes_field);
        emailField = findViewById(R.id.email_field);
        numberField = findViewById(R.id.phone_field);
        notes1Field = findViewById(R.id.notes1_field);
        diagnosis1Field = findViewById(R.id.diagnosis1_field);


        imie = getIntent().getStringExtra("imie");
        nazwisko = getIntent().getStringExtra("nazwisko");
        pesel = getIntent().getStringExtra("pesel");
        data_ur = getIntent().getStringExtra("data_ur");
        plec = getIntent().getStringExtra("plec");
        diagnoza = getIntent().getStringExtra("diagnoza");
        notatki = getIntent().getStringExtra("notatki");
        docId = getIntent().getStringExtra("docId");
        email=getIntent().getStringExtra("email");
        numer = getIntent().getStringExtra("numer");

        CollectionReference patientCollection1 = Utility.getCollectionReferenceForPatient();
        DocumentReference documentReference = patientCollection1.document(docId);

        documentReference.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()){
                if (documentSnapshot.contains("nextVisit")){
                    nextVisitValue = documentSnapshot.getString("nextVisit");
                }
            }
        });

        nameField.setText(imie);
        surnameField.setText(nazwisko);
        peselField.setText(pesel);
        birthDateField.setText(data_ur);
        genderField.setText(plec);
        diagnosisField.setText(diagnoza);
        notesField.setText(notatki);
        emailField.setText(email);
        numberField.setText(numer);


        emailField.setOnClickListener(v -> {
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + email));
            startActivity(emailIntent);
        });

        numberField.setOnClickListener(v -> {
            Intent callIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + numer));
            startActivity(callIntent);
        });

        if (diagnoza == null || diagnoza.isEmpty()){
            diagnosis1Field.setVisibility(View.GONE);
        }

        if (notatki == null || notatki.isEmpty()){
            notes1Field.setVisibility(View.GONE);
        }

        //pdfBtn = findViewById(R.id.pdf_btn);

        //pdfBtn.setOnClickListener(v -> {
            //downloadPdf(imie, nazwisko, pesel, data_ur, plec, email, numer, docId);
       // });

        DocumentReference patientRef = Utility.getCollectionReferenceForPatient().document(docId);

        patientRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    if (document.getString("patientGeneratedPassword") == null || document.getString("patientGeneratedPassword").isEmpty()) {
                        progressBtn.setVisibility(View.GONE);
                    }else {
                        patientAccBtn.setVisibility(View.GONE);
                        editBtn.setVisibility(View.GONE);
                        deleteBtn.setVisibility(View.VISIBLE);
                    }
                }
            }
        });

    }

    void deletePatient(){

        AlertDialog.Builder builder = new AlertDialog.Builder(PatientFullView.this);
        builder.setMessage("Czy na pewno chcesz trwale usunąć pacjenta, jego konto i wszystkie związane z nim dane?");
        builder.setPositiveButton("Tak", (dialog, which) -> {

            DocumentReference documentReference;
            documentReference = Utility.getCollectionReferenceForPatient().document(docId);
            documentReference.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(PatientFullView.this, "Pacjent usunięty pomyślnie", Toast.LENGTH_SHORT).show();
                        deletePatientFromPatientsDetails();
                        Intent intent = new Intent(PatientFullView.this, PatientsList.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(PatientFullView.this, "Błąd podczas usuwania pacjenta", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        });builder.setNegativeButton("Nie", (dialog, which) -> {
            dialog.dismiss();
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    void deletePatientFromPatientsDetails(){
        DocumentReference documentReference;
        documentReference = Utility.getCollectionReferenceForPatientsData().document(docId);
        documentReference.delete();
    }
}