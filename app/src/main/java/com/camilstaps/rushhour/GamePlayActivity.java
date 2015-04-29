package com.camilstaps.rushhour;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;

import java.io.InputStream;

public class GamePlayActivity extends Activity implements Board.SolveListener {

    private SoundPool soundPool;
    private int soundBackgroundId, soundCarDriveId, soundCantMoveId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fullscreen);

        setupSoundPool();

        InputStream input = getResources().openRawResource(R.raw.level);

        BoardLoader loader = new BoardLoader();
        final Board board = loader.loadBoard(input);

        final RelativeLayout boardLayout = (RelativeLayout) findViewById(R.id.board);
        ViewTreeObserver vto = boardLayout.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                boardLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                board.addToLayout(getBaseContext(), boardLayout);
            }
        });

        /*
         * Sounds on move and attempt to move
         */
        board.setDriveListener(new DriveListener() {
            @Override
            public void onDrive() {
                soundPool.play(soundCarDriveId, 1, 1, 1, 0, 1);
            }

            @Override
            public void onBlocked() {
                soundPool.play(soundCantMoveId, 1, 1, 1, 0, 1);
            }
        });

        board.setSolveListener(this);
    }

    /**
     * Load sounds; start background music
     */
    protected void setupSoundPool() {
        soundPool = new SoundPool(2, AudioManager.STREAM_MUSIC, 0);
        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                if (sampleId == soundBackgroundId) {
                    soundPool.play(soundBackgroundId, 1, 1, 2, -1, 1);
                }
            }
        });
        soundBackgroundId = soundPool.load(this, R.raw.tune, 2);
        soundCarDriveId = soundPool.load(this, R.raw.car_drive, 1);
        soundCantMoveId = soundPool.load(this, R.raw.cantmove, 1);
    }

    @Override
    public void onSolve(int score) {
        Intent intent = new Intent(this, FinishedActivity.class);
        intent.putExtra("score", score);
        startActivity(intent);
    }
}
