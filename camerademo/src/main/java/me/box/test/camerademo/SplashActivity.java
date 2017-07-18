package me.box.test.camerademo;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import me.box.library.scanqrcode.ScanActivity;

/**
 * Created by box on 2017/7/18.
 * <p>
 * 地动页面
 */

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
    }

    public void takePicture(View view) {
        startActivity(new Intent(this, TakePictureActivity.class));
    }

    public void scanQrcode(View view) {
        startActivity(new Intent(this, ScanActivity.class));
    }
}
