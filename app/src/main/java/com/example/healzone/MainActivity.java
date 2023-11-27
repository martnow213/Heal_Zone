package com.example.healzone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    ImageButton menuBtn;
    Button calendarBtn;

    TextView nameField, surnameField, emailField, numberField, addressField, cityField;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkForPatients();

        nameField = findViewById(R.id.name_field);
        surnameField = findViewById(R.id.surname_field);
        emailField = findViewById(R.id.email_field);
        numberField = findViewById(R.id.phone_field);
        addressField = findViewById(R.id.address_field);
        cityField = findViewById(R.id.city_field);

        FirebaseUser currentUser1 = FirebaseAuth.getInstance().getCurrentUser();
        String userId = currentUser1.getUid();

        DocumentReference dr = Utility.getCollectionReferenceForSpecialist().document(userId);

        dr.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    String name = document.getString("specialistName");
                    String surname = document.getString("specialistSurname");
                    String email = document.getString("specialistEmail");
                    String phoneNumber = document.getString("specialistNumber");
                    String city = document.getString("specialistCity");
                    String street = document.getString("specialistStreet");
                    String building = document.getString("specialistBuilding");

                    nameField.setText(name);
                    surnameField.setText(surname);
                    emailField.setText(email);
                    numberField.setText(phoneNumber);
                    addressField.setText(street+" "+building+", ");
                    cityField.setText(city);
                    System.out.println("Name: " + name);
                } else {
                    System.out.println("Dokument nie istnieje");
                }
            } else {
                System.out.println("Błąd: " + task.getException());
            }
        });

        calendarBtn = findViewById(R.id.calendar_btn);

        calendarBtn.setOnClickListener(v->{
            Intent intent = new Intent(MainActivity.this, CalendarActivity.class);
            startActivity(intent);
        });


        menuBtn = findViewById(R.id.menu_btn);

        menuBtn.setOnClickListener(v->showMenu());
        Button patientsBtn = findViewById(R.id.patients_btn);

        patientsBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, PatientsList.class);
            startActivity(intent);
        });

    }
    void showMenu(){
        PopupMenu popupMenu = new PopupMenu(MainActivity.this, menuBtn);
        popupMenu.getMenu().add("Wyloguj");
        popupMenu.getMenu().add("Usuń konto");
        popupMenu.show();
        popupMenu.setOnMenuItemClickListener(menuItem -> {
            if(menuItem.getTitle()=="Wyloguj"){
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(MainActivity.this, ChooseLoginActivity.class));
                finish();
                return true;
            } else if (menuItem.getTitle()=="Usuń konto") {
                deleteFirebaseAccount();
                return true;}
            return false;
        });
    }

    private void deleteFirebaseAccount() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage("Czy na pewno chcesz trwale usunąć konto i wszystkie związane z nim dane?");
        builder.setPositiveButton("Tak", (dialog, which) -> {
            deleteFromSpecialists();
            FirebaseAuth.getInstance().getCurrentUser().delete()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "Konto usunięte", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(MainActivity.this, ChooseLoginActivity.class));
                            finish();
                        } else {
                            Toast.makeText(MainActivity.this, "Błąd podczas usuwania konta", Toast.LENGTH_SHORT).show();
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

    private void checkForPatients() {
        CollectionReference patientsCollection = Utility.getCollectionReferenceForPatient();
        patientsCollection.get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (queryDocumentSnapshots.isEmpty()) {
                calendarBtn.setVisibility(View.GONE);
            } else {
                calendarBtn.setVisibility(View.VISIBLE);
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Błąd podczas sprawdzania pacjentów", Toast.LENGTH_SHORT).show();
        });
    }


}
