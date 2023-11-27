package com.example.healzone;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.Query;

public class SpecialistList extends AppCompatActivity {

    RecyclerView recyclerView;
    SpecialistAdapter specialistAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_specialist_list);

        checkForSpecialists();

        recyclerView = findViewById(R.id.specialists_recycler_view);
        setUpRecyclerView();
    }

    void setUpRecyclerView(){
        Query query = Utility.getCollectionReferenceForSpecialist();
        FirestoreRecyclerOptions<Specialist> options = new FirestoreRecyclerOptions.Builder<Specialist>()
                .setQuery(query,Specialist.class).build();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        specialistAdapter = new SpecialistAdapter(options,this);
        recyclerView.setAdapter(specialistAdapter);
    }

    private void checkForSpecialists() {
        CollectionReference specialistsCollection = Utility.getCollectionReferenceForSpecialist();
        specialistsCollection.get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (queryDocumentSnapshots.isEmpty()) {
                findViewById(R.id.no_specialists_message).setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                findViewById(R.id.no_specialists_message).setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Błąd podczas sprawdzania specialistów", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        specialistAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        specialistAdapter.stopListening();
    }

    @Override
    protected void onResume() {
        super.onResume();
        specialistAdapter.notifyDataSetChanged();
    }
}