package it.unibo.sage.view.components;

import it.unibo.sage.view.theme.AppTheme;
import javax.swing.*;
import java.awt.*;

/**
 * JButton personalizzato: mi serve per avere bottoni arrotondati e uniformi
 * anche se FlatLaf non viene caricato.
 */
public class SoftButton extends JButton {

    private int arc = 18;

    public SoftButton(String text) {
        super(text);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    public void setArc(int arc) {
        this.arc = arc;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D graphics2D = (Graphics2D) graphics.create();
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color baseColor = getBackground();
        if (getModel().isPressed()) {
            baseColor = baseColor.darker();
        }

        graphics2D.setColor(baseColor);
        graphics2D.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);

        graphics2D.setColor(AppTheme.BUTTON_BORDER);
        graphics2D.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);

        graphics2D.dispose();
        super.paintComponent(graphics);
    }
}
