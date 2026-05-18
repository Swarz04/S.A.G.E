package it.unibo.sage.view;

import it.unibo.sage.controller.LoginController;
import it.unibo.sage.model.Utente;
import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.Optional;

/**
 * Pannello di login centrato: per ora raccoglie le credenziali e porta alla
 * dashboard demo.
 */
public class LoginPanel extends AppBackgroundPanel {

    private final LoginController loginController;
    private final CardLayout authLayout = new CardLayout();
    private final JPanel authCards = new JPanel(authLayout);

    public LoginPanel(MainFrame parent) {
        super(new GridBagLayout());
        loginController = new LoginController();

        JPanel card = new GlassPanel(new BorderLayout(0, 16));
        card.setPreferredSize(new Dimension(520, 660));
        card.setBorder(BorderFactory.createEmptyBorder(22, 38, 26, 38));

        card.add(createHeader(), BorderLayout.NORTH);
        card.add(createAuthPanel(parent), BorderLayout.CENTER);

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
        badge.setBorder(BorderFactory.createEmptyBorder(7, 14, 7, 14));
        badge.setAlignmentX(Component.CENTER_ALIGNMENT);
        badge.setFont(new Font("SansSerif", Font.BOLD, 13));

        JLabel title = new JLabel("Accedi al portafoglio");
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setFont(new Font("SansSerif", Font.BOLD, 25));
        title.setForeground(AppTheme.TEXT);

        JLabel subtitle = new JLabel("Gestione spese per studenti universitari");
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitle.setForeground(AppTheme.TEXT_MUTED);

        header.add(badge);
        header.add(Box.createVerticalStrut(10));
        header.add(title);
        header.add(Box.createVerticalStrut(5));
        header.add(subtitle);

        return header;
    }

    private JPanel createAuthPanel(final MainFrame parent) {
        JPanel panel = new JPanel(new BorderLayout(0, 18));
        panel.setOpaque(false);

        JPanel switcher = new JPanel(new GridLayout(1, 2, 8, 0));
        switcher.setOpaque(false);
        SoftButton loginTab = createTabButton("Accedi", true);
        SoftButton registerTab = createTabButton("Crea account", false);

        loginTab.addActionListener(e -> {
            loginTab.setBackground(AppTheme.ACCENT);
            loginTab.setForeground(Color.WHITE);
            registerTab.setBackground(Color.WHITE);
            registerTab.setForeground(AppTheme.TEXT);
            authLayout.show(authCards, "LOGIN");
        });
        registerTab.addActionListener(e -> {
            registerTab.setBackground(AppTheme.ACCENT);
            registerTab.setForeground(Color.WHITE);
            loginTab.setBackground(Color.WHITE);
            loginTab.setForeground(AppTheme.TEXT);
            authLayout.show(authCards, "REGISTER");
        });

        switcher.add(loginTab);
        switcher.add(registerTab);

        authCards.setOpaque(false);
        authCards.add(createLoginForm(parent), "LOGIN");
        authCards.add(createRegisterForm(parent), "REGISTER");

        panel.add(switcher, BorderLayout.NORTH);
        panel.add(authCards, BorderLayout.CENTER);
        return panel;
    }

    private SoftButton createTabButton(final String text, final boolean selected) {
        SoftButton button = new SoftButton(text);
        button.setPreferredSize(new Dimension(0, 38));
        button.setArc(14);
        button.setFont(new Font("SansSerif", Font.BOLD, 13));
        button.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        button.setBackground(selected ? AppTheme.ACCENT : Color.WHITE);
        button.setForeground(selected ? Color.WHITE : AppTheme.TEXT);
        return button;
    }

