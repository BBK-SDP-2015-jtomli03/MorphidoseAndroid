package com.morphidose;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
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

import org.springframework.web.client.RestClientException;


public class SetUpActivity extends ActionBarActivity implements View.OnClickListener{
    private MorphidoseDbHelper mDbHelper;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        TextView newText = (TextView) findViewById(R.id.frontScreen);
        Button submitHospitalNumber = (Button)findViewById(R.id.ButtonSubmitHospitalNumber);
        submitHospitalNumber.setOnClickListener(this);
        mDbHelper = new MorphidoseDbHelper(getApplicationContext());
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
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(SetUpActivity.this);
        alertDialogBuilder
                .setMessage("Submit the hospital number " + hospitalNumber + "?")
                .setCancelable(false)
                .setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                        if (HttpUtility.getHttpUtility().isConnectedToInternet(cm)) {
                            new RegisterUserTask().execute(hospitalNumber);
                        } else {
                            noWIFIAlertBox().show();
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        return alertDialogBuilder.create();
    }

    public AlertDialog noWIFIAlertBox(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(SetUpActivity.this);
        alertDialogBuilder
                .setMessage("You currently have no internet connection. Please ensure you have a connection before trying again.")
                .setCancelable(true)
                .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        return alertDialogBuilder.create();
    }

    public AlertDialog userNotFoundAlertBox(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(SetUpActivity.this);
        alertDialogBuilder
                .setMessage("Sorry - we couldn't find that hospital number in the Morphidose system. Please check the number and try again.")
                .setCancelable(true)
                .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        return alertDialogBuilder.create();
    }

    public AlertDialog prescriptionNotFoundAlertBox(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(SetUpActivity.this);
        alertDialogBuilder
                .setMessage("You haven't been prescribed any medication on our system yet. Please check with your prescriber and try again.")
                .setCancelable(true)
                .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        return alertDialogBuilder.create();
    }

    private class RegisterUserTask extends AsyncTask<String, Void, User> {
        @Override
        protected User doInBackground(String... params) {
                User user = new User(params[0], null);
                Prescription prescription = HttpUtility.getHttpUtility().getUserPrescription(user);
                if(prescription == null){ //no user found
                    return null;
                }
                user.setPrescription(prescription);
                return user;
        }

        @Override
        protected void onPostExecute(User user) {
            if(user == null){
                userNotFoundAlertBox().show();
            }else if(isPrescriptionEmpty(user.getPrescription())){
                prescriptionNotFoundAlertBox().show();
            }else{
                saveUser(user); //in onPostExecute rather than doInBackground because need to handle above issues here as ptnotfound can't be handled correctly in the catch block.
                Intent doseInputActivity = new Intent(getApplicationContext(), DoseInputActivity.class);
                doseInputActivity.putExtra("user", user);
                //startActivity(doseInputActivity);
                setResult(RESULT_OK, doseInputActivity);
                finish();
            }
        }
    }

    private boolean isPrescriptionEmpty(Prescription prescription){
        return prescription.getPrescriber().equals("");
    }

    private void saveUser(User user){
            new WritePrescriptionTask(user).execute(mDbHelper);
    }

}
