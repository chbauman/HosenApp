package com.example.test;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class MyAdapter extends ArrayAdapter<String> {

    private LayoutInflater layoutInflater;
    private Bundle b;
    private Context c;

    MyAdapter(Context c, String[] dummy, Bundle b) {
        super(c, R.layout.list_row, R.id.list_title_id, dummy);
        layoutInflater = LayoutInflater.from(c);
        this.b = b;
        this.c = c;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        View row = layoutInflater.inflate(R.layout.list_row, null);

        float[] scores = b.getFloatArray("player_scores");
        String[] titleArr = b.getStringArray("player_names");
        TextView title = row.findViewById(R.id.list_title_id);
        TextView sub_title = row.findViewById(R.id.list_subtitle_id);
        if(titleArr != null && scores != null){
            title.setText(titleArr[position]);
            sub_title.setText(c.getResources().getString(R.string.score_str, scores[position]));
        } else {
            throw new IllegalStateException("Pass the right fucking arrays!");
        }
        return row;
    }
}