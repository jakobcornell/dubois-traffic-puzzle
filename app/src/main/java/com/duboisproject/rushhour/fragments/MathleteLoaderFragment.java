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

// test
import android.util.Log;

import java.util.Collection;
import java.util.HashSet;
import android.os.Bundle;
import android.content.Context;
import android.content.Loader;
import android.content.AsyncTaskLoader;

import com.duboisproject.rushhour.Application;
import com.duboisproject.rushhour.id.Mathlete;
import com.duboisproject.rushhour.database.SdbInterface;

public final class MathleteLoaderFragment extends LoaderFragment<ResultWrapper<Mathlete>> {
	protected static final String MATHLETE_ID_KEY = "mathleteId";

	/**
	 * ID of this loader, used by the LoaderManager.
	 * This identifies the type of loader rather than any particular loader.
	 */
	protected static final int LOADER_ID = MathleteLoader.class.hashCode();

	protected String mathleteId;
	protected Collection<Listener> completionListeners = new HashSet<Listener>();

	public MathleteLoaderFragment() {}

	public interface Listener {
		public void onLoadFinished(ResultWrapper<Mathlete> wrapper);
	}

	protected static final class MathleteLoader extends AsyncTaskLoader<ResultWrapper<Mathlete>> {
		protected final Context context;
		protected final String mathleteId;

		public MathleteLoader(Context context, String mathleteId) {
			super(context);
			this.context = context;
			this.mathleteId = mathleteId;
		}

		public ResultWrapper<Mathlete> loadInBackground() {
			Log.d("rushhour", "starting to calc result");
			// use mathleteId of outer class
			// TODO
			ResultWrapper<Mathlete> wrapper = new ResultWrapper<Mathlete>();
			Application app = (Application) context.getApplicationContext();
			try {
				wrapper.setResult(app.getSdbInterface().fetchMathlete(mathleteId));
			} catch (Exception exception) {
				wrapper.setException(exception);
			}
			Log.d("rushhour", "calculated a result");
			return wrapper;
		}

		// TODO remove
		public void deliverResult(ResultWrapper<Mathlete> wrapper) {
			Log.d("rushhour", "got a result");
			super.deliverResult(wrapper);
		}
	}

	public MathleteLoaderFragment(String mathleteId) {
		this.mathleteId = mathleteId;
	}

	public void registerListener(Listener l) {
		completionListeners.add(l);
	}

	public void unregisterListener(Listener l) {
		completionListeners.remove(l);
	}

	@Override
	public void onCreate(Bundle savedState) {
		setRetainInstance(true);
		super.onCreate(savedState);
		if (savedState != null) {
			mathleteId = (String) savedState.getCharSequence(MATHLETE_ID_KEY);
		}
		Log.d("rushhour", "initting loader");
		Loader l = getLoaderManager().initLoader(LOADER_ID, null, this);
		if (savedState == null) {
			l.forceLoad();
		}
	}

	@Override
	public void onActivityCreated(Bundle savedState) {
		super.onActivityCreated(savedState);
	}

	@Override
	public Loader<ResultWrapper<Mathlete>> onCreateLoader(int id, Bundle args) {
		Log.d("rushhour", "created a loader");
		return new MathleteLoader(getActivity(), mathleteId);
	}

	@Override
	public void onLoadFinished(Loader<ResultWrapper<Mathlete>> loader, ResultWrapper<Mathlete> wrapper) {
		Log.d("rushhour", "fragment notifying listener");
		for (Listener l : completionListeners) {
			l.onLoadFinished(wrapper);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle state) {
		state.putCharSequence(MATHLETE_ID_KEY, mathleteId);
	}
}
