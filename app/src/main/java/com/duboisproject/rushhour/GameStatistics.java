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

import org.joda.time.DateTime;
import org.joda.time.Duration;

/**
 * Represents the results of a level play.
 *
 * Note: Should use java.time when Java 8 is supported.
 */
public final class GameStatistics {
	public int levelId;
	public int totalMoves;
	public int resetMoves;
	public DateTime startTime;
	public Duration totalCompletionTime;
	public Duration resetCompletionTime;
}
