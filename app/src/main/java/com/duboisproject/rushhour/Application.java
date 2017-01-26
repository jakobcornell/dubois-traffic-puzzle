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

package com.duboisproject.rushhour;

import android.widget.Toast;
import android.util.Log;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;

import com.duboisproject.rushhour.Board;
import com.duboisproject.rushhour.id.Mathlete;
import com.duboisproject.rushhour.database.SdbInterface;
import com.duboisproject.rushhour.R;

public final class Application extends android.app.Application {
	protected SdbInterface sdbInterface;
	protected Toaster toaster;
	public Mathlete player;

	public final class Toaster {
		protected String errorPrefix = getResources().getString(R.string.error_prefix);

		public void toastMessage(String message) {
			Toast.makeText(Application.this, message, Toast.LENGTH_SHORT).show();
		}

		public void toastError(String message) {
			Toast.makeText(Application.this, errorPrefix + message, Toast.LENGTH_LONG).show();
		}
	}

	public SdbInterface getSdbInterface() {
		return sdbInterface;
	}

	public Toaster getToaster() {
		return toaster;
	}

	public void logError(Exception e) {
		String tag = getResources().getString(R.string.logging_tag);
		Log.e(tag, "", e);
	}

	@Override
	public void onCreate() {
		String accessKey = getString(R.string.access_key);
		String secretKey = getString(R.string.secret_key);
		AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
		sdbInterface = new SdbInterface(credentials);

		toaster = new Toaster();
	}

	/**
	 * Global state for transitions into game play activity.
	 *
	 * The best way to do this would be through Intent extras,
	 * but serializing an object that references this class seems impossible.
	 */
	public static Board.Descriptor pendingDescriptor;
}
