package com.example.healzone;

import static com.example.healzone.Utility.generateAvailableHours;
import static com.example.healzone.Utility.hasUpperCaseLetter;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;

public class CreateAccountActivity extends AppCompatActivity {

    EditText emailEditText, passwordEditText, confirmPasswordEditText;
    Button createAccountBtn;
    TextView loginBtnTextView, hello, registerText;
    EditText nameEditText, surnameEditText, phoneNumberEditText, cityEditText, streetEditText, buildingNumberEditText;

    LinearLayout lowerButtons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);


        emailEditText = findViewById(R.id.email_edit_text);
        passwordEditText = findViewById(R.id.password_edit_text);
        confirmPasswordEditText = findViewById(R.id.confirm_password_edit_text);
        createAccountBtn = findViewById(R.id.create_account_btn);
        loginBtnTextView = findViewById(R.id.login_text_view_btn);
        hello = findViewById(R.id.hello);
        registerText = findViewById(R.id.registertext);
        lowerButtons = findViewById(R.id.lowerButtonsLayout);

        //Dane użytkownika:
        nameEditText = findViewById(R.id.name_edit_text);
        surnameEditText = findViewById(R.id.surname_edit_text);
        phoneNumberEditText = findViewById(R.id.phone_number_edit_text);
        cityEditText = findViewById(R.id.city_edit_text);
        streetEditText = findViewById(R.id.street_edit_text);
        buildingNumberEditText = findViewById(R.id.building_edit_text);

        createAccountBtn.setOnClickListener(v->createAccount() );
        loginBtnTextView.setOnClickListener(v->startActivity(new Intent(CreateAccountActivity.this, LoginActivity.class)));
    }
    void createAccount() {
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        String confirmPassword = confirmPasswordEditText.getText().toString();
        String name = nameEditText.getText().toString();
        String surname = surnameEditText.getText().toString();
        String phoneNumber = phoneNumberEditText.getText().toString();
        String city = cityEditText.getText().toString();
        String street = streetEditText.getText().toString();
        String building = buildingNumberEditText.getText().toString();


        boolean isValidated = validateData(email, password, confirmPassword, name, surname, phoneNumber, city, street, building);
        if (!isValidated) {
            return;
        }

        createAccountInFirebase(email, password, name, surname, phoneNumber, city, street, building);
    }

    void createAccountInFirebase(String email, String password, String name, String surname, String phoneNumber, String city, String street, String building) {


        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(CreateAccountActivity.this, task -> {
            if (task.isSuccessful()) {

                Specialist specialist = new Specialist();
                specialist.setSpecialistName(name);
                specialist.setSpecialistSurname(surname);
                specialist.setSpecialistEmail(email);
                specialist.setSpecialistNumber(phoneNumber);
                specialist.setSpecialistCity(city);
                specialist.setSpecialistStreet(street);
                specialist.setSpecialistBuilding(building);


                addSpecialistToFirebase(specialist);

                SharedPreferences sharedPref = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("name", specialist.specialistName);
                editor.putString("surname", specialist.specialistSurname);
                editor.putString("email", specialist.specialistEmail);
                editor.putString("phoneNumber", specialist.specialistNumber);
                editor.putString("city", specialist.specialistCity);
                editor.putString("street", specialist.specialistStreet);
                editor.putString("building", specialist.specialistBuilding);
                editor.apply();
                Log.d("CreateAccountActivity", "Zapisano wartość name w SharedPreferences: " + name);


                Toast.makeText(CreateAccountActivity.this, "Potwierdź email w celu weryfikacji konta", Toast.LENGTH_SHORT).show();
                firebaseAuth.getCurrentUser().sendEmailVerification();
                firebaseAuth.signOut();
                Intent intent = new Intent(CreateAccountActivity.this, LoginActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(CreateAccountActivity.this, task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    void addSpecialistToFirebase(Specialist specialist) {
        DocumentReference documentReference;
        documentReference = Utility.getCollectionReferenceForSpecialist().document(FirebaseAuth.getInstance().getCurrentUser().getUid());

        documentReference.set(specialist).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(CreateAccountActivity.this, "Specjalista dodany pomyślnie", Toast.LENGTH_SHORT).show();
                generateAvailableHours();
                finish();
            } else {
                Toast.makeText(CreateAccountActivity.this, "Błąd podczas dodawania specjalisty", Toast.LENGTH_SHORT).show();
            }
        });
    }


    //Walidacja danych wprowadzonych przez uzytkownika
    boolean validateData(String email, String password, String confirmPassword, String name, String surname, String phoneNumber, String city, String street, String building){
        //Poprawny format maila
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            emailEditText.setError("Email nieprawidłowy");
            return false;
        }

        //Dlugosc hasla
        if (password.length()<8){
            passwordEditText.setError("Hasło musi mieć min. 8 znaków");
            return false;
        }

        //Min. jedna duza litera w hasle
        if (!hasUpperCaseLetter(password)) {
            passwordEditText.setError("Hasło musi zawierać min. jedną dużą literę");
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
            passwordEditText.setError("Hasło musi zawierać min. jedną cyfrę");
            return false;
        }

        //Czy hasla sa takie same
        if(!password.equals(confirmPassword)){
            confirmPasswordEditText.setError("Hasła nie są takie same");
            return false;
        }

        //Imie
        if (!(name.length() > 0 && Character.isUpperCase(name.charAt(0)) && name.matches("^[A-ZĄĆĘŁŃÓŚŻŹ][a-zA-ZąćęłńóśźżźĄĆĘŁŃÓŚŻŹ]+(\\s[A-ZĄĆĘŁŃÓŚŻŹ][a-zA-ZąćęłńóśźżźĄĆĘŁŃÓŚŻŹ]+)?$"))) {
            nameEditText.setError("Błędny format");
            return false;
        }

        //Nazwisko
        if (!(surname.length() > 0 && Character.isUpperCase(surname.charAt(0)) && surname.matches("^[A-ZĄĆĘŁŃÓŚŻŹ][a-zA-ZąćęłńóśźżźĄĆĘŁŃÓŚŻŹ]+(\\s[A-ZĄĆĘŁŃÓŚŻŹ][a-zA-ZąćęłńóśźżźĄĆĘŁŃÓŚŻŹ]+)?$"))) {
            surnameEditText.setError("Błędny format");
            return false;
        }

        //Format numeru telefonu
        if (!phoneNumber.matches("[1-9][0-9]{8}")) {
            phoneNumberEditText.setError("Błędny numer");
            return false;
        }

        //Miasto
        if (!(city.length() > 0 && Character.isUpperCase(city.charAt(0)) && city.matches("^[A-ZĄĆĘŁŃÓŚŻŹ][a-zA-ZąćęłńóśźżźĄĆĘŁŃÓŚŻŹ]+(\\s[A-ZĄĆĘŁŃÓŚŻŹ][a-zA-ZąćęłńóśźżźĄĆĘŁŃÓŚŻŹ]+)?$"))) {
            cityEditText.setError("Błędny format");
            return false;
        }

        //Ulica
        if (!(street.length() > 0)) {
            streetEditText.setError("Błędny format");
            return false;
        }

        //Numer budynku
        if (!(building.length() > 0 && building.charAt(0) != '0')) {
            buildingNumberEditText.setError("Błędny format");
            return false;
        }
        return true; //Wszystkie warunki spełnione
    }

}