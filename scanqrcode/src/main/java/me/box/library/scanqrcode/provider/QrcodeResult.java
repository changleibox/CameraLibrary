package me.box.library.scanqrcode.provider;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.io.ByteArrayOutputStream;

/**
 * Created by box on 2017/7/18.
 * <p>
 * 扫描返回值
 */

@SuppressWarnings("WeakerAccess")
public final class QrcodeResult implements Parcelable {

    private final String result;
    private final String barcodePath;

    transient private byte[] rawBarcode;

    private byte[] barcode;
    private boolean needResultBitmap;

    public QrcodeResult(String result, Bitmap barcode) {
        this(result, getBytes(barcode));
    }

    public QrcodeResult(String result, byte[] barcode) {
        this.needResultBitmap = true;
        this.result = result;
        this.rawBarcode = this.barcode = barcode;
        this.barcodePath = null;
    }

    public QrcodeResult(String result, String barcodePath) {
        this.needResultBitmap = true;
        this.result = result;
        this.barcodePath = barcodePath;
    }

    public void setNeedResultBitmap(boolean resultBitmap) {
        this.needResultBitmap = resultBitmap;
        this.barcode = resultBitmap ? rawBarcode : null;
    }

    @Nullable
    public String getResult() {
        return result;
    }

    @Nullable
    public Bitmap getBarcode() {
        if (!needResultBitmap || (barcode == null && TextUtils.isEmpty(barcodePath))) {
            return null;
        }
        if (!TextUtils.isEmpty(barcodePath)) {
            return BitmapFactory.decodeFile(barcodePath);
        }
        return BitmapFactory.decodeByteArray(barcode, 0, barcode.length);
    }

    public boolean isSuccess() {
        return !TextUtils.isEmpty(result) && (!needResultBitmap || barcode != null || !TextUtils.isEmpty(barcodePath));
    }

    public static byte[] getBytes(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.result);
        dest.writeString(this.barcodePath);
        dest.writeByteArray(this.barcode);
        dest.writeByte(this.needResultBitmap ? (byte) 1 : (byte) 0);
    }

    protected QrcodeResult(Parcel in) {
        this.result = in.readString();
        this.barcodePath = in.readString();
        this.barcode = in.createByteArray();
        this.needResultBitmap = in.readByte() != 0;
    }

    public static final Creator<QrcodeResult> CREATOR = new Creator<QrcodeResult>() {
        @Override
        public QrcodeResult createFromParcel(Parcel source) {
            return new QrcodeResult(source);
        }

        @Override
        public QrcodeResult[] newArray(int size) {
            return new QrcodeResult[size];
        }
    };
}
