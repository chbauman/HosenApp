package com.example.test;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class GamePlant {

    // Fix constants
    static final int n_cards = 36;

    // Constants
    final int n_players;

    int beginner_ind;

    // Derived constants
    final int n_used_cards;
    final int t_cards_start_ind;

    // Game state variables
    int turn_ind;
    int knock_ind;

    // Internal state variables
    private boolean declaration_pending;
    Move lastMove;

    // Start variables
    boolean chose_stack;

    // End of game state variables
    int id_caught_in_trap;
    int id_fired;
    int id_wrong_declared;
    boolean game_over;

    // Cards that are in the game
    // First 3: your cards
    // next 3 * (n_players - 1): Other players cards
    // last 4: Three open and one hidden card (last)
    ArrayList<Integer> used_cards;

    GamePlant(int n_players){
        this.n_players = n_players;
        n_used_cards = n_players * 3 + 4;
        t_cards_start_ind = n_players * 3;
        beginner_ind = -1;
        startGame();
    }

    // (Re)set variables
    public void startGame(){

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
        id_wrong_declared = -1;
        game_over = false;
        chose_stack = false;
    }

    // Choose weather to swap the cards with the table cards
    public void choose_stack(boolean switch_cards){

        if(switch_cards){
            take_all(beginner_ind);
        }
        turn_ind++;
        chose_stack = true;
    }

    // Make a move, either knock, take all or
    // specify which card to take and give.
    public void make_move(boolean knock, boolean take_all, int hand_ind, int table_ind){
        if(game_over){
            throw new IllegalStateException("Game is fucking over!");
        }
        if(declaration_pending){
            throw new IllegalStateException("Need to make a declaration first!");
        } else if(knock && take_all){
            throw new IllegalStateException("Cannot knock AND take all!");
        } else if(!chose_stack){
            throw new IllegalStateException("First player must choose stack first.");
        }

        lastMove = new Move(knock, take_all, hand_ind, table_ind);

        final int player_id = (beginner_ind + turn_ind) % n_players;

        Log.d("Player", "" + player_id);
        if(take_all){
            // Take all cards
            take_all(player_id);
            Log.d("Took all", "now");
        } else if(knock){
            // Cannot knock in first round
            Log.d("Knocked", "now");
            if(turn_ind < n_players + 1){
                throw new IllegalStateException("Too early to knock you bastard!!");
            }
            // Knock
            if(knock_ind == 1000){
                knock_ind = turn_ind;
            }
        } else {
            Log.d("Took single card", "now");
            // Check if trap was set
            if(trapOnTable()) {
                id_caught_in_trap = player_id;
            }
            // Swap cards
            take_single(player_id, hand_ind, table_ind);
        }

        declaration_pending = true;
    }

    // After move declare if you are on fire or your pants are down.
    public GameState declare(boolean pants_or_fire){
        if(!declaration_pending){
            throw new IllegalStateException("Need to make a move first!");
        }

        final int player_id = (beginner_ind + turn_ind) % n_players;
        if(pants_or_fire){
            if(onFire(player_id)){
                id_fired = player_id;
            } else if(!pantsDown(player_id)){
                id_wrong_declared = player_id;
            }
            game_over = true;
        }

        if(id_caught_in_trap >= 0){
            game_over = true;
        }

        declaration_pending = false;
        turn_ind++;
        if(turn_ind >= knock_ind + n_players){
            game_over = true;
        }
        return new GameState(turn_ind, knock_ind, game_over, used_cards, lastMove);
    }

    // Get information about the end of the game
    public GameOverState get_game_over_state(){
        if(!game_over){
            throw new IllegalStateException("Game not yet over!!");
        }

        Float[] scores = new Float[n_players];
        for(int i = 0; i < n_players; ++i){
            scores[i] = computeScore(i);
        }
        return new GameOverState(id_caught_in_trap, id_fired, id_wrong_declared, used_cards, scores, turn_ind);
    }

    // Automatic moving
    public GameState findMoveAdvanced(int playerID){

        final int player_id = (beginner_ind + turn_ind) % n_players;
        if(player_id != playerID){
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
        if(curr_score > 26.0f && turn_ind >= n_players){
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
            hand_index = 0;
        }

        // Move
        Log.d("now", "moving");
        Move m;
        if(has_knocked){
            make_move(true, false, -1, -1);
            m = new Move(true, false, -1, -1);
        } else if(ta){
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
        if(score_after > 30.5f){
            dec = true;
        }

        Log.d("now", "declaring");
        // Declare
        return declare(dec);
    }

    // Actions
    public void take_single(int player_id, int hand_ind, int table_ind){

        final int p_card_ind = player_id * 3 + hand_ind;
        final int t_card_ind = t_cards_start_ind + table_ind;
        int curr_hand_card = used_cards.get(p_card_ind);
        int curr_table_card = used_cards.get(t_card_ind);
        used_cards.set(p_card_ind, curr_table_card);
        used_cards.set(t_card_ind, curr_hand_card);

    }

    public void take_all(int player_id){
        final int p_cards_start_ind = player_id * 3;
        int[] temp = new int[3];
        for(int i = 0; i < 3; ++i){
            temp[i] = used_cards.get(p_cards_start_ind + i);
            int t_card = used_cards.get(t_cards_start_ind + i);
            used_cards.set(p_cards_start_ind + i, t_card);
        }
        for(int i = 0; i < 3; ++i){
            used_cards.set(t_cards_start_ind + i, temp[i]);
        }
    }

    // Analysis
    public float computeScore(int playerID){

        // Hose
        if(pantsDown(playerID)){
            return 31.0f;
        }

        // Füür
        if(onFire(playerID)){
            return 32.0f;
        }

        // Gliichi Zahl
        if(check30andHalf(playerID)){
            return 30.5f;
        }

        int[] col_points = new int[4];
        for(int i = 0; i < 4; ++i){
            col_points[i] = 0;
        }
        for(int i = 0; i < 3; ++i){
            final int hand_arr_ind = playerID * 3 + i;
            final int curr_hand_card_id = used_cards.get(hand_arr_ind);
            final int curr_number = curr_hand_card_id % 9;
            final int curr_color = curr_hand_card_id / 9;
            col_points[curr_color] += Cards.card_id_to_value(curr_number);
        }
        int currMax = col_points[0];
        for(int i = 0; i < 3; ++i){
            final int curr_val = col_points[i + 1];
            if(curr_val > currMax){
                currMax = curr_val;
            }
        }
        return (float) currMax;
    }

    public boolean check30andHalf(int playerID){
        final int first_card_arr_ind = playerID * 3;
        final int first_card_id = used_cards.get(first_card_arr_ind);
        int number = first_card_id % 9;
        for(int i = 0; i < 3; ++i){
            final int hand_arr_ind = playerID * 3 + i;
            final int curr_hand_card_id = used_cards.get(hand_arr_ind);
            final int curr_number = curr_hand_card_id % 9;
            if(number != curr_number){
                return false;
            }
        }
        return true;
    }

    public boolean oneColor(int playerID){
        final int first_card_arr_ind = playerID * 3;
        final int first_card_id = used_cards.get(first_card_arr_ind);
        final int color = first_card_id / 9;
        for(int i = 0; i < 3; ++i){
            final int hand_arr_ind = playerID * 3 + i;
            final int curr_hand_card_id = used_cards.get(hand_arr_ind);
            final int curr_color = curr_hand_card_id / 9;
            if(curr_color != color) {
                return false;
            }
        }
        return true;
    }

    public boolean onFire(int playerID){
        for(int i = 0; i < 3; ++i){
            final int hand_arr_ind = playerID * 3 + i;
            final int curr_hand_card_id = used_cards.get(hand_arr_ind);
            if(curr_hand_card_id % 9 != 0) {
                return false;
            }
        }
        return true;
    }

    public boolean pantsDown(int playerID){
        final int first_card_arr_ind = playerID * 3;
        final int first_card_id = used_cards.get(first_card_arr_ind);
        final int color = first_card_id / 9;
        boolean has_ace = false;
        for(int i = 0; i < 3; ++i){
            final int hand_arr_ind = playerID * 3 + i;
            final int curr_hand_card_id = used_cards.get(hand_arr_ind);
            final int curr_color = curr_hand_card_id / 9;
            final boolean right_col = curr_color == color;
            final boolean ace = curr_hand_card_id % 9 == 0;
            final boolean image = curr_hand_card_id % 9 > 4;
            if(ace){
                has_ace = true;
            }
            if(!right_col || (!ace && !image)) {
                return false;
            }
        }
        if(has_ace){
            return true;
        }
        return false;
    }

    public boolean trapOnTable(){
        if(check30andHalf(4)){
            return true;
        } else {
            return oneColor(4);
        }
    }
}
