package com.shark.androidcamera;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;

public class CameraActivity extends AppCompatActivity implements View.OnClickListener, Camera.PictureCallback, Camera.ShutterCallback {

    private int REQUEST_CODE_PERMISSION = 1;
    private int REQUEST_CODE_SETTING = 2;

    private CameraView cameraView;
    private ProgressDialog savePictureDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Set Activity Full Screen - Shark.M.Lin
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_camera);
        bindContentView();
        //Check Device Has Camera - Shark.M.Lin
        if(checkCameraHardware()) {
            handlePermission();
        } else {
            showNoCameraView();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_CODE_SETTING) {
            handlePermission();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        handlePermission();
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void bindContentView() {
        bindOpenPermissionButton();
    }

    private void bindOpenPermissionButton() {
        findViewById(R.id.activityCamera_openPermissionButton).setOnClickListener(this);
    }

    private void bindCameraView() {
        if(cameraView == null) {
            FrameLayout container = (FrameLayout) findViewById(R.id.activityCamera_cameraViewContainer);
            try {
                cameraView = new CameraView(this);
                container.addView(cameraView);
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        bindChangeFacingButton();
        bindCaptureCameraButton();
    }

    private void bindChangeFacingButton() {
        if(cameraView != null && cameraView.hasCameraFrontFacing()) {
            findViewById(R.id.activityCamera_changeFacingButton).setVisibility(View.VISIBLE);
            findViewById(R.id.activityCamera_changeFacingButton).setOnClickListener(this);
            return;
        }
        findViewById(R.id.activityCamera_changeFacingButton).setVisibility(View.GONE);
    }

    private void bindCaptureCameraButton() {
        findViewById(R.id.activityCamera_captureCameraButton).setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.activityCamera_openPermissionButton:
                onOpenPermissionButtonClick();
                break;
            case R.id.activityCamera_changeFacingButton:
                onChangeFacingButtonClick();
                break;
            case R.id.activityCamera_captureCameraButton:
                onCaptureCameraButtonClick();
                break;
        }
    }

    private void onOpenPermissionButtonClick() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName()));
        startActivityForResult(intent, REQUEST_CODE_SETTING);
    }

    private void onChangeFacingButtonClick() {
        if(cameraView.getCameraFacing() == Camera.CameraInfo.CAMERA_FACING_BACK) {
            cameraView.setCameraFacing(Camera.CameraInfo.CAMERA_FACING_FRONT);
        } else {
            cameraView.setCameraFacing(Camera.CameraInfo.CAMERA_FACING_BACK);
        }
    }

    private void onCaptureCameraButtonClick() {
        if(cameraView != null) {
            cameraView.takePicture(this, this);
        }
    }

    @Override
    public void onShutter() {
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioManager.playSoundEffect(AudioManager.FLAG_PLAY_SOUND);
    }

    @Override
    public void onPictureTaken(final byte[] data, final Camera camera) {
        //Use Second Third Save Bitmap - Shark.M.Lin
        showSavePictureDialog();
        new Thread(new Runnable() {
            @Override
            public void run() {
                File pictureDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                File weTouchPictureDir = new File(pictureDir, "AndroidCamera");
                if (!weTouchPictureDir.exists()) {
                    weTouchPictureDir.mkdirs();
                }

                String fileName = System.currentTimeMillis() + ".png";
                File file = new File(weTouchPictureDir, fileName);
                if (file.exists()) {
                    file.delete();
                }
                try {
                    FileOutputStream fileOutputStream = new FileOutputStream(file);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0 , data.length);
                    ExifInterface exifInterface = new ExifInterface(file.toString());

                    if(exifInterface.getAttribute(ExifInterface.TAG_ORIENTATION).equalsIgnoreCase("6")){
                        bitmap= BitmapUtil.rotate(bitmap, 90);
                    } else if(exifInterface.getAttribute(ExifInterface.TAG_ORIENTATION).equalsIgnoreCase("8")){
                        bitmap= BitmapUtil.rotate(bitmap, 270);
                    } else if(exifInterface.getAttribute(ExifInterface.TAG_ORIENTATION).equalsIgnoreCase("3")){
                        bitmap= BitmapUtil.rotate(bitmap, 180);
                    } else if(exifInterface.getAttribute(ExifInterface.TAG_ORIENTATION).equalsIgnoreCase("0")){
                        bitmap= BitmapUtil.rotate(bitmap, 90);
                    }

                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
                    fileOutputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(savePictureDialog != null) {
                            savePictureDialog.dismiss();
                        }
                        Toast.makeText(CameraActivity.this, "拍照成功，以存檔到AndroidCamera資料夾", Toast.LENGTH_SHORT).show();
                        cameraView.setTakingPicture(false);
                        camera.startPreview();
                    }
                });
            }
        }).start();

    }

    private void handlePermission() {
        if (checkCameraPermission() && checkStoragePermission()) {
            showCameraView();
            bindCameraView();
        } else {
            showPermissionContainer();
            if(shouldShowCameraRequestPermissionRationale() || shouldShowStorageRequestPermissionRationale()) {
                showDescriptionPermissionDialog();
            } else {
                requestPermission();
            }
        }
    }

    private boolean checkCameraHardware() {
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            return true;
        } else {
            return false;
        }
    }

    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean shouldShowCameraRequestPermissionRationale() {
        return ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA);
    }

    private boolean checkStoragePermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean shouldShowStorageRequestPermissionRationale() {
        return ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    private void showDescriptionPermissionDialog() {
        //FIXME change title, message, and button text from string resource
        new AlertDialog.Builder(this).setTitle("拍攝照片")
                .setMessage("拍張好看的照片分享給其他人")
                .setNegativeButton("開啟", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                requestPermission();
            }
        }).show();
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                REQUEST_CODE_PERMISSION);
    }

    private void showNoCameraView() {
        findViewById(R.id.activityCamera_noCameraTextView).setVisibility(View.VISIBLE);
        findViewById(R.id.activityCamera_permissionContainer).setVisibility(View.GONE);
        findViewById(R.id.activityCamera_cameraContainer).setVisibility(View.GONE);
    }

    private void showPermissionContainer() {
        findViewById(R.id.activityCamera_noCameraTextView).setVisibility(View.GONE);
        findViewById(R.id.activityCamera_permissionContainer).setVisibility(View.VISIBLE);
        findViewById(R.id.activityCamera_cameraContainer).setVisibility(View.GONE);
    }

    private void showCameraView() {
        findViewById(R.id.activityCamera_noCameraTextView).setVisibility(View.GONE);
        findViewById(R.id.activityCamera_permissionContainer).setVisibility(View.GONE);
        findViewById(R.id.activityCamera_cameraContainer).setVisibility(View.VISIBLE);
    }

    private void showSavePictureDialog() {
        savePictureDialog = ProgressDialog.show(this, "存檔中", "請稍後", true);
    }
}