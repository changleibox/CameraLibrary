/*
 * Copyright © 2017 CHANGLEI. All rights reserved.
 */

package me.box.library.scanqrcode;

import android.app.Activity;
import android.os.Build;
import android.view.View;

/**
 * Created by box on 2017/6/13.
 * <p>
 * 适配主题
 */

@SuppressWarnings("WeakerAccess")
public class ThemeCompat {

    private static final ThemeDelegate DELEGATE;

    static {
        DELEGATE = new NormalThemeDelegate();
    }

    public static void setLayoutFullscreen(Activity activity, View... views) {
        DELEGATE.setLayoutFullscreenFitSysytem(activity, views);
    }

    private static class NormalThemeDelegate implements ThemeDelegate {

        private KitkatThemeDelegate mKitkatThemeDelegate = new KitkatThemeDelegate();

        @Override
        public void setLayoutFullscreen(Activity activity) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                StatusBarHelper.setStatusBarMode(activity);
            } else {
                mKitkatThemeDelegate.setLayoutFullscreen(activity);
            }
        }

        @Override
        public void setLayoutFullscreenFitSysytem(Activity activity, View... views) {
            setLayoutFullscreen(activity);
        }

    }

    private static class KitkatThemeDelegate implements ThemeDelegate {

        @Override
        public void setLayoutFullscreen(Activity activity) {
            StatusBarHelper.setStatusBarMode(activity);
        }

        @Override
        public void setLayoutFullscreenFitSysytem(Activity activity, View... views) {
            setLayoutFullscreen(activity);
        }

    }

    private interface ThemeDelegate {

        void setLayoutFullscreen(Activity activity);

        void setLayoutFullscreenFitSysytem(Activity activity, View... views);

    }
}
