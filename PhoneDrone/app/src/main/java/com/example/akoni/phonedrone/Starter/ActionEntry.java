package com.example.akoni.phonedrone.Starter;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.akoni.phonedrone.DroneReceive.DroneReceiver;
import com.example.akoni.phonedrone.DroneControl.Controller.DroneController;
import com.example.akoni.phonedrone.R;

public class ActionEntry extends AppCompatActivity{

    private Button control, receive;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_action_entry);

        control = findViewById(R.id.controlB);
        receive = findViewById(R.id.receiverB);

        control.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), DroneController.class);
                startActivity(i);
            }
        });

        receive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), DroneReceiver.class);
                startActivity(i);
            }
        });
    }

    @Override
    public void onBackPressed() {

        AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
        dlgAlert.setMessage("Are you sure you want to exit?\nWarning! Drone might lost control!");
        dlgAlert.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                        moveTaskToBack(true);
                    }
                });
        dlgAlert.setCancelable(true);
        dlgAlert.create().show();
    }
}
