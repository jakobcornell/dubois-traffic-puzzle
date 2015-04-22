package com.camilstaps.rushhour;

import android.app.Activity;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewTreeObserver;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.camilstaps.rushhour.util.SystemUiHider;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class FullscreenActivity extends Activity {

    private SoundPool soundPool;
    private boolean soundBackgroundLoaded = false;
    private int soundBackgroundId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fullscreen);

        final Board board = new Board();
        board.add(new Car(new Coordinate(0,0), new Coordinate(2,0), Color.YELLOW));
        board.add(new Car(new Coordinate(3,0), new Coordinate(3,1), Color.rgb(128,223,182)));
        board.add(new Car(new Coordinate(4,0), new Coordinate(4,2), Color.rgb(198, 134,221)));
        board.add(new Car(new Coordinate(0,2), new Coordinate(1,2), Color.RED));
        board.add(new Car(new Coordinate(5,2), new Coordinate(5,3), Color.rgb(255,165,0)));
        board.add(new Car(new Coordinate(0,3), new Coordinate(0,4), Color.rgb(158,231,246)));
        board.add(new Car(new Coordinate(1,3), new Coordinate(2,3), Color.rgb(245,158,246)));
        board.add(new Car(new Coordinate(3,3), new Coordinate(4,3), Color.rgb(150,126,196)));
        board.add(new Car(new Coordinate(1,4), new Coordinate(2,4), Color.GREEN));
        board.add(new Car(new Coordinate(3,4), new Coordinate(3,5), Color.BLACK));
        board.add(new Car(new Coordinate(5,4), new Coordinate(5,5), Color.rgb(219,202,161)));
        board.add(new Car(new Coordinate(0,5), new Coordinate(2,5), Color.rgb(25,195,167)));

        soundPool = new SoundPool(2, AudioManager.STREAM_MUSIC, 0);
        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                soundBackgroundLoaded = true;
            }
        });
        soundBackgroundId = soundPool.load(this, R.raw.car_drive, 1);

        final RelativeLayout boardLayout = (RelativeLayout) findViewById(R.id.board);
        ViewTreeObserver vto = boardLayout.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                boardLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                board.addToLayout(getBaseContext(), boardLayout);
            }
        });

        board.setDriveListener(new DriveListener() {
            @Override
            public void onDrive() {
                if (soundBackgroundLoaded) {
                    soundPool.play(soundBackgroundId, 1, 1, 1, 0, 1f);
                }
            }
        });
    }
}
