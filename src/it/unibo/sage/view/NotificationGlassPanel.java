package it.unibo.sage.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class NotificationGlassPanel extends GlassPanel {

    private final JPanel accentBar;
    private final JLabel messageLabel;

    public NotificationGlassPanel() {
        super(new BorderLayout(12, 0));
        setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 16));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 54));
        setVisible(false);

        accentBar = new JPanel();
        accentBar.setOpaque(true);
        accentBar.setPreferredSize(new Dimension(5, 0));

        messageLabel = new JLabel();
        messageLabel.setFont(new Font("SansSerif", Font.BOLD, 13));

        add(accentBar, BorderLayout.WEST);
        add(messageLabel, BorderLayout.CENTER);
    }

    public void showSuccess(final String message) {
        showMessage(message, AppTheme.ACCENT, AppTheme.TEXT);
    }

    public void showError(final String message) {
        showMessage(message, AppTheme.EXPENSE, AppTheme.EXPENSE);
    }

    public void clear() {
        messageLabel.setText("");
        setVisible(false);
        revalidate();
        repaint();
    }

    private void showMessage(final String message, final Color accent,
            final Color textColor) {
        accentBar.setBackground(accent);
        messageLabel.setForeground(textColor);
        messageLabel.setText(message);
        setVisible(true);
        revalidate();
        repaint();
    }
}
