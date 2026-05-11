package studybuddy;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Map;

public class StudySessionUI extends JFrame {

    private StudySessionsController sessionController;
    private StatsController         statsController;
    private StudyBuddyContext       context;
    private String                  currentUserId;

    private CardLayout cardLayout;
    private JPanel     cardPanel;
    private JLabel[]   navIcons = new JLabel[4];
    private JLabel[]   navTexts = new JLabel[4];
    private static final String[] SCREENS = {"sessions", "timer", "stats", "subjects"};

    private DefaultTableModel     sessionTableModel;
    private JTable                sessionTable;

    private JComboBox<String>     timerSubjectCombo;
    private JLabel                timerLabel;
    private JButton               btnStart;
    private JButton               btnStop;
    private Timer                 swingTimer;
    private int                   elapsedSeconds = 0;

    private JComboBox<String>     manualSubjectCombo;
    private JSpinner              manualDurationSpinner;

    private JLabel                lblTotal;
    private JLabel                lblAvg;
    private JLabel                lblCount;
    private JLabel                lblBest;
    private JPanel                barsPanel;

    private DefaultListModel<String> subjectListModel;
    private JTextField            newSubjectField;

    private static final Color BG       = new Color(248, 248, 250);
    private static final Color SURFACE  = Color.WHITE;
    private static final Color ACCENT   = new Color(99, 102, 241);
    private static final Color ACCENT_L = new Color(99, 102, 241, 18);
    private static final Color OK       = new Color(34, 197, 94);
    private static final Color ERR      = new Color(239, 68, 68);
    private static final Color TEXT     = new Color(17, 24, 39);
    private static final Color TEXT2    = new Color(107, 114, 128);
    private static final Color SEP      = new Color(229, 231, 235);
    private static final Color NAV_OFF  = new Color(189, 189, 189);

    private static String FONT = "SansSerif";

    static {
        java.util.Set<String> fs = new java.util.HashSet<>(java.util.Arrays.asList(
            GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()));
        if (fs.contains("Segoe UI"))       FONT = "Segoe UI";
        if (fs.contains("Helvetica Neue")) FONT = "Helvetica Neue";
        if (fs.contains("SF Pro Display")) FONT = "SF Pro Display";
    }

    public StudySessionUI(StudySessionsController sessionController,
                          StatsController         statsController,
                          StudyBuddyContext       context,
                          String                  currentUserId) {
        this.sessionController = sessionController;
        this.statsController   = statsController;
        this.context           = context;
        this.currentUserId     = currentUserId;
        initFrame();
        buildUI();
        refreshAll();
        setVisible(true);
    }

