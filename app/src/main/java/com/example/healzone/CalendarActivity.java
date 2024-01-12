package com.example.healzone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

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
import java.util.concurrent.TimeUnit;

public class CalendarActivity extends AppCompatActivity {

    FloatingActionButton dodajWizyteButton;
    RecyclerView recyclerView;
    VisitAdapter visitAdapter;
    ImageButton refreshBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        System.out.println("Fetching FCM registration token failed");
                        return;
                    }

                    String token = task.getResult();

                    System.out.println(token);
                });

        checkForVisits();
        deleteOldVisits();
        updateAvailableHours();


        recyclerView = findViewById(R.id.visits_recycler_view);
        setUpRecyclerView();

        dodajWizyteButton = findViewById(R.id.add_visit_btn);

        dodajWizyteButton.setOnClickListener(v -> showDatePickerDialog());

        refreshBtn = findViewById(R.id.refresh_btn);
        refreshBtn.setOnClickListener(v->{
            Intent intent = new Intent(CalendarActivity.this,CalendarActivity.class);
            startActivity(intent);
        });
    }

    //WYŚWIETLENIE DATE PICKER - wybór daty
    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, 1);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    if (isValidDate(year, month, dayOfMonth)) {

                        showHourSelectionDialog(year, month, dayOfMonth);

                    } else {
                        Toast.makeText(CalendarActivity.this, "Nie można wybrać tej daty", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(CalendarActivity.this, "Nie można wybrać weekendu", Toast.LENGTH_SHORT).show();
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
    private void showHourSelectionDialog(final int year, final int month, final int dayOfMonth) {
        Calendar selectedDate = Calendar.getInstance();
        selectedDate.set(year, month, dayOfMonth);
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        generateAvailableHours(currentUser.getUid(), selectedDate, new OnGenerateAvailableHoursListener() {
            @Override
            public void onHoursGenerated(String[] availableHours) {
                AlertDialog.Builder builder = new AlertDialog.Builder(CalendarActivity.this);
                builder.setTitle("Wybierz godzinę")
                        .setItems(availableHours, (dialog, which) -> {
                            String selectedHour = availableHours[which];
                            handleHourSelected(year, month, dayOfMonth, selectedHour);
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
    private void generateAvailableHours(String specialistId, Calendar date, OnGenerateAvailableHoursListener listener) {
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

                        Collections.sort(timeIntervals, new TimeComparator());

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

    //Pobranie listy pacjentów
    private void handleHourSelected(final int year, final int month, final int dayOfMonth, final String selectedHour) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        db.collection("patients").document(currentUser.getUid()).collection("my_patients")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> patientNames = new ArrayList<>();
                        List<String> patientDocIds = new ArrayList<>();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String patientName = document.getString("patientName");
                            String patientSurname = document.getString("patientSurname");
                            String patientBirthdate = document.getString("patientBirthdate");
                            String patientDocId = document.getId();
                            patientNames.add(patientName + " " + patientSurname + ", ur.: " + patientBirthdate);
                            patientDocIds.add(patientDocId);
                        }

                        String[] patientsArray = patientNames.toArray(new String[0]);

                        showPatientSelectionDialog(year, month, dayOfMonth, selectedHour, patientsArray, patientDocIds);
                    } else {
                        Toast.makeText(CalendarActivity.this, "Błąd podczas pobierania listy pacjentów", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showPatientSelectionDialog(final int year, final int month, final int dayOfMonth, final String selectedHour, final String[] patientsArray, final List<String> patientDocIds) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Wybierz pacjenta")
                .setItems(patientsArray, (dialog, which) -> {
                    String selectedPatient = patientsArray[which];
                    String selectedPatientDocId = patientDocIds.get(which); // Pobierz ID dokumentu wybranego pacjenta
                    handlePatientSelected(year, month, dayOfMonth, selectedHour, selectedPatient, selectedPatientDocId);
                })
                .setNegativeButton("Anuluj", null);

        builder.show();
    }

    private void handlePatientSelected(int year, int month, int dayOfMonth, String selectedHour, String selectedPatient, String selectedPatientDocId) {
        String formattedDate = String.format(Locale.getDefault(), "%d-%02d-%02d",
                year, month + 1, dayOfMonth);
        updateHourState(selectedHour,formattedDate);

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
            Log.e("HandlePatientSelected", "Patient Selected");
            checkForVisits();
        }).addOnFailureListener(e -> Toast.makeText(CalendarActivity.this, "Błąd podczas dodawania wizyty", Toast.LENGTH_SHORT).show());

        //2.
        CollectionReference visitCollection1 = Utility.getCollectionReferenceForAllVisits();

        DocumentReference documentReference1 = visitCollection1.document(createdId);

        Visit visit1 = new Visit();
        visit1.setPatient(selectedPatient);
        visit1.setTime(selectedHour);
        visit1.setDate(formattedDate);
        visit1.setDocId(patientsDocId);

        documentReference1.set(visit1).addOnSuccessListener(aVoid -> {
            checkForVisits();
        }).addOnFailureListener(e -> Toast.makeText(CalendarActivity.this, "Błąd podczas dodawania wizyty", Toast.LENGTH_SHORT).show());

        //3.

        CollectionReference patientVisitCollection = Utility.getCollectionReferenceForPatientAllVisits(patientsDocId);

        DocumentReference documentReference2 = patientVisitCollection.document(createdId);

        PatientVisit patientVisit = new PatientVisit();
        patientVisit.setCzas(selectedHour);
        patientVisit.setData(formattedDate);
        patientVisit.setDocId(patientsDocId);

        documentReference2.set(patientVisit).addOnSuccessListener(aVoid -> {
            Toast.makeText(CalendarActivity.this, "Wizyta dodana pomyślnie", Toast.LENGTH_SHORT).show();
            checkForVisits();
        }).addOnFailureListener(e -> Toast.makeText(CalendarActivity.this, "Błąd podczas dodawania wizyty", Toast.LENGTH_SHORT).show());

        finish();

        // Uruchom aktywność ponownie
        Intent intent = new Intent(CalendarActivity.this, CalendarActivity.class);
        startActivity(intent);

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
                    .addOnFailureListener(e -> Toast.makeText(CalendarActivity.this, "Błąd podczas aktualizacji godziny", Toast.LENGTH_SHORT).show());
        }
    }

    //Usuwanie wizyt, które są z przeszłości
    private void deleteOldVisits() {
        CollectionReference visitsCollection = Utility.getCollectionReferenceForAllVisits();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String currentDate = dateFormat.format(new Date());

        Query oldVisitsQuery = visitsCollection.whereLessThanOrEqualTo("date", currentDate);

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
        CollectionReference allVisitsCollection = Utility.getCollectionReferenceForAllVisits();
        allVisitsCollection.get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (queryDocumentSnapshots.isEmpty()) {
                findViewById(R.id.no_visits_message).setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                findViewById(R.id.no_visits_message).setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Błąd podczas sprawdzania wizyt", Toast.LENGTH_SHORT).show();
        });
    }

    //2. Ustawienie recycler view
    void setUpRecyclerView(){

        Query query = Utility.getCollectionReferenceForAllVisits().orderBy("date",Query.Direction.ASCENDING).orderBy("time",Query.Direction.ASCENDING);
        FirestoreRecyclerOptions<Visit> options = new FirestoreRecyclerOptions.Builder<Visit>()
                .setQuery(query,Visit.class).build();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        visitAdapter = new VisitAdapter(options,this);
        recyclerView.setAdapter(visitAdapter);

    }

    @Override
    protected void onStart() {
        super.onStart();
        visitAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        visitAdapter.stopListening();
    }

    @Override
    protected void onResume() {
        super.onResume();
        visitAdapter.notifyDataSetChanged();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(CalendarActivity.this, PatientsList.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

}