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
import android.content.Context;
import android.content.Loader;
import android.content.AsyncTaskLoader;

import com.duboisproject.rushhour.id.Coach;

public final class CoachLoaderFragment extends LoaderFragment<ResultWrapper<Coach>> {
	protected final String coachId;

	public interface Listener {
		public void onLoadFinished(ResultWrapper<Coach> wrapper);
	}

	protected static final class CoachLoader extends AsyncTaskLoader<ResultWrapper<Coach>> {
		/**
		 * ID of this loader, used by the LoaderManager.
		 * This seems to identify the type of loader rather than any particular loader.
		 */
		public static final int id = CoachLoader.class.hashCode();

		public CoachLoader(Context context) {
			super(context);
		}

		public ResultWrapper<Coach> loadInBackground() {
			// use coachId of outer class
			// TODO
			ResultWrapper<Coach> wrapper = new ResultWrapper<Coach>();
			try {
				wrapper.setResult(new Coach("DEADBEEF", "Joe"));
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
	public Loader<ResultWrapper<Coach>> onCreateLoader(int id, Bundle args) {
		return new CoachLoader(getActivity());
	}

	@Override
	public void onLoadFinished(Loader<ResultWrapper<Coach>> loader, ResultWrapper<Coach> wrapper) {
		try {
			wrapper.getResult();
		} catch (Exception e) {}
	}
}
