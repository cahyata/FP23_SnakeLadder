import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.geom.Point2D;
import java.util.*;
import java.util.List;
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

        // Menyimpan daftar ID pemain yang ada di kotak ini (cth: [1, 3])
        private List<Integer> playersHere = new ArrayList<>();

        public GradientPanel(Color centerColor, Color edgeColor) {
            this.centerColor = centerColor;
            this.edgeColor = edgeColor;
            setOpaque(false);
        }

        public void setPlayersHere(List<Integer> players) {
            this.playersHere.clear();
            this.playersHere.addAll(players);
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

            // --- Logic Menggambar Banyak Bidak ---
            // Kita bagi kotak menjadi 4 kuadran agar muat 4 pemain
            int pawnSize = w / 3;
            int padding = 4;

            // Koordinat Kuadran
            // P1: Kiri Atas
            int x1 = (w / 2) - pawnSize - padding;
            int y1 = (h / 2) - pawnSize - padding + 10;

            // P2: Kanan Atas
            int x2 = (w / 2) + padding;
            int y2 = (h / 2) - pawnSize - padding + 10;

            // P3: Kiri Bawah
            int x3 = (w / 2) - pawnSize - padding;
            int y3 = (h / 2) + padding + 5;

            // P4: Kanan Bawah
            int x4 = (w / 2) + padding;
            int y4 = (h / 2) + padding + 5;

            for (int playerId : playersHere) {
                switch (playerId) {
                    case 1: // Merah
                        drawPawn(g2d, x1, y1, pawnSize, Color.decode("#FF5252"), "P1");
                        break;
                    case 2: // Biru
                        drawPawn(g2d, x2, y2, pawnSize, Color.decode("#448AFF"), "P2");
                        break;
                    case 3: // Hijau
                        drawPawn(g2d, x3, y3, pawnSize, Color.decode("#69F0AE"), "P3");
                        break;
                    case 4: // Oranye
                        drawPawn(g2d, x4, y4, pawnSize, Color.decode("#FFAB40"), "P4");
                        break;
                }
            }
        }

        private void drawPawn(Graphics2D g2, int x, int y, int size, Color color, String label) {
            g2.setColor(new Color(0, 0, 0, 60)); // Shadow
            g2.fillOval(x + 2, y + 2, size, size);
            g2.setColor(color); // Body
            g2.fillOval(x, y, size, size);
            g2.setColor(Color.WHITE); // Border
            g2.setStroke(new BasicStroke(2f));
            g2.drawOval(x, y, size, size);

            g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
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

    // --- Struktur Data Dinamis (Mendukung N Player) ---
    private int playerCount = 2; // Default
    private List<Stack<Integer>> allPlayerStacks = new ArrayList<>(); // List of Stacks
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

    // Warna Teks Player untuk Status Label
    private final Color[] playerTextColors = {
            Color.decode("#FF5252"), // P1 Red
            Color.decode("#448AFF"), // P2 Blue
            Color.decode("#69F0AE"), // P3 Green
            Color.decode("#FFAB40")  // P4 Orange
    };

    public SnakeDijkstraGUI() {
        setTitle("Snake Game: Multi-Player & Prime Logic");
        setSize(1150, 900);
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

            for (Map.Entry<Integer, Integer> entry : shortcuts.entrySet()) {
                int startId = entry.getKey();
                int endId = entry.getValue();

                GradientPanel startPanel = panelMap.get(startId);
                GradientPanel endPanel = panelMap.get(endId);

                if (startPanel != null && endPanel != null) {
                    Point p1 = SwingUtilities.convertPoint(startPanel, startPanel.getWidth()/2, startPanel.getHeight()/2, this);
                    Point p2 = SwingUtilities.convertPoint(endPanel, endPanel.getWidth()/2, endPanel.getHeight()/2, this);

                    g2.setColor(new Color(138, 43, 226, 150)); // Violet transparan
                    g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawLine(p1.x, p1.y, p2.x, p2.y);

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
            // 1. TANYA JUMLAH PEMAIN
            String[] options = {"2 Players", "3 Players", "4 Players"};
            int choice = JOptionPane.showOptionDialog(this,
                    "Pilih Jumlah Pemain:",
                    "Setup Game",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null, options, options[0]);

            if (choice != -1) {
                // choice 0 -> 2 pemain, 1 -> 3 pemain, dst.
                playerCount = choice + 2;

                // 2. Init Data
                initGameData();
                updatePlayerGraphics();
                updateInfoPanel();

                // 3. Pindah Layar
                cardLayout.show(mainContainer, "GAME");
                boardPanel.repaint();
            }
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

        // A. Papan Permainan
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
        statusLabel.setForeground(playerTextColors[0]);
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
        // Reset Stacks untuk N pemain
        allPlayerStacks.clear();
        for (int i = 0; i < playerCount; i++) {
            Stack<Integer> s = new Stack<>();
            s.push(1); // Semua mulai di 1
            allPlayerStacks.add(s);
        }

        // Reset Queue Giliran
        turnQueue.clear();
        for (int i = 1; i <= playerCount; i++) {
            turnQueue.add(i);
        }

        rollButton.setEnabled(true);
        statusLabel.setText("PLAYER 1 TURN");
        statusLabel.setForeground(playerTextColors[0]);

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

        // 1. Ambil Stack Pemain (Index = currentPlayer - 1)
        Stack<Integer> currentStack = allPlayerStacks.get(currentPlayer - 1);
        int previousPos = currentStack.peek();

        boolean startIsPrime = isPrime(previousPos);

        // 2. Roll Dice
        double chance = random.nextDouble();
        boolean isGreen = chance < 0.7;
        int diceValue = random.nextInt(6) + 1;
        int steps = isGreen ? diceValue : -diceValue;

        // UI Dadu
        String hexColor = isGreen ? "#2ECC71" : "#E74C3C";
        String direction = isGreen ? "MAJU" : "MUNDUR";
        diceLabel.setText("<html><center><span style='font-size:12px; color:white;'>Hasil: " + direction + "</span><br>" +
                "<span style='font-size:24px; color:" + hexColor + "; font-weight:bold;'>" + diceValue + "</span></center></html>");

        // 3. Kalkulasi Posisi
        int newPos = calculateNewPosition(previousPos, steps);

        String logMsg = "P" + currentPlayer + ": " + previousPos + " -> " + newPos;

        // 4. LOGIKA LINK
        if (shortcuts.containsKey(newPos)) {
            if (startIsPrime) {
                int jumpDest = shortcuts.get(newPos);
                logMsg += " (ACTIVE LINK -> " + jumpDest + ")";
                JOptionPane.showMessageDialog(this,
                        "PRIME JUMP!\nBerangkat dari " + previousPos + " (Prima).\nLink aktif ke " + jumpDest);
                newPos = jumpDest;
            } else {
                logMsg += " (Link Locked)";
                JOptionPane.showMessageDialog(this,
                        "Link ditemukan di " + newPos + ".\nTapi terkunci karena berangkat dari " + previousPos + " (Bukan Prima).",
                        "Link Locked", JOptionPane.WARNING_MESSAGE);
            }
        }

        currentStack.push(newPos);

        // Update Visual
        historyArea.append(logMsg + "\n");
        updatePlayerGraphics();

        // 5. Cek Menang
        if (newPos == 64) {
            JOptionPane.showMessageDialog(this, "PLAYER " + currentPlayer + " WINS!");
            rollButton.setEnabled(false);
            statusLabel.setText("WINNER: P" + currentPlayer);
            statusLabel.setForeground(Color.YELLOW);
            return;
        }

        // 6. Aturan Double Turn
        if (newPos % 5 == 0 && newPos != 1) {
            JOptionPane.showMessageDialog(this, "Kelipatan 5! Double Turn untuk P" + currentPlayer);
            turnQueue.addFirst(currentPlayer);
        } else {
            turnQueue.addLast(currentPlayer);
        }

        // Update Status Giliran Berikutnya
        int nextPlayer = turnQueue.peekFirst();
        statusLabel.setText("PLAYER " + nextPlayer + " TURN");
        // Pakai warna sesuai player (Safe index check)
        int colorIdx = (nextPlayer - 1) % playerTextColors.length;
        statusLabel.setForeground(playerTextColors[colorIdx]);
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
        // Reset semua kotak
        for (GradientPanel panel : panelMap.values()) {
            panel.setPlayersHere(new ArrayList<>());
        }

        // Kumpulkan posisi semua pemain
        // Map: Posisi ID -> List of Player IDs yang ada disitu
        Map<Integer, List<Integer>> positions = new HashMap<>();

        for (int i = 0; i < playerCount; i++) {
            int pId = i + 1;
            int pos = allPlayerStacks.get(i).peek();

            positions.putIfAbsent(pos, new ArrayList<>());
            positions.get(pos).add(pId);
        }

        // Update Panel
        for (Map.Entry<Integer, List<Integer>> entry : positions.entrySet()) {
            int pos = entry.getKey();
            List<Integer> players = entry.getValue();

            GradientPanel panel = panelMap.get(pos);
            if (panel != null) {
                panel.setPlayersHere(players);
            }
        }
    }

    private void updateInfoPanel() {
        // Optional refresh
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