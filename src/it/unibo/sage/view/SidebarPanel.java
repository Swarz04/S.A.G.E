package it.unibo.sage.view;

import javax.swing.*;
import java.awt.*;

/**
 * Barra laterale della dashboard, disegnata con un gradiente scuro per
 * separare bene la navigazione dal contenuto principale.
 */
public class SidebarPanel extends JPanel {

    public SidebarPanel() {
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D graphics2D = (Graphics2D) graphics.create();
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        GradientPaint gradient = new GradientPaint(
            0, 0, AppTheme.SIDEBAR_TOP,
            width, height, AppTheme.SIDEBAR_BOTTOM
        );
        graphics2D.setPaint(gradient);
        graphics2D.fillRect(0, 0, width, height);

        graphics2D.setColor(AppTheme.SIDEBAR_ORB_PRIMARY);
        graphics2D.fillOval(-70, 70, 170, 170);

        graphics2D.setColor(AppTheme.SIDEBAR_ORB_ACCENT);
        graphics2D.fillOval(120, height - 180, 190, 190);

        graphics2D.dispose();
        super.paintComponent(graphics);
    }
}
