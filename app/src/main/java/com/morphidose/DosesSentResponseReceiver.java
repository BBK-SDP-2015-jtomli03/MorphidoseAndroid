package com.morphidose;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.TextView;

public class DosesSentResponseReceiver extends BroadcastReceiver {
        public static final String BROADCAST_MESSAGE = "com.morphidose.BROADCAST_MESSAGE";

        @Override
        public void onReceive(Context context, Intent intent) {
//            if(intent.getAction() == BROADCAST_MESSAGE) {
//                boolean dosesInDatabase = intent.getExtras().getBoolean(SendDosesIntentService.RESULT);
//                if(!dosesInDatabase) {
//                    bottomMessage.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_check_circle, 0, 0, 0);
//                    bottomMessage.setText((getString(R.string.all_doses_sent)));
//                }
//                else {
//                    bottomMessage.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_wifi_black_48px, 0, 0, 0);
//                    bottomMessage.setText((getString(R.string.doses_to_send)));
//                }
//                TextView result = (TextView) findViewById(R.id.txt_result);
//                result.setText(text);
//            }
        }
    }

