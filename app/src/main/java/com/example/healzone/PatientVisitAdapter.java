package com.example.healzone;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

public class PatientVisitAdapter extends FirestoreRecyclerAdapter<PatientVisit, PatientVisitAdapter.PatientVisitViewHolder> {
    Context context;

    public PatientVisitAdapter(@NonNull FirestoreRecyclerOptions<PatientVisit> options, Context context) {
        super(options);
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull PatientVisitViewHolder holder, int position, @NonNull PatientVisit patientVisit) {
        holder.patientVisitTimeTextView.setText("Godzina: "+patientVisit.getCzas());
        holder.patientVisitDateTextView.setText("Data: "+patientVisit.getData());

    }

    @NonNull
    @Override
    public PatientVisitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_patient_visit_item,parent,false);
        return new PatientVisitViewHolder(view);
    }

    class PatientVisitViewHolder extends RecyclerView.ViewHolder{

        TextView patientVisitDateTextView, patientVisitTimeTextView;

        public PatientVisitViewHolder(@NonNull View itemView) {
            super(itemView);
            patientVisitDateTextView = itemView.findViewById(R.id.date_text_view);
            patientVisitTimeTextView = itemView.findViewById(R.id.time_text_view);
        }
    }
}