package com.camilstaps.rushhour;

/**
 * Created by camilstaps on 23-4-15.
 * Edited by Halzyn on 23-4-15.
 */
public class HighScore implements Comparable<HighScore> {

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

    @Override
    public int compareTo(HighScore other_score) {
        if (other_score.getScore() < score)
        {
            return -1;
        }
        else if (other_score.getScore() == score)
        {
            return 0;
        }
        else
        {
            return 1;
        }
    }

}
