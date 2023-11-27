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

public class PatientAdapter extends FirestoreRecyclerAdapter<Patient, PatientAdapter.PatientViewHolder> {
    Context context;

    public PatientAdapter(@NonNull FirestoreRecyclerOptions<Patient> options, Context context) {
        super(options);
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull PatientViewHolder holder, int position, @NonNull Patient patient) {
        holder.patientsNameTextView.setText(patient.patientName+" "+patient.patientSurname);
        holder.patientsBirthdateTextView.setText("ur. "+patient.patientBirthdate);
        holder.timestampTextView.setText(Utility.timeStampToString(patient.timestamp));

        holder.itemView.setOnClickListener(v->{
            Intent intent = new Intent(context,PatientFullView.class);
            intent.putExtra("imie",patient.patientName);
            intent.putExtra("nazwisko",patient.patientSurname);
            intent.putExtra("pesel",patient.patientPesel);
            intent.putExtra("data_ur",patient.patientBirthdate);
            intent.putExtra("plec",patient.patientGender);
            intent.putExtra("diagnoza",patient.patientDiagnosis);
            intent.putExtra("notatki",patient.patientNotes);
            intent.putExtra("email",patient.patientEmail);
            intent.putExtra("numer",patient.patientNumber);
            String docId = this.getSnapshots().getSnapshot(position).getId();
            intent.putExtra("docId",docId);
            Log.d("DEBUG", "docId received: " + docId);
            context.startActivity(intent);
        });
    }

    @NonNull
    @Override
    public PatientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_patient_item,parent,false);
        return new PatientViewHolder(view);
    }

    class PatientViewHolder extends RecyclerView.ViewHolder{

        TextView patientsNameTextView, patientsBirthdateTextView, timestampTextView;

        public PatientViewHolder(@NonNull View itemView) {
            super(itemView);
            patientsNameTextView = itemView.findViewById(R.id.patients_name_text_view);
            patientsBirthdateTextView = itemView.findViewById(R.id.patients_birthday_text_view);
            timestampTextView = itemView.findViewById(R.id.timestamp_text_view);
        }
    }
}