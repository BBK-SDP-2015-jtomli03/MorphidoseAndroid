package com.morphidose;

import android.content.ContentValues;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

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
            try{
                if(db.insertOrThrow(MorphidoseContract.DoseEntry.TABLE_NAME, null, values) != -1){
                    success = true;
                }
            }catch(SQLiteConstraintException ex){
                Log.e("SQLiteConstraintException in WriteDoseTask.doInBackground", ex.getMessage(), ex);
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(String[] param) {
        db.close();
    }
}
