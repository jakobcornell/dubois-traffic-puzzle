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
import android.app.AlertDialog;
import android.content.Intent;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Message;
import android.os.Handler;
import android.os.SystemClock;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;
import android.widget.Button;
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
	protected boolean replay;

	protected DateTime start;

	/*
	 * Used for timing gameplay.
	 */
	protected long startMillis;
	protected long resetMillis;

	boolean isFirstTime = true;

	public interface EventListener {
		public void onBoardDetach();
		public void onBoardReady();
	}

	protected final class UiListener implements EventListener {
		protected Button resetButton;
		protected ViewGroup boardLayout;

		public UiListener(Button resetButton, ViewGroup boardLayout) {
			this.resetButton = resetButton;
			this.boardLayout = boardLayout;
		}

		@Override
		public void onBoardDetach() {
			boardLayout.removeAllViews();
			resetButton.setEnabled(false);
		}

		@Override
		public void onBoardReady() {
			resetButton.setEnabled(true);
		}
	}

	protected EventListener uiListener;

	@Override
	protected void onCreate(Bundle savedState) {
		super.onCreate(savedState);

		setContentView(R.layout.activity_fullscreen);
		Button resetButton = (Button) findViewById(R.id.action_reset);
		ViewGroup boardLayout = (ViewGroup) findViewById(R.id.board);
		uiListener = new UiListener(resetButton, boardLayout);

		if (savedState == null) {
			onNewIntent(null);
		}
	}

	@Override
	public void onNewIntent(Intent intent) {
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

		uiListener.onBoardDetach();
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

	@Override
	public void onBackPressed() {
		if (board != null && board.getScore() > 0) {
			DialogInterface.OnClickListener finishListener = new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int id) {
					GamePlayActivity.this.finish();
				}
			};

			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("Really exit? Your progress on this level will be lost.");
			builder.setCancelable(false);
			builder.setPositiveButton("Exit", finishListener);
			builder.setNegativeButton("Cancel", null);
			builder.show();
		} else {
			finish();
		}
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

			uiListener.onBoardReady();
		}
	}

	protected final DialogInterface.OnClickListener continueListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int id) {
			Application app = (Application) getApplicationContext();
			replay = false;
			app.pendingDescriptor = new Board.ProgressDescriptor(app.player, app.getSdbInterface());
			GamePlayActivity.this.onNewIntent(null);
		}
	};

	protected final DialogInterface.OnClickListener replayListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int id) {
			Application app = (Application) getApplicationContext();
			replay = true;
			app.pendingDescriptor = new Board.IdDescriptor(board.id, app.getSdbInterface());
			GamePlayActivity.this.onNewIntent(null);
		}
	};

	/**
	 * Called by loader fragment when a stats put is completed.
	 * @param result  a wrapper around any exception encountered
	 */
	protected void onPutStatsResult(ResultWrapper<Void> result) {
		final Application app = (Application) getApplicationContext();
		Application.Toaster toaster = app.getToaster();
		boolean success = false;

		try {
			result.getResult();
			success = true;
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

		if (success) {
			if (!replay) {
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				String format = getResources().getString(R.string.replay_message);
				builder.setMessage(String.format(format, board.getScore()));
				builder.setCancelable(false);
				builder.setPositiveButton(getString(R.string.continue_text), continueListener);
				builder.setNegativeButton(getString(R.string.replay_text), replayListener);
				builder.show();
			} else {
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				String format = getResources().getString(R.string.solved_message);
				builder.setMessage(String.format(format, board.getScore()));
				builder.setCancelable(false);
				builder.setPositiveButton(getString(R.string.continue_text), continueListener);
				builder.show();
			}
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

		uiListener.onBoardDetach();
	}

	/**
	 * Just set up everything again for a reset
	 * @param v  view that was clicked
	 */
	public void onClickHandler(View v) {
		switch (v.getId()) {
		case R.id.action_reset:
			board.reset();
			resetMillis = SystemClock.elapsedRealtime();
			break;
		}
	}
}
