package com.example.akoni.phonedrone.DroneControl.ControlViews;

import android.content.Context;
import android.graphics.Camera;
import android.view.SurfaceView;
import android.view.SurfaceHolder;

public class DroneView extends SurfaceView implements SurfaceHolder.Callback {

    public DroneView(Context context){
        super(context);
        getHolder().addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }


}
