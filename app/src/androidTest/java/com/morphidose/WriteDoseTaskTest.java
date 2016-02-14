package com.morphidose;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
public class WriteDoseTaskTest {
    private String hospitalNumber = "1";
    private Timestamp timestamp = Timestamp.valueOf("2000-02-15 00:00:00.0");
    private Timestamp timestamp2 = Timestamp.valueOf("2000-02-04 00:00:00.0");
    private MorphidoseDbHelper morphidoseDbHelper;
    private SQLiteDatabase db;
    private MorphidoseContract morphidoseContract;
    private List<Dose> doses;


    @Before
    public void setUp() throws Exception {
        morphidoseContract = new MorphidoseContract();
        getTargetContext().deleteDatabase(MorphidoseDbHelper.DATABASE_NAME);
        morphidoseDbHelper = new MorphidoseDbHelper(getTargetContext());
        doses = new ArrayList<Dose>();
    }

    @After
    public void tearDownClass() throws Exception {
        if(db!=null){
            db.close();
        }
    }

    @Test
    public void shouldAddDosesIfNoDosesCurrentlyInDatabase() throws Exception {
        Dose dose = new Dose(timestamp, hospitalNumber);
        WriteDoseTask writeDoseTask = new WriteDoseTask(dose);
        writeDoseTask.execute(morphidoseDbHelper).get(5000, TimeUnit.MILLISECONDS);
        db = morphidoseDbHelper.getReadableDatabase();
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

    @Test
    public void shouldAddDosesIfDosesCurrentlyInDatabase() throws Exception {
        for(int i=0; i<3; i++){
            Timestamp timestamp = Timestamp.valueOf("2000-02-0" + i + " 00:00:00.0");
            Dose doseToAdd = new Dose(timestamp, hospitalNumber);
            WriteDoseTask writeDoseTask = new WriteDoseTask(doseToAdd);
            writeDoseTask.execute(morphidoseDbHelper);
        }
        WriteDoseTask writeDoseTask = new WriteDoseTask(new Dose(timestamp2, hospitalNumber));
        writeDoseTask.execute(morphidoseDbHelper).get(5000, TimeUnit.MILLISECONDS);
        db = morphidoseDbHelper.getReadableDatabase();
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
        assertThat(doses.size(), is(4));
    }

    @Test
    public void shouldNotAddADoseIfDoseCurrentlyExists() throws Exception {
        Dose dose1 = new Dose(timestamp, hospitalNumber);
        WriteDoseTask writeDoseTask1 = new WriteDoseTask(dose1);
        writeDoseTask1.execute(morphidoseDbHelper);
        Dose dose2 = new Dose(timestamp, hospitalNumber);
        WriteDoseTask writeDoseTask2 = new WriteDoseTask(dose2);
        writeDoseTask2.execute(morphidoseDbHelper).get(5000, TimeUnit.MILLISECONDS);
        db = morphidoseDbHelper.getReadableDatabase();
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

}