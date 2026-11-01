package graph;

import java.util.ArrayList;
import java.util.List;

/** Graph stored as an adjacency list (vertices 0..n-1) plus a flat list of the original edges. */
public class Graph {
    private final int n;
    private final boolean directed;
    private final List<List<Edge>> adj = new ArrayList<>();
    private final List<Edge> edges = new ArrayList<>();

    public Graph(int n, boolean directed) {
        this.n = n;
        this.directed = directed;
        for (int i = 0; i < n; i++) adj.add(new ArrayList<>());
    }

    public void addEdge(int u, int v, int w) {
        Edge e = new Edge(u, v, w);
        edges.add(e);
        adj.get(u).add(e);
        if (!directed) adj.get(v).add(new Edge(v, u, w)); // undirected: store both directions
    }

    public int V() { return n; }
    public int E() { return edges.size(); }
    public boolean isDirected() { return directed; }
    public List<Edge> adj(int v) { return adj.get(v); }
    public List<Edge> edges() { return edges; }
}
