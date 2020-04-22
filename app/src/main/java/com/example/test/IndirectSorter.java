package com.example.test;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Comparator;

// From SO: https://stackoverflow.com/questions/23587314/how-to-sort-an-array-and-keep-track-of-the-index-in-java?noredirect=1&lq=1
class IndirectSorter<T extends Comparable<T>> {
    int[] sort(@NotNull T[] args) {
        Integer[] OriginalIndices = new Integer[args.length];
        int[] ReturnValue = new int[args.length];
        for (int i = 0; i < OriginalIndices.length; i++) {
            OriginalIndices[i] = i;
        }
        Arrays.sort(OriginalIndices, new IndirectCompareClass<>(args));
        for (int i = 0; i < OriginalIndices.length; i++) ReturnValue[i] = OriginalIndices[i];
        return ReturnValue;
    }

    class IndirectCompareClass<T2 extends Comparable<T2>> implements Comparator<Integer> {
        T2[] args;

        IndirectCompareClass(T2[] args) {
            this.args = args;
        }

        public int compare(@NotNull Integer in1, @NotNull Integer in2) {
            return args[in1].compareTo(args[in2]);
        }

        public boolean equals(@NotNull Integer in1, @NotNull Integer in2) {
            return args[in1].equals(args[in2]);
        }
    }
}