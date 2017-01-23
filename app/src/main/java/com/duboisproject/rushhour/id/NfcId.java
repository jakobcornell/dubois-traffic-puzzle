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

import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.io.UnsupportedEncodingException;

import android.os.Parcelable;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;

public final class NfcId {
	public static final String MIME_TYPE = "text/plain";
	protected static final String DELIMITER = ";";

	/**
	 * Map from first payload token to token index of ID field
	 */
	protected static final Map<String, Integer> idPositions;
	static {
		Map<String, Integer> preIdPositions = new HashMap<String, Integer>();
		preIdPositions.put("Mathlete", 2);
		preIdPositions.put("Coach", 2);
		idPositions = Collections.unmodifiableMap(preIdPositions);
	}

	private NfcId() {}

	public static String getId(Parcelable[] rawNdefMessages) throws IllegalArgumentException, UnsupportedEncodingException {
		if (rawNdefMessages == null || rawNdefMessages.length == 0) {
			throw new IllegalArgumentException("No NDEF messages found");
		}
		for (Parcelable p : rawNdefMessages) {
			NdefMessage message = (NdefMessage) p;
			for (NdefRecord record : message.getRecords()) {
				if (record.getTnf() != NdefRecord.TNF_WELL_KNOWN) {
					continue;
				}
				byte[] payload = record.getPayload();
				if (payload.length == 0) {
					continue;
				}

				// see http://stackoverflow.com/a/7918973/2729736
				byte control = payload[0];
				int offset = 1 + control & 0b11111; // index of actual payload
				String encoding = ((control >> 7 & 1) == 0) ? "UTF-8" : "UTF-16";

				String content = new String(payload, offset, payload.length - offset, encoding);
				String[] tokens = content.split(DELIMITER);
				Integer idPosition = idPositions.get(tokens[0]);
				if (idPosition == null) {
					continue;
				}
				return tokens[idPosition];
			}
		}
		throw new IllegalArgumentException("No valid NDEF messages found");
	}
}
