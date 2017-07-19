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
    private final byte[] barcode;
    private final boolean isSuccess;

    public QrcodeResult(String result, Bitmap barcode) {
        this(result, getBytes(barcode));
    }

    public QrcodeResult(String result, byte[] barcode) {
        this.result = result;
        this.barcode = barcode;
        this.isSuccess = !TextUtils.isEmpty(result) && barcode != null && barcode.length > 0;
    }

    @Nullable
    public String getResult() {
        return result;
    }

    @Nullable
    public Bitmap getBarcode() {
        if (barcode == null) {
            return null;
        }
        return BitmapFactory.decodeByteArray(barcode, 0, barcode.length);
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.result);
        dest.writeByteArray(this.barcode);
        dest.writeByte(this.isSuccess ? (byte) 1 : (byte) 0);
    }

    private QrcodeResult(Parcel in) {
        this.result = in.readString();
        this.barcode = in.createByteArray();
        this.isSuccess = in.readByte() != 0;
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

    public static byte[] getBytes(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }
}
