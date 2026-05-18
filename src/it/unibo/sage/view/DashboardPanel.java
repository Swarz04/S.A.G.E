package it.unibo.sage.view;

import it.unibo.sage.controller.BudgetController;
import it.unibo.sage.controller.MovimentiController;
import it.unibo.sage.model.Budget;
import it.unibo.sage.model.Categoria;
import it.unibo.sage.model.Tag;
import it.unibo.sage.model.TipoTransazione;
import it.unibo.sage.model.Transazione;
import it.unibo.sage.model.Utente;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Schermata principale dopo il login: il menu laterale cambia le card centrali
 * e prepara la struttura per le funzioni vere dell'app.
 */
public class DashboardPanel extends AppBackgroundPanel {

    private static final String CARD_OVERVIEW = "CARD_OVERVIEW";
    private static final String CARD_SETTINGS = "CARD_SETTINGS";
    private static final int SIDEBAR_WIDTH = 300;
    private static final int MENU_BUTTON_HEIGHT = 56;

    private final CardLayout contentLayout;
    private final JPanel contentPanel;
    private final Utente currentUser;
    private final MovimentiController movimentiController = new MovimentiController();
    private final BudgetController budgetController = new BudgetController();
    private JButton selectedMenuButton;

    public DashboardPanel(final Utente currentUser) {
        super(new BorderLayout());
        this.currentUser = currentUser;

        contentLayout = new CardLayout();
        contentPanel = new JPanel(contentLayout);
        contentPanel.setOpaque(false);

        add(createMenuPanel(), BorderLayout.WEST);
        add(createMainPanel(), BorderLayout.CENTER);

        initContentCards();
        contentLayout.show(contentPanel, CARD_OVERVIEW);
    }

