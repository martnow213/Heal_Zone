package com.example.healzone;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.widget.Button;

public class ChooseLoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_login);

        Button specialistLogin = findViewById(R.id.specialist_account_btn);

        specialistLogin.setOnClickListener(view -> {
            Intent intent = new Intent(ChooseLoginActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        Button patientLogin = findViewById(R.id.patient_account_btn);

        patientLogin.setOnClickListener(view -> {
            Intent intent = new Intent(ChooseLoginActivity.this, PatientLoginActivity.class);
            startActivity(intent);
        });
    }
}