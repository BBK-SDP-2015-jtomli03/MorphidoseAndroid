package com.morphidose;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import org.springframework.web.client.RestClientException;


public class WritePrescriptionTask extends AsyncTask<MorphidoseDbHelper, Void, Void> {
    MorphidoseContract morphidoseContract;
    User user;
    SQLiteDatabase db;
    Boolean success = false;

    public WritePrescriptionTask(User user){
        super();
        this.user = user;
    }

    @Override
    protected Void doInBackground(MorphidoseDbHelper ...params) {
        while(!success) {
            db = params[0].getWritableDatabase();
            morphidoseContract = new MorphidoseContract();
            ContentValues values = morphidoseContract.createPrescriptionContentValues(user);
            if (db.insert(MorphidoseContract.PrescriptionEntry.TABLE_NAME, null, values) != -1) {
                //successful insert -> continue
                success = true;
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void param) {
      db.close();
    }
}