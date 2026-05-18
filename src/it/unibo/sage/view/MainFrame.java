package it.unibo.sage.view;

import it.unibo.sage.model.Ruolo;
import it.unibo.sage.model.Utente;
import javax.swing.*;
import java.awt.*;

/**
 * Finestra principale dell'applicazione. In questa fase tiene insieme le viste
 * Swing senza collegarsi direttamente a DAO e controller.
 */
public class MainFrame extends JFrame {

    private static final String VIEW_LOGIN = "VIEW_LOGIN";
    private static final String VIEW_DASHBOARD = "VIEW_DASHBOARD";
    private static final String VIEW_CONFIGURAZIONE_INIZIALE = "VIEW_CONFIGURAZIONE_INIZIALE";

    private final CardLayout cardLayout;
    private final JPanel mainContainer;
    private Utente currentUser;
    private JComponent dashboardPanel;

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
        cardLayout.show(mainContainer, VIEW_LOGIN);
    }

    public void loginSucceeded(final Utente user) {
        currentUser = user;
        if (dashboardPanel != null) {
            mainContainer.remove(dashboardPanel);
        }
        dashboardPanel = currentUser.getRuolo() == Ruolo.ADMIN
                ? new AdminDashboardPanel(currentUser)
                : new DashboardPanel(currentUser);
        mainContainer.add(dashboardPanel, VIEW_DASHBOARD);
        mainContainer.revalidate();
        mainContainer.repaint();
        cardLayout.show(mainContainer, VIEW_DASHBOARD);
    }

    public void showConfigurazioneIniziale(final Utente user) {
        final ConfigurazioneInizialePanel configurazionePanel =
                new ConfigurazioneInizialePanel(this, user);
        mainContainer.add(configurazionePanel, VIEW_CONFIGURAZIONE_INIZIALE);
        mainContainer.revalidate();
        mainContainer.repaint();
        cardLayout.show(mainContainer, VIEW_CONFIGURAZIONE_INIZIALE);
    }

    public void changeView(String viewName) {
        cardLayout.show(mainContainer, viewName);
    }

    public static void main(String[] args) {
        AppTheme.install();
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}
