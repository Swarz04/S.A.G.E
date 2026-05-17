package it.unibo.sage.view;

import javax.swing.*;
import java.awt.*;

/**
 * Finestra principale dell'applicazione. In questa fase tiene insieme le viste
 * Swing senza collegarsi direttamente a DAO e controller.
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
