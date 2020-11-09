package com.example.test;

/**
 * Move holds the information of one move of a player.
 */
class Move {
    /**
     * The index of the card in hand, 0 to 2.
     */
    int h_index;
    /**
     * The index of the card to switch with, 0 to 3.
     * <p>
     * 3 is the covered card.
     */
    int t_index;
    /**
     * True if player knocks.
     * <p>
     * Indices will be ignored in this case.
     */
    boolean knock;
    /**
     * Take all shown cards on table.
     */
    boolean take_all;

    /**
     * Constructor.
     *
     * @param knock    See field with same name.
     * @param take_all See field with same name.
     * @param h_index  See field with same name.
     * @param t_index  See field with same name.
     */
    Move(boolean knock, boolean take_all, int h_index, int t_index) {
        this.h_index = h_index;
        this.t_index = t_index;
        this.knock = knock;
        this.take_all = take_all;
    }
}
