package it.unibo.sage.view.components;

import it.unibo.sage.view.theme.AppTheme;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;
import javax.swing.*;

/**
 * Barra laterale della dashboard. Se presente, usa un'immagine personalizzata
 * come sfondo; altrimenti mantiene il gradiente scuro originale.
 */
public class SidebarPanel extends JPanel {

    private static final Path CUSTOM_BACKGROUND_PATH = Path.of(
            "src", "it", "unibo", "sage", "view", "assets", "sidebar-background.png");

    private final BufferedImage backgroundImage;

    public SidebarPanel() {
        setOpaque(false);
        backgroundImage = loadBackgroundImage();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D graphics2D = (Graphics2D) graphics.create();
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        if (backgroundImage != null) {
            paintCoverImage(graphics2D, width, height);
            graphics2D.dispose();
            super.paintComponent(graphics);
            return;
        }

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

    private BufferedImage loadBackgroundImage() {
        if (!Files.isRegularFile(CUSTOM_BACKGROUND_PATH)) {
            return null;
        }
        try {
            return ImageIO.read(CUSTOM_BACKGROUND_PATH.toFile());
        } catch (final IOException ignored) {
            return null;
        }
    }

    private void paintCoverImage(final Graphics2D graphics2D, final int width, final int height) {
        final double scale = Math.max(
                (double) width / backgroundImage.getWidth(),
                (double) height / backgroundImage.getHeight());
        final int scaledWidth = (int) Math.ceil(backgroundImage.getWidth() * scale);
        final int scaledHeight = (int) Math.ceil(backgroundImage.getHeight() * scale);
        final int x = (width - scaledWidth) / 2;
        final int y = (height - scaledHeight) / 2;

        graphics2D.drawImage(backgroundImage, x, y, scaledWidth, scaledHeight, null);
        graphics2D.setColor(new Color(5, 15, 30, 92));
        graphics2D.fillRect(0, 0, width, height);
    }
}
