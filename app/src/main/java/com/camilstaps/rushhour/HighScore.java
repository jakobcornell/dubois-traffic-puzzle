package com.camilstaps.rushhour;

/**
 * Created by camilstaps on 23-4-15.
 */
public class HighScore {

    private final int score;
    private final String name;

    public HighScore(int score, String name) {
        this.score = score;
        this.name = name;
    }

    public int getScore() {
        return score;
    }

    public String getName() {
        return name;
    }

}
