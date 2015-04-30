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

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

/**
 * Launcher activity
 */
public class MainActivity extends ActionBarActivity {

    /**
     * Set contentView and start music
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TheSoundPool.getSoundPool(this);
    }

    /**
     * Handler for Start & Highscores button
     * @param v
     */
    public void onClickHandler(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.action_start:
                intent = new Intent(this, GamePlayActivity.class);
                startActivity(intent);
                break;
            case R.id.action_highscores:
                intent = new Intent(this, FinishedActivity.class);
                startActivity(intent);
                break;
        }
    }

}
