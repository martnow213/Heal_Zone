package com.example.healzone;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class InfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        TextView specialistListBtn = findViewById(R.id.specialist_list);

        specialistListBtn.setOnClickListener(view -> {
            Intent intent = new Intent(InfoActivity.this, SpecialistList.class);
            startActivity(intent);
        });
    }
}