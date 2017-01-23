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

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;

import com.duboisproject.rushhour.database.SdbInterface;

public final class Application extends android.app.Application {
	protected SdbInterface sdbInterface;

	public SdbInterface getSdbInterface() {
		return sdbInterface;
	}

	@Override
	public void onCreate() {
		String accessKey = getString(R.string.access_key);
		String secretKey = getString(R.string.secret_key);
		AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
		sdbInterface = new SdbInterface(credentials);
	}
}
