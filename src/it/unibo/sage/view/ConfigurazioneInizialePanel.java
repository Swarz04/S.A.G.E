package it.unibo.sage.view;

import it.unibo.sage.controller.ConfigurazioneInizialeController;
import it.unibo.sage.model.Utente;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

public class ConfigurazioneInizialePanel extends AppBackgroundPanel {

    private final ConfigurazioneInizialeController configurazioneController =
            new ConfigurazioneInizialeController();
    private final JComboBox<String> focusSpeseCombo = new JComboBox<>(
            new String[] {"universita", "casa", "trasporti", "lavoro", "vita quotidiana"});
    private final JComboBox<String> fonteEntrataCombo = new JComboBox<>(
            new String[] {"nessuna", "borsa di studio", "stipendio",
                    "lavoro occasionale", "aiuto famiglia"});
    private final JComboBox<String> budgetCombo = new JComboBox<>(
            new String[] {"nessuno", "150", "300", "500", "800"});
    private final JComboBox<String> tagCombo = new JComboBox<>(
            new String[] {"base", "studio", "lavoro", "casa e trasporti"});

    public ConfigurazioneInizialePanel(final MainFrame parent, final Utente user) {
        super(new GridBagLayout());

        final JPanel card = new GlassPanel(new BorderLayout(0, 20));
        card.setPreferredSize(new Dimension(620, 620));
        card.setBorder(BorderFactory.createEmptyBorder(28, 42, 30, 42));

        card.add(createHeader(user), BorderLayout.NORTH);
        card.add(createForm(), BorderLayout.CENTER);
        card.add(createFooter(parent, user), BorderLayout.SOUTH);

        add(card);
    }

    private JPanel createHeader(final Utente user) {
        final JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        final JLabel badge = new JLabel("S.A.G.E.");
        badge.setOpaque(true);
        badge.setBackground(AppTheme.BADGE_BACKGROUND);
        badge.setForeground(AppTheme.BADGE_TEXT);
        badge.setBorder(BorderFactory.createEmptyBorder(7, 14, 7, 14));
        badge.setAlignmentX(Component.CENTER_ALIGNMENT);
        badge.setFont(new Font("SansSerif", Font.BOLD, 13));

        final JLabel title = new JLabel("Prime impostazioni");
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setFont(new Font("SansSerif", Font.BOLD, 25));
        title.setForeground(AppTheme.TEXT);

        final JLabel subtitle = new JLabel("Scegli cosa creare nel portafoglio di "
                + user.getNome() + ".");
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitle.setForeground(AppTheme.TEXT_MUTED);

        header.add(badge);
        header.add(Box.createVerticalStrut(10));
        header.add(title);
        header.add(Box.createVerticalStrut(5));
        header.add(subtitle);
        return header;
    }

    private JPanel createForm() {
        final JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);

        styleCombo(focusSpeseCombo);
        styleCombo(fonteEntrataCombo);
        styleCombo(budgetCombo);
        styleCombo(tagCombo);

        final GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 14, 0);

        gbc.gridy = 0;
        form.add(createQuestion("Voglio tracciare soprattutto spese per:",
                focusSpeseCombo), gbc);
        gbc.gridy = 1;
        form.add(createQuestion("La mia entrata principale sara':",
                fonteEntrataCombo), gbc);
        gbc.gridy = 2;
        form.add(createQuestion("Il budget mensile iniziale sara':",
                budgetCombo), gbc);
        gbc.gridy = 3;
        form.add(createQuestion("Voglio usare tag iniziali:",
                tagCombo), gbc);

        return form;
    }

    private JPanel createFooter(final MainFrame parent, final Utente user) {
        final JPanel footer = new JPanel(new BorderLayout(12, 0));
        footer.setOpaque(false);

        final JLabel statusLabel = new JLabel(" ");
        statusLabel.setForeground(AppTheme.EXPENSE);
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));

        final JPanel actions = new JPanel();
        actions.setOpaque(false);
        actions.setLayout(new BoxLayout(actions, BoxLayout.X_AXIS));

        final SoftButton skipButton = createButton("Salta", AppTheme.SIDEBAR_BUTTON,
                Color.WHITE);
        final SoftButton completeButton = createButton("Completa",
                AppTheme.ACCENT, Color.WHITE);
        completeButton.addMouseListener(new ButtonHoverAdapter(completeButton,
                AppTheme.ACCENT, AppTheme.ACCENT_HOVER));

        skipButton.addActionListener(e -> parent.loginSucceeded(user));
        completeButton.addActionListener(e -> completeConfiguration(parent, user,
                completeButton, skipButton, statusLabel));

        actions.add(skipButton);
        actions.add(Box.createHorizontalStrut(10));
        actions.add(completeButton);

        footer.add(statusLabel, BorderLayout.CENTER);
        footer.add(actions, BorderLayout.EAST);
        return footer;
    }

    private void completeConfiguration(final MainFrame parent, final Utente user,
            final SoftButton completeButton, final SoftButton skipButton,
            final JLabel statusLabel) {
        completeButton.setEnabled(false);
        skipButton.setEnabled(false);
        statusLabel.setForeground(AppTheme.TEXT_MUTED);
        statusLabel.setText("Salvataggio configurazione...");

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                configurazioneController.completaConfigurazione(
                        user.getEmail(),
                        selected(focusSpeseCombo),
                        selected(fonteEntrataCombo),
                        selectedBudget(),
                        selected(tagCombo));
                return null;
            }

            @Override
            protected void done() {
                completeButton.setEnabled(true);
                skipButton.setEnabled(true);
                try {
                    get();
                    parent.loginSucceeded(user);
                } catch (final Exception ex) {
                    statusLabel.setForeground(AppTheme.EXPENSE);
                    statusLabel.setText("Configurazione non salvata: " + rootMessage(ex));
                }
            }
        }.execute();
    }

    private String selected(final JComboBox<String> combo) {
        return String.valueOf(combo.getSelectedItem());
    }

    private BigDecimal selectedBudget() {
        final String value = selected(budgetCombo);
        if ("nessuno".equalsIgnoreCase(value)) {
            return null;
        }
        return new BigDecimal(value);
    }

    private String rootMessage(final Exception ex) {
        Throwable cause = ex;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }
        return cause.getMessage() == null ? "errore non previsto" : cause.getMessage();
    }

    private JPanel createQuestion(final String text, final JComponent field) {
        final JPanel group = new JPanel(new BorderLayout(0, 8));
        group.setOpaque(false);
        group.setBorder(BorderFactory.createEmptyBorder(1, 0, 1, 0));
        group.setPreferredSize(new Dimension(520, 66));
        group.setMinimumSize(new Dimension(520, 66));

        final JLabel label = new JLabel(text);
        label.setForeground(AppTheme.TEXT);
        label.setFont(new Font("SansSerif", Font.BOLD, 13));

        group.add(label, BorderLayout.NORTH);
        group.add(field, BorderLayout.CENTER);
        return group;
    }

    private void styleCombo(final JComboBox<String> combo) {
        combo.setPreferredSize(new Dimension(520, 38));
        combo.setMinimumSize(new Dimension(520, 38));
        combo.setFont(new Font("SansSerif", Font.PLAIN, 15));
        combo.setBackground(Color.WHITE);
    }

    private SoftButton createButton(final String text, final Color background,
            final Color foreground) {
        final SoftButton button = new SoftButton(text);
        button.setBackground(background);
        button.setForeground(foreground);
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        button.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        button.setPreferredSize(new Dimension(150, 42));
        button.setArc(16);
        return button;
    }
}
