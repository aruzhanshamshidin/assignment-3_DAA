import connectivity.SCC;
import connectivity.TopologicalSort;
import dsu.DSU;
import graph.Graph;
import graph.GraphGenerator;
import mst.Kruskal;
import mst.MSTResult;
import mst.Prim;
import shortestpath.Dijkstra;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Runs all experiments (5 timed runs each, System.nanoTime) and writes tables + plots into results/. */
public class Benchmark {
    static final int RUNS = 5, WARMUP = 300, MAXW = 100;
    static final long SEED = 42;
    static final int[][] SIZES = {{10, 30}, {100, 500}, {1000, 5000}}; // {V, E}
    static long sink; // keeps the JIT from removing the measured work

    record Row(String algo, int v, int e, double ms, long[] vals) {}

    interface Algo { long[] run(); }

    static Row measure(String name, int v, int e, Algo a) {
        long[] vals = null;
        for (int i = 0; i < WARMUP; i++) vals = a.run(); // warm-up (JIT), not timed
        long total = 0;
        for (int i = 0; i < RUNS; i++) {
            long t = System.nanoTime();
            sink += a.run()[0];
            total += System.nanoTime() - t;
        }
        return new Row(name, v, e, total / (double) RUNS / 1e6, vals);
    }

    public static void run() throws IOException {
        List<Row> mst = new ArrayList<>(), scc = new ArrayList<>(), topo = new ArrayList<>(), dij = new ArrayList<>();
        for (int[] s : SIZES) {
            int v = s[0], e = s[1];
            Graph und = GraphGenerator.connectedUndirected(v, e, SEED, MAXW);
            Graph dir = GraphGenerator.directed(v, e, SEED, MAXW);
            Graph dag = GraphGenerator.dag(v, e, SEED, MAXW);

            mst.add(measure("Kruskal", v, e, () -> { MSTResult r = Kruskal.run(und); return new long[]{r.weight(), r.edgeChecks(), r.ops()}; }));
            mst.add(measure("Prim", v, e, () -> { MSTResult r = Prim.run(und); return new long[]{r.weight(), r.edgeChecks(), r.ops()}; }));
            scc.add(measure("SCC (Kosaraju)", v, e, () -> { SCC.Result r = new SCC(dir).run(); return new long[]{r.components().size(), r.ops()}; }));
            topo.add(measure("Topo (DAG)", v, e, () -> { TopologicalSort.Result r = TopologicalSort.run(dag); return new long[]{r.isDag() ? 1 : 0, r.ops()}; }));
            topo.add(measure("Topo (cyclic)", v, e, () -> { TopologicalSort.Result r = TopologicalSort.run(dir); return new long[]{r.isDag() ? 1 : 0, r.ops()}; }));
            dij.add(measure("Dijkstra", v, e, () -> { Dijkstra d = new Dijkstra(dir, 0); return new long[]{d.reachableCount(), d.edgeChecks(), d.heapOps()}; }));
        }

        StringBuilder md = new StringBuilder();
        md.append(table("MST: Kruskal vs Prim (connected undirected graphs)", new String[]{"MST weight", "Edge checks", "DSU ops / heap ops"}, mst));
        md.append(table("SCC (Kosaraju, directed random graphs)", new String[]{"SCC count", "Edge examinations"}, scc));
        md.append(table("Topological ordering (1 = DAG, 0 = cycle found)", new String[]{"Is DAG", "Edge examinations"}, topo));
        md.append(table("Dijkstra (source 0, directed random graphs)", new String[]{"Reachable", "Edge checks", "Heap ops"}, dij));
        md.append(dsuTables());

        Files.createDirectories(Path.of("results/tables"));
        Files.createDirectories(Path.of("results/plots"));
        Files.writeString(Path.of("results/tables/results.md"), md.toString());

        List<Row> all = new ArrayList<>();
        all.addAll(mst); all.addAll(scc); all.addAll(topo); all.addAll(dij);
        csv(all);
        plot("results/plots/time_vs_vertices.svg", "Execution time vs number of vertices", "Vertices (V)", all, true);
        plot("results/plots/time_vs_edges.svg", "Execution time vs number of edges", "Edges (E)", all, false);
        System.out.println(md);
        System.out.println("Results written to results/ (sink=" + sink + ")");
    }

