package com.example.test;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MyAdapter extends ArrayAdapter<String> {

    private LayoutInflater layoutInflater;
    private Bundle b;
    private Context c;
    Cards cards;

    MyAdapter(Context c, String[] dummy, Bundle b) {
        super(c, R.layout.list_row, R.id.list_title_id, dummy);
        layoutInflater = LayoutInflater.from(c);
        this.b = b;
        this.c = c;
        cards = new Cards(c);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        View row = layoutInflater.inflate(R.layout.list_row, null);

        // Extract data from bundle
        float[] scores = b.getFloatArray("player_scores");
        String[] titleArr = b.getStringArray("player_names");
        int[] ids = b.getIntArray("player_ids");
        int[] cards_ids = b.getIntArray("cards");
        int n_players = b.getInt("n_players");

        // Find Views
        TextView title = row.findViewById(R.id.list_title_id);
        TextView sub_title = row.findViewById(R.id.list_subtitle_id);

        ImageView[] cardViews = new ImageView[3];
        cardViews[0] = row.findViewById(R.id.fc1);
        cardViews[1] = row.findViewById(R.id.fc2);
        cardViews[2] = row.findViewById(R.id.fc3);

        // Set cards
        for(int i = 0; i < 3; ++i){
            int card_ind = ids[position] * 3 + i;
            Bitmap card_bmp = cards.getCard(cards_ids[card_ind]);
            cardViews[i].setImageBitmap(card_bmp);
        }

        // Set name of player and score
        if(titleArr != null && scores != null){
            title.setText(titleArr[position]);
            float curr_score = scores[position];
            if(curr_score == 0.0f){
                sub_title.setText(c.getResources().getString(R.string.lost));
            } else if(curr_score == 32.0f){
                sub_title.setText(c.getResources().getString(R.string.fire));
            } else {
                sub_title.setText(c.getResources().getString(R.string.score_str, curr_score));
            }

        } else {
            throw new IllegalStateException("Pass the right fucking arrays!");
        }
        return row;
    }
}