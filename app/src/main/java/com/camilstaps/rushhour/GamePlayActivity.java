package com.camilstaps.rushhour;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;

import java.io.InputStream;

public class GamePlayActivity extends Activity implements Board.SolveListener {

    private SoundPool soundPool;
    private int soundBackgroundId, soundCarDriveId, soundCantMoveId;

    Board board;

    boolean isFirstTime = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fullscreen);

        setupSoundPool();
        setupBoard();
    }

    private void setupBoard() {
        InputStream input = getResources().openRawResource(R.raw.level);

        BoardLoader loader = new BoardLoader();
        board = loader.loadBoard(input);

        final RelativeLayout boardLayout = (RelativeLayout) findViewById(R.id.board);
        if (isFirstTime) {
            ViewTreeObserver vto = boardLayout.getViewTreeObserver();
            vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    boardLayout.removeAllViews();
                    boardLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    board.addToLayout(getBaseContext(), boardLayout);
                }
            });
        } else {
            boardLayout.removeAllViews();
            board.addToLayout(getBaseContext(), boardLayout);
        }

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

        isFirstTime = false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // See http://stackoverflow.com/a/13483049/1544337
        if (resultCode == Activity.RESULT_OK) {
            finish();
        }
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
        startActivityForResult(intent, 0);
    }

    public void onClickHandler(View v) {
        switch (v.getId()) {
            case R.id.action_reset:
                setupBoard();
                break;
        }
    }

}
