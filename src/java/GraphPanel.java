

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

// Panel for drawing the graph
class GraphPanel extends JPanel {
    private Graph graph;
    private Node draggedNode = null;

    public GraphPanel(Graph graph) {
        this.graph = graph;
        setPreferredSize(new Dimension(800, 600));
        setBackground(Color.WHITE);

        // Mouse listener for dragging nodes
        MouseAdapter mouseHandler = new MouseAdapter() {
            private int offsetX, offsetY;

            @Override
            public void mousePressed(MouseEvent e) {
                for (Node node : graph.getNodes()) {
                    if (node.contains(e.getX(), e.getY())) {
                        draggedNode = node;
                        offsetX = e.getX() - node.getX();
                        offsetY = e.getY() - node.getY();
                        break;
                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                draggedNode = null;
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (draggedNode != null) {
                    draggedNode.setX(e.getX() - offsetX);
                    draggedNode.setY(e.getY() - offsetY);
                    repaint();
                }
            }
        };

        addMouseListener(mouseHandler);
        addMouseMotionListener(mouseHandler);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw edges
        g2d.setColor(Color.GRAY);
        g2d.setStroke(new BasicStroke(2));

        for (Edge edge : graph.getEdges()) {
            Node source = edge.getSource();
            Node target = edge.getTarget();

            // Draw line
            g2d.drawLine(source.getX(), source.getY(), target.getX(), target.getY());

            // Draw arrow
            drawArrow(g2d, source.getX(), source.getY(), target.getX(), target.getY());

            // Draw weight
            int midX = (source.getX() + target.getX()) / 2;
            int midY = (source.getY() + target.getY()) / 2;
            g2d.setColor(Color.RED);
            g2d.drawString(String.valueOf(edge.getWeight()), midX, midY);
            g2d.setColor(Color.GRAY);
        }

        // Draw nodes
        for (Node node : graph.getNodes()) {
            g2d.setColor(new Color(100, 150, 255));
            g2d.fillOval(node.getX() - node.getRadius(),
                    node.getY() - node.getRadius(),
                    node.getRadius() * 2,
                    node.getRadius() * 2);

            g2d.setColor(Color.BLACK);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawOval(node.getX() - node.getRadius(),
                    node.getY() - node.getRadius(),
                    node.getRadius() * 2,
                    node.getRadius() * 2);

            // Draw node label
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 16));
            String label;
            if (graph.getLabel() != null) {
                label = graph.getLabel()[node.getId()];
            } else {
                label = String.valueOf(node.getId());
            }
            FontMetrics fm = g2d.getFontMetrics();
            int labelX = node.getX() - fm.stringWidth(label) / 2;
            int labelY = node.getY() + fm.getAscent() / 2;
            g2d.drawString(label, labelX, labelY);
        }
    }

    private void drawArrow(Graphics2D g2d, int x1, int y1, int x2, int y2) {
        double angle = Math.atan2(y2 - y1, x2 - x1);
        int arrowSize = 10;

        // Calculate arrow position near target node
        int arrowX = x2 - (int)(25 * Math.cos(angle));
        int arrowY = y2 - (int)(25 * Math.sin(angle));

        int[] xPoints = {
                arrowX,
                arrowX - (int)(arrowSize * Math.cos(angle - Math.PI / 6)),
                arrowX - (int)(arrowSize * Math.cos(angle + Math.PI / 6))
        };

        int[] yPoints = {
                arrowY,
                arrowY - (int)(arrowSize * Math.sin(angle - Math.PI / 6)),
                arrowY - (int)(arrowSize * Math.sin(angle + Math.PI / 6))
        };

        g2d.fillPolygon(xPoints, yPoints, 3);
    }
}