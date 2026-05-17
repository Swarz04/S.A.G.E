package it.unibo.sage.view;

import it.unibo.sage.controller.BudgetController;
import it.unibo.sage.controller.MovimentiController;
import it.unibo.sage.model.Budget;
import it.unibo.sage.model.Categoria;
import it.unibo.sage.model.Fonte;
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
        contentLayout.show(contentPanel, "CARD_OVERVIEW");
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
        newTransactionButton.addActionListener(e -> showNewTransactionDialog());

        headerPanel.add(titlePanel, BorderLayout.WEST);
        headerPanel.add(newTransactionButton, BorderLayout.EAST);

        return headerPanel;
    }

    private JPanel createMenuPanel() {
        JPanel menuPanel = new SidebarPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setPreferredSize(new Dimension(245, 0));
        menuPanel.setBorder(BorderFactory.createEmptyBorder(24, 18, 24, 18));

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
            AppTheme.SIDEBAR_USER_BOTTOM
        );
        userBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));
        userBox.setBorder(BorderFactory.createEmptyBorder(13, 13, 13, 13));

        JLabel avatar = new JLabel("U", SwingConstants.CENTER);
        avatar.setOpaque(true);
        avatar.setPreferredSize(new Dimension(34, 34));
        avatar.setBackground(AppTheme.AVATAR_BACKGROUND);
        avatar.setForeground(AppTheme.AVATAR_TEXT);
        avatar.setFont(new Font("SansSerif", Font.BOLD, 15));

        JLabel name = new JLabel(currentUser.getNome() + " " + currentUser.getCognome());
        name.setForeground(Color.WHITE);
        name.setFont(new Font("SansSerif", Font.BOLD, 13));

        userBox.add(avatar, BorderLayout.WEST);
        userBox.add(name, BorderLayout.CENTER);

        return userBox;
    }

    private JButton createMenuButton(String text, String cardName) {
        SoftButton button = new SoftButton(text);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        button.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setBackground(AppTheme.SIDEBAR_BUTTON);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
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
        selectedMenuButton.setBackground(AppTheme.SIDEBAR_BUTTON_SELECTED);
    }

    private void initContentCards() {
        contentPanel.add(createOverviewCard(), "CARD_OVERVIEW");
        contentPanel.add(createTransactionsCard(), "CARD_TRANSACTIONS");
        contentPanel.add(createBudgetCard(), "CARD_BUDGET");
        contentPanel.add(createCategoriesCard(), "CARD_CATEGORIES");
        contentPanel.add(createPlaceholderCard(
            "Spese Ricorrenti",
            "Modelli astratti da cui generare spese effettive periodiche."
        ), "CARD_RECURRING");
        contentPanel.add(new DocumentiPanel(currentUser), "CARD_DOCUMENTS");
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
        final String[] columns = {"Data", "Tipo", "Descrizione", "Importo"};
        final DefaultTableModel model = new DefaultTableModel(columns, 0);
        for (Transazione transazione : loadTransactions()) {
            model.addRow(new Object[] {
                transazione.getData(),
                labelTipo(transazione.getTipo()),
                transazione.getDescrizione(),
                formatEuro(transazione.getImporto())
            });
        }
        return createTableCard("Transazioni demo", "Movimenti caricati dal database per "
                + currentUser.getEmail(), model);
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

    private JPanel createCategoriesCard() {
        final String[] columns = {"Tipo", "ID", "Nome", "Origine"};
        final DefaultTableModel model = new DefaultTableModel(columns, 0);
        for (Categoria categoria : loadCategories()) {
            model.addRow(new Object[] {
                "Categoria",
                categoria.getId(),
                categoria.getNome(),
                categoria.isSystem() ? "Sistema" : "Personale"
            });
        }
        for (Tag tag : loadTags()) {
            model.addRow(new Object[] {
                "Tag",
                tag.getId(),
                tag.getNome(),
                tag.isSystem() ? "Sistema" : "Personale"
            });
        }

        JTable table = new JTable(model);
        table.setFillsViewportHeight(true);
        table.setRowHeight(28);
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actions.setOpaque(false);
        SoftButton addCategoryButton = createSmallActionButton("+ Categoria");
        SoftButton addTagButton = createSmallActionButton("+ Tag");
        SoftButton renameButton = createSmallActionButton("Rinomina");
        SoftButton deleteButton = createSmallActionButton("Elimina");
        addCategoryButton.addActionListener(e -> addPersonalCategory());
        addTagButton.addActionListener(e -> addPersonalTag());
        renameButton.addActionListener(e -> renameSelectedClassification(table));
        deleteButton.addActionListener(e -> deleteSelectedClassification(table));
        actions.add(addCategoryButton);
        actions.add(addTagButton);
        actions.add(renameButton);
        actions.add(deleteButton);

        return createTableCard("Categorie e Tag",
                "Classificazioni disponibili per l'utente corrente.", table, actions);
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
        final String newName = JOptionPane.showInputDialog(this, "Nuovo nome:", oldName);
        if (newName == null || newName.trim().isEmpty()) {
            return;
        }
        try {
            if ("Categoria".equals(type)) {
                movimentiController.rinominaCategoriaPersonale(currentUser.getEmail(), id, newName.trim());
            } else {
                movimentiController.rinominaTagPersonale(currentUser.getEmail(), id, newName.trim());
            }
            refreshContent("CARD_CATEGORIES");
        } catch (final SQLException ex) {
            showLoadError("classificazione", ex);
        }
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
        final int confirm = JOptionPane.showConfirmDialog(this,
                "Eliminare l'elemento selezionato?",
                "Conferma eliminazione",
                JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        final String type = String.valueOf(table.getValueAt(selectedRow, 0));
        final long id = Long.parseLong(String.valueOf(table.getValueAt(selectedRow, 1)));
        try {
            if ("Categoria".equals(type)) {
                movimentiController.eliminaCategoriaPersonale(currentUser.getEmail(), id);
            } else {
                movimentiController.eliminaTagPersonale(currentUser.getEmail(), id);
            }
            refreshContent("CARD_CATEGORIES");
        } catch (final SQLException ex) {
            showLoadError("classificazione", ex);
        }
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
