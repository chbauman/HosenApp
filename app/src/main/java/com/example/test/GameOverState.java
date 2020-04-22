package com.example.test;

import android.util.Log;

import java.util.ArrayList;

class GameOverState {

    int id_caught_in_trap;
    int id_fired;
    int id_pants;
    int id_wrong_declared;
    int n_turns;
    Float[] scores;

    GameOverState(int id_caught_in_trap, int id_fired, int id_wrong_declared, ArrayList<Integer> game_cards, Float[] scores, int n_turns, int id_pants) {
        Log.d("First card:", Integer.toString(game_cards.get(0)));
        this.id_caught_in_trap = id_caught_in_trap;
        this.id_fired = id_fired;
        this.id_wrong_declared = id_wrong_declared;
        this.scores = scores;
        this.n_turns = n_turns;
        this.id_pants = id_pants;
    }
}
