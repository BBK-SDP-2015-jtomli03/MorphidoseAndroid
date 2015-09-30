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
    User user;
    SQLiteDatabase db;

    public WriteDoseTask(User user){
        super();
        this.user = user;
    }

    @Override
    protected String[] doInBackground(MorphidoseDbHelper ...params) {
        Boolean success = false;
        //while(!success) {
            db = params[0].getWritableDatabase();
            morphidoseContract = new MorphidoseContract();

        //db.execSQL(MorphidoseContract.SQL_CREATE_DOSE_ENTRIES);

            ContentValues values = morphidoseContract.createDoseContentValues(user.getHospitalNumber());
            db.insert(MorphidoseContract.DoseEntry.TABLE_NAME, null, values);

//            if (db.insert(MorphidoseContract.DoseEntry.TABLE_NAME, null, values) != -1) {
//                //successful insert -> continue
//                success = true;
//            }
        //}


        return null;
    }

    @Override
    protected void onPostExecute(String[] param) {
        db.close();
    }
}
