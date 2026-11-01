import connectivity.SCC;
import connectivity.TopologicalSort;
import dsu.DSU;
import graph.Edge;
import graph.Graph;
import graph.GraphGenerator;
import mst.Kruskal;
import mst.MSTResult;
import mst.Prim;
import shortestpath.Dijkstra;

import java.util.List;

/** Plain-Java tests (no JUnit needed). Prints FAIL lines and a summary. */
public class Tests {
    private static int passed, failed;

    private static void check(boolean ok, String name) {
        if (ok) passed++; else { failed++; System.out.println("FAIL: " + name); }
    }

    public static boolean run() {
        passed = failed = 0;
        testSingleVertex();
        testDisconnected();
        testSmallAndMultipleMSTs();
        testRandomMST();
        testDSU();
        testTopological();
        testSCC();
        testDijkstra();
        System.out.println("Tests: " + passed + " passed, " + failed + " failed");
        return failed == 0;
    }

    private static void testSingleVertex() {
        Graph u = new Graph(1, false), d = new Graph(1, true);
        check(Kruskal.run(u).weight() == 0 && Kruskal.run(u).connected(), "Kruskal single vertex");
        check(Prim.run(u).weight() == 0 && Prim.run(u).connected(), "Prim single vertex");
        check(TopologicalSort.run(d).isDag(), "Topo single vertex");
        check(new SCC(d).run().components().size() == 1, "SCC single vertex");
        check(new Dijkstra(d, 0).dist(0) == 0, "Dijkstra single vertex");
    }

    private static void testDisconnected() {
        Graph g = new Graph(4, false);
        g.addEdge(0, 1, 1);
        g.addEdge(2, 3, 2);
        MSTResult k = Kruskal.run(g), p = Prim.run(g);
        check(!k.connected() && !p.connected(), "disconnected detected");
        check(k.weight() == 3 && p.weight() == 3, "disconnected forest weight");
    }

    private static void testSmallAndMultipleMSTs() {
        Graph tri = new Graph(3, false); // cycle 0-1-2
        tri.addEdge(0, 1, 1); tri.addEdge(1, 2, 2); tri.addEdge(0, 2, 3);
        check(Kruskal.run(tri).weight() == 3 && Prim.run(tri).weight() == 3, "triangle MST = 3");

        Graph sq = new Graph(4, false); // all weights equal -> several valid MSTs, weight must still be 3
        sq.addEdge(0, 1, 1); sq.addEdge(1, 2, 1); sq.addEdge(2, 3, 1); sq.addEdge(3, 0, 1);
        check(Kruskal.run(sq).weight() == 3 && Prim.run(sq).weight() == 3, "multiple MSTs weight");
    }

    private static void testRandomMST() {
        for (int seed = 0; seed < 200; seed++) {
            int maxW = seed % 2 == 0 ? 100 : 3; // maxW = 3 -> many duplicate weights
            Graph g = GraphGenerator.connectedUndirected(30, 90, seed, maxW);
            MSTResult k = Kruskal.run(g), p = Prim.run(g);
            check(k.weight() == p.weight() && k.connected() && p.connected() && k.edgesUsed() == 29,
                    "Kruskal == Prim, seed " + seed);
        }
    }

    private static void testDSU() {
        DSU d = new DSU(5);
        check(d.find(0) != d.find(1), "DSU separate at start");
        check(d.union(0, 1) && d.union(1, 2), "DSU union");
        check(d.find(0) == d.find(2), "DSU same set");
        check(!d.union(0, 2), "DSU union of same set returns false");
    }

    private static void testTopological() {
        for (int seed = 0; seed < 50; seed++) {
            Graph g = GraphGenerator.dag(40, 120, seed, 10);
            TopologicalSort.Result r = TopologicalSort.run(g);
            check(r.isDag() && r.order().size() == 40 && validOrder(g, r.order()), "valid topo order, seed " + seed);
        }
        Graph cyc = new Graph(3, true);
        cyc.addEdge(0, 1, 1); cyc.addEdge(1, 2, 1); cyc.addEdge(2, 0, 1);
        check(!TopologicalSort.run(cyc).isDag(), "cycle detected");
    }

