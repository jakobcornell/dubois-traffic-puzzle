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
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.widget.Toast;
import android.widget.TextView;
import android.content.Context;

import com.duboisproject.rushhour.BufferedHandler;
import com.duboisproject.rushhour.id.Mathlete;
import com.duboisproject.rushhour.fragments.LoaderUiFragment;
import com.duboisproject.rushhour.fragments.MathleteLoaderFragment;
import com.duboisproject.rushhour.fragments.ResultWrapper;
import com.duboisproject.rushhour.database.SdbInterface;
import com.duboisproject.rushhour.R;

public final class MathleteIdActivity extends IdActivity {
	protected static final String LOADER_FRAGMENT_TAG = "MATHLETE_ID";
	protected static final String UI_FRAGMENT_ID = "LOADER";
	public static final int MESSAGE_WHAT = MathleteIdActivity.class.hashCode();
	public LoadHandler handler = new LoadHandler();

	public final class LoadHandler extends BufferedHandler {
		@Override
		protected void processMessage(Message message) {
			if (message.what == MESSAGE_WHAT) {
				ResultWrapper<Mathlete> result = (ResultWrapper<Mathlete>) message.obj;
				MathleteIdActivity.this.onLoadFinished(result);
			}
		}
	}

	@Override
	public void onCreate(Bundle savedState) {
		super.onCreate(savedState);
		TextView textView = (TextView) findViewById(R.id.fragment_text);
		textView.setText(R.string.mathlete_scan_message);
	}

	@Override
	protected void onNewId(String id) {
		FragmentManager manager = getFragmentManager();
		loaderFragment = (MathleteLoaderFragment) manager.findFragmentByTag(LOADER_FRAGMENT_TAG);
		if (loaderFragment == null) {
			loaderFragment = new MathleteLoaderFragment(id);
			LoaderUiFragment uiFragment = new LoaderUiFragment();

			FragmentTransaction uiTransaction = manager.beginTransaction();
			uiTransaction.add(R.id.loader_container, uiFragment, UI_FRAGMENT_ID);
			uiTransaction.addToBackStack(UI_FRAGMENT_ID);
			uiTransaction.commit();

			FragmentTransaction loaderTransaction = manager.beginTransaction();
			loaderTransaction.add(loaderFragment, LOADER_FRAGMENT_TAG);
			loaderTransaction.commit();
		}
	}

	public void onLoadFinished(ResultWrapper<Mathlete> wrapper) {
		Mathlete mathlete = null;
		String errorPrefix = getResources().getString(R.string.error_prefix);
		Context appContext = getApplicationContext();

		try {
			mathlete = wrapper.getResult();
		} catch (IllegalArgumentException e) {
			Toast.makeText(appContext, errorPrefix + e.getMessage(), Toast.LENGTH_LONG).show();
		} catch (SdbInterface.RequestException e) {
			String message = errorPrefix + "Request failed. Check network connection.";
			Toast.makeText(appContext, message, Toast.LENGTH_LONG).show();
		} catch (Exception e) {
			Toast.makeText(appContext, "What happened?", Toast.LENGTH_SHORT).show();
		}
		
		if (mathlete != null) {
			String messageFormat = getResources().getString(R.string.welcome_message);
			String message = String.format(messageFormat, mathlete.firstName);
			Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
		}

		FragmentManager manager = getFragmentManager();
		manager.popBackStack(UI_FRAGMENT_ID, FragmentManager.POP_BACK_STACK_INCLUSIVE);
		FragmentTransaction uiRemoval = manager.beginTransaction();
		uiRemoval.remove(manager.findFragmentByTag(LOADER_FRAGMENT_TAG));
		uiRemoval.commit();
	}
}
