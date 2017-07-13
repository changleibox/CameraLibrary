/*
 * Copyright © 2017 CHANGLEI. All rights reserved.
 */

package me.box.library.foolcamera.compat;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.Surface;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;

/**
 * Created by box on 2017/7/11.
 * <p>
 * 拍照
 */

@SuppressWarnings({"deprecation", "WeakerAccess"})
public class CameraCompat {

    private static final String TAG = CameraCompat.class.getSimpleName();
    public static final String EXTRAS_CAMERA_FACING = "android.intent.extras.CAMERA_FACING";

    public static int getCameraId(int cameraFacing) {
        int cameraCount;
        CameraInfo cameraInfo = new CameraInfo();
        cameraCount = Camera.getNumberOfCameras(); // get cameras number

        for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
            Camera.getCameraInfo(camIdx, cameraInfo); // get camerainfo
            if (cameraInfo.facing == cameraFacing) {
                return camIdx;
            }
        }
        return 0;
    }

    public static boolean hasCameraDevice(Context ctx) {
        return ctx.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    public static boolean hasFrontCamera(Context activity) {
        return activity.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT);
    }

    public static boolean isAutoFocusSupported(Camera.Parameters params) {
        List<String> modes = params.getSupportedFocusModes();
        return modes.contains(Camera.Parameters.FOCUS_MODE_AUTO);
    }

    public static Camera openCamera(int cameraId) {
        try {
            return Camera.open(cameraId);
        } catch (Exception e) {
            return null;
        }
    }

    public static void setCameraDefaultParamters(Camera camera) {
        if (camera == null) {
            return;
        }
        try {
            final Camera.Parameters params = camera.getParameters();
            params.setPictureFormat(PixelFormat.JPEG);
            if (isAutoFocusSupported(params)) {
                params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            }
            params.setSceneMode(Camera.Parameters.SCENE_MODE_AUTO);
            params.setAutoWhiteBalanceLock(true);
            camera.setParameters(params);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("SuspiciousNameCombination")
    public static void setCameraPictureSize(Context context, Camera camera, int width, int height) {
        if (camera == null) {
            return;
        }
        Camera.Parameters params = camera.getParameters();
        try {
            int orientation = context.getResources().getConfiguration().orientation;
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                params.setPreviewSize(width, height);
                params.setPictureSize(width, height);
            } else {
                params.setPreviewSize(height, width);
                params.setPictureSize(height, width);
            }
            camera.setParameters(params);
        } catch (Exception e) {
            Camera.Size previewSize = CameraCompat.getOptimalPreviewSize(params.getSupportedPreviewSizes(), width, height);
            Camera.Size pictureSize = CameraCompat.getOptimalPreviewSize(params.getSupportedPictureSizes(), width, height);
            params.setPreviewSize(previewSize.width, previewSize.height);
            params.setPictureSize(pictureSize.width, pictureSize.height);
            camera.setParameters(params);
        }
    }

    public static void setCameraDisplayOrientation(Activity activity, int cameraId, Camera camera) {
        camera.setDisplayOrientation(getCameraOrientation(activity, cameraId));
    }

    public static int getCameraOrientation(Activity activity, int cameraId) {
        CameraInfo info = new CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        if (rotation == Surface.ROTATION_0) {
            degrees = 0;
        } else if (rotation == Surface.ROTATION_90) {
            degrees = 90;
        } else if (rotation == Surface.ROTATION_180) {
            degrees = 180;
        } else if (rotation == Surface.ROTATION_270) {
            degrees = 270;
        }

        int result;
        if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        return result;
    }

    @Nullable
    public static File onPictureTaken(Activity activity, byte[] bytes, int orientation, int cameraId) throws CameraException {
        File pictureFile = CameraCompat.getExtrasOutput(activity);
        if (pictureFile == null) {
            pictureFile = CameraCompat.getOutputMediaFile(MEDIA_TYPE_IMAGE, bytes.length);
        }
        if (pictureFile == null) {
            throw CameraException.ERROR_OTHER;
        }

        try {
            CameraInfo info = new CameraInfo();
            Camera.getCameraInfo(cameraId, info);
            boolean isFacing = info.facing == CameraInfo.CAMERA_FACING_FRONT;

            Matrix matrix = new Matrix();
            if (orientation > 325 || orientation <= 45) {
                Log.w(TAG, "Surface.ROTATION_0 -------------> " + orientation);
                matrix.setRotate(isFacing ? -90 : 90);
            } else if (orientation > 45 && orientation <= 135) {
                Log.w(TAG, " Surface.ROTATION_270 -------------> " + orientation);
                matrix.setRotate(180);
            } else if (orientation > 135 && orientation < 225) {
                Log.w(TAG, "Surface.ROTATION_180 -------------> " + orientation);
                matrix.setRotate(isFacing ? 90 : -90);
            } else {
                Log.w(TAG, "Surface.ROTATION_90 -------------> " + orientation);
                matrix.setRotate(0);
            }

            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            FileOutputStream fos = new FileOutputStream(pictureFile);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.close();
        } catch (IOException ignored) {
            ignored.printStackTrace();
        }

        //notify
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            mediaScanIntent.setData(Uri.fromFile(pictureFile));
            activity.sendBroadcast(mediaScanIntent);
        } else {
            activity.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED,
                    Uri.parse("file://" + Environment.getExternalStorageDirectory())));
        }
        return pictureFile;
    }

    public static File getOutputMediaFile(int type, int length) throws CameraException {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            throw CameraException.ERROR_NO_SDCARD;
        }
        String storage = Environment.getExternalStorageDirectory().toString();
        StatFs fs = new StatFs(storage);
        long available = fs.getAvailableBlocks() * fs.getBlockSize();
        if (available < length) {
            throw CameraException.ERROR_INSUFFICIENT_MEMORY;
        }
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), "Camera");

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.e(TAG, "failed to create directory");
                throw CameraException.ERROR_OTHER;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");
        } else {
            throw CameraException.ERROR_OTHER;
        }

        return mediaFile;
    }

    @Nullable
    public static File getExtrasOutput(Activity activity) {
        Uri uri = activity.getIntent().getParcelableExtra(MediaStore.EXTRA_OUTPUT);
        String filePath = getFilePath(activity, uri);
        return TextUtils.isEmpty(filePath) ? null : new File(filePath);
    }

    public static Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) w / h;
        if (sizes == null) return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        // Try to find an size match aspect ratio and size
        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - h) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - h);
            }
        }

        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - h) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - h);
                }
            }
        }
        return optimalSize;
    }

    public static int getExtrasCameraFacing(Activity activity) {
        int cameraFacing = activity.getIntent().getIntExtra(EXTRAS_CAMERA_FACING, CameraInfo.CAMERA_FACING_BACK);
        return hasFrontCamera(activity) ? cameraFacing : CameraInfo.CAMERA_FACING_BACK;
    }

    @Nullable
    private static String getFilePath(final Context context, final Uri uri) {
        if (null == uri) return null;
        final String scheme = uri.getScheme();
        String data = null;
        if (scheme == null)
            data = uri.getPath();
        else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            data = uri.getPath();
        } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            Cursor cursor = context.getContentResolver().query(uri, new String[]{MediaStore.Images.ImageColumns.DATA}, null, null, null);
            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                    if (index > -1) {
                        data = cursor.getString(index);
                    }
                }
                cursor.close();
            }
        }
        return data;
    }

}
