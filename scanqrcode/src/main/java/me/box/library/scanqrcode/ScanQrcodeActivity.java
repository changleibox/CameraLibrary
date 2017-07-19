/*
 * Copyright (c) All right reserved by Box
 */
package me.box.library.scanqrcode;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.provider.MediaStore.Images;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageButton;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.mining.app.zxing.camera.CameraManager;
import com.mining.app.zxing.decoding.CaptureActivityHandler;
import com.mining.app.zxing.decoding.InactivityTimer;
import com.mining.app.zxing.view.ViewfinderView;

import java.io.IOException;
import java.util.Vector;

import me.box.library.scanqrcode.Constants.Key;
import me.box.library.scanqrcode.Constants.RequestCode;
import me.box.library.scanqrcode.provider.QrcodeConfig;
import me.box.library.scanqrcode.provider.QrcodeResult;

/**
 * @author Box
 * @version v1.0
 * @since Jdk1.6
 * <p/>
 * 2015年10月12日 下午7:35:16
 * <p/>
 * 扫一扫
 * <p/>
 * 2015年10月12日 下午7:35:16
 * <p/>
 * Box
 */
@SuppressWarnings("deprecation")
public class ScanQrcodeActivity extends AppCompatActivity implements Callback, OnClickListener {

    public static final int CHOOSE_PICTURE = 0xfff;

    public static final float RATE_SCAN = 1F;
    public static final float RATE_LOCATION = 0.4F;

    private static final long VIBRATE_DURATION = 200L;

    private CaptureActivityHandler mHandler;
    private Vector<BarcodeFormat> mDecodeFormats;
    private String mCharacterSet;
    private InactivityTimer mInactivityTimer;
    private boolean hasSurface;
    private boolean isLightEnable;

    ImageButton mIbLight;
    ViewfinderView mFinderView;

    SurfaceHolder surfaceHolder;
    QrcodeConfig mQrcodeConfig;

    private ScanBitmapTask mScanImageTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mQrcodeConfig = getIntent().getParcelableExtra(Key.KEY_SCAN_CONFIG);
        setTheme(mQrcodeConfig.getTheme());
        setContentView(R.layout.qrcode_activity_scan_qrcode);

