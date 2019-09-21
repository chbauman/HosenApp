package com.example.test;

import java.util.ArrayList;
import java.util.List;

public class GamePlant {

    // Fix constants
    static final int n_cards = 36;

    // Constants
    final int n_players;
    final int beginner_ind;

    // Derived constants
    final int n_used_cards;
    final int t_cards_start_ind;

    // Game state variables
    int turn_ind;
    int knock_ind;

    // Internal state variables
    private boolean declaration_pending;

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

    GamePlant(int n_players, int beginner_ind){
        if (beginner_ind >= n_players){
            throw new IllegalArgumentException("Beginner index can not be larger than number of players");
        }
        this.n_players = n_players;
        this.beginner_ind = beginner_ind;
        n_used_cards = n_players * 3 + 4;
        t_cards_start_ind = n_players * 3;
        startGame();
    }

    // (Re)set variables
    public void startGame(){
        // Shuffle deck
        List<Integer> rp = Util.getRandPerm(n_cards).subList(0, n_used_cards);
        used_cards = new ArrayList<>(rp);

        // Set states
        turn_ind = 0;
        knock_ind = -10 - n_players;

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
        if(declaration_pending){
            throw new IllegalStateException("Need to make a declaration first!");
        } else if(knock && take_all){
            throw new IllegalStateException("Cannot knock AND take all!");
        } else if(!chose_stack){
            throw new IllegalStateException("First player must choose stack first.");
        }

        final int player_id = (beginner_ind + turn_ind) % n_players;

        if(take_all){
            // Take all cards
            take_all(player_id);
        } else if(knock){
            // Cannot knock in first round
            if(turn_ind < n_players + 1){
                throw new IllegalStateException("Too early to knock you bastard!!");
            }
            // Knock
            knock_ind = turn_ind;
        } else {
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
        if(declaration_pending){
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
        if(turn_ind == knock_ind + n_players){
            game_over = true;
        }
        return new GameState(turn_ind, knock_ind, game_over, used_cards);
    }

    // Get information about the end of the game
    public GameOverState get_game_over_state(){
        if(!game_over){
            throw new IllegalStateException("Game not yet over!!");
        }

        float[] scores = new float[n_players];
        for(int i = 0; i < n_players; ++i){
            scores[i] = computeScore(i);
        }
        return new GameOverState(id_caught_in_trap, id_fired, id_wrong_declared, used_cards, scores);
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
