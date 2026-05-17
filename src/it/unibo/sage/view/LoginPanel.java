package it.unibo.sage.view;

import javax.swing.*;
import java.awt.*;

/**
 * Pannello di login centrato: per ora raccoglie le credenziali e porta alla
 * dashboard demo.
 */
public class LoginPanel extends AppBackgroundPanel {

    public LoginPanel(MainFrame parent) {
        super(new GridBagLayout());

        JPanel card = new GlassPanel(new BorderLayout(0, 22));
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
        badge.setBackground(AppTheme.BADGE_BACKGROUND);
        badge.setForeground(AppTheme.BADGE_TEXT);
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

        SoftButton loginButton = new SoftButton("Accedi");
        loginButton.setBackground(AppTheme.ACCENT);
        loginButton.setForeground(Color.WHITE);
        loginButton.setFont(new Font("SansSerif", Font.BOLD, 15));
        loginButton.setPreferredSize(new Dimension(0, 44));
        loginButton.setArc(16);
        loginButton.addMouseListener(new ButtonHoverAdapter(loginButton, AppTheme.ACCENT, AppTheme.ACCENT_HOVER));
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
