package com.example.akoni.phonedrone.DroneControl.ControlViews;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

public class JoyStickView extends SurfaceView implements SurfaceHolder.Callback, View.OnTouchListener{
    private float centerX;
    private float centerY;
    private float baseRadius;
    private float hatRadius;
    private JoyStickListener joyStickCallback;

    public JoyStickView(Context context){
        super(context);
        getHolder().addCallback(this);
        setOnTouchListener(this);

        /*setZOrderOnTop(true);
        getHolder().setFormat(PixelFormat.TRANSPARENT);*/

        if(context instanceof JoyStickListener){
            joyStickCallback = (JoyStickListener)context;
        }
    }

    public JoyStickView(Context context, AttributeSet attributeSet, int style){
        super(context, attributeSet, style);
        getHolder().addCallback(this);
        setOnTouchListener(this);

        if(context instanceof JoyStickListener){
            joyStickCallback = (JoyStickListener)context;
        }
    }

    public JoyStickView(Context context, AttributeSet attributeSet){
        super(context, attributeSet);
        getHolder().addCallback(this);
        setOnTouchListener(this);

        if(context instanceof JoyStickListener){
            joyStickCallback = (JoyStickListener)context;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder){
        setupDimensions();
        drawJoyStick(centerX, centerY);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        final float displacement;
        final float constrainX;
        final float constrainY;
        final float ratio;
        if(v.equals(this)){
            displacement = (float) Math.sqrt(Math.pow(event.getX() - centerX, 2) + Math.pow(event.getY() - centerY, 2));
            if(event.getAction() != MotionEvent.ACTION_UP){
                if(displacement < baseRadius) {
                    drawJoyStick(event.getX(), event.getY());
                    joyStickCallback.onJoyStickMoved((event.getX() - centerX)/baseRadius, (event.getY() - centerY)/baseRadius, getId());
                }
                else {
                    ratio = baseRadius / displacement;
                    constrainX = centerX + (event.getX() - centerX) * ratio;
                    constrainY = centerY + (event.getY() - centerY) * ratio;

                    drawJoyStick(constrainX, constrainY);
                    joyStickCallback.onJoyStickMoved((constrainX - centerX)/baseRadius, (constrainY - centerY)/baseRadius, getId());
                }
            }
            else if(event.getAction() == MotionEvent.ACTION_UP){
                drawJoyStick(centerX, centerY);
                joyStickCallback.onJoyStickMoved(0, 0, getId());
            }
        }

        return true;
    }

    public void setupDimensions(){
        centerX = getWidth() / 2;
        centerY = getHeight() / 2;
        baseRadius = Math.min(getWidth(), getHeight()) / 3;
        hatRadius = Math.min(getWidth(), getHeight()) / 6;
    }

    public void drawJoyStick(float newX, float newY){

        if(getHolder().getSurface().isValid()) {
            Canvas canvas = this.getHolder().lockCanvas();
            Paint color = new Paint();

            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

            //base Circle
            color.setARGB(100, 50, 50, 50);
            canvas.drawCircle(centerX, centerY, baseRadius, color);

            //top Circle
            color.setARGB(255, 0, 0, 255);
            canvas.drawCircle(newX, newY, hatRadius, color);

            getHolder().unlockCanvasAndPost(canvas);
        }
    }

    public interface JoyStickListener{
        void onJoyStickMoved(float xPercent, float yPercent, int source);
    }
}
