package com.morphidose;

import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

public class DeleteDoseTask extends AsyncTask<MorphidoseDbHelper, Void, String[]> {
    MorphidoseContract morphidoseContract;
    Dose dose;
    SQLiteDatabase db;

    public DeleteDoseTask(Dose dose){
        super();
        this.dose = dose;
    }

    @Override
    protected String[] doInBackground(MorphidoseDbHelper ...params) {
        Boolean success = false;
        db = params[0].getWritableDatabase();
        morphidoseContract = new MorphidoseContract();
        while(!success) {
            if(dose != null){
                String selection = morphidoseContract.getSelectionClauseWhereDoseEntryDateIsLessThanOrEqualToPlaceholder();
                String[] selectionArgs = {String.valueOf(dose.getDate().getTime())};
                if (db.delete(MorphidoseContract.DoseEntry.TABLE_NAME, selection, selectionArgs) != -1) {
                    success = true;
                }
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(String[] param) {
        db.close();
    }
}
