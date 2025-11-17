

import javax.swing.*;
import java.awt.*;

// Main application class
public class GraphVisualizer extends JFrame {
    private Graph graph;
    private GraphPanel graphPanel;

    public GraphVisualizer(int[][] adjacencyMatrix) {
        setTitle("Graph Visualizer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        String[] label = {"A", "B", "C", "D", "E", "F"};
        graph = new Graph(adjacencyMatrix, label);
        graphPanel = new GraphPanel(graph);

        add(graphPanel, BorderLayout.CENTER);

        // Add info panel
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new FlowLayout());
        JLabel infoLabel = new JLabel("Drag nodes to rearrange. Edges show weights.");
        infoPanel.add(infoLabel);
        add(infoPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
    }

    public static void main(String[] args) {
        // Example adjacency matrix (weighted directed graph)
        int[][] adjacencyMatrix = {
                {0, 1, 0, 1, 0, 0},
                {1, 0, 1, 0, 0, 0},
                {0, 1, 0, 0, 0, 0},
                {1, 0, 0, 0, 1, 1},
                {0, 0, 0, 1, 0, 0},
                {0, 0, 0, 1, 0, 0}
        };

        SwingUtilities.invokeLater(() -> {
            GraphVisualizer visualizer = new GraphVisualizer(adjacencyMatrix);
            visualizer.setVisible(true);
        });
    }
}