package graph;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/** Reproducible graph generators (same seed -> same graph). No self-loops, no duplicate edges. */
public class GraphGenerator {

    /** Connected undirected graph: random spanning tree first, then random extra edges up to m. */
    public static Graph connectedUndirected(int n, int m, long seed, int maxW) {
        Random r = new Random(seed);
        Graph g = new Graph(n, false);
        Set<Long> used = new HashSet<>();
        for (int v = 1; v < n; v++) add(g, used, r.nextInt(v), v, r, maxW); // links v to an earlier vertex
        while (g.E() < m) {
            int u = r.nextInt(n), v = r.nextInt(n);
            if (u != v) add(g, used, u, v, r, maxW);
        }
        return g;
    }

    /** Random directed graph: m random edges. Usually contains cycles. */
    public static Graph directed(int n, int m, long seed, int maxW) {
        Random r = new Random(seed);
        Graph g = new Graph(n, true);
        Set<Long> used = new HashSet<>();
        while (g.E() < m) {
            int u = r.nextInt(n), v = r.nextInt(n);
            if (u != v) add(g, used, u, v, r, maxW);
        }
        return g;
    }

    /** Random DAG: edges only go from a smaller to a larger position in a hidden random order. */
    public static Graph dag(int n, int m, long seed, int maxW) {
        Random r = new Random(seed);
        int[] label = new int[n];
        for (int i = 0; i < n; i++) label[i] = i;
        for (int i = n - 1; i > 0; i--) { // Fisher-Yates shuffle
            int j = r.nextInt(i + 1);
            int t = label[i]; label[i] = label[j]; label[j] = t;
        }
        Graph g = new Graph(n, true);
        Set<Long> used = new HashSet<>();
        while (g.E() < m) {
            int a = r.nextInt(n), b = r.nextInt(n);
            if (a != b) add(g, used, label[Math.min(a, b)], label[Math.max(a, b)], r, maxW);
        }
        return g;
    }

    private static void add(Graph g, Set<Long> used, int u, int v, Random r, int maxW) {
        long key = g.isDirected() ? (long) u * 100000 + v : (long) Math.min(u, v) * 100000 + Math.max(u, v);
        if (used.add(key)) g.addEdge(u, v, r.nextInt(maxW) + 1);
    }
}
