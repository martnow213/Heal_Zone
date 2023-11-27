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

public class ProgressNoteAdapter extends FirestoreRecyclerAdapter<ProgressNote, ProgressNoteAdapter.ProgressNoteViewHolder> {
    Context context;

    public ProgressNoteAdapter(@NonNull FirestoreRecyclerOptions<ProgressNote> options, Context context) {
        super(options);
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull ProgressNoteViewHolder holder, int position, @NonNull ProgressNote progressNote) {
        holder.timestampTextView.setText(Utility.timeStampToString(progressNote.timestamp));
        holder.rateTextView.setText(progressNote.rate);

        holder.itemView.setOnClickListener(v->{
            Intent intent = new Intent(context,NoteFullView.class);
            intent.putExtra("timestamp",progressNote.timestamp);
            intent.putExtra("howAreYou",progressNote.howAreYou);
            intent.putExtra("struggles",progressNote.struggles);
            intent.putExtra("gratitude",progressNote.gratitude);
            intent.putExtra("extraNotes",progressNote.extraNotes);
            String docId = this.getSnapshots().getSnapshot(position).getId();
            intent.putExtra("docId",docId);
            Log.d("DEBUG", "docId received: " + docId);
            context.startActivity(intent);
        });
    }

    @NonNull
    @Override
    public ProgressNoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_progress_item,parent,false);
        return new ProgressNoteViewHolder(view);
    }

    class ProgressNoteViewHolder extends RecyclerView.ViewHolder{

        TextView rateTextView, timestampTextView;

        public ProgressNoteViewHolder(@NonNull View itemView) {
            super(itemView);
            timestampTextView = itemView.findViewById(R.id.timestamp_text_view);
            rateTextView = itemView.findViewById(R.id.rate_text_view);
        }
    }
}