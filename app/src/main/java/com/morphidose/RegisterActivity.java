package com.morphidose;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class RegisterActivity extends Activity implements View.OnClickListener {
    private MorphidoseDbHelper mDbHelper;
    private ProgressDialog pd;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.register);
        Button submitHospitalNumber = (Button)findViewById(R.id.ButtonSubmitHospitalNumber);
        submitHospitalNumber.setOnClickListener(this);
        mDbHelper = new MorphidoseDbHelper(getApplicationContext());
    }

    @Override
    protected void onDestroy() {
        if (pd!=null) {
            pd.dismiss();
        }
        super.onDestroy();
    }

    @Override
    public void onClick(View view){
        final EditText hospitalNumberField = (EditText) findViewById(R.id.EditTextHospitalNumber);
        String hospitalNumber = hospitalNumberField.getText().toString();
        submitHospitalNumberAlert(hospitalNumber).show();
    }

    public AlertDialog submitHospitalNumberAlert(final String hospitalNumber){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(RegisterActivity.this);
        alertDialogBuilder
                .setMessage("Submit the hospital number " + hospitalNumber + "?")
                .setCancelable(false)
                .setPositiveButton(R.string.submit, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                        if (HttpUtility.getHttpUtility().isConnectedToInternet(connectivityManager)) {
                            new RegisterUserTask().execute(hospitalNumber);
                        } else {
                            noWIFIAlertBox().show();
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        return alertDialogBuilder.create();
    }

    public AlertDialog noWIFIAlertBox(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(RegisterActivity.this);
        alertDialogBuilder
                .setMessage(R.string.no_wifi)
                .setCancelable(true)
                .setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        return alertDialogBuilder.create();
    }

    public AlertDialog userNotFoundAlertBox(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(RegisterActivity.this);
        alertDialogBuilder
                .setMessage(R.string.user_not_found)
                .setCancelable(true)
                .setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        return alertDialogBuilder.create();
    }

    public AlertDialog prescriptionNotFoundAlertBox(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(RegisterActivity.this);
        alertDialogBuilder
                .setMessage(R.string.no_prescription)
                .setCancelable(true)
                .setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        return alertDialogBuilder.create();
    }

    private class RegisterUserTask extends AsyncTask<String, Void, User> {
        long timeStarted;

        @Override
        protected void onPreExecute() {
            timeStarted = System.currentTimeMillis();
            pd = new ProgressDialog(context);
            pd.setTitle(R.string.registering);
            pd.setMessage("Please wait.");
            pd.setCancelable(false);
            pd.setIndeterminate(true);
            pd.show();
        }

        @Override
        protected User doInBackground(String... params) {
            User user = new User(params[0], null);
            Prescription prescription = HttpUtility.getHttpUtility().getUserPrescription(user);
            if(prescription == null){ //user with this hospital number does not exist in server database
                return null;
            }
            user.setPrescription(prescription);
            return user;
        }

        @Override
        protected void onPostExecute(User user) {
            if (pd!=null) {
                while(System.currentTimeMillis() < timeStarted + 2000){
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ex) {
                        Log.e("ResgisterActivity.RegisterUserTask", "InterruptedException in onPostExecute", ex);
                    }
                }
                pd.dismiss();
            }
            if(user == null){
                userNotFoundAlertBox().show();
            }else if(isPrescriptionEmpty(user.getPrescription())){
                prescriptionNotFoundAlertBox().show();
            }else{
                saveUser(user);
                Intent doseInputActivity = new Intent(context, DoseInputActivity.class);
                doseInputActivity.putExtra(DoseInputActivity.USER, user);
                setResult(RESULT_OK, doseInputActivity);
                finish();
            }
        }
    }

    public boolean isPrescriptionEmpty(Prescription prescription){
        return prescription.getPrescriber().equals("");
    }

    private void saveUser(User user){
        new WritePrescriptionTask(user).execute(mDbHelper);
    }
}
