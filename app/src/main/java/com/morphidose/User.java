package com.morphidose;

import java.io.Serializable;

public class User implements Serializable{
    private static final long serialVersionUID = 1L;
    private String hospitalNumber;
    private Prescription prescription;

    public User(String hospitalNumber, Prescription prescription){
        this.hospitalNumber = hospitalNumber;
        this.prescription = prescription;
    }

    public String getHospitalNumber() {
        return hospitalNumber;
    }

    public Prescription getPrescription() {
        return prescription;
    }

    public void setPrescription(Prescription prescription) {
        this.prescription = prescription;
    }

    public void setHospitalNumber(String hospitalNumber) {
        this.hospitalNumber = hospitalNumber;
    }
}
