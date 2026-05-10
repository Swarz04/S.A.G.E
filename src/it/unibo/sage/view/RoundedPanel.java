package it.unibo.sage.view;

import javax.swing.*;
import java.awt.*;

/**
 * JPanel con sfondo arrotondato, utile anche senza librerie esterne.
 */
public class RoundedPanel extends JPanel {

    private final Color backgroundColor;

    public RoundedPanel(LayoutManager layout, Color backgroundColor) {
        super(layout);
        this.backgroundColor = backgroundColor;
        setOpaque(false);
    }

    public RoundedPanel(Color backgroundColor) {
        this(new BorderLayout(), backgroundColor);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D graphics2D = (Graphics2D) graphics.create();
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics2D.setColor(backgroundColor);
        graphics2D.fillRoundRect(0, 0, getWidth(), getHeight(), 22, 22);
        graphics2D.dispose();
        super.paintComponent(graphics);
    }
}