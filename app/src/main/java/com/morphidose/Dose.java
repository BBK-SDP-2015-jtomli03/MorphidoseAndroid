package com.morphidose;

import java.sql.Timestamp;

/**
 * Created by Jo on 01/09/2015.
 */
public class Dose {
    private Timestamp date; //change to String
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

    public void setDate(Timestamp date) {
        this.date = date;
    }

    public String getHospitalNumber() {
        return hospitalNumber;
    }

    public void setHospitalNumber(String hospitalNumber) {
        this.hospitalNumber = hospitalNumber;
    }
}
