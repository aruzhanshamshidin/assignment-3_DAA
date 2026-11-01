package graph;

/** One weighted edge from -> to. For undirected graphs the reverse edge is stored in the adjacency list. */
public record Edge(int from, int to, int weight) {}
