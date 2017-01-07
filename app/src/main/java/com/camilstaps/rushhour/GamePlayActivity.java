/*
 *     Rush Hour Android app
 * Copyright (C) 2015 Randy Wanga, Jos Craaijo, Camil Staps
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package com.camilstaps.rushhour;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;

import java.io.InputStream;

/**
 * Playing the game
 */
public class GamePlayActivity extends Activity implements Board.SolveListener {

    Board board;

    boolean isFirstTime = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fullscreen);

        setupBoard();
    }

    /**
     * Create board, initialise listeners etc.
     */
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
                TheSoundPool.getSoundPool(getBaseContext()).play(TheSoundPool.soundCarDriveId, 1, 1, 1, 0, 1);
            }

            @Override
            public void onBlocked() {
                TheSoundPool.getSoundPool(getBaseContext()).play(TheSoundPool.soundCantMoveId, 1, 1, 1, 0, 1);
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

    @Override
    public void onSolve(int score) {
        Intent intent = new Intent(this, FinishedActivity.class);
        intent.putExtra("score", score);
        startActivityForResult(intent, 0);
    }

    /**
     * Just set up everything again for a reset
     * @param v
     */
    public void onClickHandler(View v) {
        switch (v.getId()) {
            case R.id.action_reset:
                setupBoard();
                break;
        }
    }

}
