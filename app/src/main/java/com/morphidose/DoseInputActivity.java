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

import java.net.SocketException;
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
        private ConnectivityManager connectivityManager;
        private MorphidoseContract morphidoseContract;
        private SQLiteDatabase db;
        private String[] projection;
        private Cursor cursor;
        private String hospitalNumber;
        private Long date;
        private List<Dose> doses = new ArrayList<Dose>();
        private Dose mostRecentDose;
        private Dose latestDoseToRemove;

        @Override
        protected Void doInBackground(String... params) {
            mostRecentDose = new Dose(new Timestamp(new DateTime().withZone(DateTimeZone.forID("Europe/London")).getMillis()), user.getHospitalNumber());
            connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            if (HttpUtility.getHttpUtility().isConnectedToInternet(connectivityManager)) {
                //doses = new ArrayList<Dose>();
                doses.add(mostRecentDose);
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
                db.close();
                latestDoseToRemove = HttpUtility.getHttpUtility().sendDoses(doses, DoseInputActivity.this);
                if(latestDoseToRemove == null){
                    //there has been an error -> try again later ie when wifi connected
                    saveDose(mostRecentDose);
                }else{
                    deleteSentDosesFromDatabase(latestDoseToRemove);
                }
            }else{
                saveDose(mostRecentDose);
                // if wifi connected send doses in the background -> http://stackoverflow.com/questions/17063910/need-to-run-service-while-device-got-wifi-data-connection
                // need some way of showing if doses aren't sent so pt can push prior to appointment
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void param) {
                            runOnUiThread(new Runnable()
                {

                    public void run()
                    {
                        Toast.makeText(getApplicationContext(), String.valueOf(doses.size()),
                                Toast.LENGTH_LONG).show();                    }
                });
            doseAcceptedAlertBox().show();
        }
    }

    private void saveDose(Dose dose){
        new WriteDoseTask(dose).execute(mDbHelper);
    }

    private void deleteSentDosesFromDatabase(Dose dose){
        new DeleteDoseTask(dose).execute(mDbHelper);
    }

}
