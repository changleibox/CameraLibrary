package me.box.library.scanqrcode.provider;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import me.box.library.scanqrcode.Constants.Key;
import me.box.library.scanqrcode.ScanQrcodeActivity;

/**
 * Created by box on 2017/7/18.
 * <p>
 * 二维码
 */

@SuppressWarnings({"WeakerAccess", "unused"})
public final class QrcodeProvider {

    public static void scanQrcode(@NonNull Activity activity, @NonNull QrcodeConfig config, int requestCode) {
        scanQrcode(activity, config, null, requestCode);
    }

    public static void scanQrcode(@NonNull Activity activity, @NonNull QrcodeConfig config, Bitmap bitmap, int requestCode) {
        Intent intent = new Intent(activity, ScanQrcodeActivity.class);
        intent.putExtra(Key.KEY_SCAN_CONFIG, config);
        intent.putExtra(Key.KEY_SCAN_BITMAP, QrcodeResult.getBytes(bitmap));
        activity.startActivityForResult(intent, requestCode);
    }

    @Nullable
    public static QrcodeResult getScanResult(@Nullable Intent data) {
        return data != null ? (QrcodeResult) data.getParcelableExtra(Key.KEY_SCAN_RESULT) : null;
    }
}
