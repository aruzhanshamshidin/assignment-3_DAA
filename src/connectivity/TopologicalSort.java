package connectivity;

import graph.Edge;
import graph.Graph;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/** Topological order with Kahn's algorithm. If the graph has a cycle, isDag = false and order is empty. */
public class TopologicalSort {

    public record Result(List<Integer> order, boolean isDag, long ops) {}

    public static Result run(Graph g) {
        int n = g.V();
        int[] indeg = new int[n];
        long ops = 0;
        for (Edge e : g.edges()) { indeg[e.to()]++; ops++; }
        Deque<Integer> queue = new ArrayDeque<>();
        for (int v = 0; v < n; v++) if (indeg[v] == 0) queue.add(v);
        List<Integer> order = new ArrayList<>();
        while (!queue.isEmpty()) {
            int u = queue.poll();
            order.add(u);
            for (Edge e : g.adj(u)) {
                ops++;
                if (--indeg[e.to()] == 0) queue.add(e.to());
            }
        }
        boolean dag = order.size() == n; // vertices on a cycle never reach in-degree 0
        return new Result(dag ? order : List.of(), dag, ops);
    }
}
