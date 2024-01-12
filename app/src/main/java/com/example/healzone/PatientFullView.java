package com.example.healzone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfWriter;



public class PatientFullView extends AppCompatActivity {

    String imie, nazwisko, pesel, data_ur, plec, diagnoza, notatki, docId, email, numer, nextVisitValue, generatedPassword;
    TextView nameField, surnameField, peselField, birthDateField, genderField, diagnosisField, notesField, emailField, numberField, notes1Field, diagnosis1Field, generatedPswField, generatedPswField1;
    Button progressBtn, patientAccBtn;
    ImageButton editBtn, deleteBtn, pdfBtn, calendarBtn;
    LinearLayout generatedPswLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_full_view);

        generatedPswLayout = findViewById(R.id.generatedPswLayout);
        generatedPswField = findViewById(R.id.generated_psw_field);
        generatedPswField1 = findViewById(R.id.generated_psw_field1);

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

        DocumentReference patientRef = Utility.getCollectionReferenceForPatient().document(docId);

        patientRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    if (document.getString("patientGeneratedPassword") == null || document.getString("patientGeneratedPassword").isEmpty()) {
                        progressBtn.setVisibility(View.GONE);
                        generatedPswField.setVisibility(View.GONE);
                        generatedPswField1.setVisibility(View.GONE);
                    }else {
                        progressBtn.setVisibility(View.VISIBLE);
                        patientAccBtn.setVisibility(View.GONE);
                        editBtn.setVisibility(View.GONE);
                        deleteBtn.setVisibility(View.VISIBLE);
                        generatedPswField1.setVisibility(View.VISIBLE);
                        generatedPswField.setVisibility(View.VISIBLE);
                        generatedPassword = document.getString("patientGeneratedPassword");
                        generatedPswField.setText(generatedPassword);
                    }
                }
            }
        });

        pdfBtn = findViewById(R.id.pdf_btn);

        pdfBtn.setOnClickListener(v -> {
            downloadPdf(imie, nazwisko, pesel, data_ur, plec, email, numer, docId, generatedPassword);
        });

    }

    void downloadPdf(String imie, String nazwisko, String pesel, String data_ur, String plec, String email, String numer, String docId, String generatedPassword) {
        DocumentReference patientReference = Utility.getCollectionReferenceForPatient().document(docId);

        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File directory = new File(Environment.getExternalStorageDirectory(), "HealZonePDFs");

            if (!directory.exists()) {
                if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    directory.mkdirs();
                } else {
                    requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                    return;
                }
            }
            String filePath = directory.getPath() + "/" + imie + "_" + nazwisko + ".pdf";

            Document document = new Document();

            try {
                PdfWriter.getInstance(document, new FileOutputStream(filePath));

                document.open();

                BaseFont baseFont = BaseFont.createFont("assets/AlegreyaSans-Black.otf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
                BaseFont boldFont = BaseFont.createFont("assets/AlegreyaSans-Bold.otf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
                com.itextpdf.text.Font polishFont = new com.itextpdf.text.Font(baseFont);
                com.itextpdf.text.Font polishBoldFont = new com.itextpdf.text.Font(boldFont);

                Paragraph header = new Paragraph("RAPORT PACJENTA nr " + docId, polishBoldFont);
                header.setAlignment(Element.ALIGN_CENTER);
                document.add(header);

                document.add(Chunk.NEWLINE);
                document.add(new Paragraph("DANE PACJENTA",polishBoldFont));
                document.add(new Paragraph("Imię: " + imie, polishFont));
                document.add(new Paragraph("Nazwisko: " + nazwisko, polishFont));
                document.add(Chunk.NEWLINE);
                document.add(new Paragraph("Płeć: " + plec, polishFont));
                document.add(new Paragraph("PESEL: " + pesel, polishFont));
                document.add(new Paragraph("Data urodzenia: " + data_ur, polishFont));
                document.add(Chunk.NEWLINE);
                document.add(new Paragraph("DANE KONTAKTOWE", polishBoldFont));
                document.add(new Paragraph("Email: " + email, polishFont));
                document.add(new Paragraph("Numer telefonu: " + numer, polishFont));
                document.add(Chunk.NEWLINE);
                if (generatedPassword != null){
                    document.add(new Paragraph("Wygenerowane hasło: " + generatedPassword, polishFont));

                    // Strona kopia dla pacjenta
                    document.newPage();
                    Paragraph copyHeader = new Paragraph("RAPORT - KOPIA DLA PACJENTA", polishBoldFont);
                    copyHeader.setAlignment(Element.ALIGN_CENTER);
                    document.add(copyHeader);

                    document.add(Chunk.NEWLINE);
                    document.add(new Paragraph("DANE PACJENTA", polishBoldFont));
                    document.add(new Paragraph("Imię: " + imie, polishFont));
                    document.add(new Paragraph("Nazwisko: " + nazwisko, polishFont));
                    document.add(Chunk.NEWLINE);
                    document.add(new Paragraph("Płeć: " + plec, polishFont));
                    document.add(new Paragraph("PESEL: " + pesel, polishFont));
                    document.add(new Paragraph("Data urodzenia: " + data_ur, polishFont));
                    document.add(Chunk.NEWLINE);
                    document.add(new Paragraph("DANE KONTAKTOWE", polishBoldFont));
                    document.add(new Paragraph("Email: " + email, polishFont));
                    document.add(new Paragraph("Numer telefonu: " + numer, polishFont));
                    document.add(Chunk.NEWLINE);
                    document.add(new Paragraph("Wygenerowane hasło: " + generatedPassword, polishBoldFont));
                    document.add(Chunk.NEWLINE);
                    document.add(new Paragraph("Zainstaluj aplikację HealZone na swoim urządzieniu, a następnie wybierz opcję logowania jako pacjent. Zaloguj się przy użyciu powyższego hasła. Hasło możesz zmienić w dowolnym momencie w ustawieniach swojego konta. W razie problemów skonsultuj się ze swoim specjalistą.",polishFont));

                }

                document.close();

                Toast.makeText(this, "PDF wygenerowany pomyślnie. Zapisano w: " + filePath, Toast.LENGTH_SHORT).show();
                openPdf(filePath);

            } catch (FileNotFoundException e) {
                Log.d("PDF_DEBUG", "File Path: " + filePath);

                e.printStackTrace();
                Toast.makeText(this, "Błąd generowania PDF: Plik nie znaleziony", Toast.LENGTH_SHORT).show();
            } catch (DocumentException e) {
                e.printStackTrace();
                Toast.makeText(this, "Błąd generowania PDF: Wyjątek dokumentu", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Błąd odczytu czcionki", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Pamięć zewnętrzna niedostępna", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isPdfViewerInstalled() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setType("application/pdf");
        return getPackageManager().resolveActivity(intent, 0) != null;
    }

    private void openPdf(String filePath) {
        File file = new File(filePath);
        Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".provider", file);

        Intent pdfIntent = new Intent(Intent.ACTION_VIEW);
        pdfIntent.setDataAndType(uri, "application/pdf");
        pdfIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        if (isPdfViewerInstalled()) {
            // Otwórz plik PDF przy użyciu zainstalowanej przeglądarki PDF
            startActivity(pdfIntent);
        } else {
            // Jeśli brak zainstalowanej przeglądarki PDF, wyświetl komunikat
            Toast.makeText(this, "Brak zainstalowanej przeglądarki PDF", Toast.LENGTH_SHORT).show();
        }
    }
    void deletePatient(){

        AlertDialog.Builder builder = new AlertDialog.Builder(PatientFullView.this);
        builder.setMessage("Czy na pewno chcesz trwale usunąć pacjenta i jego konto? \n \n Jeśli masz zaplanowane wizyty dla tego pacjenta, pozostaną one w Twoim kalendarzu.");
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


        DocumentReference documentReference3;
        documentReference3 = FirebaseFirestore.getInstance().collection("progressNote").document(docId);
        documentReference3.delete();


    }
}