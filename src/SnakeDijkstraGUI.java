import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.geom.Point2D;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;

public class SnakeDijkstraGUI extends JFrame {

    // ==========================================
    // 1. CLASS NODE (Logika)
    // ==========================================
    private static class Node {
        int id;
        int row, col;
        public Node(int id, int row, int col) {
            this.id = id;
            this.row = row;
            this.col = col;
        }
    }

    // ==========================================
    // 2. CLASS GRADIENT PANEL (Visual Kotak)
    // ==========================================
    private static class GradientPanel extends JPanel {
        private final Color centerColor;
        private final Color edgeColor;
        private boolean hasP1 = false;
        private boolean hasP2 = false;

        public GradientPanel(Color centerColor, Color edgeColor) {
            this.centerColor = centerColor;
            this.edgeColor = edgeColor;
            setOpaque(false);
        }

        public void setPlayerPresence(boolean p1, boolean p2) {
            this.hasP1 = p1;
            this.hasP2 = p2;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            // Background Gradient
            Point2D center = new Point2D.Float(w / 2.0f, h / 2.0f);
            float radius = Math.max(w, h);
            float[] dist = {0.0f, 1.0f};
            Color[] colors = {centerColor, edgeColor};
            RadialGradientPaint p = new RadialGradientPaint(center, radius, dist, colors);
            g2d.setPaint(p);
            g2d.fillRect(0, 0, w, h);

            // Logic Bidak Pemain
            int pawnSize = w / 3;
            int xP1 = (hasP2) ? (w / 2) - pawnSize - 4 : (w - pawnSize) / 2;
            int xP2 = (hasP1) ? (w / 2) + 4 : (w - pawnSize) / 2;
            int yPos = (h / 2) - (pawnSize / 2) + 5;

            if (hasP1) drawPawn(g2d, xP1, yPos, pawnSize, Color.decode("#FF5252"), "P1");
            if (hasP2) drawPawn(g2d, xP2, yPos, pawnSize, Color.decode("#448AFF"), "P2");
        }

        private void drawPawn(Graphics2D g2, int x, int y, int size, Color color, String label) {
            g2.setColor(new Color(0, 0, 0, 60)); // Shadow
            g2.fillOval(x + 3, y + 3, size, size);
            g2.setColor(color); // Body
            g2.fillOval(x, y, size, size);
            g2.setColor(Color.WHITE); // Border
            g2.setStroke(new BasicStroke(2.5f));
            g2.drawOval(x, y, size, size);

            g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
            g2.setColor(Color.WHITE);
            FontMetrics fm = g2.getFontMetrics();
            int tx = x + (size - fm.stringWidth(label)) / 2;
            int ty = y + (size - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(label, tx, ty);
        }
    }

    // ==========================================
    // 3. MAIN GUI CLASS
    // ==========================================

    private static final int SIZE = 8;
    private Node[][] logicBoard = new Node[SIZE][SIZE];
    private Map<Integer, GradientPanel> panelMap = new HashMap<>();

    // Struktur Data Game
    private Stack<Integer> p1Stack = new Stack<>();
    private Stack<Integer> p2Stack = new Stack<>();
    private Deque<Integer> turnQueue = new ArrayDeque<>();

    // Fitur Link/Tangga Acak
    private Map<Integer, Integer> shortcuts = new HashMap<>();

    private Random random = new Random();

    // Layout Components
    private JPanel mainContainer;
    private CardLayout cardLayout;
    private JPanel boardPanel;
    private JLabel statusLabel;
    private JLabel diceLabel;
    private JTextArea historyArea;
    private JButton rollButton;

    // Palette Warna
    private final Color blueCenter = Color.decode("#E3F2FD");
    private final Color blueEdge   = Color.decode("#BBDEFB");
    private final Color creamCenter = Color.decode("#FFFDE7");
    private final Color creamEdge   = Color.decode("#FFF9C4");
    private final Color sidebarColor = Color.decode("#2C3E50");
    private final Color accentColor  = Color.decode("#E67E22");

    public SnakeDijkstraGUI() {
        setTitle("Snake Game: Previous Position Prime Logic");
        setSize(1100, 850);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);

