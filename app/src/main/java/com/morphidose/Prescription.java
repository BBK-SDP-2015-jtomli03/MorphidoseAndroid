package com.morphidose;

import java.io.Serializable;

public class Prescription implements Serializable {
    private static final long serialVersionUID = 1L;
    private String prescriber;
    private String date;
    private String mrdrug;
    private String mrdose;
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

    public void setMRDrug(String MRDrug) {
        this.mrdrug = MRDrug;
    }

    public String getMRDose() {
        return mrdose;
    }

    public String getMRDrug() {
        return mrdrug;
    }

    public void setMRDose(String MRDose) {
        this.mrdose = MRDose;
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
