package it.unibo.sage.view;

import javax.swing.*;
import java.awt.*;

/**
 * Sfondo morbido per far risaltare le superfici glass senza immagini esterne.
 */
public class AppBackgroundPanel extends JPanel {

    public AppBackgroundPanel() {
        super();
        setOpaque(false);
    }

    public AppBackgroundPanel(LayoutManager layout) {
        super(layout);
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D graphics2D = (Graphics2D) graphics.create();
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        GradientPaint baseGradient = new GradientPaint(
            0, 0, AppTheme.BACKGROUND_TOP,
            width, height, AppTheme.BACKGROUND_BOTTOM
        );
        graphics2D.setPaint(baseGradient);
        graphics2D.fillRect(0, 0, width, height);

        graphics2D.setPaint(new GradientPaint(
            0, 0, AppTheme.BACKGROUND_WASH,
            width, height / 2, AppTheme.BACKGROUND_WASH_FADE
        ));
        graphics2D.fillRect(0, 0, width, Math.max(1, height / 2));

        graphics2D.setColor(AppTheme.BACKGROUND_COOL_GLOW);
        graphics2D.fillRoundRect(width / 3, 28, Math.max(240, width / 2), 118, 36, 36);

        graphics2D.setColor(AppTheme.BACKGROUND_WARM_GLOW);
        graphics2D.fillRoundRect(Math.max(0, width - 340), Math.max(0, height - 170), 280, 105, 34, 34);

        graphics2D.dispose();
        super.paintComponent(graphics);
    }
}
