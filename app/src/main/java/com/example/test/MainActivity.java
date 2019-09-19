package com.example.test;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.EditText;
import android.widget.ImageView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements GameOverDialog.GODialogListener {

    int n_used_cards;
    int n_play_cs;
    final int n_players = 4;
    final int n_cards = 36;
    final int baseAnimTime = 500;
    final String[] ps = {"Ich", "Hansli", "Peter", "Ruedi"};

    // Cards that are in the game
    // First 3: your cards
    // next 3 * (n_players - 1): Other players cards
    // last 4: Three open and one hidden card (last)
    ArrayList<Integer> used_cards;

    Bitmap cards;

    // State variables
    int sel_hand_c = -1;
    int chlopfed = -100;
    int turn_ind = 0;

    // End game states
    boolean game_over = false;
    int fire_index = -1;
    int forced_lost_index = -1;
    int hose_index = -1;

    // Who's turn is it
    // 0: You
    // 1, 2, 3: Player 1, 2, 3
    int turn = 0;

    // Array containing the Views of the hand cards
    ImageView[] hc_views;

    // Array containing the Views of the table cards
    ImageView[] tc_views;

    // Array containing the Views of the other players
    ImageView[] p_views;

    public void startNew(){
        startNewGame();
    }

    public void startNewGame(){

        // Set state variables
        sel_hand_c = -1;
        chlopfed = -100;
        turn_ind = 0;
        game_over = false;
        turn = 0;
        forced_lost_index = -1;
        fire_index = -1;
        hose_index = -1;

        // Shuffle deck
        List<Integer> rp = getRandPerm(n_cards).subList(0, n_used_cards);
        used_cards = new ArrayList<>(rp);

        // Set Card images
        setHidden();
        setPlayerBGImages();
        for(int i = 0; i < 3; ++i){
            int hand_cd_id = used_cards.get(i);
            int table_cd_id = used_cards.get(n_play_cs + i);
            setCard(hand_cd_id, i, true);
            setCard(table_cd_id, i, false);
        }
    }

    @TargetApi(21)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Load the cards
        Resources res = getResources();
        Drawable drawable = res.getDrawable(R.drawable.ic_card_deck_161536, getTheme());
        VectorDrawable vectorDrawable = (VectorDrawable) drawable;
        cards = getBitmap(vectorDrawable, 615, 1027);

        // Set the views
        hc_views = new ImageView[3];
        tc_views = new ImageView[4];
        p_views = new ImageView[3];
        hc_views[0] = findViewById(R.id.card1);
        hc_views[1] = findViewById(R.id.card2);
        hc_views[2] = findViewById(R.id.card3);
        tc_views[0] = findViewById(R.id.cd_op1);
        tc_views[1] = findViewById(R.id.cd_op2);
        tc_views[2] = findViewById(R.id.cd_op3);
        tc_views[3] = findViewById(R.id.cd_verd);
        p_views[0] = findViewById(R.id.p3);
        p_views[1] = findViewById(R.id.p2);
        p_views[2] = findViewById(R.id.p1);



        // Initialize game
        n_play_cs = n_players * 3;
        n_used_cards = n_play_cs + 4;

        startNewGame();

        // Set onTouch
        for(int i = 0; i < 3; ++i) {
            final int i_final = i;

            // For hand cards
            ImageView img = hc_views[i];
            img.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(final View view, final MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        sel_hand_c = i_final;
                    }
                    return true;
                }
            });
        }
        for(int i = 0; i < 4; ++i){
            final int i_final = i;

            // For table cards
            ImageView img = tc_views[i];
            img.setOnTouchListener(new View.OnTouchListener()
            {
                @Override
                public boolean onTouch(final View view, final MotionEvent event) {

                    if (event.getAction() == MotionEvent.ACTION_DOWN && turn == 0) {

                        Log.d("UP", "fuck " + i_final);
                        if(sel_hand_c != -1 && !game_over){

                            // Check Falle
                            final boolean trap_set = trapOnTable();

                            // Swap
                            int hand_ind = sel_hand_c;
                            int table_ind = n_play_cs + i_final;

                            int hand_c_id = used_cards.get(hand_ind);
                            int table_c_id = used_cards.get(table_ind);

                            // Swap
                            used_cards.set(hand_ind, table_c_id);
                            used_cards.set(table_ind, hand_c_id);
                            setCard(table_c_id, hand_ind, true);
                            if(i_final != 3){
                                setCard(hand_c_id, i_final, false);
                            }

                            // Abort Game if trap hit
                            if(trap_set){
                                Log.d("falle", "Haha, so dummm!");
                                forced_lost_index = 0;
                                openGameOverDialog();
                                return true;
                            }

                            // Reset Index
                            sel_hand_c = -1;
                            turn_ind += 1;
                            if(chlopfed == turn_ind - n_players){
                                Log.d("Game ", "Over");
                                game_over = true;
                                openGameOverDialog();
                                return true;
                            }

                            // TODO: check if Falle, Hose oder Füür

                            // Next player's turn
                            nextTurn();
                        }
                    }
                    return true;
                } });
        }
    }

    public void openGameOverDialog(){

        Log.d("openGame", "called");

        // Compute points of all players
        Float[] scores = new Float[n_players];
        for(int i = 0; i < n_players; ++i){
            scores[i] = computeScore(i);
        }

        // Fire
        if(fire_index >= 0){
            for(int i = 0; i < n_players; ++i){
                if(i != fire_index){
                    scores[i] = 0.0f;
                }
            }
        }
        else {
            // Trap or wrongly announced hose or fireButton
            if (forced_lost_index >= 0) {
                scores[forced_lost_index] = 0.0f;
            }
            // Unannounced hose or fireButton
            for (int i = 0; i < n_players; ++i) {
                if (i != hose_index && (pantsDown(i) || onFire(i))) {
                    Log.d("unannounced", ps[i]);
                    scores[i] = 0.0f;
                }
            }
        }

        // Sort and keep indices
        int[] indices = new IndirectSorter<Float>().sort(scores);
        float[] scores_cp = new float[n_players];
        String[] players_cp = new String[n_players];
        for (int i = 0; i < n_players; ++i) {
            final int curr_ind = indices[i];
            scores_cp[n_players - 1 - i] = scores[curr_ind];
            players_cp[n_players - 1 - i] = ps[curr_ind];
        }

        // Put data for dialog into bundle
        GameOverDialog goDialog = new GameOverDialog();
        Bundle game_over_data = new Bundle();
        game_over_data.putStringArray("player_names", players_cp);
        game_over_data.putFloatArray("player_scores", scores_cp);
        game_over_data.putInt("n_players", n_players);
        goDialog.setArguments(game_over_data);
        goDialog.show(getSupportFragmentManager(),"GameOverDialog");
    }

    public void nextTurn(){
        // Next player's turn
        Handler handler = new Handler();
        for(int k = 0; k < n_players - 1; ++k){
            turn += 1;
            final int final_k = k;
            handler.postDelayed(new Runnable() {
                public void run() {
                    if(!game_over){
                        makeMove(final_k);
                        if(chlopfed == turn_ind - n_players){
                            Log.d(ps[final_k + 1], "over wege chlopf");
                            game_over = true;
                        }
                    }
                }
            }, (final_k + 1) * baseAnimTime * 2);
        }
        handler.postDelayed(new Runnable() {
            public void run() {
                turn = 0;
                if(game_over){
                    openGameOverDialog();
                }
            }
        }, (n_players) * baseAnimTime * 2);
    }

    public TranslateAnimation getLinearAnim(@NotNull ImageView v1, @NotNull ImageView v2, int animTime){

        float centreX_player = v1.getX() + v1.getWidth()  / 2.0f;
        float centreY_player = v1.getY() + v1.getHeight() / 2.0f;
        float centreX_dest = v2.getX() + v2.getWidth()  / 2.0f;
        float centreY_dest = v2.getY() + v2.getHeight() / 2.0f;

        final float dx = centreX_dest - centreX_player;
        final float dy = centreY_dest - centreY_player;

        TranslateAnimation anim = new TranslateAnimation(0.0f, dx, 0.0f, dy);
        anim.setInterpolator(new LinearInterpolator());
        anim.setRepeatCount(0);
        anim.setDuration(animTime);
        return anim;
    }

    public class Move{
        Move(int t, int h, boolean hc, boolean fire, boolean ta, boolean hose){
            table_index = t;
            hand_index = h;
            has_knocked = hc;
            fired = fire;
            take_all = ta;
            has_hose = hose;
        }
        int table_index;
        int hand_index;
        boolean has_knocked;
        boolean fired;
        boolean take_all;
        boolean has_hose;
    }

    // AI for other players
    public Move findMove(int playerID){
        // Random Move
        Random gen = new Random();
        final int table_index = gen.nextInt(4);
        final int hand_index = gen.nextInt(3);
        final boolean has_knocked = false;
        final boolean fired = false;
        final boolean ta = false;
        final boolean h = false;
        return new Move(table_index, hand_index, has_knocked, fired, ta, h);
    }

    public Move findMoveAdvanced(int playerID){

        // Initialize
        int table_index = -1;
        int hand_index = -1;
        boolean has_knocked = false;
        boolean fired = false;
        boolean ta = false;
        boolean h = false;

        // Knock if current score is higher than 26
        final float curr_score = computeScore(playerID);
        if(curr_score > 26.0f && turn_ind >= n_players){
            return new Move(-1, -1, true, false, false, false);
        }

        // Compute best score after changing
        int[] table_cards = new int[3];
        int[] hand_cards = new int[3];
        int table_base_ind = n_players * 3;
        int hand_base_ind = playerID * 3;

        // Copy current cards
        for(int i = 0; i < 3; ++i){
            table_cards[i] = used_cards.get(table_base_ind + i);
            hand_cards[i] = used_cards.get(hand_base_ind + i);
        }

        // Try all swaps and choose the one yielding maximum score
        float curr_max_score = curr_score;
        float temp_score;
        for(int i = 0; i < 3; ++i){
            for(int k = 0; k < 3; ++k){
                // Set card
                used_cards.set(hand_base_ind + i, table_cards[k]);
                temp_score = computeScore(playerID);
                if(temp_score > curr_max_score){
                    curr_max_score = temp_score;
                    table_index = k;
                    hand_index = i;
                }
                // Set back
                used_cards.set(hand_base_ind + i, hand_cards[i]);
            }
        }

        // Check Take all
        for(int k = 0; k < 3; ++k) {
            used_cards.set(hand_base_ind + k, table_cards[k]);
        }
        float take_all_score = computeScore(playerID);
        if(take_all_score > curr_max_score){
            ta = true;
            curr_max_score = take_all_score;
        }
        for(int k = 0; k < 3; ++k){
            used_cards.set(hand_base_ind + k, hand_cards[k]);
        }

        // Take hidden card
        if(curr_max_score <= curr_score){
            table_index = 3;
            hand_index = 0;
        }

        // Check for fireButton or hose after moving
        if(curr_max_score == 32.0f){
            fired = true;
        } else if(curr_max_score == 31.0f){
            h = true;
        }

        // Return
        return new Move(table_index, hand_index, has_knocked, fired, ta, h);
    }

    // Animation
    public void takeSingleCardWithAnim(int playerId, int table_index, int hand_index){

        Log.d(ps[playerId], "taking card " + table_index);
        final int t_arr_ind = n_play_cs + table_index;
        final int h_arr_ind = playerId * 3 + hand_index;

        // Swap
        final int hand_c_id = used_cards.get(h_arr_ind);
        final int table_c_id = used_cards.get(t_arr_ind);

        if(playerId != 0) {
            // Animate
            final int animTime = baseAnimTime;
            ImageView playerView = p_views[playerId - 1];
            ImageView destView = tc_views[table_index];
            TranslateAnimation anim = getLinearAnim(playerView, destView, animTime);

            //Start animation
            playerView.startAnimation(anim);
            if (table_index != 3) {
                final int t_ind = table_index;
                final int p_fin = playerId - 1;
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {
                        setCard(hand_c_id, t_ind, false);
                        setPlayerBG(p_fin);
                    }
                }, animTime);
            }
        }

        // Swap
        used_cards.set(h_arr_ind, table_c_id);
        used_cards.set(t_arr_ind, hand_c_id);
        if(table_index != 3){
            setPlayerCard(playerId - 1, hand_c_id);
        }
    }

    // Player actions
    public void makeMove(int playerId){

        if(game_over){
            return;
        }

        // Find Move
        Move nextMove = findMoveAdvanced(playerId + 1);

        Log.d("Player ", " " + playerId);
        Log.d("turn", "" + turn_ind);
        Log.d("knockButton", "" + chlopfed);

        final boolean k = nextMove.has_knocked;
        final boolean fr = nextMove.fired;
        final boolean t = nextMove.take_all;
        final boolean hs = nextMove.has_hose;

        // Check if knocked
        if(k){
            Log.d("Player " + (playerId + 1), " knocked");
            knock();
            return;
        }
        // Take all cards
        if(t){
            Log.d("Player " + (playerId + 1), " took all");
            takeAll(playerId + 1);
            // TODO: animate
            return;
        }

        final boolean trap_set = trapOnTable();

        final int table_index = nextMove.table_index;
        final int hand_index = nextMove.hand_index;

        takeSingleCardWithAnim(playerId + 1, table_index, hand_index);

        // Check if fireButton or hose after moving
        if(fr){
            Log.d("Player " + (playerId + 1), " fired");
            if(onFire(playerId + 1)){
                fire_index = playerId + 1;
            } else {
                forced_lost_index = playerId + 1;
            }
            game_over = true;
            return;
        }
        if(hs){
            Log.d("Player " + (playerId + 1), " hosed");
            if(pantsDown(playerId + 1)){
                hose_index = playerId + 1;
            } else {
                forced_lost_index = playerId + 1;
            }
            game_over = true;
            return;
        }

        // Check for traps
        if(trap_set && !t && table_index != 3){
            Log.d("falle", "Haha, so dummm!");
            forced_lost_index = playerId + 1;
            game_over = true;
            return;
        }

        // Next turn
        turn_ind += 1;

    }

    public void knock(){
        if(turn_ind < n_players){
            throw new IllegalStateException("Cannot knock now!");
        }
        if(chlopfed < 0){
            chlopfed = turn_ind;
        }
        turn_ind += 1;
        turn += 1;
    }

    public void takeAll(int playerID){
        for(int i = 0; i < 3; ++i){
            final int hand_arr_ind = playerID * 3 + i;
            final int curr_hand_card_id = used_cards.get(hand_arr_ind);
            final int table_arr_ind = n_play_cs + i;
            final int curr_table_card_id = used_cards.get(table_arr_ind);
            used_cards.set(table_arr_ind, curr_hand_card_id);
            used_cards.set(hand_arr_ind, curr_table_card_id);
            setCard(curr_hand_card_id, i, false);
            if(playerID == 0){
                setCard(curr_table_card_id, i, true);
            }
        }
        // TODO: animate
        turn_ind += 1;
    }

    // Button methods
    public void knockButton(View view) {
        if(turn_ind >= n_players){
            if(turn == 0){
                knock();
                nextTurn();
            }
        }
    }

    public void takeAllButton(View view) {
        if(turn == 0){
            takeAll(0);
            nextTurn();
        }
    }

    public void fireButton(View view) {
        final boolean hose = pantsDown(0);
        final boolean on_fire = onFire(0);

        if(on_fire){
            fire_index = 0;
            Log.d("fireButton", "Niiiice");
        } else if(hose){
            hose_index = 0;
            Log.d("hose", "Niiiice");
        } else {
            forced_lost_index = 0;
            Log.d("fireButton", "Du suuugsch");
        }
        game_over = true;
        //openGameOverDialog();
    }

    // Scoring functions
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
            col_points[curr_color] += card_id_to_value(curr_number);
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
        for(int i = 0; i < 3; ++i){
            final int hand_arr_ind = playerID * 3 + i;
            final int curr_hand_card_id = used_cards.get(hand_arr_ind);
            final int curr_color = curr_hand_card_id / 9;
            final boolean right_col = curr_color == color;
            final boolean ace = curr_hand_card_id % 9 == 0;
            final boolean image = curr_hand_card_id % 9 > 4;
            if(!right_col || (!ace && !image)) {
                return false;
            }
        }
        return true;
    }

    public boolean trapOnTable(){
        if(check30andHalf(4)){
            return true;
        } else {
            return oneColor(4);
        }
    }

    // Helper functions
    public int card_id_to_value(int card_id){
        if(card_id == 0){
            return 11;
        } else if(card_id > 4){
            return 10;
        } else {
            return card_id + 5;
        }
    }

    public ArrayList<Integer> getRandPerm(int len){
        ArrayList<Integer> arr = new ArrayList<>(len);
        for(int i = 0; i < len; ++i){
            arr.add(i);
        }
        java.util.Collections.shuffle(arr);
        return arr;
    }

    // Image view helper functions
    public void setHidden(){
        Bitmap card_bmp = getCard(54, 0);
        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        card_bmp = Bitmap.createBitmap(card_bmp, 0, 0, card_bmp.getWidth(), card_bmp.getHeight(), matrix, true);
        tc_views[3].setImageBitmap(card_bmp);
    }

    public void setPlayerCard(int p_ind, int card_id){
        Bitmap card_bmp = getCard(card_id, 4);
        p_views[p_ind].setImageBitmap(card_bmp);
    }

    public void setPlayerBG(int p_ind){
        Bitmap card_bmp = getCard(54, 0);
        p_views[p_ind].setImageBitmap(card_bmp);
    }

    public void setPlayerBGImages(){
        Bitmap card_bmp = getCard(54, 0);
        for(int i = 0; i < 3; ++i){
            p_views[i].setImageBitmap(card_bmp);
        }
    }

    public void setCard(int card_id, int view_array_id, boolean hand){
        Bitmap card_bmp = getCard(card_id);
        if(view_array_id == 3){
            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            card_bmp = Bitmap.createBitmap(card_bmp, 0, 0, card_bmp.getWidth(), card_bmp.getHeight(), matrix, true);
        }
        if(hand){
            hc_views[view_array_id].setImageBitmap(card_bmp);
        } else {
            tc_views[view_array_id].setImageBitmap(card_bmp);
        }
    }

    public Bitmap getCard(int ind, int offset){
        Bitmap mBitmap = cards;
        final int cs_per_col = 13 - offset;
        int h = mBitmap.getHeight();
        int w = mBitmap.getWidth();
        int h_card = h / 5;
        int w_card = w / 13;
        int row = (ind / cs_per_col) % 5;
        int col_h = ind % cs_per_col;
        int col = col_h == 0 ? 0 : offset + ind % cs_per_col;
        mBitmap = Bitmap.createBitmap(mBitmap, col * w_card, row * h_card, w_card, h_card);
        return mBitmap;
    }
    public Bitmap getCard(int ind){
        return getCard(ind, 4);
    }

    private static Bitmap getBitmap(VectorDrawable vectorDrawable, int h, int w) {
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        vectorDrawable.draw(canvas);
        Log.d("Hoi", "getBitmap: 1");
        return bitmap;
    }
}
