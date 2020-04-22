package com.example.test;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.widget.ImageView;


public class Cards {

    // Member variables
    private Bitmap cards;

    // Constructor
    @TargetApi(21)
    Cards(Context c) {
        Resources res = c.getResources();
        Drawable drawable = res.getDrawable(R.drawable.ic_card_deck_161536, c.getTheme());
        VectorDrawable vectorDrawable = (VectorDrawable) drawable;
        cards = getBitmap(vectorDrawable, 615, 1027);
    }

    // Public methods
    public void setImageView(ImageView iv, int ind) {
        Bitmap card_bmp = getCard(ind);
        iv.setImageBitmap(card_bmp);
    }

    Bitmap getCard(int ind) {
        return getCard(ind, 4);
    }

    Bitmap rotateBitmap(Bitmap b) {
        Matrix matrix = new Matrix();
        return Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), matrix, true);
    }

    Bitmap getCardBackground() {
        return getCard(54, 0);
    }

    // Helper functions
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

    private Bitmap getBitmap(VectorDrawable vectorDrawable, int h, int w) {
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        vectorDrawable.draw(canvas);
        return bitmap;
    }
}