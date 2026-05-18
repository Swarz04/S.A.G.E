package it.unibo.sage.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class SummaryPanel extends JPanel {

    public SummaryPanel() {
        super(new GridLayout(1, 4, 16, 16));
        setOpaque(false);
        add(createMetricBox("Saldo attuale", "0,00 euro", AppTheme.PRIMARY));
        add(createMetricBox("Entrate mese", "0,00 euro", AppTheme.INCOME));
        add(createMetricBox("Spese mese", "0,00 euro", AppTheme.EXPENSE));
        add(createMetricBox("Budget usato", "0%", AppTheme.BUDGET));
    }

    private JPanel createMetricBox(final String title, final String value,
            final Color accent) {
        final JPanel panel = new GlassPanel(new BorderLayout(0, 14));
        panel.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        final JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(AppTheme.TEXT_MUTED);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 13));

        final JLabel valueLabel = new JLabel(value);
        valueLabel.setForeground(AppTheme.TEXT);
        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 24));

        final JPanel accentBar = new JPanel();
        accentBar.setPreferredSize(new Dimension(0, 5));
        accentBar.setBackground(accent);
        accentBar.setOpaque(true);

        panel.add(accentBar, BorderLayout.NORTH);
        panel.add(titleLabel, BorderLayout.CENTER);
        panel.add(valueLabel, BorderLayout.SOUTH);

        return panel;
    }
}
