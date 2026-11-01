package mst;

import dsu.DSU;
import graph.Edge;
import graph.Graph;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Kruskal: sort edges by weight, add an edge if it joins two different components (DSU). */
public class Kruskal {

    public static MSTResult run(Graph g) { return run(g, new DSU(g.V())); }

    public static MSTResult run(Graph g, DSU dsu) {
        List<Edge> sorted = new ArrayList<>(g.edges());
        sorted.sort(Comparator.comparingInt(Edge::weight));
        long weight = 0, checks = 0;
        int used = 0;
        for (Edge e : sorted) {
            if (used == g.V() - 1) break; // tree is complete
            checks++;
            if (dsu.union(e.from(), e.to())) { weight += e.weight(); used++; }
        }
        return new MSTResult(weight, used, used == g.V() - 1, checks, dsu.finds + dsu.findSteps + dsu.unions);
    }
}
