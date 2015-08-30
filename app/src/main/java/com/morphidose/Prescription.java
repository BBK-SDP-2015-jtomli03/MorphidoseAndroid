package com.morphidose;

/**
 * Created by Jo on 30/08/2015.
 */
public class Prescription {
    private String prescriber;
    private String date;
    private String MRDrug;
    private String MRDose;
    private String breakthroughDrug;
    private String breakthroughDose;


    public String getPrescriber() {
        return prescriber;
    }

    public void setPrescriber(String prescriber) {
        this.prescriber = prescriber;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getMRDrug() {
        return MRDrug;
    }

    public void setMRDrug(String MRDrug) {
        this.MRDrug = MRDrug;
    }

    public String getMRDose() {
        return MRDose;
    }

    public void setMRDose(String MRDose) {
        this.MRDose = MRDose;
    }

    public String getBreakthroughDrug() {
        return breakthroughDrug;
    }

    public void setBreakthroughDrug(String breakthroughDrug) {
        this.breakthroughDrug = breakthroughDrug;
    }

    public String getBreakthroughDose() {
        return breakthroughDose;
    }

    public void setBreakthroughDose(String breakthroughDose) {
        this.breakthroughDose = breakthroughDose;
    }
}
