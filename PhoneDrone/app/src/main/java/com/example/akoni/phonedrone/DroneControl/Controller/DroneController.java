package com.example.akoni.phonedrone.DroneControl.Controller;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.widget.TextView;
import android.widget.Toast;

import com.example.akoni.phonedrone.ConnectP2P.ConnectDevice;
import com.example.akoni.phonedrone.DroneControl.ControlViews.DroneView;
import com.example.akoni.phonedrone.DroneControl.ControlViews.JoyStickView;
import com.example.akoni.phonedrone.R;
import com.example.akoni.phonedrone.Threads.P2PConnectionThreads.ServerThread;

public class DroneController extends AppCompatActivity implements JoyStickView.JoyStickListener{

    //to get X and Y only for both joysticks
    private TextView leftX, leftY, rightX, rightY;
    private ServerThread serverThread;
    private static final int port = 8888;
    private ConnectDevice device;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drone_controller);

        serverThread = new ServerThread(port);
        new Thread(serverThread).start();

        //DroneView droneView = new DroneView(this);
        JoyStickView joyStickLeft = findViewById(R.id.joyStickLeft);
        JoyStickView joyStickRight = findViewById(R.id.joyStickRight);

        //left
        joyStickLeft.setZOrderOnTop(true);
        SurfaceHolder holderLeft = joyStickLeft.getHolder();
        holderLeft.setFormat(PixelFormat.TRANSLUCENT);

        //right
        joyStickRight.setZOrderOnTop(true);
        SurfaceHolder holderRight = joyStickRight.getHolder();
        holderRight.setFormat(PixelFormat.TRANSLUCENT);

        leftX = findViewById(R.id.leftX);
        leftY = findViewById(R.id.leftY);

        rightX = findViewById(R.id.rightX);
        rightY = findViewById(R.id.rightY);

    }

    @Override
    public void onJoyStickMoved(float xPercent, float yPercent, int source) {
        switch (source){
            case R.id.joyStickLeft:
                leftX.setText("" + xPercent);
                leftY.setText("" + yPercent);
                //Log.d("Main Method", "Left " + "X Percent " + xPercent + "Y Percent " + yPercent);
                break;
            case R.id.joyStickRight:
                rightX.setText("" + xPercent);
                rightY.setText("" + yPercent);
                Log.d("Main Method", "Right " + "X Percent " + xPercent + "Y Percent " + yPercent);
                break;
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
        dlgAlert.setMessage("Are you sure you want to change action?");
        dlgAlert.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
        dlgAlert.setCancelable(true);
        dlgAlert.create().show();
    }
}
