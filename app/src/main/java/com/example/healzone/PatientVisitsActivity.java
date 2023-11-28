package com.example.healzone;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PatientVisitsActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    PatientVisitAdapter patientVisitAdapter;
    String docId, selectedPatient;
    TextView pageTitle;
    FloatingActionButton floatingActionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_visits);


        floatingActionButton = findViewById(R.id.add_visit_btn);

        updateAvailableHours();
        docId = getIntent().getStringExtra("docId");
        DocumentReference dr = Utility.getCollectionReferenceForPatient().document(docId);

        dr.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    String patientName = document.getString("patientName");
                    String patientSurname = document.getString("patientSurname");
                    String patientBirthdate = document.getString("patientBirthdate");
                    Log.d("debug", "Patients data: "+patientName+patientSurname+patientBirthdate);
                    selectedPatient = patientName + " " + patientSurname + ", ur.: " + patientBirthdate;
                    Log.d("debug", "Selected patient: "+selectedPatient);
                } else {
                    Toast.makeText(PatientVisitsActivity.this, "Dokument nie istnieje", Toast.LENGTH_SHORT).show();
                }
            } else {
                Exception e = task.getException();
                if (e != null) {
                    e.printStackTrace();
                }
                Toast.makeText(PatientVisitsActivity.this, "Błąd podczas pobierania listy pacjentów", Toast.LENGTH_SHORT).show();
            }
        });

        floatingActionButton.setOnClickListener(v->{
            showDatePickerDialog(selectedPatient);
        });


        recyclerView = findViewById(R.id.patient_visits_recycler_view);


        deleteOldVisits();
        checkForVisits();
        setUpRecyclerView();


    }


    ///
    private void showDatePickerDialog(String selectedPatient) {
        final Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, 1);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    if (isValidDate(year, month, dayOfMonth)) {

                        showHourSelectionDialog(year, month, dayOfMonth, selectedPatient);

                    } else {
                        Toast.makeText(PatientVisitsActivity.this, "Nie można wybrać tej daty", Toast.LENGTH_SHORT).show();
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));

        // Minimalna data jako jutro
        datePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());

        // Max data za 3 miesiące (90 dni - 1 dzień, bo zaczynamy od "jutra" stąd 89)
        calendar.add(Calendar.DAY_OF_MONTH, 89);
        datePickerDialog.getDatePicker().setMaxDate(calendar.getTimeInMillis());

        // Ustawienie weekendu
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            datePickerDialog.getDatePicker().setOnDateChangedListener((view, year, monthOfYear, dayOfMonth) -> {
                Calendar selectedDate = Calendar.getInstance();
                selectedDate.set(year, monthOfYear, dayOfMonth);
                int dayOfWeek = selectedDate.get(Calendar.DAY_OF_WEEK);

                if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) {
                    Toast.makeText(PatientVisitsActivity.this, "Nie można wybrać weekendu", Toast.LENGTH_SHORT).show();
                }
            });
        }

        datePickerDialog.show();
    }

    //Walidacja wybranej daty
    private boolean isValidDate(int year, int month, int day) {
        Calendar selectedDate = Calendar.getInstance();
        selectedDate.set(year, month, day);

        return !selectedDate.before(Calendar.getInstance()) &&
                selectedDate.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY &&
                selectedDate.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY;
    }

    //WYŚWIETLENIE GODZIN
    private void showHourSelectionDialog(final int year, final int month, final int dayOfMonth, String selectedPatient) {
        Calendar selectedDate = Calendar.getInstance();
        selectedDate.set(year, month, dayOfMonth);
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        generateAvailableHours(currentUser.getUid(), selectedDate, new PatientVisitsActivity.OnGenerateAvailableHoursListener() {
            @Override
            public void onHoursGenerated(String[] availableHours) {
                AlertDialog.Builder builder = new AlertDialog.Builder(PatientVisitsActivity.this);
                builder.setTitle("Wybierz godzinę")
                        .setItems(availableHours, (dialog, which) -> {
                            String selectedHour = availableHours[which];
                            handleHourSelected(year, month, dayOfMonth, selectedHour, docId, selectedPatient);
                        })
                        .setNegativeButton("Anuluj", null);

                builder.show();
            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
            }
        });
    }


    //ZAKTUALIZOWANIE GODZIN DOSTĘPNYCH WIZYT NA NOWE DNI
    public static void updateAvailableHours() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        CollectionReference availableHoursCollection = db.collection("availableHours").document(currentUser.getUid()).collection("my_hours");

        availableHoursCollection.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> documentIds = new ArrayList<>();

                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        documentIds.add(documentSnapshot.getId());
                    }

                    Collections.sort(documentIds);

                    Calendar today = Calendar.getInstance();
                    today.set(Calendar.HOUR_OF_DAY, 0);
                    today.set(Calendar.MINUTE, 0);
                    today.set(Calendar.SECOND, 0);
                    today.set(Calendar.MILLISECOND, 0);
                    today.add(Calendar.DAY_OF_MONTH,1);

                    int documentsRemoved = 0;

                    for (String documentId : documentIds) {
                        try {
                            Calendar documentDate = Calendar.getInstance();
                            documentDate.setTime(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(documentId));

                            if (documentDate.before(today)) {
                                availableHoursCollection.document(documentId).delete();
                                documentsRemoved++;
                            } else {
                                break;
                            }
                        } catch (ParseException e) {
                            Log.e("UpdateAvailableHours", "Error parsing date from document ID: " + documentId, e);
                        }
                    }

                    Log.d("UpdateAvailableHours", "Documents removed: " + documentsRemoved);

                    if (!documentIds.isEmpty()) {
                        String lastDateKey = documentIds.get(documentIds.size() - 1);
                        Log.d("UpdateAvailableHours","LastDate: "+lastDateKey);

                        try {
                            Calendar futureDate = Calendar.getInstance();
                            futureDate.setTime(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(lastDateKey));
                            futureDate.add(Calendar.DAY_OF_MONTH, documentsRemoved);

                            for (int i = 0; i < documentsRemoved; i++) {
                                futureDate.add(Calendar.DAY_OF_MONTH, 0);

                                String dateKey = formatDate(futureDate.getTime());

                                Map<String, Object> hoursMap = new HashMap<>();
                                hoursMap.put("10:00 - 10:45", true);
                                hoursMap.put("11:00 - 11:45", true);
                                hoursMap.put("12:00 - 12:45", true);
                                hoursMap.put("13:00 - 13:45", true);
                                hoursMap.put("14:00 - 14:45", true);
                                hoursMap.put("15:00 - 15:45", true);
                                hoursMap.put("16:00 - 16:45", true);
                                hoursMap.put("17:00 - 17:45", true);

                                availableHoursCollection.document(dateKey).set(hoursMap);
                            }
                        } catch (ParseException e) {
                            Log.e("UpdateAvailableHours", "Error parsing date from document ID (lastDateKey): " + lastDateKey, e);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("UpdateAvailableHours", "Error checking max date", e);
                });
    }

    //Formatowanie daty
    private static String formatDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(date);
    }


    //Generowanie dostępnych godzin
    public interface OnGenerateAvailableHoursListener {
        void onHoursGenerated(String[] result);
        void onError(Exception e);
    }
    private void generateAvailableHours(String specialistId, Calendar date, PatientVisitsActivity.OnGenerateAvailableHoursListener listener) {
        String dateString = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date.getTime());
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference selectedDayHours = db.collection("availableHours")
                .document(specialistId)
                .collection("my_hours")
                .document(dateString);

        selectedDayHours.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    Map<String, Object> data = document.getData();
                    if (data != null) {

                        List<String> timeIntervals = new ArrayList<>();
                        for (Map.Entry<String, Object> entry : data.entrySet()) {
                            if (entry.getValue() instanceof Boolean && (Boolean) entry.getValue()) {
                                timeIntervals.add(entry.getKey());
                            }
                        }

                        Collections.sort(timeIntervals, new PatientVisitsActivity.TimeComparator());

                        listener.onHoursGenerated(timeIntervals.toArray(new String[0]));
                    }
                }
            } else {
                Exception exception = task.getException();
                if (exception != null) {
                    exception.printStackTrace();
                    listener.onError(exception);
                }
            }
        });
    }

    //Komparator w celu poprawnego (rosnącego) sortowania wyświetlanych czasów
    private static class TimeComparator implements Comparator<String> {
        @Override
        public int compare(String time1, String time2) {
            try {
                SimpleDateFormat format = new SimpleDateFormat("HH:mm", Locale.getDefault());

                Date date1Start = format.parse(time1.split(" - ")[0]);
                Date date2Start = format.parse(time2.split(" - ")[0]);

                return date1Start.compareTo(date2Start);
            } catch (ParseException e) {
                e.printStackTrace();
                return 0;
            }
        }
    }

    private void handleHourSelected(int year, int month, int dayOfMonth, String selectedHour, String selectedPatientDocId, String selectedPatient) {
        String formattedDate = String.format(Locale.getDefault(), "%d-%02d-%02d",
                year, month + 1, dayOfMonth);
        updateHourState(selectedHour,formattedDate);


        Toast.makeText(this, "Wybrana data: " + formattedDate + "\nWybrana godzina: " + selectedHour + "\nWybrany pacjent: " + selectedPatient, Toast.LENGTH_SHORT).show();

        String patientsDocId = selectedPatientDocId;

        //Dodawanie wizyty do firebase
        //1.
        CollectionReference visitCollection = Utility.getCollectionReferenceForVisit(patientsDocId);

        DocumentReference documentReference = visitCollection.document();

        Visit visit = new Visit();
        visit.setPatient(selectedPatient);
        visit.setTime(selectedHour);
        visit.setDate(formattedDate);
        visit.setDocId(patientsDocId);

        String createdId = documentReference.getId();

        documentReference.set(visit).addOnSuccessListener(aVoid -> {
            Toast.makeText(PatientVisitsActivity.this, "Wizyta dodana pomyślnie", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> Toast.makeText(PatientVisitsActivity.this, "Błąd podczas dodawania wizyty", Toast.LENGTH_SHORT).show());

        //2.
        CollectionReference visitCollection1 = Utility.getCollectionReferenceForAllVisits();

        DocumentReference documentReference1 = visitCollection1.document(createdId);

        Visit visit1 = new Visit();
        visit1.setPatient(selectedPatient);
        visit1.setTime(selectedHour);
        visit1.setDate(formattedDate);
        visit1.setDocId(patientsDocId);

        documentReference1.set(visit1).addOnSuccessListener(aVoid -> {
            Log.e("Visit", "Visit added");
        }).addOnFailureListener(e -> Toast.makeText(PatientVisitsActivity.this, "Błąd podczas dodawania wizyty", Toast.LENGTH_SHORT).show());

        //3.

        CollectionReference patientVisitCollection = Utility.getCollectionReferenceForPatientAllVisits(patientsDocId);

        DocumentReference documentReference2 = patientVisitCollection.document(createdId);

        PatientVisit patientVisit = new PatientVisit();
        patientVisit.setCzas(selectedHour);
        patientVisit.setData(formattedDate);
        patientVisit.setDocId(patientsDocId);

        documentReference2.set(patientVisit).addOnSuccessListener(aVoid -> {
            Log.e("Visit", "Patient visit added");
            checkForVisits();

        }).addOnFailureListener(e -> Toast.makeText(PatientVisitsActivity.this, "Błąd podczas dodawania wizyty", Toast.LENGTH_SHORT).show());
    }


    //Aktualizacja wybranej godziny tak, aby nie dało się jej więcej wybrać
    private void updateHourState(String selectedHour, String selectedDate) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String specialistId = currentUser.getUid();

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference selectedDayHours = db.collection("availableHours").document(specialistId).collection("my_hours").document(selectedDate);

            Map<String, Object> updateMap = new HashMap<>();
            updateMap.put(selectedHour, false);

            selectedDayHours.update(updateMap)
                    .addOnSuccessListener(aVoid -> Log.e("UpdateHourState", "Hour updated"))
                    .addOnFailureListener(e -> Toast.makeText(PatientVisitsActivity.this, "Błąd podczas aktualizacji godziny", Toast.LENGTH_SHORT).show());
        }
    }

    ///

    //Usuwanie wizyt, które są z przeszłości
    private void deleteOldVisits() {
        CollectionReference collectionReference = Utility.getCollectionReferenceForPatientAllVisits(docId);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String currentDate = dateFormat.format(new Date());

        Query oldVisitsQuery = collectionReference.whereLessThan("data", currentDate);

        oldVisitsQuery.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    document.getReference().delete();
                }
            }
        });
    }


    //USTAWIENIA WIDOKU
    //1. Sprawdza czy są wizyty do wyświetlenia
    private void checkForVisits() {
        CollectionReference allVisitsCollection = Utility.getCollectionReferenceForPatientAllVisits(docId);
        allVisitsCollection.get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (queryDocumentSnapshots.isEmpty()) {
                findViewById(R.id.no_visits_msg).setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                findViewById(R.id.no_visits_msg).setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Błąd podczas sprawdzania wizyt", Toast.LENGTH_SHORT).show();
        });
    }

    void setUpRecyclerView(){

        Query query = Utility.getCollectionReferenceForPatientAllVisits(docId).orderBy("data",Query.Direction.ASCENDING).orderBy("czas",Query.Direction.ASCENDING);
        FirestoreRecyclerOptions<PatientVisit> options = new FirestoreRecyclerOptions.Builder<PatientVisit>()
                .setQuery(query,PatientVisit.class).build();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        patientVisitAdapter = new PatientVisitAdapter(options,this);
        recyclerView.setAdapter(patientVisitAdapter);

    }

    @Override
    protected void onStart() {
        super.onStart();
        patientVisitAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        patientVisitAdapter.stopListening();
    }

    @Override
    protected void onResume() {
        super.onResume();
        patientVisitAdapter.notifyDataSetChanged();
    }
}

