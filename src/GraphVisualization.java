import javax.swing.*;

public class GraphVisualization {
    public static void main(String[] args) {
        // Example adjacency matrix (weighted directed graph)
        double[][] adjacencyMatrix = {
                {0, 2.5, 0, 1.0, 0},
                {0, 0, 3.2, 0, 1.5},
                {0, 0, 0, 4.0, 0},
                {0, 0, 0, 0, 2.8},
                {1.2, 0, 0, 0, 0}
        };

        SwingUtilities.invokeLater(() -> createAndShowGUI(adjacencyMatrix));
    }

    private static void createAndShowGUI(double[][] adjacencyMatrix) {
        JFrame frame = new JFrame("Weighted Directed Graph Visualization");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        WeightedDirectedGraph graph = new WeightedDirectedGraph(adjacencyMatrix);
        GraphPanel panel = new GraphPanel(graph);

        frame.add(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}