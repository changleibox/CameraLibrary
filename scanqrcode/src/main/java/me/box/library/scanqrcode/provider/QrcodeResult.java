package me.box.library.scanqrcode.provider;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

/**
 * Created by box on 2017/7/18.
 * <p>
 * 扫描返回值
 */

public class QrcodeResult implements Parcelable {

    private final String result;
    private final Bitmap barcode;

    public QrcodeResult(String result, Bitmap barcode) {
        this.result = result;
        this.barcode = barcode;
    }

    @Nullable
    public String getResult() {
        return result;
    }

    @Nullable
    public Bitmap getBarcode() {
        return barcode;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.result);
        dest.writeParcelable(this.barcode, flags);
    }

    private QrcodeResult(Parcel in) {
        this.result = in.readString();
        this.barcode = in.readParcelable(Bitmap.class.getClassLoader());
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
