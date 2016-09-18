package com.morphidose;

import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

public class DeleteDoseTask extends AsyncTask<MorphidoseDbHelper, Void, Boolean> {
    MorphidoseContract morphidoseContract;
    Dose dose;
    BooleanHandler booleanHandler;
    SQLiteDatabase db;

    public DeleteDoseTask(Dose dose, BooleanHandler handler){
        super();
        this.dose = dose;
        this.booleanHandler = handler;
    }

    @Override
    protected Boolean doInBackground(MorphidoseDbHelper ...params) {
        db = params[0].getWritableDatabase();
        morphidoseContract = new MorphidoseContract();
        if(dose != null){
            String selection = morphidoseContract.getSelectionClauseWhereDoseEntryDateIsLessThanOrEqualToPlaceholder();
            String[] selectionArgs = {String.valueOf(dose.getDate().getTime())};
            return db.delete(MorphidoseContract.DoseEntry.TABLE_NAME, selection, selectionArgs) == -1;
        }
        return true;
    }

    @Override
    protected void onPostExecute(Boolean dosesInDatabase) {
        db.close();
        if(booleanHandler!=null){
            booleanHandler.handleBoolean(dosesInDatabase);
        }
    }
}
