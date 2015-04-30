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

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Highscore exists of name and score (amount of moves; so lower is better)
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
            return 1;
        }
        else if (other_score.score == score)
        {
            return 0;
        }
        else
        {
            return -1;
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