    static String table(String title, String[] cols, List<Row> rows) {
        StringBuilder sb = new StringBuilder("### " + title + "\n\n| Algorithm | V | E | Avg time (ms) |");
        for (String c : cols) sb.append(" ").append(c).append(" |");
        sb.append("\n|---|---|---|---|").append("---|".repeat(cols.length)).append("\n");
        for (Row r : rows) {
            sb.append(String.format(Locale.ROOT, "| %s | %d | %d | %.4f |", r.algo(), r.v(), r.e(), r.ms()));
            for (long x : r.vals()) sb.append(" ").append(x).append(" |");
            sb.append("\n");
        }
        return sb.append("\n").toString();
    }

    static String dsuTables() {
        Object[][] variants = {{"No optimization", false, false}, {"Path compression only", true, false},
                {"Union by rank only", false, true}, {"Path compression + union by rank", true, true}};
        StringBuilder sb = new StringBuilder();

        Graph big = GraphGenerator.connectedUndirected(1000, 5000, SEED, MAXW);
        sb.append("### DSU inside Kruskal (V=1000, E=5000)\n\n| Variant | find calls | find steps (parent hops) | steps per find |\n|---|---|---|---|\n");
        for (Object[] x : variants) {
            DSU d = new DSU(1000, (boolean) x[1], (boolean) x[2]);
            Kruskal.run(big, d);
            sb.append(String.format(Locale.ROOT, "| %s | %d | %d | %.3f |%n", x[0], d.finds, d.findSteps, d.findSteps / (double) d.finds));
        }

        sb.append("\n### DSU stress test: build a chain of 1000 elements, then call find(0) 1000 times\n\n| Variant | find calls | find steps (parent hops) | steps per find |\n|---|---|---|---|\n");
        for (Object[] x : variants) {
            DSU d = new DSU(1000, (boolean) x[1], (boolean) x[2]);
            for (int i = 1; i < 1000; i++) d.union(i, i - 1); // without rank this builds a long chain
            for (int i = 0; i < 1000; i++) d.find(0);
            sb.append(String.format(Locale.ROOT, "| %s | %d | %d | %.3f |%n", x[0], d.finds, d.findSteps, d.findSteps / (double) d.finds));
        }
        return sb.append("\n").toString();
    }

    static void csv(List<Row> rows) throws IOException {
        StringBuilder sb = new StringBuilder("algorithm,V,E,avg_ms\n");
        for (Row r : rows) sb.append(String.format(Locale.ROOT, "%s,%d,%d,%.5f%n", r.algo(), r.v(), r.e(), r.ms()));
        Files.writeString(Path.of("results/tables/results.csv"), sb.toString());
    }

    static void plot(String file, String title, String xLabel, List<Row> rows, boolean byV) throws IOException {
        Map<String, List<Row>> groups = new LinkedHashMap<>();
        for (Row r : rows) groups.computeIfAbsent(r.algo(), k -> new ArrayList<>()).add(r);
        Map<String, double[][]> series = new LinkedHashMap<>();
        for (Map.Entry<String, List<Row>> g : groups.entrySet()) {
            int n = g.getValue().size();
            double[][] s = new double[2][n];
            for (int i = 0; i < n; i++) {
                Row r = g.getValue().get(i);
                s[0][i] = byV ? r.v() : r.e();
                s[1][i] = Math.max(r.ms(), 1e-6);
            }
            series.put(g.getKey(), s);
        }
        PlotWriter.line(file, title, xLabel, series);
    }
}
