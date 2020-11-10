package com.example.test;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * The game plant.
 * <p>
 * Holds all information about the on-going game.
 */
class GamePlant {

    // Fix constants
    private static final int n_cards = 36;

    // Constants
    private final int n_players;

    int beginner_ind;

    // Derived constants
    private final int n_used_cards;
    private final int t_cards_start_ind;

    // Game state variables
    int turn_ind;
    private int knock_ind;

    // Internal state variables
    private boolean declaration_pending;
    private Move lastMove;

    // Start variables
    private boolean chose_stack;

    // End of game state variables
    private int id_caught_in_trap;
    private int id_fired;
    private int id_pants;
    private int id_wrong_declared;
    private boolean game_over;

    /**
     * ArrayList of cards that are in the game.
     * <p>
     * First three: the user's cards, next three: the following player's cards
     * and so on. Last 4: The three open and the hidden card (last).
     */
    ArrayList<Integer> used_cards;

    /**
     * Constructor.
     *
     * @param n_players NUmber of players, must be 4.
     */
    GamePlant(int n_players) {
        if (BuildConfig.DEBUG && n_players != 4) {
            throw new AssertionError("Assertion failed");
        }
        this.n_players = n_players;
        n_used_cards = n_players * 3 + 4;
        t_cards_start_ind = n_players * 3;
        beginner_ind = -1;
        startGame();
    }

    /**
     * Starts the game.
     * <p>
     * Resets all variables.
     */
    void startGame() {

        // Shuffle deck
        List<Integer> rp = Util.getRandPerm(n_cards).subList(0, n_used_cards);
        used_cards = new ArrayList<>(rp);

        // Set states
        turn_ind = 0;
        knock_ind = 1000;
        beginner_ind = (beginner_ind + 1) % n_players;

        declaration_pending = false;
        id_caught_in_trap = -1;
        id_fired = -1;
        id_pants = -1;
        id_wrong_declared = -1;
        game_over = false;
        chose_stack = false;
    }

    /**
     * Initial action.
     * <p>
     * Choose weather to swap the cards with the table cards or not.
     *
     * @param switch_cards Whether to choose the cards on the table
     */
    void choose_stack(boolean switch_cards) {

        if (switch_cards) {
            take_all(beginner_ind);
        }
        turn_ind++;
        chose_stack = true;
    }

    /**
     * Make a move.
     * <p>
     * Either knock, take all or specify which card to take and give.
     * The parameters `knock` and `take_all` cannot be both true!
     *
     * @param knock     Whether the current player knocks.
     * @param take_all  Whether the current player takes all open cards.
     * @param hand_ind  The index of the card in hand to swap.
     * @param table_ind The index of the card on the table to swap.
     */
    void make_move(boolean knock, boolean take_all, int hand_ind, int table_ind) {
        if (game_over) {
            throw new IllegalStateException("Game is fucking over!");
        }
        if (declaration_pending) {
            throw new IllegalStateException("Need to make a declaration first!");
        } else if (knock && take_all) {
            throw new IllegalStateException("Cannot knock AND take all!");
        } else if (!chose_stack) {
            throw new IllegalStateException("First player must choose stack first.");
        }

        lastMove = new Move(knock, take_all, hand_ind, table_ind);

        final int player_id = (beginner_ind + turn_ind) % n_players;

        Log.d("Player", "" + player_id);
        if (take_all) {
            // Take all cards
            take_all(player_id);
            Log.d("Took all", "now");
        } else if (knock) {
            // Cannot knock in first round
            Log.d("Knocked", "now");
            if (turn_ind < n_players + 1) {
                throw new IllegalStateException("Too early to knock you bastard!!");
            }
            // Knock
            if (knock_ind == 1000) {
                knock_ind = turn_ind;
            }
        } else {
            Log.d("Took single card", "now");
            // Check if trap was set
            if (table_ind != 3 && trapOnTable()) {
                Log.d("Caught player", "in trap");
                id_caught_in_trap = player_id;
            }
            // Swap cards
            take_single(player_id, hand_ind, table_ind);
        }

        declaration_pending = true;
    }

    /**
     * Declare if you are on fire or you're pants are down.
     * Needs to be called after `make_move`.
     *
     * @param pants_or_fire Whether a player declares either.
     * @return Game state after this move.
     */
    GameState declare(boolean pants_or_fire) {
        if (!declaration_pending) {
            throw new IllegalStateException("Need to make a move first!");
        }

        final int player_id = (beginner_ind + turn_ind) % n_players;
        if (pants_or_fire) {
            Log.d("Player", "" + player_id);
            Log.d("declaring", "füür oder so");
            if (onFire(player_id)) {
                id_fired = player_id;
            } else if (!pantsDown(player_id)) {
                id_wrong_declared = player_id;
            } else {
                id_pants = player_id;
            }
            game_over = true;
        }

        if (id_caught_in_trap >= 0) {
            game_over = true;
        }

        declaration_pending = false;
        turn_ind++;
        if (turn_ind >= knock_ind + n_players) {
            game_over = true;
        }
        return new GameState(turn_ind, knock_ind, game_over, used_cards, lastMove);
    }

