package com.morphidose;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;


public class DoseInputActivity extends Activity implements Serializable{
    private static final long serialVersionUID = 1L;
    static final int SET_UP_REQUEST = 0;
    public static final String USER = "user";
    private MorphidoseDbHelper mDbHelper;
    private ConnectivityManager connectivityManager;
    private NetworkReceiver receiver;
    IntentFilter dosesSentIntentFilter;
    private Context context;
    private ProgressDialog pd;
    private User user;
    private Dose mostRecentDose;
    private boolean userInputDose = false;
//    private boolean created = false;
    private boolean dosesInDatabase = true;
    private boolean recieverRegistered = false;
    private TextView centreMessage;
    private TextView centreMessageTitle;
    private TextView centre_message_bottom;
    private TextView bottomMessage;
    private Button breakthrough_dose;
    private Button regular_dose;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        context = this;
        mDbHelper = new MorphidoseDbHelper(getApplicationContext());
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        receiver = new NetworkReceiver(this, connectivityManager);
        registerNetworkReceiver();
        loadPage();
        setContentView(R.layout.dose_input_view);
        centreMessageTitle = (TextView)findViewById(R.id.centre_message_title);
        centreMessage = (TextView)findViewById(R.id.centre_message);
        centre_message_bottom = (TextView)findViewById(R.id.centre_message_bottom);
        bottomMessage = (TextView)findViewById(R.id.bottom_message);
        breakthrough_dose = (Button)findViewById(R.id.breakthrough_dose);
        regular_dose = (Button)findViewById(R.id.regular_dose);
        //created = true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(recieverRegistered) {
            unregisterNetworkReceiver();
        }
    }

    @Override
    public void onStop(){
        super.onStop();
        if(pd!=null) {
            pd.dismiss();
        }
    }

    @Override
    public void onPause(){
        super.onPause();
        if(!dosesInDatabase && recieverRegistered) {
            unregisterNetworkReceiver();
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        if(!recieverRegistered) {
            registerReceiver(receiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }
    }

    public void loadPage(){
        if(user==null){
            new ReadPrescriptionTask().execute();
        }
    }

    public void registerNetworkReceiver(){
        dosesSentIntentFilter = new IntentFilter(NetworkReceiver.BROADCAST_MESSAGE);
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, dosesSentIntentFilter);
        recieverRegistered = true;
    }

    public void unregisterNetworkReceiver(){
        this.unregisterReceiver(receiver);
        recieverRegistered = false;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if(requestCode == SET_UP_REQUEST) {
            if (resultCode == RESULT_OK) {
                user = (User) intent.getSerializableExtra("user");
                saveUser(user);
            }
            else if (resultCode == RESULT_CANCELED) {
                errorAlertBox().show();
            }
        }
    }

    private void saveUser(User user){
        new WritePrescriptionTask(user).execute(mDbHelper);
    }

    private AlertDialog errorAlertBox(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(DoseInputActivity.this);
        alertDialogBuilder
                .setMessage(R.string.error_registering)
                .setCancelable(true)
                .setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        new ReadPrescriptionTask().execute();
                    }
                });
        return alertDialogBuilder.create();
    }

    public void showRegularDose(View view){
        if(!HttpUtility.getHttpUtility().isConnectedToInternet(connectivityManager)){
            cannotVerifyDose().show();
        }
        breakthrough_dose.setBackgroundResource(R.drawable.button_opaque);
        regular_dose.setBackgroundResource(R.drawable.button_opaque_selected);
        Prescription prescription = user.getPrescription();
        String regularDrug = prescription.getMRDrug();
        String regularDose = prescription.getMRDose();
        String formulation = getFormulation(regularDrug);
        String date = prescription.getDate();
        centreMessageTitle.setText(regularDrug + " " + regularDose);
        centreMessage.setText("Take ONE " + formulation + " TWICE a day");
        centre_message_bottom.setText("(Prescribed on: " + date + ")");
    }

    public String getFormulation(String regularDrug){
        if(regularDrug.contains("Tablet")){
            return "tablet";
        }else if(regularDrug.contains("Capsule")){
            return "capsule";
        }
        return null;
    }

    public void showBreakthroughDose(View view){
        if(!HttpUtility.getHttpUtility().isConnectedToInternet(connectivityManager)){
            cannotVerifyDose().show();
        }
        regular_dose.setBackgroundResource(R.drawable.button_opaque);
        breakthrough_dose.setBackgroundResource(R.drawable.button_opaque_selected);
        Prescription prescription = user.getPrescription();
        String breakthroughDrug = prescription.getBreakthroughDrug();
        String breakthroughDose = prescription.getBreakthroughDose();
        String date = prescription.getDate();
        if(breakthroughDrug.contains("Oramorph")){
            breakthroughDrug = breakthroughDrug + " Solution";
            breakthroughDose = getOramorphDose(breakthroughDose);
        }else{
            breakthroughDose = "Take ONE tablet when required for breakthrough pain";
        }
        centreMessageTitle.setText(breakthroughDrug);
        centreMessage.setText(breakthroughDose);
        centre_message_bottom.setText("(Prescribed on: " + date + ")");
    }

    public String getOramorphDose(String breakthroughDose){
        double doseInMg = Double.parseDouble(breakthroughDose.substring(0,breakthroughDose.length() - 2));
        return "Take ONE " + doseInMg/2 + "ml dose when required for breakthrough pain";
    }

    public void addDose(View view){
        userInputDose = true;
        mostRecentDose = new Dose(new Timestamp(new DateTime().withZone(DateTimeZone.forID("Europe/London")).getMillis()), user.getHospitalNumber());
        sendDoseAlertBox();
    }

    private void sendDoseAlertBox(){
        final AlertDialog dialog = new AlertDialog.Builder(DoseInputActivity.this)
                .setTitle(R.string.dose_submitted)
                .setMessage(R.string.send_dose)
                .setNegativeButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int id) {
                        sendDose(dialogInterface);
                    }
                })
                .setPositiveButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int id) {
                        dialogInterface.cancel();
                    }
                }).create();
        setSendDoseAlertBoxButtons(dialog);
        dialog.show();
    }

    private void sendDose(DialogInterface dialogInterface){
        if (HttpUtility.getHttpUtility().isConnectedToInternet(connectivityManager)) {
            callSendDosesIntentService(true);
            //dialogInterface.cancel();
            //new AddDoseTask().execute();
        } else {
//            bottomMessage.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_wifi_black_48px, 0, 0, 0);
//            bottomMessage.setText((getString(R.string.doses_to_send)));
            setBottomMessage(true);
            saveDose(mostRecentDose);
            //dialogInterface.cancel();
        }
        dialogInterface.cancel();
        doseAcceptedToast();
    }

    private void setSendDoseAlertBoxButtons(final AlertDialog dialog){
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                positiveButton.setPadding(5, 10, 5, 10);
                Drawable positive = DoseInputActivity.this.getResources().getDrawable(R.drawable.cross);
                positive.setBounds((int) (positive.getIntrinsicWidth() * 0.5), 0, (int) (positive.getIntrinsicWidth() * 1.5), positive.getIntrinsicHeight());
                positiveButton.setCompoundDrawables(positive, null, null, null);
                Drawable negative = DoseInputActivity.this.getResources().getDrawable(R.drawable.tick_circle);
                negative.setBounds((int) (negative.getIntrinsicWidth() * 0.5), 0, (int) (negative.getIntrinsicWidth() * 1.5), negative.getIntrinsicHeight());
                negativeButton.setCompoundDrawables(negative, null, null, null);
                negativeButton.setPadding(5, 10, 5, 10);
            }
        });
    }

    private void doseAcceptedToast(){
        Toast toast = Toast.makeText(context, R.string.dose_submitted_success, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    private AlertDialog cannotVerifyDose(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(DoseInputActivity.this);
        alertDialogBuilder
                .setIcon(R.drawable.wifi)
                .setTitle(R.string.cannot_verify_dose_title)
                .setMessage(R.string.cannot_verify_dose)
                .setCancelable(true)
                .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
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
            if(user == null && HttpUtility.getHttpUtility().isConnectedToInternet(connectivityManager)){
                Intent registerActivity = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivityForResult(registerActivity, SET_UP_REQUEST);
            }else if(user == null){
                Intent setUpActivity = new Intent(getApplicationContext(), SetUpActivity.class);
                startActivityForResult(setUpActivity, SET_UP_REQUEST);
            }
//            else if(HttpUtility.getHttpUtility().isConnectedToInternet(connectivityManager)){
//                new AddDoseTask().execute();
//            }
        }
    }

//    private class ReadDosesTask extends AsyncTask<String, Void, Void> {
////        private MorphidoseContract morphidoseContract;
////        private SQLiteDatabase db;
////        private String[] projection;
////        private Cursor cursor;
////        private String hospitalNumber;
////        private Long date;
////        private List<Dose> doses;
////        private Dose latestDoseToRemove;
//
//        @Override
//        protected Void doInBackground(String... params) {
////            doses = new ArrayList<Dose>();
//            if(dosesInDatabase){
//                addSavedDosesToDosesToSend();
//            }
////            if(doses.size() > 0){ // on loading the app it will run this task to check if any doses need sending from the DB, if not doses.size = 0.
////                latestDoseToRemove = HttpUtility.getHttpUtility().sendDoses(doses);
////                if(latestDoseToRemove != null){
////                    deleteSentDosesFromDatabase(latestDoseToRemove);
////                }else{
////                    dosesInDatabase = true;
////                }
////            }else{
////                dosesInDatabase = false;
////            }
//            return null;
//        }
//
////        @Override
////        protected void onPostExecute(Void param) {
////            if(!dosesInDatabase) {
////                bottomMessage.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_check_circle, 0, 0, 0);
////                bottomMessage.setText(getString(R.string.all_doses_sent));
////            }else{
////                bottomMessage.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_wifi_black_48px, 0, 0, 0);
////                bottomMessage.setText(getString(R.string.doses_to_send));
////            }
////        }
//
////        private void addSavedDosesToDosesToSend(){
////            db = mDbHelper.getWritableDatabase();
////            morphidoseContract = new MorphidoseContract();
////            projection = morphidoseContract.getDoseProjectionValues();
////            cursor = db.query(MorphidoseContract.DoseEntry.TABLE_NAME, projection, null, null, null, null, null);
////            if (cursor != null && cursor.moveToFirst()){
////                do {
////                    date = cursor.getLong(0);
////                    hospitalNumber = cursor.getString(1);
////                    doses.add(new Dose(new Timestamp(date), hospitalNumber));
////                }while(cursor.moveToNext());
////                cursor.close();
////            }
////            db.close();
////        }
//    }


    private class AddDoseTask extends AsyncTask<String, Void, Void> {
        private MorphidoseContract morphidoseContract;
        private SQLiteDatabase db;
        private String[] projection;
        private Cursor cursor;
        private String hospitalNumber;
        private Long date;
        private List<Dose> doses;
        private Dose latestDoseToRemove;
        private long timeStarted;

        @Override
        protected void onPreExecute() {
            if(userInputDose){
                timeStarted = System.currentTimeMillis();
                pd = new ProgressDialog(context);
                pd.setTitle(R.string.sending_dose);
                pd.setCancelable(false);
                pd.setIndeterminate(true);
                pd.show();
            }
        }

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
                    saveDose(mostRecentDose); //there has been an error -> try again later when connected to the internet.
                }else if(HttpUtility.getHttpUtility().isConnectedToInternet(connectivityManager)){
                    new AddDoseTask().execute(); //clause added because when on reconnection to internet the first addDoseTask doesn't send the doses
                }
            }else{
                dosesInDatabase = false;
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

            if (pd!=null && userInputDose) {
                while(System.currentTimeMillis() < timeStarted + 2000){
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ex) {
                        Log.e("DoseInputActivity.AddDoseTask", "InterruptedException in onPostExecute", ex);
                    }
                }
                pd.dismiss();
            }
            if(userInputDose){
                doseAcceptedToast();
            }
            if(!dosesInDatabase) {
                bottomMessage.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_check_circle, 0, 0, 0);
                bottomMessage.setText((getString(R.string.all_doses_sent)));
            }else{
                bottomMessage.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_wifi_black_48px, 0, 0, 0);
                bottomMessage.setText((getString(R.string.doses_to_send)));
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

    public void callSendDosesIntentService(boolean userInputDose){
        Intent sendDosesIntent = new Intent(context, SendDosesIntentService.class);
        sendDosesIntent.putExtra(SendDosesIntentService.USER_INPUT_DOSE, userInputDose);
        sendDosesIntent.putExtra(SendDosesIntentService.MOST_RECENT_DOSE, mostRecentDose);
        sendDosesIntent.putExtra(SendDosesIntentService.DOSES_IN_DATABASE, dosesInDatabase);
        context.startService(sendDosesIntent);
    }

    public void setBottomMessage(boolean dosesInDatabase){
        if(!dosesInDatabase){
            bottomMessage.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_check_circle, 0, 0, 0);
            bottomMessage.setText((getString(R.string.all_doses_sent)));
        }else{
            bottomMessage.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_wifi_black_48px, 0, 0, 0);
            bottomMessage.setText((getString(R.string.doses_to_send)));
        }
    }

    public boolean dosesInDatabase(){
        return dosesInDatabase;
    }

    public void setDosesInDatabase(boolean dosesInDatabase){
        this.dosesInDatabase = dosesInDatabase;
        setBottomMessage(dosesInDatabase);
    }

    public void setUserInputDose(boolean userInputDose){
        this.userInputDose = userInputDose;
    }

    public Dose getMostRecentDose(){
        return mostRecentDose;
    }

//    public class NetworkReceiver extends BroadcastReceiver {
//        DoseInputActivity doseInputActivity;
//
//        public NetworkReceiver(DoseInputActivity doseInputActivity){
//            super();
//            this.doseInputActivity = doseInputActivity;
//        }
//
//        @Override
//        public void onReceive(Context context, Intent intent) {
////            if(created){
//                if (HttpUtility.getHttpUtility().isConnectedToInternet(connectivityManager) && doseInputActivity.dosesInDatabase()) {
//                    userInputDose = false;
//                    new AddDoseTask().execute();
//
//
//                    Intent sendDosesIntent = new Intent(context, SendDosesIntentService.class);
//                    sendDosesIntent.putExtra(SendDosesIntentService.USER_INPUT_DOSE, userInputDose);
//                    sendDosesIntent.putExtra(SendDosesIntentService.MOST_RECENT_DOSE, mostRecentDose);
//                    sendDosesIntent.putExtra(SendDosesIntentService.DOSES_IN_DATABASE, dosesInDatabase);
//                    startService(sendDosesIntent);
//                }
//            //}
//        }
//    }

}
