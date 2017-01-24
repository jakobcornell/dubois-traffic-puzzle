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
import com.duboisproject.rushhour.id.Mathlete;
import com.duboisproject.rushhour.activities.MathleteIdActivity;

public final class MathleteLoaderFragment extends LoaderFragment<ResultWrapper<Mathlete>> {
	protected static final String MATHLETE_ID_KEY = "MATHLETE_ID";

	/**
	 * ID of this loader, used by the LoaderManager.
	 * This identifies the type of loader rather than any particular loader.
	 */
	protected static final int LOADER_ID = MathleteLoader.class.hashCode();

	protected String mathleteId;

	public MathleteLoaderFragment() {}

	protected static final class MathleteLoader extends AsyncTaskLoader<ResultWrapper<Mathlete>> {
		protected final Context context;
		protected final String mathleteId;

		public MathleteLoader(Context context, String mathleteId) {
			super(context);
			this.context = context;
			this.mathleteId = mathleteId;
		}

		public ResultWrapper<Mathlete> loadInBackground() {
			ResultWrapper<Mathlete> wrapper = new ResultWrapper<Mathlete>();
			Application app = (Application) context.getApplicationContext();
			try {
				wrapper.setResult(app.getSdbInterface().fetchMathlete(mathleteId));
			} catch (Exception exception) {
				wrapper.setException(exception);
			}
			return wrapper;
		}
	}

	public MathleteLoaderFragment(String mathleteId) {
		this.mathleteId = mathleteId;
	}

	@Override
	public void onCreate(Bundle savedState) {
		setRetainInstance(true);
		super.onCreate(savedState);
		if (savedState != null) {
			mathleteId = (String) savedState.getCharSequence(MATHLETE_ID_KEY);
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
	public Loader<ResultWrapper<Mathlete>> onCreateLoader(int id, Bundle args) {
		return new MathleteLoader(getActivity(), mathleteId);
	}

	@Override
	public void onLoadFinished(Loader<ResultWrapper<Mathlete>> loader, ResultWrapper<Mathlete> wrapper) {
		MathleteIdActivity activity = (MathleteIdActivity) host;
		Message message = activity.handler.obtainMessage(MathleteIdActivity.MESSAGE_WHAT);
		message.obj = wrapper;
		activity.handler.sendMessage(message);
	}

	@Override
	public void onSaveInstanceState(Bundle state) {
		state.putCharSequence(MATHLETE_ID_KEY, mathleteId);
	}
}
