### MST: Kruskal vs Prim (connected undirected graphs)

| Algorithm | V | E | Avg time (ms) | MST weight | Edge checks | DSU ops / heap ops |
|---|---|---|---|---|---|---|
| Kruskal | 10 | 30 | 0.0100 | 129 | 14 | 53 |
| Prim | 10 | 30 | 0.0125 | 129 | 60 | 62 |
| Kruskal | 100 | 500 | 0.1536 | 1170 | 164 | 679 |
| Prim | 100 | 500 | 0.1260 | 1170 | 1000 | 1002 |
| Kruskal | 1000 | 5000 | 0.7693 | 13123 | 3356 | 13932 |
| Prim | 1000 | 5000 | 1.6328 | 13123 | 10000 | 10002 |

### SCC (Kosaraju, directed random graphs)

| Algorithm | V | E | Avg time (ms) | SCC count | Edge examinations |
|---|---|---|---|---|---|
| SCC (Kosaraju) | 10 | 30 | 0.0075 | 1 | 90 |
| SCC (Kosaraju) | 100 | 500 | 0.0857 | 1 | 1500 |
| SCC (Kosaraju) | 1000 | 5000 | 0.3471 | 18 | 15000 |

### Topological ordering (1 = DAG, 0 = cycle found)

| Algorithm | V | E | Avg time (ms) | Is DAG | Edge examinations |
|---|---|---|---|---|---|
| Topo (DAG) | 10 | 30 | 0.0141 | 1 | 60 |
| Topo (cyclic) | 10 | 30 | 0.0017 | 0 | 30 |
| Topo (DAG) | 100 | 500 | 0.0410 | 1 | 1000 |
| Topo (cyclic) | 100 | 500 | 0.0077 | 0 | 500 |
| Topo (DAG) | 1000 | 5000 | 0.3097 | 1 | 10000 |
| Topo (cyclic) | 1000 | 5000 | 0.1537 | 0 | 5054 |

### Dijkstra (source 0, directed random graphs)

| Algorithm | V | E | Avg time (ms) | Reachable | Edge checks | Heap ops |
|---|---|---|---|---|---|---|
| Dijkstra | 10 | 30 | 0.0054 | 10 | 30 | 24 |
| Dijkstra | 100 | 500 | 0.0339 | 100 | 500 | 300 |
| Dijkstra | 1000 | 5000 | 0.3470 | 990 | 4946 | 2840 |

### DSU inside Kruskal (V=1000, E=5000)

| Variant | find calls | find steps (parent hops) | steps per find |
|---|---|---|---|
| No optimization | 6712 | 492169 | 73.327 |
| Path compression only | 6712 | 12549 | 1.870 |
| Union by rank only | 6712 | 11054 | 1.647 |
| Path compression + union by rank | 6712 | 6221 | 0.927 |

### DSU stress test: build a chain of 1000 elements, then call find(0) 1000 times

| Variant | find calls | find steps (parent hops) | steps per find |
|---|---|---|---|
| No optimization | 2998 | 999000 | 333.222 |
| Path compression only | 2998 | 1998 | 0.666 |
| Union by rank only | 2998 | 1997 | 0.666 |
| Path compression + union by rank | 2998 | 1997 | 0.666 |

