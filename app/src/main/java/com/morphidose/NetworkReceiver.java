package com.morphidose;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;

public class NetworkReceiver extends BroadcastReceiver {
    public static final String BROADCAST_MESSAGE = "com.morphidose.BROADCAST_MESSAGE";
    private DoseInputActivity doseInputActivity;
    private ConnectivityManager connectivityManager;


    public NetworkReceiver(DoseInputActivity doseInputActivity, ConnectivityManager connectivityManager){
        super();
        this.doseInputActivity = doseInputActivity;
        this.connectivityManager = connectivityManager;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals(BROADCAST_MESSAGE)) {
            boolean dosesInDatabase = intent.getExtras().getBoolean(SendDosesIntentService.RESULT);
            doseInputActivity.setDosesInDatabase(dosesInDatabase);
        }else if (HttpUtility.getHttpUtility().isConnectedToInternet(connectivityManager) && doseInputActivity.dosesInDatabase()) {
            doseInputActivity.callSendDosesIntentService(false);
        }
    }
}
