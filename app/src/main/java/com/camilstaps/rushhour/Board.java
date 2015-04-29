package com.camilstaps.rushhour;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by camilstaps on 16-4-15.
 */
public class Board {

    Set<Car> cars = new HashSet<>();

    public static final int DIMENSION = 6;

    private DriveListener driveListener;
    private SolveListener solveListener;

    private int score;

    /**
     * Move a car if possible, and call the appropriate listeners
     */
    private MoveListener moveListener = new MoveListener() {
        @Override
        public void onMove(Car car, int offset) {
            Coordinate newC = car.wouldMoveTo(offset);
            if (newC.getY() > DIMENSION - 1 || newC.getX() > DIMENSION - 1 || newC.getX() < 0 || newC.getY() < 0) {
                driveListener.onBlocked();
                return;
            }
            for (Car iter : cars) {
                if (iter.occupies(newC)) {
                    driveListener.onBlocked();
                    return;
                }
            }
            car.move(offset);
            score++;
            if (isSolved() && solveListener != null) {
                solveListener.onSolve(score);
            }
            driveListener.onDrive();
        }
    };

    public Board(Set<Car> cars) {
        for (Car car : cars) {
            add(car);
        }
    }

    public Board() {
        this(new HashSet<Car>());
    }

    public void add(Car car) {
        car.setMoveListener(moveListener);
        cars.add(car);
    }

    /**
     * Add all cars to an existing layout
     * RelativeLayout is assumed, although this may work with other Layouts
     * @param context
     * @param layout
     */
    public void addToLayout(Context context, ViewGroup layout) {
        for (Car car : cars) {
            layout.addView(car.getImageView(
                    context,
                    (layout.getWidth() - layout.getPaddingLeft() - layout.getPaddingRight()) / DIMENSION
            ));
        }
    }

    /**
     * True iff the red car can move out without problems
     * @return
     */
    public boolean isSolved() {
        for (int x = DIMENSION - 1; x >= 0; x--) {
            for (Car car : cars) {
                if (car.occupies(new Coordinate(x, 2))) {
                    if (car.canMoveHorizontally()) {
                        return true;
                    } else {
                        return false;
                    }
                }
            }
        }
        return false;
    }

    public void setDriveListener(DriveListener dl) {
        driveListener = dl;
    }

    public interface SolveListener {
        public void onSolve(int score);
    }

    public void setSolveListener(SolveListener sl) {
        solveListener = sl;
    }

}
