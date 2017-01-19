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
import android.content.res.Resources;

import com.duboisproject.rushhour.Application;
import com.duboisproject.rushhour.id.Mathlete;
import com.duboisproject.rushhour.concurrent.Function;
import com.duboisproject.rushhour.concurrent.Consumer;
import com.duboisproject.rushhour.fragments.LoaderFragment;
import com.duboisproject.rushhour.database.SdbInterface;
import com.duboisproject.rushhour.R;

public final class MathleteIdActivity extends IdActivity implements LoaderFragment.Listener {
	protected int loaderFragmentId;

	@Override
	public void onCreate(Bundle savedState) {
		super.onCreate(savedState);
		TextView textView = (TextView) findViewById(R.id.fragment_text);
		textView.setText(R.string.mathlete_scan_message);
	}
	
	@Override
	protected void onNewId(String id) {
		final LoaderFragment<String, Mathlete> loader = new LoaderFragment<>(
			new Function<String, Mathlete>() {
				@Override
				public Mathlete apply(String id) {
					final Application application = (Application) getApplicationContext();
					final SdbInterface sdbInterface = application.getSdbInterface();
					return sdbInterface.fetchMathlete(id);
				}
			},
			new Consumer<Mathlete>() {
				@Override
				public void accept(Mathlete m) {
					final Resources res = getResources();
					final String messageFormat = res.getString(R.string.welcome_message);
					final String message = String.format(messageFormat, m.firstName);
					Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
				}
			}
		);
		Bundle idBundle = new Bundle();
		idBundle.putSerializable(LoaderFragment.PARAM_KEY, id);
		loader.setArguments(idBundle);
		loader.registerListener(this);
		final FragmentTransaction transaction = getFragmentManager().beginTransaction();
		transaction.add(R.id.loader_container, loader);
		transaction.addToBackStack(null);
		loaderFragmentId = transaction.commit();
	}

	@Override
	public void onLoadFinish() {
		getFragmentManager().popBackStack(loaderFragmentId, FragmentManager.POP_BACK_STACK_INCLUSIVE);
	}
}
