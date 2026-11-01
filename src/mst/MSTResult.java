package mst;

/** weight = total MST (forest) weight; edgeChecks = edges examined; ops = DSU ops (Kruskal) or heap ops (Prim). */
public record MSTResult(long weight, int edgesUsed, boolean connected, long edgeChecks, long ops) {}
