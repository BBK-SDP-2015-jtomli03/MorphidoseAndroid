package com.morphidose;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
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
    private NetworkReceiver receiver;
    private IntentFilter filter;
    private User user;
    private Dose mostRecentDose;
    // Whether the display should be refreshed.
    private boolean refreshDisplay = true;
    private boolean userInputDose;
    private boolean created = false;
    private TextView hospitalNumber;
    private TextView mrdrug;
    private ConnectivityManager connectivityManager;
    private boolean dosesInDatabase = true;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        mDbHelper = new MorphidoseDbHelper(getApplicationContext());
        //new ReadPrescriptionTask().execute();
        loadPage();
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        receiver = new NetworkReceiver();
        filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(receiver, filter); // Registers BroadcastReceiver to track network connection changes.
        setContentView(R.layout.dose_input_view);
        created = true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (receiver != null) {
            this.unregisterReceiver(receiver);
        }
    }

    // Refreshes the display
//    @Override
//    public void onStart () {
//        super.onStart();
//        if(refreshDisplay){
//            loadPage();
//        }
//    }

    public void loadPage(){
        new ReadPrescriptionTask().execute();
    }

    public void addDose(View view){
        userInputDose = true;
        mostRecentDose = new Dose(new Timestamp(new DateTime().withZone(DateTimeZone.forID("Europe/London")).getMillis()), user.getHospitalNumber());
        if (HttpUtility.getHttpUtility().isConnectedToInternet(connectivityManager)) {
            new AddDoseTask().execute();
        }else{
            saveDose(mostRecentDose);
            doseAcceptedAlertBox().show();
        }
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
            if(user == null){
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
        private Dose latestDoseToRemove;

        @Override
        protected Void doInBackground(String... params) {
            doses = new ArrayList<Dose>();
            if(userInputDose){
                doses.add(mostRecentDose);
            }
            if(dosesInDatabase){
                addSavedDosesToDosesToSend();
            }
            if(doses.size() > 0){ // on loading the app it will run this task to check if any doses need sending from the DB, if not doses.size = 0.
                latestDoseToRemove = HttpUtility.getHttpUtility().sendDoses(doses);
                if(latestDoseToRemove != null){
                    deleteSentDosesFromDatabase(latestDoseToRemove);
                }else if(userInputDose && HttpUtility.getHttpUtility().isConnectedToInternet(connectivityManager)){
                    new AddDoseTask().execute(); //there has been an error -> try again
                }else if(userInputDose){
                    // need some way of showing if doses aren't sent so pt can push prior to appointment
                    saveDose(mostRecentDose); //there has been an error -> try again later when connected to the internet.
                }else if(HttpUtility.getHttpUtility().isConnectedToInternet(connectivityManager)){
                    new AddDoseTask().execute(); //clause added because when on reconnection to internet the first addDoseTask doesn't send the doses
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void param) {
//                            runOnUiThread(new Runnable() {
//
//                                public void run() {
//                                    Toast.makeText(getApplicationContext(), String.valueOf(doses.size()) + count,
//                                            Toast.LENGTH_LONG).show();
//                                }
//                            });
            if(userInputDose){
                doseAcceptedAlertBox().show();
            }
        }

        private void addSavedDosesToDosesToSend(){
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
        }
    }

    private void saveDose(Dose dose){
        new WriteDoseTask(dose).execute(mDbHelper);
        dosesInDatabase = true;
    }

    private void deleteSentDosesFromDatabase(Dose dose){
        new DeleteDoseTask(dose).execute(mDbHelper);
        dosesInDatabase = false;
    }

    public class NetworkReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(created){
                if (HttpUtility.getHttpUtility().isConnectedToInternet(connectivityManager) && dosesInDatabase) {
                    userInputDose = false;
                    new AddDoseTask().execute();
                    refreshDisplay = true;
                } else {
                    refreshDisplay = false;
                }
            }
        }
    }

}
