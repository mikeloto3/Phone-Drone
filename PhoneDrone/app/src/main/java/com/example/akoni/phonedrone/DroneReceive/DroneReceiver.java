package com.example.akoni.phonedrone.DroneReceive;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Camera;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ImageReader;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.akoni.phonedrone.Interfaces.CameraSupport;
import com.example.akoni.phonedrone.R;

import net.majorkernelpanic.streaming.Session;
import net.majorkernelpanic.streaming.rtsp.RtspClient;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class DroneReceiver extends AppCompatActivity{

    private TextureView textureView;
    private TextureView.SurfaceTextureListener textureListener;

    private CameraDevice cameraDevice;
    private CameraDevice.StateCallback cameraCallBack;
    private String cameraId;

    private HandlerThread thread;
    private Handler handler;

    private static SparseIntArray ORIENTATIONS = new SparseIntArray();

    private Size previewSize;
    private Size imageSize;
    private ImageReader imageReader;
    private ImageReader.OnImageAvailableListener imageAvailableListener;

    private static int CAMERA_REQUEST_PERMISSION = 0;
    private static final int STATE_PREVIEW = 0;
    private static final int STATE_WAIT_LOCK = 1;
    private int CAPTURE_STATE = STATE_PREVIEW;

    private CaptureRequest.Builder builder;
    private CameraCaptureSession previewCaptureSession;
    private CameraCaptureSession.CaptureCallback captureCallback;

    private ImageButton videoButton;
    private boolean isRecording = false;
    private File videoFolder;
    private String videoFileName;
    private static int VIDEO_REQUEST = 1;

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 0);
        ORIENTATIONS.append(Surface.ROTATION_90, 90);
        ORIENTATIONS.append(Surface.ROTATION_180, 180);
        ORIENTATIONS.append(Surface.ROTATION_270, 270);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drone_receiver);

        textureView = findViewById(R.id.textureViewReceiver);
        videoButton = findViewById(R.id.videoButton);

        createVideoFolder();

        videoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isRecording){
                    isRecording = false;
                    videoButton.setImageResource(R.mipmap.video);
                }
                else {
                    isRecording = true;
                    videoButton.setImageResource(R.mipmap.video_busy);
                }
            }
        });

        textureListener = new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                setUpCamera(width, height);
                connectCamera();
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {

            }
        };

        cameraCallBack = new CameraDevice.StateCallback() {
            @Override
            public void onOpened(@NonNull CameraDevice camera) {
                cameraDevice = camera;
                startPreview();
                //Toast.makeText(getApplicationContext(), "Camera connected!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDisconnected(@NonNull CameraDevice camera) {
                camera.close();
                cameraDevice = null;
            }

            @Override
            public void onError(@NonNull CameraDevice camera, int error) {
                camera.close();
                cameraDevice = null;
            }
        };

        imageAvailableListener = new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader reader) {

            }
        };

        captureCallback = new CameraCaptureSession.CaptureCallback() {

            private void process(CaptureResult captureResult){
                switch (CAPTURE_STATE){
                    case STATE_PREVIEW:
                        //do nothing
                        break;
                    case STATE_WAIT_LOCK:
                        CAPTURE_STATE = STATE_PREVIEW;
                        int focus_state = captureResult.get(CaptureResult.CONTROL_AF_STATE);
                        if(focus_state == CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED || focus_state == CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED){
                            Toast.makeText(getApplicationContext(), "Focus Locked", Toast.LENGTH_SHORT).show();
                        }

                        break;

                }
            }

            @Override
            public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                super.onCaptureCompleted(session, request, result);

                process(result);
            }
        };
    }

    public void closeCamera(){
        if(cameraDevice != null){
            cameraDevice.close();
            cameraDevice = null;
        }
    }

    public void setUpCamera(int width, int height){
        CameraManager manager = (CameraManager)getSystemService(Context.CAMERA_SERVICE);

        try {
            for(String camId : manager.getCameraIdList()){
                CameraCharacteristics characteristics = manager.getCameraCharacteristics(camId);

                if(characteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT){
                    continue;
                }

                StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

                int deviceOrientation = getWindowManager().getDefaultDisplay().getRotation();
                int totalRoation = sensorTODeviceOrientation(characteristics, deviceOrientation);
                boolean swapRotaion = totalRoation == 90 || totalRoation == 270;

                //get landscape dimensions
                int rotationWidth = width;
                int rotaionHeight = height;

                //check portrait
                if (swapRotaion){
                    //set to portrait dimensions
                    rotaionHeight = width;
                    rotationWidth = height;
                }

                previewSize = choosOptimalSize(map.getOutputSizes(SurfaceTexture.class), rotationWidth, rotaionHeight);
                imageSize = choosOptimalSize(map.getOutputSizes(ImageFormat.JPEG), rotationWidth, rotaionHeight);
                imageReader = ImageReader.newInstance(imageSize.getWidth(), imageSize.getHeight(), ImageFormat.JPEG, 1);
                imageReader.setOnImageAvailableListener(imageAvailableListener, handler);

                cameraId = camId;
                return;
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void connectCamera(){
        CameraManager manager = (CameraManager)getSystemService(CAMERA_SERVICE);

        try {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
                    manager.openCamera(cameraId, cameraCallBack, handler);
                }
                else {
                    if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                        Toast.makeText(this, "App requires camera.", Toast.LENGTH_SHORT).show();
                    }

                    requestPermissions(new String[] {Manifest.permission.CAMERA}, CAMERA_REQUEST_PERMISSION);
                }
            }
            else {
                manager.openCamera(cameraId, cameraCallBack, handler);
            }

            manager.openCamera(cameraId, cameraCallBack, handler);
        }
        catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void startPreview(){
        SurfaceTexture surfaceTexture = textureView.getSurfaceTexture();
        surfaceTexture.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());
        Surface previewSurface = new Surface(surfaceTexture);

        try {
            builder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            builder.addTarget(previewSurface);

            cameraDevice.createCaptureSession(Arrays.asList(previewSurface, imageReader.getSurface()), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {

                    previewCaptureSession = session;
                    try {
                        previewCaptureSession.setRepeatingRequest(builder.build(), null, handler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    Toast.makeText(getApplicationContext(), "Unable to setup camera preview.", Toast.LENGTH_SHORT).show();
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void lockFocus(){
        CAPTURE_STATE = STATE_WAIT_LOCK;
        builder.set(CaptureRequest.CONTROL_AF_TRIGGER, CaptureRequest.CONTROL_AF_TRIGGER_START);
        try {
            previewCaptureSession.capture(builder.build(), captureCallback, handler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }

    public static class CompareSizeByArea implements Comparator<Size>{
        @Override
        public int compare(Size o1, Size o2) {
            return Long.signum((long) o1.getHeight() * o1.getWidth() / (long) o2.getHeight() * o2.getWidth());
        }
    }

    public static Size choosOptimalSize(Size[] choice, int width, int height){
        List<Size> bigEnough = new ArrayList<Size>();

        for(Size option : choice){
            if(option.getHeight() == option.getWidth() * height / width &&
                    option.getWidth() >= width && option.getHeight() >= height){
                bigEnough.add(option);
            }
        }

        if (bigEnough.size() > 0){
            return Collections.min(bigEnough, new CompareSizeByArea());
        }
        else {
            return choice[0];
        }
    }

    public void startBackgroundThread(){
        thread = new HandlerThread("PhoneDrone");
        thread.start();
        handler = new Handler(thread.getLooper());
    }

    public void stopBackgroundThread(){
        thread.quitSafely();

        try {
            thread.join();
            thread = null;
            handler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static int sensorTODeviceOrientation(CameraCharacteristics characteristics, int deviceOrientation){
        int sensorToOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
        deviceOrientation = ORIENTATIONS.get(deviceOrientation);
        return (sensorToOrientation + deviceOrientation + 360) % 360;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CAMERA_REQUEST_PERMISSION){
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "Application requires camera.", Toast.LENGTH_SHORT).show();
            }
        }

        if(requestCode == VIDEO_REQUEST){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                isRecording = true;
                videoButton.setImageResource(R.mipmap.video_busy);

                try {
                    createVideoFolderName();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                Toast.makeText(this, "Permission Granted.", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(this, "App needs to save video and run", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void createVideoFolder(){
        File mFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
        videoFolder = new File(mFile, "VideoFile");

        if(!videoFolder.exists()){
            videoFolder.mkdir();
        }
    }

    public File createVideoFolderName() throws IOException{
        String timeStamp = new SimpleDateFormat("yyyyMMdd_ss").format(new Date());
        String prepend = "VIDEO_" + timeStamp + "_";
        File videoFile = File.createTempFile(prepend, ".mp4", videoFolder);
        videoFileName = videoFile.getAbsolutePath();

        return videoFile;
    }

    public void checkWriteStoragePermission(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                isRecording = true;
                videoButton.setImageResource(R.mipmap.video_busy);

                try {
                    createVideoFolderName();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else {
                if(shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                    Toast.makeText(this, "App needs to save videos.", Toast.LENGTH_SHORT);
                }

                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, VIDEO_REQUEST);
            }

        }
        else {
            isRecording = true;
            videoButton.setImageResource(R.mipmap.video_busy);

            try {
                createVideoFolder();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        startBackgroundThread();

        if(textureView.isAvailable()){
            setUpCamera(textureView.getWidth(), textureView.getHeight());
            connectCamera();
            startPreview();
        }
        else {
            textureView.setSurfaceTextureListener(textureListener);
        }
    }

    @Override
    protected void onPause() {
        closeCamera();
        stopBackgroundThread();
        super.onPause();
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
