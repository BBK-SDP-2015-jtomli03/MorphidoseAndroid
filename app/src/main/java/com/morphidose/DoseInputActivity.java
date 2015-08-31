package com.morphidose;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;


public class DoseInputActivity extends Activity{
    TextView welcome;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dose_input_view);
        Intent intent = getIntent();
        User user = (User) intent.getSerializableExtra("user");
        welcome = (TextView) findViewById(R.id.dose_input_view);
        if(user == null){
            welcome.setText("@string/hello" + "null" );

        }else{
            welcome.setText("@string/hello" + " " + user.getPrescription().getMRDrug());
        }
    }
}
