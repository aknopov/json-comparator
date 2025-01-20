package com.aknopov.jsoncompare.diff;

/**
 * One editing operation between two sequences
 *
 * @param e related element
 * @param t editing type
 * @param aIdx index in `A` sequence
 * @param bIdx index in `B` sequence
 * @param <T> type of elements
 */
public record Diff<T>(T e, DiffType t, int aIdx, int bIdx)
{
    /**
     * Convenience factory method for creating class instance
     *
     * @param e related element
     * @param t editing type
     * @param aIdx index in `A` sequence
     * @param bIdx index in `B` sequence
     * @return created instance
     * @param <T> type of elements
     */
    static <T> Diff<T> of(T e, DiffType t, int aIdx, int bIdx)
    {
        return new Diff<>(e, t, aIdx, bIdx);
    }
}
