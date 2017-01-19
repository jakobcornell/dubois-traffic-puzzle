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

import java.util.Collection;
import java.util.HashSet;
import java.io.Serializable;
import android.app.Fragment;
import android.os.Bundle;
import android.os.AsyncTask;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;

import com.duboisproject.rushhour.concurrent.Function;
import com.duboisproject.rushhour.concurrent.Consumer;
import com.duboisproject.rushhour.R;

public final class LoaderFragment<Param, Result> extends Fragment {
	/**
	 * Argument bundle key for task parameter from client activity.
	 */
	public static final String PARAM_KEY = "LOADER_FRAGMENT_PARAM";

	/**
	 * Bundle keys for saving and restoring state
	 */
	protected static final String TASK_KEY = "LOADER_FRAGMENT_TASK";
	protected static final String LISTENERS_KEY = "LOADER_FRAGMENT_LISTENERS";
	
	protected final class Task extends AsyncTask<Param, Void, Result> implements Serializable {
		protected Function<Param, Result> task;
		protected Consumer<Result> callback;
		
		public Task(Function<Param, Result> task, Consumer<Result> callback) {
			super();
			this.task = task;
			this.callback = callback;
		}
		
		@Override
		protected Result doInBackground(Param... params) {
			// used with exactly one parameter
			return task.apply(params[0]);
		}

		@Override
		protected void onPostExecute(Result result) {
			callback.accept(result);
			for (Listener l : completionListeners) {
				l.onLoadFinish();
			}
		}
	}

	protected Task task;

	public LoaderFragment() {}

	public LoaderFragment(Function<Param, Result> task, Consumer<Result> callback) {
		super();
		this.task = new Task(task, callback);
	}

	public static interface Listener {
		public void onLoadFinish();
	}

	protected Collection<Listener> completionListeners = new HashSet<Listener>();
	public void registerListener(Listener l) {
		completionListeners.add(l);
	}
	public void unregisterListener(Listener l) {
		completionListeners.remove(l);
	}

	@Override
	public void onCreate(Bundle savedState) {
		super.onCreate(savedState);
		if (savedState == null) {
			Param param = (Param) getArguments().getSerializable(PARAM_KEY);
			task.execute(param);
		} else {
			task = (Task) savedState.getSerializable(TASK_KEY);
			completionListeners = (Collection<Listener>) savedState.getSerializable(LISTENERS_KEY);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState) {
		return inflater.inflate(R.layout.loader_fragment, container, false);
	}

	@Override
	public void onSaveInstanceState(Bundle state) {
		super.onSaveInstanceState(state);
		state.putSerializable(TASK_KEY, task);
		state.putSerializable(LISTENERS_KEY, (Serializable) completionListeners);
	}

	// Is this necessary?
	/*
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.loader_fragment, container, false);
	}
	*/
}
