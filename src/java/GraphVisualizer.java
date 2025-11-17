import javax.swing.*;
import java.awt.*;

public class GraphVisualizer extends JFrame {
    private Graph graph;
    private GraphPanel graphPanel;

    public GraphVisualizer(int[][] adjacencyMatrix, String[] labels) {
        setTitle("Graph Visualizer - Indonesian Cities");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        graph = new Graph(adjacencyMatrix, labels);
        graphPanel = new GraphPanel(graph);

        add(graphPanel, BorderLayout.CENTER);

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new FlowLayout());
        JLabel infoLabel = new JLabel("Drag nodes to rearrange. Edges show weights (distances).");
        infoPanel.add(infoLabel);
        add(infoPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
    }

    public static void main(String[] args) {
        // Adjacency matrix sesuai dengan gambar
        // Order: MRS, SUB, CAK, BDG, DPS, FOG, DHS, MLG, BTM, PDG
        int[][] adjacencyMatrix = {
                {0, 2, 3, 0, 8, 0, 0, 0, 0, 0},
                {2, 0, 0, 3, 1, 0, 0, 0, 0, 0},
                {3, 0, 0, 4, 0, 2, 0, 0, 0, 0},
                {0, 3, 4, 0, 0, 6, 4, 0, 0, 0},
                {8, 1, 0, 0, 0, 0, 2, 3, 0, 10},
                {0, 0, 2, 6, 0, 0, 8, 0, 4, 0},
                {0, 0, 0, 4, 2, 8, 0, 0, 0, 3},
                {0, 0, 0, 0, 3, 0, 0, 0, 0, 4},
                {0, 0, 0, 0, 0, 4, 0, 0, 0, 3},
                {0, 0, 0, 0, 10, 0, 3, 4, 3,0}
        };

        String [] labels={"MKS","SUB","BDG","CGK","DPS","MLG","DHS","YOG","PDG","BTM"};

        SwingUtilities.invokeLater(() -> {
            GraphVisualizer visualizer = new GraphVisualizer(adjacencyMatrix, labels);
            visualizer.setVisible(true);
            });
    }
}