package it.unibo.sage.view.dashboard;

import it.unibo.sage.controller.MovimentiController;
import it.unibo.sage.controller.BudgetController;
import it.unibo.sage.controller.SpeseRicorrentiController;
import it.unibo.sage.model.Budget;
import it.unibo.sage.model.Categoria;
import it.unibo.sage.model.Fonte;
import it.unibo.sage.model.SpesaRicorrente;
import it.unibo.sage.model.Tag;
import it.unibo.sage.model.TipoTransazione;
import it.unibo.sage.model.Transazione;
import it.unibo.sage.model.Utente;
import it.unibo.sage.service.DashboardData;
import it.unibo.sage.service.DashboardDataService;
import it.unibo.sage.service.DashboardOverviewCalculator;
import it.unibo.sage.service.DashboardOverviewCalculator.DayExpense;
import it.unibo.sage.service.DashboardOverviewCalculator.MonthTotals;
import it.unibo.sage.service.DashboardOverviewCalculator.OverviewFilter;
import it.unibo.sage.view.components.AppBackgroundPanel;
import it.unibo.sage.view.components.ButtonHoverAdapter;
import it.unibo.sage.view.components.GlassPanel;
import it.unibo.sage.view.components.SidebarPanel;
import it.unibo.sage.view.components.SoftButton;
import it.unibo.sage.view.documents.DocumentiPanel;
import it.unibo.sage.view.theme.AppTheme;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.io.IOException;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Schermata principale dopo il login: il menu laterale cambia le card centrali
 * e prepara la struttura per le funzioni vere dell'app.
 */
public class DashboardPanel extends AppBackgroundPanel {

    private static final String CARD_SETTINGS = "CARD_SETTINGS";
    private static final int SIDEBAR_WIDTH = 300;
    private static final int MENU_BUTTON_HEIGHT = 56;

    private final CardLayout contentLayout;
    private final JPanel contentPanel;
    private final Utente currentUser;
    private final MovimentiController movimentiController = new MovimentiController();
    private final BudgetController budgetController = new BudgetController();
    private final SpeseRicorrentiController speseRicorrentiController = new SpeseRicorrentiController();
    private final DashboardDataService dashboardDataService = new DashboardDataService();
    private final DashboardOverviewCalculator overviewCalculator = new DashboardOverviewCalculator();
    private DashboardData dashboardData = DashboardData.empty();
    private JButton selectedMenuButton;
    private OverviewFilter overviewFilter = OverviewFilter.MONTH;

    public DashboardPanel(final Utente currentUser) {
        super(new BorderLayout());
        this.currentUser = currentUser;

        contentLayout = new CardLayout();
        contentPanel = new JPanel(contentLayout);
        contentPanel.setOpaque(false);

        add(createMenuPanel(), BorderLayout.WEST);
        add(createMainPanel(), BorderLayout.CENTER);

        generateDueRecurringExpensesOnStartup();
        reloadDashboardData();
        initContentCards();
        contentLayout.show(contentPanel, "CARD_OVERVIEW");
    }

