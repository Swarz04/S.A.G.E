package it.unibo.sage.view.admin;

import it.unibo.sage.model.Utente;
import it.unibo.sage.service.AdminService;
<<<<<<< HEAD
=======
import it.unibo.sage.service.AdminService.AdminSummary;
import it.unibo.sage.service.AdminService.CategoryUsage;
import it.unibo.sage.service.AdminService.MonthlyAdminStat;
>>>>>>> 3351d66 (aggiornamento interfaccia)
import it.unibo.sage.service.AdminService.TableData;
import it.unibo.sage.view.components.AppBackgroundPanel;
import it.unibo.sage.view.components.GlassPanel;
import it.unibo.sage.view.theme.AppTheme;
import java.awt.BorderLayout;
import java.awt.Color;
<<<<<<< HEAD
import java.awt.Font;
import java.sql.SQLException;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
=======
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
>>>>>>> 3351d66 (aggiornamento interfaccia)
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
<<<<<<< HEAD
=======
import javax.swing.SwingConstants;
>>>>>>> 3351d66 (aggiornamento interfaccia)
import javax.swing.table.DefaultTableModel;

public class AdminDashboardPanel extends AppBackgroundPanel {

    private final AdminService adminService = new AdminService();
    private final Utente currentUser;

    public AdminDashboardPanel(final Utente currentUser) {
        super(new BorderLayout());
        this.currentUser = currentUser;
<<<<<<< HEAD
        setBorder(BorderFactory.createEmptyBorder(26, 30, 26, 30));
        add(createHeader(), BorderLayout.NORTH);
        add(createTabs(), BorderLayout.CENTER);
=======
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        add(createHeader(), BorderLayout.NORTH);
        add(createScrollableContent(), BorderLayout.CENTER);
>>>>>>> 3351d66 (aggiornamento interfaccia)
    }

    private JPanel createHeader() {
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Area Amministratore");
        title.setFont(new Font("SansSerif", Font.BOLD, 30));
        title.setForeground(AppTheme.TEXT);

<<<<<<< HEAD
        JLabel subtitle = new JLabel("Analisi aggregate e controllo dello stato dei budget");
=======
        JLabel subtitle = new JLabel("Panoramica aggregata di utenti, movimenti, categorie e budget");
>>>>>>> 3351d66 (aggiornamento interfaccia)
        subtitle.setForeground(AppTheme.TEXT_MUTED);

        JLabel user = new JLabel(currentUser.getNome() + " " + currentUser.getCognome()
                + " - " + currentUser.getEmail());
        user.setForeground(AppTheme.TEXT_MUTED);

        header.add(title);
        header.add(Box.createVerticalStrut(8));
        header.add(subtitle);
        header.add(Box.createVerticalStrut(4));
        header.add(user);
        header.add(Box.createVerticalStrut(18));
        return header;
    }

<<<<<<< HEAD
=======
    private JScrollPane createScrollableContent() {
        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        final AdminSummary summary = loadSummary();
        final List<MonthlyAdminStat> monthlyStats = loadMonthlyStats();
        final List<CategoryUsage> categoryUsage = loadCategoryUsage();

        JPanel metrics = createMetrics(summary, categoryUsage);
        metrics.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        content.add(metrics);
        content.add(Box.createVerticalStrut(16));

        JPanel charts = new JPanel(new GridLayout(1, 2, 16, 16));
        charts.setOpaque(false);
        charts.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        charts.add(createChartCard(
                "Andamento mensile globale",
                "Entrate e spese aggregate degli ultimi 12 mesi disponibili.",
                new MonthlyChartPanel(monthlyStats)));
        charts.add(createChartCard(
                "Categorie più utilizzate",
                categorySubtitle(categoryUsage),
                new CategoryUsageChartPanel(categoryUsage)));
        content.add(charts);
        content.add(Box.createVerticalStrut(16));

        JTabbedPane tabs = createTabs();
        tabs.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        tabs.setPreferredSize(new Dimension(0, 360));
        content.add(tabs);

        JPanel topWrapper = new JPanel(new BorderLayout());
        topWrapper.setOpaque(false);
        topWrapper.add(content, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(topWrapper);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        return scrollPane;
    }

    private JPanel createMetrics(final AdminSummary summary,
            final List<CategoryUsage> categoryUsage) {
        JPanel metrics = new JPanel(new GridLayout(1, 4, 14, 14));
        metrics.setOpaque(false);
        metrics.add(createMetricBox("Utenti", String.valueOf(summary.getUsers()), AppTheme.PRIMARY));
        metrics.add(createMetricBox("Transazioni", String.valueOf(summary.getTransactions()), AppTheme.ACCENT));
        metrics.add(createMetricBox("Spese complessive", formatEuro(summary.getExpenses()), AppTheme.EXPENSE));

        final String budgetValue = summary.getExceededBudgets() == 1
                ? "1 superato"
                : summary.getExceededBudgets() + " superati";
        final String category = categoryUsage.isEmpty() ? "Nessun dato"
                : categoryUsage.get(0).getName() + " · " + categoryUsage.get(0).getUses() + " usi";
        metrics.add(createMetricBox("Budget / top categoria",
                "<html>" + budgetValue + "<br><span style='font-size:11px'>" + category + "</span></html>",
                AppTheme.BUDGET));
        return metrics;
    }

    private JPanel createMetricBox(final String title, final String value,
            final Color accent) {
        JPanel panel = new GlassPanel(new BorderLayout(0, 12));
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        panel.setPreferredSize(new Dimension(0, 115));

        JPanel accentBar = new JPanel();
        accentBar.setOpaque(true);
        accentBar.setBackground(accent);
        accentBar.setPreferredSize(new Dimension(0, 5));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(AppTheme.TEXT_MUTED);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 12));

