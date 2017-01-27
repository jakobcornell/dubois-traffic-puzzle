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
import android.os.SystemClock;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;
import android.widget.Toast;
import org.joda.time.DateTime;

import com.duboisproject.rushhour.Application;
import com.duboisproject.rushhour.BufferedHandler;
import com.duboisproject.rushhour.Board;
import com.duboisproject.rushhour.BoardLoader;
import com.duboisproject.rushhour.DriveListener;
import com.duboisproject.rushhour.GameStatistics;
import com.duboisproject.rushhour.TheSoundPool;
import com.duboisproject.rushhour.activities.HandlerActivity;
import com.duboisproject.rushhour.database.SdbInterface;
import com.duboisproject.rushhour.fragments.BoardLoaderFragment;
import com.duboisproject.rushhour.fragments.LoaderUiFragment;
import com.duboisproject.rushhour.fragments.PutStatisticsFragment;
import com.duboisproject.rushhour.fragments.ResultWrapper;
import com.duboisproject.rushhour.R;

/**
 * Playing the game
 */
public class GamePlayActivity extends Activity implements Board.SolveListener, HandlerActivity {
	public static final String BOARD_LOADER_TAG = "BOARD_LOAD";
	public static final String STATS_PUT_TAG = "STATS_PUT";
	protected final LoadHandler handler = new LoadHandler();

	public final class LoadHandler extends BufferedHandler {
		@Override
		protected void processMessage(Message message) {
			if (message.what == BoardLoaderFragment.MESSAGE_WHAT) {
				ResultWrapper<Board> result = (ResultWrapper<Board>) message.obj;
				GamePlayActivity.this.onBoardLoaded(result);
			} else if (message.what == PutStatisticsFragment.MESSAGE_WHAT) {
				ResultWrapper<Void> result = (ResultWrapper<Void>) message.obj;
				GamePlayActivity.this.onPutStatsResult(result);
			}
		}
	}

	public Handler getHandler() {
		return handler;
	}

	protected Board board;

	protected DateTime start;

	/*
	 * Used for timing gameplay.
	 */
	protected long startMillis;
	protected long resetMillis;

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
			loaderTransaction.add(loaderFragment, BOARD_LOADER_TAG);
			loaderTransaction.commit();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		handler.resume();
	}

	@Override
	public void onPause() {
		super.onPause();
		handler.pause();
	}

	/**
	 * Called by loader fragment when a load is completed.
	 * @param result  a wrapper around the fetched Board or the exception encountered
	 */
	protected void onBoardLoaded(ResultWrapper<Board> result) {
		Application app = (Application) getApplicationContext();
		Application.Toaster toaster = app.getToaster();
		board = null;

		try {
			board = result.getResult();
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
			transaction.remove(manager.findFragmentByTag(BOARD_LOADER_TAG));
			transaction.commit();

			setupBoard();
			start = new DateTime();
			startMillis = SystemClock.elapsedRealtime();
			resetMillis = startMillis;
		}
	}

	/**
	 * Called by loader fragment when a stats put is completed.
	 * @param result  a wrapper around any exception encountered
	 */
	protected void onPutStatsResult(ResultWrapper<Void> result) {
		Application app = (Application) getApplicationContext();
		Application.Toaster toaster = app.getToaster();
		board = null;

		try {
			result.getResult();
		} catch (IllegalArgumentException e) {
			toaster.toastError(e.getMessage());
			return;
		} catch (SdbInterface.RequestException e) {
			toaster.toastError("Request failed. Check network connection.");
			app.logError(e);
			return;
		} catch (Exception e) {
			toaster.toastError("An unexpected error occurred");
			app.logError(e);
			return;
		}

		toaster.toastMessage("Put success!");
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
		GameStatistics stats = new GameStatistics();
		stats.levelId = board.id;
		stats.moves = board.getScore();
		stats.startTime = start;
		long nowMillis = SystemClock.elapsedRealtime();
		stats.totalCompletionTime = new org.joda.time.Duration(startMillis, nowMillis);
		stats.resetCompletionTime = new org.joda.time.Duration(resetMillis, nowMillis);

		Application app = (Application) getApplicationContext();
		PutStatisticsFragment fragment = new PutStatisticsFragment(app.player, stats);
		FragmentManager manager = getFragmentManager();
		FragmentTransaction transaction = manager.beginTransaction();
		transaction.add(fragment, STATS_PUT_TAG);
		transaction.commit();
	}

	/**
	 * Just set up everything again for a reset
	 * @param v
	 */
	public void onClickHandler(View v) {
		switch (v.getId()) {
		case R.id.action_reset:
			setupBoard();
			resetMillis = SystemClock.elapsedRealtime();
			break;
		}
	}
}
