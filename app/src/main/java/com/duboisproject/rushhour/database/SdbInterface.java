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
import java.util.Set;
import java.util.HashSet;
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
import com.amazonaws.services.simpledb.model.SelectRequest;
import com.amazonaws.services.simpledb.model.SelectResult;
import com.amazonaws.services.simpledb.model.Item;
import com.amazonaws.services.simpledb.model.DomainMetadataRequest;
import com.amazonaws.services.simpledb.model.DomainMetadataResult;
import org.joda.time.DateTime;
import org.joda.time.Duration;

import com.duboisproject.rushhour.Board;
import com.duboisproject.rushhour.BoardLoader;
import com.duboisproject.rushhour.GameStatistics;
import com.duboisproject.rushhour.id.Mathlete;
import com.duboisproject.rushhour.id.Coach;

public final class SdbInterface {
	protected static final String PRIMARY_KEY = "itemName()";

	// domain names
	protected static final String MATHLETE_DOMAIN = "dubois_mathlete_identities";
	protected static final String COACH_DOMAIN = "dubois_coach_identities";
	protected static final String LEVELS_DOMAIN = "dubois_rushhour_levels";
	protected static final String PLAYS_DOMAIN = "dubois_rushhour_games_played";

	// attributes: mathletes table
	protected static final String MATHLETE_NAME = "name";
	protected static final String MATHLETE_LAST_NAME = "last_name";

	// attributes: coaches table
	protected static final String COACH_NAME = "Name";

	// attributes: levels table
	protected static final String LEVEL_MAP = "map";
	protected static final String LEVEL_DIFFICULTY = "difficulty";

	// attributes: plays table
	protected static final String PLAYS_MATHLETE = "mathlete";
	protected static final String PLAYS_LEVEL = "level_id";
	protected static final String PLAYS_MOVES = "moves";
	protected static final String PLAYS_START = "start_time";
	protected static final String PLAYS_TOTAL_TIME = "total_time";
	protected static final String PLAYS_RESET_TIME = "reset_time";

	protected static Integer cachedLevelCount;

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

