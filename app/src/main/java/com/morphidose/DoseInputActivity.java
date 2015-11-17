package com.morphidose;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;


public class DoseInputActivity extends Activity{
    static final int SET_UP_REQUEST = 0;
    private MorphidoseDbHelper mDbHelper;
    private User user;
    private TextView hospitalNumber;
    private TextView mrdrug;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        mDbHelper = new MorphidoseDbHelper(getApplicationContext());
        new ReadPrescriptionTask().execute();
        setContentView(R.layout.dose_input_view);
    }

    public void addDose(View view){
        new AddDoseTask().execute();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == SET_UP_REQUEST) {
            if (resultCode == RESULT_OK) {
                registrationSuccessfulAlertBox().show();
                user = (User) intent.getSerializableExtra("user");
            }
            else if (resultCode == RESULT_CANCELED) {
                errorAlertBox().show();
            }
        }
    }

    private AlertDialog doseAcceptedAlertBox(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(DoseInputActivity.this);
        alertDialogBuilder
                .setMessage("Dose submitted successfully!")
                .setCancelable(false)
                .setPositiveButton("Close the App", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }
                })
                .setNegativeButton("Keep App Open", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        return alertDialogBuilder.create();
    }

    private AlertDialog registrationSuccessfulAlertBox(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(DoseInputActivity.this);
        alertDialogBuilder
                .setMessage("Registration successful! You can now start logging your \"breakthrough\" doses.")
                .setCancelable(true)
                .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        new ReadPrescriptionTask().execute();
                    }
                });
        return alertDialogBuilder.create();
    }

    private AlertDialog errorAlertBox(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(DoseInputActivity.this);
        alertDialogBuilder
                .setMessage("Sorry - there was an error while processing your data - let us try again.")
                .setCancelable(true)
                .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        new ReadPrescriptionTask().execute();
                    }
                });
        return alertDialogBuilder.create();
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

            //db.execSQL(MorphidoseContract.SQL_CREATE_DOSE_ENTRIES);

            //db.execSQL("DROP TABLE IF EXISTS doses");

//            SQLiteDatabase dbm = mDbHelper.getWritableDatabase();
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
                startActivityForResult(setUpActivity, SET_UP_REQUEST);
            }
        }
    }

    private class AddDoseTask extends AsyncTask<String, Void, Void> {
        private MorphidoseContract morphidoseContract;
        private SQLiteDatabase db;
        private String[] projection;
        private Cursor cursor;
        private String hospitalNumber;
        private Long date;
        private List<Dose> doses;
        private Dose mostRecentDose;
        private Dose latestDoseToRemove;

        @Override
        protected Void doInBackground(String... params) {
            mostRecentDose = new Dose(new Timestamp(new DateTime().withZone(DateTimeZone.forID("Europe/London")).getMillis()), user.getHospitalNumber());
            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            if (HttpUtility.getHttpUtility().isConnectedToInternet(connectivityManager)) {
                doses = new ArrayList<Dose>();
                doses.add(mostRecentDose);
//                Prescription prescription = HttpUtility.getHttpUtility().getUserPrescription(user);
//                user.setPrescription(prescription);
                //check db for any other doses to send
                //send doses ->server side check no duplication of doses
                //recieve date of most recent dose back
               //remove doses from most recent date backwards
                //****DONT FORGET DOSES ARE IN LONG -> NEED CONVERTING TO TIMESTAMP!!!!
                db = mDbHelper.getWritableDatabase();
                morphidoseContract = new MorphidoseContract();
                projection = morphidoseContract.getDoseProjectionValues();
                cursor = db.query(MorphidoseContract.DoseEntry.TABLE_NAME, projection, null, null, null, null, null);
                if (cursor != null && cursor.moveToFirst()){
                    do {
                        date = cursor.getLong(0);
                        hospitalNumber = cursor.getString(1);
                        doses.add(new Dose(new Timestamp(date), hospitalNumber));
                    }while(cursor.moveToNext());
                    cursor.close();
                }

//                runOnUiThread(new Runnable()
//                {
//
//                    public void run()
//                    {
//                        Toast.makeText(getApplicationContext(), doses.get(doses.size() - 1).getDate().toString(),
//                                Toast.LENGTH_LONG).show();                    }
//                });

                latestDoseToRemove = HttpUtility.getHttpUtility().sendDoses(doses);

//                runOnUiThread(new Runnable()
//                {
//
//                    public void run()
//                    {
//                        Toast.makeText(getApplicationContext(), latestDoseToRemove,
//                                Toast.LENGTH_LONG).show();                    }
//                });

//                runOnUiThread(new Runnable()
//                {
//
//                    public void run()
//                    {
//                        Toast.makeText(getApplicationContext(), "in",
//                                Toast.LENGTH_LONG).show();                    }
//                });
            }else{
                saveDose(mostRecentDose);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void param) {
            db.close();
//                            runOnUiThread(new Runnable()
//                {
//
//                    public void run()
//                    {
//                        Toast.makeText(getApplicationContext(), latestDoseToRemove,
//                                Toast.LENGTH_LONG).show();                    }
//                });
            doseAcceptedAlertBox().show();
        }
    }

    private void saveDose(Dose dose){
        new WriteDoseTask(dose).execute(mDbHelper);
    }




//
//    public class DWriteDoseTask extends AsyncTask<MorphidoseDbHelper, Void, String[]> {
//        MorphidoseContract morphidoseContract;
//        User user;
//        SQLiteDatabase db;
//
//        public DWriteDoseTask(User user){
//            super();
//            this.user = user;
//        }
//
//        @Override
//        protected String[] doInBackground(MorphidoseDbHelper ...params) {
//            Boolean success = false;
//            //while(!success) {
//            //db = params[0].getWritableDatabase();
//            //morphidoseContract = new MorphidoseContract();
//
//            //db.execSQL(MorphidoseContract.SQL_CREATE_DOSE_ENTRIES);
//
////            ContentValues values = morphidoseContract.createDoseContentValues(user.getHospitalNumber());
////            db.insert(MorphidoseContract.DoseEntry.TABLE_NAME, null, values);
//
////            if (db.insert(MorphidoseContract.DoseEntry.TABLE_NAME, null, values) != -1) {
////                //successful insert -> continue
////                success = true;
////            }
//            //}
//
//            db = params[0].getReadableDatabase();
//            Cursor cursor = db.query(MorphidoseContract.DoseEntry.TABLE_NAME, null, null, null, null, null, null);
//            String[] columns = cursor.getColumnNames();
//            return columns;
//            //return null;
//        }
//
//        @Override
//        protected void onPostExecute(String[] param) {
//            db.close();
//            columnsAlertBox(param).show();
//        }
//
//        private AlertDialog columnsAlertBox(String[] param){
//            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(DoseInputActivity.this);
//            alertDialogBuilder
//                    .setMessage(param[0])
//                    .setCancelable(true)
//                    .setNeutralButton("OK", new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog, int id) {
//                            dialog.cancel();
//                        }
//                    });
//            return alertDialogBuilder.create();
//        }
//    }
}
