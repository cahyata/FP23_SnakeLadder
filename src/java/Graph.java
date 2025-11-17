
import java.util.ArrayList;

// Graph class managing nodes and edges
class Graph {
    private ArrayList<Node> nodes;
    private ArrayList<Edge> edges;
    private int[][] adjacencyMatrix;
    private String[] label;

    public Graph(int[][] adjacencyMatrix) {
        this.adjacencyMatrix = adjacencyMatrix;
        this.nodes = new ArrayList<>();
        this.edges = new ArrayList<>();
        initializeGraph();
    }

    public Graph(int[][] adjacencyMatrix, String[] l) {
        this.adjacencyMatrix = adjacencyMatrix;
        this.nodes = new ArrayList<>();
        this.edges = new ArrayList<>();
        this.label = l;
        initializeGraph();
    }

    private void initializeGraph() {
        int n = adjacencyMatrix.length;

        // Create nodes in circular layout
        int centerX = 400;
        int centerY = 300;
        int radius = 200;

        for (int i = 0; i < n; i++) {
            double angle = 2 * Math.PI * i / n;
            int x = centerX + (int)(radius * Math.cos(angle));
            int y = centerY + (int)(radius * Math.sin(angle));
            nodes.add(new Node(i, x, y));
        }

        // Create edges from adjacency matrix
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (adjacencyMatrix[i][j] != 0) {
                    edges.add(new Edge(nodes.get(i), nodes.get(j), adjacencyMatrix[i][j]));
                }
            }
        }
    }

    public ArrayList<Node> getNodes() { return nodes; }
    public ArrayList<Edge> getEdges() { return edges; }
    public int[][] getAdjacencyMatrix() { return adjacencyMatrix; }
    public String[] getLabel() { return label; }
}