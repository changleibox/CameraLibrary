package me.box.library.scanqrcode.provider;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import me.box.library.scanqrcode.Constants.Key;
import me.box.library.scanqrcode.ScanQrcodeActivity;

import static android.graphics.Color.BLACK;

/**
 * Created by box on 2017/7/18.
 * <p>
 * 二维码
 */

@SuppressWarnings({"WeakerAccess", "unused"})
public final class QrcodeProvider {

    public static Bitmap createQrcode(String content, int size) throws WriterException {
        BitMatrix matrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, size, size);
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        int[] pixels = new int[width * height];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (matrix.get(x, y)) {
                    pixels[y * width + x] = BLACK;
                }
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }

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
