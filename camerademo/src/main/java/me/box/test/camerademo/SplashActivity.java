package me.box.test.camerademo;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import me.box.library.scanqrcode.provider.QrcodeConfig;
import me.box.library.scanqrcode.provider.QrcodeProvider;
import me.box.library.scanqrcode.provider.QrcodeResult;

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
        QrcodeConfig config = new QrcodeConfig()
                .setTheme(R.style.AppTheme_ScanActivity)
                .setBorderColor(Color.GREEN)
                .setCanScanImage(true)
                .setHasFlashLight(true)
                .setPrompt("扫描二维码，请对准")
                .setTextSize(14)
                .setTextColor(Color.WHITE)
                .setDisplayHomeAsUpEnabled(true);
        QrcodeProvider.scanQrcode(this, config, 0x01);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        QrcodeResult scanResult = QrcodeProvider.getScanResult(data);
        if (scanResult != null) {
            Toast.makeText(this, scanResult.getResult(), Toast.LENGTH_SHORT).show();
        }
    }
}
