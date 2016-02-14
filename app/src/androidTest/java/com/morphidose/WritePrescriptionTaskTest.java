package com.morphidose;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
public class WritePrescriptionTaskTest {
    private MorphidoseDbHelper morphidoseDbHelper;
    private SQLiteDatabase db;
    private MorphidoseContract morphidoseContract;
    private List<String> hospitalNumbers;
    private User user;

    @Before
    public void setUp() throws Exception {
        morphidoseContract = new MorphidoseContract();
        getTargetContext().deleteDatabase(MorphidoseDbHelper.DATABASE_NAME);
        morphidoseDbHelper = new MorphidoseDbHelper(getTargetContext());
        hospitalNumbers = new ArrayList<String>();
        Prescription prescription = new Prescription();
        user = new User("1", prescription);
    }

    @After
    public void tearDownClass() throws Exception {
        if(db!=null){
            db.close();
        }
    }

    @Test
    public void shouldAddPrescriptionIfNoPrescriptionCurrentlyInDatabase() throws Exception {
        WritePrescriptionTask writePrescriptionTask = new WritePrescriptionTask(user);
        writePrescriptionTask.execute(morphidoseDbHelper).get(5000, TimeUnit.MILLISECONDS);
        db = morphidoseDbHelper.getReadableDatabase();
        String[] projection = morphidoseContract.getPrescriptionProjectionValues();
        Cursor cursor = db.query(MorphidoseContract.PrescriptionEntry.TABLE_NAME, projection, null, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()){
            do {
                String hospitalNumber = cursor.getString(0);
                hospitalNumbers.add(hospitalNumber);
            }while(cursor.moveToNext());
            cursor.close();
        }
        assertThat(hospitalNumbers.size(), is(1));
    }

    @Test
    public void shouldReplacePrescriptionCurrentlyInDatabaseWithNewPrescription() throws Exception {
        WritePrescriptionTask writePrescriptionTask = new WritePrescriptionTask(user);
        writePrescriptionTask.execute(morphidoseDbHelper);
        WritePrescriptionTask writePrescriptionTask2 = new WritePrescriptionTask(user);
        writePrescriptionTask2.execute(morphidoseDbHelper).get(5000, TimeUnit.MILLISECONDS);
        db = morphidoseDbHelper.getReadableDatabase();
        String[] projection = morphidoseContract.getPrescriptionProjectionValues();
        Cursor cursor = db.query(MorphidoseContract.PrescriptionEntry.TABLE_NAME, projection, null, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()){
            do {
                String hospitalNumber = cursor.getString(0);
                hospitalNumbers.add(hospitalNumber);
            }while(cursor.moveToNext());
            cursor.close();
        }
        assertThat(hospitalNumbers.size(), is(1));
    }

}
