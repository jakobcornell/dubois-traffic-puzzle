package com.camilstaps.rushhour;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

/**
 * Wrapper for SoundPool for app-wide sounds
 * Created by camilstaps on 29-4-15.
 */
public class TheSoundPool {

    private static SoundPool soundPool;
    public static int soundBackgroundId, soundCarDriveId, soundCantMoveId, soundVictoryId;

    public static SoundPool getSoundPool(Context context) {
        if (soundPool == null) {
            soundPool = new SoundPool(2, AudioManager.STREAM_MUSIC, 0);
            soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
                @Override
                public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                    if (sampleId == soundBackgroundId) {
                        soundPool.play(soundBackgroundId, 1, 1, 2, -1, 1);
                    }
                }
            });
            soundBackgroundId = soundPool.load(context, R.raw.tune, 2);
            soundCarDriveId = soundPool.load(context, R.raw.car_drive, 1);
            soundCantMoveId = soundPool.load(context, R.raw.cantmove, 1);
            soundVictoryId = soundPool.load(context, R.raw.victory, 1);
        }
        return soundPool;
    }

}
