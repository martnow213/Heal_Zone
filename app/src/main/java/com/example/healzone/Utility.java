package com.example.healzone;

import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Utility {

    //SKRÓTY DO KOLEKCJI Z BAZY DANYCH FIREBASE FIRESTORE:
    public static CollectionReference getCollectionReferenceForVisit(String docId){
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        return FirebaseFirestore.getInstance().collection("visits").document(currentUser.getUid()).collection("my_patients").document(docId).collection("my_visits");

    }

    public static CollectionReference getCollectionReferenceForAllVisits(){
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        return FirebaseFirestore.getInstance().collection("all_visits").document(currentUser.getUid()).collection("visits");

    }

    public static CollectionReference getCollectionReferenceForPatientAllVisits(String docId){
        return FirebaseFirestore.getInstance().collection("patient_all_visits").document(docId).collection("visits");

    }

    public static CollectionReference getCollectionReferenceForProgressNote(String docId){
        return FirebaseFirestore.getInstance().collection("progressNote").document(docId).collection("my_notes");

    }

    public static CollectionReference getCollectionReferenceForPatientsData(){
        return FirebaseFirestore.getInstance().collection("patientsData");

    }

    static CollectionReference getCollectionReferenceForSpecialist(){
        return FirebaseFirestore.getInstance().collection("specialists");

    }
    public static CollectionReference getCollectionReferenceForPatient(){
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        return FirebaseFirestore.getInstance().collection("patients").document(currentUser.getUid()).collection("my_patients");

    }


    public static String timeStampToString(Timestamp timestamp){
        return new SimpleDateFormat("dd/MM/yyyy").format(timestamp.toDate());
    }

    //SPRAWDZANIE UNIKALNOŚCI WŚRÓD PACJENTÓW:
    public static Task<QuerySnapshot> checkIfEmailExists(String email) {
        // Odczytanie kolekcji pacjentów i sprawdzenie, czy email już istnieje
        return getCollectionReferenceForPatient()
                .whereEqualTo("patientEmail", email)
                .get();
    }

    public static Task<QuerySnapshot> checkIfNumberExists(String number) {
        // Odczytanie kolekcji pacjentów i sprawdzenie, czy numer już istnieje
        return getCollectionReferenceForPatient()
                .whereEqualTo("patientNumber", number)
                .get();
    }

    public static Task<QuerySnapshot> checkIfPeselExists(String pesel) {
        // Odczytanie kolekcji pacjentów i sprawdzenie, czy pesel już istnieje
        return getCollectionReferenceForPatient()
                .whereEqualTo("patientPesel", pesel)
                .get();
    }

    //Metoda w celu uniknięcia wygenerowania hasła, które już istnieje:
    public static Task<QuerySnapshot> checkIfPasswordExists(String password) {
        return getCollectionReferenceForPatientsData()
                .whereEqualTo("patientPassword", password)
                .get();
    }

    //Generowanie randomowego hasła:
    public static String generateRandomPassword(int minLength) {
        SecureRandom random = new SecureRandom();
        String upperCaseChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String digits = "0123456789";
        String allChars = "abcdefghijklmnopqrstuvwxyz" + upperCaseChars + digits;

        if (minLength < 8) {
            minLength = 8;
        }

        String password;

        do {
            password = upperCaseChars.charAt(random.nextInt(upperCaseChars.length()))
                    + digits.charAt(random.nextInt(digits.length())) + "";

            for (int i = 0; i < minLength - 2; i++) {
                int randomIndex = random.nextInt(allChars.length());
                password += allChars.charAt(randomIndex);
            }

            password = shuffleString(password);
        } while (checkIfPasswordExists(password).isSuccessful()); // Sprawdzaj, czy hasło już istnieje

        return password;
    }

    public static String shuffleString(String input) {
        char[] characters = input.toCharArray();
        for (int i = 0; i < characters.length; i++) {
            int randomIndex = (int) (Math.random() * characters.length);
            char temp = characters[i];
            characters[i] = characters[randomIndex];
            characters[randomIndex] = temp;
        }
        return new String(characters);
    }

    //Metoda do sprawdzania czy hasło ma dużą literę
    public static boolean hasUpperCaseLetter(String password) {
        for (int i = 0; i < password.length(); i++) {
            char c = password.charAt(i);
            if (Character.isUpperCase(c)) {
                return true; // Znaleziono dużą literę
            }
        }
        return false; // Nie znaleziono dużej litery
    }

    //Metoda sprawdzająca czy hasło wpisane przez użytkownika nie jest zajęte
    public static Task<QuerySnapshot> checkIfNewPasswordExists(String password) {
        return getCollectionReferenceForPatientsData()
                .whereEqualTo("patientPassword", password)
                .get();
    }

    //Wykorzystywane przy założeniu nowego konta specjalisty, pózniej update w innej metodzie:
    public static void generateAvailableHours(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        CollectionReference availableHoursCollection = db.collection("availableHours").document(currentUser.getUid()).collection("my_hours");

        Calendar currentDate = Calendar.getInstance();
        for (int i = 0; i < 90; i++) {
            currentDate.add(Calendar.DAY_OF_MONTH, 1);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String dateKey = sdf.format(currentDate.getTime());

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




    }
}
