import java.util.ArrayList;
import java.util.List;

class WeightedDirectedGraph {
    private int numVertices;
    private double[][] adjacencyMatrix;
    private List<Edge> edges;
    private List<Node> nodes;

    public WeightedDirectedGraph(double[][] adjacencyMatrix) {
        this.adjacencyMatrix = adjacencyMatrix;
        this.numVertices = adjacencyMatrix.length;
        this.edges = new ArrayList<>();
        this.nodes = new ArrayList<>();

        buildEdgeList();
        positionNodes();
    }

    private void buildEdgeList() {
        for (int i = 0; i < numVertices; i++) {
            for (int j = 0; j < numVertices; j++) {
                if (adjacencyMatrix[i][j] != 0) {
                    edges.add(new Edge(i, j, adjacencyMatrix[i][j]));
                }
            }
        }
    }

    private void positionNodes() {
        double centerX = 400;
        double centerY = 300;
        double radius = 200;

        for (int i = 0; i < numVertices; i++) {
            double angle = 2 * Math.PI * i / numVertices;
            double x = centerX + radius * Math.cos(angle);
            double y = centerY + radius * Math.sin(angle);
            nodes.add(new Node(i, x, y));
        }
    }

    public List<Edge> getEdges() { return edges; }
    public List<Node> getNodes() { return nodes; }
    public int getNumVertices() { return numVertices; }
}