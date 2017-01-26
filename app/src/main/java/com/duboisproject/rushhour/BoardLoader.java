/*
 *     Rush Hour Android app
 * Copyright (C) 2015 Randy Wanga, Jos Craaijo, Camil Staps
 *
 * Modified by Jakob Cornell, 2017-01-25 to -01-26
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
import android.graphics.Color;

/**
 * Created by Jos on 23-4-2015.
 */
public final class BoardLoader {
	/*
	Level format:
	1 line: number of cars

	2 lines for each car:
		x1 y1 x2 y2
		r g b
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

		// Allow semicolons in place of line breaks (for SdbNavigator compat)
		scanner.useDelimiter("\\p{javaWhitespace}+|;");

		int numCars = scanner.nextInt();

		for (int carN = 0; carN < numCars; carN += 1) {
			int x1 = scanner.nextInt();
			int y1 = scanner.nextInt();
			int x2 = scanner.nextInt();
			int y2 = scanner.nextInt();

			int r = scanner.nextInt();
			int g = scanner.nextInt();
			int b = scanner.nextInt();

			Car c = new Car(new Coordinate(x1, y1), new Coordinate(x2, y2), Color.rgb(r, g, b));
			board.add(c);
		}

		return board;
	}
}
