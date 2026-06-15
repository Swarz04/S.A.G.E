package it.unibo.sage.view;

import it.unibo.sage.controller.BudgetController;
import it.unibo.sage.controller.MovimentiController;
import it.unibo.sage.controller.SpeseRicorrentiController;
import it.unibo.sage.model.Budget;
import it.unibo.sage.model.Categoria;
import it.unibo.sage.model.Fonte;
import it.unibo.sage.model.SpesaRicorrente;
import it.unibo.sage.model.Tag;
import it.unibo.sage.model.TipoTransazione;
import it.unibo.sage.model.Transazione;
import it.unibo.sage.model.Utente;
import it.unibo.sage.utils.IconStorage;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.awt.datatransfer.DataFlavor;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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

    private static final List<IconChoice> CATEGORY_ICON_CHOICES = List.of(
            new IconChoice("Generica", "generic_category.png"),
            new IconChoice("Casa", "house.png"),
            new IconChoice("Cibo", "food.png"),
            new IconChoice("Trasporti", "transport.png"),
            new IconChoice("Salute", "health.png"),
            new IconChoice("Studio", "study.png"),
            new IconChoice("Lavoro", "work.png"),
            new IconChoice("Risparmi", "savings.png"),
            new IconChoice("Shopping", "shopping.png"),
            new IconChoice("Svago", "leisure.png"),
            new IconChoice("Bollette", "bill.png"),
            new IconChoice("Palestra", "gym.png"),
            new IconChoice("Viaggi", "travel.png"),
            new IconChoice("Regalo", "gift.png"));

    private static final List<IconChoice> TAG_ICON_CHOICES = List.of(
            new IconChoice("Generica", "generic_tag.png"),
            new IconChoice("Urgente", "urgent.png"),
            new IconChoice("Studio", "study.png"),
            new IconChoice("Palestra", "gym.png"),
            new IconChoice("Lavoro", "work.png"),
            new IconChoice("Famiglia", "family.png"),
            new IconChoice("Viaggi", "travel.png"),
            new IconChoice("Regalo", "gift.png"),
            new IconChoice("Risparmi", "savings.png"),
            new IconChoice("Shopping", "shopping.png"),
            new IconChoice("Svago", "leisure.png"));

    private static final List<IconChoice> SOURCE_ICON_CHOICES = List.of(
            new IconChoice("Generica", "generic_source.png"),
            new IconChoice("Stipendio", "salary.png"),
            new IconChoice("Borsa di studio", "scholarship.png"),
            new IconChoice("Regalo", "gift.png"),
            new IconChoice("Rimborso", "refund.png"),
            new IconChoice("Ripetizioni", "tutoring.png"),
            new IconChoice("Lavoro", "work.png"),
            new IconChoice("Famiglia", "family.png"),
            new IconChoice("Risparmi", "savings.png"));

    private final CardLayout contentLayout;
    private final JPanel contentPanel;
    private final Utente currentUser;
    private final MovimentiController movimentiController = new MovimentiController();
    private final BudgetController budgetController = new BudgetController();
    private final SpeseRicorrentiController speseRicorrentiController = new SpeseRicorrentiController();
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
        contentPanel.removeAll();
        initContentCards();
        contentPanel.revalidate();
        contentPanel.repaint();
        contentLayout.show(contentPanel, cardName);
    }

    private JPanel createOverviewCard() {
        final List<Transazione> allTransactions = loadTransactions();
        final List<Transazione> transazioni = filterOverviewTransactions(allTransactions);
        final List<Budget> budgets = loadBudgets();
        final Map<Long, String> categoryNames = loadCategoryNames();
        final BigDecimal entrate = sumByType(transazioni, TipoTransazione.ENTRATA);
        final BigDecimal spese = sumByType(transazioni, TipoTransazione.SPESA);
        final BigDecimal saldo = entrate.subtract(spese);
        final String budgetUsage = calculateBudgetUsage(budgets);

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

        panel.add(createOverviewFilterPanel());
        panel.add(Box.createVerticalStrut(14));
        panel.add(metricsPanel);
        panel.add(Box.createVerticalStrut(16));
        panel.add(dashboardGrid);

        return panel;
    }

    private JPanel createTransactionsCard() {
        final String[] columns = {"ID", "Data", "Tipo", "Descrizione", "Importo"};
        final DefaultTableModel model = new DefaultTableModel(columns, 0);
        for (Transazione transazione : loadTransactions()) {
            model.addRow(new Object[] {
                transazione.getId(),
                transazione.getData(),
                labelTipo(transazione.getTipo()),
                transazione.getDescrizione(),
                formatEuro(transazione.getImporto())
            });
        }

        JTable table = new JTable(model);
        table.setFillsViewportHeight(true);
        table.setRowHeight(28);
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actions.setOpaque(false);
        SoftButton editButton = createSmallActionButton("Modifica");
        SoftButton deleteButton = createSmallActionButton("Elimina");

        editButton.addActionListener(e -> editSelectedTransaction(table));
        deleteButton.addActionListener(e -> deleteSelectedTransaction(table));

        actions.add(editButton);
        actions.add(deleteButton);

        return createTableCard("Transazioni", "Storico dei movimenti caricati per "
                + currentUser.getEmail(), table, actions);
    }

    private JPanel createBudgetCard() {
        final String[] columns = {"ID", "Periodo", "Categoria", "Limite", "Speso", "Alert"};
        final DefaultTableModel model = new DefaultTableModel(columns, 0);
        final Map<Long, String> categoryNames = loadCategoryNames();
        for (Budget budget : loadBudgets()) {
            model.addRow(new Object[] {
                budget.getId(),
                "Periodo " + budget.getIdPeriodo(),
                budget.getIdCategoria() == null
                        ? "Mensile"
                        : categoryNames.getOrDefault(budget.getIdCategoria(), "Categoria " + budget.getIdCategoria()),
                formatEuro(budget.getImportoLimite()),
                formatEuro(budget.getTotaleSpesoAttuale()),
                budget.isAlertSoglia() ? "Si" : "No"
            });
        }
        return createTableCard("Budget demo", "Budget mensili e per categoria caricati dal database.", model);
    }
    private JPanel createRecurringCard() {
        final String[] columns = {"ID", "Nome", "Categoria", "Importo", "Frequenza", "Inizio", "Prossima", "Fine"};
        final DefaultTableModel model = new DefaultTableModel(columns, 0);
        final Map<Long, String> categoryNames = loadCategoryNames();
        for (SpesaRicorrente ricorrenza : loadRecurringExpenses()) {
            model.addRow(new Object[] {
                ricorrenza.getId(),
                ricorrenza.getNome(),
                categoryNames.getOrDefault(ricorrenza.getIdCategoria(),
                        "Categoria " + ricorrenza.getIdCategoria()),
                formatEuro(ricorrenza.getImportoPrevisto()),
                ricorrenza.getFrequenzaGiorni() + " giorni",
                ricorrenza.getDataInizio(),
                ricorrenza.getDataProssimaScadenza(),
                ricorrenza.getScadenza() == null ? "-" : ricorrenza.getScadenza()
            });
        }

        JTable table = new JTable(model);
        table.setFillsViewportHeight(true);
        table.setRowHeight(28);
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actions.setOpaque(false);
        SoftButton addButton = createSmallActionButton("+ Ricorrenza");
        SoftButton generateButton = createSmallActionButton("Genera scadute");
        SoftButton deleteButton = createSmallActionButton("Elimina");
        addButton.addActionListener(e -> showAddRecurringDialog());
        generateButton.addActionListener(e -> generateDueRecurringExpenses());
        deleteButton.addActionListener(e -> deleteSelectedRecurringExpense(table));
        actions.add(addButton);
        actions.add(generateButton);
        actions.add(deleteButton);

        return createTableCard(
                "Spese Ricorrenti",
                "Modelli che generano automaticamente spese reali alla scadenza.",
                table,
                actions);
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

        if (!system) {
            JPanel buttons = new JPanel(new GridLayout(2, 1, 0, 8));
            buttons.setOpaque(false);
            SoftButton renameButton = createTinyActionButton("Modifica");
            SoftButton deleteButton = createTinyActionButton("Elimina");
            renameButton.addActionListener(e -> editClassification(type, id, name, iconName));
            deleteButton.addActionListener(e -> deleteClassification(type, id));
            buttons.add(renameButton);
            buttons.add(deleteButton);
            tile.add(buttons, BorderLayout.EAST);
        }

        installClassificationClick(tile, type, id, name);
        return tile;
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
            final List<Transazione> transazioni;
            if ("Categoria".equals(type)) {
                transazioni = movimentiController.caricaTransazioniPerCategoria(currentUser.getEmail(), id);
            } else if ("Fonte".equals(type)) {
                transazioni = movimentiController.caricaTransazioniPerFonte(currentUser.getEmail(), id);
            } else {
                transazioni = movimentiController.caricaTransazioniPerTag(currentUser.getEmail(), id);
            }
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
        final BigDecimal entrate = sumByType(transazioni, TipoTransazione.ENTRATA);
        final BigDecimal spese = sumByType(transazioni, TipoTransazione.SPESA);
        return "Entrate: " + formatEuro(entrate) + "    Spese: " + formatEuro(spese);
    }

    private JComponent createClassificationIcon(final String type, final String name,
            final String iconName) {
        final ImageIcon icon = loadClassificationIcon(type, name, iconName);
        return new JComponent() {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(56, 56);
            }

            @Override
            protected void paintComponent(final Graphics graphics) {
                Graphics2D graphics2D = (Graphics2D) graphics.create();
                graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                graphics2D.setColor(classificationBackground(type));
                graphics2D.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);

                if (icon != null) {
                    int x = (getWidth() - icon.getIconWidth()) / 2;
                    int y = (getHeight() - icon.getIconHeight()) / 2;
                    icon.paintIcon(this, graphics2D, x, y);
                } else {
                    paintFallbackClassificationIcon(graphics2D, type, getWidth(), getHeight());
                }
                graphics2D.dispose();
            }
        };
    }

    private ImageIcon loadClassificationIcon(final String type, final String name,
            final String iconName) {
        if (IconStorage.isCustomIconReference(iconName)) {
            final ImageIcon customIcon = loadIconFile(iconName, 30);
            if (customIcon != null) {
                return customIcon;
            }
        }

        final String resourcePath = getClassificationIconPath(type, name, iconName);
        ImageIcon rawIcon = null;

        final java.net.URL url = getClass().getResource(resourcePath);
        if (url != null) {
            rawIcon = new ImageIcon(url);
        } else {
            final String relativePath = resourcePath.substring(1).replace("/", java.io.File.separator);
            final java.util.List<java.nio.file.Path> possiblePaths = java.util.List.of(
                    java.nio.file.Paths.get("src", relativePath),
                    java.nio.file.Paths.get("bin", relativePath),
                    java.nio.file.Paths.get("build", "classes", relativePath));

            for (java.nio.file.Path path : possiblePaths) {
                if (java.nio.file.Files.exists(path)) {
                    rawIcon = new ImageIcon(path.toString());
                    break;
                }
            }
        }

        if (rawIcon == null || rawIcon.getIconWidth() <= 0 || rawIcon.getIconHeight() <= 0) {
            return null;
        }

        final Image scaledImage = rawIcon.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
        return new ImageIcon(scaledImage);
    }

    private String getClassificationIconPath(final String type, final String name,
            final String iconName) {
        if (iconName != null && !iconName.isBlank()
                && iconName.matches("[a-z0-9_-]+\\.png")) {
            return "/it/unibo/sage/view/icons/" + iconName;
        }
        final String normalized = normalizeClassificationName(name);
        if ("Categoria".equals(type)) {
            switch (normalized) {
                case "casa":
                case "affitto":
                    return "/it/unibo/sage/view/icons/house.png";
                case "spesa":
                case "cibo":
                case "alimentari":
                case "alimentazione":
                    return "/it/unibo/sage/view/icons/food.png";
                case "trasporti":
                case "trasporto":
                case "bus":
                case "treno":
                    return "/it/unibo/sage/view/icons/transport.png";
                case "salute":
                case "medicina":
                case "farmacia":
                    return "/it/unibo/sage/view/icons/health.png";
                case "studio":
                case "universita":
                case "libri":
                    return "/it/unibo/sage/view/icons/study.png";
                case "stipendio":
                case "entrate":
                case "lavoro":
                    return "/it/unibo/sage/view/icons/work.png";
                case "risparmi":
                case "risparmio":
                    return "/it/unibo/sage/view/icons/savings.png";
                case "shopping":
                case "acquisti":
                    return "/it/unibo/sage/view/icons/shopping.png";
                case "svago":
                case "tempo libero":
                    return "/it/unibo/sage/view/icons/leisure.png";
                case "bollette":
                case "utenze":
                    return "/it/unibo/sage/view/icons/bill.png";
                default:
                    return "/it/unibo/sage/view/icons/generic_category.png";
            }
        }
        if ("Fonte".equals(type)) {
            switch (normalized) {
                case "stipendio":
                case "salario":
                case "lavoro":
                    return "/it/unibo/sage/view/icons/salary.png";
                case "borsa di studio":
                case "borsa studio":
                case "universita":
                    return "/it/unibo/sage/view/icons/scholarship.png";
                case "regalo":
                case "regali":
                    return "/it/unibo/sage/view/icons/gift.png";
                case "rimborso":
                case "rimborsi":
                    return "/it/unibo/sage/view/icons/refund.png";
                case "ripetizioni private":
                case "ripetizioni":
                case "lezioni":
                    return "/it/unibo/sage/view/icons/tutoring.png";
                case "lavoretto weekend":
                case "lavoretto":
                case "lavoro occasionale":
                    return "/it/unibo/sage/view/icons/work.png";
                case "famiglia":
                case "aiuto famiglia":
                    return "/it/unibo/sage/view/icons/family.png";
                default:
                    return "/it/unibo/sage/view/icons/generic_source.png";
            }
        }
        switch (normalized) {
            case "urgente":
            case "importante":
                return "/it/unibo/sage/view/icons/urgent.png";
            case "universita":
            case "esami":
            case "studio":
                return "/it/unibo/sage/view/icons/study.png";
            case "palestra":
            case "gym":
            case "sport":
                return "/it/unibo/sage/view/icons/gym.png";
            case "lavoro":
                return "/it/unibo/sage/view/icons/work.png";
            case "famiglia":
                return "/it/unibo/sage/view/icons/family.png";
            case "viaggio":
            case "viaggi":
            case "travel":
                return "/it/unibo/sage/view/icons/travel.png";
            default:
                return "/it/unibo/sage/view/icons/generic_tag.png";
        }
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

    private Color classificationBackground(final String type) {
        if ("Categoria".equals(type)) {
            return new Color(37, 99, 235, 34);
        }
        if ("Fonte".equals(type)) {
            return new Color(245, 158, 11, 38);
        }
        return new Color(20, 184, 166, 38);
    }

    private Color classificationAccent(final String type) {
        if ("Categoria".equals(type)) {
            return AppTheme.PRIMARY;
        }
        if ("Fonte".equals(type)) {
            return new Color(217, 119, 6);
        }
        return AppTheme.ACCENT_HOVER;
    }

    private void paintFallbackClassificationIcon(final Graphics2D graphics2D, final String type,
            final int width, final int height) {
        graphics2D.setColor(classificationAccent(type));
        graphics2D.fillOval(width / 2 - 9, height / 2 - 9, 18, 18);
        graphics2D.setFont(new Font("SansSerif", Font.BOLD, 18));
        graphics2D.setColor(Color.WHITE);
        final String letter = "Categoria".equals(type) ? "C" : ("Fonte".equals(type) ? "F" : "T");
        FontMetrics metrics = graphics2D.getFontMetrics();
        int x = (width - metrics.stringWidth(letter)) / 2;
        int y = (height - metrics.getHeight()) / 2 + metrics.getAscent();
        graphics2D.drawString(letter, x, y);
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

        JLabel description = new JLabel(overviewPeriodDescription());
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

    private List<Transazione> filterOverviewTransactions(final List<Transazione> transazioni) {
        if (overviewFilter == OverviewFilter.ALL) {
            return transazioni;
        }

        final LocalDate today = LocalDate.now();
        final List<Transazione> filtered = new ArrayList<>();
        for (Transazione transazione : transazioni) {
            final LocalDate date = transazione.getData();
            if (date == null) {
                continue;
            }
            if (overviewFilter == OverviewFilter.MONTH
                    && date.getYear() == today.getYear()
                    && date.getMonthValue() == today.getMonthValue()) {
                filtered.add(transazione);
            } else if (overviewFilter == OverviewFilter.YEAR
                    && date.getYear() == today.getYear()) {
                filtered.add(transazione);
            }
        }
        return filtered;
    }

    private String overviewPeriodDescription() {
        final LocalDate today = LocalDate.now();
        if (overviewFilter == OverviewFilter.MONTH) {
            String month = today.getMonth().getDisplayName(TextStyle.FULL, Locale.ITALIAN);
            if (!month.isEmpty()) {
                month = Character.toUpperCase(month.charAt(0)) + month.substring(1);
            }
            return month + " " + today.getYear();
        }
        if (overviewFilter == OverviewFilter.YEAR) {
            return "Anno " + today.getYear();
        }
        return "Intero storico disponibile";
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
            final List<DayExpense> dailyExpenses = buildDailyExpenses(transazioni, currentMonth);
            final DailyExpenseChartPanel chartPanel = new DailyExpenseChartPanel(dailyExpenses);
            chartPanel.setPreferredSize(new Dimension(340, 240));

            String monthName = currentMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.ITALIAN);
            if (!monthName.isEmpty()) {
                monthName = Character.toUpperCase(monthName.charAt(0)) + monthName.substring(1);
            }
            return createAnalyticsCard(
                    "Andamento mensile",
                    "Entrate e spese giorno per giorno di " + monthName + " " + currentMonth.getYear() + ".",
                    chartPanel);
        }

        final List<MonthTotals> monthTotals = overviewFilter == OverviewFilter.YEAR
                ? buildYearMonthlyTotals(transazioni, LocalDate.now().getYear())
                : buildMonthlyTotals(transazioni, 6);
        final MonthlyTrendChartPanel chartPanel = new MonthlyTrendChartPanel(monthTotals);
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
        final LinkedHashMap<String, BigDecimal> expensesByCategory = buildExpenseDistribution(transazioni, categoryNames);
        final JPanel content = new JPanel(new BorderLayout(12, 0));
        content.setOpaque(false);

        final ExpenseDistributionChartPanel chartPanel = new ExpenseDistributionChartPanel(expensesByCategory);
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
                content.add(createBudgetProgressRow(budget, categoryNames));
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
        JPanel row = new JPanel(new BorderLayout(10, 8));
        row.setOpaque(false);

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

        row.add(top, BorderLayout.NORTH);
        row.add(progressBar, BorderLayout.CENTER);
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
            swatch.setBackground(chartColor(index));
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

    private List<DayExpense> buildDailyExpenses(final List<Transazione> transazioni,
            final YearMonth month) {
        final BigDecimal[] incomes = new BigDecimal[month.lengthOfMonth()];
        final BigDecimal[] expenses = new BigDecimal[month.lengthOfMonth()];
        for (int i = 0; i < month.lengthOfMonth(); i++) {
            incomes[i] = BigDecimal.ZERO;
            expenses[i] = BigDecimal.ZERO;
        }

        for (Transazione transazione : transazioni) {
            if (transazione.getData() == null
                    || !YearMonth.from(transazione.getData()).equals(month)) {
                continue;
            }

            final int dayIndex = transazione.getData().getDayOfMonth() - 1;
            if (transazione.getTipo() == TipoTransazione.ENTRATA) {
                incomes[dayIndex] = incomes[dayIndex].add(transazione.getImporto());
            } else {
                expenses[dayIndex] = expenses[dayIndex].add(transazione.getImporto());
            }
        }

        final List<DayExpense> result = new ArrayList<>();
        for (int i = 0; i < month.lengthOfMonth(); i++) {
            result.add(new DayExpense(i + 1, incomes[i], expenses[i]));
        }
        return result;
    }

    private List<MonthTotals> buildYearMonthlyTotals(final List<Transazione> transazioni,
            final int year) {
        final List<MonthTotals> result = new ArrayList<>();
        for (int monthNumber = 1; monthNumber <= 12; monthNumber++) {
            final YearMonth currentMonth = YearMonth.of(year, monthNumber);
            BigDecimal income = BigDecimal.ZERO;
            BigDecimal expense = BigDecimal.ZERO;
            for (Transazione transazione : transazioni) {
                if (transazione.getData() == null
                        || !YearMonth.from(transazione.getData()).equals(currentMonth)) {
                    continue;
                }
                if (transazione.getTipo() == TipoTransazione.ENTRATA) {
                    income = income.add(transazione.getImporto());
                } else {
                    expense = expense.add(transazione.getImporto());
                }
            }
            String label = currentMonth.getMonth().getDisplayName(TextStyle.SHORT, Locale.ITALIAN);
            if (!label.isEmpty()) {
                label = Character.toUpperCase(label.charAt(0)) + label.substring(1).replace(".", "");
            }
            result.add(new MonthTotals(label, income, expense));
        }
        return result;
    }

    private List<MonthTotals> buildMonthlyTotals(final List<Transazione> transazioni, final int months) {
        LocalDate referenceDate = LocalDate.now();
        for (Transazione transazione : transazioni) {
            if (transazione.getData() != null && transazione.getData().isAfter(referenceDate)) {
                referenceDate = transazione.getData();
            }
        }
        if (!transazioni.isEmpty()) {
            referenceDate = transazioni.stream()
                    .map(Transazione::getData)
                    .filter(d -> d != null)
                    .max(Comparator.naturalOrder())
                    .orElse(referenceDate);
        }

        final List<MonthTotals> result = new ArrayList<>();
        final YearMonth end = YearMonth.from(referenceDate);
        for (int i = months - 1; i >= 0; i--) {
            final YearMonth currentMonth = end.minusMonths(i);
            BigDecimal income = BigDecimal.ZERO;
            BigDecimal expense = BigDecimal.ZERO;
            for (Transazione transazione : transazioni) {
                if (transazione.getData() == null || !YearMonth.from(transazione.getData()).equals(currentMonth)) {
                    continue;
                }
                if (transazione.getTipo() == TipoTransazione.ENTRATA) {
                    income = income.add(transazione.getImporto());
                } else {
                    expense = expense.add(transazione.getImporto());
                }
            }
            String label = currentMonth.getMonth().getDisplayName(TextStyle.SHORT, Locale.ITALIAN);
            if (!label.isEmpty()) {
                label = Character.toUpperCase(label.charAt(0)) + label.substring(1).replace(".", "");
            }
            result.add(new MonthTotals(label, income, expense));
        }
        return result;
    }

    private LinkedHashMap<String, BigDecimal> buildExpenseDistribution(final List<Transazione> transazioni,
            final Map<Long, String> categoryNames) {
        final Map<String, BigDecimal> totals = new HashMap<>();
        for (Transazione transazione : transazioni) {
            if (transazione.getTipo() != TipoTransazione.SPESA) {
                continue;
            }
            String category = transazione.getIdCategoria() == null
                    ? "Senza categoria"
                    : categoryNames.getOrDefault(transazione.getIdCategoria(), "Categoria " + transazione.getIdCategoria());
            totals.merge(category, transazione.getImporto(), BigDecimal::add);
        }

        List<Map.Entry<String, BigDecimal>> sorted = new ArrayList<>(totals.entrySet());
        sorted.sort((left, right) -> right.getValue().compareTo(left.getValue()));

        LinkedHashMap<String, BigDecimal> result = new LinkedHashMap<>();
        BigDecimal other = BigDecimal.ZERO;
        int limit = 5;
        for (int i = 0; i < sorted.size(); i++) {
            Map.Entry<String, BigDecimal> entry = sorted.get(i);
            if (i < limit) {
                result.put(entry.getKey(), entry.getValue());
            } else {
                other = other.add(entry.getValue());
            }
        }
        if (other.signum() > 0) {
            result.put("Altro", other);
        }
        return result;
    }

    private Color chartColor(final int index) {
        Color[] colors = {
            AppTheme.PRIMARY,
            AppTheme.ACCENT,
            AppTheme.BUDGET,
            AppTheme.EXPENSE,
            AppTheme.INCOME,
            new Color(124, 58, 237),
            new Color(14, 165, 233)
        };
        return colors[index % colors.length];
    }

    private String formatCompactAmount(final double value) {
        if (value >= 1000.0) {
            return String.format(Locale.US, "%.1fk", value / 1000.0).replace('.', ',');
        }
        return String.format(Locale.US, "%.0f", value).replace('.', ',');
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
            return movimentiController.caricaTagTransazione(currentUser.getEmail(), idTransazione);
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
        final List<IconChoice> choices = category
                ? CATEGORY_ICON_CHOICES
                : (source ? SOURCE_ICON_CHOICES : TAG_ICON_CHOICES);
        final String defaultIcon = category
                ? "generic_category.png"
                : (source ? "generic_source.png" : "generic_tag.png");
        final JTextField nameField = createDialogTextField(initialName);
        final IconSelectionPanel iconSelector = new IconSelectionPanel(
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

    private JComboBox<IconChoice> createIconChoiceCombo(final List<IconChoice> choices,
            final String selectedIcon) {
        final JComboBox<IconChoice> combo = new JComboBox<>(choices.toArray(new IconChoice[0]));
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(final JList<?> list, final Object value,
                    final int index, final boolean isSelected, final boolean cellHasFocus) {
                final JLabel label = (JLabel) super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
                if (value instanceof IconChoice) {
                    final IconChoice choice = (IconChoice) value;
                    label.setText(choice.label);
                    label.setIcon(loadIconFile(choice.fileName, 22));
                    label.setIconTextGap(10);
                }
                return label;
            }
        });
        for (int i = 0; i < combo.getItemCount(); i++) {
            if (combo.getItemAt(i).fileName.equals(selectedIcon)) {
                combo.setSelectedIndex(i);
                break;
            }
        }
        return combo;
    }

    private ImageIcon loadIconFile(final String fileName, final int size) {
        if (IconStorage.isCustomIconReference(fileName)) {
            final Path customPath = IconStorage.resolveCustomIcon(fileName);
            if (customPath == null || !Files.isRegularFile(customPath)) {
                return null;
            }
            final ImageIcon customIcon = new ImageIcon(customPath.toString());
            if (customIcon.getIconWidth() <= 0 || customIcon.getIconHeight() <= 0) {
                return null;
            }
            return new ImageIcon(customIcon.getImage().getScaledInstance(
                    size, size, Image.SCALE_SMOOTH));
        }

        final String resourcePath = "/it/unibo/sage/view/icons/" + fileName;
        ImageIcon rawIcon = null;
        final java.net.URL url = getClass().getResource(resourcePath);
        if (url != null) {
            rawIcon = new ImageIcon(url);
        } else {
            final String relativePath = resourcePath.substring(1).replace("/", java.io.File.separator);
            for (java.nio.file.Path path : java.util.List.of(
                    java.nio.file.Paths.get("src", relativePath),
                    java.nio.file.Paths.get("bin", relativePath),
                    java.nio.file.Paths.get("build", "classes", relativePath))) {
                if (java.nio.file.Files.exists(path)) {
                    rawIcon = new ImageIcon(path.toString());
                    break;
                }
            }
        }
        if (rawIcon == null || rawIcon.getIconWidth() <= 0 || rawIcon.getIconHeight() <= 0) {
            return null;
        }
        return new ImageIcon(rawIcon.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH));
    }

    /**
     * Selettore unico usato da categorie, tag e fonti. L'utente puo' scegliere
     * un'icona inclusa nell'app oppure trascinare una propria immagine.
     */
    private final class IconSelectionPanel extends JPanel {

        private final JComboBox<IconChoice> presetCombo;
        private final JLabel previewLabel = new JLabel();
        private final JLabel statusLabel = new JLabel();
        private File pendingCustomFile;
        private String selectedReference;

        private IconSelectionPanel(final List<IconChoice> choices,
                final String initialIcon) {
            super(new BorderLayout(0, 8));
            setOpaque(false);
            setPreferredSize(new Dimension(390, 150));

            presetCombo = createIconChoiceCombo(choices, initialIcon);
            add(presetCombo, BorderLayout.NORTH);

            final JPanel dropArea = new JPanel(new BorderLayout(10, 0));
            dropArea.setOpaque(true);
            dropArea.setBackground(new Color(248, 250, 252));
            dropArea.setBorder(BorderFactory.createDashedBorder(
                    AppTheme.BORDER, 1.5f, 5.0f, 3.0f, true));
            dropArea.setPreferredSize(new Dimension(390, 96));
            dropArea.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            previewLabel.setPreferredSize(new Dimension(62, 62));
            previewLabel.setHorizontalAlignment(SwingConstants.CENTER);
            previewLabel.setVerticalAlignment(SwingConstants.CENTER);
            previewLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

            final JPanel instructions = new JPanel();
            instructions.setOpaque(false);
            instructions.setLayout(new BoxLayout(instructions, BoxLayout.Y_AXIS));

            final JLabel title = new JLabel("Trascina qui una tua immagine");
            title.setFont(new Font("SansSerif", Font.BOLD, 12));
            title.setForeground(AppTheme.TEXT);

            statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
            statusLabel.setForeground(AppTheme.TEXT_MUTED);

            final JButton chooseButton = new JButton("Scegli file");
            chooseButton.setFocusable(false);
            chooseButton.addActionListener(e -> chooseCustomImage());

            instructions.add(Box.createVerticalGlue());
            instructions.add(title);
            instructions.add(Box.createVerticalStrut(4));
            instructions.add(statusLabel);
            instructions.add(Box.createVerticalStrut(6));
            instructions.add(chooseButton);
            instructions.add(Box.createVerticalGlue());

            dropArea.add(previewLabel, BorderLayout.WEST);
            dropArea.add(instructions, BorderLayout.CENTER);
            add(dropArea, BorderLayout.CENTER);

            final TransferHandler dropHandler = createImageDropHandler();
            installTransferHandler(dropArea, dropHandler);

            selectedReference = initialIcon;
            if (IconStorage.isCustomIconReference(initialIcon)) {
                final ImageIcon currentIcon = loadIconFile(initialIcon, 50);
                if (currentIcon != null) {
                    previewLabel.setIcon(currentIcon);
                    statusLabel.setText("Icona personalizzata attuale");
                } else {
                    selectPreset((IconChoice) presetCombo.getSelectedItem());
                }
            } else {
                selectPreset((IconChoice) presetCombo.getSelectedItem());
            }

            presetCombo.addActionListener(e ->
                    selectPreset((IconChoice) presetCombo.getSelectedItem()));
        }

        private void selectPreset(final IconChoice choice) {
            if (choice == null) {
                return;
            }
            pendingCustomFile = null;
            selectedReference = choice.fileName;
            previewLabel.setIcon(loadIconFile(choice.fileName, 50));
            statusLabel.setText("Predefinita: " + choice.label
                    + " - oppure trascina PNG/JPG/GIF/BMP");
        }

        private void chooseCustomImage() {
            final JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Scegli icona personalizzata");
            chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                    "Immagini (PNG, JPG, GIF, BMP)", "png", "jpg", "jpeg", "gif", "bmp"));
            if (chooser.showOpenDialog(DashboardPanel.this) == JFileChooser.APPROVE_OPTION) {
                selectCustomFile(chooser.getSelectedFile());
            }
        }

        private TransferHandler createImageDropHandler() {
            return new TransferHandler() {
                @Override
                public boolean canImport(final TransferSupport support) {
                    return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
                }

                @Override
                public boolean importData(final TransferSupport support) {
                    if (!canImport(support)) {
                        return false;
                    }
                    try {
                        final Object data = support.getTransferable().getTransferData(
                                DataFlavor.javaFileListFlavor);
                        if (!(data instanceof java.util.List<?>)) {
                            return false;
                        }
                        final java.util.List<?> files = (java.util.List<?>) data;
                        if (files.isEmpty() || !(files.get(0) instanceof File)) {
                            return false;
                        }
                        selectCustomFile((File) files.get(0));
                        return true;
                    } catch (final Exception ex) {
                        showImageSelectionError(ex.getMessage());
                        return false;
                    }
                }
            };
        }

        private void installTransferHandler(final Component component,
                final TransferHandler handler) {
            if (component instanceof JComponent) {
                ((JComponent) component).setTransferHandler(handler);
            }
            if (component instanceof Container) {
                for (Component child : ((Container) component).getComponents()) {
                    installTransferHandler(child, handler);
                }
            }
        }

        private void selectCustomFile(final File file) {
            try {
                if (file == null || !file.isFile()) {
                    throw new IOException("File non valido");
                }
                if (file.length() > 10L * 1024L * 1024L) {
                    throw new IOException("L'immagine supera il limite di 10 MB");
                }
                final ImageIcon rawIcon = new ImageIcon(file.getAbsolutePath());
                if (rawIcon.getIconWidth() <= 0 || rawIcon.getIconHeight() <= 0) {
                    throw new IOException("Formato immagine non supportato");
                }
                pendingCustomFile = file;
                selectedReference = null;
                previewLabel.setIcon(new ImageIcon(rawIcon.getImage().getScaledInstance(
                        50, 50, Image.SCALE_SMOOTH)));
                statusLabel.setText("Personalizzata: " + file.getName());
            } catch (final IOException ex) {
                showImageSelectionError(ex.getMessage());
            }
        }

        private void showImageSelectionError(final String message) {
            JOptionPane.showMessageDialog(DashboardPanel.this,
                    "Immagine non valida: " + message,
                    "Errore immagine",
                    JOptionPane.ERROR_MESSAGE);
        }

        private String resolveIconReference() throws IOException {
            if (pendingCustomFile != null) {
                return IconStorage.saveCustomIcon(pendingCustomFile);
            }
            if (selectedReference == null || selectedReference.isBlank()) {
                final IconChoice choice = (IconChoice) presetCombo.getSelectedItem();
                if (choice == null) {
                    throw new IOException("Seleziona un'icona");
                }
                return choice.fileName;
            }
            return selectedReference;
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
            JOptionPane.showMessageDialog(this, "Seleziona una categoria o un tag personale.");
            return;
        }
        final String origin = String.valueOf(table.getValueAt(selectedRow, 3));
        if (!"Personale".equals(origin)) {
            JOptionPane.showMessageDialog(this, "Gli elementi di sistema non si modificano.");
            return;
        }
        final String type = String.valueOf(table.getValueAt(selectedRow, 0));
        final long id = Long.parseLong(String.valueOf(table.getValueAt(selectedRow, 1)));
        final String oldName = String.valueOf(table.getValueAt(selectedRow, 2));
        renameClassification(type, id, oldName);
    }

    private void deleteSelectedClassification(final JTable table) {
        final int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Seleziona una categoria o un tag personale.");
            return;
        }
        final String origin = String.valueOf(table.getValueAt(selectedRow, 3));
        if (!"Personale".equals(origin)) {
            JOptionPane.showMessageDialog(this, "Gli elementi di sistema non si eliminano.");
            return;
        }
        final String type = String.valueOf(table.getValueAt(selectedRow, 0));
        final long id = Long.parseLong(String.valueOf(table.getValueAt(selectedRow, 1)));
        deleteClassification(type, id);
    }

    private void generateDueRecurringExpensesOnStartup() {
        try {
            speseRicorrentiController.generaSpeseScadute(currentUser.getEmail(), LocalDate.now());
        } catch (final SQLException ex) {
            System.err.println("Generazione automatica delle ricorrenze non completata: "
                    + ex.getMessage());
        }
    }

    private List<Transazione> loadTransactions() {
        try {
            return movimentiController.caricaTransazioni(
                    currentUser.getEmail(),
                    LocalDate.of(1900, 1, 1),
                    LocalDate.of(2100, 12, 31));
        } catch (final SQLException ex) {
            showLoadError("transazioni", ex);
            return List.of();
        }
    }

    private List<Budget> loadBudgets() {
        try {
            return budgetController.caricaBudget(currentUser.getEmail());
        } catch (final SQLException ex) {
            showLoadError("budget", ex);
            return List.of();
        }
    }

    private List<SpesaRicorrente> loadRecurringExpenses() {
        try {
            return speseRicorrentiController.caricaRicorrenze(currentUser.getEmail());
        } catch (final SQLException ex) {
            showLoadError("spese ricorrenti", ex);
            return List.of();
        }
    }

    private List<Categoria> loadCategories() {
        try {
            return movimentiController.caricaCategorieDisponibili(currentUser.getEmail());
        } catch (final SQLException ex) {
            showLoadError("categorie", ex);
            return List.of();
        }
    }

    private Map<Long, String> loadCategoryNames() {
        final Map<Long, String> names = new HashMap<>();
        for (Categoria categoria : loadCategories()) {
            names.put(categoria.getId(), categoria.getNome());
        }
        return names;
    }

    private List<Tag> loadTags() {
        try {
            return movimentiController.caricaTagDisponibili(currentUser.getEmail());
        } catch (final SQLException ex) {
            showLoadError("tag", ex);
            return List.of();
        }
    }

    private List<Fonte> loadSources() {
        try {
            return movimentiController.caricaFontiDisponibili(currentUser.getEmail());
        } catch (final SQLException ex) {
            showLoadError("fonti", ex);
            return List.of();
        }
    }

    private BigDecimal sumByType(final List<Transazione> transazioni, final TipoTransazione tipo) {
        BigDecimal totale = BigDecimal.ZERO;
        for (Transazione transazione : transazioni) {
            if (transazione.getTipo() == tipo) {
                totale = totale.add(transazione.getImporto());
            }
        }
        return totale;
    }

    private String calculateBudgetUsage(final List<Budget> budgets) {
        for (Budget budget : budgets) {
            if (budget.getIdCategoria() == null && budget.getImportoLimite().signum() > 0) {
                return budget.getTotaleSpesoAttuale()
                        .multiply(BigDecimal.valueOf(100))
                        .divide(budget.getImportoLimite(), 0, java.math.RoundingMode.HALF_UP)
                        + "%";
            }
        }
        return "0%";
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

    private enum OverviewFilter {
        MONTH,
        YEAR,
        ALL
    }

    private static final class DayExpense {
        private final int day;
        private final BigDecimal income;
        private final BigDecimal expense;

        private DayExpense(final int day, final BigDecimal income, final BigDecimal expense) {
            this.day = day;
            this.income = income;
            this.expense = expense;
        }
    }

    private final class DailyExpenseChartPanel extends JPanel {
        private final List<DayExpense> dailyExpenses;

        private DailyExpenseChartPanel(final List<DayExpense> dailyExpenses) {
            this.dailyExpenses = dailyExpenses;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(final Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();
            int left = 42;
            int right = 16;
            int top = 18;
            int bottom = 32;
            int chartWidth = width - left - right;
            int chartHeight = height - top - bottom;

            double maxValue = 0.0;
            for (DayExpense dayExpense : dailyExpenses) {
                maxValue = Math.max(maxValue, dayExpense.income.doubleValue());
                maxValue = Math.max(maxValue, dayExpense.expense.doubleValue());
            }
            if (maxValue <= 0.0) {
                g2.setColor(AppTheme.TEXT_MUTED);
                g2.setFont(new Font("SansSerif", Font.PLAIN, 13));
                g2.drawString("Nessuna entrata o spesa registrata nel mese selezionato.", 16, height / 2);
                g2.dispose();
                return;
            }

            for (int i = 0; i <= 4; i++) {
                int y = top + (int) Math.round(chartHeight * i / 4.0);
                g2.setColor(new Color(225, 232, 240));
                g2.drawLine(left, y, width - right, y);

                double labelValue = maxValue * (4 - i) / 4.0;
                g2.setColor(AppTheme.TEXT_MUTED);
                g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
                g2.drawString(formatCompactAmount(labelValue), 4, y + 4);
            }

            int days = Math.max(1, dailyExpenses.size());
            double slotWidth = chartWidth / (double) days;
            int barWidth = Math.max(2, Math.min(8, (int) Math.floor(slotWidth * 0.34)));
            int chartBottom = top + chartHeight;

            for (int i = 0; i < dailyExpenses.size(); i++) {
                DayExpense dayExpense = dailyExpenses.get(i);
                int groupWidth = barWidth * 2 + 2;
                int baseX = left + (int) Math.round(i * slotWidth + (slotWidth - groupWidth) / 2.0);
                int incomeHeight = (int) Math.round(chartHeight * (dayExpense.income.doubleValue() / maxValue));
                int expenseHeight = (int) Math.round(chartHeight * (dayExpense.expense.doubleValue() / maxValue));

                if (incomeHeight > 0) {
                    g2.setColor(AppTheme.INCOME);
                    g2.fillRoundRect(baseX, chartBottom - incomeHeight, barWidth, incomeHeight, 5, 5);
                }
                if (expenseHeight > 0) {
                    g2.setColor(AppTheme.EXPENSE);
                    g2.fillRoundRect(baseX + barWidth + 2, chartBottom - expenseHeight,
                            barWidth, expenseHeight, 5, 5);
                }

                boolean showLabel = dayExpense.day == 1
                        || dayExpense.day == dailyExpenses.size()
                        || dayExpense.day % 5 == 0;
                if (showLabel) {
                    String label = String.valueOf(dayExpense.day);
                    g2.setColor(AppTheme.TEXT_MUTED);
                    g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
                    int labelWidth = g2.getFontMetrics().stringWidth(label);
                    int labelX = left + (int) Math.round((i + 0.5) * slotWidth) - labelWidth / 2;
                    g2.drawString(label, labelX, height - 8);
                }
            }

            g2.setColor(AppTheme.INCOME);
            g2.fillRoundRect(width - 130, 4, 12, 12, 4, 4);
            g2.setColor(AppTheme.TEXT_MUTED);
            g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
            g2.drawString("Entrate", width - 112, 14);
            g2.setColor(AppTheme.EXPENSE);
            g2.fillRoundRect(width - 62, 4, 12, 12, 4, 4);
            g2.setColor(AppTheme.TEXT_MUTED);
            g2.drawString("Spese", width - 44, 14);

            g2.dispose();
        }
    }

    private static final class MonthTotals {
        private final String label;
        private final BigDecimal income;
        private final BigDecimal expense;

        private MonthTotals(final String label, final BigDecimal income, final BigDecimal expense) {
            this.label = label;
            this.income = income;
            this.expense = expense;
        }
    }

    private final class MonthlyTrendChartPanel extends JPanel {
        private final List<MonthTotals> monthTotals;

        private MonthlyTrendChartPanel(final List<MonthTotals> monthTotals) {
            this.monthTotals = monthTotals;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(final Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();
            int left = 42;
            int right = 16;
            int top = 18;
            int bottom = 32;
            int chartWidth = width - left - right;
            int chartHeight = height - top - bottom;

            double maxValue = 0.0;
            for (MonthTotals totals : monthTotals) {
                maxValue = Math.max(maxValue, totals.income.doubleValue());
                maxValue = Math.max(maxValue, totals.expense.doubleValue());
            }
            if (maxValue <= 0.0) {
                g2.setColor(AppTheme.TEXT_MUTED);
                g2.setFont(new Font("SansSerif", Font.PLAIN, 13));
                g2.drawString("Nessun dato disponibile per il grafico.", 16, height / 2);
                g2.dispose();
                return;
            }

            g2.setColor(new Color(225, 232, 240));
            for (int i = 0; i <= 4; i++) {
                int y = top + (int) Math.round(chartHeight * i / 4.0);
                g2.drawLine(left, y, width - right, y);
                double labelValue = maxValue * (4 - i) / 4.0;
                g2.setColor(AppTheme.TEXT_MUTED);
                g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
                g2.drawString(formatCompactAmount(labelValue), 4, y + 4);
                g2.setColor(new Color(225, 232, 240));
            }

            int months = Math.max(1, monthTotals.size());
            int groupWidth = Math.max(32, chartWidth / months);
            int barWidth = Math.max(10, Math.min(24, (groupWidth - 12) / 2));
            int chartBottom = top + chartHeight;

            for (int i = 0; i < monthTotals.size(); i++) {
                MonthTotals totals = monthTotals.get(i);
                int baseX = left + i * groupWidth + Math.max(6, (groupWidth - (barWidth * 2 + 6)) / 2);
                int incomeHeight = (int) Math.round(chartHeight * (totals.income.doubleValue() / maxValue));
                int expenseHeight = (int) Math.round(chartHeight * (totals.expense.doubleValue() / maxValue));

                g2.setColor(AppTheme.INCOME);
                g2.fillRoundRect(baseX, chartBottom - incomeHeight, barWidth, incomeHeight, 8, 8);
                g2.setColor(AppTheme.EXPENSE);
                g2.fillRoundRect(baseX + barWidth + 6, chartBottom - expenseHeight, barWidth, expenseHeight, 8, 8);

                g2.setColor(AppTheme.TEXT_MUTED);
                g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
                int labelWidth = g2.getFontMetrics().stringWidth(totals.label);
                int labelX = left + i * groupWidth + (groupWidth - labelWidth) / 2;
                g2.drawString(totals.label, labelX, height - 8);
            }

            g2.setColor(AppTheme.INCOME);
            g2.fillRoundRect(width - 130, 4, 12, 12, 4, 4);
            g2.setColor(AppTheme.TEXT_MUTED);
            g2.drawString("Entrate", width - 112, 14);
            g2.setColor(AppTheme.EXPENSE);
            g2.fillRoundRect(width - 62, 4, 12, 12, 4, 4);
            g2.setColor(AppTheme.TEXT_MUTED);
            g2.drawString("Spese", width - 44, 14);

            g2.dispose();
        }
    }

    private final class ExpenseDistributionChartPanel extends JPanel {
        private final List<Map.Entry<String, BigDecimal>> slices;

        private ExpenseDistributionChartPanel(final LinkedHashMap<String, BigDecimal> expensesByCategory) {
            this.slices = new ArrayList<>(expensesByCategory.entrySet());
            setOpaque(false);
        }

        @Override
        protected void paintComponent(final Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            if (slices.isEmpty()) {
                g2.setColor(AppTheme.TEXT_MUTED);
                g2.setFont(new Font("SansSerif", Font.PLAIN, 13));
                g2.drawString("Nessuna spesa disponibile.", 12, getHeight() / 2);
                g2.dispose();
                return;
            }

            int size = Math.min(getWidth(), getHeight()) - 28;
            int x = (getWidth() - size) / 2;
            int y = (getHeight() - size) / 2;
            BigDecimal total = BigDecimal.ZERO;
            for (Map.Entry<String, BigDecimal> entry : slices) {
                total = total.add(entry.getValue());
            }

            double startAngle = 90.0;
            for (int i = 0; i < slices.size(); i++) {
                Map.Entry<String, BigDecimal> entry = slices.get(i);
                double angle = entry.getValue().doubleValue() * 360.0 / total.doubleValue();
                g2.setColor(chartColor(i));
                g2.fillArc(x, y, size, size, (int) Math.round(startAngle), (int) -Math.round(angle));
                startAngle -= angle;
            }

            int innerSize = (int) (size * 0.56);
            int innerX = x + (size - innerSize) / 2;
            int innerY = y + (size - innerSize) / 2;
            g2.setColor(new Color(255, 255, 255, 235));
            g2.fillOval(innerX, innerY, innerSize, innerSize);

            g2.setColor(AppTheme.TEXT_MUTED);
            g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
            String line1 = "Totale spese";
            String line2 = formatEuro(total);
            FontMetrics fm = g2.getFontMetrics();
            int line1Width = fm.stringWidth(line1);
            g2.drawString(line1, getWidth() / 2 - line1Width / 2, getHeight() / 2 - 6);

            g2.setColor(AppTheme.TEXT);
            g2.setFont(new Font("SansSerif", Font.BOLD, 14));
            fm = g2.getFontMetrics();
            int line2Width = fm.stringWidth(line2);
            g2.drawString(line2, getWidth() / 2 - line2Width / 2, getHeight() / 2 + 14);

            g2.dispose();
        }
    }

    private static final class IconChoice {
        private final String label;
        private final String fileName;

        private IconChoice(final String label, final String fileName) {
            this.label = label;
            this.fileName = fileName;
        }

        @Override
        public String toString() {
            return label;
        }
    }
}
