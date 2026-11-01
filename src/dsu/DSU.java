package dsu;

/** Disjoint Set Union. Path compression and union by rank can be switched off to compare them. */
public class DSU {
    private final int[] parent, rank;
    private final boolean compress, byRank;
    public long finds, findSteps, unions; // operation counters

    public DSU(int n) { this(n, true, true); }

    public DSU(int n, boolean compress, boolean byRank) {
        this.compress = compress;
        this.byRank = byRank;
        parent = new int[n];
        rank = new int[n];
        for (int i = 0; i < n; i++) parent[i] = i;
    }

    public int find(int x) {
        finds++;
        int root = x;
        while (parent[root] != root) { root = parent[root]; findSteps++; }
        if (compress) { // second pass: point every node on the path directly to the root
            while (parent[x] != root) { int next = parent[x]; parent[x] = root; x = next; }
        }
        return root;
    }

    /** Merges the sets of a and b. Returns false if they were already in the same set. */
    public boolean union(int a, int b) {
        int ra = find(a), rb = find(b);
        if (ra == rb) return false;
        unions++;
        if (byRank) {
            if (rank[ra] < rank[rb]) { int t = ra; ra = rb; rb = t; } // ra = higher rank
            parent[rb] = ra;
            if (rank[ra] == rank[rb]) rank[ra]++;
        } else {
            parent[rb] = ra;
        }
        return true;
    }
}
