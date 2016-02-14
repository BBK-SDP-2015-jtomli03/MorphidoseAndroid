package com.morphidose;


import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.support.test.runner.AndroidJUnit4;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static android.support.test.InstrumentationRegistry.getTargetContext;

import android.util.Log;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
public class DeleteDoseTaskTest {
    private String hospitalNumber = "1";
    private Timestamp timestamp = Timestamp.valueOf("2000-02-15 00:00:00.0");
    private MorphidoseDbHelper morphidoseDbHelper;
    private boolean dosesInDatabase;
    private DeleteDoseTask deleteDoseTask;
    private SQLiteDatabase db;
    private MorphidoseContract morphidoseContract;


    @Before
    public void setUp() throws Exception {
        morphidoseContract = new MorphidoseContract();
        getTargetContext().deleteDatabase(MorphidoseDbHelper.DATABASE_NAME);
        Dose dose = new Dose(timestamp, hospitalNumber);
        BooleanHandler booleanHandler = new BooleanHandler() {
            @Override
            public void handleBoolean(Boolean result) {
                dosesInDatabase = result;
            }
        };
        deleteDoseTask = new DeleteDoseTask(dose, booleanHandler);
    }

    @After
    public void tearDown() throws Exception {
        if(db!=null){
            db.close();
        }
    }

    @Test
    public void shouldHandleDeleteDoseTaskCorrectlyIfNoDosesInDatabase() throws Exception {
        List<Dose> doses = new ArrayList<Dose>();
        getReadableDatabase();
        deleteDoseTask.execute(morphidoseDbHelper).get(5000, TimeUnit.MILLISECONDS);
        getReadableDatabase();
        String[] projection = morphidoseContract.getDoseProjectionValues();
        Cursor cursor = db.query(MorphidoseContract.DoseEntry.TABLE_NAME, projection, null, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()){
            do {
                Long date = cursor.getLong(0);
                String hospitalNumber = cursor.getString(1);
                doses.add(new Dose(new Timestamp(date), hospitalNumber));
            }while(cursor.moveToNext());
            cursor.close();
        }
        assertFalse(dosesInDatabase);
        assertThat(doses.size(), is(0));
    }

    @Test
    public void shouldHandleDeleteDoseTaskCorrectlyIfDosesInDatabaseDatedPriorToDoseGiven() throws Exception {
        getWritableDatabase();
        populateDatabaseWithDoseEntries(5, 1);
        List<Dose> doses = new ArrayList<Dose>();
        assertTrue(dosesInDatabase);
        deleteDoseTask.execute(morphidoseDbHelper).get(5000, TimeUnit.MILLISECONDS);
        getReadableDatabase();
        String[] projection = morphidoseContract.getDoseProjectionValues();
        Cursor cursor = db.query(MorphidoseContract.DoseEntry.TABLE_NAME, projection, null, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()){
            do {
                Long date = cursor.getLong(0);
                String hospitalNumber = cursor.getString(1);
                doses.add(new Dose(new Timestamp(date), hospitalNumber));
            }while(cursor.moveToNext());
            cursor.close();
        }
        assertFalse(dosesInDatabase);
        assertThat(doses.size(), is(0));
    }

    @Test
    public void shouldHandleDeleteDoseTaskCorrectlyIfDosesInDatabaseDatedPriorAndAfterDoseGiven() throws Exception {
        getWritableDatabase();
        populateDatabaseWithDoseEntries(5, 1);
        List<Dose> doses = new ArrayList<Dose>();
        assertTrue(dosesInDatabase);
        deleteDoseTask.execute(morphidoseDbHelper);
        populateDatabaseWithDoseEntries(1, 20); //adds a dose with a timestamp later than the latest dose to be removed.
        Thread.sleep(5000);
        getReadableDatabase();
        String[] projection = morphidoseContract.getDoseProjectionValues();
        Cursor cursor = db.query(MorphidoseContract.DoseEntry.TABLE_NAME, projection, null, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()){
            do {
                Long date = cursor.getLong(0);
                String hospitalNumber = cursor.getString(1);
                doses.add(new Dose(new Timestamp(date), hospitalNumber));
            }while(cursor.moveToNext());
            cursor.close();
        }
        assertThat(doses.size(), is(1));
    }

    private void populateDatabaseWithDoseEntries(int numberOfEntries, int startAt){
        for(int i=startAt; i < numberOfEntries + startAt; i++){
            String day = i < 10? "0"+i : Integer.toString(i);
            Timestamp timestamp = Timestamp.valueOf("2000-02-" + day + " 00:00:00.0");
            Dose doseToAdd = new Dose(timestamp, hospitalNumber);
            ContentValues values = morphidoseContract.createDoseContentValues(doseToAdd);
            try{
                db.insertOrThrow(MorphidoseContract.DoseEntry.TABLE_NAME, null, values);
            }catch(SQLiteConstraintException ex){
                Log.e("SQLiteConstraintEx", "SQLiteConstraintException in DeleteDoseTaskTest. Msge; " + ex.getMessage(), ex);
            }
        }
        dosesInDatabase = true;
    }

    private void getReadableDatabase(){
        morphidoseDbHelper = new MorphidoseDbHelper(getTargetContext());
        db = morphidoseDbHelper.getReadableDatabase();
    }

    private void getWritableDatabase(){
        morphidoseDbHelper = new MorphidoseDbHelper(getTargetContext());
        db = morphidoseDbHelper.getWritableDatabase();
    }

}
