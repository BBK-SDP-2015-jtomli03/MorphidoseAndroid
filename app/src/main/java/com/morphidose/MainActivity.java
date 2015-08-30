package com.morphidose;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;


public class MainActivity extends ActionBarActivity implements View.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView newText = (TextView) findViewById(R.id.frontScreen);
        Button submitHospitalNumber = (Button)findViewById(R.id.ButtonSubmitHospitalNumber);
        submitHospitalNumber.setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
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
        final EditText hospitalNumberField = (EditText) findViewById(R.id.EditTextHospitalNumber);
        String hospitalNumber = hospitalNumberField.getText().toString();
        createAlertBox(hospitalNumber).show();
    }

    public AlertDialog createAlertBox(final String hospitalNumber){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
        alertDialogBuilder
                .setMessage("Submit the hospital number " + hospitalNumber + "?")
                .setCancelable(false)
                .setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        new RegisterUserTask().execute(hospitalNumber);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        return alertDialogBuilder.create();
    }

    private class RegisterUserTask extends AsyncTask<String, Void, User> {
        @Override
        protected User doInBackground(String... params) {
            try {
                String hospitalNumber = "{\"hospitalNumber\":\"" + params[0] + "\"}";
                HttpHeaders requestHeaders = new HttpHeaders();
                requestHeaders.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<String> requestEntity = new HttpEntity<String>(hospitalNumber, requestHeaders);
                final String url = "http://192.168.1.69:9000/patient/prescription";
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
                Prescription prescription = restTemplate.getForObject(url, Prescription.class, requestEntity);
                return new User(hospitalNumber, prescription);
            } catch (RestClientException e) {
                Log.e("MainActivity", e.getMessage(), e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(User user) {

                saveUser(user);
        }

    }

    private void saveUser(User user){
        //confirmation -> store data -> go to new view
        //handle if no user returned ?null returned -> ie show "user not found" msge and return to input
        //error -> back to first screen with error message
        Intent doseInputActivity = new Intent(getApplicationContext(), DoseInputActivity.class);
        //doseInputActivity.putExtra("user", user);
        startActivity(doseInputActivity);
    }
}
