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
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;
import java.io.StringReader;
import com.amazonaws.AmazonClientException;
import com.amazonaws.services.simpledb.AmazonSimpleDBClient;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.simpledb.model.GetAttributesRequest;
import com.amazonaws.services.simpledb.model.GetAttributesResult;
import com.amazonaws.services.simpledb.model.PutAttributesRequest;
import com.amazonaws.services.simpledb.model.Attribute;
import com.amazonaws.services.simpledb.model.ReplaceableAttribute;

import com.duboisproject.rushhour.Board;
import com.duboisproject.rushhour.BoardLoader;
import com.duboisproject.rushhour.GameStatistics;
import com.duboisproject.rushhour.id.Mathlete;
import com.duboisproject.rushhour.id.Coach;

public final class SdbInterface {
	// domain names
	protected static final String MATHLETE_DOMAIN = "dubois_mathlete_identities";
	protected static final String COACH_DOMAIN = "dubois_coach_identities";
	protected static final String LEVELS_DOMAIN = "dubois_rushhour_levels";
	protected static final String PLAYS_DOMAIN = "dubois_rushhour_games_played";

	// attribute names
	protected static final String MATHLETE_NAME = "name";
	protected static final String MATHLETE_LAST_NAME = "last_name";
	protected static final String COACH_NAME = "Name";
	protected static final String LEVEL_MAP = "map";
	protected static final String LEVEL_ID = "level_id";
	protected static final String MOVES = "moves";
	protected static final String START_TIME = "start_time";
	protected static final String TOTAL_TIME = "total_time";

	protected static final String REQUEST_FAILED_MESSAGE = "Unable to complete request";

	protected static enum GetRequestDetails {
		MATHLETE_ID(
			MATHLETE_DOMAIN,
			new String[] { MATHLETE_NAME, MATHLETE_LAST_NAME }
		),
		COACH_ID(
			COACH_DOMAIN,
			new String[] { COACH_NAME }
		),
		MAP_FETCH(
			LEVELS_DOMAIN,
			new String[] { LEVEL_MAP }
		);

		public final String domainName;
		public final Collection<String> attributeNames;

		private GetRequestDetails(String domainName, String[] attributeNames) {
			this.domainName = domainName;
			this.attributeNames = Arrays.asList(attributeNames);
		}

		public GetAttributesRequest toAttributesRequest() {
			GetAttributesRequest request = new GetAttributesRequest();
			request.setDomainName(domainName);
			request.setAttributeNames(attributeNames);
			request.setConsistentRead(true);
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
		GetAttributesRequest request = GetRequestDetails.MATHLETE_ID.toAttributesRequest();
		request.setItemName(id);
		GetAttributesResult result;
		try {
			result = client.getAttributes(request);
		} catch (AmazonClientException e) {
			throw new RequestException(REQUEST_FAILED_MESSAGE);
		}
		List<Attribute> attributesList = result.getAttributes();
		if (attributesList.size() == 0) {
			throw new IllegalArgumentException("No such mathlete in database");
		}
		Map<String, String> attributes = mapify(attributesList);
		return new Mathlete(id, attributes.get(MATHLETE_NAME), attributes.get(MATHLETE_LAST_NAME));
	}

	public Coach fetchCoach(String id) throws IllegalArgumentException, RequestException {
		GetAttributesRequest request = GetRequestDetails.COACH_ID.toAttributesRequest();
		request.setItemName(id);
		GetAttributesResult result;
		try {
			result = client.getAttributes(request);
		} catch (AmazonClientException e) {
			throw new RequestException(REQUEST_FAILED_MESSAGE);
		}
		List<Attribute> attributesList = result.getAttributes();
		if (attributesList.size() == 0) {
			throw new IllegalArgumentException("No such coach in database");
		}
		Map<String, String> attributes = mapify(attributesList);
		return new Coach(id, attributes.get(COACH_NAME));
	}

	public Board fetchBoard(int id) throws IllegalArgumentException, RequestException {
		GetAttributesRequest request = GetRequestDetails.MAP_FETCH.toAttributesRequest();
		request.setItemName(Integer.toString(id));
		GetAttributesResult result;
		try {
			result = client.getAttributes(request);
		} catch (AmazonClientException e) {
			throw new RequestException(REQUEST_FAILED_MESSAGE);
		}
		List<Attribute> attributesList = result.getAttributes();
		if (attributesList.size() == 0) {
			throw new IllegalArgumentException("No such level in database");
		}
		Map<String, String> attributes = mapify(attributesList);
		String map = attributes.get(LEVEL_MAP);
		return BoardLoader.loadBoard(new StringReader(map));
	}

	public void putStats(Mathlete player, GameStatistics stats) throws RequestException {
		Map<String, String> attributes = new HashMap<String, String>();
		attributes.put(LEVEL_ID, Integer.toString(stats.levelId));
		attributes.put(MOVES, Integer.toString(stats.moves));
		attributes.put(START_TIME, stats.startTime.toString());
		attributes.put(TOTAL_TIME, Long.toString(stats.totalCompletionTime.getMillis()));
		PutAttributesRequest request = new PutAttributesRequest();
		request.setDomainName(PLAYS_DOMAIN);
		request.setItemName(player.id);
		request.setAttributes(listify(attributes));
		try {
			client.putAttributes(request);
		} catch (AmazonClientException e) {
			throw new RequestException(REQUEST_FAILED_MESSAGE);
		}
	}

	protected static Map<String, String> mapify(List<Attribute> attributes) {
		Map<String, String> attributesMap = new HashMap<String, String>();
		for (Attribute a : attributes) {
			attributesMap.put(a.getName(), a.getValue());
		}
		return attributesMap;
	}

	protected static List<ReplaceableAttribute> listify(Map<String, String> attributes) {
		List<ReplaceableAttribute> attributesList = new ArrayList<ReplaceableAttribute>();
		for (Map.Entry<String, String> entry : attributes.entrySet()) {
			attributesList.add(new ReplaceableAttribute(entry.getKey(), entry.getValue(), true));
		}
		return attributesList;
	}
}
