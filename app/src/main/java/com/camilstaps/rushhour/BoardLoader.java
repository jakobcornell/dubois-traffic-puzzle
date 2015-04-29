/*
 *     Rush Hour Android app
 * Copyright (C) 2015 Randy Wanga, Jos Craaijo, Camil Staps
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
package com.camilstaps.rushhour;

import android.graphics.Color;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

/**
 * Created by Jos on 23-4-2015.
 */
public class BoardLoader {

    public BoardLoader()
    {}

    /**
     * Load a board from a file
     * @param file
     * @return
     */
    public Board loadBoard(InputStream file)
    {
        /*
        Level formaat:
        1 regel: Aantal auto's

        voor iedere auto:
        x1 y1 x2 y2 op een regel.
        r g b op een regel.
         */
        Scanner scan = new Scanner(file);

        Board board = new Board();

        int numCars = scan.nextInt();
        scan.nextLine();

        for(int carN = 0; carN < numCars; carN++)
        {
            int x1 = scan.nextInt();
            int y1 = scan.nextInt();
            int x2 = scan.nextInt();
            int y2 = scan.nextInt();
            scan.nextLine();

            int r = scan.nextInt();
            int g = scan.nextInt();
            int b = scan.nextInt();

            Car c = new Car(new Coordinate(x1, y1), new Coordinate(x2, y2), Color.rgb(r, g, b));
            board.add(c);

            if(scan.hasNext()) scan.nextLine();
        }

        return board;
    }
}
