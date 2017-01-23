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

package com.duboisproject.rushhour.database;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.FutureTask;
import java.util.concurrent.Callable;
import android.util.Log; // TODO remove
import com.amazonaws.services.simpledb.AmazonSimpleDBClient;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.simpledb.model.GetAttributesRequest;
import com.amazonaws.services.simpledb.model.GetAttributesResult;
import com.amazonaws.services.simpledb.model.Attribute;

import com.duboisproject.rushhour.id.Mathlete;
import com.duboisproject.rushhour.id.Coach;
import com.duboisproject.rushhour.R;

public final class SdbInterface {
	protected static final String MATHLETE_NAME = "name";
	protected static final String MATHLETE_LAST_NAME = "last_name";
	protected static final String COACH_NAME = "Name";

	protected static enum RequestDetails {
		MATHLETE_IDENTIFY(
			"dubois_mathlete_identities",
			new String[] { MATHLETE_NAME, MATHLETE_LAST_NAME }
		),
		COACH_IDENTITY(
			"dubois_coach_identities",
			new String[] { COACH_NAME }
		);

		public final String domainName;
		public final Collection<String> attributeNames;

		private RequestDetails(String domainName, String[] attributeNames) {
			this.domainName = domainName;
			this.attributeNames = Arrays.asList(attributeNames);
		}

		public GetAttributesRequest toAttributesRequest() {
			GetAttributesRequest request = new GetAttributesRequest();
			request.setDomainName(domainName);
			request.setAttributeNames(attributeNames);
			return request;
		}
	}

	public static final class RequestException extends Exception {
		public RequestException() {}

		public RequestException(String message) {
			super(message);
		}
	}

	protected final AmazonSimpleDBClient client;

	public SdbInterface(AWSCredentials credentials) {
		client = new AmazonSimpleDBClient(credentials);
	}

	public Mathlete fetchMathlete(String id) throws IllegalArgumentException, RequestException {
		GetAttributesRequest request = RequestDetails.MATHLETE_IDENTIFY.toAttributesRequest();
		request.setItemName(id);
		request.setConsistentRead(true);
		GetAttributesResult result;
		try {
			result = client.getAttributes(request);
		} catch (com.amazonaws.AmazonClientException e) {
			throw new RequestException("Unable to complete request");
		}
		List<Attribute> attributesList = result.getAttributes();
		if (attributesList.size() == 0) {
			throw new IllegalArgumentException("Anonymous or missing mathlete");
		}
		Map<String, String> attributes = mapify(attributesList);
		return new Mathlete(id, attributes.get(MATHLETE_NAME), attributes.get(MATHLETE_LAST_NAME));
	}

	public Coach fetchCoach(String id) throws IllegalArgumentException {
		return null;
	}

	protected static Map<String, String> mapify(List<Attribute> attributes) {
		Map<String, String> attributesMap = new HashMap<String, String>();
		for (Attribute a : attributes) {
			attributesMap.put(a.getName(), a.getValue());
		}
		return attributesMap;
	}
}
