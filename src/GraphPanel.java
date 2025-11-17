import javax.swing.*;
import java.awt.*;
import java.awt.geom.Line2D;

class GraphPanel extends JPanel {
    private WeightedDirectedGraph graph;
    private static final int NODE_RADIUS = 25;
    private static final Color NODE_COLOR = new Color(100, 149, 237);
    private static final Color EDGE_COLOR = new Color(70, 70, 70);
    private static final Color TEXT_COLOR = Color.WHITE;

    public GraphPanel(WeightedDirectedGraph graph) {
        this.graph = graph;
        setPreferredSize(new Dimension(800, 600));
        setBackground(Color.WHITE);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawEdges(g2d);
        drawNodes(g2d);
    }

    private void drawEdges(Graphics2D g2d) {
        g2d.setColor(EDGE_COLOR);
        g2d.setStroke(new BasicStroke(2));

        for (Edge edge : graph.getEdges()) {
            Node source = graph.getNodes().get(edge.getSource());
            Node dest = graph.getNodes().get(edge.getDestination());

            double dx = dest.getX() - source.getX();
            double dy = dest.getY() - source.getY();
            double angle = Math.atan2(dy, dx);

            // Adjust start and end points to be on the circle edge
            double startX = source.getX() + NODE_RADIUS * Math.cos(angle);
            double startY = source.getY() + NODE_RADIUS * Math.sin(angle);
            double endX = dest.getX() - NODE_RADIUS * Math.cos(angle);
            double endY = dest.getY() - NODE_RADIUS * Math.sin(angle);

            // Draw line
            g2d.draw(new Line2D.Double(startX, startY, endX, endY));

            // Draw arrow head
            drawArrowHead(g2d, endX, endY, angle);

            // Draw weight label
            double midX = (startX + endX) / 2;
            double midY = (startY + endY) / 2;
            g2d.setColor(Color.RED);
            g2d.setFont(new Font("Arial", Font.BOLD, 12));
            String weightStr = String.format("%.1f", edge.getWeight());
            g2d.drawString(weightStr, (float)midX + 5, (float)midY - 5);
            g2d.setColor(EDGE_COLOR);
        }
    }

    private void drawArrowHead(Graphics2D g2d, double x, double y, double angle) {
        int arrowSize = 10;
        double angle1 = angle + Math.PI - Math.PI / 6;
        double angle2 = angle + Math.PI + Math.PI / 6;

        int x1 = (int)(x + arrowSize * Math.cos(angle1));
        int y1 = (int)(y + arrowSize * Math.sin(angle1));
        int x2 = (int)(x + arrowSize * Math.cos(angle2));
        int y2 = (int)(y + arrowSize * Math.sin(angle2));

        g2d.fillPolygon(new int[]{(int)x, x1, x2}, new int[]{(int)y, y1, y2}, 3);
    }

    private void drawNodes(Graphics2D g2d) {
        for (Node node : graph.getNodes()) {
            // Draw node circle
            g2d.setColor(NODE_COLOR);
            g2d.fillOval((int)(node.getX() - NODE_RADIUS),
                    (int)(node.getY() - NODE_RADIUS),
                    NODE_RADIUS * 2, NODE_RADIUS * 2);

            // Draw node border
            g2d.setColor(Color.BLACK);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawOval((int)(node.getX() - NODE_RADIUS),
                    (int)(node.getY() - NODE_RADIUS),
                    NODE_RADIUS * 2, NODE_RADIUS * 2);

            // Draw node label
            g2d.setColor(TEXT_COLOR);
            g2d.setFont(new Font("Arial", Font.BOLD, 16));
            String label = String.valueOf(node.getId());
            FontMetrics fm = g2d.getFontMetrics();
            int labelWidth = fm.stringWidth(label);
            int labelHeight = fm.getHeight();
            g2d.drawString(label,
                    (float)(node.getX() - labelWidth / 2),
                    (float)(node.getY() + labelHeight / 4));
        }
    }
}