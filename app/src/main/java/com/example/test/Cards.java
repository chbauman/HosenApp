package com.example.test;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;


/**
 * Class holding the image with all playing cards.
 */
public class Cards {

    /**
     * Bitmap of all cards.
     */
    private final Bitmap cards;

    /**
     * Constructor.
     *
     * @param c The current context.
     */
    @TargetApi(21)
    Cards(@NotNull Context c) {
        Resources res = c.getResources();
        Drawable drawable = res.getDrawable(R.drawable.ic_card_deck_161536, c.getTheme());
        VectorDrawable vectorDrawable = (VectorDrawable) drawable;
        cards = getBitmap(vectorDrawable, 615, 1027);
    }

    /**
     * Returns the front image of a playing card.
     *
     * @param ind The index of the card.
     * @return The Bitmap of the requested card.
     */
    Bitmap getCard(int ind) {
        return getCard(ind, 4);
    }

    /**
     * Rotates the given bitmap 90 degrees.
     *
     * @param b The Bitmap to rotate.
     * @return The rotated bitmap.
     */
    static Bitmap rotateBitmap(Bitmap b) {
        Matrix matrix = new Matrix();
        return Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), matrix, true);
    }

    /**
     * Returns the backside image of a card.
     *
     * @return The Bitmap of the backside.
     */
    Bitmap getCardBackground() {
        return getCard(54, 0);
    }

    /**
     * Computes the value of the card with a specific index.
     *
     * @param card_id The index of the card.
     * @return Value of the card.
     */
    @Contract(pure = true)
    static int card_id_to_value(int card_id) {
        if (card_id == 0) {
            return 11;
        } else if (card_id > 4) return 10;
        else {
            return card_id + 5;
        }
    }

    // Private methods
    private Bitmap getCard(int ind, int offset) {
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

    private Bitmap getBitmap(@NotNull VectorDrawable vectorDrawable, int h, int w) {
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        vectorDrawable.draw(canvas);
        return bitmap;
    }
}