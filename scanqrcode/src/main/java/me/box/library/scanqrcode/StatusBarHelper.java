/*
 * Copyright (c) All right reserved by Box
 */

package me.box.library.scanqrcode;

import android.app.Activity;
import android.os.Build;
import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

class StatusBarHelper {
    @IntDef({
            OTHER,
            MIUI,
            FLYME,
            ANDROID_M
    })
    @Retention(RetentionPolicy.SOURCE)
    @interface SystemType {
    }

    static final int OTHER = -1;
    static final int MIUI = 1;
    static final int FLYME = 2;
    static final int ANDROID_M = 3;

    static int setStatusBarMode(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return StatusBarHelper.setStatusBarMode(activity,
                    AttrUtils.getBoolean(activity, android.R.attr.windowLightStatusBar, false));
        }
        return 0;
    }

    private static int setStatusBarMode(Activity activity, boolean isFontColorDark) {
        @SystemType int result = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (new MIUIHelper().setStatusBarLightMode(activity, isFontColorDark)) {
                result = MIUI;
            } else if (new FlymeHelper().setStatusBarLightMode(activity, isFontColorDark)) {
                result = FLYME;
            } else if (new AndroidMHelper().setStatusBarLightMode(activity, isFontColorDark)) {
                result = ANDROID_M;
            }
        }
        return result;
    }

    /**
     * 设置状态栏黑色字体图标，
     * 适配4.4以上版本MIUIV、Flyme和6.0以上版本其他Android
     *
     * @return 1:MIUI 2:Flyme 3:android6.0
     */
    public static int setStatusBarLightMode(Activity activity) {
        @SystemType int result = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (new MIUIHelper().setStatusBarLightMode(activity, true)) {
                result = MIUI;
            } else if (new FlymeHelper().setStatusBarLightMode(activity, true)) {
                result = FLYME;
            } else if (new AndroidMHelper().setStatusBarLightMode(activity, true)) {
                result = ANDROID_M;
            }
        }
        return result;
    }

    /**
     * 设置状态栏白色字体图标，
     * 适配4.4以上版本MIUIV、Flyme和6.0以上版本其他Android
     *
     * @return 1:MIUI 2:Flyme 3:android6.0
     */
    public static int setStatusBarDarkMode(Activity activity) {
        @SystemType int result = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (new MIUIHelper().setStatusBarLightMode(activity, false)) {
                result = MIUI;
            } else if (new FlymeHelper().setStatusBarLightMode(activity, false)) {
                result = FLYME;
            } else if (new AndroidMHelper().setStatusBarLightMode(activity, false)) {
                result = ANDROID_M;
            }
        }
        return result;
    }

    /**
     * 已知系统类型时，设置状态栏黑色字体图标。
     * 适配4.4以上版本MIUI6、Flyme和6.0以上版本其他Android
     *
     * @param type 1:MIUI 2:Flyme 3:android6.0
     */
    public static void setStatusBarLightMode(Activity activity, @SystemType int type) {
        statusBarMode(activity, type, true);
    }

    /**
     * 清除MIUI或flyme或6.0以上版本状态栏白色字体
     *
     * @param type 1:MIUI 2:Flyme 3:android6.0
     */
    public static void statusBarDarkMode(Activity activity, @SystemType int type) {
        statusBarMode(activity, type, false);
    }

    private static void statusBarMode(Activity activity, @SystemType int type, boolean isFontColorDark) {
        if (type == MIUI) {
            new MIUIHelper().setStatusBarLightMode(activity, isFontColorDark);
        } else if (type == FLYME) {
            new FlymeHelper().setStatusBarLightMode(activity, isFontColorDark);
        } else if (type == ANDROID_M) {
            new AndroidMHelper().setStatusBarLightMode(activity, isFontColorDark);
        }
    }

}