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

public class VisitAdapter extends FirestoreRecyclerAdapter<Visit, VisitAdapter.VisitViewHolder> {
    Context context;

    public VisitAdapter(@NonNull FirestoreRecyclerOptions<Visit> options, Context context) {
        super(options);
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull VisitViewHolder holder, int position, @NonNull Visit visit) {
        holder.visitTimeTextView.setText("Godzina: "+visit.getTime());
        holder.visitPatientTextView.setText("Pacjent: "+visit.getPatient());
        holder.visitDateTextView.setText("Data: "+visit.getDate());

        holder.itemView.setOnClickListener(v->{
            Intent intent = new Intent(context,VisitFullView.class);
            intent.putExtra("patient",visit.patient);
            intent.putExtra("date",visit.date);
            intent.putExtra("time",visit.time);
            intent.putExtra("docId",visit.docId);
            Log.d("DEBUG", "docId received: " + visit.docId);
            String thisDocId = this.getSnapshots().getSnapshot(position).getId();
            intent.putExtra("thisDocId",thisDocId);
            context.startActivity(intent);
        });
    }

    @NonNull
    @Override
    public VisitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_visits_item,parent,false);
        return new VisitViewHolder(view);
    }

    class VisitViewHolder extends RecyclerView.ViewHolder{

        TextView visitDateTextView, visitTimeTextView, visitPatientTextView;

        public VisitViewHolder(@NonNull View itemView) {
            super(itemView);
            visitDateTextView = itemView.findViewById(R.id.date_text_view);
            visitTimeTextView = itemView.findViewById(R.id.time_text_view);
            visitPatientTextView = itemView.findViewById(R.id.name_text_view);
        }
    }
}