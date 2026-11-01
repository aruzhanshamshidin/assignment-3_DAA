package shortestpath;

import graph.Edge;
import graph.Graph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;

/** Dijkstra with a binary heap (lazy deletion). Rejects negative weights. */
public class Dijkstra {
    public static final long INF = Long.MAX_VALUE;

    private final long[] dist;
    private final int[] prev;
    private long edgeChecks, heapOps;

    public Dijkstra(Graph g, int source) {
        for (Edge e : g.edges())
            if (e.weight() < 0) throw new IllegalArgumentException("Negative edge weight: Dijkstra not applicable");
        dist = new long[g.V()];
        prev = new int[g.V()];
        Arrays.fill(dist, INF);
        Arrays.fill(prev, -1);
        dist[source] = 0;
        PriorityQueue<long[]> pq = new PriorityQueue<>((a, b) -> Long.compare(a[0], b[0])); // {distance, vertex}
        pq.add(new long[]{0, source});
        heapOps++;
        while (!pq.isEmpty()) {
            long[] top = pq.poll();
            heapOps++;
            int u = (int) top[1];
            if (top[0] > dist[u]) continue; // outdated entry
            for (Edge e : g.adj(u)) {
                edgeChecks++;
                long nd = dist[u] + e.weight();
                if (nd < dist[e.to()]) {
                    dist[e.to()] = nd;
                    prev[e.to()] = u;
                    pq.add(new long[]{nd, e.to()});
                    heapOps++;
                }
            }
        }
    }

    public long dist(int v) { return dist[v]; }
    public boolean reachable(int v) { return dist[v] != INF; }
    public long edgeChecks() { return edgeChecks; }
    public long heapOps() { return heapOps; }

    public int reachableCount() {
        int c = 0;
        for (long d : dist) if (d != INF) c++;
        return c;
    }

    /** Path source -> v as a vertex list; empty if v is unreachable. */
    public List<Integer> path(int v) {
        List<Integer> p = new ArrayList<>();
        if (!reachable(v)) return p;
        for (int x = v; x != -1; x = prev[x]) p.add(x);
        Collections.reverse(p);
        return p;
    }
}
