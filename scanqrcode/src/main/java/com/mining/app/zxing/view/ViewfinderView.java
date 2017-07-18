/*
 * Copyright (c) All right reserved by Box
 */

package com.mining.app.zxing.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.FloatRange;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import com.google.zxing.ResultPoint;
import com.mining.app.zxing.camera.CameraManager;

import java.util.Collection;
import java.util.HashSet;

import me.box.library.scanqrcode.R;

/**
 * This view is overlaid on top of the camera preview. It adds the viewfinder
 * rectangle and partial transparency outside it, as well as the laser scanner
 * animation and result points.
 */
@SuppressWarnings({"unused", "deprecation"})
public final class ViewfinderView extends View {

    @SuppressWarnings("unused")
    private static final String TAG = "log";
    private static final long ANIMATION_DELAY = 10L;
    private static final int OPAQUE = 0xFF;

    private int ScreenRate;

    private static final int CORNER_WIDTH = 6;
    public static final int MIDDLE_LINE_WIDTH = 6;

    public static final int MIDDLE_LINE_PADDING = 5;

    public static final int SPEEN_DISTANCE = 5;

    private static float density;
    @SuppressWarnings("unused")
    private static final int TEXT_PADDING_TOP = 40;
    public static final int CORNER_SIZE = 15;

    private Paint paint;

    private int slideTop;

    private Bitmap resultBitmap;
    private final int maskColor;
    private final int resultColor;

    private final int resultPointColor;
    private Collection<ResultPoint> possibleResultPoints;
    private Collection<ResultPoint> lastPossibleResultPoints;

    boolean isFirst;
    private Drawable lineDrawable;
    private Rect mRect;
    private String mText;
    private float mRate = 1F;
    private float mLocationRate = 0.5f;
    private int borderColor;
    private int textColor = Color.WHITE;
    private int textSize = 14;

    public ViewfinderView(Context context, AttributeSet attrs) {
        super(context, attrs);

        density = context.getResources().getDisplayMetrics().density;
        ScreenRate = (int) (CORNER_SIZE * density);

        paint = new Paint();
        Resources resources = getResources();
        maskColor = resources.getColor(R.color.qrcode_viewfinder_mask);
        resultColor = resources.getColor(R.color.qrcode_result_view);
        borderColor = resources.getColor(R.color.qrcode_color_qrcode_border);

        resultPointColor = resources.getColor(R.color.qrcode_possible_result_points);
        possibleResultPoints = new HashSet<>(5);

        lineDrawable = getResources().getDrawable(R.drawable.qrcode_img_scan_diver);
        mRect = new Rect();

        mText = getResources().getString(R.string.qrcode_label_default_scan_prompt);
    }

    public void setLineDrawable(@DrawableRes int lineDrawable) {
        Drawable drawable = getResources().getDrawable(R.drawable.qrcode_img_scan_diver);
        if (drawable != null) {
            this.lineDrawable = drawable;
        }
    }

    public void setBorderColor(@ColorInt int color) {
        borderColor = color;
    }

    public void setTextColor(@ColorInt int textColor) {
        this.textColor = textColor;
    }

    public void setTextSize(int textSize) {
        this.textSize = textSize;
    }

    public void setText(CharSequence sequence) {
        mText = sequence.toString();
    }

    public void setText(int resId) {
        setText(getResources().getString(resId));
    }

    public void setRate(float rate) {
        this.mRate = rate;
        isFirst = false;
        invalidate();
    }

    public void setLocationRate(@FloatRange(from = 0.1, to = .9) float rate) {
        this.mLocationRate = rate;
        isFirst = false;
        invalidate();
    }

