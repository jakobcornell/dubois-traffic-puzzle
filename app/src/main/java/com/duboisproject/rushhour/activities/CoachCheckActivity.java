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
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.widget.Toast;
import android.widget.TextView;
import android.content.Context;
import android.content.Intent;

import com.duboisproject.rushhour.Application;
import com.duboisproject.rushhour.id.Coach;
import com.duboisproject.rushhour.fragments.LoaderUiFragment;
import com.duboisproject.rushhour.fragments.CoachLoaderFragment;
import com.duboisproject.rushhour.fragments.ResultWrapper;
import com.duboisproject.rushhour.fragments.TextFragment;
import com.duboisproject.rushhour.database.SdbInterface;
import com.duboisproject.rushhour.R;

public class CoachCheckActivity extends CoachIdActivity {
	// For describing why the activity was launched. Passed through intent.
	public static final String REASON_KEY = "CHECK_REASON";
	public static enum Reason {
		TIME, ERROR;
	}

	@Override
	public void onCreate(Bundle savedState) {
		Intent intent = getIntent();
		Reason reason = (Reason) intent.getSerializableExtra(REASON_KEY);
		if (reason == Reason.ERROR) {
			setTheme(R.style.Error);
		}

		super.onCreate(savedState);
		setContentView(R.layout.check_activity);

		FragmentManager manager = getFragmentManager();
		TextFragment messageFragment = (TextFragment) manager.findFragmentById(R.id.check_message);
		messageFragment.setText(getString(R.string.coach_scan_message));

		TextFragment detailFragment = (TextFragment) manager.findFragmentById(R.id.check_detail);
		TextView detailText = (TextView) detailFragment.getView().findViewById(R.id.fragment_text);
		detailText.setTextSize(getResources().getDimension(R.dimen.small_text));

		if (reason == Reason.ERROR) {
			detailFragment.setText(getString(R.string.log_detail));
		} else {
			detailFragment.setText(getString(R.string.time_detail));
		}
	}

	@Override
	public void onBackPressed() {
		Application app = (Application) getApplicationContext();
		app.getToaster().toastMessage(getString(R.string.coach_required));
	}

	public void onLoadFinished(ResultWrapper<Coach> wrapper) {
		Coach coach = null;
		Application app = (Application) getApplicationContext();
		Application.Toaster toaster = app.getToaster();

		try {
			coach = wrapper.getResult();
		} catch (IllegalArgumentException e) {
			toaster.toastError(e.getMessage());
		} catch (SdbInterface.RequestException e) {
			toaster.toastError("Request failed. Check network connection.");
			app.logError(e);
		} catch (Exception e) {
			toaster.toastError("Error #1 occurred CoachCheckActivity.java");
			app.logError(e);
		}
	
		FragmentManager manager = getFragmentManager();
		manager.popBackStack(LoaderUiFragment.TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE);
		FragmentTransaction uiRemoval = manager.beginTransaction();
		uiRemoval.remove(manager.findFragmentByTag(LOADER_FRAGMENT_TAG));
		uiRemoval.commit();

		if (coach != null) {
			setResult(RESULT_OK);
			finish();
		}
	}
}
