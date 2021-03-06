package com.example.test;

import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/*
 * TODO:
 *  - Ziehete falls unentschiede.
 *  - Enable Hosenabe button if there is a change resulting in hose
 *  - Meh Ziit zum Hose aa säge
 *  - Fix more warnings!
 *  - Horizontal view, adjust card placement.
 *  - Save statistics locally (number of wins and total games).
 *  - Better AI opponents :P
 *  - Publish on Play Store
 *  - One handler only to handle animations
 *  - Add (unit) tests
 *  - Make playable with drag and drop
 *
 * Done:
 *  - Fixed Animations.
 *  - Fix landscape mode.
 * */

/**
 * The type Main activity.
 */
public class MainActivity extends AppCompatActivity implements GameOverDialog.GODialogListener {

    /**
     * The total number of used cards.
     */
    int n_used_cards;
    /**
     * The N play cs.
     */
    int n_play_cs;
    /**
     * The number of players.
     */
    final int n_players = 4;
    /**
     * The total number of cards in the deck.
     */
    final int n_cards = 36;
    /**
     * The base animation time.
     */
    final int baseAnimTime = 1000;
    /**
     * The player names.
     */
    final String[] ps = {"Ich", "Hansli", "Peter", "Ruedi"};

    /**
     * Cards that are in the game.
     * <p>
     * First 3: your cards
     * next 3 * (n_players - 1): Other players cards
     * last 4: Three open and one hidden card (last)
     */
    ArrayList<Integer> used_cards;

    /**
     * The Cards.
     */
    Cards cards;
    /**
     * The Game plant.
     */
    GamePlant game;

    /**
     * The index of the selected hand card.
     * <p>
     * -1 means no card selected.
     */
    int sel_hand_c = -1;
    /**
     * The id of the starting player.
     */
    int starting_player = 0;
    /**
     * The Declared.
     */
    boolean declared = false;
    /**
     * The Your turn.
     */
    boolean your_turn = false;

    /**
     * The Chlopfed.
     */
    int chlopfed = -100;
    /**
     * The Turn ind.
     */
    int turn_ind = 0;

    /**
     * The Game over.
     */
    boolean game_over = false;

    /**
     * Who's turn is it.
     * <p>
     * 0: You
     * 1, 2, 3: Player 1, 2, 3
     */
    int turn = 0;

    /**
     * The array containing the Views of the hand cards.
     */
    ImageView[] hc_views;

    /**
     * The Array containing the Views of the table cards.
     */
    ImageView[] tc_views;

    /**
     * Array containing the Views of the other players.
     */
    ImageView[] p_views;

    /**
     * The Added texts views.
     */
    List<TextView> addedTextsViews = new ArrayList<>();

    // Same as below
    public void startNew() {
        startNewGame();
    }

