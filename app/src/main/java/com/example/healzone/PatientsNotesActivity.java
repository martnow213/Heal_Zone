package com.example.healzone;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.Query;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PatientsNotesActivity extends AppCompatActivity {

    FloatingActionButton addNoteBtn;
    RecyclerView recyclerView;
    ProgressNoteAdapter progressNoteAdapter;

    TextView pageTitle;

    String docId, howAreYou, gratitude, struggles, extraNotes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patients_notes);

        docId=getIntent().getStringExtra("docId");
        howAreYou = getIntent().getStringExtra("howAreYou");
        gratitude = getIntent().getStringExtra("gratitude");
        struggles = getIntent().getStringExtra("struggles");
        extraNotes = getIntent().getStringExtra("extraNotes");

        recyclerView = findViewById(R.id.patient_notes_recycler_view);
        setUpRecyclerView();

        pageTitle = findViewById(R.id.page_title);
        addNoteBtn = findViewById(R.id.add_progress_btn);
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null){
            addNoteBtn.setOnClickListener(v->{
                    Intent intent = new Intent(PatientsNotesActivity.this,NoteDetailsActivity.class);
                    intent.putExtra("docId",docId);
                    startActivity(intent);
            });
        }else{
            pageTitle.setText("Postępy pacjenta:");
            addNoteBtn.setVisibility(View.GONE);
        }


    }

    void setUpRecyclerView(){
        Query query = Utility.getCollectionReferenceForProgressNote(docId).orderBy("timestamp",Query.Direction.DESCENDING);
        FirestoreRecyclerOptions<ProgressNote> options = new FirestoreRecyclerOptions.Builder<ProgressNote>()
                .setQuery(query,ProgressNote.class).build();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        progressNoteAdapter = new ProgressNoteAdapter(options,this);
        recyclerView.setAdapter(progressNoteAdapter);
    }


    @Override
    protected void onStart() {
        super.onStart();
        progressNoteAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        progressNoteAdapter.stopListening();
    }

    @Override
    protected void onResume() {
        super.onResume();
        progressNoteAdapter.notifyDataSetChanged();
    }
}