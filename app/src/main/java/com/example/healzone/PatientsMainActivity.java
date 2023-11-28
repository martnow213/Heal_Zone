package com.example.healzone;

import static com.example.healzone.Utility.hasUpperCaseLetter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.security.auth.callback.PasswordCallback;

public class PatientsMainActivity extends AppCompatActivity {

    ImageButton menuBtn;
    Button progressBtn, calendarBtn;
    String docId, patientName, patientSurname, patientEmail, patientNumber, patientPassword, specialistName, specialistNumber, specialistEmail, nextVisit;
    TextView nameField, surnameField, emailField, phoneField, specialistNameInfo, specialistEmailInfo, specialistNumberInfo, nextVisitField, nextVisitField1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patients_main);

        //DANE PACJENTA:
        docId = getIntent().getStringExtra("docId");
        patientName = getIntent().getStringExtra("patientName");
        patientSurname = getIntent().getStringExtra("patientSurname");
        patientEmail = getIntent().getStringExtra("patientEmail");
        patientNumber = getIntent().getStringExtra("patientNumber");
        patientPassword = getIntent().getStringExtra("patientPassword");

        nameField = findViewById(R.id.name_field);
        nameField.setText(patientName);
        surnameField = findViewById(R.id.surname_field);
        surnameField.setText(patientSurname);
        emailField = findViewById(R.id.email_field);
        emailField.setText(patientEmail);
        phoneField = findViewById(R.id.phone_field);
        phoneField.setText(patientNumber);


        nextVisitField = findViewById(R.id.nextvisit_field);
        nextVisitField1 = findViewById(R.id.nextvisit1_field);

        deleteOldVisits();
        setUpNextVisit();


        //DANE SPECIALISTY
        specialistName = getIntent().getStringExtra("specialistName");
        specialistEmail = getIntent().getStringExtra("specialistEmail");
        specialistNumber = getIntent().getStringExtra("specialistNumber");

        specialistNameInfo = findViewById(R.id.specialist_info_name);
        specialistNameInfo.setText(specialistName);

        specialistEmailInfo = findViewById(R.id.specialist_info_mail);
        specialistEmailInfo.setText(specialistEmail);
        specialistEmailInfo.setOnClickListener(v -> {
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + specialistEmail));
            startActivity(emailIntent);
        });

        specialistNumberInfo = findViewById(R.id.specialist_info_number);
        specialistNumberInfo.setText(specialistNumber);
        specialistNumberInfo.setOnClickListener(v -> {
            Intent callIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + specialistNumber));
            startActivity(callIntent);
        });


        menuBtn = findViewById(R.id.menu_btn);
        menuBtn.setOnClickListener(v -> showMenu());

        progressBtn = findViewById(R.id.patients_progress_btn);
        progressBtn.setOnClickListener(v -> {
            Intent intent = new Intent(PatientsMainActivity.this, PatientsNotesActivity.class);
            intent.putExtra("docId", docId);
            startActivity(intent);
        });

        calendarBtn = findViewById(R.id.calendar_btn);

        calendarBtn.setOnClickListener(v -> {
            Intent intent = new Intent(PatientsMainActivity.this, ThisPatientVisits.class);
            intent.putExtra("docId", docId);
            Log.d("Intent content", "Przekazane docId: "+docId);
            startActivity(intent);
        });

    }

    void showMenu() {
        PopupMenu popupMenu = new PopupMenu(PatientsMainActivity.this, menuBtn);
        popupMenu.getMenu().add("Zmień hasło"); // Dodaj nową opcję "Zmień hasło"
        popupMenu.getMenu().add("Wyloguj");
        popupMenu.show();
        popupMenu.setOnMenuItemClickListener(menuItem -> {
            if (menuItem.getTitle().equals("Zmień hasło")) {
                showChangePasswordDialog(); // Wywołaj funkcję do zmiany hasła
                return true;
            } else if (menuItem.getTitle().equals("Wyloguj")) {
                startActivity(new Intent(PatientsMainActivity.this, ChooseLoginActivity.class));
                finish();
                return true;
            }
            return false;
        });
    }

    void showChangePasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Zmień hasło");

        View view = getLayoutInflater().inflate(R.layout.change_password_dialog, null);
        builder.setView(view);

        final EditText currentPasswordEditText = view.findViewById(R.id.current_password);
        final EditText newPasswordEditText = view.findViewById(R.id.new_password);

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        final Button saveNewPswBtn = view.findViewById(R.id.save_new_psw_btn);
        final Button cancelBtn = view.findViewById(R.id.cancel_btn);

        saveNewPswBtn.setOnClickListener(v -> {
            AlertDialog.Builder builder1 = new AlertDialog.Builder(PatientsMainActivity.this);
            builder1.setMessage("Czy na pewno chcesz zmienić hasło?");
            builder1.setPositiveButton("Tak", (dialog, which) -> {
                String currentPassword = currentPasswordEditText.getText().toString();
                String newPassword = newPasswordEditText.getText().toString();

                getPasswords(newPassword, isPasswordUnique -> {
                    if (currentPassword.equals(patientPassword)) {
                        if (newPassword.equals(patientPassword)) {
                            newPasswordEditText.setError("Hasła są takie same");
                        } else {
                            if (isPasswordUnique) {
                                saveNewPasswordToFirebase(newPassword);
                                alertDialog.dismiss();
                            } else {
                                newPasswordEditText.setError("Hasło jest zajęte, wybierz inne");
                            }
                        }
                    } else {
                        currentPasswordEditText.setError("Hasło niepoprawne");
                    }
                });
            });
            builder1.setNegativeButton("Nie", (dialog, which) -> {
                dialog.dismiss();
            });
            AlertDialog dialog = builder1.create();
            dialog.show();
        });

        cancelBtn.setOnClickListener(v -> {
            alertDialog.dismiss();
        });

    }


    void saveNewPasswordToFirebase(String password) {

        validatePassword(password);
        if (validatePassword(password) == true) {
            DocumentReference patientRef = Utility.getCollectionReferenceForPatientsData().document(docId);

            Map<String, Object> data = new HashMap<>();
            data.put("patientPassword", password);
            patientPassword = password;


            patientRef.update(data)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(PatientsMainActivity.this, "Hasło zapisane w bazie danych Firebase.", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(PatientsMainActivity.this, "Wystąpił błąd podczas zapisywania hasła w bazie danych Firebase.", Toast.LENGTH_SHORT).show();
                    });
        }


    }

    boolean validatePassword(String password) {
        //Dlugosc hasla
        if (password.length() < 8) {
            Toast.makeText(PatientsMainActivity.this, "Hasło musi mieć min. 8 znaków", Toast.LENGTH_SHORT).show();
            return false;
        }

        //Min. jedna duza litera w hasle
        if (!hasUpperCaseLetter(password)) {
            Toast.makeText(PatientsMainActivity.this, "Hasło musi zawierać min. jedną dużą literę", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(PatientsMainActivity.this, "Hasło musi zawierać min. jedną cyfrę", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void getPasswords(String newPassword, PasswordCallback callback) {
        CollectionReference collectionReference = Utility.getCollectionReferenceForPatientsData();
        collectionReference.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> passwordsList = new ArrayList<>();

                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        String password = documentSnapshot.getString("patientPassword");

                        if (password != null) {
                            passwordsList.add(password);
                        }
                    }

                    for (String passwords : passwordsList) {
                        Log.d("Passwords in database: ", passwords);
                    }

                    boolean isPasswordUnique = !passwordsList.contains(newPassword);
                    callback.onResult(isPasswordUnique);
                });
    }

    interface PasswordCallback {
        void onResult(boolean isPasswordUnique);
    }

    //Usuwanie wizyt, które są z przeszłości
    private void deleteOldVisits() {
        CollectionReference visitsCollection = Utility.getCollectionReferenceForPatientAllVisits(docId);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String currentDate = dateFormat.format(new Date());

        Query oldVisitsQuery = visitsCollection.whereLessThanOrEqualTo("data", currentDate);

        oldVisitsQuery.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    document.getReference().delete();
                }
            }
        });
    }


    private void setUpNextVisit() {
        CollectionReference collectionReference = Utility.getCollectionReferenceForPatientAllVisits(docId);

        collectionReference.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> dateAndTimeList = new ArrayList<>();

                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {

                        String date = documentSnapshot.getString("data");
                        String time = documentSnapshot.getString("czas");

                        if (date != null && time != null) {
                            dateAndTimeList.add(date + ", " + time);
                        }
                    }

                    for (String dateAndTime : dateAndTimeList) {
                        Log.d("DateAndTime", dateAndTime);
                    }

                    Collections.sort(dateAndTimeList, new DateTimeComparator());

                    for (String sortedDateAndTime : dateAndTimeList) {
                        Log.d("SortedDateAndTime", sortedDateAndTime);
                    }

                    nextVisit = dateAndTimeList.get(0);

                    nextVisitField.setText(nextVisit);

                    if (nextVisit == null || nextVisit.isEmpty()) {
                        nextVisitField1.setVisibility(View.GONE);
                    }
                });

    }

    public class DateTimeComparator implements Comparator<String> {
        private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd, HH:mm");

        @Override
        public int compare(String date1, String date2) {
            try {
                Date d1 = sdf.parse(date1);
                Date d2 = sdf.parse(date2);

                int dateComparison = d1.compareTo(d2);

                if (dateComparison != 0) {
                    return dateComparison;
                } else {
                    int hour1 = Integer.parseInt(date1.substring(11, 13));
                    int hour2 = Integer.parseInt(date2.substring(11, 13));

                    return Integer.compare(hour1, hour2);
                }
            } catch (ParseException e) {
                e.printStackTrace();
                return 0;
            }
        }


    }
}

