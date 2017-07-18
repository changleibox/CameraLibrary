/*
 * Copyright © 2017 CHANGLEI. All rights reserved.
 */

package me.box.library.foolcamera.ui;

import me.box.library.foolcamera.compat.CameraException;

/**
 * Created by box on 2017/5/3.
 * <p>
 * http 返回数据
 */

@SuppressWarnings("WeakerAccess")
public final class Response<T> {

    /**
     * code : string
     * data : {}
     * msg : string
     * success : true
     */

    private T data;
    private String code;
    private String msg;
    private boolean success;

    private Response() {
    }

    public int getCode() {
        int code = CameraException.ERROR_OTHER.getCode();
        try {
            code = Integer.valueOf(this.code);
        } catch (Exception ignored) {
        }
        return code;
    }

    public void setCode(int code) {
        this.code = String.valueOf(code);
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public void setException(CameraException e) {
        setCode(e.getCode());
        setMsg(e.getMessage());
    }

    public static <T> Response<T> createSuccess(T data) {
        Response<T> response = new Response<>();
        response.setSuccess(true);
        response.setData(data);
        return response;
    }

    public static <T> Response<T> createFailure(CameraException e) {
        Response<T> response = new Response<>();
        response.setSuccess(false);
        response.setException(e);
        return response;
    }

}
