/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.camilstaps.rushhour;

import java.util.Collections;
import java.util.List;

/**
 *
 * @author Created by halzyn on 23-4-15.
 */
public class HighScoreList {
    
    private List<HighScore> list;
    
    public HighScoreList (List<HighScore> some_list) {
        this.list = some_list;
    }
    
    public void addToList(HighScore score)
    {
        list.add(score);
        Collections.sort(list);
    }
   
}