        JLabel valueLabel = new JLabel(value);
        valueLabel.setForeground(AppTheme.TEXT);
        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 20));

        panel.add(accentBar, BorderLayout.NORTH);
        panel.add(titleLabel, BorderLayout.CENTER);
        panel.add(valueLabel, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createChartCard(final String title, final String subtitle,
            final JComponent chart) {
        JPanel panel = new GlassPanel(new BorderLayout(0, 12));
        panel.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        panel.setPreferredSize(new Dimension(0, 315));

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        titleLabel.setForeground(AppTheme.TEXT);

        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        subtitleLabel.setForeground(AppTheme.TEXT_MUTED);

        header.add(titleLabel);
        header.add(Box.createVerticalStrut(5));
        header.add(subtitleLabel);

        chart.setPreferredSize(new Dimension(360, 235));
        panel.add(header, BorderLayout.NORTH);
        panel.add(chart, BorderLayout.CENTER);
        return panel;
    }

    private String categorySubtitle(final List<CategoryUsage> categories) {
        if (categories.isEmpty()) {
            return "Nessuna spesa categorizzata disponibile.";
        }
        final CategoryUsage first = categories.get(0);
        return "Più usata: " + first.getName() + " (" + first.getUses() + " transazioni, "
                + formatEuro(first.getTotalExpenses()) + ").";
    }

>>>>>>> 3351d66 (aggiornamento interfaccia)
    private JTabbedPane createTabs() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Statistiche mensili", createTablePanel(loadStats()));
        tabs.addTab("Stato budget", createTablePanel(loadBudgetState()));
        return tabs;
    }

    private JPanel createTablePanel(final TableData data) {
        JPanel panel = new GlassPanel(new BorderLayout());
<<<<<<< HEAD
        panel.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        DefaultTableModel model = new DefaultTableModel(
                data.getColumns().toArray(new String[0]), 0);
=======
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        DefaultTableModel model = new DefaultTableModel(
                data.getColumns().toArray(new String[0]), 0) {
            @Override
            public boolean isCellEditable(final int row, final int column) {
                return false;
            }
        };
>>>>>>> 3351d66 (aggiornamento interfaccia)
        for (Object[] row : data.getRows()) {
            model.addRow(row);
        }

        JTable table = new JTable(model);
        table.setFillsViewportHeight(true);
        table.setRowHeight(28);
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(218, 226, 236)));
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

