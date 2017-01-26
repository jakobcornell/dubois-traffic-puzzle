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
import com.duboisproject.rushhour.GameStatistics;
import com.duboisproject.rushhour.id.Mathlete;
import com.duboisproject.rushhour.activities.MathleteIdActivity;

public final class PutStatisticsFragment extends LoaderFragment<ResultWrapper<Void>> {
	/**
	 * ID of this loader, used by the LoaderManager.
	 * This identifies the type of loader rather than any particular loader.
	 */
	protected static final int LOADER_ID = PutStatisticsLoader.class.hashCode();

	/**
	 * The "what" field of messages this fragment sends to its host on completion.
	 */
	public static final int MESSAGE_WHAT = PutStatisticsLoader.class.hashCode();

	protected Mathlete mathlete;
	protected GameStatistics stats;

	public PutStatisticsFragment() {}

	protected static final class PutStatisticsLoader extends AsyncTaskLoader<ResultWrapper<Void>> {
		protected final Context context;
		protected final Mathlete mathlete;
		protected final GameStatistics stats;

		public PutStatisticsLoader(Context context, Mathlete mathlete, GameStatistics stats) {
			super(context);
			this.context = context;
			this.mathlete = mathlete;
			this.stats = stats;
		}

		public ResultWrapper<Void> loadInBackground() {
			ResultWrapper<Void> wrapper = new ResultWrapper<Void>();
			Application app = (Application) context.getApplicationContext();
			try {
				app.getSdbInterface().putStats(mathlete, stats);
			} catch (Exception exception) {
				wrapper.setException(exception);
			}
			return wrapper;
		}
	}

	public PutStatisticsFragment(Mathlete mathlete, GameStatistics stats) {
		this.mathlete = mathlete;
		this.stats = stats;
	}

	@Override
	public void onCreate(Bundle savedState) {
		setRetainInstance(true);
		super.onCreate(savedState);
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
	public Loader<ResultWrapper<Void>> onCreateLoader(int id, Bundle args) {
		return new PutStatisticsLoader(getActivity(), mathlete, stats);
	}

	@Override
	public void onLoadFinished(Loader<ResultWrapper<Void>> loader, ResultWrapper<Void> wrapper) {
		Message message = host.getHandler().obtainMessage(MESSAGE_WHAT);
		message.obj = wrapper;
		host.getHandler().sendMessage(message);
	}
}