    private static boolean validOrder(Graph g, List<Integer> order) {
        int[] pos = new int[g.V()];
        for (int i = 0; i < order.size(); i++) pos[order.get(i)] = i;
        for (Edge e : g.edges()) if (pos[e.from()] >= pos[e.to()]) return false;
        return true;
    }

    private static void testSCC() {
        Graph g = new Graph(5, true); // {0,1,2} and {3,4}
        g.addEdge(0, 1, 1); g.addEdge(1, 2, 1); g.addEdge(2, 0, 1);
        g.addEdge(2, 3, 1); g.addEdge(3, 4, 1); g.addEdge(4, 3, 1);
        check(new SCC(g).run().components().size() == 2, "SCC known example");

        for (int seed = 0; seed < 50; seed++) {
            Graph r = GraphGenerator.directed(15, 30, seed, 10);
            List<List<Integer>> comps = new SCC(r).run().components();
            int[] id = new int[15];
            int[] count = new int[15];
            for (int c = 0; c < comps.size(); c++)
                for (int v : comps.get(c)) { id[v] = c; count[v]++; }
            boolean once = true;
            for (int c : count) if (c != 1) once = false; // every vertex in exactly one SCC
            check(once, "each vertex in exactly one SCC, seed " + seed);

            long[][] d = floyd(r);
            boolean same = true;
            for (int u = 0; u < 15; u++)
                for (int v = 0; v < 15; v++) {
                    boolean mutual = d[u][v] < INF && d[v][u] < INF;
                    if (mutual != (id[u] == id[v])) same = false;
                }
            check(same, "SCC = mutual reachability, seed " + seed);
        }
    }

    private static void testDijkstra() {
        for (int seed = 0; seed < 50; seed++) {
            Graph g = GraphGenerator.directed(15, 40, seed, 20);
            long[][] f = floyd(g);
            for (int s = 0; s < 15; s++) {
                Dijkstra dj = new Dijkstra(g, s);
                for (int v = 0; v < 15; v++) {
                    boolean ok = f[s][v] >= INF ? !dj.reachable(v) : dj.dist(v) == f[s][v];
                    check(ok, "Dijkstra vs Floyd, seed " + seed);
                }
            }
        }
        Graph g = new Graph(3, true); // vertex 2 is unreachable
        g.addEdge(0, 1, 5);
        Dijkstra d = new Dijkstra(g, 0);
        check(d.dist(1) == 5 && d.path(1).equals(List.of(0, 1)), "Dijkstra path");
        check(!d.reachable(2) && d.path(2).isEmpty(), "Dijkstra unreachable");

        Graph neg = new Graph(2, true);
        neg.addEdge(0, 1, -1);
        boolean thrown = false;
        try { new Dijkstra(neg, 0); } catch (IllegalArgumentException e) { thrown = true; }
        check(thrown, "Dijkstra rejects negative weights");
    }

    private static final long INF = Long.MAX_VALUE / 4;

    /** Reference all-pairs shortest paths, used only to verify other algorithms. */
    private static long[][] floyd(Graph g) {
        int n = g.V();
        long[][] d = new long[n][n];
        for (int i = 0; i < n; i++) { java.util.Arrays.fill(d[i], INF); d[i][i] = 0; }
        for (Edge e : g.edges()) d[e.from()][e.to()] = Math.min(d[e.from()][e.to()], e.weight());
        for (int k = 0; k < n; k++)
            for (int i = 0; i < n; i++)
                for (int j = 0; j < n; j++)
                    if (d[i][k] + d[k][j] < d[i][j]) d[i][j] = d[i][k] + d[k][j];
        return d;
    }
}
