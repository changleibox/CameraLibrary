package me.box.library.scanqrcode.provider;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.v4.media.RatingCompat;

/**
 * Created by box on 2017/7/18.
 * <p>
 * 扫描二维码配置
 */

public class QrcodeConfig implements Parcelable {

    private String prompt;
    private int borderColor;
    private int divider;
    private int theme;
    private boolean hasFlashLight;
    private boolean canScanImage;

    public String getPrompt() {
        return prompt;
    }

    public QrcodeConfig setPrompt(String prompt) {
        this.prompt = prompt;
        return this;
    }

    @ColorInt
    public int getBorderColor() {
        return borderColor;
    }

    public QrcodeConfig setBorderColor(@ColorInt int borderColor) {
        this.borderColor = borderColor;
        return this;
    }

    @DrawableRes
    public int getDivider() {
        return divider;
    }

    public QrcodeConfig setDivider(@DrawableRes int divider) {
        this.divider = divider;
        return this;
    }

    public boolean isHasFlashLight() {
        return hasFlashLight;
    }

    public QrcodeConfig setHasFlashLight(boolean hasFlashLight) {
        this.hasFlashLight = hasFlashLight;
        return this;
    }

    public boolean isCanScanImage() {
        return canScanImage;
    }

    public QrcodeConfig setCanScanImage(boolean canScanImage) {
        this.canScanImage = canScanImage;
        return this;
    }

    public int getTheme() {
        return theme;
    }

    public QrcodeConfig setTheme(@RatingCompat.Style int theme) {
        this.theme = theme;
        return this;
    }

    public QrcodeConfig() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.prompt);
        dest.writeInt(this.borderColor);
        dest.writeInt(this.divider);
        dest.writeInt(this.theme);
        dest.writeByte(this.hasFlashLight ? (byte) 1 : (byte) 0);
        dest.writeByte(this.canScanImage ? (byte) 1 : (byte) 0);
    }

    private QrcodeConfig(Parcel in) {
        this.prompt = in.readString();
        this.borderColor = in.readInt();
        this.divider = in.readInt();
        this.theme = in.readInt();
        this.hasFlashLight = in.readByte() != 0;
        this.canScanImage = in.readByte() != 0;
    }

    public static final Creator<QrcodeConfig> CREATOR = new Creator<QrcodeConfig>() {
        @Override
        public QrcodeConfig createFromParcel(Parcel source) {
            return new QrcodeConfig(source);
        }

        @Override
        public QrcodeConfig[] newArray(int size) {
            return new QrcodeConfig[size];
        }
    };
}
