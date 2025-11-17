

import java.util.ArrayList;

class Graph {
    private ArrayList<Node> nodes;
    private ArrayList<Edge> edges;
    private int[][] adjacencyMatrix;
    private String[] label;

    public Graph(int[][] adjacencyMatrix, String[] label) {
        this.adjacencyMatrix = adjacencyMatrix;
        this.nodes = new ArrayList<>();
        this.edges = new ArrayList<>();
        this.label = label;
        initializeGraph();
    }

    private void initializeGraph() {
        int n = adjacencyMatrix.length;

        // Posisi manual untuk setiap node sesuai gambar
        int[][] positions = {
                {150, 250},   // MRS (0)
                {300, 150},   // SUB (1)
                {350, 350},   // CAK (2)
                {250, 500},   // BDG (3)
                {500, 250},   // DPS (4)
                {700, 200},   // FOG (5)
                {650, 350},   // DHS (6)
                {550, 500},   // MLG (7)
                {850, 400},   // BTM (8)
                {750, 550}    // PDG (9)
        };

        for (int i = 0; i < n; i++) {
            nodes.add(new Node(i, positions[i][0], positions[i][1]));
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
