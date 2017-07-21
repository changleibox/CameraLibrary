package me.box.library.scanqrcode;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.support.annotation.DrawableRes;
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

    public static Bitmap addLogo(Context context, Bitmap qrBitmap, @DrawableRes int drawableId, float scale, int padding) {
        return addLogo(qrBitmap, BitmapFactory.decodeResource(context.getResources(), drawableId), scale, padding);
    }

    public static Bitmap addLogo(Bitmap qrBitmap, Bitmap logoBitmap, float scale, int padding) {
        int qrBitmapWidth = qrBitmap.getWidth();
        int qrBitmapHeight = qrBitmap.getHeight();
        int logoBitmapWidth = logoBitmap.getWidth();
        int logoBitmapHeight = logoBitmap.getHeight();
        Bitmap blankBitmap = Bitmap.createBitmap(qrBitmapWidth, qrBitmapHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(blankBitmap);
        canvas.drawBitmap(qrBitmap, 0, 0, null);
        canvas.save();

        canvas.scale(scale, scale, qrBitmapWidth / 2, qrBitmapHeight / 2);
        int left = (qrBitmapWidth - logoBitmapWidth) / 2;
        int top = (qrBitmapHeight - logoBitmapHeight) / 2;

        final int strokeWidth = 1;
        final int strokeColor = 0xff999999;
        final int radius = 15;

        Paint paintStroke = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintStroke.setColor(strokeColor);
        paintStroke.setAntiAlias(true);
        paintStroke.setStrokeWidth(strokeWidth);
        paintStroke.setStyle(Paint.Style.STROKE);

        RectF rect = new RectF(left - padding, top - padding, left + logoBitmapWidth + padding, top + logoBitmapHeight + padding);
        canvas.drawRoundRect(rect, radius, radius, paintStroke);
        canvas.save();

        Paint paintRect = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintRect.setColor(Color.WHITE);
        paintRect.setAntiAlias(true);

        rect = new RectF(rect.left + strokeWidth, rect.top + strokeWidth, rect.right - strokeWidth, rect.bottom - strokeWidth);
        canvas.drawRoundRect(rect, radius, radius, paintRect);
        canvas.save();

        canvas.drawBitmap(logoBitmap, left, top, null);
        canvas.restore();
        return blankBitmap;
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
