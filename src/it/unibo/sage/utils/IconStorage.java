package it.unibo.sage.utils;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import javax.imageio.ImageIO;

/**
 * Salva le icone personalizzate dell'utente in una cartella stabile del profilo
 * Windows e restituisce un riferimento breve da memorizzare nel database.
 */
public final class IconStorage {

    public static final String CUSTOM_PREFIX = "custom:";
    private static final int OUTPUT_SIZE = 256;
    private static final int MAX_FILE_SIZE_MB = 10;
    private static final Path ICON_DIRECTORY = Paths.get(
            System.getProperty("user.home"), ".sage", "icons");

    private IconStorage() {
    }

    public static String saveCustomIcon(final File sourceFile) throws IOException {
        if (sourceFile == null || !sourceFile.isFile()) {
            throw new IOException("Seleziona un file immagine valido");
        }
        if (sourceFile.length() > MAX_FILE_SIZE_MB * 1024L * 1024L) {
            throw new IOException("L'immagine supera il limite di " + MAX_FILE_SIZE_MB + " MB");
        }

        final BufferedImage source = ImageIO.read(sourceFile);
        if (source == null || source.getWidth() <= 0 || source.getHeight() <= 0) {
            throw new IOException("Formato immagine non supportato. Usa PNG, JPG, GIF o BMP");
        }

        Files.createDirectories(ICON_DIRECTORY);
        final String fileName = UUID.randomUUID().toString().replace("-", "") + ".png";
        final Path destination = ICON_DIRECTORY.resolve(fileName);

        final BufferedImage normalized = new BufferedImage(
                OUTPUT_SIZE, OUTPUT_SIZE, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D graphics = normalized.createGraphics();
        try {
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            graphics.setRenderingHint(RenderingHints.KEY_RENDERING,
                    RenderingHints.VALUE_RENDER_QUALITY);
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            final double scale = Math.min(
                    (double) OUTPUT_SIZE / source.getWidth(),
                    (double) OUTPUT_SIZE / source.getHeight());
            final int width = Math.max(1, (int) Math.round(source.getWidth() * scale));
            final int height = Math.max(1, (int) Math.round(source.getHeight() * scale));
            final int x = (OUTPUT_SIZE - width) / 2;
            final int y = (OUTPUT_SIZE - height) / 2;
            graphics.drawImage(source, x, y, width, height, null);
        } finally {
            graphics.dispose();
        }

        if (!ImageIO.write(normalized, "png", destination.toFile())) {
            throw new IOException("Impossibile salvare l'icona personalizzata");
        }
        return CUSTOM_PREFIX + fileName;
    }

    public static boolean isCustomIconReference(final String reference) {
        if (reference == null || !reference.startsWith(CUSTOM_PREFIX)) {
            return false;
        }
        final String fileName = reference.substring(CUSTOM_PREFIX.length());
        return fileName.matches("[a-fA-F0-9]{32}\\.png");
    }

    public static Path resolveCustomIcon(final String reference) {
        if (!isCustomIconReference(reference)) {
            return null;
        }
        final String fileName = reference.substring(CUSTOM_PREFIX.length());
        final Path resolved = ICON_DIRECTORY.resolve(fileName).normalize();
        return resolved.startsWith(ICON_DIRECTORY) ? resolved : null;
    }
}
