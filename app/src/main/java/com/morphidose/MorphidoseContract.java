package com.morphidose;

import android.content.ContentValues;
import android.provider.BaseColumns;

public class MorphidoseContract {

    private static final String PLACEHOLDER = "?";
    private static final String LESS_THAN_OR_EQUAL_TO = "<=";
    private static final String TEXT_TYPE = " TEXT";
    private static final String INTEGER_TYPE = " INTEGER";
    private static final String COMMA_SEP = ",";
    private static final String PRIMARY_KEY = " PRIMARY KEY";
    public static final String SQL_CREATE_DOSE_ENTRIES =
                               "CREATE TABLE " + DoseEntry.TABLE_NAME + " (" +
                               DoseEntry.COLUMN_NAME_DATE_ID + INTEGER_TYPE + PRIMARY_KEY + COMMA_SEP +
                               DoseEntry.COLUMN_NAME_HOSPITAL + TEXT_TYPE + " )";
    public static final String SQL_CREATE_PRESCRIPTION_ENTRY =
                               "CREATE TABLE " + PrescriptionEntry.TABLE_NAME + " (" +
                               PrescriptionEntry.COLUMN_NAME_HOSPITAL_ID + TEXT_TYPE + PRIMARY_KEY + COMMA_SEP +
                               PrescriptionEntry.COLUMN_NAME_PRESCRIBER + TEXT_TYPE + COMMA_SEP +
                               PrescriptionEntry.COLUMN_NAME_DATE + TEXT_TYPE + COMMA_SEP +
                               PrescriptionEntry.COLUMN_NAME_MRDRUG + TEXT_TYPE + COMMA_SEP +
                               PrescriptionEntry.COLUMN_NAME_MRDOSE + TEXT_TYPE + COMMA_SEP +
                               PrescriptionEntry.COLUMN_NAME_BREAKTHROUGH_DRUG + TEXT_TYPE + COMMA_SEP +
                               PrescriptionEntry.COLUMN_NAME_BREAKTHROUGH_DOSE + TEXT_TYPE + " )";

    public MorphidoseContract() {}

    /* Defines the doses table contents */
    public static abstract class DoseEntry implements BaseColumns {
        public static final String TABLE_NAME = "doses";
        public static final String COLUMN_NAME_DATE_ID = "date";
        public static final String COLUMN_NAME_HOSPITAL = "hospitalnumber";
    }

    /* Defines the prescription table contents */
    public static abstract class PrescriptionEntry implements BaseColumns {
        public static final String TABLE_NAME = "prescription";
        public static final String COLUMN_NAME_HOSPITAL_ID = "hospitalnumber";
        public static final String COLUMN_NAME_PRESCRIBER = "prescriber";
        public static final String COLUMN_NAME_DATE = "date";
        public static final String COLUMN_NAME_MRDRUG = "mrdrug";
        public static final String COLUMN_NAME_MRDOSE = "mrdose";
        public static final String COLUMN_NAME_BREAKTHROUGH_DRUG = "breakthroughDrug";
        public static final String COLUMN_NAME_BREAKTHROUGH_DOSE = "breakthroughDose";
    }

    // Creates a new map of values for the prescription table, where column names are the keys
    public ContentValues createPrescriptionContentValues(User user){
        ContentValues values = new ContentValues();
        values.put(PrescriptionEntry.COLUMN_NAME_HOSPITAL_ID, user.getHospitalNumber());
        values.put(PrescriptionEntry.COLUMN_NAME_PRESCRIBER, user.getPrescription().getPrescriber());
        values.put(PrescriptionEntry.COLUMN_NAME_DATE, user.getPrescription().getDate());
        values.put(PrescriptionEntry.COLUMN_NAME_MRDRUG, user.getPrescription().getMRDrug());
        values.put(PrescriptionEntry.COLUMN_NAME_MRDOSE, user.getPrescription().getMRDose());
        values.put(PrescriptionEntry.COLUMN_NAME_BREAKTHROUGH_DRUG, user.getPrescription().getBreakthroughDrug());
        values.put(PrescriptionEntry.COLUMN_NAME_BREAKTHROUGH_DOSE, user.getPrescription().getBreakthroughDose());
        return values;
    }
    // Defines a projection that specifies which columns from the prescription table to read
    public String[] getPrescriptionProjectionValues(){
        String[] projection = {
                PrescriptionEntry.COLUMN_NAME_HOSPITAL_ID,
                PrescriptionEntry.COLUMN_NAME_PRESCRIBER,
                PrescriptionEntry.COLUMN_NAME_DATE,
                PrescriptionEntry.COLUMN_NAME_MRDRUG,
                PrescriptionEntry.COLUMN_NAME_MRDOSE,
                PrescriptionEntry.COLUMN_NAME_BREAKTHROUGH_DRUG,
                PrescriptionEntry.COLUMN_NAME_BREAKTHROUGH_DOSE
        };
        return projection;
    }

    // Creates a new map of values for the doses table, where column names are the keys
    public ContentValues createDoseContentValues(Dose dose){
        ContentValues values = new ContentValues();
        values.put(DoseEntry.COLUMN_NAME_DATE_ID, dose.getDate().getTime());
        values.put(DoseEntry.COLUMN_NAME_HOSPITAL, dose.getHospitalNumber());
        return values;
    }

    public String getSelectionClauseWhereDoseEntryDateIsLessThanOrEqualToPlaceholder(){
        return DoseEntry.COLUMN_NAME_DATE_ID + LESS_THAN_OR_EQUAL_TO + PLACEHOLDER;
    }

    // Defines a projection that specifies which columns from the doses table to read
    public String[] getDoseProjectionValues(){
        String[] projection = {
                DoseEntry.COLUMN_NAME_DATE_ID,
                DoseEntry.COLUMN_NAME_HOSPITAL
        };
        return projection;
    }

}
