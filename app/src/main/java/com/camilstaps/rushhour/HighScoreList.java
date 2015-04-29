/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.camilstaps.rushhour;

import android.content.Context;
import android.preference.PreferenceManager;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Created by halzyn on 23-4-15.
 */
public class HighScoreList {

    private final String PREFERENCES_KEY = "highscores";
    
    private List<HighScore> list;
    
    public HighScoreList (List<HighScore> some_list) {
        this.list = some_list;
    }

    /**
     * Get highscores from sharedpreferences
     * @param context
     */
    public HighScoreList(Context context) {
        Set<String> jsonList = PreferenceManager.getDefaultSharedPreferences(context).getStringSet(PREFERENCES_KEY, new HashSet<String>());
        for (String json : jsonList) {
            list.add(new HighScore(json));
        }
    }

    public List<HighScore> getList() {
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
        PreferenceManager.getDefaultSharedPreferences(context).edit().putStringSet(PREFERENCES_KEY, jsonList).apply();
    }
   
}