    @SuppressLint("DrawAllocation")
    @Override
    public void onDraw(Canvas canvas) {
        CameraManager manager = CameraManager.getInstance();
        if (manager == null) {
            return;
        }
        manager.setRate(mRate);
        manager.setLocationRate(mLocationRate);
        Rect frame = manager.getFramingRect();
        if (frame == null) {
            return;
        }

        if (!isFirst) {
            isFirst = true;
            slideTop = frame.top;
            int slideBottom = frame.bottom;
        }

        int width = canvas.getWidth();
        int height = canvas.getHeight();

        paint.setColor(resultBitmap != null ? resultColor : maskColor);

        canvas.drawRect(0, 0, width, frame.top, paint);
        canvas.drawRect(0, frame.top, frame.left, frame.bottom + 1, paint);
        canvas.drawRect(frame.right + 1, frame.top, width, frame.bottom + 1,
                paint);
        canvas.drawRect(0, frame.bottom + 1, width, height, paint);

        if (resultBitmap != null) {
            // Draw the opaque result bitmap over the scanning rectangle
            paint.setAlpha(OPAQUE);
            canvas.drawBitmap(resultBitmap, frame.left, frame.top, paint);
        } else {
            paint.setColor(borderColor);
            canvas.drawRect(frame.left, frame.top, frame.left + ScreenRate,
                    frame.top + CORNER_WIDTH, paint);
            canvas.drawRect(frame.left, frame.top, frame.left + CORNER_WIDTH,
                    frame.top + ScreenRate, paint);
            canvas.drawRect(frame.right - ScreenRate, frame.top, frame.right,
                    frame.top + CORNER_WIDTH, paint);
            canvas.drawRect(frame.right - CORNER_WIDTH, frame.top, frame.right,
                    frame.top + ScreenRate, paint);
            canvas.drawRect(frame.left, frame.bottom - CORNER_WIDTH, frame.left
                    + ScreenRate, frame.bottom, paint);
            canvas.drawRect(frame.left, frame.bottom - ScreenRate, frame.left
                    + CORNER_WIDTH, frame.bottom, paint);
            canvas.drawRect(frame.right - ScreenRate, frame.bottom
                    - CORNER_WIDTH, frame.right, frame.bottom, paint);
            canvas.drawRect(frame.right - CORNER_WIDTH, frame.bottom
                    - ScreenRate, frame.right, frame.bottom, paint);

            // slideTop += SPEEN_DISTANCE;
            // if(slideTop >= frame.bottom){
            // slideTop = frame.top;
            // }
            // canvas.drawRect(frame.left + MIDDLE_LINE_PADDING, slideTop -
            // MIDDLE_LINE_WIDTH/2, frame.right - MIDDLE_LINE_PADDING,slideTop +
            // MIDDLE_LINE_WIDTH/2, paint);

            if (mRate <= 1) {
                if ((slideTop += 4) < (frame.bottom - frame.top)) {
                    mRect.set(frame.left + 6, frame.top + slideTop,
                            frame.right - 6, frame.top + 15 + slideTop);
                    lineDrawable.setBounds(mRect);
                    lineDrawable.draw(canvas);

                } else {
                    slideTop = 0;
                }
            }

            paint.setColor(textColor);
            paint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, textSize, getResources().getDisplayMetrics()));
            paint.setAlpha(140);
            paint.setAntiAlias(true);
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setSubpixelText(true);
            paint.setTypeface(Typeface.create("System", Typeface.BOLD));
            canvas.drawText(
                    mText,
                    getWidth() / 2,
                    frame.bottom + (float) TEXT_PADDING_TOP * density,
                    paint);

            Collection<ResultPoint> currentPossible = possibleResultPoints;
            Collection<ResultPoint> currentLast = lastPossibleResultPoints;
            if (currentPossible.isEmpty()) {
                lastPossibleResultPoints = null;
            } else {
                possibleResultPoints = new HashSet<>(5);
                lastPossibleResultPoints = currentPossible;
                paint.setAlpha(OPAQUE);
                paint.setColor(resultPointColor);
                for (ResultPoint point : currentPossible) {
                    canvas.drawCircle(frame.left + point.getX(), frame.top
                            + point.getY(), 6.0f, paint);
                }
            }
            if (currentLast != null) {
                paint.setAlpha(OPAQUE / 2);
                paint.setColor(resultPointColor);
                for (ResultPoint point : currentLast) {
                    canvas.drawCircle(frame.left + point.getX(), frame.top
                            + point.getY(), 3.0f, paint);
                }
            }

            postInvalidateDelayed(ANIMATION_DELAY, frame.left, frame.top,
                    frame.right, frame.bottom);
        }
    }

    public void drawViewfinder() {
        resultBitmap = null;
        invalidate();
    }

    /**
     * Draw a bitmap with the result points highlighted instead of the live
     * scanning display.
     *
     * @param barcode An image of the decoded barcode.
     */
    public void drawResultBitmap(Bitmap barcode) {
        resultBitmap = barcode;
        invalidate();
    }

    public void addPossibleResultPoint(ResultPoint point) {
        possibleResultPoints.add(point);
    }

}
