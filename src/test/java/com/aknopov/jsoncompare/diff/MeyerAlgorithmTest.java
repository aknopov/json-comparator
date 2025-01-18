package com.aknopov.jsoncompare.diff;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.*;
import static com.aknopov.jsoncompare.diff.DiffType.DELETE;
import static com.aknopov.jsoncompare.diff.DiffType.ADD;

class MeyerAlgorithmTest
{
    @Test
    void testSerialization()
    {
        List<Character> a = List.of('a', 'b', 'c');
        List<Character> b = List.of('a', 'b', 'd');

        List<Diff<Character>> diffs = MeyerAlgorithm.compareSequences(a, b);
        String sDiff = MeyerAlgorithm.serializeDiffs(diffs);

        assertEquals("-c[2<->-1]\n+d[-1<->2]\n", sDiff);
    }

    private static Stream<Arguments> stringDiffsSupplier()
    {
        return Stream.of(
            Arguments.of("abc", "abd", List.of(Diff.of('c', DELETE, 2, -1), Diff.of('d', ADD, -1, 2))),
            Arguments.of("abcdef", "dacfea", List.of(
                    Diff.of('d', ADD, -1, 0),
                    Diff.of('b', DELETE, 1, -1),
                    Diff.of('d', DELETE, 3, -1),
                    Diff.of('e', DELETE, 4, -1),
                    Diff.of('e', ADD, -1, 4),
                    Diff.of('a', ADD, -1, 5))),
            Arguments.of("acbdeacbed", "acebdabbabed", List.of(
                    Diff.of('e', ADD, -1, 2),
                    Diff.of('e', DELETE, 4, -1),
                    Diff.of('c', DELETE, 6, -1),
                    Diff.of('b', ADD, -1, 7),
                    Diff.of('a', ADD, -1, 8),
                    Diff.of('b', ADD, -1, 9))),
            Arguments.of("acebdabbabed", "acbdeacbed", List.of(
                    Diff.of('e', DELETE, 2, -1),
                    Diff.of('e', ADD, -1, 4),
                    Diff.of('c', ADD, -1, 6),
                    Diff.of('b', DELETE, 7, -1),
                    Diff.of('a', DELETE, 8, -1),
                    Diff.of('b', DELETE, 9, -1))),
            Arguments.of("abcbda", "bdcaba", List.of(
                    Diff.of('a', DELETE, 0, -1),
                    Diff.of('d', ADD, -1, 1),
                    Diff.of('a', ADD, -1, 3),
                    Diff.of('d', DELETE, 4, -1))),
            Arguments.of("bokko", "bokkko", List.of(Diff.of('k', ADD, -1, 4))),
            Arguments.of("abcaaaaaabd", "abdaaaaaabc", List.of(
                    Diff.of('c', DELETE, 2, -1),
                    Diff.of('d', ADD, -1, 2),
                    Diff.of('d', DELETE, 10, -1),
                    Diff.of('c', ADD, -1, 10))),
            Arguments.of("", "", List.of()),
            Arguments.of("a", "", List.of(Diff.of('a', DELETE, 0, -1))),
            Arguments.of("", "b", List.of(Diff.of('b', ADD, -1, 0))),
            Arguments.of("Привет!", "Прювет!", List.of(
                    Diff.of('и', DELETE, 2, -1),
                    Diff.of('ю', ADD, -1, 2))),
            Arguments.of("ab", "ba", List.of(Diff.of('a', DELETE, 0, -1), Diff.of('a', ADD, -1, 1)))
        );
    }

    @ParameterizedTest
    @MethodSource("stringDiffsSupplier")
    void testStringDiffs(String a, String b, List<Diff<Character>> expectedDiffs)
    {
        List<Character> aChars = a.chars().mapToObj(c -> (char)c).toList();
        List<Character> bChars = b.chars().mapToObj(c -> (char)c).toList();

        List<Diff<Character>> actualDiffs = MeyerAlgorithm.compareSequences(aChars, bChars);

        assertEquals(expectedDiffs, actualDiffs);
    }

    private static Stream<Arguments> intDifssSupplier()
    {
        return Stream.of(
            Arguments.of(List.of(1, 2, 3, 4, 5, 6, 6, 6, 7, 8, 9), List.of(1, 2, 3, 4, 5, 0, 7, 8, 9), List.of(
                    Diff.of(6, DELETE, 5, -1),
                    Diff.of(6, DELETE, 6, -1),
                    Diff.of(6, DELETE, 7, -1),
                    Diff.of(0, ADD, -1, 5))),
            Arguments.of(List.of(1, 2, 3), List.of(1, 5, 3), List.of(
                    Diff.of(2, DELETE, 1, -1),
                    Diff.of(5, ADD, -1, 1))),
            Arguments.of(List.of(), List.of(), List.of())
        );
    }

    @ParameterizedTest
    @MethodSource("intDifssSupplier")
    void testIntegerDiffs(List<Integer> a, List<Integer> b, List<Diff<Integer>> expectedDiffs)
    {
        List<Diff<Integer>> actualDiffs = MeyerAlgorithm.compareSequences(a, b);

        assertEquals(expectedDiffs, actualDiffs);
    }

    @Test
    void testMaxDiffs()
    {
        List<Character> a = List.of('a', 'b', 'c', 'd');
        List<Character> b = List.of('d', 'c', 'b', 'a');

        List<Diff<Character>> diffs1 = MeyerAlgorithm.compareSequences(a, b);
        assertEquals(6, diffs1.size());

        List<Diff<Character>> diffs2 = MeyerAlgorithm.compareSequences(a, b, 1);
        assertEquals(2, diffs2.size());
    }
}