<<<<<<< HEAD
=======
    private AdminSummary loadSummary() {
        try {
            return adminService.caricaRiepilogo();
        } catch (final SQLException ex) {
            showLoadError(ex);
            return new AdminSummary(0, 0, BigDecimal.ZERO, BigDecimal.ZERO, 0);
        }
    }

    private List<MonthlyAdminStat> loadMonthlyStats() {
        try {
            return adminService.caricaAndamentoMensile();
        } catch (final SQLException ex) {
            showLoadError(ex);
            return List.of();
        }
    }

    private List<CategoryUsage> loadCategoryUsage() {
        try {
            return adminService.caricaCategoriePiuUsate();
        } catch (final SQLException ex) {
            showLoadError(ex);
            return List.of();
        }
    }

>>>>>>> 3351d66 (aggiornamento interfaccia)
    private TableData loadStats() {
        try {
            return adminService.caricaStatisticheAggregate();
        } catch (final SQLException ex) {
            showLoadError(ex);
<<<<<<< HEAD
            return new TableData(java.util.List.of(), java.util.List.of());
=======
            return new TableData(List.of(), List.of());
>>>>>>> 3351d66 (aggiornamento interfaccia)
        }
    }

    private TableData loadBudgetState() {
        try {
            return adminService.caricaStatoBudget();
        } catch (final SQLException ex) {
            showLoadError(ex);
<<<<<<< HEAD
            return new TableData(java.util.List.of(), java.util.List.of());
        }
    }

=======
            return new TableData(List.of(), List.of());
        }
    }

    private String formatEuro(final BigDecimal value) {
        return String.format(Locale.ITALY, "%.2f €", value == null ? BigDecimal.ZERO : value);
    }

>>>>>>> 3351d66 (aggiornamento interfaccia)
    private void showLoadError(final SQLException ex) {
        JOptionPane.showMessageDialog(this,
                "Errore durante il caricamento dei dati admin: " + ex.getMessage(),
                "Errore database",
                JOptionPane.ERROR_MESSAGE);
    }
