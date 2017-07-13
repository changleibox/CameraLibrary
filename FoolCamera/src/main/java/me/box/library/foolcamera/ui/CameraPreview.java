/*
 * Copyright © 2017 CHANGLEI. All rights reserved.
 */

package me.box.library.foolcamera.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.view.OrientationEventListener;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import me.box.library.foolcamera.compat.CameraCompat;
import me.box.library.foolcamera.compat.CameraException;

/**
 * Created by box on 2017/7/11.
 * <p>
 * 拍照
 */

@SuppressLint("ViewConstructor")
@SuppressWarnings({"deprecation"})
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

    private Camera mCamera;
    private SurfaceHolder mHolder;
    private TakePictureTask mPictureTask;
    private OrientationEventListener mOrientationListener;

    private int mOrientation;
    private int mCameraId;

    public CameraPreview(Context context, Camera camera, int cameraId) {
        super(context);
        this.mCamera = camera;
        this.mCameraId = cameraId;

        mHolder = this.getHolder();
        mHolder.addCallback(this);
        mHolder.setKeepScreenOn(true);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mHolder.setFormat(PixelFormat.TRANSPARENT);

        mOrientationListener = new OrientationEventListener(context) {
            @Override
            public void onOrientationChanged(int orientation) {
                mOrientation = orientation;
            }
        };
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mOrientationListener.enable();
        startPreviewDisplay(holder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (mCamera == null) {
            return;
        }

        CameraCompat.setCameraDefaultParamters(mCamera);
        CameraCompat.setCameraPictureSize(getContext(), mCamera, width, height);
        CameraCompat.setCameraDisplayOrientation((Activity) getContext(), mCameraId, mCamera);

        stopPreviewDisplay();
        startPreviewDisplay(mHolder);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mOrientationListener.disable();
        stopPreviewDisplay();
        holder.getSurface().release();
        if (mPictureTask != null) {
            mPictureTask.cancel(true);
            mPictureTask = null;
        }
    }

    public void startPreviewDisplay(SurfaceHolder holder) {
        if (mCamera == null) {
            return;
        }
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
            mCamera.cancelAutoFocus();
        } catch (IOException ignored) {
            ignored.printStackTrace();
        }
    }

    public void startPreviewDisplay() {
        startPreviewDisplay(getHolder());
    }

    public void stopPreviewDisplay() {
        if (mCamera == null) {
            return;
        }
        try {
            mCamera.stopPreview();
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }
    }

    public void reconnect() {
        try {
            if (mCamera != null) {
                mCamera.reconnect();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void switchCamera(Camera camera, int cameraId) {
        mCamera = camera;
        mCameraId = cameraId;
        CameraCompat.setCameraDefaultParamters(mCamera);
        CameraCompat.setCameraPictureSize(getContext(), mCamera, getWidth(), getHeight());
        CameraCompat.setCameraDisplayOrientation((Activity) getContext(), mCameraId, mCamera);
        startPreviewDisplay();
    }

    public void takePicture(Activity activity, PictureCallback jpeg) {
        if (mCamera == null) {
            return;
        }
        if (mPictureTask != null && mPictureTask.getStatus() == AsyncTask.Status.RUNNING) {
            return;
        }
        (mPictureTask = new TakePictureTask(activity, jpeg)).execute(mCamera);
    }

    public interface PictureCallback {
        void onPictureTaken(Response<File> response);
    }

    private class TakePictureTask extends AsyncTask<Camera, Void, Response<File>> {

        private PictureCallback mCallback;
        private Activity mActivity;

        private TakePictureTask(Activity activity, PictureCallback callback) {
            this.mCallback = callback;
            this.mActivity = activity;
        }

        @Override
        protected Response<File> doInBackground(Camera... cameras) {
            if (isCancelled()) {
                return null;
            }
            final byte[][] data = new byte[1][1];
            final CountDownLatch latch = new CountDownLatch(1);
            mCamera.takePicture(null, null, new Camera.PictureCallback() {
                @Override
                public void onPictureTaken(byte[] bytes, Camera camera) {
                    data[0] = bytes;
                    latch.countDown();
                }
            });
            try {
                latch.await();
            } catch (InterruptedException ignored) {
                ignored.printStackTrace();
            }
            try {
                return Response.createSuccess(CameraCompat.onPictureTaken(mActivity, data[0], mOrientation, mCameraId));
            } catch (CameraException e) {
                return Response.createFailure(e);
            }
        }

        @Override
        protected void onPostExecute(Response<File> response) {
            if (isCancelled()) {
                return;
            }
            if (mCallback != null) {
                mCallback.onPictureTaken(response);
            }
        }
    }

}
