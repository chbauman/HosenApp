package com.example.test;

import java.util.ArrayList;

/**
 * Current state of the game.
 */
public class GameState {

    /**
     * See constructor.
     */
    int turn_ind;
    /**
     * See constructor.
     */
    boolean game_over;
    /**
     * See constructor.
     */
    int knock_ind;
    /**
     * See constructor.
     */
    ArrayList<Integer> game_cards;
    /**
     * See constructor.
     */
    Move lastMove;

    /**
     * Constructor.
     *
     * @param turn_ind  The index of the player who's turn it is.
     * @param knock_ind The id of the player who has knocked if anyone has, else -1.
     * @param game_over Whether the game is over.
     * @param cards     The array list containing the cards.
     * @param lastMove  The most recent move taken.
     */
    GameState(int turn_ind, int knock_ind, boolean game_over, ArrayList<Integer> cards, Move lastMove) {
        this.turn_ind = turn_ind;
        this.knock_ind = knock_ind;
        this.game_over = game_over;
        this.game_cards = cards;
        this.lastMove = lastMove;
    }
}
