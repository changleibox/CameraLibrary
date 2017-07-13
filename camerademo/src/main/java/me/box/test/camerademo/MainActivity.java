package me.box.test.camerademo;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.BitmapFactory;
import android.hardware.Camera.CameraInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.RequiresPermission;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;

import me.box.library.foolcamera.CameraActivity;
import me.box.library.foolcamera.provider.CameraStore;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_CAMERA = 0x1;
    private static final int REQUEST_CODE_PERMISSION = 0x2;

    private File mCameraFile;
    private ImageView mIvPicture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mIvPicture = (ImageView) findViewById(R.id.iv_picture);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK || mCameraFile == null) {
            return;
        }
        mIvPicture.setImageBitmap(BitmapFactory.decodeFile(mCameraFile.getAbsolutePath()));
    }

    public void onTakePicture(View view) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_PERMISSION);
            return;
        }
        mCameraFile = new File(getCachePath(this), System.currentTimeMillis() + ".jpg");
        openCamera(this, REQUEST_CODE_CAMERA, false, mCameraFile);
    }

    @SuppressWarnings("deprecation")
    @RequiresPermission(allOf = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    public static void openCamera(Activity activity, int requestCode, boolean isFontCamera, File outputFile) {
        PackageManager packageManager = activity.getPackageManager();
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            Toast.makeText(activity, R.string.prompt_no_camera, Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intentImage = new Intent(activity, CameraActivity.class);
        intentImage.addCategory(Intent.CATEGORY_DEFAULT);
        intentImage.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(outputFile));
        if (packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)) {
            int facing = isFontCamera ? CameraInfo.CAMERA_FACING_FRONT : CameraInfo.CAMERA_FACING_BACK;
            intentImage.putExtra(CameraStore.EXTRAS_CAMERA_FACING, facing);
        } else if (isFontCamera) {
            Toast.makeText(activity, R.string.prompt_no_facing_camera, Toast.LENGTH_SHORT).show();
        }
        intentImage.putExtra(CameraStore.EXTRAS_SCREEN_ORIENTATION, Configuration.ORIENTATION_UNDEFINED);
        activity.startActivityForResult(intentImage, requestCode);
    }

    public static String getCachePath(Context context) {
        String cachePath;
        File externalCacheDir;
        if (isExistSD() && (externalCacheDir = context.getExternalCacheDir()) != null) {
            //外部存储可用
            cachePath = externalCacheDir.getPath();
        } else {
            //外部存储不可用
            cachePath = context.getCacheDir().getPath();
        }
        return cachePath;
    }

    public static boolean isExistSD() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) || !Environment.isExternalStorageRemovable();
    }
}