    // Get information about the end of the game
    GameOverState get_game_over_state() {
        if (!game_over) {
            throw new IllegalStateException("Game not yet over!!");
        }

        Float[] scores = new Float[n_players];
        for (int i = 0; i < n_players; ++i) {
            scores[i] = computeScore(i);
        }
        return new GameOverState(id_caught_in_trap, id_fired, id_wrong_declared, used_cards, scores, turn_ind, id_pants);
    }

    /**
     * Finds and executes a move for a player.
     *
     * @param playerID The ID of the player whose move needs to be found.
     * @return GameState after executing selected move.
     */
    GameState findMoveAdvanced(int playerID) {

        final int player_id = (beginner_ind + turn_ind) % n_players;
        if (player_id != playerID) {
            Log.d("turn_id", "" + turn_ind);
            Log.d("beginner_ind", "" + beginner_ind);
            Log.d("playerID", "" + playerID);
            throw new IllegalStateException("Not this player's turn");
        }

        // Initialize
        int table_index = -1;
        int hand_index = -1;
        boolean has_knocked = false;
        boolean ta = false;

        // Compute current score
        final float curr_score = computeScore(playerID);
        if (curr_score > 26.0f && turn_ind > n_players) {
            has_knocked = true;
        }

        // Compute best score after changing
        int[] table_cards = new int[3];
        int[] hand_cards = new int[3];
        int table_base_ind = n_players * 3;
        int hand_base_ind = playerID * 3;

        // Copy current cards
        for (int i = 0; i < 3; ++i) {
            table_cards[i] = used_cards.get(table_base_ind + i);
            hand_cards[i] = used_cards.get(hand_base_ind + i);
        }

        // Try all swaps and choose the one yielding maximum score
        float curr_max_score = curr_score;
        float temp_score;
        for (int i = 0; i < 3; ++i) {
            for (int k = 0; k < 3; ++k) {
                // Set card
                used_cards.set(hand_base_ind + i, table_cards[k]);
                temp_score = computeScore(playerID);
                if (temp_score > curr_max_score) {
                    curr_max_score = temp_score;
                    table_index = k;
                    hand_index = i;
                }
                // Set back
                used_cards.set(hand_base_ind + i, hand_cards[i]);
            }
        }

        // Check Take all
        for (int k = 0; k < 3; ++k) {
            used_cards.set(hand_base_ind + k, table_cards[k]);
        }
        float take_all_score = computeScore(playerID);
        if (take_all_score > curr_max_score) {
            ta = true;
            curr_max_score = take_all_score;
        }
        for (int k = 0; k < 3; ++k) {
            used_cards.set(hand_base_ind + k, hand_cards[k]);
        }

        // Check for trap
        if (trapOnTable()) {
            if (take_all_score > curr_score) {
                ta = true;
            } else {
                table_index = 3;
                hand_index = 0;
            }
        }

        // Take hidden card
        if (curr_max_score <= curr_score) {
            table_index = 3;

            // Choose hand card with least value
            hand_index = 0;
            int curr_min = 15;
            for(int i = 0; i < 3; ++i){
                final int curr_hand_card_id = hand_cards[i];
                final int curr_number = curr_hand_card_id % 9;
                final int curr_val = Cards.card_id_to_value(curr_number);
                if(curr_val < curr_min){
                    curr_min = curr_val;
                    hand_index = i;
                }
            }
        }

        // Move
        Log.d("now", "moving");
        Move m;
        if (has_knocked) {
            make_move(true, false, -1, -1);
            m = new Move(true, false, -1, -1);
        } else if (ta) {
            make_move(false, true, -1, -1);
            m = new Move(false, true, -1, -1);
        } else {
            make_move(false, false, hand_index, table_index);
            m = new Move(false, false, hand_index, table_index);
        }
        lastMove = m;

        // Check for fire or pants down
        float score_after = computeScore(playerID);
        boolean dec = false;
        if (score_after > 30.5f) {
            dec = true;
        }

        Log.d("now", "declaring");
        // Declare
        return declare(dec);
    }

    boolean chooseStackAI(int playerID) {
        final float curr_score = computeScore(playerID);
        final boolean res = curr_score < 17.0f;
        choose_stack(res);
        return res;
    }

