package com.example.healzone;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

public class NoteFullView extends AppCompatActivity {

    TextView howAreYouTextView, gratitudeTextView, strugglesTextView, extraNotesTextView, extraNotes1TextView;
    String howAreYou, gratitude, struggles, extraNotes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_full_view);

        howAreYouTextView = findViewById(R.id.howAreYou_field);
        gratitudeTextView = findViewById(R.id.gratitude_field);
        strugglesTextView = findViewById(R.id.struggles_field);
        extraNotesTextView = findViewById(R.id.extraNotes_field);
        extraNotes1TextView = findViewById(R.id.extraNotes1_field);

        howAreYou = getIntent().getStringExtra("howAreYou");
        gratitude = getIntent().getStringExtra("gratitude");
        struggles = getIntent().getStringExtra("struggles");
        extraNotes = getIntent().getStringExtra("extraNotes");

        howAreYouTextView.setText(howAreYou);
        gratitudeTextView.setText(gratitude);
        strugglesTextView.setText(struggles);
        extraNotesTextView.setText(extraNotes);

        if (extraNotes == null || extraNotes.isEmpty()){
            extraNotes1TextView.setVisibility(View.GONE);
        }


    }
}