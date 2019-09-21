package com.example.test;

public class Move {

    int h_index;
    int t_index;
    boolean knock;
    boolean take_all;

    Move(boolean knock, boolean take_all, int h_index, int t_index){
        this.h_index = h_index;
        this.t_index = t_index;
        this.knock = knock;
        this.take_all = take_all;
    }
}
