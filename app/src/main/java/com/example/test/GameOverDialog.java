package com.example.test;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;

/**
 * The dialog that pops-up when the game is over.
 */
public class GameOverDialog extends DialogFragment {

    private GODialogListener listener;

    /**
     * Constructor.
     */
    public GameOverDialog() {
        super();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        listener = (GODialogListener) context;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        // Get Arguments
        Bundle b = getArguments();
        String[] titleArr;
        if (b != null) {
            titleArr = b.getStringArray("player_names");
        } else {
            throw new IllegalStateException("Where is the fucking bundle?");
        }

        // Inflate
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        Activity currentActivity = getActivity();
        LayoutInflater inflater;
        if (currentActivity != null) {
            inflater = currentActivity.getLayoutInflater();
        } else {
            throw new IllegalStateException("Where is the fucking activity of this dialog?");
        }
        View view = inflater.inflate(R.layout.layout_dialog, null);


        ListView lv = view.findViewById(R.id.list_view);
        MyAdapter clad = new MyAdapter(getActivity(), titleArr, b);
        lv.setAdapter(clad);

        builder.setView(view)
                .setTitle(getString(R.string.game_over))
                .setPositiveButton(getString(R.string.new_game), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d("new", "game");
                        listener.startNew();
                    }
                });
        return builder.create();
    }

    public interface GODialogListener {
        void startNew();
    }
}