    private void initFrame() {
        setTitle("StudyBuddy");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(390, 760);
        setMinimumSize(new Dimension(360, 640));
        setLocationRelativeTo(null);

        try { UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()); }
        catch (Exception ignored) {}
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG);

        cardLayout = new CardLayout();
        cardPanel  = new JPanel(cardLayout);
        cardPanel.setBackground(BG);
        cardPanel.add(buildSessionsTab(),  "sessions");
        cardPanel.add(buildTimerTab(),     "timer");
        cardPanel.add(buildStatsTab(),     "stats");
        cardPanel.add(buildSubjectsTab(),  "subjects");

        root.add(cardPanel,       BorderLayout.CENTER);
        root.add(buildBottomNav(), BorderLayout.SOUTH);
        setContentPane(root);
    }

    // ----------------------------------------------------------------
    //  Bottom nav
    // ----------------------------------------------------------------
    private JPanel buildBottomNav() {
        JPanel bar = new JPanel(new GridLayout(1, 4));
        bar.setBackground(SURFACE);
        bar.setBorder(new MatteBorder(1, 0, 0, 0, SEP));
        bar.setPreferredSize(new Dimension(0, 62));

        String[] icons  = {"☰", "◷", "≈", "⊞"};
        String[] labels = {"Seje", "Timer", "Statistika", "Predmeti"};

        for (int i = 0; i < 4; i++) {
            final int idx = i;

            JLabel icon = new JLabel(icons[i], SwingConstants.CENTER);
            icon.setFont(new Font(FONT, Font.PLAIN, 22));
            icon.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel txt = new JLabel(labels[i], SwingConstants.CENTER);
            txt.setFont(new Font(FONT, Font.PLAIN, 10));
            txt.setAlignmentX(Component.CENTER_ALIGNMENT);

            JPanel item = new JPanel();
            item.setLayout(new BoxLayout(item, BoxLayout.Y_AXIS));
            item.setBackground(SURFACE);
            item.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            item.add(Box.createVerticalGlue());
            item.add(icon);
            item.add(Box.createVerticalStrut(2));
            item.add(txt);
            item.add(Box.createVerticalGlue());
            item.addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) { switchTo(idx); }
            });

            navIcons[i] = icon;
            navTexts[i] = txt;
            bar.add(item);
        }
        switchTo(0);
        return bar;
    }

    private void switchTo(int idx) {
        cardLayout.show(cardPanel, SCREENS[idx]);
        for (int i = 0; i < 4; i++) {
            boolean on = (i == idx);
            navIcons[i].setForeground(on ? ACCENT : NAV_OFF);
            navTexts[i].setForeground(on ? ACCENT : NAV_OFF);
        }
    }

    // ================================================================
    //  Screen: Seje
    // ================================================================
    private JPanel buildSessionsTab() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(BG);
        p.add(pageHeader("Moje seje"), BorderLayout.NORTH);

        String[] cols = {"#", "Predmet", "Datum", "Trajanje"};
        sessionTableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        sessionTable = new JTable(sessionTableModel) {
            @Override public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                if (!isRowSelected(row)) c.setBackground(row % 2 == 0 ? SURFACE : BG);
                else c.setBackground(ACCENT_L);
                return c;
            }
        };
        sessionTable.setRowHeight(42);
        sessionTable.setFont(new Font(FONT, Font.PLAIN, 13));
        sessionTable.setShowGrid(false);
        sessionTable.setIntercellSpacing(new Dimension(0, 0));
        sessionTable.setFillsViewportHeight(true);
        sessionTable.setSelectionBackground(ACCENT_L);
        sessionTable.setSelectionForeground(TEXT);

        JTableHeader th = sessionTable.getTableHeader();
        th.setFont(new Font(FONT, Font.BOLD, 11));
        th.setBackground(BG);
        th.setForeground(TEXT2);
        th.setBorder(new MatteBorder(0, 0, 1, 0, SEP));
        th.setReorderingAllowed(false);

        // Hide ID column
        TableColumn idCol = sessionTable.getColumnModel().getColumn(0);
        idCol.setMinWidth(0); idCol.setMaxWidth(0); idCol.setWidth(0);
        sessionTable.getColumnModel().getColumn(3).setMaxWidth(100);

        JScrollPane scroll = new JScrollPane(sessionTable);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(SURFACE);
        p.add(scroll, BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(BG);
        footer.setBorder(new EmptyBorder(10, 16, 14, 16));
        JButton btnDelete = actionButton("Izbriši izbrano", ERR);
        btnDelete.addActionListener(e -> deleteSelectedSession());
        footer.add(btnDelete, BorderLayout.CENTER);
        p.add(footer, BorderLayout.SOUTH);

        return p;
    }

    // ================================================================
    //  Screen: Timer
    // ================================================================
    private JPanel buildTimerTab() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(BG);
        p.add(pageHeader("Timer"), BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(BG);
        body.setBorder(new EmptyBorder(10, 0, 10, 0));

        // Timer card
        JPanel timerCard = card();
        timerCard.add(chipLabel("PREDMET"));
        timerCard.add(Box.createVerticalStrut(8));
        timerSubjectCombo = combo();
        timerSubjectCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        timerCard.add(timerSubjectCombo);
        timerCard.add(Box.createVerticalStrut(20));

        timerLabel = new JLabel("00:00", SwingConstants.CENTER);
        timerLabel.setFont(new Font(FONT, Font.PLAIN, 64));
        timerLabel.setForeground(TEXT);
        timerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        timerCard.add(timerLabel);
        timerCard.add(Box.createVerticalStrut(20));

        JPanel btnRow = new JPanel(new GridLayout(1, 2, 10, 0));
        btnRow.setOpaque(false);
        btnRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btnRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnStart = actionButton("▶  Start", OK);
        btnStop  = actionButton("■  Stop & Shrani", ERR);
        btnStop.setEnabled(false);
        btnStart.addActionListener(e -> startTimer());
        btnStop.addActionListener(e -> stopTimer());
        btnRow.add(btnStart);
        btnRow.add(btnStop);
        timerCard.add(btnRow);

        body.add(timerCard);
        body.add(Box.createVerticalStrut(8));

        // Manual entry card
        JPanel manCard = card();
        manCard.add(chipLabel("ROČNI VNOS"));
        manCard.add(Box.createVerticalStrut(10));
        manualSubjectCombo = combo();
        manualSubjectCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        manCard.add(manualSubjectCombo);
        manCard.add(Box.createVerticalStrut(8));

        JPanel durRow = new JPanel();
        durRow.setOpaque(false);
        durRow.setLayout(new BoxLayout(durRow, BoxLayout.X_AXIS));
        durRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        durRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));

        manualDurationSpinner = new JSpinner(new SpinnerNumberModel(30, 1, 600, 5));
        manualDurationSpinner.setFont(new Font(FONT, Font.PLAIN, 13));
        manualDurationSpinner.setMaximumSize(new Dimension(80, 38));
        styleSpinner(manualDurationSpinner);

        JLabel minLbl = new JLabel(" min");
        minLbl.setFont(new Font(FONT, Font.PLAIN, 13));
        minLbl.setForeground(TEXT2);

        JButton btnManual = actionButton("Dodaj sejo", ACCENT);
        btnManual.addActionListener(e -> createManualSession());

        durRow.add(manualDurationSpinner);
        durRow.add(minLbl);
        durRow.add(Box.createHorizontalGlue());
        durRow.add(btnManual);
        manCard.add(durRow);

        body.add(manCard);

        p.add(body, BorderLayout.CENTER);
        return p;
    }

    // ================================================================
    //  Screen: Statistika
    // ================================================================
    private JPanel buildStatsTab() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(BG);
        p.add(pageHeader("Statistika"), BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(BG);
        body.setBorder(new EmptyBorder(16, 16, 16, 16));

        JPanel grid = new JPanel(new GridLayout(2, 2, 10, 10));
        grid.setOpaque(false);
        grid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 140));
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblTotal = metricCard(grid, "Skupaj minut", "0");
        lblAvg   = metricCard(grid, "Povprečje/sejo", "0");
        lblCount = metricCard(grid, "Število sej", "0");
        lblBest  = metricCard(grid, "Najboljši", "—");
        body.add(grid);
        body.add(Box.createVerticalStrut(16));

        JLabel chartLbl = chipLabel("PO PREDMETIH");
        chartLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(chartLbl);
        body.add(Box.createVerticalStrut(10));

        barsPanel = new JPanel();
        barsPanel.setBackground(BG);
        barsPanel.setLayout(new BoxLayout(barsPanel, BoxLayout.Y_AXIS));
        barsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(barsPanel);

        JScrollPane scroll = new JScrollPane(body);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(BG);
        p.add(scroll, BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(BG);
        footer.setBorder(new EmptyBorder(8, 16, 14, 16));
        JButton btnRefresh = actionButton("Osveži statistike", ACCENT);
        btnRefresh.addActionListener(e -> showStats());
        footer.add(btnRefresh, BorderLayout.CENTER);
        p.add(footer, BorderLayout.SOUTH);

        return p;
    }

    // ================================================================
    //  Screen: Predmeti
    // ================================================================
    private JPanel buildSubjectsTab() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(BG);
        p.add(pageHeader("Predmeti"), BorderLayout.NORTH);

        subjectListModel = new DefaultListModel<>();
        JList<String> list = new JList<>(subjectListModel);
        list.setFont(new Font(FONT, Font.PLAIN, 14));
        list.setBackground(SURFACE);
        list.setSelectionBackground(ACCENT_L);
        list.setSelectionForeground(TEXT);
        list.setFixedCellHeight(46);
        list.setBorder(new EmptyBorder(4, 20, 4, 20));
        list.setCellRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> l, Object v,
                    int i, boolean sel, boolean foc) {
                super.getListCellRendererComponent(l, v, i, sel, foc);
                setBorder(new EmptyBorder(0, 0, 0, 0));
                setFont(new Font(FONT, Font.PLAIN, 14));
                if (!sel) setBackground(i % 2 == 0 ? SURFACE : BG);
                return this;
            }
        });

        JScrollPane scroll = new JScrollPane(list);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(SURFACE);
        p.add(scroll, BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout(10, 0));
        footer.setBackground(BG);
        footer.setBorder(new EmptyBorder(10, 16, 14, 16));
        newSubjectField = field("Ime predmeta...");
        JButton btnAdd = actionButton("Dodaj", ACCENT);
        btnAdd.addActionListener(e -> addSubject());
        newSubjectField.addActionListener(e -> addSubject());
        footer.add(newSubjectField, BorderLayout.CENTER);
        footer.add(btnAdd, BorderLayout.EAST);
        p.add(footer, BorderLayout.SOUTH);

        return p;
    }

    // ================================================================
    //  Public boundary methods
    // ================================================================
    public void showSessionForm() { switchTo(1); }

    public void showSessionList() {
        sessionTableModel.setRowCount(0);
        List<StudySession> sessions = sessionController.index();
        sessions.sort((a, b) -> b.getStartTime().compareTo(a.getStartTime()));
        for (StudySession s : sessions) {
            Subject sub  = context.findSubject(s.getSubjectId());
            String  name = (sub != null) ? sub.getName() : "—";
            sessionTableModel.addRow(new Object[]{
                s.getStudySessionId(), name, s.getFormattedDate(),
                s.getDurationMinutes() + " min"
            });
        }
    }

    public void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Napaka", JOptionPane.ERROR_MESSAGE);
    }

    public void showStats() {
        StatsResult stats = statsController.getStats();
        lblTotal.setText(String.valueOf(stats.getTotalMinutes()));
        lblAvg.setText(String.valueOf(stats.getAvgMinutes()));
        lblCount.setText(String.valueOf(stats.getSessionCount()));
        lblBest.setText(stats.getBestSubject());

        barsPanel.removeAll();
        Map<String, Integer> by = stats.getBySubject();
        if (by.isEmpty()) {
            JLabel empty = new JLabel("Ni podatkov.");
            empty.setFont(new Font(FONT, Font.PLAIN, 13));
            empty.setForeground(TEXT2);
            barsPanel.add(empty);
        } else {
            int max = by.values().stream().mapToInt(v -> v).max().orElse(1);
            for (Map.Entry<String, Integer> e : by.entrySet()) {
                barsPanel.add(buildBarRow(e.getKey(), e.getValue(), max));
                barsPanel.add(Box.createVerticalStrut(12));
            }
        }
        barsPanel.revalidate();
        barsPanel.repaint();
    }

    // ================================================================
    //  Timer
    // ================================================================
    private void startTimer() {
        elapsedSeconds = 0;
        btnStart.setEnabled(false);
        btnStop.setEnabled(true);
        swingTimer = new Timer(1000, e -> {
            elapsedSeconds++;
            timerLabel.setText(String.format("%02d:%02d", elapsedSeconds / 60, elapsedSeconds % 60));
        });
        swingTimer.start();
    }

    private void stopTimer() {
        if (swingTimer != null) swingTimer.stop();
        btnStart.setEnabled(true);
        btnStop.setEnabled(false);
        timerLabel.setText("00:00");

        int minutes = Math.max(1, elapsedSeconds / 60);
        String subject = (String) timerSubjectCombo.getSelectedItem();
        if (subject == null) { showError("Izberite predmet."); return; }

        if (sessionController.createFromTimer(subject, minutes)) {
            toast("Seja shranjena · " + minutes + " min");
            refreshAll();
        } else {
            showError("Napaka pri shranjevanju seje.");
        }
        elapsedSeconds = 0;
    }

    // ================================================================
    //  Actions
    // ================================================================
    private void createManualSession() {
        String subject = (String) manualSubjectCombo.getSelectedItem();
        int    dur     = (Integer) manualDurationSpinner.getValue();
        if (subject == null) { showError("Izberite predmet."); return; }
        if (sessionController.createFromTimer(subject, dur)) {
            toast("Seja dodana · " + dur + " min");
            refreshAll();
        } else {
            showError("Napaka pri ustvarjanju seje.");
        }
    }

    private void deleteSelectedSession() {
        int row = sessionTable.getSelectedRow();
        if (row < 0) { showError("Izberite sejo za brisanje."); return; }
        int id = (Integer) sessionTableModel.getValueAt(row, 0);
        if (sessionController.deleteConfirmed(id)) {
            toast("Seja izbrisana.");
            refreshAll();
        }
    }

    private void addSubject() {
        String name = newSubjectField.getText().trim();
        if (name.isEmpty()) return;
        if (context.findSubjectByName(name, currentUserId) != null) {
            showError("Predmet že obstaja."); return;
        }
        context.addSubject(new Subject(context.nextSubjectId(), name, currentUserId));
        newSubjectField.setText("");
        refreshSubjectCombos();
        showSubjectList();
        toast("Predmet dodan · " + name);
    }

    // ================================================================
    //  Refresh
    // ================================================================
    private void refreshAll() {
        refreshSubjectCombos();
        showSessionList();
        showStats();
        showSubjectList();
    }

    private void refreshSubjectCombos() {
        String prevTimer  = (String) timerSubjectCombo.getSelectedItem();
        String prevManual = (String) manualSubjectCombo.getSelectedItem();
        timerSubjectCombo.removeAllItems();
        manualSubjectCombo.removeAllItems();
        for (Subject s : context.getSubjects())
            if (s.getUserId().equals(currentUserId)) {
                timerSubjectCombo.addItem(s.getName());
                manualSubjectCombo.addItem(s.getName());
            }
        if (prevTimer  != null) timerSubjectCombo.setSelectedItem(prevTimer);
        if (prevManual != null) manualSubjectCombo.setSelectedItem(prevManual);
    }

    private void showSubjectList() {
        subjectListModel.clear();
        for (Subject s : context.getSubjects())
            if (s.getUserId().equals(currentUserId))
                subjectListModel.addElement(s.getName());
    }

    // ================================================================
    //  UI helpers
    // ================================================================
    private JPanel pageHeader(String title) {
        JPanel h = new JPanel(new BorderLayout());
        h.setBackground(SURFACE);
        h.setBorder(new CompoundBorder(
            new MatteBorder(0, 0, 1, 0, SEP),
            new EmptyBorder(22, 20, 18, 20)));
        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font(FONT, Font.BOLD, 22));
        lbl.setForeground(TEXT);
        h.add(lbl, BorderLayout.WEST);
        return h;
    }

    private JPanel card() {
        JPanel c = new JPanel();
        c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));
        c.setBackground(SURFACE);
        c.setBorder(new EmptyBorder(18, 18, 18, 18));
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        return c;
    }

    private JLabel chipLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font(FONT, Font.BOLD, 10));
        l.setForeground(TEXT2);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JButton actionButton(String text, Color color) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = !isEnabled()        ? new Color(200, 200, 200, 90)
                         : getModel().isPressed() ? color.darker()
                         : color;
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setFont(new Font(FONT, Font.PLAIN, 13));
        b.setForeground(Color.WHITE);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(11, 16, 11, 16));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addPropertyChangeListener("enabled", e -> b.repaint());
        return b;
    }

    private JComboBox<String> combo() {
        JComboBox<String> cb = new JComboBox<>();
        cb.setFont(new Font(FONT, Font.PLAIN, 13));
        cb.setBackground(BG);
        cb.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        return cb;
    }

    private JTextField field(String placeholder) {
        JTextField tf = new JTextField() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setColor(NAV_OFF);
                    g2.setFont(getFont());
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(placeholder, getInsets().left + 2,
                        (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
                    g2.dispose();
                }
            }
        };
        tf.setFont(new Font(FONT, Font.PLAIN, 13));
        tf.setBorder(new CompoundBorder(
            new LineBorder(SEP, 1, true),
            new EmptyBorder(9, 12, 9, 12)));
        tf.setBackground(SURFACE);
        tf.setForeground(TEXT);
        return tf;
    }

    private JLabel metricCard(JPanel parent, String title, String value) {
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(SURFACE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.setColor(SEP);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 14, 14);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(12, 8, 12, 8));

        JLabel titleLbl = new JLabel(title, SwingConstants.CENTER);
        titleLbl.setFont(new Font(FONT, Font.PLAIN, 10));
        titleLbl.setForeground(TEXT2);
        titleLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel valLbl = new JLabel(value, SwingConstants.CENTER);
        valLbl.setFont(new Font(FONT, Font.BOLD, 24));
        valLbl.setForeground(ACCENT);
        valLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(titleLbl);
        card.add(Box.createVerticalStrut(4));
        card.add(valLbl);
        parent.add(card);
        return valLbl;
    }

    private JPanel buildBarRow(String name, int minutes, int max) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setBackground(BG);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        JLabel nameLbl = new JLabel(name);
        nameLbl.setFont(new Font(FONT, Font.PLAIN, 12));
        nameLbl.setForeground(TEXT);
        nameLbl.setPreferredSize(new Dimension(90, 20));

        int pct = (int) ((double) minutes / max * 100);
        JPanel track = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(SEP);
                g2.fillRoundRect(0, (getHeight()-8)/2, getWidth(), 8, 6, 6);
                g2.setColor(ACCENT);
                int w = (int) ((double) pct / 100 * getWidth());
                if (w > 0) g2.fillRoundRect(0, (getHeight()-8)/2, w, 8, 6, 6);
                g2.dispose();
            }
        };
        track.setOpaque(false);

        JLabel valLbl = new JLabel(minutes + " min");
        valLbl.setFont(new Font(FONT, Font.PLAIN, 11));
        valLbl.setForeground(TEXT2);
        valLbl.setPreferredSize(new Dimension(55, 20));
        valLbl.setHorizontalAlignment(SwingConstants.RIGHT);

        row.add(nameLbl, BorderLayout.WEST);
        row.add(track,   BorderLayout.CENTER);
        row.add(valLbl,  BorderLayout.EAST);
        return row;
    }

    private void styleSpinner(JSpinner spinner) {
        spinner.setBorder(new CompoundBorder(
            new LineBorder(SEP, 1, true),
            new EmptyBorder(4, 6, 4, 6)));
        ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField()
            .setFont(new Font(FONT, Font.PLAIN, 13));
    }

    private void toast(String msg) {
        JWindow toast = new JWindow(this);
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(30, 30, 30, 220));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(10, 20, 10, 20));
        JLabel lbl = new JLabel(msg);
        lbl.setFont(new Font(FONT, Font.PLAIN, 13));
        lbl.setForeground(Color.WHITE);
        panel.add(lbl, BorderLayout.CENTER);
        toast.setContentPane(panel);
        toast.pack();
        Rectangle bounds = getBounds();
        toast.setLocation(
            bounds.x + (bounds.width  - toast.getWidth())  / 2,
            bounds.y + bounds.height  - toast.getHeight() - 80);
        toast.setVisible(true);
        new Timer(2000, e -> toast.dispose()) {{ setRepeats(false); start(); }};
    }
}
