/*
 *     Rush Hour Android app
 * Copyright (C) 2015 Randy Wanga, Jos Craaijo, Camil Staps
 *
 * Modified by Jakob Cornell, 2017-01-25 to -02-01
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

import java.io.Reader;
import java.util.Scanner;

/**
 * Created by Jos on 23-4-2015.
 */
public final class BoardLoader {
	/*
	These are set in dubois_rushhour_levels
	Level format:
	1 line: number of cars

	2 lines for each car:
		x1 y1 x2 y2
		r g b
	*/
/*
   By convention:
      levels 101 to 140 are the Rush Hour Junior Levels
         level 10x = level RHJx and the difficulty is set to 2 + 3x
         We might change the difficulty at some point, though it is equally
         likely that we will adjust the difficulty of the other challenges we
         add to match this initial definition of difficulty
      levels 141 to 180 are the Rush Hour "Adult" levels
      levels 1001 and up are the Unblock car levels
 */
	protected BoardLoader() {}

	/**
	 * Load a board from a Reader (e.g. FileReader or StringReader).
	 * @param reader  the Reader to read the level map from
	 * @return
	 */
	public static Board loadBoard(Reader reader) {
		Board board = new Board();
		Scanner scanner = new Scanner(reader);
		Car.ColorGenerator colorGenerator = new Car.ColorGenerator();

		// Allow semicolons in place of line breaks (for SdbNavigator compat)
		scanner.useDelimiter("\\p{javaWhitespace}+|;");

		int goalX = scanner.nextInt();
		Car goalCar = new Car(new Coordinate(goalX, 2), new Coordinate(goalX + 1, 2), Car.GOAL_CAR_COLOR);
		board.add(goalCar);
		board.setGoalCar(goalCar);

		while (scanner.hasNext()) {
			int x1 = scanner.nextInt();
			int y1 = scanner.nextInt();
			int x2 = scanner.nextInt();
			int y2 = scanner.nextInt();

			Car c = new Car(new Coordinate(x1, y1), new Coordinate(x2, y2), colorGenerator.next());
			board.add(c);
		}

		return board;
	}
}
