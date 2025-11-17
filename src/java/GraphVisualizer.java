import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class GraphVisualizer extends JFrame {
    private Graph graph;
    private GraphPanel graphPanel;

    public GraphVisualizer(int[][] adjacencyMatrix, String[] labels) {
        setTitle("Graph Visualizer - Indonesian Cities (Dijkstra Path)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        graph = new Graph(adjacencyMatrix, labels);
        graphPanel = new GraphPanel(graph);

        // --- MODIFIKASI: Jalankan Dijkstra dan kirim path ke Panel ---

        // Tentukan Source dan Destination (contoh: MKS ke PDG)
        // (labels: MKS=0, SUB=1, BDG=2, CGK=3, DPS=4, MLG=5, DHS=6, YOG=7, PDG=8, BTM=9)
        Node sourceNode = graph.getNodes().get(0); // MKS
        Node destNode = graph.getNodes().get(8);   // PDG

        // 1. Panggil method dijkstra
        ArrayList<Edge> shortestPath = graph.dijkstra(sourceNode, destNode);

        // 2. Kirim hasilnya ke GraphPanel untuk digambar
        graphPanel.setShortestPath(shortestPath);

        // --- AKHIR MODIFIKASI ---

        add(graphPanel, BorderLayout.CENTER);

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new FlowLayout());

        // --- MODIFIKASI BARU: Tambahkan deskripsi jarak ---

        String infoText;
        if (shortestPath != null && !shortestPath.isEmpty()) {
            // Hitung total jarak
            int totalDistance = 0;
            for (Edge edge : shortestPath) {
                totalDistance += edge.getWeight();
            }

            // Dapatkan label
            String sourceLabel = graph.getLabel()[sourceNode.getId()];
            String destLabel = graph.getLabel()[destNode.getId()];

            infoText = String.format("Rute terpendek (Dijkstra) dari %s ke %s adalah: %d",
                    sourceLabel, destLabel, totalDistance);
        } else if (sourceNode.equals(destNode)) {
            infoText = "Source dan Destination sama.";
        } else {
            String sourceLabel = graph.getLabel()[sourceNode.getId()];
            String destLabel = graph.getLabel()[destNode.getId()];
            infoText = String.format("Tidak ditemukan rute dari %s ke %s.", sourceLabel, destLabel);
        }

        JLabel infoLabel = new JLabel(infoText);
        // --- AKHIR MODIFIKASI BARU ---

        infoPanel.add(infoLabel);
        add(infoPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
    }

    public static void main(String[] args) {
        // (Saya akan gunakan matriks yang sudah diperbaiki dari jawaban sebelumnya)
        int[][] adjacencyMatrixFixed = {
                //MKS,SUB,BDG,CGK,DPS,MLG,DHS,YOG,PDG,BTM
                {0, 2, 3, 0, 8, 0, 0, 0, 0, 0},    // MKS (0)
                {2, 0, 0, 3, 1, 0, 0, 0, 0, 0},    // SUB (1)
                {3, 0, 0, 4, 0, 2, 0, 0, 0, 0},    // BDG (2)
                {0, 3, 4, 0, 0, 6, 4, 0, 0, 0},    // CGK (3)
                {8, 1, 0, 0, 0, 0, 2, 3, 10, 0},   // DPS (4)
                {0, 0, 2, 6, 0, 0, 8, 0, 0, 4},    // MLG (5)
                {0, 0, 0, 4, 2, 8, 0, 0, 3, 0},    // DHS (6)
                {0, 0, 0, 0, 3, 0, 0, 0, 4, 0},    // YOG (7)
                {0, 0, 0, 0, 10, 0, 3, 4, 0, 3},   // PDG (8)
                {0, 0, 0, 0, 0, 4, 0, 0, 3, 0}     // BTM (9)
        };

        String [] labels={"MKS","SUB","BDG","CGK","DPS","MLG","DHS","YOG","PDG","BTM"};

        SwingUtilities.invokeLater(() -> {
            GraphVisualizer visualizer = new GraphVisualizer(adjacencyMatrixFixed, labels);
            visualizer.setVisible(true);
        });
    }
}