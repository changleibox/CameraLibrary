/*
 * Copyright (c) All right reserved by Box
 */

package me.box.library.foolcamera.compat;

import java.io.IOException;

/**
 * Created by Box on 17/3/15.
 * <p/>
 * 错误
 */
@SuppressWarnings({"ThrowableInstanceNeverThrown", "WeakerAccess"})
public class CameraException extends IOException {

    public static final CameraException ERROR_OTHER = new CameraException("OTHER ERROR!", -1);
    public static final CameraException ERROR_INSUFFICIENT_MEMORY = new CameraException("INSUFFICIENT MEMORY", -2);
    public static final CameraException ERROR_NO_SDCARD = new CameraException("NO SDCARD", -3);

    private final int code;

    public CameraException(String msg, int code) {
        super(msg);
        this.code = code;
    }

    public CameraException(String msg, int code, Throwable cause) {
        super(msg, cause);
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj) || (obj != null && obj instanceof CameraException && ((CameraException) obj).getCode() == getCode());
    }

    public boolean equals(CameraException e) {
        return equals(this, e);
    }

    public boolean equals(int code) {
        return this.code == code;
    }

    public static boolean equals(Throwable e1, CameraException e2) {
        return !(e1 == null || e2 == null) && e2.equals(e1);
    }

}