        setTitle(mQrcodeConfig.getTitle());
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(mQrcodeConfig.isDisplayHomeAsUpEnabled());
        }

        mIbLight = (ImageButton) findViewById(R.id.qrcode_ib_light);
        mFinderView = (ViewfinderView) findViewById(R.id.qrcode_viewfinder_view);

        mIbLight.setVisibility(mQrcodeConfig.isHasFlashLight() ? View.VISIBLE : View.GONE);

        CameraManager.init(getApplication());

        hasSurface = false;
        mInactivityTimer = new InactivityTimer(this);

        String prompt = mQrcodeConfig.getPrompt();
        mFinderView.setText(TextUtils.isEmpty(prompt) ? getString(R.string.qrcode_label_default_scan_prompt) : prompt);
        mFinderView.setRate(RATE_SCAN);
        mFinderView.setLocationRate(RATE_LOCATION);
        mFinderView.setLineDrawable(mQrcodeConfig.getDivider());
        mFinderView.setBorderColor(mQrcodeConfig.getBorderColor());
        mFinderView.setTextSize(mQrcodeConfig.getTextSize());
        mFinderView.setTextColor(mQrcodeConfig.getTextColor());

        ClickFilter.filterForeground(mIbLight);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (mQrcodeConfig.isCanScanImage()) {
            getMenuInflater().inflate(R.menu.qrcode_menu_scan_image, menu);
        }
        boolean optionsMenu = super.onCreateOptionsMenu(menu);
        MenuItem item = menu.findItem(R.id.qrcode_menu_image);
        if (item != null) {
            item.setIcon(mQrcodeConfig.getScanImageIcon());
        }
        return optionsMenu;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.qrcode_menu_image) {
            Intent intentPick = new Intent(Intent.ACTION_PICK);
            intentPick.setDataAndType(Images.Media.INTERNAL_CONTENT_URI, "image/*");
            startActivityForResult(intentPick, CHOOSE_PICTURE);
        } else if (id == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.qrcode_ib_light) {
            switchFlashLight(v);
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onResume() {
        super.onResume();

        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.qrcode_preview_view);
        if (surfaceView == null) {
            return;
        }
        surfaceHolder = surfaceView.getHolder();
        if (hasSurface) {
            initCamera(surfaceHolder);
        } else {
            surfaceHolder.addCallback(this);
            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }
        mDecodeFormats = null;
        mCharacterSet = null;

        AudioManager audioService = (AudioManager) getSystemService(AUDIO_SERVICE);
        if (audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
            mQrcodeConfig.setPlayBeep(false);
        }

        Intent intent = getIntent();
        byte[] bytes = intent.getByteArrayExtra(Key.KEY_SCAN_BITMAP);
        if (bytes != null && bytes.length > 0) {
            intent.removeExtra(Key.KEY_SCAN_BITMAP);
            setIntent(intent);
            mScanImageTask = (ScanBitmapTask) new ScanBitmapTask(bytes).execute();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mHandler != null) {
            mHandler.quitSynchronously();
            mHandler = null;
        }
        CameraManager.getInstance().closeDriver();
    }

    @Override
    protected void onDestroy() {
        mInactivityTimer.shutdown();
        if (mScanImageTask != null) {
            mScanImageTask.cancel(true);
            mScanImageTask = null;
        }
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RequestCode.REQUEST_CODE_CAMERA) {
            if (AppUtils.hasSelfPermission(grantResults)) {
                if (surfaceHolder == null) {
                    return;
                }
                if (hasSurface) {
                    initCamera(surfaceHolder);
                } else {
                    surfaceHolder.addCallback(this);
                    //noinspection deprecation
                    surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
                }
            } else {
                ToastCompat.showText(ScanQrcodeActivity.this, R.string.qrcode_toast_please_start_pic);
                finish();
            }
        }
    }

    public void handleDecode(Result result, Bitmap barcode) {
        onResultHandler(result.getText(), barcode);
    }

    private void initCamera(SurfaceHolder surfaceHolder) {
        try {
            if (!AppUtils.checkSelfPermission(this, RequestCode.REQUEST_CODE_CAMERA, Manifest.permission.CAMERA)) {
                return;
            }
            CameraManager.getInstance().openDriver(surfaceHolder);
        } catch (IOException ioe) {
            return;
        } catch (RuntimeException e) {
            ToastCompat.showText(ScanQrcodeActivity.this, R.string.qrcode_toast_please_start_pic);
            finish();
            return;
        }
        if (mHandler == null) {
            mHandler = new CaptureActivityHandler(this, mDecodeFormats, mCharacterSet);
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (!hasSurface) {
            hasSurface = true;
            initCamera(holder);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        hasSurface = false;
    }

    public ViewfinderView getViewfinderView() {
        return mFinderView;
    }

    public Handler getHandler() {
        return mHandler;
    }

    public void drawViewfinder() {
        mFinderView.drawViewfinder();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK || data == null) {
            return;
        }
        if (requestCode == CHOOSE_PICTURE) {
            if (mScanImageTask != null) {
                mScanImageTask.cancel(true);
                mScanImageTask = null;
            }
            mScanImageTask = (ScanBitmapTask) new ScanBitmapTask(this, data.getData()).execute();
        }
    }

    private void playBeepSoundAndVibrate() {
        if (mQrcodeConfig.isPlayBeep()) {
            Media.start(this, "sound/beep.ogg");
        }
        if (mQrcodeConfig.isVibrate()) {
            ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(VIBRATE_DURATION);
        }
    }

    private void onResultHandler(String resultString, Bitmap barcode) {
        onResultHandler(new QrcodeResult(resultString, barcode));
    }

    private void onResultHandler(QrcodeResult result) {
        mInactivityTimer.onActivity();
        try {
            playBeepSoundAndVibrate();
        } catch (Exception ignored) {
        }
        Intent resultIntent = new Intent();
        resultIntent.putExtra(Key.KEY_SCAN_RESULT, result);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    private void switchFlashLight(View v) {
        Camera camera = CameraManager.getInstance().getCamera();
        try {
            isLightEnable = !isLightEnable;
            if (isLightEnable) {
                FlashlightManager.turnLightOn(camera);
            } else {
                FlashlightManager.turnLightOff(camera);
            }
            CameraManager.getInstance().startPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }
        v.setSelected(isLightEnable);
    }

    private class ScanBitmapTask extends ScanImageTask {

        ScanBitmapTask(byte[] bytes) {
            super(bytes);
        }

        ScanBitmapTask(Context context, Uri uri) {
            super(context, uri);
        }

        @Override
        protected void onPostExecute(QrcodeResult result) {
            if (isCancelled()) {
                return;
            }
            onResultHandler(result);
        }
    }

}
