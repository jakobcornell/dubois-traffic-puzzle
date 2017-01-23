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

import android.app.Activity;
import android.app.PendingIntent;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.widget.Toast;
import android.os.Bundle;
import android.os.Parcelable;
import android.nfc.NfcAdapter;

import com.duboisproject.rushhour.Application;
import com.duboisproject.rushhour.id.DuboisIdentity;
import com.duboisproject.rushhour.id.NfcId;
import com.duboisproject.rushhour.fragments.LoaderFragment;
import com.duboisproject.rushhour.database.SdbInterface;
import com.duboisproject.rushhour.R;

public abstract class IdActivity extends Activity {
	protected PendingIntent nfcPendingIntent;
	protected IntentFilter[] nfcFilters;
	protected NfcAdapter nfcAdapter;
	protected SdbInterface sdbInterface;

	@Override
	public void onCreate(Bundle savedState) {
		super.onCreate(savedState);
		setContentView(R.layout.id_activity);

		final Intent innerIntent = new Intent(this, getClass());
		innerIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		nfcPendingIntent = PendingIntent.getActivity(this, 0, innerIntent, 0);

		final IntentFilter f = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
		try {
			f.addDataType(NfcId.MIME_TYPE);
		} catch (IntentFilter.MalformedMimeTypeException e) {
			throw new RuntimeException(e);
		}
		nfcFilters = new IntentFilter[] { f };

		nfcAdapter = NfcAdapter.getDefaultAdapter(getApplicationContext());

		if (sdbInterface == null) {
			sdbInterface = ((Application) getApplicationContext()).getSdbInterface();
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		final String action = intent.getAction();
		if (intent != null && action.equals(NfcAdapter.ACTION_NDEF_DISCOVERED)) {
			if (intent.getType().equals(NfcId.MIME_TYPE)) {
				final Parcelable[] rawMessages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
				String id;
				try {
					id = NfcId.getId(rawMessages);
				} catch (IllegalArgumentException | java.io.UnsupportedEncodingException e) {
					String errorPrefix = getString(R.string.error_prefix);
					String message = errorPrefix + e.getMessage();
					Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
					return;
				}
				onNewId(id);
			}
		}
	}

	protected abstract void onNewId(String id);

	@Override
	public void onPause() {
		super.onPause();
		nfcAdapter.disableForegroundDispatch(this);
	}

	@Override
	public void onResume() {
		super.onResume();
		nfcAdapter.enableForegroundDispatch(this, nfcPendingIntent, nfcFilters, new String[0][]);
	}
}