	public Mathlete fetchMathlete(String id) throws RequestException {
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

	public Coach fetchCoach(String id) throws RequestException {
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

	/**
	 * Get the map for the specified level id, and construct a board.
	 */
	public Board fetchBoard(int id) throws RequestException {
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
		Board board = BoardLoader.loadBoard(new StringReader(map));
		board.id = id;
		return board;
	}

	/**
	 * Get the total number of levels: the number of records in the levels table.
	 */
	public int fetchLevelCount() throws RequestException {
		// We don't expect this to change during app runs, so we'll cache it.
		if (cachedLevelCount == null) {
			DomainMetadataRequest request = new DomainMetadataRequest(LEVELS_DOMAIN);
			try {
				cachedLevelCount = client.domainMetadata(request).getItemCount();
			} catch (AmazonClientException e) {
				throw new RequestException(REQUEST_FAILED_MESSAGE);
			}
		}
		return cachedLevelCount;
	}

	/**
	 * Add a play to the database.
	 */
	public void putStats(Mathlete player, GameStatistics stats) throws RequestException {
		Map<String, String> attributes = new HashMap<String, String>();
		attributes.put(PLAYS_MATHLETE, player.id);
		attributes.put(PLAYS_LEVEL, Integer.toString(stats.levelId));
		attributes.put(PLAYS_MOVES, Integer.toString(stats.moves));
		attributes.put(PLAYS_START, stats.startTime.toString());
		attributes.put(PLAYS_TOTAL_TIME, stats.totalCompletionTime.toString());
		attributes.put(PLAYS_RESET_TIME, stats.resetCompletionTime.toString());

		PutAttributesRequest request = new PutAttributesRequest();
		request.setDomainName(PLAYS_DOMAIN);
		request.setItemName(UUID.randomUUID().toString());
		request.setAttributes(listify(attributes));

		try {
			client.putAttributes(request);
		} catch (AmazonClientException e) {
			throw new RequestException(REQUEST_FAILED_MESSAGE);
		}
	}

	/**
	 * Fetch the stats for a mathlete's most recent play.
	 *
	 * Uses at most one query, and sorts on the database side.
	 *
	 * @return stats for the last play, or <code>null</code> if no plays exist
	 */
	public GameStatistics fetchLastPlay(Mathlete mathlete) throws RequestException {
		String format =
			"select * from `%s` where `%s` = \"%s\" and `%s` is not null " +
			"order by `%s` desc limit 1"
		;
		String query = String.format(
			format,
			sdbEscape(PLAYS_DOMAIN, '`'),
			sdbEscape(PLAYS_MATHLETE, '`'),
			sdbEscape(mathlete.id, '"'),
			sdbEscape(PLAYS_START, '`'),
			sdbEscape(PLAYS_START, '`')
		);

		SelectRequest request = new SelectRequest(query, true);
		SelectResult result;
		try {
			result = client.select(request);
		} catch (AmazonClientException e) {
			throw new RequestException(REQUEST_FAILED_MESSAGE);
		}

		List<Item> items = result.getItems();
		if (items.isEmpty()) {
			return null;
		} else {
			return parseStats(items.get(0));
		}
	}

	/**
	 * Get the stats for all levels a mathlete has played.
	 */
	public Set<GameStatistics> fetchAllPlays(Mathlete mathlete) throws RequestException {
		String format = "select * from `%s` where `%s` = \"%s\" limit 2500";
		String query = String.format(
			format,
			sdbEscape(PLAYS_DOMAIN, '`'),
			sdbEscape(PLAYS_MATHLETE, '`'),
			sdbEscape(mathlete.id, '"')
		);

		Set<GameStatistics> plays = new HashSet<GameStatistics>();

		SelectRequest request = new SelectRequest(query, true);
		String nextToken = null;
		do {
			request.setNextToken(nextToken);
			SelectResult result = client.select(request);
			for (Item item : result.getItems()) {
				plays.add(parseStats(item));
			}
			nextToken = result.getNextToken();
		} while (nextToken != null);

		return plays;
	}

	/**
	 * Get the stats for all plays by a mathlete at a given difficulty.
	 *
	 * May be useful for determining which level in a certain difficulty should be played next.
	 */
	public Map<Integer, GameStatistics[]> fetchStatsAtDifficulty(Mathlete player, int difficulty) throws RequestException {
		Map<Integer, GameStatistics[]> levelStats = new HashMap<Integer, GameStatistics[]>();

		// Select all levels of the specified difficulty
		String format = "select `%s` from `%s` where `%s` = \"%s\"";
		String query = String.format(
			format,
			sdbEscape(PRIMARY_KEY, '`'),
			sdbEscape(LEVELS_DOMAIN, '`'),
			sdbEscape(LEVEL_DIFFICULTY, '`'),
			sdbEscape(Integer.toString(difficulty), '"')
		);

		SelectRequest request = new SelectRequest(query, true);
		SelectResult result;
		try {
			result = client.select(request);
		} catch (AmazonClientException e) {
			throw new RequestException(REQUEST_FAILED_MESSAGE);
		}

		// Get all stats for each such level
		format = "select * from `%s` where `%s` = \"%s\" and `%s` = \"%s\"";
		for (Item item : result.getItems()) {
			int id = Integer.parseInt(item.getName());
			query = String.format(
				format,
				sdbEscape(PLAYS_DOMAIN, '`'),
				sdbEscape(PLAYS_LEVEL, '`'),
				sdbEscape(Integer.toString(id), '"'),
				sdbEscape(PLAYS_MATHLETE, '`'),
				sdbEscape(player.id, '"')
			);
			
			SelectRequest playsRequest = new SelectRequest(query, true);
			SelectResult playsResult;
			try {
				playsResult = client.select(playsRequest);
			} catch (AmazonClientException e) {
				throw new RequestException(REQUEST_FAILED_MESSAGE);
			}

			List<Item> items = playsResult.getItems();
			GameStatistics[] stats = new GameStatistics[items.size()];
			for (int i = 0; i < items.size(); i += 1) {
				stats[i] = parseStats(items.get(i));
			}
			levelStats.put(id, stats);
		}
		return levelStats;
	}

	/**
	 * Escapes a string (domain name, attrubute name/value) for use in SDB select statements.
	 *
	 * This process involves "expanding" certain characters depending on context.
	 * See <a href="http://docs.aws.amazon.com/AmazonSimpleDB/latest/DeveloperGuide/QuotingRulesSelect.html">quoting rules</a>.
	 *
	 * @param value  the string to be escaped
	 * @param special  the special character to be expanded
	 * @return  the escaped string
	 */
	protected static String sdbEscape(String value, char special) {
		String from = Character.toString(special);
		String to = from + from;
		return value.replace(from, to);
	}

	protected static GameStatistics parseStats(Item item) {
		Map<String, String> attributes = mapify(item.getAttributes());
		GameStatistics stats = new GameStatistics();
		stats.levelId = Integer.parseInt(attributes.get(PLAYS_LEVEL));
		stats.moves = Integer.parseInt(attributes.get(PLAYS_MOVES));
		stats.startTime = DateTime.parse(attributes.get(PLAYS_START));
		stats.totalCompletionTime = Duration.parse(attributes.get(PLAYS_TOTAL_TIME));
		stats.resetCompletionTime = Duration.parse(attributes.get(PLAYS_RESET_TIME));
		return stats;
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
