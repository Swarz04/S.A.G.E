package it.unibo.sage.view;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Method;

/**
 * S.A.G.E. Main Entry Point.
 * La GUI resta indipendente da DAO e Controller: per ora gestisce solo le card.
 */
public class MainFrame extends JFrame {

    private static final String VIEW_LOGIN = "VIEW_LOGIN";
    private static final String VIEW_DASHBOARD = "VIEW_DASHBOARD";

    private final CardLayout cardLayout;
    private final JPanel mainContainer;

    public MainFrame() {
        setTitle("S.A.G.E. - Gestione Spese Personali");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1050, 700));
        setSize(1120, 740);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);

        initViews();
        add(mainContainer);
    }

    private void initViews() {
        mainContainer.add(new LoginPanel(this), VIEW_LOGIN);
        mainContainer.add(new DashboardPanel(), VIEW_DASHBOARD);
        cardLayout.show(mainContainer, VIEW_LOGIN);
    }

    public void changeView(String viewName) {
        cardLayout.show(mainContainer, viewName);
    }

    public static void main(String[] args) {
        AppTheme.install();
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}

/**
 * Tema visuale centralizzato.
 * Se FlatLaf e' presente in lib/, viene usato automaticamente.
 */
final class AppTheme {

    static final Color BACKGROUND = new Color(244, 247, 251);
    static final Color SURFACE = Color.WHITE;
    static final Color SURFACE_MUTED = new Color(232, 239, 248);
    static final Color PRIMARY = new Color(24, 119, 242);
    static final Color PRIMARY_DARK = new Color(18, 34, 64);
    static final Color PRIMARY_HOVER = new Color(38, 132, 255);
    static final Color SIDEBAR_BUTTON = new Color(31, 52, 91);
    static final Color SIDEBAR_BUTTON_HOVER = new Color(46, 76, 130);
    static final Color TEXT = new Color(30, 41, 59);
    static final Color TEXT_MUTED = new Color(100, 116, 139);
    static final Color BORDER = new Color(220, 226, 235);
    static final Color INCOME = new Color(17, 122, 101);
    static final Color EXPENSE = new Color(190, 70, 58);

    private AppTheme() {
    }

    static void install() {
        if (!installFlatLaf()) {
            installSystemLookAndFeel();
        }

        UIManager.put("Button.arc", 12);
        UIManager.put("Component.arc", 12);
        UIManager.put("TextComponent.arc", 10);
        UIManager.put("defaultFont", new Font("SansSerif", Font.PLAIN, 14));
    }

    private static boolean installFlatLaf() {
        try {
            Class<?> flatLightLafClass = Class.forName("com.formdev.flatlaf.FlatLightLaf");
            Method setupMethod = flatLightLafClass.getMethod("setup");
            setupMethod.invoke(null);
            return true;
        } catch (ReflectiveOperationException e) {
            return false;
        }
    }

    private static void installSystemLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

/**
 * Componente LoginPanel centrato.
 */
class LoginPanel extends JPanel {

    public LoginPanel(MainFrame parent) {
        setLayout(new GridBagLayout());
        setBackground(AppTheme.BACKGROUND);

        JPanel card = new RoundedPanel(new BorderLayout(0, 22), AppTheme.SURFACE);
        card.setPreferredSize(new Dimension(420, 430));
        card.setBorder(BorderFactory.createEmptyBorder(34, 38, 34, 38));

        card.add(createHeader(), BorderLayout.NORTH);
        card.add(createForm(parent), BorderLayout.CENTER);

        add(card);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JLabel badge = new JLabel("S.A.G.E.");
        badge.setOpaque(true);
        badge.setBackground(AppTheme.SURFACE_MUTED);
        badge.setForeground(AppTheme.PRIMARY_DARK);
        badge.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        badge.setAlignmentX(Component.CENTER_ALIGNMENT);
        badge.setFont(new Font("SansSerif", Font.BOLD, 13));

        JLabel title = new JLabel("Accedi al portafoglio");
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setFont(new Font("SansSerif", Font.BOLD, 28));
        title.setForeground(AppTheme.TEXT);

        JLabel subtitle = new JLabel("Gestione spese per studenti universitari");
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitle.setForeground(AppTheme.TEXT_MUTED);

        header.add(badge);
        header.add(Box.createVerticalStrut(18));
        header.add(title);
        header.add(Box.createVerticalStrut(8));
        header.add(subtitle);

        return header;
    }

