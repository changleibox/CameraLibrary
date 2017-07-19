package me.box.library.scanqrcode;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.util.HashMap;
import java.util.Map;

import static android.graphics.Color.BLACK;
import static android.graphics.Color.WHITE;

/**
 * Created by Box on 2017/7/19.
 * <p>
 * 生成二维码
 */

@SuppressWarnings("WeakerAccess")
public final class CreateQrcodeTask extends AsyncTask<Void, Void, Bitmap> {

    private static final String UTF_8 = "UTF-8";
    private static final QRCodeWriter WRITER = new QRCodeWriter(1, getHints());

    private String mContent;
    private int mWidth;
    private int mHeight;

    private Callback mCallback;

    private CreateQrcodeTask(String content, int width, int height) {
        this.mContent = content;
        this.mWidth = width;
        this.mHeight = height;
    }

    public CreateQrcodeTask setCallback(Callback callback) {
        this.mCallback = callback;
        return this;
    }

    @Override
    protected Bitmap doInBackground(Void... voids) {
        if (isCancelled()) {
            return null;
        }
        Bitmap bitmap = null;
        try {
            bitmap = createQrcode(mContent, mWidth, mHeight);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if (isCancelled()) {
            return;
        }
        if (mCallback != null) {
            mCallback.onCallback(bitmap);
        }
    }

    public static CreateQrcodeTask create(String content, int width, int height, Callback callback) {
        return (CreateQrcodeTask) new CreateQrcodeTask(content, width, height).setCallback(callback).execute();
    }

    private Bitmap createQrcode(String content, int width, int height) throws Exception {
        if (TextUtils.isEmpty(content) || width <= 0 || height <= 0) {
            return null;
        }
        BitMatrix matrix = WRITER.encode(content, BarcodeFormat.QR_CODE, width, height);
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (matrix.get(x, y)) {
                    pixels[y * width + x] = BLACK;
                } else {
                    pixels[y * width + x] = WHITE;
                }
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }

    private static Map<EncodeHintType, Object> getHints() {
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, UTF_8);
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
        return hints;
    }

    public interface Callback {
        void onCallback(Bitmap bitmap);
    }
}
