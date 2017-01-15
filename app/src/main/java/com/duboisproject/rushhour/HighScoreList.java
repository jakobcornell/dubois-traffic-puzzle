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
package com.duboisproject.rushhour;

import android.content.Context;
import android.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A list of highscores from SharedPreferences
 * @author Created by halzyn on 23-4-15.
 */
public class HighScoreList {

    private final String PREFERENCES_KEY = "highscores";
    
    private ArrayList<HighScore> list;

    /**
     * Get highscores from sharedpreferences
     * @param context
     */
    public HighScoreList(Context context) {
        Set<String> jsonList = PreferenceManager
                .getDefaultSharedPreferences(context)
                .getStringSet(PREFERENCES_KEY, new HashSet<String>());
        list = new ArrayList<>();
        for (String json : jsonList) {
            list.add(new HighScore(json));
        }
        Collections.sort(list);
    }

    public ArrayList<HighScore> getList() {
        return list;
    }
    
    public void addToList(HighScore score)
    {
        list.add(score);
        Collections.sort(list);
    }

    /**
     * Save highscores to sharedpreferences
     * @param context
     */
    public void save(Context context) {
        Set<String> jsonList = new HashSet<>();
        for (HighScore hs : list) {
            jsonList.add(hs.toString());
        }
        PreferenceManager
                .getDefaultSharedPreferences(context)
                .edit()
                .putStringSet(PREFERENCES_KEY, jsonList)
                .apply();
    }
   
}
