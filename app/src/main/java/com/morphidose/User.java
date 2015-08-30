package com.morphidose;

import java.io.Serializable;

public class User implements Serializable{
    private static final long serialVersionUID = 1L;
    private String hospitalNumber;
    private String name;
    private String MRDrug;
    private String breakthroughDrug;
    private String MRDose;
    private String breakthroughDose;

    public User(){}

    public String getHospitalNumber() {
        return hospitalNumber;
    }

    public String getBreakthroughDose() {
        return breakthroughDose;
    }

    public String getName() {
        return name;
    }

    public String getMRDrug() {
        return MRDrug;
    }

    public String getBreakthroughDrug() {
        return breakthroughDrug;
    }

    public String getMRDose() {
        return MRDose;
    }
}