    private JPanel createLoginForm(MainFrame parent) {
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField userField = new JTextField();
        JPasswordField passField = new JPasswordField();
        JLabel statusLabel = new JLabel(" ");
        statusLabel.setForeground(AppTheme.EXPENSE);
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        styleTextField(userField);
        styleTextField(passField);

        SoftButton loginButton = new SoftButton("Accedi");
        loginButton.setBackground(AppTheme.ACCENT);
        loginButton.setForeground(Color.WHITE);
        loginButton.setFont(new Font("SansSerif", Font.BOLD, 15));
        loginButton.setPreferredSize(new Dimension(0, 44));
        loginButton.setArc(16);
        loginButton.addMouseListener(new ButtonHoverAdapter(loginButton, AppTheme.ACCENT, AppTheme.ACCENT_HOVER));
        loginButton.addActionListener(e -> login(parent, userField, passField, loginButton, statusLabel));

        gbc.gridy = 0;
        form.add(createFieldGroup("Email", userField), gbc);
        gbc.gridy = 1;
        gbc.insets = new Insets(16, 0, 0, 0);
        form.add(createFieldGroup("Password", passField), gbc);
        gbc.gridy = 2;
        gbc.insets = new Insets(26, 0, 0, 0);
        form.add(loginButton, gbc);
        gbc.gridy = 3;
        gbc.insets = new Insets(10, 0, 0, 0);
        form.add(statusLabel, gbc);

        return form;
    }

