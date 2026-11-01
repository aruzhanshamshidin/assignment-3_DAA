import graph.Graph;
import graph.GraphGenerator;
import mst.Kruskal;
import mst.Prim;
import shortestpath.Dijkstra;

/** Entry point: runs tests, a small demo, then all experiments. Run from the project root so results/ is created there. */
public class Main {
    public static void main(String[] args) throws Exception {
        if (!Tests.run()) return; // stop if any test fails

        Graph und = GraphGenerator.connectedUndirected(10, 30, 42, 100);
        System.out.println("Demo (V=10, E=30): Kruskal weight = " + Kruskal.run(und).weight()
                + ", Prim weight = " + Prim.run(und).weight());

        Graph dir = GraphGenerator.directed(10, 30, 42, 100);
        Dijkstra d = new Dijkstra(dir, 0);
        for (int v = 0; v < dir.V(); v++)
            System.out.println("0 -> " + v + ": " + (d.reachable(v) ? d.dist(v) + " path " + d.path(v) : "unreachable"));

        Benchmark.run();
    }
}
