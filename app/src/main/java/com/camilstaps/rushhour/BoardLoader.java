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

    public Board loadBoard(InputStream file)
    {
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
