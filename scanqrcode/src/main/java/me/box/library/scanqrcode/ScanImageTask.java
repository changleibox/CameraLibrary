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

@SuppressWarnings({"SuspiciousNameCombination", "WeakerAccess"})
public final class ScanImageTask extends AsyncTask<Void, Void, QrcodeResult> {

    private static final String UTF8 = "UTF8";
    private static final MultiFormatReader READER = new MultiFormatReader();

    static {
        READER.setHints(getHints());
    }

    private byte[] mData;
    private Uri mUri;
    private Context mContext;
    private Callback mCallback;

    private ScanImageTask(byte[] data) {
        this.mData = data;
    }

    private ScanImageTask(Context context, Uri uri) {
        this.mContext = context;
        this.mUri = uri;
    }

    private ScanImageTask setCallback(Callback callback) {
        this.mCallback = callback;
        return this;
    }

    @Override
    protected QrcodeResult doInBackground(Void... voids) {
        if (isCancelled()) {
            return null;
        }
        boolean isYuv = false;
        if (mContext != null && mUri != null) {
            isYuv = true;
            String path = GetPathFromUri4kitkat.getPath(mContext, mUri);
            mData = QrcodeResult.getBytes(BitmapFactory.decodeFile(path));
        }
        if (mData == null || mData.length == 0) {
            return null;
        }
        return scanningImage(mData, isYuv);
    }

    @Override
    protected void onPostExecute(QrcodeResult result) {
        if (isCancelled() || mCallback == null) {
            return;
        }
        mCallback.onCallback(result);
    }

    public static ScanImageTask scan(byte[] data, Callback callback) {
        return (ScanImageTask) new ScanImageTask(data).setCallback(callback).execute();
    }

    public static ScanImageTask scan(Context context, Uri uri, Callback callback) {
        return (ScanImageTask) new ScanImageTask(context, uri).setCallback(callback).execute();
    }

    private QrcodeResult scanningImage(byte[] data, boolean isYuv) {
        if (data == null || data.length == 0) {
            return null;
        }

        if (!isYuv) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            RGBLuminanceSource source = new RGBLuminanceSource(bitmap);
            return decode(source, bitmap);
        }

        Point point = CameraManager.getInstance().getCameraResolution();
        if (point == null) {
            return null;
        }

        int width = point.x;
        int height = point.y;

        byte[] rotatedData = new byte[data.length];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                rotatedData[x * height + height - y - 1] = data[x + y * width];
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
        return result == null ? new QrcodeResult(null, (byte[]) null) : new QrcodeResult(result.getText(), bitmap);
    }

    private static Map<DecodeHintType, Object> getHints() {
        Map<DecodeHintType, Object> hints = new HashMap<>();

        List<BarcodeFormat> decodeFormats = new ArrayList<>();
        decodeFormats.addAll(DecodeFormatManager.ONE_D_FORMATS);
        decodeFormats.addAll(DecodeFormatManager.QR_CODE_FORMATS);
        decodeFormats.addAll(DecodeFormatManager.DATA_MATRIX_FORMATS);

        hints.put(DecodeHintType.PURE_BARCODE, Boolean.TRUE);
        hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
        hints.put(DecodeHintType.POSSIBLE_FORMATS, decodeFormats);
        hints.put(DecodeHintType.CHARACTER_SET, UTF8);
        return hints;
    }

    public interface Callback {
        void onCallback(QrcodeResult result);
    }
}