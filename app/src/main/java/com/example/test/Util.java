package com.example.test;

import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

// Helper class containing utility functions
class Util {

    // Returns a random permutaion of length 'len'.
    static ArrayList<Integer> getRandPerm(int len){
        ArrayList<Integer> arr = new ArrayList<>(len);
        for(int i = 0; i < len; ++i){
            arr.add(i);
        }
        java.util.Collections.shuffle(arr);
        return arr;
    }

    static TranslateAnimation getLinearAnim(@NotNull ImageView v1, @NotNull ImageView v2, int animTime){

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

}
