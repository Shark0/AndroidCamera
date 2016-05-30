package com.shark.androidcamera;

import android.app.Activity;
import android.hardware.Camera;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

/**
 * Created by Shark0 on 2016/5/27.
 */
public class CameraView extends SurfaceView implements SurfaceHolder.Callback {

    private int cameraFacing = Camera.CameraInfo.CAMERA_FACING_BACK;
    private boolean isTakingPicture = false;
    private SurfaceHolder holder;
    private Camera camera;
    private Activity activity;


    public CameraView(Activity activity) {
        super(activity);
        this.activity = activity;
        holder = getHolder();
        holder.addCallback(this);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        setCameraFacing();
    }

    public void surfaceDestroyed(SurfaceHolder holder) {}

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        if (this.holder.getSurface() == null) {
            return;
        }
        try {
            camera.stopPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            camera.setPreviewDisplay(this.holder);
            camera.startPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setCameraFacing() {
        if(camera != null) {
            camera.stopPreview();
            camera.release();
        }

        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        int cameraCount = Camera.getNumberOfCameras();
        for (int cameraId = 0; cameraId < cameraCount; cameraId++) {
            Camera.getCameraInfo(cameraId, cameraInfo);
            if (cameraInfo.facing == cameraFacing) {
                try {
                    camera = Camera.open(cameraId);
                    int rotation = activity.getWindowManager().getDefaultDisplay()
                            .getRotation();
                    int degrees = 0;
                    switch (rotation) {
                        case Surface.ROTATION_0:
                            degrees = 0;
                            break;
                        case Surface.ROTATION_90:
                            degrees = 90;
                            break;
                        case Surface.ROTATION_180:
                            degrees = 180;
                            break;
                        case Surface.ROTATION_270:
                            degrees = 270;
                            break;
                    }
                    int result;
                    if(cameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                        result = (cameraInfo.orientation + degrees) % 360;
                        result = (360 - result) % 360; // compensate the mirror
                    } else {
                        result = (cameraInfo.orientation - degrees + 360) % 360;
                    }
                    camera.setDisplayOrientation(result);
                    camera.setPreviewDisplay(holder);
                    camera.startPreview();
                } catch (RuntimeException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void takePicture(Camera.ShutterCallback shutterCallback, Camera.PictureCallback pictureCallback) {
        if(camera != null) {
            if (!isTakingPicture) {
                setTakingPicture(true);
                camera.takePicture(shutterCallback, null, pictureCallback);
            }
        }
    }

    public boolean hasCameraFrontFacing() {
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        int cameraCount = Camera.getNumberOfCameras();
        for (int cameraId = 0; cameraId < cameraCount; cameraId++) {
            Camera.getCameraInfo(cameraId, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                return true;
            }
        }
        return false;
    }

    public int getCameraFacing() {
        return cameraFacing;
    }

    public void setCameraFacing(int cameraFacing) {
        this.cameraFacing = cameraFacing;
        setCameraFacing();
    }

    public boolean isTakingPicture() {
        return isTakingPicture;
    }

    public void setTakingPicture(boolean takingPicture) {
        isTakingPicture = takingPicture;
    }
}
