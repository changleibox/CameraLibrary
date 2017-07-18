/*
 * Copyright (c) All right reserved by Box
 */

package me.box.library.scanqrcode;

import android.content.Context;
import android.support.annotation.StringRes;
import android.text.TextUtils;
import android.widget.Toast;

import java.lang.ref.WeakReference;

/**
 * Created by Box on 17/3/9.
 * <p/>
 * 显示Toast
 */
class ToastCompat {

    private static WeakReference<Toast> mToastWeakReference;

    private static OnCreateToastListener mCreateToastListener;

    public synchronized static Object showText(Context context, CharSequence text) {
        if (TextUtils.isEmpty(text)) {
            return null;
        }
        Context applicationContext = context.getApplicationContext();
        Object o;
        if (mCreateToastListener != null && (o = mCreateToastListener.onCreateToast(applicationContext, text)) != null) {
            return o;
        }
        Toast toast = null;
        if (mToastWeakReference != null) {
            toast = mToastWeakReference.get();
        }
        if (toast == null) {
            toast = Toast.makeText(applicationContext, text, Toast.LENGTH_LONG);
            mToastWeakReference = new WeakReference<>(toast);
        }
        toast.setText(text);
        toast.show();
        return toast;
    }

    public static Object showText(Context context, @StringRes int res) {
        return showText(context, context.getString(res));
    }

    public static Object showText(Context context, @StringRes int res, Object... formatArgs) {
        return showText(context, context.getString(res, formatArgs));
    }

    /**
     * 注意：设置这个监听以后就要自己实现显示toast的方法，当回调方法的返回值为空的时候，显示Toast
     *
     * @param createToastListener 监听
     */
    public static void setOnCreateToastListener(OnCreateToastListener createToastListener) {
        ToastCompat.mCreateToastListener = createToastListener;
    }

    @SuppressWarnings("WeakerAccess")
    public interface OnCreateToastListener {
        Object onCreateToast(Context context, CharSequence text);
    }
}
