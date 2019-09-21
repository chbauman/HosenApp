package com.example.test;

import java.util.ArrayList;

public class GameOverState {

    int id_caught_in_trap;
    int id_fired;
    int id_wrong_declared;
    ArrayList<Integer> game_cards;
    float[] scores;

    GameOverState(int id_caught_in_trap, int id_fired, int id_wrong_declared, ArrayList<Integer> game_cards, float[] scores) {
        this.id_caught_in_trap = id_caught_in_trap;
        this.id_fired = id_fired;
        this.id_wrong_declared = id_wrong_declared;
        this.game_cards = game_cards;
        this.scores = scores;
    }
}