    private JPanel createMainPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setOpaque(false);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(24, 26, 24, 26));

        mainPanel.add(createHeaderPanel(), BorderLayout.NORTH);

        // Avvolgo il contentPanel in uno JScrollPane per permettere lo scorrimento
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

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
        newTransactionButton.addActionListener(e -> showNewTransactionDialog());

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
        menuPanel.add(createMenuButton("Riepilogo", "CARD_OVERVIEW"));
        menuPanel.add(createMenuButton("Transazioni", "CARD_TRANSACTIONS"));
        menuPanel.add(createMenuButton("Budget", "CARD_BUDGET"));
        menuPanel.add(createMenuButton("Categorie e Tag", "CARD_CATEGORIES"));
        menuPanel.add(createMenuButton("Fonti", "CARD_SOURCES"));
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
        contentPanel.add(createTransactionsCard(), "CARD_TRANSACTIONS");
        contentPanel.add(createBudgetCard(), "CARD_BUDGET");
        contentPanel.add(createCategoriesCard(), "CARD_CATEGORIES");
        contentPanel.add(createSourcesCard(), "CARD_SOURCES");
        contentPanel.add(createRecurringCard(), "CARD_RECURRING");
        contentPanel.add(new DocumentiPanel(currentUser), "CARD_DOCUMENTS");
        contentPanel.add(createSettingsCard(), CARD_SETTINGS);
    }

    private void refreshContent(final String cardName) {
        reloadDashboardData();
        contentPanel.removeAll();
        initContentCards();
        contentPanel.revalidate();
        contentPanel.repaint();
        contentLayout.show(contentPanel, cardName);
    }

    private void reloadDashboardData() {
        try {
            dashboardData = dashboardDataService.loadForUser(currentUser.getEmail());
        } catch (final SQLException ex) {
            dashboardData = DashboardData.empty();
            showLoadError("dashboard", ex);
        }
    }

    private JPanel createOverviewCard() {
        final List<Transazione> allTransactions = loadTransactions();
        final List<Transazione> transazioni =
                overviewCalculator.filterTransactions(allTransactions, overviewFilter);
        final List<Budget> budgets = loadBudgets();
        final Map<Long, String> categoryNames = loadCategoryNames();
        final BigDecimal entrate = overviewCalculator.sumByType(
                transazioni, TipoTransazione.ENTRATA);
        final BigDecimal spese = overviewCalculator.sumByType(
                transazioni, TipoTransazione.SPESA);
        final BigDecimal saldo = entrate.subtract(spese);
        final String budgetUsage = overviewCalculator.calculateBudgetUsage(budgets);

        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JPanel metricsPanel = new JPanel(new GridLayout(1, 4, 16, 16));
        metricsPanel.setOpaque(false);
        metricsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        metricsPanel.add(createMetricBox("Saldo attuale", formatEuro(saldo), AppTheme.PRIMARY));
        metricsPanel.add(createMetricBox("Entrate", formatEuro(entrate), AppTheme.INCOME));
        metricsPanel.add(createMetricBox("Spese", formatEuro(spese), AppTheme.EXPENSE));
        metricsPanel.add(createMetricBox("Budget usato", budgetUsage, AppTheme.BUDGET));

        JPanel dashboardGrid = new JPanel(new GridLayout(0, 2, 16, 16));
        dashboardGrid.setOpaque(false);
        dashboardGrid.setAlignmentX(Component.LEFT_ALIGNMENT);
        dashboardGrid.add(createMonthlyTrendCard(transazioni));
        dashboardGrid.add(createExpenseDistributionCard(transazioni, categoryNames));
        dashboardGrid.add(createBudgetProgressCard(budgets, categoryNames));
        dashboardGrid.add(createRecentTransactionsListCard(transazioni));

        JPanel filterPanel = createOverviewFilterPanel();
        filterPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                filterPanel.getPreferredSize().height));
        metricsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 132));
        dashboardGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                dashboardGrid.getPreferredSize().height));

        panel.add(filterPanel);
        panel.add(Box.createVerticalStrut(14));
        panel.add(metricsPanel);
        panel.add(Box.createVerticalStrut(16));
        panel.add(dashboardGrid);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(panel, BorderLayout.NORTH);
        return wrapper;
    }

    private JPanel createTransactionsCard() {
        final List<Transazione> transazioni = loadTransactions();
        final Map<Long, Categoria> categoriesById = new LinkedHashMap<>();
        for (Categoria categoria : loadCategories()) {
            categoriesById.put(categoria.getId(), categoria);
        }
        final Map<Long, Fonte> sourcesById = new LinkedHashMap<>();
        for (Fonte fonte : loadSources()) {
            sourcesById.put(fonte.getId(), fonte);
        }

        JPanel panel = new GlassPanel(new BorderLayout(0, 14));
        panel.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel("Transazioni");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        titleLabel.setForeground(AppTheme.TEXT);

        JLabel subtitleLabel = new JLabel(
                "Movimenti registrati per " + currentUser.getEmail()
                + ". Ogni scheda puo' essere modificata o eliminata.");
        subtitleLabel.setForeground(AppTheme.TEXT_MUTED);

        titlePanel.add(titleLabel);
        titlePanel.add(Box.createVerticalStrut(6));
        titlePanel.add(subtitleLabel);

        JPanel grid = new JPanel(new GridLayout(0, 2, 14, 14));
        grid.setOpaque(false);
        for (Transazione transazione : transazioni) {
            grid.add(createTransactionTile(transazione, categoriesById, sourcesById));
        }

        if (grid.getComponentCount() == 0) {
            grid.add(createPlaceholderBox("Nessuna transazione",
                    "Aggiungi il primo movimento con il pulsante Nuova transazione."));
        }

        panel.add(titlePanel, BorderLayout.NORTH);
        panel.add(createClassificationScrollPane(grid), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createBudgetCard() {
        final List<Budget> budgets = loadBudgets();
        final Map<Long, String> categoryNames = loadCategoryNames();

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actions.setOpaque(false);
        SoftButton addBudgetButton = createSmallActionButton("+ Budget");
        addBudgetButton.addActionListener(e -> showAddBudgetDialog());
        actions.add(addBudgetButton);

        JPanel panel = new GlassPanel(new BorderLayout(0, 18));
        panel.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setOpaque(false);
        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel("Budget");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        titleLabel.setForeground(AppTheme.TEXT);

        JLabel subtitleLabel = new JLabel("Imposta e controlla i limiti mensili e per categoria.");
        subtitleLabel.setForeground(AppTheme.TEXT_MUTED);

        titlePanel.add(titleLabel);
        titlePanel.add(Box.createVerticalStrut(6));
        titlePanel.add(subtitleLabel);
        header.add(titlePanel, BorderLayout.CENTER);
        header.add(actions, BorderLayout.EAST);

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        if (budgets.isEmpty()) {
            content.add(createPlaceholderBox("Nessun budget configurato",
                    "Crea un budget mensile o per categoria con il pulsante Budget."));
        } else {
            for (int i = 0; i < budgets.size(); i++) {
                content.add(createBudgetProgressRow(budgets.get(i), categoryNames, true));
                if (i < budgets.size() - 1) {
                    content.add(Box.createVerticalStrut(18));
                }
            }
        }

        panel.add(header, BorderLayout.NORTH);
        panel.add(createClassificationScrollPane(content), BorderLayout.CENTER);
        return panel;
    }
    private JPanel createRecurringCard() {
        final List<SpesaRicorrente> ricorrenze = loadRecurringExpenses();
        final Map<Long, Categoria> categoriesById = new LinkedHashMap<>();
        for (Categoria categoria : loadCategories()) {
            categoriesById.put(categoria.getId(), categoria);
        }

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actions.setOpaque(false);
        SoftButton addButton = createSmallActionButton("+ Ricorrenza");
        addButton.addActionListener(e -> showAddRecurringDialog());
        actions.add(addButton);

        JPanel panel = new GlassPanel(new BorderLayout(0, 14));
        panel.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setOpaque(false);
        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel("Spese Ricorrenti");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        titleLabel.setForeground(AppTheme.TEXT);

        JLabel subtitleLabel = new JLabel(
                "Clicca una ricorrenza per vedere tutte le transazioni che ha generato.");
        subtitleLabel.setForeground(AppTheme.TEXT_MUTED);

        titlePanel.add(titleLabel);
        titlePanel.add(Box.createVerticalStrut(6));
        titlePanel.add(subtitleLabel);
        header.add(titlePanel, BorderLayout.CENTER);
        header.add(actions, BorderLayout.EAST);

        JPanel grid = new JPanel(new GridLayout(0, 2, 14, 14));
        grid.setOpaque(false);
        for (SpesaRicorrente ricorrenza : ricorrenze) {
            grid.add(createRecurringTile(ricorrenza, categoriesById));
        }

        if (grid.getComponentCount() == 0) {
            grid.add(createPlaceholderBox("Nessuna ricorrenza",
                    "Aggiungi una spesa periodica con il pulsante Ricorrenza."));
        }

        panel.add(header, BorderLayout.NORTH);
        panel.add(createClassificationScrollPane(grid), BorderLayout.CENTER);
        return panel;
    }


    private JPanel createCategoriesCard() {
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actions.setOpaque(false);
        SoftButton addCategoryButton = createSmallActionButton("+ Categoria");
        SoftButton addTagButton = createSmallActionButton("+ Tag");
        addCategoryButton.addActionListener(e -> addPersonalCategory());
        addTagButton.addActionListener(e -> addPersonalTag());
        actions.add(addCategoryButton);
        actions.add(addTagButton);

        JPanel panel = new GlassPanel(new BorderLayout(0, 14));
        panel.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setOpaque(false);
        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel("Categorie e Tag");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        titleLabel.setForeground(AppTheme.TEXT);

        JLabel subtitleLabel = new JLabel("Classificazioni disponibili per l'utente corrente.");
        subtitleLabel.setForeground(AppTheme.TEXT_MUTED);

        titlePanel.add(titleLabel);
        titlePanel.add(Box.createVerticalStrut(6));
        titlePanel.add(subtitleLabel);
        header.add(titlePanel, BorderLayout.CENTER);
        header.add(actions, BorderLayout.EAST);

        JPanel grid = new JPanel(new GridLayout(0, 2, 14, 14));
        grid.setOpaque(false);
        for (Categoria categoria : loadCategories()) {
            grid.add(createClassificationTile(
                    "Categoria",
                    categoria.getId(),
                    categoria.getNome(),
                    categoria.isSystem(),
                    categoria.getIcona()));
        }
        for (Tag tag : loadTags()) {
            grid.add(createClassificationTile(
                    "Tag",
                    tag.getId(),
                    tag.getNome(),
                    tag.isSystem(),
                    tag.getIcona()));
        }

        if (grid.getComponentCount() == 0) {
            grid.add(createPlaceholderBox("Nessuna classificazione", "Aggiungi categorie o tag personali."));
        }

        JScrollPane scrollPane = createClassificationScrollPane(grid);

        panel.add(header, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createSourcesCard() {
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actions.setOpaque(false);
        SoftButton addSourceButton = createSmallActionButton("+ Fonte");
        addSourceButton.addActionListener(e -> addPersonalSource());
        actions.add(addSourceButton);

        JPanel panel = new GlassPanel(new BorderLayout(0, 14));
        panel.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setOpaque(false);
        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel("Fonti");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        titleLabel.setForeground(AppTheme.TEXT);

        JLabel subtitleLabel = new JLabel("Fonti disponibili per classificare le entrate dell'utente corrente.");
        subtitleLabel.setForeground(AppTheme.TEXT_MUTED);

        titlePanel.add(titleLabel);
        titlePanel.add(Box.createVerticalStrut(6));
        titlePanel.add(subtitleLabel);
        header.add(titlePanel, BorderLayout.CENTER);
        header.add(actions, BorderLayout.EAST);

        JPanel grid = new JPanel(new GridLayout(0, 2, 14, 14));
        grid.setOpaque(false);
        for (Fonte fonte : loadSources()) {
            grid.add(createClassificationTile(
                    "Fonte",
                    fonte.getId(),
                    fonte.getNome(),
                    fonte.isSystem(),
                    fonte.getIcona()));
        }

        if (grid.getComponentCount() == 0) {
            grid.add(createPlaceholderBox("Nessuna fonte", "Aggiungi una fonte personale per le entrate."));
        }

        JScrollPane scrollPane = createClassificationScrollPane(grid);

        panel.add(header, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JScrollPane createClassificationScrollPane(final JPanel grid) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(grid, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(wrapper);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        return scrollPane;
    }

    private JPanel createClassificationTile(final String type, final long id,
            final String name, final boolean system) {
        return createClassificationTile(type, id, name, system, null);
    }

    private JPanel createClassificationTile(final String type, final long id,
            final String name, final boolean system, final String iconName) {
        final JPanel tile = new GlassPanel(new BorderLayout(14, 0));
        tile.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        tile.setPreferredSize(new Dimension(0, 104));

        final JComponent icon = createClassificationIcon(type, name, iconName);
        tile.add(icon, BorderLayout.WEST);

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(new Font("SansSerif", Font.BOLD, 17));
        nameLabel.setForeground(AppTheme.TEXT);

        JLabel metaLabel = new JLabel(type + " - " + (system ? "Sistema" : "Personale"));
        metaLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        metaLabel.setForeground(system ? AppTheme.TEXT_MUTED : AppTheme.BADGE_TEXT);

        textPanel.add(Box.createVerticalGlue());
        textPanel.add(nameLabel);
        textPanel.add(Box.createVerticalStrut(6));
        textPanel.add(metaLabel);
        textPanel.add(Box.createVerticalGlue());
        tile.add(textPanel, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new GridLayout(2, 1, 0, 8));
        buttons.setOpaque(false);
        SoftButton renameButton = createTinyActionButton("Modifica");
        SoftButton deleteButton = createTinyActionButton("Elimina");
        renameButton.addActionListener(e -> editClassification(type, id, name, iconName));
        deleteButton.addActionListener(e -> deleteClassification(type, id));
        buttons.add(renameButton);
        buttons.add(deleteButton);
        tile.add(buttons, BorderLayout.EAST);

        installClassificationClick(tile, type, id, name);
        return tile;
    }

    private JPanel createTransactionTile(final Transazione transazione,
            final Map<Long, Categoria> categoriesById, final Map<Long, Fonte> sourcesById) {
        final JPanel tile = new GlassPanel(new BorderLayout(14, 0));
        tile.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        tile.setPreferredSize(new Dimension(0, 122));

        final boolean expense = transazione.getTipo() == TipoTransazione.SPESA;
        final Categoria categoria = transazione.getIdCategoria() == null
                ? null : categoriesById.get(transazione.getIdCategoria());
        final Fonte fonte = transazione.getIdFonte() == null
                ? null : sourcesById.get(transazione.getIdFonte());
        final String classificationName = expense
                ? (categoria == null ? "Senza categoria" : categoria.getNome())
                : (fonte == null ? "Senza fonte" : fonte.getNome());
        final String iconName = expense
                ? (categoria == null ? "generic_category.png" : categoria.getIcona())
                : (fonte == null ? "generic_source.png" : fonte.getIcona());
        tile.add(createClassificationIcon(expense ? "Categoria" : "Fonte",
                classificationName, iconName), BorderLayout.WEST);

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

        JLabel descriptionLabel = new JLabel(transazione.getDescrizione());
        descriptionLabel.setFont(new Font("SansSerif", Font.BOLD, 17));
        descriptionLabel.setForeground(AppTheme.TEXT);

        JLabel amountLabel = new JLabel(formatEuro(transazione.getImporto()));
        amountLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        amountLabel.setForeground(expense ? AppTheme.EXPENSE : AppTheme.INCOME);

        String recurringText = transazione.getIdRicorrenza() == null ? "" : " - Ricorrente";
        JLabel metaLabel = new JLabel(labelTipo(transazione.getTipo()) + " - "
                + classificationName + recurringText);
        metaLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        metaLabel.setForeground(AppTheme.TEXT_MUTED);

        JLabel dateLabel = new JLabel("Data: " + transazione.getData());
        dateLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        dateLabel.setForeground(AppTheme.TEXT_MUTED);

        textPanel.add(Box.createVerticalGlue());
        textPanel.add(descriptionLabel);
        textPanel.add(Box.createVerticalStrut(5));
        textPanel.add(amountLabel);
        textPanel.add(Box.createVerticalStrut(5));
        textPanel.add(metaLabel);
        textPanel.add(Box.createVerticalStrut(3));
        textPanel.add(dateLabel);
        textPanel.add(Box.createVerticalGlue());
        tile.add(textPanel, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new GridLayout(2, 1, 0, 8));
        buttons.setOpaque(false);
        SoftButton editButton = createTinyActionButton("Modifica");
        SoftButton deleteButton = createTinyActionButton("Elimina");
        editButton.addActionListener(e -> showEditDialog(transazione));
        deleteButton.addActionListener(e -> deleteTransaction(transazione));
        buttons.add(editButton);
        buttons.add(deleteButton);
        tile.add(buttons, BorderLayout.EAST);

        return tile;
    }

    private JPanel createRecurringTile(final SpesaRicorrente ricorrenza,
            final Map<Long, Categoria> categoriesById) {
        final JPanel tile = new GlassPanel(new BorderLayout(14, 0));
        tile.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        tile.setPreferredSize(new Dimension(0, 132));

        final Categoria categoria = categoriesById.get(ricorrenza.getIdCategoria());
        final String categoryName = categoria == null
                ? "Categoria " + ricorrenza.getIdCategoria() : categoria.getNome();
        final String iconName = categoria == null ? "generic_category.png" : categoria.getIcona();
        tile.add(createClassificationIcon("Categoria", categoryName, iconName), BorderLayout.WEST);

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

        JLabel nameLabel = new JLabel(ricorrenza.getNome());
        nameLabel.setFont(new Font("SansSerif", Font.BOLD, 17));
        nameLabel.setForeground(AppTheme.TEXT);

        JLabel amountLabel = new JLabel(formatEuro(ricorrenza.getImportoPrevisto()));
        amountLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        amountLabel.setForeground(AppTheme.EXPENSE);

        JLabel metaLabel = new JLabel(categoryName + " - ogni "
                + ricorrenza.getFrequenzaGiorni() + " giorni");
        metaLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        metaLabel.setForeground(AppTheme.TEXT_MUTED);

        String endText = ricorrenza.getScadenza() == null ? "senza fine"
                : "fino al " + ricorrenza.getScadenza();
        JLabel datesLabel = new JLabel("Inizio " + ricorrenza.getDataInizio()
                + " - prossima " + ricorrenza.getDataProssimaScadenza()
                + " - " + endText);
        datesLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        datesLabel.setForeground(AppTheme.TEXT_MUTED);

        textPanel.add(Box.createVerticalGlue());
        textPanel.add(nameLabel);
        textPanel.add(Box.createVerticalStrut(5));
        textPanel.add(amountLabel);
        textPanel.add(Box.createVerticalStrut(5));
        textPanel.add(metaLabel);
        textPanel.add(Box.createVerticalStrut(3));
        textPanel.add(datesLabel);
        textPanel.add(Box.createVerticalGlue());
        tile.add(textPanel, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new GridLayout(2, 1, 0, 8));
        buttons.setOpaque(false);
        SoftButton editButton = createTinyActionButton("Modifica");
        SoftButton deleteButton = createTinyActionButton("Elimina");
        editButton.addActionListener(e -> showEditRecurringDialog(ricorrenza));
        deleteButton.addActionListener(e -> deleteRecurringExpense(ricorrenza));
        buttons.add(editButton);
        buttons.add(deleteButton);
        tile.add(buttons, BorderLayout.EAST);

        installRecurringClick(tile, ricorrenza);
        return tile;
    }

    private void installRecurringClick(final Component component,
            final SpesaRicorrente ricorrenza) {
        if (component instanceof AbstractButton) {
            return;
        }
        component.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        if (component instanceof JComponent) {
            ((JComponent) component).setToolTipText(
                    "Mostra tutte le spese generate da " + ricorrenza.getNome());
        }
        component.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(final java.awt.event.MouseEvent event) {
                if (SwingUtilities.isLeftMouseButton(event)) {
                    showRecurringTransactions(ricorrenza);
                }
            }
        });
        if (component instanceof Container) {
            for (Component child : ((Container) component).getComponents()) {
                installRecurringClick(child, ricorrenza);
            }
        }
    }

    private void showRecurringTransactions(final SpesaRicorrente ricorrenza) {
        try {
            final List<Transazione> transazioni =
                    dashboardDataService.loadTransactionsForRecurringExpense(
                            currentUser.getEmail(), ricorrenza.getId());
            showClassificationTransactionsDialog(
                    "Ricorrenza", ricorrenza.getNome(), transazioni);
        } catch (final SQLException ex) {
            showLoadError("spese generate da " + ricorrenza.getNome(), ex);
        }
    }

    private void deleteTransaction(final Transazione transazione) {
        final int confirm = JOptionPane.showConfirmDialog(this,
                "Eliminare definitivamente la transazione "
                + transazione.getDescrizione() + " del " + transazione.getData() + "?",
                "Conferma eliminazione",
                JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            movimentiController.eliminaTransazione(
                    currentUser.getEmail(), transazione.getId());
            refreshContent("CARD_TRANSACTIONS");
        } catch (final Exception ex) {
            showLoadError("eliminazione transazione", new SQLException(ex.getMessage()));
        }
    }

    private void deleteRecurringExpense(final SpesaRicorrente ricorrenza) {
        final int confirm = JOptionPane.showConfirmDialog(this,
                "Eliminare la ricorrenza " + ricorrenza.getNome()
                + "? Le transazioni gia' generate restano nello storico.",
                "Conferma eliminazione",
                JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            speseRicorrentiController.eliminaRicorrenza(
                    currentUser.getEmail(), ricorrenza.getId());
            refreshContent("CARD_RECURRING");
        } catch (final Exception ex) {
            showRecurringError(ex);
        }
    }

    private void installClassificationClick(final Component component, final String type,
            final long id, final String name) {
        if (component instanceof AbstractButton) {
            return;
        }

        component.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        if (component instanceof JComponent) {
            ((JComponent) component).setToolTipText(
                    "Mostra movimenti collegati a " + name);
        }
        component.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(final java.awt.event.MouseEvent event) {
                if (SwingUtilities.isLeftMouseButton(event)) {
                    showTransactionsForClassification(type, id, name);
                }
            }
        });

        if (component instanceof Container) {
            for (Component child : ((Container) component).getComponents()) {
                installClassificationClick(child, type, id, name);
            }
        }
    }

    private void showTransactionsForClassification(final String type, final long id,
            final String name) {
        try {
            final List<Transazione> transazioni =
                    dashboardDataService.loadTransactionsForClassification(
                            currentUser.getEmail(), type, id);
            showClassificationTransactionsDialog(type, name, transazioni);
        } catch (final SQLException ex) {
            showLoadError("movimenti collegati a " + name, ex);
        }
    }

    private void showClassificationTransactionsDialog(final String type, final String name,
            final List<Transazione> transazioni) {
        final JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this),
                "Movimenti collegati", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        final JPanel root = new JPanel(new BorderLayout(0, 14));
        root.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        root.setBackground(Color.WHITE);

        final JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        final JLabel titleLabel = new JLabel(type + ": " + name);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        titleLabel.setForeground(AppTheme.TEXT);

        final JLabel subtitleLabel = new JLabel(buildClassificationDialogSubtitle(type, transazioni));
        subtitleLabel.setForeground(AppTheme.TEXT_MUTED);

        header.add(titleLabel);
        header.add(Box.createVerticalStrut(6));
        header.add(subtitleLabel);

        final JTable table = createLinkedTransactionsTable(transazioni);
        final JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(218, 226, 236)));

        final JPanel footer = new JPanel(new BorderLayout(12, 0));
        footer.setOpaque(false);
        final JLabel summaryLabel = new JLabel(buildTransactionsSummary(transazioni));
        summaryLabel.setForeground(AppTheme.TEXT_MUTED);
        final SoftButton closeButton = createDialogButton("Chiudi", AppTheme.ACCENT);
        closeButton.addActionListener(e -> dialog.dispose());
        footer.add(summaryLabel, BorderLayout.CENTER);
        footer.add(closeButton, BorderLayout.EAST);

        root.add(header, BorderLayout.NORTH);
        root.add(scrollPane, BorderLayout.CENTER);
        root.add(footer, BorderLayout.SOUTH);

        dialog.setContentPane(root);
        dialog.pack();
        dialog.setMinimumSize(new Dimension(760, 420));
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private JTable createLinkedTransactionsTable(final List<Transazione> transazioni) {
        final String[] columns = {"ID", "Data", "Tipo", "Descrizione", "Importo"};
        final DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(final int row, final int column) {
                return false;
            }
        };

        for (Transazione transazione : transazioni) {
            model.addRow(new Object[] {
                transazione.getId(),
                transazione.getData(),
                labelTipo(transazione.getTipo()),
                transazione.getDescrizione(),
                formatEuro(transazione.getImporto())
            });
        }

        final JTable table = new JTable(model);
        table.setFillsViewportHeight(true);
        table.setRowHeight(28);
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        return table;
    }

    private String buildClassificationDialogSubtitle(final String type,
            final List<Transazione> transazioni) {
        if (transazioni.isEmpty()) {
            return "Nessun movimento collegato a questa " + type.toLowerCase() + ".";
        }
        return transazioni.size() + " movimenti collegati alla " + type.toLowerCase()
                + " selezionata.";
    }

    private String buildTransactionsSummary(final List<Transazione> transazioni) {
        final BigDecimal entrate = overviewCalculator.sumByType(
                transazioni, TipoTransazione.ENTRATA);
        final BigDecimal spese = overviewCalculator.sumByType(
                transazioni, TipoTransazione.SPESA);
        return "Entrate: " + formatEuro(entrate) + "    Spese: " + formatEuro(spese);
    }

    private JComponent createClassificationIcon(final String type, final String name,
            final String iconName) {
        return ClassificationIconSupport.createClassificationIcon(type, name, iconName);
    }

    private String normalizeClassificationName(final String name) {
        if (name == null) {
            return "";
        }
        return name.trim().toLowerCase()
                .replace("à", "a")
                .replace("è", "e")
                .replace("é", "e")
                .replace("ì", "i")
                .replace("ò", "o")
                .replace("ù", "u");
    }

    private SoftButton createTinyActionButton(final String text) {
        SoftButton button = new SoftButton(text);
        button.setBackground(AppTheme.SURFACE_MUTED);
        button.setForeground(AppTheme.TEXT);
        button.setFont(new Font("SansSerif", Font.BOLD, 11));
        button.setBorder(BorderFactory.createEmptyBorder(7, 10, 7, 10));
        button.setArc(12);
        button.addMouseListener(new ButtonHoverAdapter(button, AppTheme.SURFACE_MUTED, AppTheme.BADGE_BACKGROUND));
        return button;
    }

    private JPanel createOverviewFilterPanel() {
        JPanel panel = new JPanel(new BorderLayout(14, 0));
        panel.setOpaque(false);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel labels = new JPanel();
        labels.setOpaque(false);
        labels.setLayout(new BoxLayout(labels, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Periodo analizzato");
        title.setForeground(AppTheme.TEXT);
        title.setFont(new Font("SansSerif", Font.BOLD, 14));

        JLabel description = new JLabel(overviewCalculator.periodDescription(overviewFilter));
        description.setForeground(AppTheme.TEXT_MUTED);
        description.setFont(new Font("SansSerif", Font.PLAIN, 12));

        labels.add(title);
        labels.add(Box.createVerticalStrut(3));
        labels.add(description);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.setOpaque(false);
        buttons.add(createOverviewFilterButton("Mese", OverviewFilter.MONTH));
        buttons.add(createOverviewFilterButton("Anno", OverviewFilter.YEAR));
        buttons.add(createOverviewFilterButton("Tutto", OverviewFilter.ALL));

        panel.add(labels, BorderLayout.WEST);
        panel.add(buttons, BorderLayout.EAST);
        return panel;
    }

    private SoftButton createOverviewFilterButton(final String text, final OverviewFilter filter) {
        final boolean selected = overviewFilter == filter;
        SoftButton button = new SoftButton(text);
        button.setFont(new Font("SansSerif", Font.BOLD, 12));
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        button.setArc(14);
        button.setBackground(selected ? AppTheme.ACCENT : AppTheme.SURFACE_MUTED);
        button.setForeground(selected ? Color.WHITE : AppTheme.TEXT);
        button.addMouseListener(new ButtonHoverAdapter(
                button,
                selected ? AppTheme.ACCENT : AppTheme.SURFACE_MUTED,
                selected ? AppTheme.ACCENT_HOVER : AppTheme.BADGE_BACKGROUND));
        button.addActionListener(e -> {
            if (overviewFilter != filter) {
                overviewFilter = filter;
                refreshContent("CARD_OVERVIEW");
            }
        });
        return button;
    }

    private JPanel createAnalyticsCard(final String title, final String subtitle, final JComponent content) {
        JPanel panel = new GlassPanel(new BorderLayout(0, 14));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        titleLabel.setForeground(AppTheme.TEXT);

        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setForeground(AppTheme.TEXT_MUTED);
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));

        header.add(titleLabel);
        header.add(Box.createVerticalStrut(6));
        header.add(subtitleLabel);

        panel.add(header, BorderLayout.NORTH);
        panel.add(content, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createMonthlyTrendCard(final List<Transazione> transazioni) {
        if (overviewFilter == OverviewFilter.MONTH) {
            final YearMonth currentMonth = YearMonth.now();
            final List<DayExpense> dailyExpenses =
                    overviewCalculator.buildDailyExpenses(transazioni, currentMonth);
            final JPanel chartPanel = DashboardCharts.dailyExpenses(dailyExpenses);
            chartPanel.setPreferredSize(new Dimension(340, 240));

            final String monthName = overviewCalculator.monthName(currentMonth);
            return createAnalyticsCard(
                    "Andamento mensile",
                    "Entrate e spese giorno per giorno di " + monthName + " " + currentMonth.getYear() + ".",
                    chartPanel);
        }

        final List<MonthTotals> monthTotals = overviewFilter == OverviewFilter.YEAR
                ? overviewCalculator.buildYearMonthlyTotals(transazioni, LocalDate.now().getYear())
                : overviewCalculator.buildMonthlyTotals(transazioni, 6);
        final JPanel chartPanel = DashboardCharts.monthlyTrend(monthTotals);
        chartPanel.setPreferredSize(new Dimension(340, 240));
        return createAnalyticsCard(
                "Andamento mensile",
                overviewFilter == OverviewFilter.YEAR
                        ? "Confronto tra entrate e spese mese per mese nell'anno corrente."
                        : "Confronto tra entrate e spese negli ultimi 6 mesi disponibili.",
                chartPanel);
    }

    private JPanel createExpenseDistributionCard(final List<Transazione> transazioni,
            final Map<Long, String> categoryNames) {
        final LinkedHashMap<String, BigDecimal> expensesByCategory =
                overviewCalculator.buildExpenseDistribution(transazioni, categoryNames);
        final JPanel content = new JPanel(new BorderLayout(12, 0));
        content.setOpaque(false);

        final JPanel chartPanel = DashboardCharts.expenseDistribution(expensesByCategory);
        chartPanel.setPreferredSize(new Dimension(220, 220));
        content.add(chartPanel, BorderLayout.CENTER);
        content.add(createLegendPanel(expensesByCategory), BorderLayout.EAST);

        return createAnalyticsCard(
                "Spese per categoria",
                "Distribuzione delle uscite per categoria nel periodo caricato.",
                content);
    }

    private JPanel createBudgetProgressCard(final List<Budget> budgets, final Map<Long, String> categoryNames) {
        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        if (budgets.isEmpty()) {
            JLabel emptyLabel = new JLabel("Nessun budget configurato.");
            emptyLabel.setForeground(AppTheme.TEXT_MUTED);
            emptyLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            content.add(emptyLabel);
        } else {
            int limit = Math.min(5, budgets.size());
            for (int i = 0; i < limit; i++) {
                Budget budget = budgets.get(i);
                content.add(createBudgetProgressRow(budget, categoryNames, false));
                if (i < limit - 1) {
                    content.add(Box.createVerticalStrut(12));
                }
            }
        }

        return createAnalyticsCard(
                "Stato budget",
                "Avanzamento dei budget mensili e per categoria.",
                content);
    }

    private JPanel createBudgetProgressRow(final Budget budget, final Map<Long, String> categoryNames) {
        return createBudgetProgressRow(budget, categoryNames, false);
    }

    private JPanel createBudgetProgressRow(final Budget budget, final Map<Long, String> categoryNames,
            final boolean withActions) {
        JPanel row = new JPanel(new BorderLayout(12, 8));
        row.setOpaque(false);

        JPanel progressArea = new JPanel(new BorderLayout(10, 8));
        progressArea.setOpaque(false);

        String label = budget.getIdCategoria() == null
                ? "Budget mensile"
                : categoryNames.getOrDefault(budget.getIdCategoria(), "Categoria " + budget.getIdCategoria());
        BigDecimal limite = budget.getImportoLimite() == null ? BigDecimal.ZERO : budget.getImportoLimite();
        BigDecimal speso = budget.getTotaleSpesoAttuale() == null ? BigDecimal.ZERO : budget.getTotaleSpesoAttuale();
        int percentage = 0;
        if (limite.signum() > 0) {
            percentage = speso.multiply(BigDecimal.valueOf(100))
                    .divide(limite, 0, java.math.RoundingMode.HALF_UP)
                    .intValue();
        }

        JLabel nameLabel = new JLabel(label);
        nameLabel.setForeground(AppTheme.TEXT);
        nameLabel.setFont(new Font("SansSerif", Font.BOLD, 13));

        JLabel valueLabel = new JLabel(formatEuro(speso) + " / " + formatEuro(limite));
        valueLabel.setForeground(AppTheme.TEXT_MUTED);
        valueLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(nameLabel, BorderLayout.WEST);
        top.add(valueLabel, BorderLayout.EAST);

        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setValue(Math.min(percentage, 100));
        progressBar.setString(percentage + "%");
        progressBar.setStringPainted(true);
        progressBar.setForeground(percentage >= 100 ? AppTheme.EXPENSE : (percentage >= 80 ? AppTheme.BUDGET : AppTheme.ACCENT));
        progressBar.setBackground(AppTheme.SURFACE_MUTED);
        progressBar.setBorder(BorderFactory.createEmptyBorder());

        progressArea.add(top, BorderLayout.NORTH);
        progressArea.add(progressBar, BorderLayout.CENTER);
        row.add(progressArea, BorderLayout.CENTER);

        if (withActions) {
            JPanel actionPanel = new JPanel(new GridLayout(2, 1, 0, 8));
            actionPanel.setOpaque(false);
            SoftButton editButton = createSmallActionButton("Modifica");
            editButton.addActionListener(e -> showEditBudgetDialog(budget));
            SoftButton deleteButton = createSmallActionButton("Elimina");
            deleteButton.addActionListener(e -> deleteBudget(budget));
            actionPanel.add(editButton);
            actionPanel.add(deleteButton);
            row.add(actionPanel, BorderLayout.EAST);
        }

        return row;
    }

    private JPanel createRecentTransactionsListCard(final List<Transazione> transazioni) {
        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        if (transazioni.isEmpty()) {
            JLabel emptyLabel = new JLabel("Nessuna transazione caricata.");
            emptyLabel.setForeground(AppTheme.TEXT_MUTED);
            emptyLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            content.add(emptyLabel);
        } else {
            int limit = Math.min(5, transazioni.size());
            for (int i = 0; i < limit; i++) {
                content.add(createTransactionSummaryRow(transazioni.get(i)));
                if (i < limit - 1) {
                    content.add(Box.createVerticalStrut(10));
                }
            }
        }

        return createAnalyticsCard(
                "Ultime transazioni",
                "Movimenti più recenti registrati nel portafoglio.",
                content);
    }

    private JPanel createTransactionSummaryRow(final Transazione transazione) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 236, 242)),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

        JLabel descriptionLabel = new JLabel(transazione.getDescrizione());
        descriptionLabel.setForeground(AppTheme.TEXT);
        descriptionLabel.setFont(new Font("SansSerif", Font.BOLD, 13));

        JLabel dateLabel = new JLabel(String.valueOf(transazione.getData()));
        dateLabel.setForeground(AppTheme.TEXT_MUTED);
        dateLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));

        left.add(descriptionLabel);
        left.add(Box.createVerticalStrut(4));
        left.add(dateLabel);

        JLabel amountLabel = new JLabel(formatEuro(transazione.getImporto()));
        amountLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        amountLabel.setForeground(transazione.getTipo() == TipoTransazione.ENTRATA
                ? AppTheme.INCOME
                : AppTheme.EXPENSE);

        row.add(left, BorderLayout.CENTER);
        row.add(amountLabel, BorderLayout.EAST);
        return row;
    }

    private JPanel createLegendPanel(final LinkedHashMap<String, BigDecimal> expensesByCategory) {
        JPanel legend = new JPanel();
        legend.setOpaque(false);
        legend.setLayout(new BoxLayout(legend, BoxLayout.Y_AXIS));
        legend.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));

        if (expensesByCategory.isEmpty()) {
            JLabel emptyLabel = new JLabel("Nessuna spesa disponibile");
            emptyLabel.setForeground(AppTheme.TEXT_MUTED);
            legend.add(emptyLabel);
            return legend;
        }

        int index = 0;
        for (Map.Entry<String, BigDecimal> entry : expensesByCategory.entrySet()) {
            JPanel item = new JPanel(new BorderLayout(8, 0));
            item.setOpaque(false);

            JPanel swatch = new JPanel();
            swatch.setOpaque(true);
            swatch.setBackground(DashboardCharts.chartColor(index));
            swatch.setPreferredSize(new Dimension(12, 12));
            swatch.setMinimumSize(new Dimension(12, 12));
            swatch.setMaximumSize(new Dimension(12, 12));

            JLabel label = new JLabel(entry.getKey());
            label.setForeground(AppTheme.TEXT);
            label.setFont(new Font("SansSerif", Font.PLAIN, 12));

            JLabel value = new JLabel(formatEuro(entry.getValue()));
            value.setForeground(AppTheme.TEXT_MUTED);
            value.setFont(new Font("SansSerif", Font.BOLD, 12));

            JPanel labelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            labelPanel.setOpaque(false);
            labelPanel.add(swatch);
            labelPanel.add(label);

            item.add(labelPanel, BorderLayout.WEST);
            item.add(value, BorderLayout.EAST);

            legend.add(item);
            legend.add(Box.createVerticalStrut(8));
            index++;
        }
        return legend;
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
        JTable table = new JTable(model);
        table.setFillsViewportHeight(true);
        table.setRowHeight(28);
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        return createTableCard(title, subtitle, table, null);
    }

    private JPanel createTableCard(final String title, final String subtitle,
            final JTable table, final JComponent actions) {
        JPanel panel = new GlassPanel(new BorderLayout(0, 14));
        panel.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setOpaque(false);
        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        titleLabel.setForeground(AppTheme.TEXT);

        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setForeground(AppTheme.TEXT_MUTED);

        titlePanel.add(titleLabel);
        titlePanel.add(Box.createVerticalStrut(6));
        titlePanel.add(subtitleLabel);
        header.add(titlePanel, BorderLayout.CENTER);
        if (actions != null) {
            header.add(actions, BorderLayout.EAST);
        }

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(218, 226, 236)));

        panel.add(header, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private SoftButton createSmallActionButton(final String text) {
        SoftButton button = new SoftButton(text);
        button.setBackground(AppTheme.ACCENT);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("SansSerif", Font.BOLD, 12));
        button.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        button.setArc(12);
        button.addMouseListener(new ButtonHoverAdapter(button, AppTheme.ACCENT, AppTheme.ACCENT_HOVER));
        return button;
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

    private JPanel createSettingsCard() {
        JPanel panel = new GlassPanel(new BorderLayout(0, 18));
        panel.setBorder(BorderFactory.createEmptyBorder(26, 28, 26, 28));

        JLabel titleLabel = new JLabel("Impostazioni");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 26));
        titleLabel.setForeground(AppTheme.TEXT);

        JPanel fieldsPanel = new JPanel(new GridLayout(4, 1, 0, 12));
        fieldsPanel.setOpaque(false);
        fieldsPanel.add(createSettingsRow("Nome", currentUser.getNome()));
        fieldsPanel.add(createSettingsRow("Cognome", currentUser.getCognome()));
        fieldsPanel.add(createSettingsRow("Email", currentUser.getEmail()));
        fieldsPanel.add(createSettingsRow("Ruolo", currentUser.getRuolo().name()));

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(fieldsPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createSettingsRow(String label, String value) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);

        JLabel labelComponent = new JLabel(label);
        labelComponent.setForeground(AppTheme.TEXT_MUTED);
        labelComponent.setFont(new Font("SansSerif", Font.BOLD, 13));

        JLabel valueComponent = new JLabel(value);
        valueComponent.setForeground(AppTheme.TEXT);
        valueComponent.setFont(new Font("SansSerif", Font.BOLD, 16));

        row.add(labelComponent, BorderLayout.WEST);
        row.add(valueComponent, BorderLayout.EAST);

        return row;
    }

    private void showNewTransactionDialog() {
        final List<Categoria> categories = loadCategories();
        final List<Fonte> sources = loadSources();
        final List<Tag> tags = loadTags();
        if (categories.isEmpty() || sources.isEmpty() || tags.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Servono almeno una categoria, una fonte e un tag disponibili.",
                    "Dati mancanti",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Nuova transazione",
                Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setContentPane(createTransactionDialogContent(dialog, categories, sources, tags));
        dialog.pack();
        dialog.setMinimumSize(new Dimension(520, 520));
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private JPanel createTransactionDialogContent(final JDialog dialog,
            final List<Categoria> categories, final List<Fonte> sources, final List<Tag> tags) {
        JPanel root = new JPanel(new BorderLayout(0, 14));
        root.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        root.setBackground(Color.WHITE);

        JTabbedPane tabs = new JTabbedPane();

        JTextField expenseAmount = createDialogTextField();
        JTextField expenseDate = createDialogTextField(LocalDate.now().toString());
        JTextField expenseDescription = createDialogTextField();
        JComboBox<Categoria> expenseCategory = createCategoryCombo(categories);
        JList<Tag> expenseTags = createTagList(tags);

        JTextField incomeAmount = createDialogTextField();
        JTextField incomeDate = createDialogTextField(LocalDate.now().toString());
        JTextField incomeDescription = createDialogTextField();
        JComboBox<Fonte> incomeSource = createSourceCombo(sources);

        tabs.addTab("Spesa", createExpenseForm(expenseAmount, expenseDate,
                expenseDescription, expenseCategory, expenseTags));
        tabs.addTab("Entrata", createIncomeForm(incomeAmount, incomeDate,
                incomeDescription, incomeSource));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);
        SoftButton cancelButton = createDialogButton("Annulla", AppTheme.SIDEBAR_BUTTON);
        SoftButton saveButton = createDialogButton("Salva", AppTheme.ACCENT);
        cancelButton.addActionListener(e -> dialog.dispose());
        saveButton.addActionListener(e -> {
            if (tabs.getSelectedIndex() == 0) {
                saveExpense(dialog, expenseAmount, expenseDate, expenseDescription,
                        expenseCategory, expenseTags);
            } else {
                saveIncome(dialog, incomeAmount, incomeDate, incomeDescription, incomeSource);
            }
        });
        actions.add(cancelButton);
        actions.add(saveButton);

        root.add(tabs, BorderLayout.CENTER);
        root.add(actions, BorderLayout.SOUTH);
        return root;
    }

    private JPanel createExpenseForm(final JTextField amountField, final JTextField dateField,
            final JTextField descriptionField, final JComboBox<Categoria> categoryField,
            final JList<Tag> tagList) {
        JPanel panel = createDialogFormPanel();
        addFormRow(panel, "Importo", amountField);
        addFormRow(panel, "Data", dateField);
        addFormRow(panel, "Descrizione", descriptionField);
        addFormRow(panel, "Categoria", categoryField);
        JScrollPane tagScroll = new JScrollPane(tagList);
        tagScroll.setPreferredSize(new Dimension(0, 110));
        addFormRow(panel, "Tag", tagScroll);
        return panel;
    }

    private JPanel createIncomeForm(final JTextField amountField, final JTextField dateField,
            final JTextField descriptionField, final JComboBox<Fonte> sourceField) {
        JPanel panel = createDialogFormPanel();
        addFormRow(panel, "Importo", amountField);
        addFormRow(panel, "Data", dateField);
        addFormRow(panel, "Descrizione", descriptionField);
        addFormRow(panel, "Fonte entrata", sourceField);
        return panel;
    }

    private JPanel createDialogFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(18, 10, 10, 10));
        return panel;
    }

    private void addFormRow(final JPanel panel, final String label, final JComponent field) {
        GridBagConstraints labelConstraints = new GridBagConstraints();
        labelConstraints.gridx = 0;
        labelConstraints.gridy = panel.getComponentCount() / 2;
        labelConstraints.anchor = GridBagConstraints.WEST;
        labelConstraints.insets = new Insets(0, 0, 12, 14);

        JLabel labelComponent = new JLabel(label);
        labelComponent.setFont(new Font("SansSerif", Font.BOLD, 13));
        labelComponent.setForeground(AppTheme.TEXT);
        panel.add(labelComponent, labelConstraints);

        GridBagConstraints fieldConstraints = new GridBagConstraints();
        fieldConstraints.gridx = 1;
        fieldConstraints.gridy = labelConstraints.gridy;
        fieldConstraints.weightx = 1;
        fieldConstraints.fill = GridBagConstraints.HORIZONTAL;
        fieldConstraints.insets = new Insets(0, 0, 12, 0);
        panel.add(field, fieldConstraints);
    }

    private JTextField createDialogTextField() {
        return createDialogTextField("");
    }

    private JTextField createDialogTextField(final String value) {
        JTextField field = new JTextField(value);
        field.setPreferredSize(new Dimension(260, 36));
        field.setFont(new Font("SansSerif", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.BORDER),
                BorderFactory.createEmptyBorder(7, 10, 7, 10)));
        return field;
    }

    private JComboBox<Categoria> createCategoryCombo(final List<Categoria> categories) {
        JComboBox<Categoria> combo = new JComboBox<>(categories.toArray(new Categoria[0]));
        combo.setRenderer((list, value, index, selected, focus) ->
                new JLabel(value == null ? "" : value.getNome()));
        combo.setPreferredSize(new Dimension(260, 36));
        return combo;
    }

    private JComboBox<Fonte> createSourceCombo(final List<Fonte> sources) {
        JComboBox<Fonte> combo = new JComboBox<>(sources.toArray(new Fonte[0]));
        combo.setRenderer((list, value, index, selected, focus) ->
                new JLabel(value == null ? "" : value.getNome()));
        combo.setPreferredSize(new Dimension(260, 36));
        return combo;
    }

    private JList<Tag> createTagList(final List<Tag> tags) {
        JList<Tag> list = new JList<>(tags.toArray(new Tag[0]));
        list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        list.setVisibleRowCount(5);
        list.setSelectedIndex(0);
        list.setCellRenderer((tagList, value, index, selected, focus) -> {
            JLabel label = new JLabel(value == null ? "" : value.getNome());
            label.setOpaque(true);
            label.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
            label.setBackground(selected ? AppTheme.ACCENT : Color.WHITE);
            label.setForeground(selected ? Color.WHITE : AppTheme.TEXT);
            return label;
        });
        return list;
    }

    private SoftButton createDialogButton(final String text, final Color color) {
        SoftButton button = new SoftButton(text);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("SansSerif", Font.BOLD, 13));
        button.setBorder(BorderFactory.createEmptyBorder(9, 16, 9, 16));
        button.setArc(12);
        return button;
    }

    private void saveExpense(final JDialog dialog, final JTextField amountField,
            final JTextField dateField, final JTextField descriptionField,
            final JComboBox<Categoria> categoryField, final JList<Tag> tagList) {
        try {
            final BigDecimal amount = parseAmount(amountField);
            final LocalDate date = LocalDate.parse(dateField.getText().trim());
            final String description = requireDescription(descriptionField);
            final Categoria category = (Categoria) categoryField.getSelectedItem();
            final List<Tag> selectedTags = tagList.getSelectedValuesList();
            if (selectedTags.isEmpty()) {
                throw new IllegalArgumentException("Seleziona almeno un tag.");
            }
            movimentiController.registraSpesa(currentUser.getEmail(), amount, date,
                    description, category.getId(), selectedTags.stream().map(Tag::getId).toList(), "");
            dialog.dispose();
            refreshContent("CARD_OVERVIEW");
        } catch (final Exception ex) {
            showTransactionError(ex);
        }
    }

    private void saveIncome(final JDialog dialog, final JTextField amountField,
            final JTextField dateField, final JTextField descriptionField,
            final JComboBox<Fonte> sourceField) {
        try {
            final BigDecimal amount = parseAmount(amountField);
            final LocalDate date = LocalDate.parse(dateField.getText().trim());
            final String description = requireDescription(descriptionField);
            final Fonte source = (Fonte) sourceField.getSelectedItem();
            movimentiController.registraEntrata(currentUser.getEmail(), amount, date,
                    description, source.getId());
            dialog.dispose();
            refreshContent("CARD_OVERVIEW");
        } catch (final Exception ex) {
            showTransactionError(ex);
        }
    }

    private BigDecimal parseAmount(final JTextField amountField) {
        final BigDecimal amount = new BigDecimal(amountField.getText().trim().replace(",", "."));
        if (amount.signum() <= 0) {
            throw new IllegalArgumentException("L'importo deve essere positivo.");
        }
        return amount;
    }

    private String requireDescription(final JTextField descriptionField) {
        final String description = descriptionField.getText().trim();
        if (description.isEmpty()) {
            throw new IllegalArgumentException("La descrizione e' obbligatoria.");
        }
        return description;
    }

    private void showTransactionError(final Exception ex) {
        JOptionPane.showMessageDialog(this,
                "Transazione non salvata: " + ex.getMessage(),
                "Errore",
                JOptionPane.ERROR_MESSAGE);
    }

    private void showAddBudgetDialog() {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this),
                "Nuovo budget", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setContentPane(createBudgetDialogContent(dialog, null));
        dialog.pack();
        dialog.setMinimumSize(new Dimension(520, 290));
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void showEditBudgetDialog(final Budget budget) {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this),
                "Modifica budget", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setContentPane(createBudgetDialogContent(dialog, budget));
        dialog.pack();
        dialog.setMinimumSize(new Dimension(520, 290));
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private JPanel createBudgetDialogContent(final JDialog dialog, final Budget budget) {
        final boolean editing = budget != null;
        JPanel root = new JPanel(new BorderLayout(0, 14));
        root.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        root.setBackground(Color.WHITE);

        JTextField limit = createDialogTextField(editing
                ? budget.getImportoLimite().toPlainString()
                : "");
        JCheckBox alert = new JCheckBox("Avvisa quando il budget viene superato",
                !editing || budget.isAlertSoglia());
        alert.setOpaque(false);
        alert.setForeground(AppTheme.TEXT);
        JComboBox<Object> category = createBudgetCategoryCombo();
        if (editing) {
            selectBudgetCategory(category, budget.getIdCategoria());
        }

        JPanel form = createDialogFormPanel();
        addFormRow(form, "Categoria", category);
        addFormRow(form, "Limite", limit);
        addFormRow(form, "Alert", alert);

        JLabel note = new JLabel("Ogni budget e' unico: il limite viene riusato e ricalcolato mese per mese.");
        note.setForeground(AppTheme.TEXT_MUTED);
        note.setFont(new Font("SansSerif", Font.PLAIN, 12));
        form.add(note);

        SoftButton saveButton = createDialogButton(editing ? "Salva modifiche" : "Salva", AppTheme.ACCENT);
        saveButton.addActionListener(e -> {
            if (editing) {
                updateBudget(dialog, budget.getId(), category, limit, alert);
            } else {
                saveBudget(dialog, category, limit, alert);
            }
        });

        root.add(form, BorderLayout.CENTER);
        root.add(createDialogFooter(dialog, saveButton), BorderLayout.SOUTH);
        return root;
    }

    private void selectBudgetCategory(final JComboBox<Object> categoryField, final Long idCategoria) {
        if (idCategoria == null) {
            categoryField.setSelectedIndex(0);
            return;
        }
        for (int i = 0; i < categoryField.getItemCount(); i++) {
            final Object item = categoryField.getItemAt(i);
            if (item instanceof Categoria
                    && Long.valueOf(((Categoria) item).getId()).equals(idCategoria)) {
                categoryField.setSelectedIndex(i);
                return;
            }
        }
    }

    private JComboBox<Object> createBudgetCategoryCombo() {
        final java.util.List<Object> values = new java.util.ArrayList<>();
        values.add("Budget mensile totale");
        values.addAll(loadCategories());
        JComboBox<Object> combo = new JComboBox<>(values.toArray());
        combo.setRenderer((list, value, index, selected, focus) -> {
            final JLabel label = new JLabel(value instanceof Categoria
                    ? ((Categoria) value).getNome()
                    : String.valueOf(value));
            label.setOpaque(true);
            label.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
            label.setBackground(selected ? AppTheme.ACCENT : Color.WHITE);
            label.setForeground(selected ? Color.WHITE : AppTheme.TEXT);
            return label;
        });
        combo.setPreferredSize(new Dimension(260, 36));
        return combo;
    }

    private void saveBudget(final JDialog dialog, final JComboBox<Object> categoryField,
            final JTextField limitField, final JCheckBox alertField) {
        try {
            final BudgetFormData formData = readBudgetForm(categoryField, limitField, alertField);
            final LocalDate today = LocalDate.now();
            budgetController.salvaBudget(currentUser.getEmail(), today.getMonthValue(), today.getYear(),
                    formData.idCategoria, formData.limit, formData.alert);
            dialog.dispose();
            refreshContent("CARD_BUDGET");
        } catch (final Exception ex) {
            showBudgetError(ex);
        }
    }

    private void updateBudget(final JDialog dialog, final long idBudget,
            final JComboBox<Object> categoryField, final JTextField limitField,
            final JCheckBox alertField) {
        try {
            final BudgetFormData formData = readBudgetForm(categoryField, limitField, alertField);
            final LocalDate today = LocalDate.now();
            budgetController.aggiornaBudget(currentUser.getEmail(), idBudget, today.getMonthValue(), today.getYear(),
                    formData.idCategoria, formData.limit, formData.alert);
            dialog.dispose();
            refreshContent("CARD_BUDGET");
        } catch (final Exception ex) {
            showBudgetError(ex);
        }
    }

    private BudgetFormData readBudgetForm(final JComboBox<Object> categoryField,
            final JTextField limitField, final JCheckBox alertField) {
        final BigDecimal limit = parseAmount(limitField);
        final Object selected = categoryField.getSelectedItem();
        final Long idCategoria = selected instanceof Categoria
                ? ((Categoria) selected).getId()
                : null;
        return new BudgetFormData(idCategoria, limit, alertField.isSelected());
    }

    private void deleteBudget(final Budget budget) {
        final int confirm = JOptionPane.showConfirmDialog(this,
                "Eliminare definitivamente il budget selezionato?",
                "Conferma eliminazione budget",
                JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            budgetController.eliminaBudget(currentUser.getEmail(), budget.getId());
            refreshContent("CARD_BUDGET");
        } catch (final SQLException ex) {
            showBudgetError(ex);
        }
    }

    private static final class BudgetFormData {
        private final Long idCategoria;
        private final BigDecimal limit;
        private final boolean alert;

        private BudgetFormData(final Long idCategoria, final BigDecimal limit, final boolean alert) {
            this.idCategoria = idCategoria;
            this.limit = limit;
            this.alert = alert;
        }
    }

    private void showBudgetError(final Exception ex) {
        JOptionPane.showMessageDialog(this,
                "Budget non salvato: " + ex.getMessage(),
                "Errore",
                JOptionPane.ERROR_MESSAGE);
    }

    private void showAddRecurringDialog() {
        final List<Categoria> categories = loadCategories();
        if (categories.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Serve almeno una categoria disponibile.",
                    "Dati mancanti",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this),
                "Nuova spesa ricorrente", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setContentPane(createRecurringDialogContent(dialog, categories));
        dialog.pack();
        dialog.setMinimumSize(new Dimension(520, 360));
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private JPanel createRecurringDialogContent(final JDialog dialog,
            final List<Categoria> categories) {
        JPanel root = new JPanel(new BorderLayout(0, 14));
        root.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        root.setBackground(Color.WHITE);

        JTextField name = createDialogTextField();
        JTextField amount = createDialogTextField();
        JTextField frequency = createDialogTextField("30");
        JTextField start = createDialogTextField(LocalDate.now().toString());
        JTextField end = createDialogTextField();
        JComboBox<Categoria> category = createCategoryCombo(categories);

        JPanel form = createDialogFormPanel();
        addFormRow(form, "Nome", name);
        addFormRow(form, "Importo", amount);
        addFormRow(form, "Frequenza giorni (30 = mensile)", frequency);
        addFormRow(form, "Data inizio / prima spesa", start);
        addFormRow(form, "Fine opzionale", end);
        addFormRow(form, "Categoria", category);

        JLabel historyNote = new JLabel(
                "Le scadenze gia' maturate dalla data iniziale vengono aggiunte automaticamente.");
        historyNote.setForeground(AppTheme.TEXT_MUTED);
        historyNote.setFont(new Font("SansSerif", Font.PLAIN, 12));
        form.add(historyNote);

        SoftButton saveButton = createDialogButton("Salva", AppTheme.ACCENT);
        saveButton.addActionListener(e -> saveRecurringExpense(
                dialog, name, amount, frequency, start, end, category));

        root.add(form, BorderLayout.CENTER);
        root.add(createDialogFooter(dialog, saveButton), BorderLayout.SOUTH);
        return root;
    }

    private void saveRecurringExpense(final JDialog dialog, final JTextField nameField,
            final JTextField amountField, final JTextField frequencyField, final JTextField startField,
            final JTextField endField, final JComboBox<Categoria> categoryField) {
        try {
            final String name = requireDescription(nameField);
            final BigDecimal amount = parseAmount(amountField);
            final int frequency = Integer.parseInt(frequencyField.getText().trim());
            if (frequency <= 0) {
                throw new IllegalArgumentException("La frequenza deve essere positiva.");
            }
            final LocalDate start = LocalDate.parse(startField.getText().trim());
            final LocalDate end = parseOptionalDate(endField);
            final Categoria category = (Categoria) categoryField.getSelectedItem();

            speseRicorrentiController.aggiungiRicorrenzaERegistraPrimaSpesa(
                    currentUser.getEmail(), name, amount, frequency, start, start, end,
                    category.getId(), LocalDate.now());
            dialog.dispose();
            JOptionPane.showMessageDialog(this,
                    "Ricorrenza salvata. Tutte le scadenze maturate sono state registrate nelle transazioni.",
                    "Ricorrenza creata",
                    JOptionPane.INFORMATION_MESSAGE);
            refreshContent("CARD_RECURRING");
        } catch (final Exception ex) {
            showRecurringError(ex);
        }
    }

    private void showEditRecurringDialog(final SpesaRicorrente ricorrenza) {
        final List<Categoria> categories = loadCategories();
        if (categories.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Serve almeno una categoria disponibile.",
                    "Dati mancanti",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this),
                "Modifica spesa ricorrente", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout(0, 14));
        root.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        root.setBackground(Color.WHITE);

        JTextField name = createDialogTextField(ricorrenza.getNome());
        JTextField amount = createDialogTextField(ricorrenza.getImportoPrevisto().toString());
        JTextField frequency = createDialogTextField(String.valueOf(ricorrenza.getFrequenzaGiorni()));
        JTextField start = createDialogTextField(ricorrenza.getDataInizio().toString());
        JTextField next = createDialogTextField(ricorrenza.getDataProssimaScadenza().toString());
        JTextField end = createDialogTextField(
                ricorrenza.getScadenza() == null ? "" : ricorrenza.getScadenza().toString());
        JComboBox<Categoria> category = createCategoryCombo(categories);
        selectCategory(category, ricorrenza.getIdCategoria());

        JPanel form = createDialogFormPanel();
        addFormRow(form, "Nome", name);
        addFormRow(form, "Importo", amount);
        addFormRow(form, "Frequenza giorni (30 = mensile)", frequency);
        addFormRow(form, "Data inizio", start);
        addFormRow(form, "Prossima scadenza", next);
        addFormRow(form, "Fine opzionale", end);
        addFormRow(form, "Categoria", category);

        JLabel note = new JLabel(
                "Nota: le transazioni gia' generate restano nello storico. Le modifiche valgono per le prossime scadenze.");
        note.setForeground(AppTheme.TEXT_MUTED);
        note.setFont(new Font("SansSerif", Font.PLAIN, 12));
        form.add(note);

        SoftButton saveButton = createDialogButton("Salva", AppTheme.ACCENT);
        saveButton.addActionListener(e -> updateRecurringExpense(
                dialog, ricorrenza, name, amount, frequency, start, next, end, category));

        root.add(form, BorderLayout.CENTER);
        root.add(createDialogFooter(dialog, saveButton), BorderLayout.SOUTH);
        dialog.setContentPane(root);
        dialog.pack();
        dialog.setMinimumSize(new Dimension(560, 420));
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void selectCategory(final JComboBox<Categoria> categoryCombo,
            final long idCategoria) {
        for (int i = 0; i < categoryCombo.getItemCount(); i++) {
            final Categoria categoria = categoryCombo.getItemAt(i);
            if (categoria.getId() == idCategoria) {
                categoryCombo.setSelectedIndex(i);
                return;
            }
        }
    }

    private void updateRecurringExpense(final JDialog dialog, final SpesaRicorrente ricorrenza,
            final JTextField nameField, final JTextField amountField,
            final JTextField frequencyField, final JTextField startField,
            final JTextField nextField, final JTextField endField,
            final JComboBox<Categoria> categoryField) {
        try {
            final String name = requireDescription(nameField);
            final BigDecimal amount = parseAmount(amountField);
            final int frequency = Integer.parseInt(frequencyField.getText().trim());
            if (frequency <= 0) {
                throw new IllegalArgumentException("La frequenza deve essere positiva.");
            }
            final LocalDate start = LocalDate.parse(startField.getText().trim());
            final LocalDate next = LocalDate.parse(nextField.getText().trim());
            final LocalDate end = parseOptionalDate(endField);
            final Categoria category = (Categoria) categoryField.getSelectedItem();

            speseRicorrentiController.modificaRicorrenza(
                    currentUser.getEmail(), ricorrenza.getId(), name, amount,
                    frequency, start, next, end, category.getId());
            dialog.dispose();
            refreshContent("CARD_RECURRING");
        } catch (final Exception ex) {
            showRecurringError(ex);
        }
    }

    private void generateDueRecurringExpenses() {
        try {
            final int generated = speseRicorrentiController.generaSpeseScadute(
                    currentUser.getEmail(), LocalDate.now());
            JOptionPane.showMessageDialog(this,
                    "Spese ricorrenti generate: " + generated,
                    "Generazione completata",
                    JOptionPane.INFORMATION_MESSAGE);
            refreshContent("CARD_RECURRING");
        } catch (final Exception ex) {
            showRecurringError(ex);
        }
    }

    private void deleteSelectedRecurringExpense(final JTable table) {
        final int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Seleziona una spesa ricorrente da eliminare.");
            return;
        }

        final int confirm = JOptionPane.showConfirmDialog(this,
                "Eliminare la spesa ricorrente selezionata?",
                "Conferma eliminazione",
                JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            final long id = Long.parseLong(String.valueOf(table.getValueAt(selectedRow, 0)));
            speseRicorrentiController.eliminaRicorrenza(currentUser.getEmail(), id);
            refreshContent("CARD_RECURRING");
        } catch (final Exception ex) {
            showRecurringError(ex);
        }
    }

    private LocalDate parseOptionalDate(final JTextField dateField) {
        final String value = dateField.getText().trim();
        return value.isEmpty() ? null : LocalDate.parse(value);
    }

    private void showRecurringError(final Exception ex) {
        JOptionPane.showMessageDialog(this,
                "Operazione sulle ricorrenze non completata: " + ex.getMessage(),
                "Errore",
                JOptionPane.ERROR_MESSAGE);
    }

    private void addPersonalCategory() {
        showClassificationEditor("Categoria", 0, "", "generic_category.png");
    }

    private void editSelectedTransaction(final JTable table) {
        final int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Seleziona una transazione da modificare.");
            return;
        }

        final long id = Long.parseLong(String.valueOf(table.getValueAt(selectedRow, 0)));
        Transazione target = null;
        // Cerco l'oggetto transazione corrispondente all'ID tra quelle caricate
        for (Transazione t : loadTransactions()) {
            if (t.getId() == id) {
                target = t;
                break;
            }
        }

        if (target != null) {
            showEditDialog(target);
        }
    }

    private void showEditDialog(final Transazione t) {
        final List<Categoria> categories = loadCategories();
        final List<Fonte> sources = loadSources();
        final List<Tag> tags = loadTags();
        final List<Long> selectedTagIds = loadTransactionTagIds(t.getId());

        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Modifica transazione",
                Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout(0, 14));
        root.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        root.setBackground(Color.WHITE);

        JTextField amount = createDialogTextField(t.getImporto().toString());
        JTextField date = createDialogTextField(t.getData().toString());
        JTextField desc = createDialogTextField(t.getDescrizione());

        JPanel form;
        if (t.getTipo() == TipoTransazione.SPESA) {
            JComboBox<Categoria> catCombo = createCategoryCombo(categories);
            for (int i = 0; i < catCombo.getItemCount(); i++) {
                if (Long.valueOf(catCombo.getItemAt(i).getId()).equals(t.getIdCategoria())) {
                    catCombo.setSelectedIndex(i);
                    break;
                }
            }
            JList<Tag> tagList = createTagList(tags);
            selectTags(tagList, selectedTagIds);
            form = createExpenseForm(amount, date, desc, catCombo, tagList);

            SoftButton save = createDialogButton("Salva modifiche", AppTheme.ACCENT);
            save.addActionListener(e -> {
                try {
                    final List<Tag> selectedTags = tagList.getSelectedValuesList();
                    if (selectedTags.isEmpty()) {
                        throw new IllegalArgumentException("Seleziona almeno un tag.");
                    }
                    movimentiController.aggiornaTransazione(currentUser.getEmail(), t.getId(),
                        parseAmount(amount),
                        LocalDate.parse(date.getText().trim()),
                        requireDescription(desc),
                        ((Categoria) catCombo.getSelectedItem()).getId(),
                        null,
                        selectedTags.stream().map(Tag::getId).toList());
                    dialog.dispose();
                    refreshContent("CARD_TRANSACTIONS");
                } catch (Exception ex) { showTransactionError(ex); }
            });
            root.add(form, BorderLayout.CENTER);
            root.add(createDialogFooter(dialog, save), BorderLayout.SOUTH);
        } else {
            JComboBox<Fonte> srcCombo = createSourceCombo(sources);
            for (int i = 0; i < srcCombo.getItemCount(); i++) {
                if (Long.valueOf(srcCombo.getItemAt(i).getId()).equals(t.getIdFonte())) {
                    srcCombo.setSelectedIndex(i);
                    break;
                }
            }
            form = createIncomeForm(amount, date, desc, srcCombo);

            SoftButton save = createDialogButton("Salva modifiche", AppTheme.ACCENT);
            save.addActionListener(e -> {
                try {
                    movimentiController.aggiornaTransazione(currentUser.getEmail(), t.getId(),
                        parseAmount(amount),
                        LocalDate.parse(date.getText().trim()),
                        requireDescription(desc),
                        null,
                        ((Fonte) srcCombo.getSelectedItem()).getId(),
                        List.of());
                    dialog.dispose();
                    refreshContent("CARD_TRANSACTIONS");
                } catch (Exception ex) { showTransactionError(ex); }
            });
            root.add(form, BorderLayout.CENTER);
            root.add(createDialogFooter(dialog, save), BorderLayout.SOUTH);
        }

        dialog.setContentPane(root);
        dialog.pack();
        dialog.setMinimumSize(new Dimension(520, 520));
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void deleteSelectedTransaction(final JTable table) {
        final int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Seleziona una transazione da eliminare.");
            return;
        }

        final long id = Long.parseLong(String.valueOf(table.getValueAt(selectedRow, 0)));
        final int confirm = JOptionPane.showConfirmDialog(this,
                "Eliminare definitivamente la transazione ID " + id + "?",
                "Conferma eliminazione",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                movimentiController.eliminaTransazione(currentUser.getEmail(), id);
                refreshContent("CARD_TRANSACTIONS");
            } catch (final Exception ex) {
                showLoadError("eliminazione transazione", new SQLException(ex.getMessage()));
            }
        }
    }

    private List<Long> loadTransactionTagIds(final long idTransazione) {
        try {
            return dashboardDataService.loadTransactionTagIds(currentUser.getEmail(), idTransazione);
        } catch (final SQLException ex) {
            showLoadError("tag della transazione", ex);
            return List.of();
        }
    }

    private void selectTags(final JList<Tag> tagList, final List<Long> selectedTagIds) {
        final java.util.List<Integer> selectedIndices = new java.util.ArrayList<>();
        for (int i = 0; i < tagList.getModel().getSize(); i++) {
            if (selectedTagIds.contains(tagList.getModel().getElementAt(i).getId())) {
                selectedIndices.add(i);
            }
        }
        final int[] indices = selectedIndices.stream().mapToInt(Integer::intValue).toArray();
        tagList.setSelectedIndices(indices);
    }

    private JPanel createDialogFooter(JDialog dialog, SoftButton saveButton) {
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);
        SoftButton cancel = createDialogButton("Annulla", AppTheme.SIDEBAR_BUTTON);
        cancel.addActionListener(e -> dialog.dispose());
        actions.add(cancel);
        actions.add(saveButton);
        return actions;
    }

    private void addPersonalTag() {
        showClassificationEditor("Tag", 0, "", "generic_tag.png");
    }

    private void addPersonalSource() {
        showClassificationEditor("Fonte", 0, "", "generic_source.png");
    }

    private void editClassification(final String type, final long id,
            final String oldName, final String oldIcon) {
        showClassificationEditor(type, id, oldName, oldIcon);
    }

    private void showClassificationEditor(final String type, final long id,
            final String initialName, final String initialIcon) {
        final boolean category = "Categoria".equals(type);
        final boolean source = "Fonte".equals(type);
        final List<ClassificationIconSupport.IconChoice> choices =
                ClassificationIconSupport.iconChoices(type);
        final String defaultIcon = ClassificationIconSupport.defaultIcon(type);
        final JTextField nameField = createDialogTextField(initialName);
        final ClassificationIconSupport.IconSelectionPanel iconSelector =
                new ClassificationIconSupport.IconSelectionPanel(
                        this,
                        choices,
                        initialIcon == null || initialIcon.isBlank() ? defaultIcon : initialIcon);

        final JPanel form = createDialogFormPanel();
        addFormRow(form, "Nome", nameField);
        addFormRow(form, "Icona", iconSelector);

        final int result = JOptionPane.showConfirmDialog(this, form,
                id == 0 ? "Nuovo " + type.toLowerCase() : "Modifica " + type.toLowerCase(),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        final String name = nameField.getText() == null ? "" : nameField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Il nome e' obbligatorio.");
            return;
        }
        try {
            final String iconReference = iconSelector.resolveIconReference();
            if (category) {
                if (id == 0) {
                    movimentiController.aggiungiCategoriaPersonale(
                            currentUser.getEmail(), name, iconReference);
                } else {
                    movimentiController.modificaCategoriaPersonale(
                            currentUser.getEmail(), id, name, iconReference);
                }
            } else if (source) {
                if (id == 0) {
                    movimentiController.aggiungiFontePersonale(
                            currentUser.getEmail(), name, iconReference);
                } else {
                    movimentiController.modificaFontePersonale(
                            currentUser.getEmail(), id, name, iconReference);
                }
            } else {
                if (id == 0) {
                    movimentiController.aggiungiTagPersonale(
                            currentUser.getEmail(), name, iconReference);
                } else {
                    movimentiController.modificaTagPersonale(
                            currentUser.getEmail(), id, name, iconReference);
                }
            }
            refreshContent(source ? "CARD_SOURCES" : "CARD_CATEGORIES");
        } catch (final SQLException ex) {
            showLoadError(type.toLowerCase(), ex);
        } catch (final IOException | IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this,
                    "Icona non salvata: " + ex.getMessage(),
                    "Errore immagine",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void renameClassification(final String type, final long id, final String oldName) {
        final String newName = JOptionPane.showInputDialog(this, "Nuovo nome:", oldName);
        if (newName == null || newName.trim().isEmpty()) {
            return;
        }
        try {
            if ("Categoria".equals(type)) {
                movimentiController.rinominaCategoriaPersonale(currentUser.getEmail(), id, newName.trim());
                refreshContent("CARD_CATEGORIES");
            } else if ("Fonte".equals(type)) {
                movimentiController.rinominaFontePersonale(currentUser.getEmail(), id, newName.trim());
                refreshContent("CARD_SOURCES");
            } else {
                movimentiController.rinominaTagPersonale(currentUser.getEmail(), id, newName.trim());
                refreshContent("CARD_CATEGORIES");
            }
        } catch (final SQLException ex) {
            showLoadError("classificazione", ex);
        }
    }

    private void deleteClassification(final String type, final long id) {
        final int confirm = JOptionPane.showConfirmDialog(this,
                "Eliminare l'elemento selezionato?",
                "Conferma eliminazione",
                JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            if ("Categoria".equals(type)) {
                movimentiController.eliminaCategoriaPersonale(currentUser.getEmail(), id);
                refreshContent("CARD_CATEGORIES");
            } else if ("Fonte".equals(type)) {
                movimentiController.eliminaFontePersonale(currentUser.getEmail(), id);
                refreshContent("CARD_SOURCES");
            } else {
                movimentiController.eliminaTagPersonale(currentUser.getEmail(), id);
                refreshContent("CARD_CATEGORIES");
            }
        } catch (final SQLException ex) {
            showLoadError("classificazione", ex);
        }
    }

    private void renameSelectedClassification(final JTable table) {
        final int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Seleziona una categoria o un tag.");
            return;
        }
        final String origin = String.valueOf(table.getValueAt(selectedRow, 3));
        final String type = String.valueOf(table.getValueAt(selectedRow, 0));
        final long id = Long.parseLong(String.valueOf(table.getValueAt(selectedRow, 1)));
        final String oldName = String.valueOf(table.getValueAt(selectedRow, 2));
        renameClassification(type, id, oldName);
    }

    private void deleteSelectedClassification(final JTable table) {
        final int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Seleziona una categoria o un tag.");
            return;
        }
        final String origin = String.valueOf(table.getValueAt(selectedRow, 3));
        final String type = String.valueOf(table.getValueAt(selectedRow, 0));
        final long id = Long.parseLong(String.valueOf(table.getValueAt(selectedRow, 1)));
        deleteClassification(type, id);
    }

    private void generateDueRecurringExpensesOnStartup() {
        try {
            dashboardDataService.generateDueRecurringExpenses(currentUser.getEmail(), LocalDate.now());
        } catch (final SQLException ex) {
            System.err.println("Generazione automatica delle ricorrenze non completata: "
                    + ex.getMessage());
        }
    }

    private List<Transazione> loadTransactions() {
        return dashboardData.getTransazioni();
    }

    private List<Budget> loadBudgets() {
        return dashboardData.getBudget();
    }

    private List<SpesaRicorrente> loadRecurringExpenses() {
        return dashboardData.getRicorrenze();
    }

    private List<Categoria> loadCategories() {
        return dashboardData.getCategorie();
    }

    private Map<Long, String> loadCategoryNames() {
        return dashboardData.getNomiCategorie();
    }

    private List<Tag> loadTags() {
        return dashboardData.getTag();
    }

    private List<Fonte> loadSources() {
        return dashboardData.getFonti();
    }

    private String formatEuro(final BigDecimal value) {
        return String.format("%.2f euro", value);
    }

    private String labelTipo(final TipoTransazione tipo) {
        return tipo == TipoTransazione.SPESA ? "Spesa" : "Entrata";
    }

    private void showLoadError(final String dataType, final SQLException ex) {
        JOptionPane.showMessageDialog(this,
                "Errore durante il caricamento di " + dataType + ": " + ex.getMessage(),
                "Errore database",
                JOptionPane.ERROR_MESSAGE);
    }

}
