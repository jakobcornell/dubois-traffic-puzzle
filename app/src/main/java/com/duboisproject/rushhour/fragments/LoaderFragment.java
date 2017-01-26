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

import android.app.Fragment;
import android.app.Activity;
import android.app.LoaderManager;
import android.os.Bundle;
import android.content.Loader;

import com.duboisproject.rushhour.activities.HandlerActivity;

/**
 * Headless fragment for fetching data asynchronously with a Loader.
 * See <http://www.androiddesignpatterns.com/2013/04/retaining-objects-across-config-changes.html>
 */
public abstract class LoaderFragment<Result> extends Fragment implements LoaderManager.LoaderCallbacks<Result> {
	protected HandlerActivity host;

	@Override
	public void onCreate(Bundle savedState) {
		// keep this fragment alive across configuration changes
		setRetainInstance(true);

		super.onCreate(savedState);
	}

	@Override
	public void onAttach(Activity host) {
		super.onAttach(host);
		this.host = (HandlerActivity) host;
	}

	@Override
	public void onDetach() {
		super.onDetach();
		host = null;
	}

	@Override
	public void onLoaderReset(Loader<Result> loader) {
		// TODO implement
	}
}
