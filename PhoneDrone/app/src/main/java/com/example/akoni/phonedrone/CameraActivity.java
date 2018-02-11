package com.example.akoni.phonedrone;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SurfaceHolder;

import net.majorkernelpanic.streaming.Session;
import net.majorkernelpanic.streaming.SessionBuilder;
import net.majorkernelpanic.streaming.audio.AudioQuality;
import net.majorkernelpanic.streaming.gl.SurfaceView;
import net.majorkernelpanic.streaming.rtsp.RtspClient;
import net.majorkernelpanic.streaming.video.VideoQuality;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CameraActivity extends AppCompatActivity implements RtspClient.Callback, Session.Callback, SurfaceHolder.Callback{

    private RtspClient rtspClient;
    private Session session;
    private SurfaceView surfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        surfaceView = findViewById(R.id.mSurfaceView);
        surfaceView.getHolder().addCallback(this);

        initialize_RTSP();
    }

    public void initialize_RTSP(){
        session = SessionBuilder.getInstance()
                .setCallback(this)
                .setSurfaceView(surfaceView)
                .setPreviewOrientation(90)
                .setContext(getApplicationContext())
                .setAudioEncoder(SessionBuilder.AUDIO_NONE)
                .setAudioQuality(new AudioQuality(16000, 32000))
                .setVideoEncoder(SessionBuilder.VIDEO_H264)
                .setVideoQuality(new VideoQuality(320,240,20,500000))
                .build();

        //rtsp client configuration
        rtspClient = new RtspClient();
        rtspClient.setSession(session);
        rtspClient.setCallback(this);
        surfaceView.setAspectRatioMode(SurfaceView.ASPECT_RATIO_PREVIEW);

        final String port, ip, path;

        Pattern url = Pattern.compile("rtps://(.+):(\\d+)/(.+)");
        Matcher matcher = url.matcher("rtsp://192.168.2:1935/live/myStream");
        matcher.find();

        ip = matcher.group(1);
        port = matcher.group(2);
        path = matcher.group(3);

        rtspClient.setStreamPath("/" + path);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onPreviewStarted() {

    }

    @Override
    public void onSessionConfigured() {

    }

    @Override
    public void onSessionStarted() {

    }

    @Override
    public void onSessionStopped() {

    }

    @Override
    public void onBitrateUpdate(long bitrate) {

    }

    @Override
    public void onSessionError(int reason, int streamType, Exception e) {
        switch (reason){
            case Session.ERROR_CAMERA_ALREADY_IN_USE:
                break;
            case Session.ERROR_CONFIGURATION_NOT_SUPPORTED:
                break;
            case Session.ERROR_INVALID_SURFACE:
                break;
            case Session.ERROR_CAMERA_HAS_NO_FLASH:
                break;
            case Session.ERROR_STORAGE_NOT_READY:
                break;
            case Session.ERROR_OTHER:
                break;
        }

        if (e != null){
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        session.startPreview();
        surfaceView.startGLThread();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    public void onRtspUpdate(int message, Exception exception) {
        switch (message){
            case RtspClient.ERROR_CONNECTION_FAILED:
                break;
            case RtspClient.ERROR_WRONG_CREDENTIALS:
                break;
        }
    }
}
