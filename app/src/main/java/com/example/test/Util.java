package com.example.test;

import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

/**
 * Helper class containing utility functions
 */
class Util {

    /**
     * Generates a random permutation.
     *
     * @param len Length of permutation.
     * @return The permutation.
     */
    static ArrayList<Integer> getRandPerm(int len) {
        ArrayList<Integer> arr = new ArrayList<>(len);
        for (int i = 0; i < len; ++i) {
            arr.add(i);
        }
        java.util.Collections.shuffle(arr);
        return arr;
    }

    /**
     * Returns an animation for a card movement.
     *
     * @param v2 ImageView of the card on the table.
     * @param animTime The animation time in milli seconds.
     * @param back Whether the animation shall be reversed after being executed.
     * @param vh1 The height of the ImageView of the card in the hand.
     * @param vw1 The width of the ImageView of the card in the hand.
     * @param x The x coordinate of the position of the ImageView of the card in the hand.
     * @param y The y coordinate of the position of the ImageView of the card in the hand.
     * @return The requested animation.
     */
    static Animation getLinearAnimFromParams(@NotNull ImageView v2, int animTime, boolean back, float vh1, float vw1, float x, float y) {
        // Read parameters
        final float vh2 = v2.getHeight();
        final float vw2 = v2.getWidth();
        final float centreX_player = x + vw1 / 2.0f;
        final float centreY_player = y + vh1 / 2.0f;
        final float centreX_dest = v2.getLeft() + vw2 / 2.0f;
        final float centreY_dest = v2.getTop() + vh2 / 2.0f;

        // Determine if a rotation and / or a scaling is needed
        final boolean rotate = vw2 > vh2;
        float scaling_fac = ((float) vh2) / vh1;
        boolean scale = !rotate && (vh1 > vh2);
        if(rotate && (vh1 > vw2)){
            scale = true;
            scaling_fac = ((float) vh2) / vw1;
        }

        // Compute offset
        final float dx = centreX_dest - centreX_player;
        final float dy = centreY_dest - centreY_player;

        // Put them together
        Interpolator ip = new LinearInterpolator();
        AnimationSet animSet = new AnimationSet(true);
        animSet.setFillAfter(true);
        animSet.setInterpolator(ip);

        if (rotate) {
            Animation rot90 = new RotateAnimation(0, 90, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            rot90.setDuration(animTime);
            animSet.addAnimation(rot90);
        }
        if (scale) {
            Animation scaleAnim = new ScaleAnimation(1.0f, scaling_fac, 1.0f, scaling_fac, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            scaleAnim.setDuration(animTime);
            animSet.addAnimation(scaleAnim);
        }
        TranslateAnimation anim = new TranslateAnimation(0.0f, dx, 0.0f, dy);
        anim.setDuration(animTime);
        animSet.addAnimation(anim);

        // Add reversed animations
        if (back) {
            if (rotate) {
                Animation rot90 = new RotateAnimation(0, -90, Animation.RELATIVE_TO_SELF, 0.5f + dx / vw1, Animation.RELATIVE_TO_SELF, 0.5f + dy / vh1);
                rot90.setStartOffset(animTime + 200);
                rot90.setDuration(animTime);
                animSet.addAnimation(rot90);
            }
            Animation anim_back = new TranslateAnimation(0.0f, -dx, 0.0f, -dy);
            anim_back.setStartOffset(animTime + 200);
            anim_back.setDuration(animTime);
            animSet.addAnimation(anim_back);
            if (scale) {
                Animation scaleAnim = new ScaleAnimation(1.0f, 1.0f / scaling_fac, 1.0f, 1.0f / scaling_fac, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                scaleAnim.setStartOffset(animTime + 200);
                scaleAnim.setDuration(animTime);
                animSet.addAnimation(scaleAnim);
            }
        }
        return animSet;
    }

    /**
     * Returns an animation for a card movement.
     *
     * @param v1 ImageView of the player's card.
     * @param v2 ImageView of the card on the table.
     * @param animTime The animation time in milli seconds.
     * @param back Whether the animation shall be reversed after being executed.
     * @return The requested animation.
     */
    static Animation getLinearAnim(@NotNull ImageView v1, @NotNull ImageView v2, int animTime, boolean back) {
        return getLinearAnimFromParams(v2, animTime, back, v1.getHeight(), v1.getWidth(), v1.getLeft(), v1.getTop());
    }
}