    private JPanel createForm(MainFrame parent) {
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField userField = new JTextField();
        JPasswordField passField = new JPasswordField();
        styleTextField(userField);
        styleTextField(passField);

        JButton loginButton = new JButton("Accedi");
        loginButton.setFocusPainted(false);
        loginButton.setBackground(AppTheme.PRIMARY);
        loginButton.setForeground(Color.WHITE);
        loginButton.setOpaque(true);
        loginButton.setBorderPainted(false);
        loginButton.setFont(new Font("SansSerif", Font.BOLD, 15));
        loginButton.setPreferredSize(new Dimension(0, 44));
        loginButton.addMouseListener(new ButtonHoverAdapter(loginButton, AppTheme.PRIMARY, AppTheme.PRIMARY_HOVER));
        loginButton.addActionListener(e -> parent.changeView("VIEW_DASHBOARD"));

        gbc.gridy = 0;
        form.add(createFieldGroup("Username", userField), gbc);
        gbc.gridy = 1;
        gbc.insets = new Insets(16, 0, 0, 0);
        form.add(createFieldGroup("Password", passField), gbc);
        gbc.gridy = 2;
        gbc.insets = new Insets(26, 0, 0, 0);
        form.add(loginButton, gbc);

        return form;
    }

    private JPanel createFieldGroup(String label, JComponent field) {
        JPanel group = new JPanel(new BorderLayout(0, 8));
        group.setOpaque(false);

        JLabel labelComponent = new JLabel(label);
        labelComponent.setForeground(AppTheme.TEXT);
        labelComponent.setFont(new Font("SansSerif", Font.BOLD, 13));

        group.add(labelComponent, BorderLayout.NORTH);
        group.add(field, BorderLayout.CENTER);

        return group;
    }

    private void styleTextField(JTextField field) {
        field.setPreferredSize(new Dimension(0, 42));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(AppTheme.BORDER),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        field.setFont(new Font("SansSerif", Font.PLAIN, 15));
    }
}

/**
 * Schermata principale dopo il login.
 * Il menu laterale cambia le card dell'area centrale senza accedere al database.
 */
class DashboardPanel extends JPanel {

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

/**
 * JPanel con sfondo arrotondato, utile anche senza librerie esterne.
 */
class RoundedPanel extends JPanel {

    private final Color backgroundColor;

    RoundedPanel(LayoutManager layout, Color backgroundColor) {
        super(layout);
        this.backgroundColor = backgroundColor;
        setOpaque(false);
    }

    RoundedPanel(Color backgroundColor) {
        this(new BorderLayout(), backgroundColor);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D graphics2D = (Graphics2D) graphics.create();
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics2D.setColor(backgroundColor);
        graphics2D.fillRoundRect(0, 0, getWidth(), getHeight(), 22, 22);
        graphics2D.dispose();
        super.paintComponent(graphics);
    }
}

/**
 * Piccolo effetto hover per rendere i JButton piu visibili anche senza FlatLaf.
 */
class ButtonHoverAdapter extends java.awt.event.MouseAdapter {

    private final AbstractButton button;
    private final Color normalColor;
    private final Color hoverColor;

    ButtonHoverAdapter(AbstractButton button, Color normalColor, Color hoverColor) {
        this.button = button;
        this.normalColor = normalColor;
        this.hoverColor = hoverColor;
    }

    @Override
    public void mouseEntered(java.awt.event.MouseEvent event) {
        button.setBackground(hoverColor);
    }

    @Override
    public void mouseExited(java.awt.event.MouseEvent event) {
        button.setBackground(normalColor);
    }
}
