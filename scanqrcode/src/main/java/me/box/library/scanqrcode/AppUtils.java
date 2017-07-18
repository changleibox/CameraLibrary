/*
 * Copyright (c) All right reserved by Box
 */
package me.box.library.scanqrcode;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Box
 *         <p/>
 *         2015年7月16日
 */
final class AppUtils {

    static boolean checkSelfPermission(@NonNull Activity context, int requestCode, @NonNull String... permissions) {
        List<String> notPermissions = new ArrayList<>();
        if (!checkSelfPermissions(context.getApplication(), notPermissions, permissions)) {
            ActivityCompat.requestPermissions(context, notPermissions.toArray(new String[notPermissions.size()]), requestCode);
            return false;
        }
        return true;
    }

    public static boolean checkSelfPermissions(@NonNull Context context, @NonNull String... permissions) {
        return checkSelfPermissions(context, null, permissions);
    }

    private static boolean checkSelfPermissions(@NonNull Context context, List<String> notPermissions, @NonNull String... permissions) {
        if (notPermissions == null) {
            notPermissions = new ArrayList<>();
        }
        // boolean hasPermission = true;
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                // hasPermission = false;
                // break;
                notPermissions.add(permission);
            }
        }
        return notPermissions.size() == 0;
    }

    static boolean hasSelfPermission(@NonNull int[] grantResults) {
        for (int grantResult : grantResults) {
            if (grantResult != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

}
