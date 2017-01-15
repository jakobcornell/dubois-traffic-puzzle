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

package com.duboisproject.rushhour.id;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.widget.TextView;
import android.os.Bundle;
import android.os.Parcelable;
import android.nfc.NfcAdapter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import com.duboisproject.rushhour.R;

// temp
import android.widget.Toast;

public final class MathleteIDActivity extends Activity {
	protected PendingIntent nfcPendingIntent;
	protected IntentFilter[] nfcFilters;
	protected NfcAdapter nfcAdapter;

	@Override
	public void onCreate(Bundle savedState) {
		super.onCreate(savedState);
		setContentView(R.layout.id_activity);
		TextView textView = (TextView) findViewById(R.id.fragment_text);
		textView.setText("Scan mathlete tag");

		nfcPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

		IntentFilter f = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
		try {
			f.addDataType("text/plain");
		} catch (IntentFilter.MalformedMimeTypeException e) {
			throw new RuntimeException(e);
		}
		nfcFilters = new IntentFilter[] { f };

		nfcAdapter = NfcAdapter.getDefaultAdapter(getApplicationContext());
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		String action = intent.getAction();
		if (intent != null && action.equals(NfcAdapter.ACTION_NDEF_DISCOVERED)) {
			if (intent.getType().equals("text/plain")) {
				Parcelable[] rawMessages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
				if (rawMessages == null || rawMessages.length == 0) {
					Toast.makeText(getApplicationContext(), "No messages!", Toast.LENGTH_SHORT).show();
				} else {
					NdefRecord[] records = ((NdefMessage) rawMessages[0]).getRecords();
					if (records == null || records.length == 0) {
						Toast.makeText(getApplicationContext(), "No records in first message!", Toast.LENGTH_SHORT).show();
					} else {
						NdefRecord r = records[0];
						if (r.getTnf() != NdefRecord.TNF_WELL_KNOWN) {
							Toast.makeText(getApplicationContext(), "Unrecognized TNF!", Toast.LENGTH_SHORT).show();
						}
						Toast.makeText(getApplicationContext(), new String(records[0].getPayload()), Toast.LENGTH_SHORT).show();
					}
				}
			}
		}
	}

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
