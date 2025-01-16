package com.aknopov.jsoncompare.diff;

/**
 * One editing operation between two sequences
 *
 * @param <T> type of comparable elements
 */

/**
 * One editing operation between two sequences
 *
 * @param e related element
 * @param t editing type
 * @param aIdx index in `A` sequence
 * @param bIdx index in `B` sequence
 * @param <T> type of comparable elements
 */
record Diff<T>(T e, DiffType t, int aIdx, int bIdx)
{
    static <T> Diff<T> of(T e, DiffType t, int aIdx, int bIdx)
    {
        return new Diff<>(e, t, aIdx, bIdx);
    }
}
