package me.box.library.foolcamera;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;

import java.io.File;

import me.box.library.foolcamera.compat.CameraCompat;
import me.box.library.foolcamera.ui.CameraPreview;
import me.box.library.foolcamera.ui.Response;
import me.box.library.foolcamera.util.CameraToastCompat;

import static me.box.library.foolcamera.compat.CameraException.ERROR_INSUFFICIENT_MEMORY;
import static me.box.library.foolcamera.compat.CameraException.ERROR_NO_SDCARD;
import static me.box.library.foolcamera.provider.CameraStore.EXTRAS_CAMERA_FACING;
import static me.box.library.foolcamera.provider.CameraStore.EXTRAS_PICTURE_FILE;
import static me.box.library.foolcamera.provider.CameraStore.EXTRAS_SCREEN_ORIENTATION;

/**
 * Created by box on 2017/7/13.
 * <p>
 * 拍照
 */

@SuppressWarnings("deprecation")
public class CameraActivity extends AppCompatActivity implements CameraPreview.PictureCallback {

    private CameraPreview mCameraPreview;
    private View mOperationContainer;
    private View mTakePictureContainer;
    private ImageView mIvPreview;

    @Nullable
    private Camera mCamera;
    private File mPictureFile;

    private int mCameraFacing;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        if (!CameraCompat.hasCameraDevice(this)) {
            CameraToastCompat.showText(this, R.string.camera_prompt_no_camera);
            finish();
            return;
        }

        mCameraFacing = CameraCompat.getExtrasCameraFacing(this);
        int cameraId = CameraCompat.getCameraId(mCameraFacing);
        mCamera = CameraCompat.openCamera(cameraId);
        if (mCamera == null) {
            CameraToastCompat.showText(this, R.string.camera_prompt_open_camera_failure);
            finish();
            return;
        }
        mCameraPreview = new CameraPreview(this, mCamera, cameraId);
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        mCameraPreview.setLayoutParams(params);

        int requestedOrientation = getRequestedOrientation();
        int screenOrientation = getScreenOrientation();
        if (requestedOrientation != screenOrientation) {
            //noinspection WrongConstant
            setRequestedOrientation(screenOrientation);
        }

        setContentView(R.layout.camera_activity_camera);
    }

    @Override
    public void onContentChanged() {
        FrameLayout previewContainer = (FrameLayout) findViewById(R.id.camera_fl_preview_container);
        mOperationContainer = findViewById(R.id.camera_ll_operation);
        mTakePictureContainer = findViewById(R.id.camera_fl_take_picture);
        mIvPreview = (ImageView) findViewById(R.id.camera_iv_preview);
        mIvPreview.setVisibility(View.GONE);
        mOperationContainer.setVisibility(View.GONE);
        mTakePictureContainer.setVisibility(View.VISIBLE);

        ViewGroup parent = (ViewGroup) mCameraPreview.getParent();
        if (parent != null) {
            parent.removeView(mCameraPreview);
        }
        previewContainer.addView(mCameraPreview, 0);

        if (mPictureFile != null) {
            onPictureTaken(Response.createSuccess(mPictureFile));
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setContentView(R.layout.camera_activity_camera);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(EXTRAS_CAMERA_FACING, mCameraFacing);
        outState.putSerializable(EXTRAS_PICTURE_FILE, mPictureFile);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    public void onPictureTaken(Response<File> response) {
        mPictureFile = response.getData();

        mCameraPreview.reconnect();
        if (response.isSuccess() && mPictureFile != null) {
            mOperationContainer.setVisibility(View.VISIBLE);
            mTakePictureContainer.setVisibility(View.GONE);
            mIvPreview.setVisibility(View.VISIBLE);
            mIvPreview.setImageBitmap(BitmapFactory.decodeFile(mPictureFile.getAbsolutePath()));
        } else {
            mCameraPreview.startPreviewDisplay();
            int code = response.getCode();
            CameraToastCompat.showText(this,
                    ERROR_INSUFFICIENT_MEMORY.equals(code) ? getString(R.string.camera_prompt_insufficient_memory)
                            : ERROR_NO_SDCARD.equals(code) ? getString(R.string.camera_prompt_no_sdcard)
                            : getString(R.string.camera_prompt_camera_failure));
        }
    }

    public void onTakeCamera(View view) {
        mCameraPreview.takePicture(this, this);
    }

    public void onCancel(View view) {
        finish();
    }

    public void onRetake(View view) {
        if (mPictureFile != null) {
            mPictureFile.deleteOnExit();
            mPictureFile = null;
        }
        mIvPreview.setVisibility(View.GONE);
        mTakePictureContainer.setVisibility(View.VISIBLE);
        mOperationContainer.setVisibility(View.GONE);
        mCameraPreview.startPreviewDisplay();
    }

    public void onUse(View view) {
        Intent intent = new Intent();
        intent.setData(Uri.fromFile(mPictureFile));
        setResult(RESULT_OK, intent);
        finish();
    }

    public void onSwitchCamera(View view) {
        mCameraPreview.stopPreviewDisplay();
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
        if (mCameraFacing == Camera.CameraInfo.CAMERA_FACING_BACK && CameraCompat.hasFrontCamera(this)) {
            mCameraFacing = Camera.CameraInfo.CAMERA_FACING_FRONT;
        } else {
            mCameraFacing = Camera.CameraInfo.CAMERA_FACING_BACK;
        }
        int cameraId = CameraCompat.getCameraId(mCameraFacing);
        mCamera = CameraCompat.openCamera(cameraId);
        if (mCamera == null) {
            CameraToastCompat.showText(this, R.string.camera_prompt_open_camera_failure);
            finish();
            return;
        }
        mCameraPreview.switchCamera(mCamera, cameraId);
    }

    private int getScreenOrientation() {
        switch (getIntent().getIntExtra(EXTRAS_SCREEN_ORIENTATION, Configuration.ORIENTATION_UNDEFINED)) {
            case Configuration.ORIENTATION_LANDSCAPE:
                return ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
            case Configuration.ORIENTATION_PORTRAIT:
                return ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
            default:
                return ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
        }
    }
}
