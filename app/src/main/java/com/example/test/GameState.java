package com.example.test;

import java.util.ArrayList;


public class GameState {

    int turn_ind;
    boolean game_over;
    int knock_ind;
    ArrayList<Integer> game_cards;
    Move lastMove;

    GameState(int turn_ind, int knock_ind, boolean game_over, ArrayList<Integer> cards, Move lastMove){
        this.turn_ind = turn_ind;
        this.knock_ind = knock_ind;
        this.game_over = game_over;
        this.game_cards = cards;
        this.lastMove = lastMove;
    }
}
