package mst;

import graph.Edge;
import graph.Graph;

import java.util.PriorityQueue;

/** Prim with a binary heap (lazy deletion). Restarts from every unvisited vertex, so it returns a forest if disconnected. */
public class Prim {

    public static MSTResult run(Graph g) {
        int n = g.V();
        boolean[] seen = new boolean[n];
        PriorityQueue<int[]> pq = new PriorityQueue<>((a, b) -> Integer.compare(a[0], b[0])); // {weight, vertex}
        long weight = 0, checks = 0, ops = 0;
        int visited = 0, components = 0;
        for (int s = 0; s < n; s++) {
            if (seen[s]) continue;
            components++;
            pq.add(new int[]{0, s}); // start vertex enters with cost 0
            ops++;
            while (!pq.isEmpty()) {
                int[] top = pq.poll();
                ops++;
                if (seen[top[1]]) continue;
                seen[top[1]] = true;
                weight += top[0];
                visited++;
                for (Edge e : g.adj(top[1])) {
                    checks++;
                    if (!seen[e.to()]) { pq.add(new int[]{e.weight(), e.to()}); ops++; }
                }
            }
        }
        return new MSTResult(weight, visited - components, components == 1, checks, ops);
    }
}
