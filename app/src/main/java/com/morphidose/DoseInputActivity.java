package com.morphidose;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.TextView;


public class DoseInputActivity extends Activity{
    MorphidoseDbHelper mDbHelper;
    User user;
    TextView welcome;
    TextView mrdrug;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        mDbHelper = new MorphidoseDbHelper(getApplicationContext());
        new ReadPrescriptionTask().execute();
        setContentView(R.layout.dose_input_view);
        //Intent intent = getIntent();
        //User user = (User) intent.getSerializableExtra("user");
        welcome = (TextView) findViewById(R.id.dose_input_view);
        welcome.setText("Registration Successful!");
        mrdrug = (TextView) findViewById(R.id.mrdrug);
        mrdrug.setText(user.getPrescription().getMRDrug());

    }

    private boolean userCreated(){
        return user != null;
    }

    public class ReadPrescriptionTask extends AsyncTask<String, Void, Void> {
        MorphidoseContract morphidoseContract;
        SQLiteDatabase db;
        String[] projection;
        Cursor cursor;
        Prescription prescription;
        String hospitalNumber;

        @Override
        protected Void doInBackground(String ...params) {
            db = mDbHelper.getReadableDatabase();
            morphidoseContract = new MorphidoseContract();
            projection = morphidoseContract.getPrescriptionProjectionValues();
            cursor = db.query(MorphidoseContract.PrescriptionEntry.TABLE_NAME, projection, null, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()){
                prescription = new Prescription();
                hospitalNumber = cursor.getString(0);
                prescription.setPrescriber(cursor.getString(1));
                prescription.setDate(cursor.getString(2));
                prescription.setMRDrug(cursor.getString(3));
                prescription.setMRDose(cursor.getString(4));
                prescription.setBreakthroughDrug(cursor.getString(5));
                prescription.setBreakthroughDose(cursor.getString(6));
                user = new User(hospitalNumber, prescription);
                cursor.close();
            }

//            SQLiteDatabase dbm = mDbHelper.getWritableDatabase();
//
//            // 2. delete
//            dbm.delete(MorphidoseContract.PrescriptionEntry.TABLE_NAME, //table name
//                    MorphidoseContract.PrescriptionEntry.COLUMN_NAME_HOSPITAL_ID + " = ?",  // selections
//                    new String[] { String.valueOf("A9876") }); //selections args



            return null;
        }

        @Override
        protected void onPostExecute(Void param) {
            db.close();
            if(!userCreated()){
                Intent setUpActivity = new Intent(getApplicationContext(), SetUpActivity.class);
                startActivity(setUpActivity);
            }
        }
    }
}
