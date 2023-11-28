package com.example.healzone;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.Query;

public class ThisPatientVisits extends AppCompatActivity {

    RecyclerView recyclerView;
    ThisPatientVisitAdapter thisPatientVisitAdapter;
    String docId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_this_patient_visits);

        docId = getIntent().getStringExtra("docId");

        recyclerView = findViewById(R.id.patient_visits_recycler_view);

        setUpRecyclerView();
    }

    void setUpRecyclerView(){

        Query query = Utility.getCollectionReferenceForPatientAllVisits(docId).orderBy("data",Query.Direction.ASCENDING).orderBy("czas",Query.Direction.ASCENDING);
        FirestoreRecyclerOptions<PatientVisit> options = new FirestoreRecyclerOptions.Builder<PatientVisit>()
                .setQuery(query,PatientVisit.class).build();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        thisPatientVisitAdapter = new ThisPatientVisitAdapter(options,this);
        recyclerView.setAdapter(thisPatientVisitAdapter);

    }
    @Override
    protected void onStart() {
        super.onStart();
        thisPatientVisitAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        thisPatientVisitAdapter.stopListening();
    }

    @Override
    protected void onResume() {
        super.onResume();
        thisPatientVisitAdapter.notifyDataSetChanged();
    }
}