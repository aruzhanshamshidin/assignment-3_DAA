package connectivity;

import graph.Edge;
import graph.Graph;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/** Strongly connected components with Kosaraju: DFS for finish order, then DFS on the reversed graph. */
public class SCC {

    public record Result(List<List<Integer>> components, long ops) {}

    private final Graph g;
    private final List<List<Integer>> rev = new ArrayList<>();
    private boolean[] seen;
    private long ops;

    public SCC(Graph g) { this.g = g; }

    public Result run() {
        int n = g.V();
        seen = new boolean[n];
        Deque<Integer> order = new ArrayDeque<>(); // stack: last finished vertex on top
        for (int v = 0; v < n; v++) if (!seen[v]) dfs1(v, order);

        for (int v = 0; v < n; v++) rev.add(new ArrayList<>());
        for (Edge e : g.edges()) { rev.get(e.to()).add(e.from()); ops++; }

        seen = new boolean[n];
        List<List<Integer>> components = new ArrayList<>();
        while (!order.isEmpty()) {
            int v = order.pop();
            if (seen[v]) continue;
            List<Integer> comp = new ArrayList<>();
            dfs2(v, comp);
            components.add(comp);
        }
        return new Result(components, ops);
    }

    private void dfs1(int v, Deque<Integer> order) {
        seen[v] = true;
        for (Edge e : g.adj(v)) {
            ops++;
            if (!seen[e.to()]) dfs1(e.to(), order);
        }
        order.push(v);
    }

    private void dfs2(int v, List<Integer> comp) {
        seen[v] = true;
        comp.add(v);
        for (int u : rev.get(v)) {
            ops++;
            if (!seen[u]) dfs2(u, comp);
        }
    }
}
