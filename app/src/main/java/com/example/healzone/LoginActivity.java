package com.example.healzone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    EditText emailEditText, passwordEditText;
    Button loginBtn;
    TextView createAccountBtnTextView, passwordReminderBtnTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        emailEditText = findViewById(R.id.email_edit_text);
        passwordEditText = findViewById(R.id.password_edit_text);
        loginBtn = findViewById(R.id.login_btn);
        passwordReminderBtnTextView = findViewById(R.id.password_reminder_view_btn);
        createAccountBtnTextView = findViewById(R.id.create_account_text_view_btn);

        loginBtn.setOnClickListener(v->loginUser());
        createAccountBtnTextView.setOnClickListener(v->startActivity(new Intent(LoginActivity.this, CreateAccountActivity.class)));
        passwordReminderBtnTextView.setOnClickListener(v->RemindPassword());
    }

    void loginUser(){
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        boolean isValidated = validateData(email, password);
        if(!isValidated){
            return;
        }

        LoginInFirebase(email,password);
    }

    void LoginInFirebase(String email, String password){
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                if (firebaseAuth.getCurrentUser().isEmailVerified()){
                    startActivity(new Intent(LoginActivity.this,PatientsList.class));
                }else {
                    Toast.makeText(LoginActivity.this, "Potwierdź email w celu weryfikacji konta", Toast.LENGTH_SHORT).show();
                }
            }else{
                Toast.makeText(LoginActivity.this,"Nieprawidłowe dane logowania",Toast.LENGTH_SHORT).show();
            }
        });
    }

    boolean validateData(String email, String password){
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

        return true; //Wszystkie warunki spełnione
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

    void RemindPassword(){
        String email = emailEditText.getText().toString();

        if (email.isEmpty()) {
            emailEditText.setError("Wprowadź adres e-mail");
            return;
        }

        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this, "Link resetujący hasło został wysłany na Twój adres e-mail", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(LoginActivity.this, "Błąd podczas wysyłania linku resetującego hasło: " + task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}