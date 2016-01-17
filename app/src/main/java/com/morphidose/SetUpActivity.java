package com.morphidose;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class SetUpActivity extends Activity implements View.OnClickListener{
    static final int SET_UP_REQUEST = 0;
    private ConnectivityManager connectivityManager;
    NetworkReceiver receiver;
    private boolean created = false;
    SetUpActivity self = this;
    Button goToRegisterUserView;
    TextView continueMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        receiver = new NetworkReceiver();
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(receiver, filter);
        goToRegisterUserView = (Button)findViewById(R.id.GoToRegisterUserView);
        goToRegisterUserView.setVisibility(View.INVISIBLE);
        continueMessage = (TextView)findViewById(R.id.continue_message);
        continueMessage.setVisibility(View.INVISIBLE);
        created = true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view){
        Intent registerActivity = new Intent(getApplicationContext(), RegisterActivity.class);
        startActivityForResult(registerActivity, SET_UP_REQUEST);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (receiver != null) {
            this.unregisterReceiver(receiver);
        }
    }

    public class NetworkReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(created){
                if (HttpUtility.getHttpUtility().isConnectedToInternet(connectivityManager)) {
                    goToRegisterUserView.setOnClickListener(self);
                    goToRegisterUserView.setVisibility(View.VISIBLE);
                    continueMessage.setVisibility(View.VISIBLE);
                }
            }
        }
    }

}
