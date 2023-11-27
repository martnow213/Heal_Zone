package com.example.healzone;

import com.google.firebase.Timestamp;

import java.sql.Time;

public class Patient {
    String patientName;
    String patientSurname;
    String patientPesel;
    String patientBirthdate;
    String patientDiagnosis;
    String patientNotes;
    String patientGender;

    String patientEmail;

    String patientNumber;

    String patientGeneratedPassword;


    com.google.firebase.Timestamp timestamp;


    public Patient() {
    }

    public String getPatientEmail() {
        return patientEmail;
    }

    public String getPatientGeneratedPassword() {
        return patientGeneratedPassword;
    }

    public void setPatientGeneratedPassword(String patientGeneratedPassword) {
        this.patientGeneratedPassword = patientGeneratedPassword;
    }

    public void setPatientEmail(String patientEmail) {
        this.patientEmail = patientEmail;
    }

    public String getPatientNumber() {
        return patientNumber;
    }

    public void setPatientNumber(String patientNumber) {
        this.patientNumber = patientNumber;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getPatientSurname() {
        return patientSurname;
    }

    public void setPatientSurname(String patientSurname) {
        this.patientSurname = patientSurname;
    }

    public String getPatientPesel() {
        return patientPesel;
    }

    public void setPatientPesel(String patientPesel) {
        this.patientPesel = patientPesel;
    }

    public String getPatientBirthdate() {
        return patientBirthdate;
    }

    public void setPatientBirthdate(String patientBirthdate) {
        this.patientBirthdate = patientBirthdate;
    }

    public String getPatientDiagnosis() {
        return patientDiagnosis;
    }

    public void setPatientDiagnosis(String patientDiagnosis) {
        this.patientDiagnosis = patientDiagnosis;
    }

    public String getPatientNotes() {
        return patientNotes;
    }

    public void setPatientNotes(String patientNotes) {
        this.patientNotes = patientNotes;
    }

    public String getPatientGender() {
        return patientGender;
    }

    public void setPatientGender(String patientGender) {
        this.patientGender = patientGender;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}




