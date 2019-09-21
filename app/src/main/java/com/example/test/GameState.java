package com.example.test;

import java.util.ArrayList;

public class GameState {

    int turn_ind;
    boolean game_over;
    int knock_ind;
    ArrayList<Integer> game_cards;

    GameState(int turn_ind, int knock_ind, boolean game_over, ArrayList<Integer> cards){
        this.turn_ind = turn_ind;
        this.knock_ind = knock_ind;
        this.game_over = game_over;
        this.game_cards = cards;
    }
}
