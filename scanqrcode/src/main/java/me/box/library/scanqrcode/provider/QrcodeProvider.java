package me.box.library.scanqrcode.provider;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.media.RatingCompat;

import me.box.library.scanqrcode.Constants.Key;
import me.box.library.scanqrcode.ScanActivity;

/**
 * Created by box on 2017/7/18.
 * <p>
 * 二维码
 */

public final class QrcodeProvider {

    public static void scanQrcode(@NonNull Activity activity, @Nullable String prompt, @RatingCompat.Style int theme, int requestCode) {
        Intent intent = new Intent(activity, ScanActivity.class);
        intent.putExtra(Key.KEY_SCAN_PROMPT, prompt);
        intent.putExtra(Key.KEY_SCAN_THEME, theme);
        activity.startActivityForResult(intent, requestCode);
    }

    @Nullable
    public static String getScanResult(@Nullable Intent data) {
        return data != null ? data.getStringExtra(Key.KEY_SCAN_RESULT) : null;
    }
}