        mainContainer.add(createMenuPanel(), "MENU");
        mainContainer.add(createGamePanel(), "GAME");

        add(mainContainer);
        setLocationRelativeTo(null);
    }

    // --- HELPER: CEK BILANGAN PRIMA ---
    private boolean isPrime(int num) {
        if (num <= 1) return false;
        for (int i = 2; i <= Math.sqrt(num); i++) {
            if (num % i == 0) return false;
        }
        return true;
    }

    // --- PANEL KHUSUS MENGGAMBAR GARIS LINK ---
    private class BoardDrawingPanel extends JPanel {
        public BoardDrawingPanel(GridLayout layout) {
            super(layout);
        }

        @Override
        public void paint(Graphics g) {
            super.paint(g); // Gambar kotak

            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Gambar Garis Link
            // Karena syaratnya tergantung posisi player (dinamis),
            // Kita gambar semua link sebagai "Potential Link" (warna Ungu/Emas)
            for (Map.Entry<Integer, Integer> entry : shortcuts.entrySet()) {
                int startId = entry.getKey();
                int endId = entry.getValue();

                GradientPanel startPanel = panelMap.get(startId);
                GradientPanel endPanel = panelMap.get(endId);

                if (startPanel != null && endPanel != null) {
                    Point p1 = SwingUtilities.convertPoint(startPanel, startPanel.getWidth()/2, startPanel.getHeight()/2, this);
                    Point p2 = SwingUtilities.convertPoint(endPanel, endPanel.getWidth()/2, endPanel.getHeight()/2, this);

                    // Style Garis "Magic Link"
                    g2.setColor(new Color(138, 43, 226, 180)); // BlueViolet semi-transparent
                    g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawLine(p1.x, p1.y, p2.x, p2.y);

                    // Lingkaran ujung
                    int r = 5;
                    g2.setColor(Color.MAGENTA);
                    g2.fillOval(p1.x - r, p1.y - r, r*2, r*2);
                    g2.setColor(Color.CYAN);
                    g2.fillOval(p2.x - r, p2.y - r, r*2, r*2);
                }
            }
        }
    }

    // --- HALAMAN MENU ---
    private JPanel createMenuPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(sidebarColor);

        JLabel title = new JLabel("PRIME WALKER SNAKE");
        title.setFont(new Font("Segoe UI", Font.BOLD, 40));
        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel("<html><center>Link/Tangga hanya aktif jika posisi anda SEBELUM kocok dadu<br>adalah <b>BILANGAN PRIMA</b>.</center></html>");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        subtitle.setForeground(Color.LIGHT_GRAY);

        JButton startButton = styleButton("START NEW GAME", new Color(46, 204, 113));
        startButton.setPreferredSize(new Dimension(250, 60));

