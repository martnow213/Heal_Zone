package com.example.healzone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;

import java.sql.Time;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

public class NoteDetailsActivity extends AppCompatActivity {

    ImageButton saveNoteBtn;
    RadioButton rateBtn1, rateBtn2, rateBtn3, rateBtn4, rateBtn5;
    EditText howAreYouEditText, gratitudeEditText, strugglesEditText, extraNotesEditText;
    TextView pageTitleTextView;

    String docId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_details);

        saveNoteBtn = findViewById(R.id.save_note_btn);
        pageTitleTextView = findViewById(R.id.page_title);
        howAreYouEditText = findViewById(R.id.how_are_u_text);
        gratitudeEditText = findViewById(R.id.gratitude_text);
        strugglesEditText = findViewById(R.id.struggles_text);
        extraNotesEditText = findViewById(R.id.extra_notes_text);
        rateBtn1 = findViewById(R.id.radio_button1);
        rateBtn2 = findViewById(R.id.radio_button2);
        rateBtn3 = findViewById(R.id.radio_button3);
        rateBtn4 = findViewById(R.id.radio_button4);
        rateBtn5 = findViewById(R.id.radio_button5);

        docId=getIntent().getStringExtra("docId");

        saveNoteBtn.setOnClickListener(v-> saveNote());

    }

    void saveNote(){
        String howAreYou = howAreYouEditText.getText().toString();
        String gratitude = gratitudeEditText.getText().toString();
        String struggles = strugglesEditText.getText().toString();
        String extraNotes = extraNotesEditText.getText().toString();



        boolean isValidated = validateNoteData(howAreYou, gratitude, struggles);
        if(!isValidated){
            return;
        }

        ProgressNote progressNote = new ProgressNote();
        progressNote.setHowAreYou(howAreYou);
        progressNote.setGratitude(gratitude);
        progressNote.setStruggles(struggles);
        progressNote.setExtraNotes(extraNotes);

      if(rateBtn1.isChecked()){
          progressNote.setRate("1");
      }else if (rateBtn2.isChecked()){
          progressNote.setRate("2");
      } else if (rateBtn3.isChecked()) {
          progressNote.setRate("3");
      } else if (rateBtn4.isChecked()) {
          progressNote.setRate("4");
      } else if (rateBtn5.isChecked()) {
          progressNote.setRate("5");
      }

      progressNote.setTimestamp(Timestamp.now());

      saveNoteToFirebase(progressNote);
    }

    boolean validateNoteData(String howAreYou, String gratitude, String struggles) {

        if(howAreYou.length()<=0){
            howAreYouEditText.setError("Pole nie może pozostać puste");
            return false;
        }

        if(gratitude.length()<=0){
            gratitudeEditText.setError("Pole nie może pozostać puste");
            return false;
        }

        if(struggles.length()<=0){
            strugglesEditText.setError("Pole nie może pozostać puste");
            return false;
        }

        if(!(rateBtn1.isChecked()||rateBtn2.isChecked()||rateBtn3.isChecked()||rateBtn4.isChecked()||rateBtn5.isChecked()))
        {
            rateBtn5.setError("Oceń swoje samopoczucie");
            return false;
        }else{
            rateBtn5.setError(null);
        }

        return true;
    }

    void saveNoteToFirebase(ProgressNote progressNote) {

        CollectionReference progressNoteCollection = Utility.getCollectionReferenceForProgressNote(docId);

        DocumentReference documentReference = progressNoteCollection.document();

        ProgressNote newProgressNote = new ProgressNote();
        newProgressNote.setHowAreYou(progressNote.getHowAreYou());
        newProgressNote.setStruggles(progressNote.getStruggles());
        newProgressNote.setGratitude(progressNote.getGratitude());
        newProgressNote.setExtraNotes(progressNote.getExtraNotes());
        newProgressNote.setRate(progressNote.getRate());
        newProgressNote.setTimestamp(progressNote.getTimestamp());

        documentReference.set(newProgressNote).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(NoteDetailsActivity.this, "Notatka dodana pomyślnie", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(NoteDetailsActivity.this, PatientsNotesActivity.class));
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(NoteDetailsActivity.this, "Błąd podczas dodawania notatki", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

