package com.morphidose;

import android.content.ContentValues;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

public class WriteDoseTask extends AsyncTask<MorphidoseDbHelper, Void, Void> {
    MorphidoseContract morphidoseContract;
    Dose dose;
    SQLiteDatabase db;

    public WriteDoseTask(Dose dose){
        super();
        this.dose = dose;
    }

    @Override
    protected Void doInBackground(MorphidoseDbHelper ...params) {
            db = params[0].getWritableDatabase();
            morphidoseContract = new MorphidoseContract();
            ContentValues values = morphidoseContract.createDoseContentValues(dose);
            try{
                db.insertOrThrow(MorphidoseContract.DoseEntry.TABLE_NAME, null, values);
            }catch(SQLiteConstraintException ex){
                Log.e("SQLiteConstraintEx", "SQLiteConstraintException in WriteDoseTask.doInBackground", ex);
            }
        return null;
    }

    @Override
    protected void onPostExecute(Void param) {
        db.close();
    }
}
