package me.box.library.scanqrcode.provider;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import java.io.ByteArrayOutputStream;

/**
 * Created by box on 2017/7/18.
 * <p>
 * 扫描返回值
 */

public class QrcodeResult implements Parcelable {

    private final String result;
    private final byte[] barcode;

    public QrcodeResult(String result, Bitmap barcode) {
        this(result, getBytes(barcode));
    }

    public QrcodeResult(String result, byte[] barcode) {
        this.result = result;
        this.barcode = barcode;
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.result);
        dest.writeByteArray(this.barcode);
    }

    private QrcodeResult(Parcel in) {
        this.result = in.readString();
        this.barcode = in.createByteArray();
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
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        return baos.toByteArray();//创建分配字节数组
    }

    private static Bitmap compressImage(Bitmap image, float kbSize) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        System.out.println("---------------------------------------->压缩到大小：" + kbSize * 1024);
        System.out.println("---------------------------------------->原始大小：" + baos.toByteArray().length);
        int options = 90;
        while (options >= 0 && baos.toByteArray().length > kbSize * 1024) {
            System.out.println("---------------------------------------->压缩后大小：" + baos.toByteArray().length);
            baos.reset();
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);
            options -= 10;
        }
        byte[] bytes = baos.toByteArray();
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

}
