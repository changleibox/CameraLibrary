package me.box.library.scanqrcode;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;

import com.google.zxing.Result;
import com.mining.app.zxing.view.ViewfinderView;

/**
 * Created by box on 2019-10-28.
 */
public interface CaptureHandleImpl {

    ViewfinderView getViewfinderView();

    void handleDecode(Result obj, Bitmap barcode);

    void setResult(int resultOk, Intent obj);

    void startActivity(Intent intent);

    void finish();

    void drawViewfinder();

    Handler getHandler();
}
