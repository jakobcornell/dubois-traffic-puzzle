package com.camilstaps.rushhour;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TableLayout;

import java.util.Random;

/**
 * Created by camilstaps on 16-4-15.
 */
public class Car {

    private Coordinate startCoordinate, endCoordinate;
    private final int colour;

    private static final int SIZE = 50;
    private static final int MARGIN = 5;

    private MoveListener moveListener;

    private float widthPerCell;
    private int calculatedWidth, calculatedHeight;

    ImageView iv;

    public Car(Coordinate start, Coordinate end) {
        startCoordinate = start;
        endCoordinate = end;
        this.colour = Color.BLACK;
    }

    public Car(Coordinate start, Coordinate end, int colour) {
        startCoordinate = start;
        endCoordinate = end;
        this.colour = colour;
    }

    public Car(Car car) {
        startCoordinate = car.startCoordinate;
        endCoordinate = car.endCoordinate;
        colour = car.colour;
    }

    public void setMoveListener(MoveListener listener) {
        moveListener = listener;
    }

    public void setLayoutParams() {
        ViewGroup.MarginLayoutParams marginParams = new RelativeLayout.LayoutParams(calculatedWidth, calculatedHeight);
        marginParams.setMargins((int) (startCoordinate.getX() * (widthPerCell + MARGIN) + MARGIN), (int) (startCoordinate.getY() * (widthPerCell + MARGIN) + MARGIN), MARGIN, MARGIN);
        iv.setLayoutParams(marginParams);
    }

    public ImageView getImageView(Context context, float widthPerCell) {

        this.widthPerCell = widthPerCell - MARGIN;
        calculatedWidth = (int) ((endCoordinate.getX() - startCoordinate.getX() + 1) * (this.widthPerCell + MARGIN) - MARGIN);
        calculatedHeight = (int) ((endCoordinate.getY() - startCoordinate.getY() + 1) * (this.widthPerCell + MARGIN) - MARGIN);

        iv = new ImageView(context);

        int[] images = null;
        if(canMoveHorizontally()) {
            if(getCarLength() == 2) {
                images = new int[] {
                        R.drawable.car_1_white,
                        R.drawable.car_2_white,
                        R.drawable.car_3_white,
                        R.drawable.car_4_white};
            }else{
                images = new int[] {
                        R.drawable.truck_1_white,
                        R.drawable.truck_2_white,
                        R.drawable.truck_3_white,
                        R.drawable.truck_4_white};
            }
        }else{
            if(getCarLength() == 2) {
                images = new int[] {
                        R.drawable.car_1_white_vertical,
                        R.drawable.car_2_white_vertical,
                        R.drawable.car_3_white_vertical,
                        R.drawable.car_4_white_vertical};
            }else{
                images = new int[] {
                        R.drawable.truck_1_white_vertical,
                        R.drawable.truck_2_white_vertical,
                        R.drawable.truck_3_white_vertical,
                        R.drawable.truck_4_white_vertical};
            }
        }

        iv.setImageResource(choose(images));
        iv.setColorFilter(colour);
        iv.setMinimumWidth(calculatedWidth);
        iv.setMinimumHeight(calculatedHeight);

        setLayoutParams();

        final GestureDetector gdt = new GestureDetector(new GestureListener());
        iv.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gdt.onTouchEvent(event);
                return true;
            }
        });

        return iv;
    }

    private static int choose(int[] a)
    {
        Random r = new Random();
        return a[r.nextInt(a.length)];
    }

    private float getCarDirection()
    {
        final double TWOPI = Math.PI * 2;
        double radians = ((Math.atan2(startCoordinate.getY() - endCoordinate.getY(), endCoordinate.getX() - startCoordinate.getX()) + TWOPI) % TWOPI);
        double degrees = radians / Math.PI * 180.0;

        return (float)degrees;
    }
    private int getCarLength()
    {
        if(startCoordinate.getX() == endCoordinate.getX())
        {
            return endCoordinate.getY() - startCoordinate.getY() + 1;
        }else{
            return endCoordinate.getX() - startCoordinate.getX() + 1;
        }
    }

    public boolean canMoveHorizontally() {
        return startCoordinate.getY() == endCoordinate.getY();
    }

    public boolean canMoveVertically() {
        return startCoordinate.getX() == endCoordinate.getX();
    }

    public void move(int offset) {
        if (canMoveHorizontally()) {
            moveHorizontally(offset);
        } else {
            moveVertically(offset);
        }
    }

    public void moveHorizontally(int offset) {
        startCoordinate.move(offset, 0);
        endCoordinate.move(offset, 0);

        setLayoutParams();
    }

    public void moveVertically(int offset) {
        startCoordinate.move(0, offset);
        endCoordinate.move(0, offset);

        setLayoutParams();
    }

    public Coordinate wouldMoveTo(int offset) {
        Coordinate movedCoordinate;
        if (offset < 0) {
            movedCoordinate = new Coordinate(startCoordinate);
        } else {
            movedCoordinate = new Coordinate(endCoordinate);
        }
        if (canMoveHorizontally()) {
            movedCoordinate.move(offset, 0);
        } else {
            movedCoordinate.move(0, offset);
        }
        return movedCoordinate;
    }

    public boolean occupies(Coordinate c) {
        return c.getX() >= startCoordinate.getX() && c.getX() <= endCoordinate.getX() && c.getY() >= startCoordinate.getY() && c.getY() <= endCoordinate.getY();
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        private static final int SWIPE_MIN_DISTANCE = SIZE;
        private static final int SWIPE_THRESHOLD_VELOCITY = SIZE;

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY && canMoveHorizontally()) {
                if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE) {
                    moveListener.onMove(Car.this, -1);
                } else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE) {
                    moveListener.onMove(Car.this, 1);
                }
            }

            if (Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY && canMoveVertically()) {
                if (e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE) {
                    moveListener.onMove(Car.this, -1);
                } else if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE) {
                    moveListener.onMove(Car.this, 1);
                }
            }

            return true;
        }
    }

}
