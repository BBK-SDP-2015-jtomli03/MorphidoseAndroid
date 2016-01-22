package com.morphidose;

import java.io.Serializable;
import java.sql.Timestamp;

public class Dose implements Serializable {
    private static final long serialVersionUID = 1L;
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
