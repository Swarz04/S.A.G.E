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

    private static final String CARD_SETTINGS = "CARD_SETTINGS";
    private static final int SIDEBAR_WIDTH = 300;
    private static final int MENU_BUTTON_HEIGHT = 56;

    private final CardLayout contentLayout;
    private final JPanel contentPanel;
    private final Utente currentUser;
    private final MovimentiController movimentiController = new MovimentiController();
    private final BudgetController budgetController = new BudgetController();
    private final SpeseRicorrentiController speseRicorrentiController = new SpeseRicorrentiController();
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
        metricsPanel.add(createMetricBox("Saldo demo", formatEuro(saldo), AppTheme.PRIMARY));
        metricsPanel.add(createMetricBox("Entrate", formatEuro(entrate), AppTheme.INCOME));
        metricsPanel.add(createMetricBox("Spese", formatEuro(spese), AppTheme.EXPENSE));
        metricsPanel.add(createMetricBox("Budget usato", budgetUsage, AppTheme.BUDGET));

        JPanel lowerPanel = new JPanel(new GridLayout(1, 2, 16, 16));
        lowerPanel.setOpaque(false);
        lowerPanel.add(createMiniTransactionsBox(transazioni));
        lowerPanel.add(createMiniBudgetBox(budgets));

        panel.add(metricsPanel, BorderLayout.NORTH);
        panel.add(lowerPanel, BorderLayout.CENTER);

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
        final String[] columns = {"ID", "Categoria", "Importo", "Frequenza", "Inizio", "Prossima", "Fine"};
        final DefaultTableModel model = new DefaultTableModel(columns, 0);
        final Map<Long, String> categoryNames = loadCategoryNames();
        for (SpesaRicorrente ricorrenza : loadRecurringExpenses()) {
            model.addRow(new Object[] {
                ricorrenza.getId(),
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
                    categoria.isSystem()));
        }
        for (Tag tag : loadTags()) {
            grid.add(createClassificationTile(
                    "Tag",
                    tag.getId(),
                    tag.getNome(),
                    tag.isSystem()));
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
                    fonte.isSystem()));
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
        final JPanel tile = new GlassPanel(new BorderLayout(14, 0));
        tile.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        tile.setPreferredSize(new Dimension(0, 104));

        final JComponent icon = createClassificationIcon(type, name);
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
            SoftButton renameButton = createTinyActionButton("Rinomina");
            SoftButton deleteButton = createTinyActionButton("Elimina");
            renameButton.addActionListener(e -> renameClassification(type, id, name));
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

    private JComponent createClassificationIcon(final String type, final String name) {
        final ImageIcon icon = loadClassificationIcon(type, name);
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

    private ImageIcon loadClassificationIcon(final String type, final String name) {
        final String resourcePath = getClassificationIconPath(type, name);
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

    private String getClassificationIconPath(final String type, final String name) {
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

        JTextField amount = createDialogTextField();
        JTextField frequency = createDialogTextField("30");
        JTextField start = createDialogTextField(LocalDate.now().toString());
        JTextField next = createDialogTextField(LocalDate.now().toString());
        JTextField end = createDialogTextField();
        JComboBox<Categoria> category = createCategoryCombo(categories);

        JPanel form = createDialogFormPanel();
        addFormRow(form, "Importo", amount);
        addFormRow(form, "Frequenza giorni", frequency);
        addFormRow(form, "Data inizio", start);
        addFormRow(form, "Prossima scadenza", next);
        addFormRow(form, "Fine opzionale", end);
        addFormRow(form, "Categoria", category);

        SoftButton saveButton = createDialogButton("Salva", AppTheme.ACCENT);
        saveButton.addActionListener(e -> saveRecurringExpense(
                dialog, amount, frequency, start, next, end, category));

        root.add(form, BorderLayout.CENTER);
        root.add(createDialogFooter(dialog, saveButton), BorderLayout.SOUTH);
        return root;
    }

    private void saveRecurringExpense(final JDialog dialog, final JTextField amountField,
            final JTextField frequencyField, final JTextField startField,
            final JTextField nextField, final JTextField endField,
            final JComboBox<Categoria> categoryField) {
        try {
            final BigDecimal amount = parseAmount(amountField);
            final int frequency = Integer.parseInt(frequencyField.getText().trim());
            if (frequency <= 0) {
                throw new IllegalArgumentException("La frequenza deve essere positiva.");
            }
            final LocalDate start = LocalDate.parse(startField.getText().trim());
            final LocalDate next = LocalDate.parse(nextField.getText().trim());
            final LocalDate end = parseOptionalDate(endField);
            final Categoria category = (Categoria) categoryField.getSelectedItem();

            speseRicorrentiController.aggiungiRicorrenza(
                    currentUser.getEmail(), amount, frequency, start, next, end, category.getId());
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
        final String name = JOptionPane.showInputDialog(this, "Nome nuova categoria personale:");
        if (name == null || name.trim().isEmpty()) {
            return;
        }
        try {
            movimentiController.aggiungiCategoriaPersonale(currentUser.getEmail(), name.trim());
            refreshContent("CARD_CATEGORIES");
        } catch (final SQLException ex) {
            showLoadError("categorie", ex);
        }
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
        final String name = JOptionPane.showInputDialog(this, "Nome nuovo tag personale:");
        if (name == null || name.trim().isEmpty()) {
            return;
        }
        try {
            movimentiController.aggiungiTagPersonale(currentUser.getEmail(), name.trim());
            refreshContent("CARD_CATEGORIES");
        } catch (final SQLException ex) {
            showLoadError("tag", ex);
        }
    }

    private void addPersonalSource() {
        final String name = JOptionPane.showInputDialog(this, "Nome nuova fonte personale:");
        if (name == null || name.trim().isEmpty()) {
            return;
        }
        try {
            movimentiController.aggiungiFontePersonale(currentUser.getEmail(), name.trim());
            refreshContent("CARD_SOURCES");
        } catch (final SQLException ex) {
            showLoadError("fonti", ex);
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

    private List<Transazione> loadTransactions() {
        try {
            return movimentiController.caricaTransazioni(
                    currentUser.getEmail(),
                    LocalDate.of(2026, 1, 1),
                    LocalDate.of(2026, 12, 31));
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
}
