package com.morphidose;

import java.sql.Timestamp;

public class Dose {
    private Timestamp date;
    private String hospitalNumber;

    public Dose(){
        //empty constructor to allow for Jackson json conversion
    }

    public Dose(Timestamp date, String hospitalNumber){
        this.date = date;
        this.hospitalNumber = hospitalNumber;
    }

    public Timestamp getDate() {
        return date;
    }

    public String getHospitalNumber() {
        return hospitalNumber;
    }
}
