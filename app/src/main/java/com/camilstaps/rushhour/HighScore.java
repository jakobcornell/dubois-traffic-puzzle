package com.camilstaps.rushhour;

import org.json.JSONException;
import org.json.JSONObject;

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

    /**
     * HighScore from json
     * @see #toString()
     * @param jsonString
     */
    public HighScore(String jsonString) {
        int temp_score = -1;
        String temp_name = null;
        try {
            JSONObject json = new JSONObject(jsonString);
            temp_score = json.getInt("score");
            temp_name = json.getString("name");
        } catch (JSONException e) {
        }
        score = temp_score;
        name = temp_name;
    }

    public int getScore() {
        return score;
    }

    public String getName() {
        return name;
    }

    @Override
    public int compareTo(HighScore other_score) {
        if (other_score.score < score)
        {
            return -1;
        }
        else if (other_score.score == score)
        {
            return 0;
        }
        else
        {
            return 1;
        }
    }

    /**
     * JSON representation
     * @see #HighScore(String)
     * @return
     */
    public String toString() {
        JSONObject json = new JSONObject();
        try {
            json.put("score", score);
            json.put("name", name);
        } catch (JSONException ex) {
        }
        return json.toString();
    }

}
