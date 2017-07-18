/**
 * Copyright © All right reserved by IZHUO.NET.
 */
package me.box.library.scanqrcode;

import android.app.Service;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.os.Vibrator;

/**
 * @author Changlei
 *         <p>
 *         2015年1月6日
 */
public class Media {

    public static void start(Context context, String path) {
        try {
            AssetFileDescriptor fileDescriptor = context.getAssets().openFd(path);
            @SuppressWarnings("unused")
            Vibrator vibrator = (Vibrator) context.getSystemService(Service.VIBRATOR_SERVICE);
            // vibrator.vibrate(100);
            @SuppressWarnings("deprecation") SoundPool soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 5);
            // 载入音频流，返回在池中的id
            final int sourceid = soundPool.load(fileDescriptor, 1);
            // 播放音频，第二个参数为左声道音量;第三个参数为右声道音量;第四个参数为优先级；
            // 第五个参数为循环次数，0不循环，-1循环;第六个参数为速率，速率最低0.5最高为2，1代表正常速度
            soundPool.setOnLoadCompleteListener(new OnLoadCompleteListener() {
                public void onLoadComplete(SoundPool soundPool, int sampleId,
                                           int status) {
                    soundPool.play(sourceid, 2, 2, 0, 0, 1);
                }
            });
        } catch (Exception ignored) {
        }
    }
}
