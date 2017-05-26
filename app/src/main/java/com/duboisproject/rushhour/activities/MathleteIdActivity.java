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

import android.os.Bundle;
import android.os.Message;
import android.os.Handler;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.widget.TextView;
import android.content.Intent;

import com.duboisproject.rushhour.Application;
import com.duboisproject.rushhour.BufferedHandler;
import com.duboisproject.rushhour.id.Mathlete;
import com.duboisproject.rushhour.fragments.LoaderUiFragment;
import com.duboisproject.rushhour.fragments.MathleteLoaderFragment;
import com.duboisproject.rushhour.fragments.ResultWrapper;
import com.duboisproject.rushhour.database.SdbInterface;
import com.duboisproject.rushhour.R;

public final class MathleteIdActivity extends IdActivity implements HandlerActivity {
	protected static final String LOADER_FRAGMENT_TAG = "MATHLETE_ID";
	protected final LoadHandler handler = new LoadHandler();

	public final class LoadHandler extends BufferedHandler {
		@Override
		protected void processMessage(Message message) {
			if (message.what == MathleteLoaderFragment.MESSAGE_WHAT) {
				ResultWrapper<Mathlete> result = (ResultWrapper<Mathlete>) message.obj;
				MathleteIdActivity.this.onLoadFinished(result);
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
		textView.setText(R.string.mathlete_scan_message);
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
		loaderFragment = (MathleteLoaderFragment) manager.findFragmentByTag(LOADER_FRAGMENT_TAG);
		if (loaderFragment == null) {
			loaderFragment = new MathleteLoaderFragment(id);
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

	public void onLoadFinished(ResultWrapper<Mathlete> wrapper) {
		Mathlete mathlete = null;
		Application app = (Application) getApplicationContext();
		Application.Toaster toaster = app.getToaster();

		try {
			mathlete = wrapper.getResult();
		} catch (IllegalArgumentException e) {
			toaster.toastError(e.getMessage());
		} catch (SdbInterface.RequestException e) {
			toaster.toastError("Request failed. Check network connection.");
			app.logError(e);
		} catch (Throwable e) {
			toaster.toastError("Error #1 occurred MathleteIdActivity.java");
			app.logError(e);
		}

		FragmentManager manager = getFragmentManager();
		manager.popBackStack(LoaderUiFragment.TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE);
		FragmentTransaction uiRemoval = manager.beginTransaction();
		uiRemoval.remove(manager.findFragmentByTag(LOADER_FRAGMENT_TAG));
		uiRemoval.commit();

		if (mathlete != null) {
			app.player = mathlete;
			String messageFormat = getResources().getString(R.string.welcome_message);
			String message = String.format(messageFormat, mathlete.firstName);
			toaster.toastMessage(message);
			startActivity(new Intent(this, CoachIdActivity.class));
		}
	}
}
