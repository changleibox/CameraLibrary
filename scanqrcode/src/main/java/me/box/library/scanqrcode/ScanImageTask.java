package me.box.library.scanqrcode;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.mining.app.zxing.camera.CameraManager;
import com.mining.app.zxing.camera.PlanarYUVLuminanceSource;
import com.mining.app.zxing.decoding.DecodeFormatManager;
import com.mining.app.zxing.decoding.RGBLuminanceSource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.box.library.scanqrcode.FileUtils.GetPathFromUri4kitkat;
import me.box.library.scanqrcode.provider.QrcodeResult;

@SuppressWarnings("SuspiciousNameCombination")
public abstract class ScanImageTask extends AsyncTask<Void, Void, QrcodeResult> {

    private static final String UTF8 = "UTF8";
    private static final MultiFormatReader READER = new MultiFormatReader();

    static {
        READER.setHints(getHints());
    }

    private byte[] bytes;
    private Uri uri;
    private Context mContext;

    public ScanImageTask(byte[] bytes) {
        this.bytes = bytes;
    }

    public ScanImageTask(Context context, Uri uri) {
        this.mContext = context;
        this.uri = uri;
    }

    @Override
    protected QrcodeResult doInBackground(Void... uris) {
        if (isCancelled()) {
            return null;
        }
        boolean isYuv = false;
        if (mContext != null && uri != null) {
            isYuv = true;
            String path = GetPathFromUri4kitkat.getPath(mContext, uri);
            bytes = QrcodeResult.getBytes(BitmapFactory.decodeFile(path));
        }
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        return scanningImage(bytes, isYuv);
    }

    @Override
    protected abstract void onPostExecute(QrcodeResult result);

    private QrcodeResult scanningImage(byte[] bytes, boolean isYuv) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }

        if (!isYuv) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            RGBLuminanceSource source = new RGBLuminanceSource(bitmap);
            return decode(source, bitmap);
        }

        Point point = CameraManager.getInstance().getCameraResolution();
        if (point == null) {
            return null;
        }

        int width = point.x;
        int height = point.y;

        byte[] rotatedData = new byte[bytes.length];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                rotatedData[x * height + height - y - 1] = bytes[x + y * width];
            }
        }

        int tmp = width; // Here we are swapping, that's the difference to #11
        width = height;
        height = tmp;

        PlanarYUVLuminanceSource source = CameraManager.getInstance().buildLuminanceSource(rotatedData, width, height);
        return decode(source, source.renderCroppedGreyscaleBitmap());
    }

    private static QrcodeResult decode(LuminanceSource source, Bitmap bitmap) {
        Result result = null;
        try {
            result = READER.decodeWithState(new BinaryBitmap(new HybridBinarizer(source)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result == null ? null : new QrcodeResult(result.getText(), bitmap);
    }

    private static Map<DecodeHintType, Object> getHints() {
        Map<DecodeHintType, Object> hints = new HashMap<>();

        List<BarcodeFormat> decodeFormats = new ArrayList<>();
        decodeFormats.addAll(DecodeFormatManager.ONE_D_FORMATS);
        decodeFormats.addAll(DecodeFormatManager.QR_CODE_FORMATS);
        decodeFormats.addAll(DecodeFormatManager.DATA_MATRIX_FORMATS);

        hints.put(DecodeHintType.POSSIBLE_FORMATS, decodeFormats);
        hints.put(DecodeHintType.CHARACTER_SET, UTF8);
        return hints;
    }
}