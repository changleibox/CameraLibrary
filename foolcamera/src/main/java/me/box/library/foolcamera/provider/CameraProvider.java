package me.box.library.foolcamera.provider;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.hardware.Camera.CameraInfo;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.IntDef;
import android.support.annotation.RequiresPermission;

import java.io.File;

import me.box.library.foolcamera.CameraActivity;
import me.box.library.foolcamera.R;
import me.box.library.foolcamera.util.CameraToastCompat;

/**
 * Created by box on 2017/7/13.
 * <p>
 * 拍照操作
 */

public final class CameraProvider {

    @IntDef(value = {Configuration.ORIENTATION_UNDEFINED, Configuration.ORIENTATION_LANDSCAPE, Configuration.ORIENTATION_PORTRAIT})
    @interface CameraOrientation {
    }

    @SuppressWarnings("deprecation")
    @RequiresPermission(allOf = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    public static void openCamera(Activity activity, int requestCode, boolean isFontCamera, File outputFile, @CameraOrientation int orientation) {
        PackageManager packageManager = activity.getPackageManager();
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            CameraToastCompat.showText(activity, R.string.camera_prompt_no_camera);
            return;
        }
        Intent intentImage = new Intent(activity, CameraActivity.class);
        intentImage.addCategory(Intent.CATEGORY_DEFAULT);
        intentImage.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(outputFile));
        if (packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)) {
            int facing = isFontCamera ? CameraInfo.CAMERA_FACING_FRONT : CameraInfo.CAMERA_FACING_BACK;
            intentImage.putExtra(CameraStore.EXTRAS_CAMERA_FACING, facing);
        } else if (isFontCamera) {
            CameraToastCompat.showText(activity, R.string.camera_prompt_no_facing_camera);
        }
        intentImage.putExtra(CameraStore.EXTRAS_SCREEN_ORIENTATION, orientation);
        activity.startActivityForResult(intentImage, requestCode);
    }
}
