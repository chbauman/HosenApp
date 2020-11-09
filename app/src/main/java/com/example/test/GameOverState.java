package com.example.test;

import android.util.Log;

import java.util.ArrayList;

/**
 * Holds the information about the outcome of the game.
 */
class GameOverState {
    /**
     * The id of the player that was trapped, -1 if none was.
     */
    int id_caught_in_trap;
    /**
     * The id of the player that called fire, -1 if none was.
     */
    int id_fired;
    /**
     * The id of the player that called pants, -1 if none was.
     */
    int id_pants;
    /**
     * The id of the player that wrongfully declared pants or fire, -1 if none was.
     */
    int id_wrong_declared;
    /**
     * The number of turns that were played.
     */
    int n_turns;
    /**
     * An array containing the scores of the players.
     */
    Float[] scores;

    /**
     * Constructor.
     *
     * @param id_caught_in_trap See corresponding field.
     * @param id_fired See corresponding field.
     * @param id_wrong_declared See corresponding field.
     * @param game_cards See corresponding field.
     * @param scores See corresponding field.
     * @param n_turns See corresponding field.
     * @param id_pants See corresponding field.
     */
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
