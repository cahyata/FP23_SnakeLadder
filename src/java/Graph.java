import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

class Graph {
    private ArrayList<Node> nodes;
    private ArrayList<Edge> edges;
    private int[][] adjacencyMatrix;
    private String[] label;
    // ADJLIST BARU: Untuk pencarian tetangga yang efisien di Dijkstra
    private Map<Node, ArrayList<Edge>> adjList;

    public Graph(int[][] adjacencyMatrix, String[] label) {
        this.adjacencyMatrix = adjacencyMatrix;
        this.nodes = new ArrayList<>();
        this.edges = new ArrayList<>();
        this.label = label;
        this.adjList = new HashMap<>(); // Inisialisasi adjList
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
            Node node = new Node(i, positions[i][0], positions[i][1]);
            nodes.add(node);
            adjList.put(node, new ArrayList<>()); // Inisialisasi list untuk setiap node
        }

        // Create edges from adjacency matrix
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (adjacencyMatrix[i][j] != 0) {
                    Node source = nodes.get(i);
                    Node target = nodes.get(j);
                    Edge edge = new Edge(source, target, adjacencyMatrix[i][j]);
                    edges.add(edge);
                    adjList.get(source).add(edge); // Tambahkan edge ke adjList
                }
            }
        }
    }

    /**
     * Implementasi Algoritma Dijkstra untuk mencari rute terpendek.
     * @param source Node awal
     * @param destination Node tujuan
     * @return ArrayList<Edge> yang merepresentasikan rute terpendek
     */
    public ArrayList<Edge> dijkstra(Node source, Node destination) {
        Map<Node, Integer> distances = new HashMap<>();
        Map<Node, Node> previous = new HashMap<>();
        Map<Node, Edge> edgeToPrev = new HashMap<>(); // Melacak edge ke node sebelumnya
        PriorityQueue<Node> pq = new PriorityQueue<>(Comparator.comparingInt(node -> distances.getOrDefault(node, Integer.MAX_VALUE)));
        Set<Node> visited = new HashSet<>();

        // Inisialisasi jarak
        for (Node node : nodes) {
            distances.put(node, Integer.MAX_VALUE);
        }
        distances.put(source, 0);
        pq.add(source);

        while (!pq.isEmpty()) {
            Node current = pq.poll();

            if (current.equals(destination)) {
                break; // Ditemukan
            }
            if (visited.contains(current)) {
                continue;
            }
            visited.add(current);

            // Cek semua tetangga
            for (Edge edge : adjList.get(current)) {
                Node neighbor = edge.getTarget();
                if (visited.contains(neighbor)) {
                    continue;
                }

                int newDist = distances.get(current) + edge.getWeight();
                if (newDist < distances.getOrDefault(neighbor, Integer.MAX_VALUE)) {
                    distances.put(neighbor, newDist);
                    previous.put(neighbor, current);
                    edgeToPrev.put(neighbor, edge); // Simpan edge yang digunakan

                    // Hapus dan tambahkan lagi untuk update prioritas
                    pq.remove(neighbor);
                    pq.add(neighbor);
                }
            }
        }

        // Rekonstruksi rute
        ArrayList<Edge> path = new ArrayList<>();
        Node step = destination;
        while (previous.get(step) != null) {
            Edge pathEdge = edgeToPrev.get(step);
            if (pathEdge == null) break; // Tidak ada rute
            path.add(pathEdge);
            step = previous.get(step);
        }
        Collections.reverse(path); // Balik urutan agar dari source -> destination

        if (path.isEmpty() && !source.equals(destination)) {
            System.out.println("Tidak ditemukan rute dari " + label[source.getId()] + " ke " + label[destination.getId()]);
            return null; // Tidak ada rute
        }

        return path;
    }

    public ArrayList<Node> getNodes() { return nodes; }
    public ArrayList<Edge> getEdges() { return edges; }
    public int[][] getAdjacencyMatrix() { return adjacencyMatrix; }
    public String[] getLabel() { return label; }
}