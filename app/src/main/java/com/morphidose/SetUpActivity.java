package com.morphidose;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class SetUpActivity extends Activity implements View.OnClickListener{
    static final int SET_UP_REQUEST = 0;
    private ConnectivityManager connectivityManager;
    NetworkReceiver receiver;
    SetUpActivity self = this;
    Button goToRegisterUserView;
    TextView continueMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        receiver = new NetworkReceiver();
        goToRegisterUserView = (Button)findViewById(R.id.GoToRegisterUserView);
        goToRegisterUserView.setVisibility(View.INVISIBLE);
        continueMessage = (TextView)findViewById(R.id.continue_message);
        continueMessage.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onClick(View view){
        Intent registerActivity = new Intent(getApplicationContext(), RegisterActivity.class);
//        startActivityForResult(registerActivity, SET_UP_REQUEST);
        startActivity(registerActivity);
        finish();
    }

    @Override
    public void onPause(){
        super.onPause();
        if (receiver != null) {
            this.unregisterReceiver(receiver);
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        registerReceiver(receiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public class NetworkReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
              if (HttpUtility.getHttpUtility().isConnectedToInternet(connectivityManager)) {
                  goToRegisterUserView.setOnClickListener(self);
                  goToRegisterUserView.setVisibility(View.VISIBLE);
                  continueMessage.setVisibility(View.VISIBLE);
              }
        }
    }

}
