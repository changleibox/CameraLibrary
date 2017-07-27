package me.box.library.scanqrcode.provider;

import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.ColorInt;
import android.support.annotation.Dimension;
import android.support.annotation.DrawableRes;
import android.support.v4.media.RatingCompat;

import me.box.library.scanqrcode.R;
import me.box.library.scanqrcode.ScanQrcodeActivity;

/**
 * Created by box on 2017/7/18.
 * <p>
 * 扫描二维码配置
 */

public final class QrcodeConfig implements Parcelable {

    private String title;
    private String prompt;
    private int borderColor = Color.WHITE;
    private int divider = R.drawable.qrcode_img_scan_diver;
    private int theme = android.support.v7.appcompat.R.style.Theme_AppCompat;
    private int textColor = Color.WHITE;
    private int textSize = 14;
    private int scanImageIcon = R.drawable.qrcode_btn_scan_picture;
    private boolean hasFlashLight = true;
    private boolean canScanImage = true;
    private boolean isPlayBeep = true;
    private boolean isVibrate = true;
    private boolean displayHomeAsUpEnabled;

    transient private Class<? super ScanQrcodeActivity> subClass;

    public String getTitle() {
        return title;
    }

    public QrcodeConfig setTitle(String title) {
        this.title = title;
        return this;
    }

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

    public int getTextColor() {
        return textColor;
    }

    public QrcodeConfig setTextColor(@ColorInt int textColor) {
        this.textColor = textColor;
        return this;
    }

    public int getTextSize() {
        return textSize;
    }

    public QrcodeConfig setTextSize(@Dimension int textSize) {
        this.textSize = textSize;
        return this;
    }

    public int getScanImageIcon() {
        return scanImageIcon;
    }

    public QrcodeConfig setScanImageIcon(@DrawableRes int scanImageIcon) {
        this.scanImageIcon = scanImageIcon;
        return this;
    }

    public boolean isDisplayHomeAsUpEnabled() {
        return displayHomeAsUpEnabled;
    }

    public QrcodeConfig setDisplayHomeAsUpEnabled(boolean displayHomeAsUpEnabled) {
        this.displayHomeAsUpEnabled = displayHomeAsUpEnabled;
        return this;
    }

    public boolean isPlayBeep() {
        return isPlayBeep;
    }

    public QrcodeConfig setPlayBeep(boolean playBeep) {
        isPlayBeep = playBeep;
        return this;
    }

    public boolean isVibrate() {
        return isVibrate;
    }

    public QrcodeConfig setVibrate(boolean vibrate) {
        isVibrate = vibrate;
        return this;
    }

    Class<? super ScanQrcodeActivity> getSubClass() {
        return subClass;
    }

    public QrcodeConfig setSubClass(Class<? super ScanQrcodeActivity> subClass) {
        this.subClass = subClass;
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
        dest.writeString(this.title);
        dest.writeString(this.prompt);
        dest.writeInt(this.borderColor);
        dest.writeInt(this.divider);
        dest.writeInt(this.theme);
        dest.writeInt(this.textColor);
        dest.writeInt(this.textSize);
        dest.writeInt(this.scanImageIcon);
        dest.writeByte(this.hasFlashLight ? (byte) 1 : (byte) 0);
        dest.writeByte(this.canScanImage ? (byte) 1 : (byte) 0);
        dest.writeByte(this.isPlayBeep ? (byte) 1 : (byte) 0);
        dest.writeByte(this.isVibrate ? (byte) 1 : (byte) 0);
        dest.writeByte(this.displayHomeAsUpEnabled ? (byte) 1 : (byte) 0);
    }

    private QrcodeConfig(Parcel in) {
        this.title = in.readString();
        this.prompt = in.readString();
        this.borderColor = in.readInt();
        this.divider = in.readInt();
        this.theme = in.readInt();
        this.textColor = in.readInt();
        this.textSize = in.readInt();
        this.scanImageIcon = in.readInt();
        this.hasFlashLight = in.readByte() != 0;
        this.canScanImage = in.readByte() != 0;
        this.isPlayBeep = in.readByte() != 0;
        this.isVibrate = in.readByte() != 0;
        this.displayHomeAsUpEnabled = in.readByte() != 0;
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
