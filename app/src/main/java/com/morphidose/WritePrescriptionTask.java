package com.morphidose;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;


public class WritePrescriptionTask extends AsyncTask<MorphidoseDbHelper, Void, Void> {
    MorphidoseContract morphidoseContract;
    User user;
    SQLiteDatabase db;

    public WritePrescriptionTask(User user){
        super();
        this.user = user;
    }

    @Override
    protected Void doInBackground(MorphidoseDbHelper ...params) {
        Boolean success = false;
        while(!success) {
            db = params[0].getWritableDatabase();
            morphidoseContract = new MorphidoseContract();
            ContentValues values = morphidoseContract.createPrescriptionContentValues(user);
            if (db.insertWithOnConflict(MorphidoseContract.PrescriptionEntry.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE) != -1) {
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
