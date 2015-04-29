package com.camilstaps.rushhour;

/**
 * Created by camilstaps on 22-4-15.
 */
public abstract class DriveListener {
    /**
     * Called when a car moves
     */
    public abstract void onDrive();

    /**
     * Called when a car attempted to move, but couldn't
     */
    public abstract void onBlocked();
}
