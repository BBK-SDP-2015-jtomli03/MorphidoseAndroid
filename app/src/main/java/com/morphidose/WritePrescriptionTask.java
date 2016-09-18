package com.morphidose;

import android.content.ContentValues;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;


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
            db = params[0].getWritableDatabase();
            morphidoseContract = new MorphidoseContract();
            ContentValues values = morphidoseContract.createPrescriptionContentValues(user);
            try{
                db.insertWithOnConflict(MorphidoseContract.PrescriptionEntry.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
            }catch(SQLiteConstraintException ex){
                Log.e("SQLiteConstraintEx", "SQLiteConstraintException in WritePrescriptionTask.doInBackground", ex);
            }
        return null;
    }

    @Override
    protected void onPostExecute(Void param) {
      db.close();
    }
}
