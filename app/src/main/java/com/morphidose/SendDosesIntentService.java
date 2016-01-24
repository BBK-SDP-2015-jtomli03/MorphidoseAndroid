package com.morphidose;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.support.v4.content.LocalBroadcastManager;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class SendDosesIntentService extends IntentService {
    public static final String BROADCAST_MESSAGE = "com.morphidose.BROADCAST_MESSAGE";
    public static final String USER_INPUT_DOSE = "userInputDose";
    public static final String MOST_RECENT_DOSE = "mostRecentDose";
    public static final String DOSES_IN_DATABASE = "dosesInDatabase";
    public static final String RESULT = "result";
    private MorphidoseContract morphidoseContract;
    private MorphidoseDbHelper mDbHelper;
    private ConnectivityManager connectivityManager;
    private SQLiteDatabase db;
    private String[] projection;
    private Cursor cursor;
    private String hospitalNumber;
    private Long date;
    private List<Dose> doses;
    private Dose latestDoseToRemove;
    private boolean userInputDose;
    private Dose mostRecentDose;
    private boolean dosesInDatabase;

    public SendDosesIntentService() {
        super("SendDosesIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        userInputDose = intent.getExtras().getBoolean(USER_INPUT_DOSE);
        mostRecentDose = (Dose) intent.getSerializableExtra(MOST_RECENT_DOSE);
        dosesInDatabase = intent.getExtras().getBoolean(DOSES_IN_DATABASE);
        mDbHelper = new MorphidoseDbHelper(getApplicationContext());
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        sendDoses();
        Intent broadcastIntent = new Intent(BROADCAST_MESSAGE);
        //broadcastIntent.setAction(DosesSentResponseReceiver.ACTION_RESP);
        //broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.putExtra(RESULT, dosesInDatabase);
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
        //sendBroadcast(broadcastIntent);
    }

    private void sendDoses(){
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
                sendDoses(); //there has been an error -> try again
            }else if(userInputDose){
                saveDose(mostRecentDose); //there has been an error -> try again later when connected to the internet.
            }
            else if(HttpUtility.getHttpUtility().isConnectedToInternet(connectivityManager)){
                sendDoses(); //clause added because when on reconnection to internet the first call to sendDoses doesn't send the doses
            }
        }else{
            dosesInDatabase = false;
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

    private void deleteSentDosesFromDatabase(Dose dose){
        new DeleteDoseTask(dose).execute(mDbHelper);
        dosesInDatabase = false;
    }

    private void saveDose(Dose dose){
        new WriteDoseTask(dose).execute(mDbHelper);
        dosesInDatabase = true;
    }
}

