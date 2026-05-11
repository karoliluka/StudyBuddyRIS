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
    private JPanel[]   navBars  = new JPanel[4];
    private JLabel[]   navTexts = new JLabel[4];
    private static final String[] SCREENS = {"sessions", "timer", "stats", "subjects"};

    // Sessions
    private DefaultTableModel sessionTableModel;
    private JTable            sessionTable;

    // Timer
    private JComboBox<String> timerSubjectCombo;
    private JLabel            timerLabel;
    private JButton           btnStart;
    private JButton           btnStop;
    private Timer             swingTimer;
    private int               elapsedSeconds = 0;

    // Manual entry (collapsible)
    private JPanel            manualPanel;
    private JLabel            forgetLink;
    private JComboBox<String> manualSubjectCombo;
    private JSpinner          manualDurationSpinner;

    // Stats
    private JLabel lblTotal;
    private JLabel lblAvg;
    private JLabel lblCount;
    private JLabel lblBest;
    private JPanel barsPanel;

    // Subjects
    private DefaultTableModel subjectTableModel;
    private JTable            subjectTable;
    private JTextField        newSubjectField;

    // ── Color palette ────────────────────────────────────────────────
    // #EEF1EF  #DC965A  #242325
    private static final Color BG       = new Color(238, 241, 239);
    private static final Color SURFACE  = Color.WHITE;
    private static final Color ACCENT   = new Color(220, 150, 90);
    private static final Color ACCENT_L = new Color(220, 150, 90, 45);
    private static final Color DARK     = new Color(36, 35, 37);
    private static final Color TEXT     = DARK;
    private static final Color TEXT2    = new Color(120, 118, 122);
    private static final Color SEP      = new Color(208, 213, 209);
    private static final Color NAV_OFF  = new Color(175, 173, 177);
    private static final Color OK       = new Color(52, 168, 83);
    private static final Color ERR      = new Color(210, 50, 65);
    private static final Color ROW_ALT  = new Color(244, 246, 244);

    private static final int PAD = 16;

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
        setResizable(false);
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

        root.add(cardPanel,        BorderLayout.CENTER);
        root.add(buildBottomNav(), BorderLayout.SOUTH);
        setContentPane(root);
    }

    // ================================================================
    //  Bottom navigation bar
    // ================================================================
    private JPanel buildBottomNav() {
        JPanel bar = new JPanel(new GridLayout(1, 4));
        bar.setBackground(SURFACE);
        bar.setBorder(new MatteBorder(1, 0, 0, 0, SEP));
        bar.setPreferredSize(new Dimension(0, 52));

        String[] labels = { "Seje", "Timer", "Statistika", "Predmeti" };

        for (int i = 0; i < 4; i++) {
            final int idx = i;

            // Accent indicator line at top of each tab
            JPanel indicator = new JPanel();
            indicator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 3));
            indicator.setPreferredSize(new Dimension(0, 3));

            JLabel txt = new JLabel(labels[i], SwingConstants.CENTER);
            txt.setAlignmentX(Component.CENTER_ALIGNMENT);

            JPanel item = new JPanel();
            item.setLayout(new BoxLayout(item, BoxLayout.Y_AXIS));
            item.setBackground(SURFACE);
            item.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            item.add(indicator);
            item.add(Box.createVerticalGlue());
            item.add(txt);
            item.add(Box.createVerticalGlue());
            item.addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) { switchTo(idx); }
            });

            navBars[i]  = indicator;
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
            navBars[i].setBackground(on ? ACCENT : SURFACE);
            navTexts[i].setForeground(on ? ACCENT : NAV_OFF);
            navTexts[i].setFont(new Font(FONT, on ? Font.BOLD : Font.PLAIN, 12));
        }
    }

    // ================================================================
    //  Screen: Seje — Zgodovina
    // ================================================================
    private JPanel buildSessionsTab() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(BG);
        p.add(pageHeader("Zgodovina"), BorderLayout.NORTH);

        String[] cols = {"#", "Predmet", "Datum", "Trajanje"};
        sessionTableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        sessionTable = styledTable(sessionTableModel);
        // hide ID column
        hideColumn(sessionTable, 0);
        sessionTable.getColumnModel().getColumn(3).setMaxWidth(105);

        JScrollPane scroll = tableScroll(sessionTable);
        JPanel wrap = padded(scroll);
        p.add(wrap, BorderLayout.CENTER);

        JPanel footer = footer();
        JButton btnDel = actionButton("Izbriši izbrano", ERR);
        btnDel.addActionListener(e -> deleteSelectedSession());
        footer.add(btnDel, BorderLayout.CENTER);
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
        body.setBorder(new EmptyBorder(PAD, PAD, PAD, PAD));

        // ── Main timer card ──────────────────────────────────────────
        JPanel timerCard = card();

        timerLabel = new JLabel("00:00", SwingConstants.CENTER);
        timerLabel.setFont(new Font(FONT, Font.PLAIN, 72));
        timerLabel.setForeground(DARK);
        timerLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        timerLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        timerCard.add(timerLabel);
        timerCard.add(Box.createVerticalStrut(22));

        timerCard.add(chipLabel("PREDMET"));
        timerCard.add(Box.createVerticalStrut(6));
        timerSubjectCombo = combo();
        timerSubjectCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        timerCard.add(timerSubjectCombo);
        timerCard.add(Box.createVerticalStrut(16));

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
        body.add(Box.createVerticalStrut(6));

        // ── "Si pozabil beležiti?" toggle ─────────────────────────
        forgetLink = new JLabel("  Si pozabil beležiti?  →");
        forgetLink.setFont(new Font(FONT, Font.PLAIN, 13));
        forgetLink.setForeground(ACCENT);
        forgetLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        forgetLink.setAlignmentX(Component.LEFT_ALIGNMENT);
        forgetLink.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        body.add(forgetLink);
        body.add(Box.createVerticalStrut(4));

        // ── Manual entry card (hidden by default) ─────────────────
        manualPanel = card();
        manualPanel.setVisible(false);

        manualPanel.add(chipLabel("ROČNI VNOS"));
        manualPanel.add(Box.createVerticalStrut(6));
        manualPanel.add(chipLabel("PREDMET"));
        manualPanel.add(Box.createVerticalStrut(6));
        manualSubjectCombo = combo();
        manualSubjectCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        manualPanel.add(manualSubjectCombo);
        manualPanel.add(Box.createVerticalStrut(12));

        manualDurationSpinner = new JSpinner(new SpinnerNumberModel(30, 1, 600, 5));
        manualDurationSpinner.setFont(new Font(FONT, Font.PLAIN, 13));
        manualDurationSpinner.setMaximumSize(new Dimension(90, 38));
        styleSpinner(manualDurationSpinner);

        JLabel minLbl = new JLabel(" min");
        minLbl.setFont(new Font(FONT, Font.PLAIN, 13));
        minLbl.setForeground(TEXT2);

        JButton btnManual = actionButton("Dodaj sejo", ACCENT);
        btnManual.addActionListener(e -> createManualSession());

        JPanel durRow = new JPanel();
        durRow.setOpaque(false);
        durRow.setLayout(new BoxLayout(durRow, BoxLayout.X_AXIS));
        durRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        durRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        durRow.add(manualDurationSpinner);
        durRow.add(minLbl);
        durRow.add(Box.createHorizontalGlue());
        durRow.add(btnManual);
        manualPanel.add(durRow);

        body.add(manualPanel);

        // Toggle
        forgetLink.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                boolean show = !manualPanel.isVisible();
                manualPanel.setVisible(show);
                forgetLink.setText(show
                    ? "  Si pozabil beležiti?  ↑"
                    : "  Si pozabil beležiti?  →");
                body.revalidate();
                body.repaint();
            }
        });

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(BG);
        wrap.add(body, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(wrap);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(BG);
        p.add(scroll, BorderLayout.CENTER);

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
        body.setBorder(new EmptyBorder(PAD, PAD, PAD, PAD));

        // Stat rows inside a single white card
        JPanel statsCard = card();
        statsCard.setBorder(new EmptyBorder(0, 0, 0, 0));
        lblTotal = statRow(statsCard, "Skupaj minut",   "0",  true);
        lblAvg   = statRow(statsCard, "Povprečje/sejo", "0",  true);
        lblCount = statRow(statsCard, "Število sej",    "0",  true);
        lblBest  = statRow(statsCard, "Najboljši",      "—",  false);
        body.add(statsCard);
        body.add(Box.createVerticalStrut(PAD));

        JLabel chartLbl = chipLabel("PO PREDMETIH");
        chartLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(chartLbl);
        body.add(Box.createVerticalStrut(10));

        barsPanel = new JPanel();
        barsPanel.setBackground(BG);
        barsPanel.setLayout(new BoxLayout(barsPanel, BoxLayout.Y_AXIS));
        barsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(barsPanel);

        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(BG);
        outer.add(body, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(outer);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(BG);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        p.add(scroll, BorderLayout.CENTER);

        JPanel foot = footer();
        JButton btnRefresh = actionButton("Osveži statistike", ACCENT);
        btnRefresh.addActionListener(e -> showStats());
        foot.add(btnRefresh, BorderLayout.CENTER);
        p.add(foot, BorderLayout.SOUTH);

        return p;
    }

    // ================================================================
    //  Screen: Predmeti (tabela)
    // ================================================================
    private JPanel buildSubjectsTab() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(BG);
        p.add(pageHeader("Predmeti"), BorderLayout.NORTH);

        String[] cols = {"#", "Ime predmeta"};
        subjectTableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        subjectTable = styledTable(subjectTableModel);
        subjectTable.getColumnModel().getColumn(0).setMaxWidth(52);
        subjectTable.getColumnModel().getColumn(0).setPreferredWidth(42);

        JScrollPane scroll = tableScroll(subjectTable);
        JPanel wrap = padded(scroll);
        p.add(wrap, BorderLayout.CENTER);

        JPanel foot = footer();
        newSubjectField = field("Ime predmeta...");
        JButton btnAdd = actionButton("Dodaj", ACCENT);
        btnAdd.addActionListener(e -> addSubject());
        newSubjectField.addActionListener(e -> addSubject());
        foot.add(newSubjectField, BorderLayout.CENTER);
        foot.add(Box.createHorizontalStrut(8), BorderLayout.NORTH); // ignored; gap via layout
        foot.add(btnAdd, BorderLayout.EAST);
        p.add(foot, BorderLayout.SOUTH);

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
                s.getStudySessionId(), name,
                s.getFormattedDate(), s.getDurationMinutes() + " min"
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
    //  Timer logic
    // ================================================================
    private void startTimer() {
        elapsedSeconds = 0;
        btnStart.setEnabled(false);
        btnStop.setEnabled(true);
        swingTimer = new Timer(1000, e -> {
            elapsedSeconds++;
            timerLabel.setText(String.format("%02d:%02d",
                elapsedSeconds / 60, elapsedSeconds % 60));
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
            manualPanel.setVisible(false);
            forgetLink.setText("  Si pozabil beležiti?  →");
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
    //  Refresh helpers
    // ================================================================
    private void refreshAll() {
        refreshSubjectCombos();
        showSessionList();
        showStats();
        showSubjectList();
    }

    private void refreshSubjectCombos() {
        String pT = (String) timerSubjectCombo.getSelectedItem();
        String pM = (String) manualSubjectCombo.getSelectedItem();
        timerSubjectCombo.removeAllItems();
        manualSubjectCombo.removeAllItems();
        for (Subject s : context.getSubjects())
            if (s.getUserId().equals(currentUserId)) {
                timerSubjectCombo.addItem(s.getName());
                manualSubjectCombo.addItem(s.getName());
            }
        if (pT != null) timerSubjectCombo.setSelectedItem(pT);
        if (pM != null) manualSubjectCombo.setSelectedItem(pM);
    }

    private void showSubjectList() {
        subjectTableModel.setRowCount(0);
        int i = 1;
        for (Subject s : context.getSubjects())
            if (s.getUserId().equals(currentUserId))
                subjectTableModel.addRow(new Object[]{ i++, s.getName() });
    }

    // ================================================================
    //  Reusable UI builders
    // ================================================================
    private JPanel pageHeader(String title) {
        JPanel h = new JPanel(new BorderLayout());
        h.setBackground(SURFACE);
        h.setBorder(new CompoundBorder(
            new MatteBorder(0, 0, 1, 0, SEP),
            new EmptyBorder(22, PAD, 16, PAD)));
        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font(FONT, Font.BOLD, 22));
        lbl.setForeground(DARK);
        h.add(lbl, BorderLayout.WEST);
        return h;
    }

    // White card with consistent internal padding
    private JPanel card() {
        JPanel c = new JPanel();
        c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));
        c.setBackground(SURFACE);
        c.setBorder(new EmptyBorder(PAD, PAD, PAD, PAD));
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        return c;
    }

    // Table area wrapper — gives consistent PAD margin on left/right/top
    private JPanel padded(JScrollPane scroll) {
        JPanel w = new JPanel(new BorderLayout());
        w.setBackground(BG);
        w.setBorder(new EmptyBorder(PAD, PAD, 0, PAD));
        w.add(scroll, BorderLayout.CENTER);
        return w;
    }

    // Footer bar — PAD on all sides, gap between content and table
    private JPanel footer() {
        JPanel f = new JPanel(new BorderLayout(8, 0));
        f.setBackground(BG);
        f.setBorder(new EmptyBorder(PAD / 2, PAD, PAD, PAD));
        return f;
    }

    private JTable styledTable(DefaultTableModel model) {
        JTable t = new JTable(model) {
            @Override public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                if (!isRowSelected(row)) c.setBackground(row % 2 == 0 ? SURFACE : ROW_ALT);
                else c.setBackground(ACCENT_L);
                return c;
            }
        };
        t.setRowHeight(42);
        t.setFont(new Font(FONT, Font.PLAIN, 13));
        t.setShowGrid(false);
        t.setIntercellSpacing(new Dimension(0, 0));
        t.setFillsViewportHeight(true);
        t.setSelectionBackground(ACCENT_L);
        t.setSelectionForeground(TEXT);

        JTableHeader th = t.getTableHeader();
        th.setFont(new Font(FONT, Font.BOLD, 11));
        th.setBackground(ROW_ALT);
        th.setForeground(TEXT2);
        th.setBorder(new MatteBorder(0, 0, 1, 0, SEP));
        th.setReorderingAllowed(false);
        return t;
    }

    private JScrollPane tableScroll(JTable t) {
        JScrollPane s = new JScrollPane(t);
        s.setBorder(new LineBorder(SEP, 1, true));
        s.getViewport().setBackground(SURFACE);
        return s;
    }

    private void hideColumn(JTable t, int col) {
        TableColumn c = t.getColumnModel().getColumn(col);
        c.setMinWidth(0); c.setMaxWidth(0); c.setWidth(0);
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
                Color bg = !isEnabled()           ? new Color(200, 200, 200, 80)
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
                    g2.setColor(TEXT2);
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

    // Full-width stat row: title on left, value on right, optional bottom divider
    private JLabel statRow(JPanel parent, String title, String value, boolean divider) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(SURFACE);
        row.setBorder(new EmptyBorder(14, PAD, 14, PAD));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font(FONT, Font.PLAIN, 13));
        titleLbl.setForeground(TEXT);

        JLabel valLbl = new JLabel(value);
        valLbl.setFont(new Font(FONT, Font.BOLD, 18));
        valLbl.setForeground(ACCENT);

        row.add(titleLbl, BorderLayout.WEST);
        row.add(valLbl,   BorderLayout.EAST);

        if (divider) {
            JPanel wrap = new JPanel(new BorderLayout());
            wrap.setBackground(SURFACE);
            wrap.setAlignmentX(Component.LEFT_ALIGNMENT);
            wrap.add(row, BorderLayout.CENTER);
            wrap.add(new JSeparator(), BorderLayout.SOUTH);
            ((JSeparator) wrap.getComponent(1)).setForeground(SEP);
            parent.add(wrap);
        } else {
            parent.add(row);
        }
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
                int y = (getHeight() - 8) / 2;
                g2.setColor(SEP);
                g2.fillRoundRect(0, y, getWidth(), 8, 6, 6);
                g2.setColor(ACCENT);
                int w = (int) ((double) pct / 100 * getWidth());
                if (w > 0) g2.fillRoundRect(0, y, w, 8, 6, 6);
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
                g2.setColor(new Color(36, 35, 37, 220));
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
            bounds.y +  bounds.height - toast.getHeight() - 80);
        toast.setVisible(true);
        new Timer(2000, e -> toast.dispose()) {{ setRepeats(false); start(); }};
    }
}
