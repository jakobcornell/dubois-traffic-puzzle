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

package com.duboisproject.rushhour.fragments;

import android.os.Bundle;
import android.os.Message;
import android.content.Context;
import android.content.Loader;
import android.content.AsyncTaskLoader;

import com.duboisproject.rushhour.Application;
import com.duboisproject.rushhour.id.Coach;
import com.duboisproject.rushhour.activities.CoachIdActivity;

public final class CoachLoaderFragment extends LoaderFragment<ResultWrapper<Coach>> {
	protected static final String COACH_ID_KEY = "COACH_ID";

	/**
	 * ID of this loader, used by the LoaderManager.
	 * This identifies the type of loader rather than any particular loader.
	 */
	protected static final int LOADER_ID = CoachLoader.class.hashCode();

	protected String coachId;

	public CoachLoaderFragment() {}

	protected static final class CoachLoader extends AsyncTaskLoader<ResultWrapper<Coach>> {
		protected final Context context;
		protected final String coachId;

		public CoachLoader(Context context, String coachId) {
			super(context);
			this.context = context;
			this.coachId = coachId;
		}

		public ResultWrapper<Coach> loadInBackground() {
			ResultWrapper<Coach> wrapper = new ResultWrapper<Coach>();
			Application app = (Application) context.getApplicationContext();
			try {
				wrapper.setResult(app.getSdbInterface().fetchCoach(coachId));
			} catch (Exception exception) {
				wrapper.setException(exception);
			}
			return wrapper;
		}
	}

	public CoachLoaderFragment(String coachId) {
		this.coachId = coachId;
	}

	@Override
	public void onCreate(Bundle savedState) {
		setRetainInstance(true);
		super.onCreate(savedState);
		if (savedState != null) {
			coachId = (String) savedState.getCharSequence(COACH_ID_KEY);
		}
		Loader l = getLoaderManager().initLoader(LOADER_ID, null, this);
		if (savedState == null) {
			// necessary due to bug in Android
			l.forceLoad();
		}
	}

	@Override
	public void onActivityCreated(Bundle savedState) {
		super.onActivityCreated(savedState);
	}

	@Override
	public Loader<ResultWrapper<Coach>> onCreateLoader(int id, Bundle args) {
		return new CoachLoader(getActivity(), coachId);
	}

	@Override
	public void onLoadFinished(Loader<ResultWrapper<Coach>> loader, ResultWrapper<Coach> wrapper) {
		CoachIdActivity activity = (CoachIdActivity) host;
		Message message = activity.handler.obtainMessage(CoachIdActivity.MESSAGE_WHAT);
		message.obj = wrapper;
		activity.handler.sendMessage(message);
	}

	@Override
	public void onSaveInstanceState(Bundle state) {
		state.putCharSequence(COACH_ID_KEY, coachId);
	}
}
