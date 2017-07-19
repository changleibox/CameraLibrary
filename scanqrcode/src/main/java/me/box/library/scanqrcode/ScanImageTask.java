package me.box.library.scanqrcode;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import me.box.library.scanqrcode.FileUtils.GetPathFromUri4kitkat;
import me.box.library.scanqrcode.provider.QrcodeResult;

@SuppressWarnings({"WeakerAccess"})
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
        if (mContext != null && mUri != null) {
            String path = GetPathFromUri4kitkat.getPath(mContext, mUri);
            mData = QrcodeResult.getBytes(BitmapFactory.decodeFile(path));
        }
        if (mData == null || mData.length == 0) {
            return null;
        }
        return scanningImage(mData);
    }

    @Override
    protected void onPostExecute(QrcodeResult result) {
        if (isCancelled() || mCallback == null) {
            return;
        }
        mCallback.onCallback(result);
    }

    public static ScanImageTask scan(Bitmap data, Callback callback) {
        return scan(QrcodeResult.getBytes(data), callback);
    }

    public static ScanImageTask scan(byte[] data, Callback callback) {
        return (ScanImageTask) new ScanImageTask(data).setCallback(callback).execute();
    }

    static ScanImageTask scan(Context context, Uri uri, Callback callback) {
        return (ScanImageTask) new ScanImageTask(context, uri).setCallback(callback).execute();
    }

    private QrcodeResult scanningImage(byte[] data) {
        if (data == null || data.length == 0) {
            return null;
        }

        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        return decode(new RGBLuminanceSource(width, height, pixels), bitmap);
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

        hints.put(DecodeHintType.PURE_BARCODE, Boolean.TRUE);
        hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
        hints.put(DecodeHintType.POSSIBLE_FORMATS, Arrays.asList(BarcodeFormat.values()));
        hints.put(DecodeHintType.CHARACTER_SET, UTF8);
        return hints;
    }

    public interface Callback {
        void onCallback(QrcodeResult result);
    }
}