<<<<<<< HEAD
=======

    private static String compactNumber(final double value) {
        if (value >= 1000.0) {
            return String.format(Locale.US, "%.1fk", value / 1000.0).replace('.', ',');
        }
        return String.format(Locale.US, "%.0f", value);
    }

    private static final class MonthlyChartPanel extends JPanel {
        private final List<MonthlyAdminStat> stats;

        private MonthlyChartPanel(final List<MonthlyAdminStat> stats) {
            this.stats = stats;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(final Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            if (stats.isEmpty()) {
                drawEmpty(g2, "Nessun movimento disponibile.");
                return;
            }

            int width = getWidth();
            int height = getHeight();
            int left = 42;
            int right = 12;
            int top = 25;
            int bottom = 32;
            int chartWidth = Math.max(1, width - left - right);
            int chartHeight = Math.max(1, height - top - bottom);
            int chartBottom = top + chartHeight;

            double max = 0.0;
            for (MonthlyAdminStat stat : stats) {
                max = Math.max(max, stat.getExpenses().doubleValue());
                max = Math.max(max, stat.getIncomes().doubleValue());
            }
            if (max <= 0.0) {
                drawEmpty(g2, "Nessun importo disponibile.");
                return;
            }

            for (int i = 0; i <= 4; i++) {
                int y = top + (int) Math.round(chartHeight * i / 4.0);
                g2.setColor(new Color(224, 232, 239));
                g2.drawLine(left, y, width - right, y);
                g2.setColor(AppTheme.TEXT_MUTED);
                g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
                g2.drawString(compactNumber(max * (4 - i) / 4.0), 2, y + 4);
            }

            double slot = chartWidth / (double) Math.max(1, stats.size());
            int barWidth = Math.max(5, Math.min(15, (int) (slot * 0.28)));
            for (int i = 0; i < stats.size(); i++) {
                MonthlyAdminStat stat = stats.get(i);
                int groupWidth = barWidth * 2 + 4;
                int baseX = left + (int) Math.round(i * slot + (slot - groupWidth) / 2.0);
                int incomeHeight = (int) Math.round(chartHeight * stat.getIncomes().doubleValue() / max);
                int expenseHeight = (int) Math.round(chartHeight * stat.getExpenses().doubleValue() / max);

                g2.setColor(AppTheme.INCOME);
                g2.fillRoundRect(baseX, chartBottom - incomeHeight, barWidth, incomeHeight, 6, 6);
                g2.setColor(AppTheme.EXPENSE);
                g2.fillRoundRect(baseX + barWidth + 4, chartBottom - expenseHeight,
                        barWidth, expenseHeight, 6, 6);

                g2.setColor(AppTheme.TEXT_MUTED);
                g2.setFont(new Font("SansSerif", Font.PLAIN, 9));
                String label = stat.getLabel();
                FontMetrics metrics = g2.getFontMetrics();
                int x = left + (int) Math.round((i + 0.5) * slot) - metrics.stringWidth(label) / 2;
                g2.drawString(label, x, height - 8);
            }

            g2.setColor(AppTheme.INCOME);
            g2.fillRoundRect(width - 132, 4, 11, 11, 4, 4);
            g2.setColor(AppTheme.TEXT_MUTED);
            g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
            g2.drawString("Entrate", width - 116, 13);
            g2.setColor(AppTheme.EXPENSE);
            g2.fillRoundRect(width - 64, 4, 11, 11, 4, 4);
            g2.setColor(AppTheme.TEXT_MUTED);
            g2.drawString("Spese", width - 48, 13);
            g2.dispose();
        }

        private void drawEmpty(final Graphics2D g2, final String text) {
            g2.setColor(AppTheme.TEXT_MUTED);
            g2.setFont(new Font("SansSerif", Font.PLAIN, 13));
            g2.drawString(text, 12, Math.max(25, getHeight() / 2));
            g2.dispose();
        }
    }

    private static final class CategoryUsageChartPanel extends JPanel {
        private final List<CategoryUsage> categories;

        private CategoryUsageChartPanel(final List<CategoryUsage> categories) {
            this.categories = categories;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(final Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            if (categories.isEmpty()) {
                g2.setColor(AppTheme.TEXT_MUTED);
                g2.setFont(new Font("SansSerif", Font.PLAIN, 13));
                g2.drawString("Nessuna categoria utilizzata.", 12, Math.max(25, getHeight() / 2));
                g2.dispose();
                return;
            }

            int width = getWidth();
            int height = getHeight();
            int left = 120;
            int right = 65;
            int top = 12;
            int rowHeight = Math.max(28, (height - top - 8) / categories.size());
            int maxUses = Math.max(1, categories.get(0).getUses());
            int availableWidth = Math.max(40, width - left - right);

            for (int i = 0; i < categories.size(); i++) {
                CategoryUsage category = categories.get(i);
                int y = top + i * rowHeight;
                int barY = y + 6;
                int barHeight = Math.max(14, rowHeight - 14);
                int barWidth = (int) Math.round(availableWidth * category.getUses() / (double) maxUses);

                g2.setColor(AppTheme.TEXT);
                g2.setFont(new Font("SansSerif", Font.BOLD, 11));
                String name = abbreviate(category.getName(), 17);
                g2.drawString(name, 4, barY + barHeight - 3);

                g2.setColor(new Color(226, 234, 241));
                g2.fillRoundRect(left, barY, availableWidth, barHeight, 8, 8);
                g2.setColor(i == 0 ? AppTheme.PRIMARY : AppTheme.ACCENT);
                g2.fillRoundRect(left, barY, Math.max(4, barWidth), barHeight, 8, 8);

                g2.setColor(AppTheme.TEXT_MUTED);
                g2.setFont(new Font("SansSerif", Font.BOLD, 11));
                g2.drawString(category.getUses() + " usi", left + availableWidth + 8,
                        barY + barHeight - 3);
            }
            g2.dispose();
        }

        private static String abbreviate(final String value, final int maxLength) {
            if (value == null || value.length() <= maxLength) {
                return value == null ? "" : value;
            }
            return value.substring(0, maxLength - 1) + "…";
        }
    }
>>>>>>> 3351d66 (aggiornamento interfaccia)
}