    private JPanel createRegisterForm(final MainFrame parent) {
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField nameField = new JTextField();
        JTextField surnameField = new JTextField();
        JTextField emailField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JPasswordField confirmPasswordField = new JPasswordField();
        JLabel statusLabel = new JLabel(" ");
        statusLabel.setForeground(AppTheme.EXPENSE);
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));

        styleTextField(nameField);
        styleTextField(surnameField);
        styleTextField(emailField);
        styleTextField(passwordField);
        styleTextField(confirmPasswordField);

        SoftButton registerButton = new SoftButton("Crea account");
        registerButton.setBackground(AppTheme.ACCENT);
        registerButton.setForeground(Color.WHITE);
        registerButton.setFont(new Font("SansSerif", Font.BOLD, 15));
        registerButton.setPreferredSize(new Dimension(0, 44));
        registerButton.setArc(16);
        registerButton.addMouseListener(new ButtonHoverAdapter(registerButton,
                AppTheme.ACCENT, AppTheme.ACCENT_HOVER));
        registerButton.addActionListener(e -> register(parent, nameField, surnameField,
                emailField, passwordField, confirmPasswordField, registerButton, statusLabel));

        gbc.gridy = 0;
        form.add(createFieldGroup("Nome", nameField), gbc);
        gbc.gridy = 1;
        gbc.insets = new Insets(9, 0, 0, 0);
        form.add(createFieldGroup("Cognome", surnameField), gbc);
        gbc.gridy = 2;
        form.add(createFieldGroup("Email", emailField), gbc);
        gbc.gridy = 3;
        form.add(createFieldGroup("Password", passwordField), gbc);
        gbc.gridy = 4;
        form.add(createFieldGroup("Conferma password", confirmPasswordField), gbc);
        gbc.gridy = 5;
        gbc.insets = new Insets(16, 0, 0, 0);
        form.add(registerButton, gbc);
        gbc.gridy = 6;
        gbc.insets = new Insets(10, 0, 0, 0);
        form.add(statusLabel, gbc);

        return form;
    }

    private void login(final MainFrame parent, final JTextField userField,
            final JPasswordField passField, final JButton loginButton,
            final JLabel statusLabel) {
        final String email = userField.getText().trim();
        final char[] password = passField.getPassword();
        if (email.isEmpty() || password.length == 0) {
            statusLabel.setText("Inserisci email e password.");
            Arrays.fill(password, '\0');
            return;
        }

        loginButton.setEnabled(false);
        statusLabel.setForeground(AppTheme.TEXT_MUTED);
        statusLabel.setText("Accesso in corso...");

        new SwingWorker<Optional<Utente>, Void>() {
            @Override
            protected Optional<Utente> doInBackground() throws Exception {
                try {
                    return loginController.login(email, new String(password));
                } finally {
                    Arrays.fill(password, '\0');
                }
            }

            @Override
            protected void done() {
                loginButton.setEnabled(true);
                try {
                    Optional<Utente> user = get();
                    if (user.isPresent()) {
                        parent.loginSucceeded(user.get());
                    } else {
                        statusLabel.setForeground(AppTheme.EXPENSE);
                        statusLabel.setText("Credenziali non valide.");
                    }
                } catch (Exception ex) {
                    statusLabel.setForeground(AppTheme.EXPENSE);
                    statusLabel.setText(buildLoginErrorMessage(ex));
                }
            }
        }.execute();
    }

    private String buildLoginErrorMessage(final Exception ex) {
        Throwable cause = ex;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }
        final String message = cause.getMessage();
        if (message == null || message.isBlank()) {
            return "Errore login non previsto.";
        }
        if (message.contains("No suitable driver") || message.contains("Driver MySQL JDBC")) {
            return "Driver MySQL mancante nel classpath: usa Run S.A.G.E.";
        }
        if (message.contains("Access denied")) {
            return "Accesso MySQL negato: controlla utente/password DB.";
        }
        if (message.contains("Unknown database")) {
            return "Database S.A.G.E. non creato: esegui gli script SQL.";
        }
        return "Errore login: " + message;
    }

    private void register(final MainFrame parent, final JTextField nameField,
            final JTextField surnameField, final JTextField emailField,
            final JPasswordField passwordField, final JPasswordField confirmPasswordField,
            final JButton registerButton, final JLabel statusLabel) {
        final String name = nameField.getText().trim();
        final String surname = surnameField.getText().trim();
        final String email = emailField.getText().trim();
        final char[] password = passwordField.getPassword();
        final char[] confirmPassword = confirmPasswordField.getPassword();

        if (name.isEmpty() || surname.isEmpty() || email.isEmpty()
                || password.length == 0 || confirmPassword.length == 0) {
            statusLabel.setText("Compila tutti i campi.");
            clearPasswords(password, confirmPassword);
            return;
        }
        if (!email.contains("@") || !email.contains(".")) {
            statusLabel.setText("Inserisci un'email valida.");
            clearPasswords(password, confirmPassword);
            return;
        }
        if (password.length < 6) {
            statusLabel.setText("La password deve avere almeno 6 caratteri.");
            clearPasswords(password, confirmPassword);
            return;
        }
        if (!Arrays.equals(password, confirmPassword)) {
            statusLabel.setText("Le password non coincidono.");
            clearPasswords(password, confirmPassword);
            return;
        }

        registerButton.setEnabled(false);
        statusLabel.setForeground(AppTheme.TEXT_MUTED);
        statusLabel.setText("Creazione account...");

        new SwingWorker<Optional<Utente>, Void>() {
            @Override
            protected Optional<Utente> doInBackground() throws Exception {
                try {
                    final String passwordText = new String(password);
                    loginController.registraUtente(email, passwordText, name, surname);
                    return loginController.login(email, passwordText);
                } finally {
                    clearPasswords(password, confirmPassword);
                }
            }

            @Override
            protected void done() {
                registerButton.setEnabled(true);
                try {
                    Optional<Utente> user = get();
                    if (user.isPresent()) {
                        parent.loginSucceeded(user.get());
                    } else {
                        statusLabel.setForeground(AppTheme.EXPENSE);
                        statusLabel.setText("Account creato, ma login non riuscito.");
                    }
                } catch (Exception ex) {
                    statusLabel.setForeground(AppTheme.EXPENSE);
                    statusLabel.setText(buildRegisterErrorMessage(ex));
                }
            }
        }.execute();
    }

    private String buildRegisterErrorMessage(final Exception ex) {
        Throwable cause = ex;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }
        final String message = cause.getMessage();
        if (message != null && message.contains("Duplicate")) {
            return "Email gia' registrata.";
        }
        if (message == null || message.isBlank()) {
            return "Registrazione non riuscita.";
        }
        return "Errore registrazione: " + message;
    }

    private void clearPasswords(final char[]... passwords) {
        for (char[] password : passwords) {
            Arrays.fill(password, '\0');
        }
    }

    private JPanel createFieldGroup(String label, JComponent field) {
        JPanel group = new JPanel(new BorderLayout(0, 8));
        group.setOpaque(false);
        group.setBorder(BorderFactory.createEmptyBorder(1, 0, 1, 0));
        group.setPreferredSize(new Dimension(420, 60));
        group.setMinimumSize(new Dimension(420, 60));

        JLabel labelComponent = new JLabel(label);
        labelComponent.setForeground(AppTheme.TEXT);
        labelComponent.setFont(new Font("SansSerif", Font.BOLD, 13));

        group.add(labelComponent, BorderLayout.NORTH);
        group.add(field, BorderLayout.CENTER);

        return group;
    }

    private void styleTextField(JTextField field) {
        field.setPreferredSize(new Dimension(420, 36));
        field.setMinimumSize(new Dimension(420, 36));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(AppTheme.BORDER),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        field.setFont(new Font("SansSerif", Font.PLAIN, 15));
    }
}
