package it.unibo.sage.view.admin;

import it.unibo.sage.model.Utente;
import it.unibo.sage.service.AdminService;
import it.unibo.sage.service.AdminService.TableData;
import it.unibo.sage.view.components.AppBackgroundPanel;
import it.unibo.sage.view.components.GlassPanel;
import it.unibo.sage.view.theme.AppTheme;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.sql.SQLException;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class AdminDashboardPanel extends AppBackgroundPanel {

    private final AdminService adminService = new AdminService();
    private final Utente currentUser;

    public AdminDashboardPanel(final Utente currentUser) {
        super(new BorderLayout());
        this.currentUser = currentUser;
        setBorder(BorderFactory.createEmptyBorder(26, 30, 26, 30));
        add(createHeader(), BorderLayout.NORTH);
        add(createTabs(), BorderLayout.CENTER);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Area Amministratore");
        title.setFont(new Font("SansSerif", Font.BOLD, 30));
        title.setForeground(AppTheme.TEXT);

        JLabel subtitle = new JLabel("Analisi aggregate e controllo dello stato dei budget");
        subtitle.setForeground(AppTheme.TEXT_MUTED);

        JLabel user = new JLabel(currentUser.getNome() + " " + currentUser.getCognome()
                + " - " + currentUser.getEmail());
        user.setForeground(AppTheme.TEXT_MUTED);

        header.add(title);
        header.add(Box.createVerticalStrut(8));
        header.add(subtitle);
        header.add(Box.createVerticalStrut(4));
        header.add(user);
        header.add(Box.createVerticalStrut(18));
        return header;
    }

    private JTabbedPane createTabs() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Statistiche mensili", createTablePanel(loadStats()));
        tabs.addTab("Stato budget", createTablePanel(loadBudgetState()));
        return tabs;
    }

    private JPanel createTablePanel(final TableData data) {
        JPanel panel = new GlassPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        DefaultTableModel model = new DefaultTableModel(
                data.getColumns().toArray(new String[0]), 0);
        for (Object[] row : data.getRows()) {
            model.addRow(row);
        }

        JTable table = new JTable(model);
        table.setFillsViewportHeight(true);
        table.setRowHeight(28);
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(218, 226, 236)));
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private TableData loadStats() {
        try {
            return adminService.caricaStatisticheAggregate();
        } catch (final SQLException ex) {
            showLoadError(ex);
            return new TableData(java.util.List.of(), java.util.List.of());
        }
    }

    private TableData loadBudgetState() {
        try {
            return adminService.caricaStatoBudget();
        } catch (final SQLException ex) {
            showLoadError(ex);
            return new TableData(java.util.List.of(), java.util.List.of());
        }
    }

    private void showLoadError(final SQLException ex) {
        JOptionPane.showMessageDialog(this,
                "Errore durante il caricamento dei dati admin: " + ex.getMessage(),
                "Errore database",
                JOptionPane.ERROR_MESSAGE);
    }
}
