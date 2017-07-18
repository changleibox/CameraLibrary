/*
 * Copyright (c) All right reserved by Box
 */
package me.box.library.scanqrcode;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageButton;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.mining.app.zxing.camera.CameraManager;
import com.mining.app.zxing.decoding.CaptureActivityHandler;
import com.mining.app.zxing.decoding.InactivityTimer;
import com.mining.app.zxing.decoding.RGBLuminanceSource;
import com.mining.app.zxing.view.ViewfinderView;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

import me.box.library.scanqrcode.Constants.Key;
import me.box.library.scanqrcode.Constants.RequestCode;
import me.box.library.scanqrcode.FileUtils.GetPathFromUri4kitkat;

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
@SuppressWarnings({"unused", "deprecation"})
public class ScanActivity extends AppCompatActivity implements Callback, OnClickListener {

    public static final int CHOOSE_PICTURE = 0xfff;

    public static final int SCAN_INDEX = 0;
    public static final int COVER_INDEX = 1;
    public static final int VISTA_INDEX = 2;
    public static final int TRANSLATE_INDEX = 3;

    public static final float RATE_SCAN = 1F;
    public static final float RATE_LOCATION = 0.4F;
    public static final float RATE_COVER = 0.9F;
    public static final float RATE_VISTA = 0.9F;
    public static final float RATE_TRANSLATE = 4F;

    private static final int PARSE_BARCODE_SUC = 300;
    private static final int PARSE_BARCODE_FAIL = 303;

    private CaptureActivityHandler mHandler;
    private boolean hasSurface;
    private Vector<BarcodeFormat> mDecodeFormats;
    private String mCharacterSet;
    private InactivityTimer mInactivityTimer;
    @SuppressWarnings("unused")
    private MediaPlayer mMediaPlayer;
    @SuppressWarnings("unused")
    private static final float BEEP_VOLUME = 0.10f;
    private boolean isVibrate;
    private boolean isLightEnable;
    ImageButton mIbLight;
    ViewfinderView mFinderView;

    SurfaceHolder surfaceHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Intent intent = getIntent();
        setTheme(intent.getIntExtra(Key.KEY_SCAN_THEME, R.style.Theme_AppCompat));
        setContentView(R.layout.qrcode_activity_scan);

        mIbLight = (ImageButton) findViewById(R.id.qrcode_ib_light);
        mFinderView = (ViewfinderView) findViewById(R.id.qrcode_viewfinder_view);

        Toolbar toolbar = (Toolbar) findViewById(R.id.qrcode_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        ThemeCompat.setLayoutFullscreen(this, toolbar);

        CameraManager.init(getApplication());

        hasSurface = false;
        mInactivityTimer = new InactivityTimer(this);

        String prompt = intent.getStringExtra(Key.KEY_SCAN_PROMPT);
        mFinderView.setText(TextUtils.isEmpty(prompt) ? getString(R.string.qrcode_label_default_scan_prompt) : prompt);
        mFinderView.setRate(RATE_SCAN);
        mFinderView.setLocationRate(RATE_LOCATION);

        ClickFilter.filterForeground(mIbLight);
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

        boolean isPlayBeep = true;
        AudioManager audioService = (AudioManager) getSystemService(AUDIO_SERVICE);
        if (audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
            //noinspection UnusedAssignment
            isPlayBeep = false;
        }
        isVibrate = true;
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
                ToastCompat.showText(ScanActivity.this, R.string.qrcode_toast_please_start_pic);
                finish();
            }
        }
    }

    public void handleDecode(Result result, Bitmap barcode) {
        String resultString = result.getText();
        onResultHandler(resultString);
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
            ToastCompat.showText(ScanActivity.this, R.string.qrcode_toast_please_start_pic);
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
        if (resultCode == RESULT_OK && requestCode == CHOOSE_PICTURE && data != null) {
            final String path = GetPathFromUri4kitkat.getPath(this, data.getData());
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Result result = scanningImage(path);
                    if (result != null) {
                        Message m = mScanHandler.obtainMessage();
                        m.what = PARSE_BARCODE_SUC;
                        m.obj = result.getText();
                        mScanHandler.sendMessage(m);
                    } else {
                        Message m = mScanHandler.obtainMessage();
                        m.what = PARSE_BARCODE_FAIL;
                        m.obj = "Scan failed!";
                        mScanHandler.sendMessage(m);
                    }
                }
            }).start();
        }
    }

    private static final long VIBRATE_DURATION = 200L;

    private void playBeepSoundAndVibrate() {
        Media.start(this, "sound/beep.ogg");
        if (isVibrate) {
            Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            vibrator.vibrate(VIBRATE_DURATION);
        }
    }

    /**
     * 扫描二维码图片的方法
     *
     * @param path
     * @return
     */
    @SuppressWarnings("JavaDoc")
    public Result scanningImage(String path) {
        if (TextUtils.isEmpty(path)) {
            return null;
        }
        Hashtable<DecodeHintType, String> hints = new Hashtable<>();
        hints.put(DecodeHintType.CHARACTER_SET, "UTF8"); // 设置二维码内容的编码

        try {
            RGBLuminanceSource source = new RGBLuminanceSource(path);
            BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(
                    source));
            QRCodeReader reader = new QRCodeReader();
            return reader.decode(binaryBitmap, hints);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return null;
    }

    private Handler mScanHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case PARSE_BARCODE_SUC:
                    onResultHandler((String) msg.obj);
                    break;
                case PARSE_BARCODE_FAIL:
                    ToastCompat.showText(ScanActivity.this, R.string.qrcode_toast_scan_failure);
                    break;

            }
            return false;
        }
    });

    /**
     * 跳转到上一个页面
     *
     * @param resultString
     */
    @SuppressWarnings("JavaDoc")
    private void onResultHandler(String resultString) {
        mInactivityTimer.onActivity();
        try {
            playBeepSoundAndVibrate();
        } catch (Exception ignored) {
        }
        // if (TextUtils.isEmpty(resultString)) {
        //     showText(R.string.toast_scan_failure);
        // } else {
        Intent resultIntent = new Intent();
        resultIntent.putExtra(Key.KEY_SCAN_RESULT, resultString);
        setResult(RESULT_OK, resultIntent);
        // }
        // showText(resultString);
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

}