    /**
     * Reset and start new game.
     */
    public void startNewGame() {

        // Set state variables
        sel_hand_c = -1;
        chlopfed = -100;
        turn_ind = 0;
        game_over = false;

        turn = 0;

        // Remove texts
        Iterator<TextView> itr = addedTextsViews.iterator();
        while (itr.hasNext()) {
            TextView v = itr.next();
            ViewGroup vg = (ViewGroup) (v.getParent());
            vg.removeView(v);
            itr.remove();
        }

        // Shuffle deck
        List<Integer> rp = Util.getRandPerm(n_cards).subList(0, n_used_cards);
        used_cards = new ArrayList<>(rp);

        // Start new game
        game.startGame();
        used_cards = game.used_cards;
        starting_player = game.beginner_ind;
        Log.d("beginning player: ", "" + starting_player);

        setCardsAsDeck(false);

        if (starting_player == 0) {
            // Wait for declaration
            final Handler handler = new Handler();
            int dec_delay = 2000;
            handler.postDelayed(new Runnable() {
                public void run() {
                    // first move (switching cards)
                    changeCardsDialog();
                }
            }, dec_delay);
        } else {
            // Swap or not
            int base_delay = 500;
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    setTextViewBelowImgView(p_views[starting_player - 1], R.string.starting_player, true);
                }
            }, 200);
            handler.postDelayed(new Runnable() {
                public void run() {
                    removeFirstText();
                }
            }, 5000);
            boolean switch_stack = game.chooseStackAI(starting_player);
            if (switch_stack) {
                int dec_delay = baseAnimTime + 200 + base_delay;
                int half_anim_delay = baseAnimTime / 2 + 100 + base_delay;
                handler.postDelayed(new Runnable() {
                    public void run() {
                        takeAllAnim(starting_player);
                    }
                }, base_delay);
                handler.postDelayed(new Runnable() {
                    public void run() {
                        setCardsAsDeck(true);
                    }
                }, half_anim_delay);
                handler.postDelayed(new Runnable() {
                    public void run() {
                        moveNext(false, starting_player);
                    }
                }, dec_delay);
            } else {
                setCardsAsDeck(true);
                moveNext(false, starting_player);
            }
        }
    }

    /**
     * Sets cards as deck.
     *
     * @param open_table the open table
     */
    public void setCardsAsDeck(boolean open_table) {
        // Sets the card images as they are in the deck.
        setHidden();
        setPlayerBGImages();
        for (int i = 0; i < 3; ++i) {
            int hand_cd_id = used_cards.get(i);
            int table_cd_id = used_cards.get(n_play_cs + i);
            setCard(hand_cd_id, i, true);
            if (open_table) {
                setCard(table_cd_id, i, false);
            }
        }
        if (!open_table) {
            setTableCardsToBG();
        }
    }

    /**
     * Dialog that asks if player wants to swap or keep initial cards.
     */
    public void changeCardsDialog() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final Handler handler = new Handler();
                int base_delay = 500;
                int dec_delay = baseAnimTime + 200 + base_delay;
                int half_anim_delay = baseAnimTime / 2 + 100 + base_delay;
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        game.choose_stack(true);
                        handler.postDelayed(new Runnable() {
                            public void run() {
                                takeAllAnim(0);
                            }
                        }, base_delay);
                        handler.postDelayed(new Runnable() {
                            public void run() {
                                setCardsAsDeck(true);
                            }
                        }, half_anim_delay);
                        handler.postDelayed(new Runnable() {
                            public void run() {
                                moveNext(false, 0);
                            }
                        }, dec_delay);
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        game.choose_stack(false);
                        setCardsAsDeck(true);
                        moveNext(false, 0);
                        break;
                }
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.change_cards)).setPositiveButton(getString(R.string.yes), dialogClickListener)
                .setNegativeButton(getString(R.string.no), dialogClickListener);
        builder.setCancelable(false);
        AlertDialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }

    // Creates and finds card views and sets listeners
    @TargetApi(21)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Load the cards
        cards = new Cards(this);

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
        game = new GamePlant(n_players);

        n_play_cs = n_players * 3;
        n_used_cards = n_play_cs + 4;

        startNewGame();

        // Set onTouch
        for (int i = 0; i < 3; ++i) {
            final int i_final = i;

            // For hand cards
            ImageView img = hc_views[i];
            img.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View view) {
                    sel_hand_c = i_final;
                }
            });
        }
        for (int i = 0; i < 4; ++i) {
            final int i_final = i;

            // For table cards
            ImageView img = tc_views[i];
            img.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View view) {

                    if (your_turn) {

                        if (sel_hand_c != -1 && !game_over) {
                            your_turn = false;

                            // Make move
                            game.make_move(false, false, sel_hand_c, i_final);
                            int hand_ind = sel_hand_c;
                            takeCardWithAnim(0, i_final, hand_ind);
                            moveNext(true, 0);
                        }
                    }
                }
            });
        }
    }


    /**
     * Move next.
     *
     * @param declare_first the declare first
     * @param player_ind    the player ind
     */
    public void moveNext(boolean declare_first, int player_ind) {

        final Handler handler = new Handler();
        int dec_delay = 800;

        if (declare_first && player_ind == 0) {
            // Wait for declaration
            if (pantsDown(0) || onFire(0)) {
                handler.postDelayed(new Runnable() {
                    public void run() {
                        GameState gs = game.declare(declared);
                        if (gs.game_over) {
                            game_over = true;
                            openGameOverDialog();
                        }
                    }
                }, dec_delay);
            } else {
                dec_delay = 0;
                GameState gs = game.declare(false);
                if (gs.game_over) {
                    game_over = true;
                    openGameOverDialog();
                    return;
                }
            }
        }

        // Move other players
        for (int i = player_ind; i < n_players - 1; ++i) {
            final int fin_i = i;
            final int dec_i = i - player_ind - (player_ind > 0 ? 1 : 0);
            handler.postDelayed(new Runnable() {
                public void run() {
                    if (!game_over) {
                        GameState gs = game.findMoveAdvanced(fin_i + 1);
                        if (gs.lastMove.knock) {
                            knock(fin_i + 1);
                        } else if (gs.lastMove.take_all) {
                            final Handler handler = new Handler();
                            int dec_delay = baseAnimTime / 2 + 100;
                            takeAllAnim(fin_i + 1);
                            handler.postDelayed(new Runnable() {
                                public void run() {
                                    setCardsAsDeck(true);
                                }
                            }, dec_delay);
                        } else {
                            takeCardWithAnim(fin_i + 1, gs.lastMove.t_index, gs.lastMove.h_index);
                        }
                        if (gs.game_over) {
                            game_over = true;
                            openGameOverDialog();
                            return;
                        }
                    }
                    if (fin_i == n_players - 2) {
                        your_turn = true;
                    }
                }
            }, (dec_i + 1) * 1000 + dec_delay);
        }
        if (player_ind == n_players - 1) {
            your_turn = true;
        }
    }

    /**
     * Shows final message and then the game over dialog with the scores.
     */
    public void openGameOverDialog() {
        GameOverState gos = game.get_game_over_state();
        if (gos.id_pants >= 0) {
            pantsDownAnim(gos.id_pants);
        } else if (gos.id_fired >= 0) {
            fireAnim(gos.id_fired);
        }
        final Handler handler = new Handler();
        int dec_delay = 1300;
        handler.postDelayed(new Runnable() {
            public void run() {
                // first move (switching cards)
                openGameOverDialogNonDelayed();
            }
        }, dec_delay);
    }

    /**
     * Computes the scores and creates and opens the score dialog.
     */
    public void openGameOverDialogNonDelayed() {

        Log.d("openGame", "called");

        // Compute points of all players
        GameOverState gos = game.get_game_over_state();
        Float[] scores = gos.scores;
        int wrong_dec = gos.id_wrong_declared;
        int firer = gos.id_fired;

        // Fire
        if (firer >= 0) {
            for (int i = 0; i < n_players; ++i) {
                if (i != firer) {
                    scores[i] = 0.0f;
                }
            }
        } else if (gos.n_turns > n_players) {
            // Trap or wrongly announced hose or fireButton
            if (wrong_dec >= 0) {
                scores[wrong_dec] = 0.0f;
            }
            if (gos.id_caught_in_trap >= 0) {
                scores[gos.id_caught_in_trap] = 0.0f;
            }
            // Unannounced hose or fireButton
            for (int i = 0; i < n_players; ++i) {
                if (i != gos.id_pants && (pantsDown(i) || onFire(i))) {
                    Log.d("unannounced", ps[i]);
                    scores[i] = 0.0f;
                }
            }
        }

        // Put data for dialog into bundle
        GameOverDialog goDialog = new GameOverDialog();
        Bundle game_over_data = new Bundle();

        // Sort and keep indices
        int[] indices = new IndirectSorter<Float>().sort(scores);
        float[] scores_cp = new float[n_players];
        String[] players_cp = new String[n_players];
        int[] playerIds = new int[n_players];

        // Scores, Player Ids and Names
        for (int i = 0; i < n_players; ++i) {
            final int curr_ind = indices[i];
            scores_cp[n_players - 1 - i] = scores[curr_ind];
            players_cp[n_players - 1 - i] = ps[curr_ind];
            playerIds[n_players - 1 - i] = curr_ind;
        }
        game_over_data.putStringArray("player_names", players_cp);
        game_over_data.putFloatArray("player_scores", scores_cp);
        game_over_data.putIntArray("player_ids", playerIds);
        game_over_data.putInt("n_players", n_players);

        // Add used cards
        int[] cards = new int[n_players * 3];
        for (int i = 0; i < n_players * 3; ++i) {
            cards[i] = used_cards.get(i);
        }
        game_over_data.putIntArray("cards", cards);

        // Add bundle and show
        goDialog.setArguments(game_over_data);
        goDialog.show(getSupportFragmentManager(), "GameOverDialog");
    }

    /**
     * Animation of taking a card.
     *
     * @param playerId    the player id
     * @param table_index the table index
     * @param hand_index  the hand index
     */
    public void takeCardWithAnim(int playerId, int table_index, int hand_index) {

        Log.d(ps[playerId], "taking card " + table_index);

        final int t_arr_ind = n_play_cs + table_index;
        final int h_arr_ind = playerId * 3 + hand_index;

        // Get cards
        final int hand_c_id = used_cards.get(t_arr_ind);
        final int table_c_id = used_cards.get(h_arr_ind);

        final int animTime = baseAnimTime / 2;
        ImageView pCardView = playerId == 0 ? hc_views[hand_index] : p_views[playerId - 1];
        ImageView destView = tc_views[table_index];

        // Uncover player card if covered
        if (table_index != 3 && playerId != 0) {
            setPlayerCard(playerId - 1, hand_c_id);
        }

        // Animate
        Animation anim = Util.getLinearAnim(pCardView, destView, animTime, true);
        pCardView.startAnimation(anim);

        final int p_fin = playerId - 1;
        final int t_ind = table_index;
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                // Set table card
                if (t_ind != 3) {
                    setCard(hand_c_id, t_ind, false);
                }
                if (p_fin >= 0) {
                    // Cover player card
                    setPlayerBG(p_fin);
                } else {
                    // Set hand card
                    setCard(table_c_id, h_arr_ind, true);
                }
            }
        }, animTime + 100);
    }

    /**
     * Player with specified player id knocks.
     *
     * @param playerId the player id
     */
    public void knock(int playerId) {
        ImageView player_card_view = playerId == 0 ? hc_views[1] : p_views[playerId - 1];
        setTextViewBelowImgView(player_card_view, R.string.knocker, true);
    }

    /**
     * Pants down anim.
     *
     * @param playerId the player id
     */
    public void pantsDownAnim(int playerId) {
        ImageView player_card_view = playerId == 0 ? hc_views[1] : p_views[playerId - 1];
        setTextViewBelowImgView(player_card_view, R.string.pants_down, false);
    }

    /**
     * Fire animation.
     *
     * @param playerId The ID of the player that called fire.
     */
    public void fireAnim(int playerId) {
        ImageView player_card_view = playerId == 0 ? hc_views[1] : p_views[playerId - 1];
        setTextViewBelowImgView(player_card_view, R.string.fire, false);
    }

    /**
     * Animation of taking all cards.
     *
     * @param playerId The ID of the player taking the cards.
     */
    public void takeAllAnim(int playerId) {
        ImageView[] tempIvs = new ImageView[3];
        int hth = 0;
        int wth = 0;
        int top_dist = 0;
        int left_dist = 0;

        // Create two more image views in the case of opponents
        if (playerId != 0) {
            ImageView p_v = p_views[playerId - 1];
            tempIvs[2] = p_v;

            RelativeLayout rl = findViewById(R.id.main_layout);
            top_dist = p_v.getTop();
            left_dist = p_v.getLeft();
            hth = p_v.getHeight();
            wth = p_v.getWidth();
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(wth, hth);
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
            params.leftMargin = left_dist;
            params.topMargin = top_dist;

            Bitmap card_bmp = cards.getCardBackground();
            for (int i = 0; i < 2; ++i) {
                // Add ImageView
                tempIvs[i] = new ImageView(this);
                tempIvs[i].setImageBitmap(card_bmp);
                rl.addView(tempIvs[i], params);
            }
        }

        // Create and start the animation of all three cards
        for (int i = 0; i < 3; ++i) {
            ImageView iv1 = playerId == 0 ? hc_views[i] : tempIvs[i];
            ImageView iv2 = tc_views[i];
            Animation anim;
            if (playerId == 0) {
                anim = Util.getLinearAnim(iv1, iv2, baseAnimTime / 2, true);
            } else {
                anim = Util.getLinearAnimFromParams(iv2, baseAnimTime / 2, true, (float) hth, (float) wth, (float) left_dist, (float) top_dist);
            }
            iv1.startAnimation(anim);
        }

        // Remove Image views again
        if (playerId > 0) {
            final Handler handler = new Handler();
            int dec_delay = baseAnimTime + 150;
            final ImageView iv1 = tempIvs[0];
            final ImageView iv2 = tempIvs[1];
            handler.postDelayed(new Runnable() {
                public void run() {
                    ViewGroup vg = (ViewGroup) (iv2.getParent());
                    vg.removeView(iv1);
                    vg.removeView(iv2);
                }
            }, dec_delay);
        }
    }

    /**
     * Knock button method.
     *
     * @param view the view
     */
    public void knockButton(View view) {
        if (game.turn_ind > n_players) {
            your_turn = false;
            knock(0);
            game.make_move(true, false, -1, -1);
            moveNext(true, 0);
        }
    }

    /**
     * Take all cards button method.
     *
     * @param view the view
     */
    public void takeAllButton(View view) {
        if (your_turn) {
            your_turn = false;
            game.make_move(false, true, -1, -1);
            final Handler handler = new Handler();
            int dec_delay = baseAnimTime / 2 + 100;
            takeAllAnim(0);
            handler.postDelayed(new Runnable() {
                public void run() {
                    setCardsAsDeck(true);
                }
            }, dec_delay);
            moveNext(true, 0);
        }
    }

    /**
     * Fire button.
     *
     * @param view the view
     */
    public void fireButton(View view) {
        declared = true;
    }

    /**
     * On fire boolean.
     *
     * @param playerID the player id
     * @return the boolean
     */
    public boolean onFire(int playerID) {
        for (int i = 0; i < 3; ++i) {
            final int hand_arr_ind = playerID * 3 + i;
            final int curr_hand_card_id = used_cards.get(hand_arr_ind);
            if (curr_hand_card_id % 9 != 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if player has pants down.
     *
     * @param playerID The id of the player.
     * @return True if player has pants down, false else.
     */
    public boolean pantsDown(int playerID) {
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
     * Sets a TextView below an image view and enlarges it.
     *
     * @param iv          The ImageView under which to place the text.
     * @param s           String id of the text to put.
     * @param add_bw_anim Adds the backward animation to make it small again.
     */
    public void setTextViewBelowImgView(ImageView iv, int s, boolean add_bw_anim) {

        RelativeLayout rl = findViewById(R.id.main_layout);
        rl.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        int l_w = rl.getWidth();
        int l_h = rl.getHeight();

        int top_dist = iv.getTop();
        int left_dist = iv.getLeft();
        top_dist += iv.getLayoutParams().height;

        // Add TextView
        TextView tv = new TextView(this);
        tv.setText(s);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        params.leftMargin = left_dist;
        params.topMargin = top_dist;
        rl.addView(tv, params);
        addedTextsViews.add(tv);

        // Get Text params
        tv.measure(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        int t_height = tv.getMeasuredHeight();
        int t_width = tv.getMeasuredWidth();

        // Define animation params
        Interpolator ip = new LinearInterpolator();
        int animDuration = 600;
        int animPauseLength = 100;

        float scale_fac = ((float) l_w) / t_width;
        float dy = (float) (l_h - t_height) / 2 - top_dist;
        float dx = (float) -left_dist;
        Log.d("scaling factor: ", "" + scale_fac);

        // Scaling
        float reciprocal_sf = 1.0f / scale_fac;
        ScaleAnimation scAnim = new ScaleAnimation(1.0f, scale_fac, 1.0f, scale_fac);
        ScaleAnimation scAnimBW = new ScaleAnimation(1.0f, reciprocal_sf, 1.0f, reciprocal_sf);
        scAnimBW.setStartOffset(animDuration + animPauseLength);

        // Translation
        TranslateAnimation anim = new TranslateAnimation(0.0f, dx, 0.0f, dy);
        TranslateAnimation animBW = new TranslateAnimation(0.0f, -dx, 0.0f, -dy);
        animBW.setStartOffset(animDuration + animPauseLength);

        // Put them together
        AnimationSet animSet = new AnimationSet(true);
        animSet.setFillAfter(true);
        animSet.setDuration(animDuration);
        animSet.setInterpolator(ip);

        // Add to set
        animSet.addAnimation(scAnim);
        animSet.addAnimation(anim);
        if (add_bw_anim) {
            animSet.addAnimation(animBW);
            animSet.addAnimation(scAnimBW);
        }

        // Start
        tv.startAnimation(animSet);
    }

    /**
     * Remove first text.
     */
    public void removeFirstText() {
        if (addedTextsViews.size() > 0) {
            TextView iv = addedTextsViews.get(0);
            ViewGroup vg = (ViewGroup) (iv.getParent());
            vg.removeView(iv);
            addedTextsViews.remove(0);
        }
    }

    /**
     * Sets the image of the hidden card.
     */
    public void setHidden() {
        Bitmap card_bmp = cards.getCardBackground();
        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        card_bmp = Bitmap.createBitmap(card_bmp, 0, 0, card_bmp.getWidth(), card_bmp.getHeight(), matrix, true);
        tc_views[3].setImageBitmap(card_bmp);
    }

    /**
     * Sets a players card.
     *
     * @param p_ind   the p ind
     * @param card_id the card id
     */
    public void setPlayerCard(int p_ind, int card_id) {
        Bitmap card_bmp = cards.getCard(card_id);
        p_views[p_ind].setImageBitmap(card_bmp);
    }

    /**
     * Set player background image.
     *
     * @param p_ind Index of player.
     */
    public void setPlayerBG(int p_ind) {
        Bitmap card_bmp = cards.getCardBackground();
        p_views[p_ind].setImageBitmap(card_bmp);
    }

    /**
     * Sets table cards to bg.
     */
    public void setTableCardsToBG() {
        Bitmap card_bmp = cards.getCardBackground();
        for (int i = 0; i < 3; ++i) {
            tc_views[i].setImageBitmap(card_bmp);
        }
    }

    /**
     * Sets player bg images.
     */
    public void setPlayerBGImages() {
        Bitmap card_bmp = cards.getCardBackground();
        for (int i = 0; i < 3; ++i) {
            p_views[i].setImageBitmap(card_bmp);
        }
    }

    /**
     * Sets card.
     *
     * @param card_id       the card id
     * @param view_array_id the view array id
     * @param hand          the hand
     */
    public void setCard(int card_id, int view_array_id, boolean hand) {
        Bitmap card_bmp = cards.getCard(card_id);
        if (view_array_id == 3) {
            card_bmp = Cards.rotateBitmap(card_bmp);
        }
        ImageView[] v_arr = hand ? hc_views : tc_views;
        v_arr[view_array_id].setImageBitmap(card_bmp);
    }
}
