package it.unibo.sage.view;

import javax.swing.*;
import java.awt.*;

/**
 * Schermata principale dopo il login.
 * Il menu laterale cambia le card dell'area centrale senza accedere al database.
 */
public class DashboardPanel extends JPanel {

    private final CardLayout contentLayout;
    private final JPanel contentPanel;

    public DashboardPanel() {
        setLayout(new BorderLayout());
        setBackground(AppTheme.BACKGROUND);

        contentLayout = new CardLayout();
        contentPanel = new JPanel(contentLayout);
        contentPanel.setBackground(AppTheme.BACKGROUND);

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

        JButton newTransactionButton = new JButton("+ Nuova transazione");
        newTransactionButton.setFocusPainted(false);
        newTransactionButton.setBackground(AppTheme.PRIMARY);
        newTransactionButton.setForeground(Color.WHITE);
        newTransactionButton.setOpaque(true);
        newTransactionButton.setBorderPainted(false);
        newTransactionButton.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        newTransactionButton.setFont(new Font("SansSerif", Font.BOLD, 13));
        newTransactionButton.addMouseListener(new ButtonHoverAdapter(
            newTransactionButton,
            AppTheme.PRIMARY,
            AppTheme.PRIMARY_HOVER
        ));

        headerPanel.add(titlePanel, BorderLayout.WEST);
        headerPanel.add(newTransactionButton, BorderLayout.EAST);

        return headerPanel;
    }

    private JPanel createMenuPanel() {
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setPreferredSize(new Dimension(245, 0));
        menuPanel.setBorder(BorderFactory.createEmptyBorder(24, 18, 24, 18));
        menuPanel.setBackground(AppTheme.PRIMARY_DARK);

        JLabel appName = new JLabel("S.A.G.E.");
        appName.setForeground(Color.WHITE);
        appName.setFont(new Font("SansSerif", Font.BOLD, 25));
        appName.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel appCaption = new JLabel("Personal finance");
        appCaption.setForeground(new Color(190, 209, 242));
        appCaption.setAlignmentX(Component.LEFT_ALIGNMENT);

        menuPanel.add(appName);
        menuPanel.add(Box.createVerticalStrut(4));
        menuPanel.add(appCaption);
        menuPanel.add(Box.createVerticalStrut(30));
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
        JPanel userBox = new JPanel(new BorderLayout(10, 0));
        userBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));
        userBox.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        userBox.setBackground(new Color(35, 58, 101));

        JLabel avatar = new JLabel("U", SwingConstants.CENTER);
        avatar.setOpaque(true);
        avatar.setPreferredSize(new Dimension(34, 34));
        avatar.setBackground(Color.WHITE);
        avatar.setForeground(AppTheme.PRIMARY_DARK);
        avatar.setFont(new Font("SansSerif", Font.BOLD, 15));

        JLabel name = new JLabel("Utente demo");
        name.setForeground(Color.WHITE);
        name.setFont(new Font("SansSerif", Font.BOLD, 13));

        userBox.add(avatar, BorderLayout.WEST);
        userBox.add(name, BorderLayout.CENTER);

        return userBox;
    }

    private JButton createMenuButton(String text, String cardName) {
        JButton button = new JButton(text);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setBackground(AppTheme.SIDEBAR_BUTTON);
        button.setForeground(Color.WHITE);
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        button.addMouseListener(new ButtonHoverAdapter(
            button,
            AppTheme.SIDEBAR_BUTTON,
            AppTheme.SIDEBAR_BUTTON_HOVER
        ));
        button.addActionListener(e -> contentLayout.show(contentPanel, cardName));
        return button;
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
        contentPanel.add(createPlaceholderCard(
            "Documenti Digitali",
            "Path di scontrini e documenti associati alle spese."
        ), "CARD_DOCUMENTS");
    }

    private JPanel createOverviewCard() {
        JPanel panel = new JPanel(new BorderLayout(0, 18));
        panel.setOpaque(false);

        JPanel metricsPanel = new JPanel(new GridLayout(1, 4, 16, 16));
        metricsPanel.setOpaque(false);
        metricsPanel.add(createMetricBox("Saldo attuale", "0,00 euro", AppTheme.PRIMARY));
        metricsPanel.add(createMetricBox("Entrate mese", "0,00 euro", AppTheme.INCOME));
        metricsPanel.add(createMetricBox("Spese mese", "0,00 euro", AppTheme.EXPENSE));
        metricsPanel.add(createMetricBox("Budget usato", "0%", new Color(120, 85, 170)));

        JPanel lowerPanel = new JPanel(new GridLayout(1, 2, 16, 16));
        lowerPanel.setOpaque(false);
        lowerPanel.add(createPlaceholderBox("Ultime transazioni", "Nessuna transazione registrata."));
        lowerPanel.add(createPlaceholderBox("Budget del mese", "Nessun budget configurato."));

        panel.add(metricsPanel, BorderLayout.NORTH);
        panel.add(lowerPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createMetricBox(String title, String value, Color accent) {
        JPanel panel = new RoundedPanel(new BorderLayout(0, 14), AppTheme.SURFACE);
        panel.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(AppTheme.TEXT_MUTED);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 13));

        JLabel valueLabel = new JLabel(value);
        valueLabel.setForeground(AppTheme.TEXT);
        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 24));

        JPanel accentBar = new JPanel();
        accentBar.setPreferredSize(new Dimension(0, 4));
        accentBar.setBackground(accent);

        panel.add(accentBar, BorderLayout.NORTH);
        panel.add(titleLabel, BorderLayout.CENTER);
        panel.add(valueLabel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createPlaceholderBox(String title, String description) {
        JPanel panel = new RoundedPanel(new BorderLayout(0, 12), AppTheme.SURFACE);
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
        JPanel panel = new RoundedPanel(new GridBagLayout(), AppTheme.SURFACE);
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