        startButton.addActionListener(e -> {
            initGameData();
            updatePlayerGraphics();
            updateInfoPanel();
            cardLayout.show(mainContainer, "GAME");
            boardPanel.repaint();
        });

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 10, 0);
        panel.add(title, gbc);
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 50, 0);
        panel.add(subtitle, gbc);
        gbc.gridy = 2;
        panel.add(startButton, gbc);

        return panel;
    }

    // --- HALAMAN GAME ---
    private JPanel createGamePanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // A. Papan Permainan (Custom Drawing Panel)
        boardPanel = new BoardDrawingPanel(new GridLayout(SIZE, SIZE));
        boardPanel.setBorder(new LineBorder(sidebarColor, 5));
        initializeLogicBoard();
        initializeVisualBoard(boardPanel);

        // B. Sidebar
        JPanel sidePanel = new JPanel();
        sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.Y_AXIS));
        sidePanel.setPreferredSize(new Dimension(300, 0));
        sidePanel.setBorder(new EmptyBorder(30, 20, 30, 20));
        sidePanel.setBackground(sidebarColor);

        statusLabel = new JLabel("PLAYER 1 TURN");
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        diceLabel = new JLabel("Roll the dice!");
        diceLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        diceLabel.setForeground(Color.LIGHT_GRAY);
        diceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        rollButton = styleButton("ROLL DICE", accentColor);
        rollButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        rollButton.setMaximumSize(new Dimension(250, 60));
        rollButton.addActionListener(e -> playTurn());

        historyArea = new JTextArea(12, 1);
        historyArea.setEditable(false);
        historyArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        historyArea.setBackground(new Color(44, 62, 80));
        historyArea.setForeground(new Color(46, 204, 113));
        historyArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JScrollPane scrollStack = new JScrollPane(historyArea);
        scrollStack.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY), "Game Log",
                0, 0, new Font("Segoe UI", Font.BOLD, 12), Color.WHITE));
        scrollStack.setOpaque(false);
        scrollStack.getViewport().setOpaque(false);
        scrollStack.setAlignmentX(Component.CENTER_ALIGNMENT);

        sidePanel.add(statusLabel);
        sidePanel.add(Box.createRigidArea(new Dimension(0, 10)));
        sidePanel.add(diceLabel);
        sidePanel.add(Box.createRigidArea(new Dimension(0, 30)));
        sidePanel.add(rollButton);
        sidePanel.add(Box.createRigidArea(new Dimension(0, 30)));
        sidePanel.add(scrollStack);

        panel.add(boardPanel, BorderLayout.CENTER);
        panel.add(sidePanel, BorderLayout.EAST);

        return panel;
    }

    private JButton styleButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 18));
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setContentAreaFilled(false);
        btn.setOpaque(true);
        return btn;
    }

    // --- GAME DATA & LOGIC ---

    private void initGameData() {
        p1Stack.clear(); p1Stack.push(1);
        p2Stack.clear(); p2Stack.push(1);
        turnQueue.clear();
        turnQueue.add(1);
        turnQueue.add(2);
        rollButton.setEnabled(true);
        generateRandomLinks();
    }

    private void generateRandomLinks() {
        shortcuts.clear();
        while (shortcuts.size() < 5) {
            int start = random.nextInt(62) + 2;
            int end = random.nextInt(62) + 2;

            if (start != end && !shortcuts.containsKey(start) && !shortcuts.containsValue(start)) {
                shortcuts.put(start, end);
            }
        }
        System.out.println("Links Generated: " + shortcuts);
    }

    private void initializeLogicBoard() {
        int idCounter = 1;
        for (int r = SIZE - 1; r >= 0; r--) {
            boolean leftToRight = (SIZE - 1 - r) % 2 == 0;
            if (leftToRight) {
                for (int c = 0; c < SIZE; c++) {
                    logicBoard[r][c] = new Node(idCounter++, r, c);
                }
            } else {
                for (int c = SIZE - 1; c >= 0; c--) {
                    logicBoard[r][c] = new Node(idCounter++, r, c);
                }
            }
        }
    }

    private void initializeVisualBoard(JPanel boardPanel) {
        boardPanel.removeAll();
        panelMap.clear();

        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                Node node = logicBoard[r][c];
                Color cCenter = ((r + c) % 2 == 0) ? blueCenter : creamCenter;
                Color cEdge = ((r + c) % 2 == 0) ? blueEdge : creamEdge;

                GradientPanel cell = new GradientPanel(cCenter, cEdge);
                cell.setLayout(new BorderLayout());
                cell.setBorder(new MatteBorder(1, 1, 1, 1, Color.WHITE));

                JLabel numLabel = new JLabel(String.valueOf(node.id));
                numLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
                numLabel.setForeground(new Color(80, 80, 80));
                numLabel.setHorizontalAlignment(SwingConstants.RIGHT);
                numLabel.setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 8));

                cell.add(numLabel, BorderLayout.NORTH);
                boardPanel.add(cell);
                panelMap.put(node.id, cell);
            }
        }
    }

    private void playTurn() {
        if (turnQueue.isEmpty()) return;

        int currentPlayer = turnQueue.pollFirst();

        // 1. Ambil Posisi SEBELUM kocok dadu (Previous Position)
        Stack<Integer> currentStack = (currentPlayer == 1) ? p1Stack : p2Stack;
        int previousPos = currentStack.peek();

        // Cek apakah posisi awal adalah Prima (Untuk syarat Link nanti)
        boolean startIsPrime = isPrime(previousPos);

        // 2. Roll Dice Logic
        double chance = random.nextDouble();
        boolean isGreen = chance < 0.7;
        int diceValue = random.nextInt(6) + 1;
        int steps = isGreen ? diceValue : -diceValue;

        // UI Dadu
        String hexColor = isGreen ? "#2ECC71" : "#E74C3C";
        String direction = isGreen ? "MAJU" : "MUNDUR";
        diceLabel.setText("<html><center><span style='font-size:12px; color:white;'>Hasil: " + direction + "</span><br>" +
                "<span style='font-size:24px; color:" + hexColor + "; font-weight:bold;'>" + diceValue + "</span></center></html>");

        // 3. Kalkulasi Posisi Baru
        int newPos = calculateNewPosition(previousPos, steps);

        String logMsg = "P" + currentPlayer + ": " + previousPos + " -> " + newPos;

        // 4. LOGIKA LINK & SYARAT BILANGAN PRIMA (FIXED)
        if (shortcuts.containsKey(newPos)) {
            // Kita cek 'previousPos' (posisi awal pemain)
            if (startIsPrime) {
                // Syarat Terpenuhi
                int jumpDest = shortcuts.get(newPos);
                logMsg += " (ACTIVE LINK -> " + jumpDest + ")";
                JOptionPane.showMessageDialog(this,
                        "Shortest Path Activated!\nAnda berangkat dari angka PRIMA (" + previousPos + ").\nLink aktif menuju " + jumpDest);
                newPos = jumpDest;
            } else {
                // Syarat Gagal
                logMsg += " (Link Locked: Start not Prime)";
                JOptionPane.showMessageDialog(this,
                        "Link ditemukan di " + newPos + ".\nTapi terkunci karena anda berangkat dari " + previousPos + " (Bukan Prima).",
                        "Link Locked", JOptionPane.WARNING_MESSAGE);
            }
        }

        // Push ke Stack
        currentStack.push(newPos);

        // Update Visual
        historyArea.append(logMsg + "\n");
        updatePlayerGraphics();
        updateInfoPanel();

        // 5. Cek Menang
        if (newPos == 64) {
            JOptionPane.showMessageDialog(this, "PLAYER " + currentPlayer + " WINS!");
            rollButton.setEnabled(false);
            statusLabel.setText("WINNER: P" + currentPlayer);
            statusLabel.setForeground(Color.YELLOW);
            return;
        }

        // 6. Aturan Kelipatan 5 (Double Turn)
        if (newPos % 5 == 0 && newPos != 1) {
            JOptionPane.showMessageDialog(this, "Kelipatan 5! Double Turn untuk P" + currentPlayer);
            turnQueue.addFirst(currentPlayer);
        } else {
            turnQueue.addLast(currentPlayer);
        }

        int nextPlayer = turnQueue.peekFirst();
        statusLabel.setText("PLAYER " + nextPlayer + " TURN");
        statusLabel.setForeground(nextPlayer == 1 ? Color.decode("#FF5252") : Color.decode("#448AFF"));
    }

    private int calculateNewPosition(int currentPos, int steps) {
        int newPos = currentPos + steps;
        if (newPos < 1) return 1;
        if (newPos > 64) {
            int surplus = newPos - 64;
            newPos = 64 - surplus;
        }
        return newPos;
    }

    private void updatePlayerGraphics() {
        for (GradientPanel panel : panelMap.values()) {
            panel.setPlayerPresence(false, false);
        }
        int posP1 = p1Stack.peek();
        int posP2 = p2Stack.peek();

        GradientPanel p1Panel = panelMap.get(posP1);
        GradientPanel p2Panel = panelMap.get(posP2);

        if (p1Panel != null) {
            boolean p2IsHere = (posP1 == posP2);
            p1Panel.setPlayerPresence(true, p2IsHere);
        }
        if (p2Panel != null && posP1 != posP2) {
            p2Panel.setPlayerPresence(false, true);
        }
    }

    private void updateInfoPanel() {
        // Optional
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            new SnakeDijkstraGUI().setVisible(true);
        });
    }
}