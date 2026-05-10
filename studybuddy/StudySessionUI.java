package studybuddy;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class StudySessionUI extends JFrame {

    private StudySessionsController sessionController;
    private StatsController         statsController;
    private StudyBuddyContext       context;
    private String                  currentUserId;

    private JTabbedPane tabs;

    private DefaultTableModel sessionTableModel;
    private JTable            sessionTable;

    private JComboBox<String> timerSubjectCombo;
    private JLabel            timerLabel;
    private JButton           btnStart;
    private JButton           btnStop;
    private Timer             swingTimer;
    private int               elapsedSeconds = 0;

    private JComboBox<String> manualSubjectCombo;
    private JSpinner          manualDurationSpinner;

    private JLabel lblTotal;
    private JLabel lblAvg;
    private JLabel lblCount;
    private JLabel lblBest;
    private JPanel barsPanel;

    private DefaultListModel<String> subjectListModel;
    private JTextField               newSubjectField;

    // Apple-style palette
    private static final Color BG        = new Color(242, 242, 247);   // iOS system gray 6
    private static final Color SURFACE   = Color.WHITE;
    private static final Color ACCENT    = new Color(0, 122, 255);     // Apple blue
    private static final Color ACCENT_L  = new Color(0, 122, 255, 20);
    private static final Color OK        = new Color(52, 199, 89);     // Apple green
    private static final Color OK_L      = new Color(52, 199, 89, 20);
    private static final Color ERR       = new Color(255, 59, 48);     // Apple red
    private static final Color ERR_L     = new Color(255, 59, 48, 20);
    private static final Color TEXT      = new Color(0, 0, 0);
    private static final Color TEXT2     = new Color(60, 60, 67);
    private static final Color MUTED     = new Color(60, 60, 67, 153);
    private static final Color SEP       = new Color(60, 60, 67, 36);
    private static final Color SIDEBAR   = new Color(28, 28, 30);      // Apple dark nav

    // Font stack: SF Pro → Helvetica Neue → SansSerif
    private static String FONT = "SF Pro Display";
    private static String FONT_TEXT = "SF Pro Text";

    static {
        String[] available = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        boolean hasSF = false;
        for (String f : available) { if (f.equals("SF Pro Display")) { hasSF = true; break; } }
        if (!hasSF) {
            boolean hasHelv = false;
            for (String f : available) { if (f.equals("Helvetica Neue")) { hasHelv = true; break; } }
            FONT = hasHelv ? "Helvetica Neue" : "SansSerif";
            FONT_TEXT = FONT;
        }
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
        setSize(860, 600);
        setMinimumSize(new Dimension(700, 500));
        setLocationRelativeTo(null);
        setBackground(BG);

        // Use cross-platform L&F for consistent custom styling
        try { UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()); }
        catch (Exception ignored) {}

        // Global UI defaults
        UIManager.put("TabbedPane.selected", SURFACE);
        UIManager.put("TabbedPane.background", BG);
        UIManager.put("TabbedPane.contentAreaColor", SURFACE);
        UIManager.put("TabbedPane.tabsOpaque", false);
        UIManager.put("ScrollBar.width", 8);
        UIManager.put("ScrollBar.thumbColor", new Color(0, 0, 0, 60));
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(BG);

        // Sidebar navigation
        root.add(buildSidebar(), BorderLayout.WEST);

        // Content with card-style tabs
        tabs = new JTabbedPane(JTabbedPane.TOP);
        tabs.setBackground(BG);
        tabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        // Hide tab bar – navigation driven by sidebar buttons
        tabs.addTab("Seje",       buildSessionsTab());
        tabs.addTab("Timer",      buildTimerTab());
        tabs.addTab("Statistika", buildStatsTab());
        tabs.addTab("Predmeti",   buildSubjectsTab());

        // Remove default tab bar by making it zero height
        UIManager.put("TabbedPane.tabAreaInsets", new Insets(0, 0, 0, 0));
        tabs.setUI(new HiddenTabUI());

        root.add(tabs, BorderLayout.CENTER);
        setContentPane(root);
    }

    // ----------------------------------------------------------------
    //  Sidebar
    // ----------------------------------------------------------------
    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setBackground(SIDEBAR);
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(200, 0));
        sidebar.setBorder(new EmptyBorder(0, 0, 0, 0));

        // App title area
        JPanel titleArea = new JPanel(new BorderLayout());
        titleArea.setBackground(SIDEBAR);
        titleArea.setBorder(new EmptyBorder(28, 20, 20, 20));
        JLabel appTitle = new JLabel("StudyBuddy");
        appTitle.setFont(new Font(FONT, Font.BOLD, 20));
        appTitle.setForeground(Color.WHITE);
        titleArea.add(appTitle, BorderLayout.CENTER);
        sidebar.add(titleArea);

        // Nav items
        String[][] navItems = {
            {"📋", "Seje",       "0"},
            {"⏱",  "Timer",      "1"},
            {"📊", "Statistika", "2"},
            {"📚", "Predmeti",   "3"},
        };

        ButtonGroup navGroup = new ButtonGroup();
        for (String[] item : navItems) {
            JToggleButton btn = navButton(item[0], item[1]);
            final int idx = Integer.parseInt(item[2]);
            btn.addActionListener(e -> tabs.setSelectedIndex(idx));
            navGroup.add(btn);
            sidebar.add(btn);
            if (idx == 0) btn.setSelected(true);
        }

        sidebar.add(Box.createVerticalGlue());

        // Footer
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(SIDEBAR);
        footer.setBorder(new EmptyBorder(12, 20, 20, 20));
        JLabel authors = new JLabel("Luka Karoli & Aljaž Smole");
        authors.setFont(new Font(FONT_TEXT, Font.PLAIN, 10));
        authors.setForeground(new Color(255, 255, 255, 80));
        footer.add(authors, BorderLayout.CENTER);
        sidebar.add(footer);

        return sidebar;
    }

    private JToggleButton navButton(String icon, String label) {
        JToggleButton btn = new JToggleButton(icon + "  " + label) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (isSelected()) {
                    g2.setColor(new Color(255, 255, 255, 30));
                    g2.fillRoundRect(8, 2, getWidth() - 16, getHeight() - 4, 10, 10);
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(255, 255, 255, 15));
                    g2.fillRoundRect(8, 2, getWidth() - 16, getHeight() - 4, 10, 10);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font(FONT_TEXT, Font.PLAIN, 14));
        btn.setForeground(new Color(255, 255, 255, 200));
        btn.setBackground(new Color(0, 0, 0, 0));
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(new EmptyBorder(10, 20, 10, 20));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addChangeListener(e -> btn.setForeground(btn.isSelected()
            ? Color.WHITE : new Color(255, 255, 255, 200)));
        return btn;
    }

    // ================================================================
    //  TAB: Seje
    // ================================================================
    private JPanel buildSessionsTab() {
        JPanel p = card();
        p.setBorder(new EmptyBorder(24, 24, 24, 24));
        p.setLayout(new BorderLayout(0, 16));

        JLabel heading = heading("Moje seje");
        p.add(heading, BorderLayout.NORTH);

        String[] cols = {"ID", "Predmet", "Datum", "Trajanje"};
        sessionTableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        sessionTable = new JTable(sessionTableModel) {
            @Override public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                if (!isRowSelected(row)) c.setBackground(row % 2 == 0 ? SURFACE : new Color(242, 242, 247));
                else c.setBackground(ACCENT_L);
                return c;
            }
        };
        sessionTable.setRowHeight(36);
        sessionTable.setFont(new Font(FONT_TEXT, Font.PLAIN, 13));
        sessionTable.setShowGrid(false);
        sessionTable.setIntercellSpacing(new Dimension(0, 0));
        sessionTable.setFillsViewportHeight(true);

        JTableHeader header = sessionTable.getTableHeader();
        header.setFont(new Font(FONT_TEXT, Font.BOLD, 11));
        header.setBackground(new Color(242, 242, 247));
        header.setForeground(MUTED);
        header.setBorder(new MatteBorder(0, 0, 1, 0, SEP));
        header.setReorderingAllowed(false);

        sessionTable.setSelectionBackground(new Color(0, 122, 255, 30));
        sessionTable.setSelectionForeground(TEXT);
        sessionTable.getColumnModel().getColumn(0).setMaxWidth(50);
        sessionTable.getColumnModel().getColumn(3).setMaxWidth(120);

        JScrollPane scroll = new JScrollPane(sessionTable);
        scroll.setBorder(new LineBorder(SEP, 1, true));
        scroll.getViewport().setBackground(SURFACE);
        scroll.setBackground(SURFACE);
        p.add(scroll, BorderLayout.CENTER);

        JButton btnDelete = pillButton("Izbriši izbrano", ERR, ERR_L);
        btnDelete.addActionListener(e -> deleteSelectedSession());
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        bottom.setOpaque(false);
        bottom.add(btnDelete);
        p.add(bottom, BorderLayout.SOUTH);
        return p;
    }

    // ================================================================
    //  TAB: Timer
    // ================================================================
    private JPanel buildTimerTab() {
        JPanel p = card();
        p.setBorder(new EmptyBorder(32, 48, 32, 48));
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

        p.add(centerHeading("Timer"));
        p.add(Box.createVerticalStrut(24));

        // Subject
        JLabel subLbl = smallLabel("PREDMET");
        subLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(subLbl);
        p.add(Box.createVerticalStrut(6));
        timerSubjectCombo = appleCombo();
        timerSubjectCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(timerSubjectCombo);
        p.add(Box.createVerticalStrut(32));

        // Big timer
        timerLabel = new JLabel("00:00", SwingConstants.CENTER);
        timerLabel.setFont(new Font(FONT, Font.PLAIN, 72));
        timerLabel.setForeground(TEXT);
        timerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(timerLabel);
        p.add(Box.createVerticalStrut(28));

        // Buttons
        JPanel btnRow = new JPanel(new GridLayout(1, 2, 12, 0));
        btnRow.setOpaque(false);
        btnRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        btnStart = pillButton("▶   Start",         OK,  OK_L);
        btnStop  = pillButton("■   Stop & Shrani",  ERR, ERR_L);
        btnStop.setEnabled(false);
        btnStart.addActionListener(e -> startTimer());
        btnStop.addActionListener(e -> stopTimer());
        btnRow.add(btnStart);
        btnRow.add(btnStop);
        p.add(btnRow);
        p.add(Box.createVerticalStrut(36));

        // Divider
        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setForeground(SEP);
        p.add(sep);
        p.add(Box.createVerticalStrut(24));

        // Manual entry
        JLabel manLbl = smallLabel("ROČNI VNOS");
        manLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(manLbl);
        p.add(Box.createVerticalStrut(10));

        JPanel manualRow = new JPanel();
        manualRow.setOpaque(false);
        manualRow.setLayout(new BoxLayout(manualRow, BoxLayout.X_AXIS));
        manualRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        manualRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        manualSubjectCombo = appleCombo();
        manualSubjectCombo.setMaximumSize(new Dimension(200, 36));
        manualDurationSpinner = new JSpinner(new SpinnerNumberModel(30, 1, 600, 5));
        manualDurationSpinner.setFont(new Font(FONT_TEXT, Font.PLAIN, 13));
        manualDurationSpinner.setMaximumSize(new Dimension(80, 36));
        styleSpinner(manualDurationSpinner);

        JLabel minLabel = new JLabel(" min");
        minLabel.setFont(new Font(FONT_TEXT, Font.PLAIN, 13));
        minLabel.setForeground(MUTED);

        JButton btnManual = pillButton("Dodaj sejo", ACCENT, ACCENT_L);
        btnManual.addActionListener(e -> createManualSession());

        manualRow.add(manualSubjectCombo);
        manualRow.add(Box.createHorizontalStrut(10));
        manualRow.add(manualDurationSpinner);
        manualRow.add(minLabel);
        manualRow.add(Box.createHorizontalStrut(10));
        manualRow.add(btnManual);
        p.add(manualRow);

        return p;
    }

    // ================================================================
    //  TAB: Statistika
    // ================================================================
    private JPanel buildStatsTab() {
        JPanel p = card();
        p.setBorder(new EmptyBorder(24, 24, 24, 24));
        p.setLayout(new BorderLayout(0, 20));

        p.add(heading("Statistika"), BorderLayout.NORTH);

        JPanel cards = new JPanel(new GridLayout(1, 4, 14, 0));
        cards.setOpaque(false);
        lblTotal = metricCard(cards, "Skupaj minut", "0");
        lblAvg   = metricCard(cards, "Povprečje/sejo", "0");
        lblCount = metricCard(cards, "Število sej",  "0");
        lblBest  = metricCard(cards, "Najboljši", "—");

        barsPanel = new JPanel();
        barsPanel.setBackground(SURFACE);
        barsPanel.setLayout(new BoxLayout(barsPanel, BoxLayout.Y_AXIS));
        barsPanel.setBorder(new EmptyBorder(16, 16, 16, 16));

        JScrollPane scroll = new JScrollPane(barsPanel);
        scroll.setBorder(new LineBorder(SEP, 1, true));
        scroll.getViewport().setBackground(SURFACE);
        scroll.setBackground(SURFACE);

        JPanel center = new JPanel(new BorderLayout(0, 14));
        center.setOpaque(false);
        center.add(cards, BorderLayout.NORTH);
        center.add(scroll, BorderLayout.CENTER);
        p.add(center, BorderLayout.CENTER);

        JButton btnRefresh = pillButton("Osveži", ACCENT, ACCENT_L);
        btnRefresh.addActionListener(e -> showStats());
        JPanel bot = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        bot.setOpaque(false);
        bot.add(btnRefresh);
        p.add(bot, BorderLayout.SOUTH);
        return p;
    }

    // ================================================================
    //  TAB: Predmeti
    // ================================================================
    private JPanel buildSubjectsTab() {
        JPanel p = card();
        p.setBorder(new EmptyBorder(24, 24, 24, 24));
        p.setLayout(new BorderLayout(0, 16));

        p.add(heading("Predmeti"), BorderLayout.NORTH);

        subjectListModel = new DefaultListModel<>();
        JList<String> list = new JList<>(subjectListModel) {
            @Override public void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                super.paintComponent(g2);
                g2.dispose();
            }
        };
        list.setFont(new Font(FONT_TEXT, Font.PLAIN, 14));
        list.setBackground(SURFACE);
        list.setSelectionBackground(new Color(0, 122, 255, 30));
        list.setSelectionForeground(TEXT);
        list.setFixedCellHeight(40);
        list.setBorder(new EmptyBorder(4, 12, 4, 12));

        DefaultListCellRenderer renderer = new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setBorder(new EmptyBorder(0, 4, 0, 4));
                setFont(new Font(FONT_TEXT, Font.PLAIN, 14));
                if (!isSelected) setBackground(index % 2 == 0 ? SURFACE : new Color(242, 242, 247));
                return this;
            }
        };
        list.setCellRenderer(renderer);

        JScrollPane scroll = new JScrollPane(list);
        scroll.setBorder(new LineBorder(SEP, 1, true));
        scroll.getViewport().setBackground(SURFACE);
        p.add(scroll, BorderLayout.CENTER);

        JPanel addRow = new JPanel(new BorderLayout(10, 0));
        addRow.setOpaque(false);
        newSubjectField = appleTextField("Ime predmeta...");
        JButton btnAdd = pillButton("Dodaj", ACCENT, ACCENT_L);
        btnAdd.addActionListener(e -> addSubject());
        newSubjectField.addActionListener(e -> addSubject());
        addRow.add(newSubjectField, BorderLayout.CENTER);
        addRow.add(btnAdd, BorderLayout.EAST);
        p.add(addRow, BorderLayout.SOUTH);
        return p;
    }

    // ================================================================
    //  VOPC methods
    // ================================================================
    public void showSessionForm() { tabs.setSelectedIndex(1); }

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
            empty.setFont(new Font(FONT_TEXT, Font.PLAIN, 13));
            empty.setForeground(MUTED);
            barsPanel.add(empty);
        } else {
            int max = by.values().stream().mapToInt(v -> v).max().orElse(1);
            for (Map.Entry<String, Integer> e : by.entrySet()) {
                barsPanel.add(buildBarRow(e.getKey(), e.getValue(), max));
                barsPanel.add(Box.createVerticalStrut(10));
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
    //  Refresh helpers
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
    private JPanel card() {
        JPanel p = new JPanel();
        p.setBackground(SURFACE);
        return p;
    }

    private JLabel heading(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font(FONT, Font.BOLD, 22));
        l.setForeground(TEXT);
        l.setBorder(new EmptyBorder(0, 0, 4, 0));
        return l;
    }

    private JLabel centerHeading(String text) {
        JLabel l = heading(text);
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        l.setHorizontalAlignment(SwingConstants.CENTER);
        return l;
    }

    private JLabel smallLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font(FONT_TEXT, Font.BOLD, 10));
        l.setForeground(MUTED);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JButton pillButton(String text, Color fg, Color bg) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bgColor = getModel().isPressed()  ? bg.darker()
                              : getModel().isRollover() ? bg.brighter()
                              : bg;
                if (!isEnabled()) bgColor = new Color(200, 200, 200, 40);
                g2.setColor(bgColor);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
            @Override protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(fg.getRed(), fg.getGreen(), fg.getBlue(), isEnabled() ? 60 : 30));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 10, 10);
                g2.dispose();
            }
        };
        b.setFont(new Font(FONT_TEXT, Font.PLAIN, 13));
        b.setForeground(isEnabled() ? fg : MUTED);
        b.setBackground(new Color(0, 0, 0, 0));
        b.setOpaque(false);
        b.setContentAreaFilled(false);
        b.setBorderPainted(true);
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(9, 18, 9, 18));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        // Keep foreground updated on enable/disable
        b.addPropertyChangeListener("enabled", e -> b.setForeground(b.isEnabled() ? fg : MUTED));
        return b;
    }

    private JComboBox<String> appleCombo() {
        JComboBox<String> cb = new JComboBox<>();
        cb.setFont(new Font(FONT_TEXT, Font.PLAIN, 13));
        cb.setBackground(BG);
        cb.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        return cb;
    }

    private JTextField appleTextField(String placeholder) {
        JTextField tf = new JTextField() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setColor(MUTED);
                    g2.setFont(getFont());
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(placeholder, getInsets().left + 2,
                        (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
                    g2.dispose();
                }
            }
        };
        tf.setFont(new Font(FONT_TEXT, Font.PLAIN, 13));
        tf.setBorder(new CompoundBorder(
            new LineBorder(SEP, 1, true),
            new EmptyBorder(8, 12, 8, 12)));
        tf.setBackground(SURFACE);
        tf.setForeground(TEXT);
        tf.setPreferredSize(new Dimension(0, 38));
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
        card.setBorder(new EmptyBorder(14, 14, 14, 14));

        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font(FONT_TEXT, Font.PLAIN, 11));
        lbl.setForeground(MUTED);
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel val = new JLabel(value, SwingConstants.CENTER);
        val.setFont(new Font(FONT, Font.BOLD, 28));
        val.setForeground(ACCENT);
        val.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(lbl);
        card.add(Box.createVerticalStrut(4));
        card.add(val);
        parent.add(card);
        return val;
    }

    private JPanel buildBarRow(String name, int minutes, int max) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setBackground(SURFACE);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));

        JLabel nameLbl = new JLabel(name);
        nameLbl.setFont(new Font(FONT_TEXT, Font.PLAIN, 12));
        nameLbl.setForeground(TEXT2);
        nameLbl.setPreferredSize(new Dimension(110, 20));

        int pct = (int) ((double) minutes / max * 100);
        JPanel track = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.setColor(ACCENT);
                int fillW = (int) ((double) pct / 100 * getWidth());
                g2.fillRoundRect(0, 0, fillW, getHeight(), 6, 6);
                g2.dispose();
            }
        };
        track.setOpaque(false);
        track.setPreferredSize(new Dimension(0, 8));

        JLabel valLbl = new JLabel(minutes + " min");
        valLbl.setFont(new Font(FONT_TEXT, Font.PLAIN, 11));
        valLbl.setForeground(MUTED);
        valLbl.setPreferredSize(new Dimension(55, 20));
        valLbl.setHorizontalAlignment(SwingConstants.RIGHT);

        JPanel trackWrapper = new JPanel(new GridBagLayout());
        trackWrapper.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        trackWrapper.add(track, gbc);

        row.add(nameLbl,      BorderLayout.WEST);
        row.add(trackWrapper, BorderLayout.CENTER);
        row.add(valLbl,       BorderLayout.EAST);
        return row;
    }

    private void styleSpinner(JSpinner spinner) {
        spinner.setBorder(new CompoundBorder(
            new LineBorder(SEP, 1, true),
            new EmptyBorder(4, 6, 4, 6)));
        ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField()
            .setFont(new Font(FONT_TEXT, Font.PLAIN, 13));
    }

    private void toast(String msg) {
        JWindow toast = new JWindow(this);
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(30, 30, 30, 230));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(10, 20, 10, 20));
        JLabel lbl = new JLabel(msg);
        lbl.setFont(new Font(FONT_TEXT, Font.PLAIN, 13));
        lbl.setForeground(Color.WHITE);
        panel.add(lbl, BorderLayout.CENTER);
        toast.setContentPane(panel);
        toast.pack();
        // Center bottom of window
        Rectangle bounds = getBounds();
        toast.setLocation(
            bounds.x + (bounds.width - toast.getWidth()) / 2,
            bounds.y + bounds.height - toast.getHeight() - 40);
        toast.setVisible(true);
        new Timer(2000, e -> toast.dispose()) {{ setRepeats(false); start(); }};
    }

    // ================================================================
    //  Hidden tab UI - sidebar drives navigation
    // ================================================================
    private static class HiddenTabUI extends javax.swing.plaf.basic.BasicTabbedPaneUI {
        @Override protected int calculateTabAreaHeight(int placement, int runCount, int maxTabHeight) { return 0; }
        @Override protected void paintTabArea(Graphics g, int placement, int selectedIndex) {}
        @Override protected void paintTab(Graphics g, int placement, Rectangle[] rects, int tabIndex,
                Rectangle iconRect, Rectangle textRect) {}
        @Override protected Insets getContentBorderInsets(int placement) { return new Insets(0,0,0,0); }
        @Override protected void paintContentBorder(Graphics g, int placement, int selectedIndex) {}
    }
}
