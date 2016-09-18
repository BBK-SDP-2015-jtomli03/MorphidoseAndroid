package com.morphidose;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
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


public class DoseInputActivity extends Activity implements Serializable{
    private static final long serialVersionUID = 1L;
    private static final String DOSES_IN_DATABASE = "dosesInDatabase";
    private static final String REGULAR = "regular";
    public static final int SET_UP_REQUEST = 2;
    public static final String USER = "user";
    private static final String LOG_DOSES_MESSAGE = "logDosesMessage";
    private MorphidoseDbHelper mDbHelper;
    private ConnectivityManager connectivityManager;
    private NetworkReceiver receiver;
    private Intent sendDosesIntent;
    private Context context;
    private User user;
    private Dose mostRecentDose;
    private boolean registered = false;
    private boolean dosesInDatabase = false;
    private boolean recieverRegistered = false;
    private boolean latestPrescriptionReceived = false;
    private boolean regularDose = false;
    private boolean logDosesMessageDisplayed = true;
    private boolean created;
    private boolean userInput = true;
    private TextView centreMessage;
    private TextView centreMessageTitle;
    private TextView centre_message_bottom;
    private TextView bottomMessage;
    private Button breakthrough_dose;
    private Button regular_dose;
    private SharedPreferences sharedPreferences;
    private long timeStarted;
    private ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        context = this;
        mDbHelper = new MorphidoseDbHelper(getApplicationContext());
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        receiver = new NetworkReceiver();
        sendDosesIntent = new Intent(this, SendDosesIntentService.class);
        sharedPreferences = getPreferences(Context.MODE_PRIVATE);
        created = true;
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
        latestPrescriptionReceived = false;
        created = false;
    }

    @Override
    public void onStart(){
        super.onStart();
        if(user == null){
            new ReadPrescriptionTask().execute();
        }else if(!latestPrescriptionReceived && HttpUtility.getHttpUtility().isConnectedToInternet(connectivityManager)){
            new GetLatestPrescriptionTask().execute(false);
            loadPage();
        }
        else if(!created){
            loadPage();
        }
    }

    @Override
    public void onPause(){
        super.onPause();
        if(!dosesInDatabase && recieverRegistered) {
            unregisterNetworkReceiver();
        }
        if(registered){
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(DOSES_IN_DATABASE, dosesInDatabase);
            editor.putBoolean(REGULAR, regularDose);
            editor.putBoolean(LOG_DOSES_MESSAGE, logDosesMessageDisplayed);
            editor.commit();
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        if(!recieverRegistered) {
            recieverRegistered = registerNetworkReceiver();
        }
    }

    public void loadPage(){
        setContentView(R.layout.dose_input_view);
        initialiseViewVariables();
        if(registered) {
            setSavedView();
        }else{
            setInitialView();
        }
    }

    public void setSavedView(){
        getSharedPreferencesAndSetVariables();
        setBottomMessage(dosesInDatabase);
        if(logDosesMessageDisplayed || !created){
            logDosesMessageDisplayed = true;
            centreMessage.setText(R.string.log_doses);
        }
        else if(regularDose){
            userInput = false;
            showRegularDose(regular_dose);
        }else{
            userInput = false;
            showBreakthroughDose(breakthrough_dose);
        }
    }

    public void setInitialView(){
        centreMessageTitle.setText(R.string.registration_successful);
        centreMessage.setText(R.string.dose_intro);
        bottomMessage.setText(R.string.log_dose);
    }

    public void getSharedPreferencesAndSetVariables(){
        dosesInDatabase = sharedPreferences.getBoolean(DOSES_IN_DATABASE, false);
        regularDose = sharedPreferences.getBoolean(REGULAR, false);
        logDosesMessageDisplayed = sharedPreferences.getBoolean(LOG_DOSES_MESSAGE, true);
    }

    public void initialiseViewVariables(){
        centreMessageTitle = (TextView)findViewById(R.id.centre_message_title);
        centreMessage = (TextView)findViewById(R.id.centre_message);
        centre_message_bottom = (TextView)findViewById(R.id.centre_message_bottom);
        bottomMessage = (TextView)findViewById(R.id.bottom_message);
        breakthrough_dose = (Button)findViewById(R.id.breakthrough_dose);
        regular_dose = (Button)findViewById(R.id.regular_dose);
    }

    public Boolean registerNetworkReceiver(){
        IntentFilter filter = new IntentFilter(SendDosesIntentService.BROADCAST_MESSAGE);
        registerReceiver(receiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        registerReceiver(receiver, filter);
        return true;
    }

    public void unregisterNetworkReceiver(){
        this.unregisterReceiver(receiver);
        recieverRegistered = false;
    }

    public void setUser(User user){
        this.user = user;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode == RESULT_OK) {
            User registeredUser = (User) intent.getSerializableExtra(USER);
            setUser(registeredUser);
            loadPage();
            registered = true;
            latestPrescriptionReceived = true;
            created = true;
        }
        else if (resultCode == RESULT_CANCELED) {
            errorAlertBox().show();
        }
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
        regularDose = true;
        logDosesMessageDisplayed = false;
        checkForLatestPrescription();
        breakthrough_dose.setBackgroundResource(R.drawable.button_opaque);
        regular_dose.setBackgroundResource(R.drawable.button_opaque_selected);
        Prescription prescription = user.getPrescription();
        String regularDrug = prescription.getMRDrug();
        String regularDose = getRegularDose(prescription);
        String formulation = getFormulation(regularDrug);
        String date = prescription.getDate();
        centreMessageTitle.setText(regularDrug + " " + regularDose);
        if(formulation != null){
            centreMessage.setText("Take ONE " + formulation + " TWICE a day");
        }else{
            centreMessage.setText(R.string.formulation_error);
        }
        centre_message_bottom.setText("(Prescribed on: " + date + ")");
    }

    private void checkForLatestPrescription(){
        if(userInput && !created){
            if(!latestPrescriptionReceived){
                if(HttpUtility.getHttpUtility().isConnectedToInternet(connectivityManager)){
                    new GetLatestPrescriptionTask().execute(true);
                    timeStarted = System.currentTimeMillis();
                    pd = new ProgressDialog(context);
                    pd.setTitle(R.string.getting_doses);
                    pd.setMessage("Please wait.");
                    pd.setCancelable(false);
                    pd.setIndeterminate(true);
                    pd.show();
                }else{
                    cannotVerifyDose().show();
                }
            }
        }else{
            userInput = true; //reset to true as this is just a screen rotation
        }
    }

    public String getRegularDose(Prescription prescription){
        String regularDose = prescription.getMRDose();
        if(regularDose.contains(".0mg")){
            regularDose = regularDose.replace(".0", "");
        }
        return regularDose;
    }

    public String getFormulation(String regularDrug){
        regularDrug = regularDrug.toLowerCase();
        if(regularDrug.contains("tablet")){
            return "tablet";
        }else if(regularDrug.contains("capsule")){
            return "capsule";
        }else if(regularDrug.contains("suspension")){
            return "sachet as directed";
        }
        return null;
    }

    public void showBreakthroughDose(View view){
        regularDose = false;
        logDosesMessageDisplayed = false;
        checkForLatestPrescription();
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
            breakthroughDrug = breakthroughDrug + " " + breakthroughDose;
            breakthroughDose = "Take ONE tablet when required for breakthrough pain";
        }
        centreMessageTitle.setText(breakthroughDrug);
        centreMessage.setText(breakthroughDose);
        centre_message_bottom.setText("(Prescribed on: " + date + ")");
    }

    public String getOramorphDose(String breakthroughDose){
        Double doseInMg = Double.parseDouble(breakthroughDose.substring(0,breakthroughDose.length() - 2))/2;
        Number doseToReturn;
        if(doseInMg % Math.floor(doseInMg) == 0){
            doseToReturn = Math.round(doseInMg);
        }else{
            doseToReturn = doseInMg;
        }
        return "Take ONE " + doseToReturn + "ml dose when required for breakthrough pain";
    }

    public void addDose(View view){
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
        } else {
            setBottomMessage(true);
            saveDose(mostRecentDose);
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

    public void callSendDosesIntentService(boolean userInputDose){
        sendDosesIntent.putExtra(SendDosesIntentService.USER_INPUT_DOSE, userInputDose);
        sendDosesIntent.putExtra(SendDosesIntentService.MOST_RECENT_DOSE, mostRecentDose);
        sendDosesIntent.putExtra(SendDosesIntentService.DOSES_IN_DATABASE, dosesInDatabase);
        dosesInDatabase = false; //set here as a check to see whether anything has changed since SendDosesIntentService was called & the return of dosesInDatabase from the service -> if returned value = false but this instance = true, then dosesInDatabase = true.
        startService(sendDosesIntent);
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

    private class GetLatestPrescriptionTask extends AsyncTask<Boolean, Void, User> {
        @Override
        protected User doInBackground(Boolean... params) {
            Prescription prescription = HttpUtility.getHttpUtility().getUserPrescription(user);
            if(prescription == null){ //error
                return null;
            }
            user.setPrescription(prescription);
            return user;
        }

        @Override
        protected void onPostExecute(User user) {
            if(user != null){
                latestPrescriptionReceived = true;
                saveUser(user);
                dismissProgressDialog();
            }else if(HttpUtility.getHttpUtility().isConnectedToInternet(connectivityManager)){ // bug with Spring android therefore on reconnnection to internet have to make call twice to succeed
                new GetLatestPrescriptionTask().execute();
            }
        }
    }

    public void dismissProgressDialog(){
        if (pd!=null) {
            while(System.currentTimeMillis() < timeStarted + 2000){
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    Log.e("InterruptedException", "In GetLatestPrescriptionTask onPostExecute", ex);
                }
            }
            pd.dismiss();
        }
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
                setUser(new User(hospitalNumber, prescription));
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
            }else{
                if(HttpUtility.getHttpUtility().isConnectedToInternet(connectivityManager) && !created){ //!created, ie. not just a screen rotation
                    new GetLatestPrescriptionTask().execute(false);
                }
                registered = true;
                loadPage();
            }
        }
    }

    private void saveDose(Dose dose){
        new WriteDoseTask(dose).execute(mDbHelper);
        dosesInDatabase = true;
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

    public void setDosesInDatabase(boolean dosesInDatabase){
        this.dosesInDatabase = dosesInDatabase;
        setBottomMessage(dosesInDatabase);
    }

    private void saveUser(User user){
        new WritePrescriptionTask(user).execute(mDbHelper);
    }

    public class NetworkReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(registered){
                if(intent.getAction().equals(SendDosesIntentService.BROADCAST_MESSAGE)) {
                    final boolean dosesInDatabaseReturned = intent.getExtras().getBoolean(SendDosesIntentService.RESULT);
                    setDosesInDatabase(dosesInDatabase || dosesInDatabaseReturned);
                }
                else if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION) && HttpUtility.getHttpUtility().isConnectedToInternet(connectivityManager) && dosesInDatabase) {
                    callSendDosesIntentService(false);
                }
            }
        }
    }

}
