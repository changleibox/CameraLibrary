package me.box.test.camerademo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import me.box.library.scanqrcode.ScanImageTask;
import me.box.library.scanqrcode.provider.QrcodeConfig;
import me.box.library.scanqrcode.provider.QrcodeProvider;
import me.box.library.scanqrcode.provider.QrcodeResult;

/**
 * Created by box on 2017/7/18.
 * <p>
 * 地动页面
 */

public class SplashActivity extends AppCompatActivity implements ScanImageTask.Callback, View.OnLongClickListener {

    private ImageView mIvQrcode;
    private ScanImageTask mScanTask;
    @Nullable
    private Bitmap mBitmap;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        mIvQrcode = (ImageView) findViewById(R.id.iv_qrcode);
        mIvQrcode.setOnLongClickListener(this);
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
        onScanQrcodeResult(QrcodeProvider.getScanResult(data));
    }

    @Override
    public boolean onLongClick(View view) {
        if (mScanTask != null && mScanTask.getStatus() == AsyncTask.Status.RUNNING) {
            return false;
        }
        if (mBitmap == null) {
            return false;
        }
        mScanTask = ScanImageTask.scan(mBitmap, this);
        return true;
    }

    @Override
    public void onCallback(QrcodeResult result) {
        onScanQrcodeResult(result);
    }

    private void onScanQrcodeResult(QrcodeResult result) {
        if (result != null && result.isSuccess()) {
            Toast.makeText(this, result.getResult(), Toast.LENGTH_SHORT).show();
            mIvQrcode.setImageBitmap(mBitmap = result.getBarcode());
        } else {
            Toast.makeText(this, "扫描失败", Toast.LENGTH_SHORT).show();
            mIvQrcode.setImageBitmap(mBitmap);
        }
    }

}
