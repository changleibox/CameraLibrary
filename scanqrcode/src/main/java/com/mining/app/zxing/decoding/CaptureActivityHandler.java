/*
 * Copyright (c) All right reserved by Box
 */

package com.mining.app.zxing.decoding;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.mining.app.zxing.camera.CameraManager;
import com.mining.app.zxing.view.ViewfinderResultPointCallback;

import java.util.Vector;

import me.box.library.scanqrcode.CaptureHandleImpl;
import me.box.library.scanqrcode.R;

/**
 * This class handles all the messaging which comprises the state machine for
 * capture.
 */
public final class CaptureActivityHandler extends Handler {

    private static final String TAG = CaptureActivityHandler.class
            .getSimpleName();

    private final CaptureHandleImpl activity;
    private final DecodeThread decodeThread;
    private State state;

    private enum State {
        PREVIEW, SUCCESS, DONE
    }

    public CaptureActivityHandler(CaptureHandleImpl activity, Vector<BarcodeFormat> decodeFormats, String characterSet) {
        this.activity = activity;
        decodeThread = new DecodeThread(activity, decodeFormats, characterSet,
                new ViewfinderResultPointCallback(activity.getViewfinderView()));
        decodeThread.start();
        state = State.SUCCESS;
        // Start ourselves capturing previews and decoding.
        CameraManager.getInstance().startPreview();
        restartPreviewAndDecode();
    }

    @Override
    public void handleMessage(Message message) {
        if (message.what == R.id.qrcode_auto_focus) {// Log.d(TAG, "Got auto-focus message");
            // When one auto focus pass finishes, start another. This is the
            // closest thing to
            // continuous AF. It does seem to hunt a bit, but I'm not sure what
            // else to do.
            if (state == State.PREVIEW) {
                CameraManager.getInstance().requestAutoFocus(this,
                        R.id.qrcode_auto_focus);
            }

        } else if (message.what == R.id.qrcode_restart_preview) {
            Log.d(TAG, "Got restart preview message");
            restartPreviewAndDecode();

        } else if (message.what == R.id.qrcode_decode_succeeded) {
            Log.d(TAG, "Got decode succeeded message");
            state = State.SUCCESS;
            Bundle bundle = message.getData();

            /***********************************************************************/
            Bitmap barcode = bundle == null ? null : (Bitmap) bundle
                    .getParcelable(DecodeThread.BARCODE_BITMAP);// ���ñ����߳�

            activity.handleDecode((Result) message.obj, barcode);// ���ؽ��?
            // /***********************************************************************/

        } else if (message.what == R.id.qrcode_decode_failed) {// We're decoding as fast as possible, so when one decode fails,
            // start another.
            state = State.PREVIEW;
            CameraManager.getInstance().requestPreviewFrame(
                    decodeThread.getHandler(), R.id.qrcode_decode);

        } else if (message.what == R.id.qrcode_return_scan_result) {
            Log.d(TAG, "Got return scan result message");
            activity.setResult(Activity.RESULT_OK, (Intent) message.obj);
            activity.finish();

        } else if (message.what == R.id.qrcode_launch_product_query) {
            Log.d(TAG, "Got product query message");
            String url = (String) message.obj;
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            activity.startActivity(intent);

        }
    }

    public void quitSynchronously() {
        state = State.DONE;
        CameraManager.getInstance().stopPreview();
        Message quit = Message.obtain(decodeThread.getHandler(), R.id.qrcode_quit);
        quit.sendToTarget();
        try {
            decodeThread.join();
        } catch (InterruptedException e) {
            // continue
        }

        // Be absolutely sure we don't send any queued up messages
        removeMessages(R.id.qrcode_decode_succeeded);
        removeMessages(R.id.qrcode_decode_failed);
    }

    private void restartPreviewAndDecode() {
        if (state == State.SUCCESS) {
            state = State.PREVIEW;
            CameraManager.getInstance().requestPreviewFrame(
                    decodeThread.getHandler(), R.id.qrcode_decode);
            CameraManager.getInstance().requestAutoFocus(this, R.id.qrcode_auto_focus);
            activity.drawViewfinder();
        }
    }

}
