package com.example.healzone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class PatientDetailsActivity extends AppCompatActivity {

    EditText patientNameEditText, patientSurnameEditText, patientPeselEditText, patientBirthDateEditText, patientDiagnosisEditText, patientNotesEditText, patientEmailEditText, patientNumberEditText;
    RadioButton maleButton, femaleButton;
    ImageButton savePatientButton;
    TextView pageTitleTextView, deletePatientTextViewBtn;
    String imie, nazwisko, pesel, data_ur, plec, diagnoza, notatki, docId, email, numer;
    boolean isEditing = false;

    boolean isEmailUnique, isNumberUnique, isPeselUnique = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_details);

        patientNameEditText = findViewById(R.id.patient_name_text);
        patientSurnameEditText = findViewById(R.id.patient_surname_text);
        patientPeselEditText = findViewById(R.id.patient_pesel_text);
        patientBirthDateEditText = findViewById(R.id.patient_birthday_text);
        patientDiagnosisEditText = findViewById(R.id.patient_diagnosis_text);
        patientNotesEditText = findViewById(R.id.patient_notes_text);
        patientEmailEditText = findViewById(R.id.patient_email_text);
        patientNumberEditText = findViewById(R.id.patient_number_text);

        maleButton = findViewById(R.id.male_radio_button);
        femaleButton = findViewById(R.id.female_radio_button);
        savePatientButton = findViewById(R.id.save_patient_btn);
        deletePatientTextViewBtn = findViewById(R.id.delete_patient_btn);

        savePatientButton.setOnClickListener(v-> savePatient());

        pageTitleTextView = findViewById(R.id.page_title);
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


        //Jeśli istnieje id dokumentu, jesteśmy w trybie edycji, tzn. że dane pacjenta będą przekazywane do pól edycji tekstu
        if(docId!=null&&!docId.isEmpty()){
            isEditing = true;
        }

        if (isEditing == true){
            pageTitleTextView.setText("Edycja danych pacjenta");

            patientNameEditText.setText(imie);
            patientSurnameEditText.setText(nazwisko);
            patientPeselEditText.setText(pesel);
            patientBirthDateEditText.setText(data_ur);
            patientEmailEditText.setText(email);
            patientNumberEditText.setText(numer);
            patientDiagnosisEditText.setText(diagnoza);
            patientNotesEditText.setText(notatki);

            if (plec.equals("Mężczyzna")){
                maleButton.setChecked(true);
            }else if(plec.equals("Kobieta")){
                femaleButton.setChecked(true);
            }

            //Ustawienie widoczności przycisku usuwania konta
            deletePatientTextViewBtn.setVisibility(View.VISIBLE);
            deletePatientTextViewBtn.setOnClickListener(v->deletePatientFromFirebase());
        }

    }

    //Logika usuwania pacjenta z bazy
    void deletePatientFromFirebase(){
        AlertDialog.Builder builder = new AlertDialog.Builder(PatientDetailsActivity.this);
        builder.setMessage("Czy na pewno chcesz trwale usunąć pacjenta? \n \n Jeśli masz zaplanowane wizyty dla tego pacjenta, pozostaną one w Twoim kalendarzu.");
        builder.setPositiveButton("Tak", (dialog, which) -> {

            DocumentReference documentReference;
            documentReference = Utility.getCollectionReferenceForPatient().document(docId);
            documentReference.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(PatientDetailsActivity.this, "Pacjent usunięty pomyślnie", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(PatientDetailsActivity.this, PatientsList.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(PatientDetailsActivity.this, "Błąd podczas usuwania pacjenta", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        });builder.setNegativeButton("Nie", (dialog, which) -> {
            dialog.dismiss();
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }



    void savePatient(){
        String patientName = patientNameEditText.getText().toString();
        String patientSurname = patientSurnameEditText.getText().toString();
        String patientPesel = patientPeselEditText.getText().toString();
        String patientBirthDate = patientBirthDateEditText.getText().toString();
        String patientEmail = patientEmailEditText.getText().toString();
        String patientNumber = patientNumberEditText.getText().toString();
        String patientDiagnosis = patientDiagnosisEditText.getText().toString();
        String patientNotes = patientNotesEditText.getText().toString();




        boolean isValidated = validateData(patientName, patientSurname, patientPesel, patientBirthDate, patientEmail, patientNumber);
        if(!isValidated){
            return;
        }

        Patient patient = new Patient();
        patient.setPatientName(patientName);
        patient.setPatientSurname(patientSurname);
        patient.setPatientPesel(patientPesel);
        patient.setPatientBirthdate(patientBirthDate);
        if (maleButton.isChecked()) {
            patient.setPatientGender("Mężczyzna");
        } else if (femaleButton.isChecked()) {
            patient.setPatientGender("Kobieta");
        }
        patient.setPatientEmail(patientEmail);
        patient.setPatientNumber(patientNumber);
        patient.setPatientDiagnosis(patientDiagnosis);
        patient.setPatientNotes(patientNotes);
        patient.setTimestamp(Timestamp.now());

        //Zabezpieczenie przed dodaniem pacjentów o takich samych mailach, nr tel i pesel
        //Podział na tryb dodawania nowego pacjenta i tryb edycji
        if (!isEditing){
            Utility.checkIfEmailExists(patientEmail)
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (queryDocumentSnapshots.isEmpty()) {
                            isEmailUnique = true; //Nie znaleziono takiego maila w bazie - jest on unikatowy
                            // Sprawdź unikalność numeru pesel
                            Utility.checkIfPeselExists(patientPesel)
                                    .addOnSuccessListener(peselSnapshots -> {
                                        if (peselSnapshots.isEmpty()) {
                                            isPeselUnique = true;
                                            // Sprawdź unikalność numeru telefonu
                                            Utility.checkIfNumberExists(patientNumber)
                                                    .addOnSuccessListener(numberSnapshots -> {
                                                        if (numberSnapshots.isEmpty()) {
                                                            isNumberUnique = true;
                                                            // Zapisz pacjenta do bazy danych
                                                            savePatientToFirebase(patient);
                                                        } else {
                                                            isNumberUnique = false;
                                                            patientNumberEditText.setError("Numer zajęty");
                                                        }
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        Toast.makeText(PatientDetailsActivity.this, "Błąd podczas sprawdzania numeru w bazie danych", Toast.LENGTH_SHORT).show();
                                                        isNumberUnique = false;
                                                    });
                                        } else {
                                            isPeselUnique = false;
                                            patientPeselEditText.setError("Pesel zajęty");
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(PatientDetailsActivity.this, "Błąd podczas sprawdzania numeru pesel w bazie danych", Toast.LENGTH_SHORT).show();
                                        isPeselUnique = false;
                                    });
                        } else {
                            isEmailUnique = false;
                            patientEmailEditText.setError("Email zajęty");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(PatientDetailsActivity.this, "Błąd podczas sprawdzania emaila w bazie danych", Toast.LENGTH_SHORT).show();
                        isEmailUnique = false;
                    });
        }else {
            // Edycja istniejącego pacjenta

            String currentEmail = getIntent().getStringExtra("email");
            String currentPesel = getIntent().getStringExtra("pesel");
            String currentNumber = getIntent().getStringExtra("numer");

            // Sprawdzenie, czy wartości się zmieniły, jeśli nie to pozwolenie na zapisanie już istniejących
            boolean isEmailChanged = !currentEmail.equals(patientEmail);
            boolean isPeselChanged = !currentPesel.equals(patientPesel);
            boolean isNumberChanged = !currentNumber.equals(patientNumber);

            if(isEmailChanged==false&&isNumberChanged==false&&isPeselChanged==false){
                savePatientToFirebase(patient);
            }

            //Jeśli, którakolwiek wartość się zmieniła - logika jak wyżej
            if (isEmailChanged){
                Utility.checkIfEmailExists(patientEmail)
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            if (queryDocumentSnapshots.isEmpty()) {
                                isEmailUnique = true;
                                if (isEmailUnique == true && isNumberChanged == false && isPeselChanged == false){
                                    savePatientToFirebase(patient);
                                }
                            } else {
                                isEmailUnique = false;
                                patientEmailEditText.setError("Email zajęty");
                            }
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(PatientDetailsActivity.this, "Błąd podczas sprawdzania emaila w bazie danych", Toast.LENGTH_SHORT).show();
                            isEmailUnique = false;
                        });
            }

            if (isNumberChanged){
                Utility.checkIfNumberExists(patientNumber)
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            if (queryDocumentSnapshots.isEmpty()) {
                                isNumberUnique = true;
                                if (isNumberUnique == true && isPeselChanged == false && isEmailChanged == false){
                                    savePatientToFirebase(patient);
                                } else if (isNumberUnique == true && isEmailUnique == true && isPeselChanged == false) {
                                    savePatientToFirebase(patient);
                                }
                            } else {
                                isNumberUnique = false;
                                patientNumberEditText.setError("Numer zajęty");
                            }
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(PatientDetailsActivity.this, "Błąd podczas sprawdzania numeru w bazie danych", Toast.LENGTH_SHORT).show();
                            isNumberUnique = false;
                        });
            }

            if (isPeselChanged){
                Utility.checkIfPeselExists(patientPesel)
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            if (queryDocumentSnapshots.isEmpty()) {
                                isPeselUnique = true;
                                if (isPeselUnique == true && isNumberChanged == false && isEmailChanged == false){
                                    savePatientToFirebase(patient);
                                } else if (isPeselUnique == true && isEmailUnique == true && isNumberChanged == false) {
                                    savePatientToFirebase(patient);
                                } else if (isPeselUnique == true && isNumberUnique == true && isEmailChanged == false) {
                                    savePatientToFirebase(patient);
                                } else if (isPeselUnique == true && isNumberUnique == true && isEmailUnique == true) {
                                    savePatientToFirebase(patient);
                                }
                            } else {
                                isPeselUnique = false;
                                patientPeselEditText.setError("Pesel zajęty");
                            }
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(PatientDetailsActivity.this, "Błąd podczas sprawdzania numeru pesel w bazie danych", Toast.LENGTH_SHORT).show();
                            isPeselUnique = false;
                        });
            }

        }


    }

    void savePatientToFirebase(Patient patient){
        DocumentReference documentReference;
        if (isEditing){
            documentReference = Utility.getCollectionReferenceForPatient().document(docId);
        }else {
            documentReference = Utility.getCollectionReferenceForPatient().document();
        }

        documentReference.set(patient).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
               if (task.isSuccessful()){
                   if (isEditing)
                   {
                       Toast.makeText(PatientDetailsActivity.this, "Edytowano pomyślnie", Toast.LENGTH_SHORT).show();
                   }else{
                       Toast.makeText(PatientDetailsActivity.this, "Pacjent dodany pomyślnie", Toast.LENGTH_SHORT).show();
                   }
                   Intent intent = new Intent(PatientDetailsActivity.this, PatientsList.class);
                   startActivity(intent);
                   finish();
               }else {
                   Toast.makeText(PatientDetailsActivity.this, "Błąd podczas dodawania pacjenta", Toast.LENGTH_SHORT).show();
               }
            }
        });}

    //Sprawdzenie poprawności wprowadzonych danych
    boolean validateData(String patientName, String patientSurname, String patientPesel, String patientBirthDate, String patientEmail, String patientNumber){
        //Imie
        if (!(patientName.length() > 0 && Character.isUpperCase(patientName.charAt(0)) && patientName.matches("^[A-ZĄĆĘŁŃÓŚŻŹ][a-zA-ZąćęłńóśźżźĄĆĘŁŃÓŚŻŹ]+(\\s[A-ZĄĆĘŁŃÓŚŻŹ][a-zA-ZąćęłńóśźżźĄĆĘŁŃÓŚŻŹ]+)?$"))) {
            patientNameEditText.setError("Błędny format");
            return false;
        }

        //Nazwisko
        if (!(patientSurname.length() > 0 && Character.isUpperCase(patientSurname.charAt(0)) && patientSurname.matches("^[A-ZĄĆĘŁŃÓŚŻŹ][a-zA-ZąćęłńóśźżźĄĆĘŁŃÓŚŻŹ]+(\\s[A-ZĄĆĘŁŃÓŚŻŹ][a-zA-ZąćęłńóśźżźĄĆĘŁŃÓŚŻŹ]+)?$"))) {
            patientSurnameEditText.setError("Błędny format");
            return false;
        }

        //Format numeru pesel
        if (!patientPesel.matches("[0-9]{11}")) {
            patientPeselEditText.setError("Błędny format");
            return false;
        }

        // Pobierz obecną datę
        Date currentDate = new Date();

        // Format daty urodzenia
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
        try {
            Date birthDate = dateFormat.parse(patientBirthDate);

            // Sprawdź, czy data urodzenia jest większa od obecnej daty
            if (birthDate.after(currentDate)) {
                patientBirthDateEditText.setError("Błędna data urodzenia");
                return false;
            }

            // Sprawdź, czy data urodzenia jest większa niż 01.01.1900
            Calendar minBirthDate = Calendar.getInstance();
            minBirthDate.set(1890, Calendar.JANUARY, 1);
            if (birthDate.before(minBirthDate.getTime())) {
                patientBirthDateEditText.setError("Błędny rok urodzenia");
                return false;
            }

        } catch (ParseException e) {
            // Obsłuż błąd parsowania daty
            e.printStackTrace();
            patientBirthDateEditText.setError("Błędny format");
            return false;
        }

        //Format daty urodzenia
        if (!patientBirthDate.matches("[0-9][0-9].[0-9][0-9].[0-9][0-9][0-9][0-9]")) {
            patientBirthDateEditText.setError("Błędny format daty urodzenia");
            return false;
        }

        ///Płeć
        if (!(maleButton.isChecked() || femaleButton.isChecked())) {
            femaleButton.setError("Wybierz płeć");
            return false;
        } else {
            femaleButton.setError(null);}

        //Email
        if(!Patterns.EMAIL_ADDRESS.matcher(patientEmail).matches()){
            patientEmailEditText.setError("Email nieprawidłowy");
            return false;
        }

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String yourEmail = currentUser.getEmail();
        if (yourEmail.equalsIgnoreCase(patientEmail)){
            patientEmailEditText.setError("Nie możesz użyć swojego adresu email");
            return false;
        }

        //Format numeru telefonu
        if (!patientNumber.matches("[1-9][0-9]{8}")) {
            patientNumberEditText.setError("Błędny numer");
            return false;
        }

        return true; //Wszystkie warunki spełnione - można zapisać pacjenta
    }
}