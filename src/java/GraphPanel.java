import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList; // IMPORT BARU
import java.util.HashSet;   // IMPORT BARU
import java.util.Set;       // IMPORT BARU

class GraphPanel extends JPanel {
    private Graph graph;
    private Node draggedNode = null;

    // FIELD BARU: Untuk menyimpan rute terpendek
    private ArrayList<Edge> shortestPath = null;
    private Set<Node> pathNodes = new HashSet<>();

    public GraphPanel(Graph graph) {
        this.graph = graph;
        setPreferredSize(new Dimension(1000, 700));
        setBackground(Color.WHITE);

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

    /**
     * METHOD BARU: Untuk menerima hasil Dijkstra dan memicu repaint
     */
    public void setShortestPath(ArrayList<Edge> path) {
        this.shortestPath = path;
        this.pathNodes.clear();

        if (path != null) {
            for (Edge edge : path) {
                pathNodes.add(edge.getSource());
                pathNodes.add(edge.getTarget());
            }
        }
        repaint(); // Gambar ulang panel dengan rute baru
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // --- MODIFIKASI GAMBAR EDGE ---
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2));

        for (Edge edge : graph.getEdges()) {
            Node source = edge.getSource();
            Node target = edge.getTarget();

            // Cek apakah edge ini bagian dari rute terpendek
            if (shortestPath != null && shortestPath.contains(edge)) {
                g2d.setColor(Color.RED);
                g2d.setStroke(new BasicStroke(4)); // Garis lebih tebal
            } else {
                g2d.setColor(Color.GRAY); // Ubah edge non-path jadi abu-abu
                g2d.setStroke(new BasicStroke(2));
            }

            // Draw line
            g2d.drawLine(source.getX(), source.getY(), target.getX(), target.getY());

            // Draw arrow (akan menggunakan warna RED jika bagian dari path)
            drawArrow(g2d, source.getX(), source.getY(), target.getX(), target.getY());

            // Draw weight
            int midX = (source.getX() + target.getX()) / 2;
            int midY = (source.getY() + target.getY()) / 2;
            g2d.setColor(Color.BLACK); // Warna teks bobot selalu hitam
            g2d.setFont(new Font("Arial", Font.BOLD, 14));
            g2d.drawString(String.valueOf(edge.getWeight()), midX + 5, midY - 5);
        }

        // Reset warna default
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(1));
        // --- AKHIR MODIFIKASI EDGE ---


        // --- MODIFIKASI GAMBAR NODE ---
        for (Node node : graph.getNodes()) {
            g2d.setColor(Color.WHITE);
            g2d.fillOval(node.getX() - node.getRadius(),
                    node.getY() - node.getRadius(),
                    node.getRadius() * 2,
                    node.getRadius() * 2);

            // Cek apakah node ini bagian dari rute terpendek
            if (pathNodes.contains(node)) {
                g2d.setColor(Color.RED);
                g2d.setStroke(new BasicStroke(4)); // Outline lebih tebal
            } else {
                g2d.setColor(Color.BLACK);
                g2d.setStroke(new BasicStroke(2));
            }

            g2d.drawOval(node.getX() - node.getRadius(),
                    node.getY() - node.getRadius(),
                    node.getRadius() * 2,
                    node.getRadius() * 2);

            // Draw node label
            g2d.setColor(Color.BLACK); // Warna label selalu hitam
            g2d.setFont(new Font("Arial", Font.BOLD, 14));
            String label = graph.getLabel()[node.getId()];
            FontMetrics fm = g2d.getFontMetrics();
            int labelX = node.getX() - fm.stringWidth(label) / 2;
            int labelY = node.getY() + fm.getAscent() / 2;
            g2d.drawString(label, labelX, labelY);
        }
        // --- AKHIR MODIFIKASI NODE ---
    }

    private void drawArrow(Graphics2D g2d, int x1, int y1, int x2, int y2) {
        double angle = Math.atan2(y2 - y1, x2 - x1);
        int arrowSize = 10;

        // Pindahkan panah ke outline lingkaran (radius 30)
        int arrowX = x2 - (int)(30 * Math.cos(angle));
        int arrowY = y2 - (int)(30 * Math.sin(angle));

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