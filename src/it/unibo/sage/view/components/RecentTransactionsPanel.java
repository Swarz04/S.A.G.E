package it.unibo.sage.view.components;

import it.unibo.sage.view.theme.AppTheme;
import java.awt.BorderLayout;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class RecentTransactionsPanel extends GlassPanel {

    public RecentTransactionsPanel() {
        super(new BorderLayout(0, 12));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        final JLabel titleLabel = new JLabel("Ultime transazioni");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        titleLabel.setForeground(AppTheme.TEXT);

        final JLabel descriptionLabel = new JLabel("Nessuna transazione registrata.");
        descriptionLabel.setForeground(AppTheme.TEXT_MUTED);

        add(titleLabel, BorderLayout.NORTH);
        add(descriptionLabel, BorderLayout.CENTER);
    }
}
