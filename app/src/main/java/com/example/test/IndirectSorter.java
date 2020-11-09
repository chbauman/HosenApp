package com.example.test;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Comparator;

/**
 * Sorts an array and keeps track of the indices.
 * <p>
 * From SO: https://stackoverflow.com/questions/23587314/how-to-sort-an-array-and-keep-track-of-the-index-in-java?noredirect=1&lq=1
 *
 * @param <T> The array element type.
 */
class IndirectSorter<T extends Comparable<T>> {
    /**
     * Computes the sorting indices.
     * <p>
     * Does not actually sort the array.
     *
     * @param args Input array.
     * @return The indices of how to sort the given array.
     */
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

    static class IndirectCompareClass<T2 extends Comparable<T2>> implements Comparator<Integer> {
        T2[] args;

        IndirectCompareClass(T2[] args) {
            this.args = args;
        }

        public int compare(@NotNull Integer in1, @NotNull Integer in2) {
            return args[in1].compareTo(args[in2]);
        }
    }
}