    private JPanel createMainPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setOpaque(false);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(24, 26, 24, 26));

        mainPanel.add(createHeaderPanel(), BorderLayout.NORTH);
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        return mainPanel;
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 22, 0));

        JLabel titleLabel = new JLabel("Area Utente");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
        titleLabel.setForeground(AppTheme.TEXT);

        JLabel subtitleLabel = new JLabel("Dashboard personale per spese, entrate, budget e classificazioni");
        subtitleLabel.setForeground(AppTheme.TEXT_MUTED);

        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.add(titleLabel);
        titlePanel.add(Box.createVerticalStrut(6));
        titlePanel.add(subtitleLabel);

        SoftButton newTransactionButton = new SoftButton("+ Nuova transazione");
        newTransactionButton.setBackground(AppTheme.ACCENT);
        newTransactionButton.setForeground(Color.WHITE);
        newTransactionButton.setBorder(BorderFactory.createEmptyBorder(11, 18, 11, 18));
        newTransactionButton.setFont(new Font("SansSerif", Font.BOLD, 13));
        newTransactionButton.setArc(16);
        newTransactionButton.addMouseListener(new ButtonHoverAdapter(
            newTransactionButton,
            AppTheme.ACCENT,
            AppTheme.ACCENT_HOVER
        ));

        headerPanel.add(titlePanel, BorderLayout.WEST);
        headerPanel.add(newTransactionButton, BorderLayout.EAST);

        return headerPanel;
    }

    private JPanel createMenuPanel() {
        JPanel menuPanel = new SidebarPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setPreferredSize(new Dimension(SIDEBAR_WIDTH, 0));
        menuPanel.setBorder(BorderFactory.createEmptyBorder(24, 22, 24, 22));

        JLabel appName = new JLabel("S.A.G.E.");
        appName.setForeground(Color.WHITE);
        appName.setFont(new Font("SansSerif", Font.BOLD, 25));
        appName.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel appCaption = new JLabel("Personal finance");
        appCaption.setForeground(AppTheme.SIDEBAR_CAPTION);
        appCaption.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sectionLabel = new JLabel("Workspace");
        sectionLabel.setForeground(AppTheme.SIDEBAR_SECTION);
        sectionLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
        sectionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        menuPanel.add(appName);
        menuPanel.add(Box.createVerticalStrut(4));
        menuPanel.add(appCaption);
        menuPanel.add(Box.createVerticalStrut(28));
        menuPanel.add(sectionLabel);
        menuPanel.add(Box.createVerticalStrut(10));
        menuPanel.add(createMenuButton("Riepilogo", CARD_OVERVIEW));
        menuPanel.add(createMenuButton("Transazioni", "CARD_TRANSACTIONS"));
        menuPanel.add(createMenuButton("Budget", "CARD_BUDGET"));
        menuPanel.add(createMenuButton("Categorie e Tag", "CARD_CATEGORIES"));
        menuPanel.add(createMenuButton("Ricorrenze", "CARD_RECURRING"));
        menuPanel.add(createMenuButton("Documenti", "CARD_DOCUMENTS"));
        menuPanel.add(Box.createVerticalGlue());
        menuPanel.add(createUserBox());

        return menuPanel;
    }

    private JPanel createUserBox() {
        JPanel userBox = new GlassPanel(
            new BorderLayout(10, 0),
            AppTheme.SIDEBAR_USER_TOP,
            AppTheme.SIDEBAR_USER_BOTTOM,
            false
        );
        userBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        userBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 66));
        userBox.setMinimumSize(new Dimension(0, 66));
        userBox.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        userBox.setToolTipText("Apri impostazioni");
        userBox.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel avatar = new JLabel("U", SwingConstants.CENTER);
        avatar.setOpaque(true);
        avatar.setPreferredSize(new Dimension(38, 38));
        avatar.setBackground(AppTheme.AVATAR_BACKGROUND);
        avatar.setForeground(AppTheme.AVATAR_TEXT);
        avatar.setFont(new Font("SansSerif", Font.BOLD, 16));
        avatar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        avatar.setToolTipText("Apri impostazioni");

        JLabel name = new JLabel(currentUser.getNome() + " " + currentUser.getCognome());
        name.setForeground(Color.WHITE);
        name.setFont(new Font("SansSerif", Font.BOLD, 15));
        name.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        name.setToolTipText("Apri impostazioni");

        java.awt.event.MouseAdapter settingsClick = new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent event) {
                showSettings();
            }
        };
        userBox.addMouseListener(settingsClick);
        avatar.addMouseListener(settingsClick);
        name.addMouseListener(settingsClick);

        userBox.add(avatar, BorderLayout.WEST);
        userBox.add(name, BorderLayout.CENTER);

        return userBox;
    }

    private JButton createMenuButton(String text, String cardName) {
        SoftButton button = new SoftButton(text);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, MENU_BUTTON_HEIGHT));
        button.setMinimumSize(new Dimension(0, MENU_BUTTON_HEIGHT));
        button.setPreferredSize(new Dimension(0, MENU_BUTTON_HEIGHT));
        button.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setBackground(AppTheme.SIDEBAR_BUTTON);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("SansSerif", Font.BOLD, 16));
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent event) {
                if (button != selectedMenuButton) {
                    button.setBackground(AppTheme.SIDEBAR_BUTTON_HOVER);
                }
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent event) {
                if (button != selectedMenuButton) {
                    button.setBackground(AppTheme.SIDEBAR_BUTTON);
                }
            }
        });
        button.addActionListener(e -> {
            selectMenuButton(button);
            contentLayout.show(contentPanel, cardName);
        });

        if (selectedMenuButton == null) {
            selectMenuButton(button);
        }

        return button;
    }

    private void selectMenuButton(JButton button) {
        if (selectedMenuButton != null) {
            selectedMenuButton.setBackground(AppTheme.SIDEBAR_BUTTON);
        }

        selectedMenuButton = button;
        if (selectedMenuButton != null) {
            selectedMenuButton.setBackground(AppTheme.SIDEBAR_BUTTON_SELECTED);
        }
    }

    private void showSettings() {
        selectMenuButton(null);
        contentLayout.show(contentPanel, CARD_SETTINGS);
    }

    private void initContentCards() {
        contentPanel.add(createOverviewCard(), "CARD_OVERVIEW");
        contentPanel.add(createPlaceholderCard(
            "Transazioni",
            "Entrate e spese saranno filtrabili per periodo, categoria e tag."
        ), "CARD_TRANSACTIONS");
        contentPanel.add(createPlaceholderCard(
            "Budget",
            "Budget mensili, totale speso ridondante e soglie per categoria."
        ), "CARD_BUDGET");
        contentPanel.add(createPlaceholderCard(
            "Categorie e Tag",
            "Classificazioni di sistema e personali, coerenti con le gerarchie ISA."
        ), "CARD_CATEGORIES");
        contentPanel.add(createPlaceholderCard(
            "Spese Ricorrenti",
            "Modelli astratti da cui generare spese effettive periodiche."
        ), "CARD_RECURRING");
        contentPanel.add(new DocumentiPanel(currentUser), "CARD_DOCUMENTS");
        contentPanel.add(createSettingsCard(), CARD_SETTINGS);
    }

    private JPanel createOverviewCard() {
        final List<Transazione> transazioni = loadTransactions();
        final List<Budget> budgets = loadBudgets();
        final BigDecimal entrate = sumByType(transazioni, TipoTransazione.ENTRATA);
        final BigDecimal spese = sumByType(transazioni, TipoTransazione.SPESA);
        final BigDecimal saldo = entrate.subtract(spese);
        final String budgetUsage = calculateBudgetUsage(budgets);

        JPanel panel = new JPanel(new BorderLayout(0, 18));
        panel.setOpaque(false);

        JPanel metricsPanel = new JPanel(new GridLayout(1, 4, 16, 16));
        metricsPanel.setOpaque(false);
        metricsPanel.add(createMetricBox("Saldo attuale", "0,00 euro", AppTheme.PRIMARY));
        metricsPanel.add(createMetricBox("Entrate mese", "0,00 euro", AppTheme.INCOME));
        metricsPanel.add(createMetricBox("Spese mese", "0,00 euro", AppTheme.EXPENSE));
        metricsPanel.add(createMetricBox("Budget usato", "0%", AppTheme.BUDGET));

        JPanel lowerPanel = new JPanel(new GridLayout(1, 2, 16, 16));
        lowerPanel.setOpaque(false);
        lowerPanel.add(createPlaceholderBox("Ultime transazioni", "Nessuna transazione registrata."));
        lowerPanel.add(createPlaceholderBox("Budget del mese", "Nessun budget configurato."));

        panel.add(new SummaryPanel(), BorderLayout.NORTH);
        panel.add(lowerPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createMetricBox(String title, String value, Color accent) {
        JPanel panel = new GlassPanel(new BorderLayout(0, 14));
        panel.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(AppTheme.TEXT_MUTED);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 13));

        JLabel valueLabel = new JLabel(value);
        valueLabel.setForeground(AppTheme.TEXT);
        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 24));

        JPanel accentBar = new JPanel();
        accentBar.setPreferredSize(new Dimension(0, 5));
        accentBar.setBackground(accent);
        accentBar.setOpaque(true);

        panel.add(accentBar, BorderLayout.NORTH);
        panel.add(titleLabel, BorderLayout.CENTER);
        panel.add(valueLabel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createPlaceholderBox(String title, String description) {
        JPanel panel = new GlassPanel(new BorderLayout(0, 12));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        titleLabel.setForeground(AppTheme.TEXT);

        JLabel descriptionLabel = new JLabel(description);
        descriptionLabel.setForeground(AppTheme.TEXT_MUTED);

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(descriptionLabel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createMiniTransactionsBox(final List<Transazione> transazioni) {
        final StringBuilder text = new StringBuilder("<html>");
        final int limit = Math.min(4, transazioni.size());
        for (int i = 0; i < limit; i++) {
            final Transazione transazione = transazioni.get(i);
            text.append(transazione.getData())
                    .append(" - ")
                    .append(transazione.getDescrizione())
                    .append(" - ")
                    .append(formatEuro(transazione.getImporto()))
                    .append("<br>");
        }
        if (limit == 0) {
            text.append("Nessuna transazione caricata.");
        }
        text.append("</html>");
        return createPlaceholderBox("Ultime transazioni", text.toString());
    }

    private JPanel createMiniBudgetBox(final List<Budget> budgets) {
        if (budgets.isEmpty()) {
            return createPlaceholderBox("Budget", "Nessun budget configurato.");
        }
        final Budget first = budgets.get(0);
        return createPlaceholderBox("Budget", "Limite " + formatEuro(first.getImportoLimite())
                + ", speso " + formatEuro(first.getTotaleSpesoAttuale()));
    }

    private JPanel createTableCard(final String title, final String subtitle,
            final DefaultTableModel model) {
        JPanel panel = new GlassPanel(new BorderLayout(0, 14));
        panel.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        titleLabel.setForeground(AppTheme.TEXT);

        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setForeground(AppTheme.TEXT_MUTED);

        header.add(titleLabel);
        header.add(Box.createVerticalStrut(6));
        header.add(subtitleLabel);

        JTable table = new JTable(model);
        table.setFillsViewportHeight(true);
        table.setRowHeight(28);
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(218, 226, 236)));

        panel.add(header, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createPlaceholderCard(String title, String description) {
        JPanel panel = new GlassPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 26));
        titleLabel.setForeground(AppTheme.TEXT);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel descriptionLabel = new JLabel(description);
        descriptionLabel.setForeground(AppTheme.TEXT_MUTED);
        descriptionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        textPanel.add(titleLabel);
        textPanel.add(Box.createVerticalStrut(12));
        textPanel.add(descriptionLabel);

        panel.add(textPanel);

        return panel;
    }
}
