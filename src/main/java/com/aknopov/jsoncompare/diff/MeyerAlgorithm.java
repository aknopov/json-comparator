package com.aknopov.jsoncompare.diff;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Implementation of O(NP) Myers' diff algorithm <a href='http://www.xmailserver.org/diff2.pdf'>http://www.xmailserver.org/diff2.pdf</a>
 *
 * @param <T> type of comparable elements
 */
public class MeyerAlgorithm<T>
{
    private final static int DEFAULT_MAX_DIFFS = 2000000;

    // `Coord` is a coordinate in edit graph
    private record Coord(int x, int y)
    {
    }

    // `Graph` is a coordinate in edit graph with attached route
    private record Graph(int x, int y, int r)
    {
    }

    private final List<T> a;
    private final List<T> b;
    private final int maxDiffs;
    private final boolean recordEquals;
    private final List<Diff<T>> diffs;
    private final boolean reverse;

    /**
     * Compares two sequences of any type and returns a list of differences.
     *
     * @param a the first sequence to compare
     * @param b the second sequence to compare
     * @param <T> the type of the elements in the sequences
     *
     * @return a list of differences between the two sequences.
     */
    static <T> List<Diff<T>> compareSequences(List<T> a, List<T> b)
    {
        return compareSequences(a, b, DEFAULT_MAX_DIFFS);
    }

    /**
     * Compares two sequences of any type and returns a list of differences.
     *
     * @param a the first sequence to compare
     * @param b the second sequence to compare
     * @param maxDiffs maximum number of edit graphs to analyse
     * @param <T> the type of the elements in the sequences
     *
     * @return a list of differences between the two sequences.
     */
    static <T> List<Diff<T>> compareSequences(List<T> a, List<T> b, int maxDiffs)
    {
        assert maxDiffs > 0;

        MeyerAlgorithm<T> algorithm;
        if (a.size() < b.size())
        {
            algorithm = new MeyerAlgorithm<>(a, b, maxDiffs, false, false);
        }
        else
        {
            algorithm = new MeyerAlgorithm<>(b, a, maxDiffs, true, false);
        }

        algorithm.doCompare();

        return algorithm.diffs;
    }

    /**
     * Creates string presentation of supplied differences that contains differences separated with LF character.
     *
     * @param diffs list of differences
     *
     * @return the string
     */
    static <T> String serializeDiffs(List<Diff<T>> diffs)
    {
        StringBuilder sb = new StringBuilder();
        for (Diff<?> diff : diffs)
        {
            sb.append(switch (diff.t())
            {
                case DELETE -> String.format("-%s[%d<->%d]\n", diff.e(), diff.aIdx(), diff.bIdx());
                case ADD -> String.format("+%s[%d<->%d]\n", diff.e(), diff.aIdx(), diff.bIdx());
                case SAME -> String.format("=%s[%d<->%d]\n", diff.e(), diff.aIdx(), diff.bIdx());
            });
        }

        return sb.toString();
    }

    private MeyerAlgorithm(List<T> a, List<T> b, int maxDiffs, boolean reverse, boolean recordEquals)
    {
        this.a = a;
        this.b = b;
        this.maxDiffs = maxDiffs;
        this.reverse = reverse;
        this.recordEquals = recordEquals;
        this.diffs = new ArrayList<>();
    }

    private void doCompare()
    {
        recordDiffs(compose());
    }

    private void recordDiffs(List<Coord> comparePoints)
    {
        int x = 1;
        int y = 1;
        int px = 0;
        int py = 0;
        for (int i = comparePoints.size() - 1; i >= 0; i--)
        {
            while (px < comparePoints.get(i).x || py < comparePoints.get(i).y)
            {
                if (comparePoints.get(i).y - comparePoints.get(i).x > py - px)
                {
                    if (this.reverse)
                    {
                        diffs.add(new Diff<>(this.b.get(py), DiffType.DELETE, y - 1, -1));
                    }
                    else
                    {
                        diffs.add(new Diff<>(this.b.get(py), DiffType.ADD, -1, y - 1));
                    }
                    y++;
                    py++;
                }
                else if (comparePoints.get(i).y - comparePoints.get(i).x < py - px)
                {
                    if (this.reverse)
                    {
                        diffs.add(new Diff<>(this.a.get(px), DiffType.ADD, -1, x - 1));
                    }
                    else
                    {
                        diffs.add(new Diff<>(this.a.get(px), DiffType.DELETE, x - 1, -1));
                    }
                    x++;
                    px++;
                }
                else
                {
                    if (this.recordEquals)
                    {
                        if (this.reverse)
                        {
                            diffs.add(new Diff<>(this.b.get(py), DiffType.SAME, y - 1, x - 1));
                        }
                        else
                        {
                            diffs.add(new Diff<>(this.a.get(px), DiffType.SAME, x - 1, y - 1));
                        }
                    }
                    x++;
                    y++;
                    px++;
                    py++;
                }
            }
        }
    }

    private List<Coord> compose()
    {
        int[] fp = new int[a.size() + b.size() + 3];
        int [] paths = new int[a.size() + b.size() + 3];
        List<Graph> graphs = new ArrayList<>();

        Arrays.fill(fp, -1);
        Arrays.fill(paths, -1);

        int offset = this.a.size() + 1;
        int delta = this.b.size() - this.a.size();
        for (int p = 0; ; p++)
        {
            for (int k = -p; k <= delta - 1; k++)
            {
                fp[k + offset] = snake(k, fp[k - 1 + offset] + 1, fp[k + 1 + offset], offset, paths, graphs);
            }
            for (int k = delta + p; k >= delta + 1; k--)
            {
                fp[k + offset] = snake(k, fp[k - 1 + offset] + 1, fp[k + 1 + offset], offset, paths, graphs);
            }

            fp[delta + offset] = snake(delta, fp[delta - 1 + offset] + 1, fp[delta + 1 + offset], offset, paths, graphs);

            if (fp[delta + offset] >= this.a.size() || graphs.size() > this.maxDiffs)
            {
                break;
            }
        }

        int r = paths[delta + offset];
        var comparePoint = new ArrayList<Coord>();
        while (r != -1)
        {
            Graph graph = graphs.get(r);
            comparePoint.add(new Coord(graph.x, graph.y));
            r = graph.r;
        }

        return comparePoint;
    }

    private int snake(int k, int p, int pp, int offset, int[] paths, List<Graph> graphs)
    {
        int r;
        if (p > pp)
        {
            r = paths[k - 1 + offset];
        }
        else
        {
            r = paths[k + 1 + offset];
        }

        int y = Math.max(p, pp);
        int x = y - k;

        while (x < a.size() && y < b.size() && Objects.equals(a.get(x), b.get(y)))
        {
            x++;
            y++;
        }

        paths[k + offset] = graphs.size();
        graphs.add(new Graph(x, y, r));

        return y;
    }
}
