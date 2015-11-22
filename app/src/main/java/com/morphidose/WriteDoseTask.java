package com.morphidose;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import java.sql.Timestamp;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;


public class WriteDoseTask extends AsyncTask<MorphidoseDbHelper, Void, String[]> {
    MorphidoseContract morphidoseContract;
    Dose dose;
    SQLiteDatabase db;

    public WriteDoseTask(Dose dose){
        super();
        this.dose = dose;
    }

    @Override
    protected String[] doInBackground(MorphidoseDbHelper ...params) {
        Boolean success = false;
        while(!success) {
            db = params[0].getWritableDatabase();
            morphidoseContract = new MorphidoseContract();
            ContentValues values = morphidoseContract.createDoseContentValues(dose);
            if(db.insert(MorphidoseContract.DoseEntry.TABLE_NAME, null, values) != -1){
                success = true;
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(String[] param) {
        db.close();
    }
}
