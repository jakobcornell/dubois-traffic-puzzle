/*
 * Dubois Traffic Puzzle
 * Jakob Cornell, 2017
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
import android.os.Bundle;
import android.os.Message;
import android.os.Handler;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.widget.Toast;
import android.widget.TextView;
import android.content.Context;
import android.content.Intent;

import com.duboisproject.rushhour.Application;
import com.duboisproject.rushhour.BufferedHandler;
import com.duboisproject.rushhour.Board;
import com.duboisproject.rushhour.id.Coach;
import com.duboisproject.rushhour.activities.GamePlayActivity;
import com.duboisproject.rushhour.fragments.LoaderUiFragment;
import com.duboisproject.rushhour.fragments.CoachLoaderFragment;
import com.duboisproject.rushhour.fragments.ResultWrapper;
import com.duboisproject.rushhour.database.SdbInterface;
import com.duboisproject.rushhour.R;

public class CoachIdActivity extends IdActivity implements HandlerActivity {
	protected static final String LOADER_FRAGMENT_TAG = "COACH_ID";
	protected final LoadHandler handler = new LoadHandler();

	public final class LoadHandler extends BufferedHandler {
		@Override
		protected void processMessage(Message message) {
			if (message.what == CoachLoaderFragment.MESSAGE_WHAT) {
				ResultWrapper<Coach> result = (ResultWrapper<Coach>) message.obj;
				CoachIdActivity.this.onLoadFinished(result);
			}
		}
	}

	public Handler getHandler() {
		return handler;
	}

	@Override
	public void onCreate(Bundle savedState) {
		super.onCreate(savedState);
		TextView textView = (TextView) findViewById(R.id.fragment_text);
		textView.setText(R.string.coach_scan_message);
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
	protected void onNewId(String id) {
		FragmentManager manager = getFragmentManager();
		loaderFragment = (CoachLoaderFragment) manager.findFragmentByTag(LOADER_FRAGMENT_TAG);
		if (loaderFragment == null) {
			loaderFragment = new CoachLoaderFragment(id);
			LoaderUiFragment uiFragment = new LoaderUiFragment();

			FragmentTransaction uiTransaction = manager.beginTransaction();
			uiTransaction.add(R.id.loader_container, uiFragment, LoaderUiFragment.TAG);
			uiTransaction.addToBackStack(LoaderUiFragment.TAG);
			uiTransaction.commit();

			FragmentTransaction loaderTransaction = manager.beginTransaction();
			loaderTransaction.add(loaderFragment, LOADER_FRAGMENT_TAG);
			loaderTransaction.commit();
		}
	}

	public void onLoadFinished(ResultWrapper<Coach> wrapper) {
		Coach coach = null;
		Application app = (Application) getApplicationContext();
		Application.Toaster toaster = app.getToaster();

		try {
			coach = wrapper.getResult();
		} catch (IllegalArgumentException e) {
			toaster.toastError(e.getMessage());
			app.logError(e);
		} catch (SdbInterface.RequestException e) {
			toaster.toastError("Request failed. Check network connection.");
			app.logError(e);
		} catch (Throwable e) {
			toaster.toastError("Error #1 occurred CoachIdActivity.java");
			app.logError(e);
		}
	
		FragmentManager manager = getFragmentManager();
		manager.popBackStack(LoaderUiFragment.TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE);
		FragmentTransaction uiRemoval = manager.beginTransaction();
		uiRemoval.remove(manager.findFragmentByTag(LOADER_FRAGMENT_TAG));
		uiRemoval.commit();

		if (coach != null) {
			app.pendingDescriptor = new Board.ProgressDescriptor(app.player, app.getSdbInterface());
			Intent intent = new Intent(this, GamePlayActivity.class);
			finish();
			startActivity(intent);
		}
	}
}