    // Actions
    private void take_single(int player_id, int hand_ind, int table_ind) {

        final int p_card_ind = player_id * 3 + hand_ind;
        final int t_card_ind = t_cards_start_ind + table_ind;
        int curr_hand_card = used_cards.get(p_card_ind);
        int curr_table_card = used_cards.get(t_card_ind);
        used_cards.set(p_card_ind, curr_table_card);
        used_cards.set(t_card_ind, curr_hand_card);

    }

    /**
     * Take all open card as a move.
     *
     * @param player_id The player that takes all cards.
     */
    private void take_all(int player_id) {
        final int p_cards_start_ind = player_id * 3;
        int[] temp = new int[3];
        for (int i = 0; i < 3; ++i) {
            temp[i] = used_cards.get(p_cards_start_ind + i);
            int t_card = used_cards.get(t_cards_start_ind + i);
            used_cards.set(p_cards_start_ind + i, t_card);
        }
        for (int i = 0; i < 3; ++i) {
            used_cards.set(t_cards_start_ind + i, temp[i]);
        }
    }

    /**
     * Computes the score of a player.
     *
     * @param playerID The ID of the player.
     * @return The score.
     */
    private float computeScore(int playerID) {

        // Hose
        if (pantsDown(playerID)) {
            return 31.0f;
        }

        // Füür
        if (onFire(playerID)) {
            return 32.0f;
        }

        // Gliichi Zahl
        if (check30andHalf(playerID)) {
            return 30.5f;
        }

        int[] col_points = new int[4];
        for (int i = 0; i < 4; ++i) {
            col_points[i] = 0;
        }
        for (int i = 0; i < 3; ++i) {
            final int hand_arr_ind = playerID * 3 + i;
            final int curr_hand_card_id = used_cards.get(hand_arr_ind);
            final int curr_number = curr_hand_card_id % 9;
            final int curr_color = curr_hand_card_id / 9;
            col_points[curr_color] += Cards.card_id_to_value(curr_number);
        }
        int currMax = col_points[0];
        for (int i = 0; i < 3; ++i) {
            final int curr_val = col_points[i + 1];
            if (curr_val > currMax) {
                currMax = curr_val;
            }
        }
        return (float) currMax;
    }

    /**
     * Checks if a player has three cards of one kind.
     *
     * @param playerID Player ID.
     * @return Whether the player has three cards of one kind in his hands.
     */
    private boolean check30andHalf(int playerID) {
        final int first_card_arr_ind = playerID * 3;
        final int first_card_id = used_cards.get(first_card_arr_ind);
        int number = first_card_id % 9;
        for (int i = 0; i < 3; ++i) {
            final int hand_arr_ind = playerID * 3 + i;
            final int curr_hand_card_id = used_cards.get(hand_arr_ind);
            final int curr_number = curr_hand_card_id % 9;
            if (number != curr_number) {
                return false;
            }
        }
        return true;
    }

    private boolean oneColor(int playerID) {
        final int first_card_arr_ind = playerID * 3;
        final int first_card_id = used_cards.get(first_card_arr_ind);
        final int color = first_card_id / 9;
        for (int i = 0; i < 3; ++i) {
            final int hand_arr_ind = playerID * 3 + i;
            final int curr_hand_card_id = used_cards.get(hand_arr_ind);
            final int curr_color = curr_hand_card_id / 9;
            if (curr_color != color) {
                return false;
            }
        }
        return true;
    }

    private boolean onFire(int playerID) {
        for (int i = 0; i < 3; ++i) {
            final int hand_arr_ind = playerID * 3 + i;
            final int curr_hand_card_id = used_cards.get(hand_arr_ind);
            if (curr_hand_card_id % 9 != 0) {
                return false;
            }
        }
        return true;
    }

    private boolean pantsDown(int playerID) {
        final int first_card_arr_ind = playerID * 3;
        final int first_card_id = used_cards.get(first_card_arr_ind);
        final int color = first_card_id / 9;
        boolean has_ace = false;
        for (int i = 0; i < 3; ++i) {
            final int hand_arr_ind = playerID * 3 + i;
            final int curr_hand_card_id = used_cards.get(hand_arr_ind);
            final int curr_color = curr_hand_card_id / 9;
            final boolean right_col = curr_color == color;
            final boolean ace = curr_hand_card_id % 9 == 0;
            final boolean image = curr_hand_card_id % 9 > 4;
            if (ace) {
                has_ace = true;
            }
            if (!right_col || (!ace && !image)) {
                return false;
            }
        }
        return has_ace;
    }

    /**
     * Checks if there lies a trap on the table.
     *
     * @return True if there is a trap.
     */
    private boolean trapOnTable() {
        if (check30andHalf(4)) {
            return true;
        } else {
            return oneColor(4);
        }
    }
}
