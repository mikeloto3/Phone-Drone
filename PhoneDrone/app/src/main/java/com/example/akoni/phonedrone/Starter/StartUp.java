package com.example.akoni.phonedrone.Starter;

import android.content.Intent;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.akoni.phonedrone.CameraActivity;
import com.example.akoni.phonedrone.ConnectP2P.ConnectDevice;
import com.example.akoni.phonedrone.R;
import com.example.akoni.phonedrone.Starter.ActionEntry;

public class StartUp extends AppCompatActivity {

    private ImageView imageView;
    private ProgressBar progressBar;
    private TextView textConnect, textPhone, textDrone;
    private CountDownTimer timer1, timer2;
    private RelativeLayout relativeLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_up);

//        imageView = findViewById(R.id.startImage);
        textPhone = findViewById(R.id.textPhone);
        textDrone = findViewById(R.id.textDrone);
        textConnect = findViewById(R.id.loadView);
        progressBar = findViewById(R.id.progressBar);
        relativeLayout = findViewById(R.id.startLayout);

        timer1 = new CountDownTimer(3000,3000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {

                //imageView.setVisibility(View.INVISIBLE);
                textDrone.setVisibility(View.INVISIBLE);
                textPhone.setVisibility(View.INVISIBLE);
                textConnect.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.VISIBLE);
                progressBar.setProgress(0);
                relativeLayout.setBackgroundColor(Color.WHITE);

                timer2.start();
            }
        };

        timer2 = new CountDownTimer(3000,3000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {

                Intent i = new Intent(getApplicationContext(), ConnectDevice.class);
                startActivity(i);
                /*Intent i = new Intent(getBaseContext(), ActionEntry.class);
                startActivity(i);*/
            }
        };

        timer1.start();

    }
}
