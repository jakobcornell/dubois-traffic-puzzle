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
import com.duboisproject.rushhour.Board;
import com.duboisproject.rushhour.BoardLoader;
import com.duboisproject.rushhour.activities.CoachIdActivity;

public final class BoardLoaderFragment extends LoaderFragment<ResultWrapper<Board>> {
	/**
	 * ID of this loader, used by the LoaderManager.
	 * This identifies the type of loader rather than any particular loader.
	 */
	protected static final int LOADER_ID = BoardLoader.class.hashCode();

	/**
	 * The "what" field of messages this fragment sends to its host on completion.
	 */
	public static final int MESSAGE_WHAT = BoardLoader.class.hashCode();

	protected Board.Descriptor descriptor;

	public BoardLoaderFragment() {}

	protected static final class BoardLoader extends AsyncTaskLoader<ResultWrapper<Board>> {
		protected final Context context;
		protected final Board.Descriptor descriptor;

		public BoardLoader(Context context, Board.Descriptor descriptor) {
			super(context);
			this.context = context;
			this.descriptor = descriptor;
		}

		public ResultWrapper<Board> loadInBackground() {
			ResultWrapper<Board> wrapper = new ResultWrapper<Board>();
			Application app = (Application) context.getApplicationContext();
			try {
				wrapper.setResult(descriptor.resolve());
			} catch (Exception exception) {
				wrapper.setException(exception);
			}
			return wrapper;
		}
	}

	public BoardLoaderFragment(Board.Descriptor descriptor) {
		this.descriptor = descriptor;
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
	public Loader<ResultWrapper<Board>> onCreateLoader(int id, Bundle args) {
		return new BoardLoader(getActivity(), descriptor);
	}

	@Override
	public void onLoadFinished(Loader<ResultWrapper<Board>> loader, ResultWrapper<Board> wrapper) {
		Message message = host.getHandler().obtainMessage(MESSAGE_WHAT);
		message.obj = wrapper;
		host.getHandler().sendMessage(message);
	}
}
