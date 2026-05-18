package it.unibo.sage.view;

import it.unibo.sage.model.Utente;
import javax.swing.*;
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
        contentPanel.add(createOverviewCard(), CARD_OVERVIEW);
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
        JPanel panel = new JPanel(new BorderLayout(0, 18));
        panel.setOpaque(false);

        JPanel lowerPanel = new JPanel(new GridLayout(1, 2, 16, 16));
        lowerPanel.setOpaque(false);
        lowerPanel.add(new RecentTransactionsPanel());
        lowerPanel.add(createPlaceholderBox("Budget del mese", "Nessun budget configurato."));

        panel.add(new SummaryPanel(), BorderLayout.NORTH);
        panel.add(lowerPanel, BorderLayout.CENTER);

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
}
