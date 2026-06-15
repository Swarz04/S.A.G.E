package it.unibo.sage.view.components;

import it.unibo.sage.view.theme.AppTheme;
import javax.swing.*;
import java.awt.*;

/**
 * Pannello "glass" usato come contenitore leggero: bordo chiaro, trasparenza e
 * una piccola ombra per staccarlo dallo sfondo.
 */
public class GlassPanel extends JPanel {

    private static final int ARC = 26;
    private static final int SHADOW_OFFSET = 5;
    private static final int SHADOW_SIZE = 10;

    private final Color topColor;
    private final Color bottomColor;
    private final boolean highlightVisible;

    public GlassPanel(LayoutManager layout) {
        this(layout, AppTheme.GLASS_TOP, AppTheme.GLASS_BOTTOM);
    }

    public GlassPanel(LayoutManager layout, Color topColor, Color bottomColor) {
        this(layout, topColor, bottomColor, true);
    }

    public GlassPanel(LayoutManager layout, Color topColor, Color bottomColor,
            boolean highlightVisible) {
        super(layout);
        this.topColor = topColor;
        this.bottomColor = bottomColor;
        this.highlightVisible = highlightVisible;
        setOpaque(false);
    }

    public GlassPanel() {
        this(new BorderLayout());
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D graphics2D = (Graphics2D) graphics.create();
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int x = SHADOW_SIZE / 2;
        int y = SHADOW_SIZE / 2;
        int width = getWidth() - SHADOW_SIZE;
        int height = getHeight() - SHADOW_SIZE;

        graphics2D.setColor(AppTheme.GLASS_SHADOW);
        graphics2D.fillRoundRect(x + 1, y + SHADOW_OFFSET, width, height, ARC, ARC);

        GradientPaint glassGradient = new GradientPaint(x, y, topColor, x, y + height, bottomColor);
        graphics2D.setPaint(glassGradient);
        graphics2D.fillRoundRect(x, y, width, height, ARC, ARC);

        graphics2D.setColor(AppTheme.GLASS_BORDER);
        graphics2D.drawRoundRect(x, y, width - 1, height - 1, ARC, ARC);

        if (highlightVisible) {
            graphics2D.setColor(AppTheme.GLASS_HIGHLIGHT);
            graphics2D.drawRoundRect(x + 1, y + 1, width - 3,
                    Math.max(18, height / 2), ARC - 4, ARC - 4);
        }

        graphics2D.dispose();
        super.paintComponent(graphics);
    }
}
