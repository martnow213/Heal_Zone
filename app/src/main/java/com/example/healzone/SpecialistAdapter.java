package com.example.healzone;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

public class SpecialistAdapter extends FirestoreRecyclerAdapter<Specialist, SpecialistAdapter.SpecialistViewHolder> {
    Context context;

    public SpecialistAdapter(@NonNull FirestoreRecyclerOptions<Specialist> options, Context context) {
        super(options);
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull SpecialistViewHolder holder, int position, @NonNull Specialist specialist) {
        holder.specialistNameTextView.setText(specialist.specialistName+" "+specialist.specialistSurname);
        holder.specialistNumberTextView.setText("nr tel.: " + specialist.specialistNumber);
        holder.specialistEmailTextView.setText("email: " + specialist.specialistEmail);
        holder.specialistAddressTextView.setText(specialist.specialistStreet+" "+specialist.specialistBuilding);
        holder.specialistCityTextView.setText(specialist.specialistCity);
    }

    @NonNull
    @Override
    public SpecialistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_specialist_item,parent,false);
        return new SpecialistViewHolder(view);
    }

    class SpecialistViewHolder extends RecyclerView.ViewHolder{

        TextView specialistNameTextView, specialistNumberTextView, specialistEmailTextView, specialistAddressTextView, specialistCityTextView;

        public SpecialistViewHolder(@NonNull View itemView) {
            super(itemView);
            specialistNameTextView = itemView.findViewById(R.id.specialists_name_text_view);
            specialistNumberTextView = itemView.findViewById(R.id.specialists_number_text_view);
            specialistEmailTextView = itemView.findViewById(R.id.specialists_email_text_view);
            specialistAddressTextView = itemView.findViewById(R.id.specialists_address_text_view);
            specialistCityTextView = itemView.findViewById(R.id.specialists_city_text_view);


        }
    }
}