package me.box.test.camerademo;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import java.io.File;

import me.box.library.foolcamera.provider.CameraProvider;

public class TakePictureActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_CAMERA = 0x1;
    private static final int REQUEST_CODE_PERMISSION = 0x2;

    private File mCameraFile;
    private ImageView mIvPicture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_picture);

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
        CameraProvider.openCamera(this, REQUEST_CODE_CAMERA, true, mCameraFile, Configuration.ORIENTATION_UNDEFINED);
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
