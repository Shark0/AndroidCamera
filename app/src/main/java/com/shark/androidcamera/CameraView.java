package com.shark.androidcamera;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import java.io.IOException;

/**
 * Created by Shark0 on 2016/5/27.
 */
public class CameraView extends SurfaceView implements SurfaceHolder.Callback, Camera.FaceDetectionListener {

    private int cameraFacing = Camera.CameraInfo.CAMERA_FACING_BACK;
    private boolean isTakingPicture = false;
    private SurfaceHolder holder;
    private Camera camera;
    private Context context;
    private Camera.FaceDetectionListener faceDetectListener;

    public CameraView(Context context) {
        super(context);
        this.context = context;
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
            if(faceDetectListener != null) {
                camera.startFaceDetection();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setCameraFacing() {
        if(camera != null) {
            camera.stopPreview();
            camera.release();
        }

        int cameraId = getCameraIndex(cameraFacing);
        if(cameraId != -1) {
            try {
                camera = Camera.open(cameraId);
                 WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
                int rotation = windowManager.getDefaultDisplay().getRotation();
                int windowRotation = 0;
                switch (rotation) {
                    case Surface.ROTATION_0:
                        windowRotation = 0;
                        break;
                    case Surface.ROTATION_90:
                        windowRotation = 90;
                        break;
                    case Surface.ROTATION_180:
                        windowRotation = 180;
                        break;
                    case Surface.ROTATION_270:
                        windowRotation = 270;
                        break;
                }
                Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
                Camera.getCameraInfo(cameraId, cameraInfo);
                int cameraRotation;
                int orientation;
                if (cameraInfo.facing ==  Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    cameraRotation = (cameraInfo.orientation + windowRotation) % 360;
                    orientation = (360 - cameraRotation) % 360;
                } else {
                    cameraRotation = (cameraInfo.orientation - windowRotation + 360) % 360;
                    orientation = cameraRotation;
                }
                camera.setDisplayOrientation(orientation);
                camera.setPreviewDisplay(holder);
                camera.setFaceDetectionListener(this);
                camera.startPreview();
                if(faceDetectListener != null) {
                    camera.startFaceDetection();
                }
                Log.e("CameraView", "startFaceDetection");
            } catch (IOException e) {
                e.printStackTrace();
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

    public void setFaceDetectListener(Camera.FaceDetectionListener faceDetectListener) {
        this.faceDetectListener = faceDetectListener;
        if(camera != null) {
            if(this.faceDetectListener != null) {
                camera.startFaceDetection();
            } else {
                camera.stopFaceDetection();
            }
        }
    }

    private int getCameraIndex(int facing) {
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for (int index = 0; index < Camera.getNumberOfCameras(); ++index) {
            Camera.getCameraInfo(index, cameraInfo);
            if (cameraInfo.facing == facing) {
                return index;
            }
        }
        return -1;
    }

    @Override
    public void onFaceDetection(Camera.Face[] faces, Camera camera) {
        if(faceDetectListener != null) {
            faceDetectListener.onFaceDetection(faces,camera);
        }
    }
}
