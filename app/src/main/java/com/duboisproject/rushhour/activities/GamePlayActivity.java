/*
 *     Rush Hour Android app
 * Copyright (C) 2015 Randy Wanga, Jos Craaijo, Camil Staps
 *
 * Modified by Jakob Cornell, 2017-01-25 to -01-26
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

package com.duboisproject.rushhour.activities;

import java.io.Serializable;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.os.Handler;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;
import android.widget.Toast;

// temp
import android.util.Log;

import com.duboisproject.rushhour.Application;
import com.duboisproject.rushhour.BufferedHandler;
import com.duboisproject.rushhour.Board;
import com.duboisproject.rushhour.BoardLoader;
import com.duboisproject.rushhour.DriveListener;
import com.duboisproject.rushhour.TheSoundPool;
import com.duboisproject.rushhour.activities.HandlerActivity;
import com.duboisproject.rushhour.database.SdbInterface;
import com.duboisproject.rushhour.fragments.BoardLoaderFragment;
import com.duboisproject.rushhour.fragments.LoaderUiFragment;
import com.duboisproject.rushhour.fragments.ResultWrapper;
import com.duboisproject.rushhour.R;

/**
 * Playing the game
 */
public class GamePlayActivity extends Activity implements Board.SolveListener, HandlerActivity {
	public static final String LOADER_FRAGMENT_TAG = "BOARD_LOAD";
	protected final LoadHandler handler = new LoadHandler();

	public final class LoadHandler extends BufferedHandler {
		@Override
		protected void processMessage(Message message) {
			if (message.what == BoardLoaderFragment.MESSAGE_WHAT) {
				ResultWrapper<Board> result = (ResultWrapper<Board>) message.obj;
				GamePlayActivity.this.onLoadFinished(result);
			}
		}
	}

	public Handler getHandler() {
		return handler;
	}

	Board board;

	boolean isFirstTime = true;

	@Override
	protected void onCreate(Bundle savedState) {
		super.onCreate(savedState);

		setContentView(R.layout.activity_fullscreen);

		if (savedState == null) {
			Application app = (Application) getApplicationContext();
			Board.Descriptor descriptor = app.pendingDescriptor;
			app.pendingDescriptor = null;

			BoardLoaderFragment loaderFragment = new BoardLoaderFragment(descriptor);
			LoaderUiFragment uiFragment = new LoaderUiFragment();
			FragmentManager manager = getFragmentManager();

			FragmentTransaction uiTransaction = manager.beginTransaction();
			uiTransaction.add(R.id.board, uiFragment, LoaderUiFragment.TAG);
			uiTransaction.commit();

			FragmentTransaction loaderTransaction = manager.beginTransaction();
			loaderTransaction.add(loaderFragment, LOADER_FRAGMENT_TAG);
			loaderTransaction.commit();
		}
	}

	/**
	 * Called by loader fragment when a load is completed.
	 * @param result  a wrapper around the fetched Board or the exception encountered
	 */
	protected void onLoadFinished(ResultWrapper<Board> result) {
		Application app = (Application) getApplicationContext();
		Application.Toaster toaster = app.getToaster();
		board = null;

		try {
			board = result.getResult();
			app.logError(new Exception()); // testing
		} catch (IllegalArgumentException e) {
			toaster.toastError(e.getMessage());
			finish();
		} catch (SdbInterface.RequestException e) {
			toaster.toastError("Request failed. Check network connection.");
			app.logError(e);
			finish();
		} catch (Exception e) {
			toaster.toastError("An unexpected error occurred");
			app.logError(e);
			finish();
		}

		if (board != null) {
			FragmentManager manager = getFragmentManager();
			FragmentTransaction transaction = manager.beginTransaction();
			transaction.remove(manager.findFragmentByTag(LoaderUiFragment.TAG));
			transaction.remove(manager.findFragmentByTag(LOADER_FRAGMENT_TAG));
			transaction.commit();

			setupBoard();
		}
	}

	/**
	 * Create board, initialise listeners etc.
	 */
	private void setupBoard() {
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
