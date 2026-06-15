package it.unibo.sage.view.dashboard;

import it.unibo.sage.utils.IconStorage;
import it.unibo.sage.view.theme.AppTheme;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.datatransfer.DataFlavor;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.TransferHandler;

final class ClassificationIconSupport {

    private static final List<IconChoice> CATEGORY_ICON_CHOICES = List.of(
            new IconChoice("Generica", "generic_category.png"),
            new IconChoice("Casa", "house.png"),
            new IconChoice("Cibo", "food.png"),
            new IconChoice("Trasporti", "transport.png"),
            new IconChoice("Salute", "health.png"),
            new IconChoice("Studio", "study.png"),
            new IconChoice("Lavoro", "work.png"),
            new IconChoice("Risparmi", "savings.png"),
            new IconChoice("Shopping", "shopping.png"),
            new IconChoice("Svago", "leisure.png"),
            new IconChoice("Bollette", "bill.png"),
            new IconChoice("Palestra", "gym.png"),
            new IconChoice("Viaggi", "travel.png"),
            new IconChoice("Regalo", "gift.png"));

    private static final List<IconChoice> TAG_ICON_CHOICES = List.of(
            new IconChoice("Generica", "generic_tag.png"),
            new IconChoice("Urgente", "urgent.png"),
            new IconChoice("Studio", "study.png"),
            new IconChoice("Palestra", "gym.png"),
            new IconChoice("Lavoro", "work.png"),
            new IconChoice("Famiglia", "family.png"),
            new IconChoice("Viaggi", "travel.png"),
            new IconChoice("Regalo", "gift.png"),
            new IconChoice("Risparmi", "savings.png"),
            new IconChoice("Shopping", "shopping.png"),
            new IconChoice("Svago", "leisure.png"));

    private static final List<IconChoice> SOURCE_ICON_CHOICES = List.of(
            new IconChoice("Generica", "generic_source.png"),
            new IconChoice("Stipendio", "salary.png"),
            new IconChoice("Borsa di studio", "scholarship.png"),
            new IconChoice("Regalo", "gift.png"),
            new IconChoice("Rimborso", "refund.png"),
            new IconChoice("Ripetizioni", "tutoring.png"),
            new IconChoice("Lavoro", "work.png"),
            new IconChoice("Famiglia", "family.png"),
            new IconChoice("Risparmi", "savings.png"));

    private ClassificationIconSupport() {
    }

    static List<IconChoice> iconChoices(final String type) {
        if ("Categoria".equals(type)) {
            return CATEGORY_ICON_CHOICES;
        }
        if ("Fonte".equals(type)) {
            return SOURCE_ICON_CHOICES;
        }
        return TAG_ICON_CHOICES;
    }

    static String defaultIcon(final String type) {
        if ("Categoria".equals(type)) {
            return "generic_category.png";
        }
        if ("Fonte".equals(type)) {
            return "generic_source.png";
        }
        return "generic_tag.png";
    }

    static JComponent createClassificationIcon(final String type, final String name,
            final String iconName) {
        final ImageIcon icon = loadClassificationIcon(type, name, iconName);
        return new JComponent() {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(56, 56);
            }

            @Override
            protected void paintComponent(final Graphics graphics) {
                final Graphics2D graphics2D = (Graphics2D) graphics.create();
                graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                graphics2D.setColor(classificationBackground(type));
                graphics2D.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);

                if (icon != null) {
                    final int x = (getWidth() - icon.getIconWidth()) / 2;
                    final int y = (getHeight() - icon.getIconHeight()) / 2;
                    icon.paintIcon(this, graphics2D, x, y);
                } else {
                    paintFallbackClassificationIcon(graphics2D, type, getWidth(), getHeight());
                }
                graphics2D.dispose();
            }
        };
    }

    private static ImageIcon loadClassificationIcon(final String type, final String name,
            final String iconName) {
        if (IconStorage.isCustomIconReference(iconName)) {
            final ImageIcon customIcon = loadIconFile(iconName, 30);
            if (customIcon != null) {
                return customIcon;
            }
        }

        final String resourcePath = getClassificationIconPath(type, name, iconName);
        ImageIcon rawIcon = null;

        final java.net.URL url = ClassificationIconSupport.class.getResource(resourcePath);
        if (url != null) {
            rawIcon = new ImageIcon(url);
        } else {
            final String relativePath = resourcePath.substring(1).replace("/", java.io.File.separator);
            final List<java.nio.file.Path> possiblePaths = List.of(
                    java.nio.file.Paths.get("src", relativePath),
                    java.nio.file.Paths.get("bin", relativePath),
                    java.nio.file.Paths.get("build", "classes", relativePath));

            for (final java.nio.file.Path path : possiblePaths) {
                if (Files.exists(path)) {
                    rawIcon = new ImageIcon(path.toString());
                    break;
                }
            }
        }

        if (rawIcon == null || rawIcon.getIconWidth() <= 0 || rawIcon.getIconHeight() <= 0) {
            return null;
        }

        final Image scaledImage = rawIcon.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
        return new ImageIcon(scaledImage);
    }

    private static String getClassificationIconPath(final String type, final String name,
            final String iconName) {
        if (iconName != null && !iconName.isBlank()
                && iconName.matches("[a-z0-9_-]+\\.png")) {
            return "/it/unibo/sage/view/icons/" + iconName;
        }
        final String normalized = normalizeClassificationName(name);
        if ("Categoria".equals(type)) {
            switch (normalized) {
                case "casa":
                case "affitto":
                    return "/it/unibo/sage/view/icons/house.png";
                case "spesa":
                case "cibo":
                case "alimentari":
                case "alimentazione":
                    return "/it/unibo/sage/view/icons/food.png";
                case "trasporti":
                case "trasporto":
                case "bus":
                case "treno":
                    return "/it/unibo/sage/view/icons/transport.png";
                case "salute":
                case "medicina":
                case "farmacia":
                    return "/it/unibo/sage/view/icons/health.png";
                case "studio":
                case "universita":
                case "libri":
                    return "/it/unibo/sage/view/icons/study.png";
                case "stipendio":
                case "entrate":
                case "lavoro":
                    return "/it/unibo/sage/view/icons/work.png";
                case "risparmi":
                case "risparmio":
                    return "/it/unibo/sage/view/icons/savings.png";
                case "shopping":
                case "acquisti":
                    return "/it/unibo/sage/view/icons/shopping.png";
                case "svago":
                case "tempo libero":
                    return "/it/unibo/sage/view/icons/leisure.png";
                case "bollette":
                case "utenze":
                    return "/it/unibo/sage/view/icons/bill.png";
                default:
                    return "/it/unibo/sage/view/icons/generic_category.png";
            }
        }
        if ("Fonte".equals(type)) {
            switch (normalized) {
                case "stipendio":
                case "salario":
                case "lavoro":
                    return "/it/unibo/sage/view/icons/salary.png";
                case "borsa di studio":
                case "borsa studio":
                case "universita":
                    return "/it/unibo/sage/view/icons/scholarship.png";
                case "regalo":
                case "regali":
                    return "/it/unibo/sage/view/icons/gift.png";
                case "rimborso":
                case "rimborsi":
                    return "/it/unibo/sage/view/icons/refund.png";
                case "ripetizioni private":
                case "ripetizioni":
                case "lezioni":
                    return "/it/unibo/sage/view/icons/tutoring.png";
                case "lavoretto weekend":
                case "lavoretto":
                case "lavoro occasionale":
                    return "/it/unibo/sage/view/icons/work.png";
                case "famiglia":
                case "aiuto famiglia":
                    return "/it/unibo/sage/view/icons/family.png";
                default:
                    return "/it/unibo/sage/view/icons/generic_source.png";
            }
        }
        switch (normalized) {
            case "urgente":
            case "importante":
                return "/it/unibo/sage/view/icons/urgent.png";
            case "universita":
            case "esami":
            case "studio":
                return "/it/unibo/sage/view/icons/study.png";
            case "palestra":
            case "gym":
            case "sport":
                return "/it/unibo/sage/view/icons/gym.png";
            case "lavoro":
                return "/it/unibo/sage/view/icons/work.png";
            case "famiglia":
                return "/it/unibo/sage/view/icons/family.png";
            case "viaggio":
            case "viaggi":
            case "travel":
                return "/it/unibo/sage/view/icons/travel.png";
            default:
                return "/it/unibo/sage/view/icons/generic_tag.png";
        }
    }

    private static String normalizeClassificationName(final String name) {
        if (name == null) {
            return "";
        }
        return name.trim().toLowerCase()
                .replace('\u00e0', 'a')
                .replace('\u00e8', 'e')
                .replace('\u00e9', 'e')
                .replace('\u00ec', 'i')
                .replace('\u00f2', 'o')
                .replace('\u00f9', 'u');
    }

    private static Color classificationBackground(final String type) {
        if ("Categoria".equals(type)) {
            return new Color(37, 99, 235, 34);
        }
        if ("Fonte".equals(type)) {
            return new Color(245, 158, 11, 38);
        }
        return new Color(20, 184, 166, 38);
    }

    private static Color classificationAccent(final String type) {
        if ("Categoria".equals(type)) {
            return AppTheme.PRIMARY;
        }
        if ("Fonte".equals(type)) {
            return new Color(217, 119, 6);
        }
        return AppTheme.ACCENT_HOVER;
    }

    private static void paintFallbackClassificationIcon(final Graphics2D graphics2D, final String type,
            final int width, final int height) {
        graphics2D.setColor(classificationAccent(type));
        graphics2D.fillOval(width / 2 - 9, height / 2 - 9, 18, 18);
        graphics2D.setFont(new Font("SansSerif", Font.BOLD, 18));
        graphics2D.setColor(Color.WHITE);
        final String letter = "Categoria".equals(type) ? "C" : ("Fonte".equals(type) ? "F" : "T");
        final FontMetrics metrics = graphics2D.getFontMetrics();
        final int x = (width - metrics.stringWidth(letter)) / 2;
        final int y = (height - metrics.getHeight()) / 2 + metrics.getAscent();
        graphics2D.drawString(letter, x, y);
    }

    private static JComboBox<IconChoice> createIconChoiceCombo(final List<IconChoice> choices,
            final String selectedIcon) {
        final JComboBox<IconChoice> combo = new JComboBox<>(choices.toArray(new IconChoice[0]));
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(final JList<?> list, final Object value,
                    final int index, final boolean isSelected, final boolean cellHasFocus) {
                final JLabel label = (JLabel) super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
                if (value instanceof IconChoice) {
                    final IconChoice choice = (IconChoice) value;
                    label.setText(choice.label);
                    label.setIcon(loadIconFile(choice.fileName, 22));
                    label.setIconTextGap(10);
                }
                return label;
            }
        });
        for (int i = 0; i < combo.getItemCount(); i++) {
            if (combo.getItemAt(i).fileName.equals(selectedIcon)) {
                combo.setSelectedIndex(i);
                break;
            }
        }
        return combo;
    }

    private static ImageIcon loadIconFile(final String fileName, final int size) {
        if (IconStorage.isCustomIconReference(fileName)) {
            final Path customPath = IconStorage.resolveCustomIcon(fileName);
            if (customPath == null || !Files.isRegularFile(customPath)) {
                return null;
            }
            final ImageIcon customIcon = new ImageIcon(customPath.toString());
            if (customIcon.getIconWidth() <= 0 || customIcon.getIconHeight() <= 0) {
                return null;
            }
            return new ImageIcon(customIcon.getImage().getScaledInstance(
                    size, size, Image.SCALE_SMOOTH));
        }

        final String resourcePath = "/it/unibo/sage/view/icons/" + fileName;
        ImageIcon rawIcon = null;
        final java.net.URL url = ClassificationIconSupport.class.getResource(resourcePath);
        if (url != null) {
            rawIcon = new ImageIcon(url);
        } else {
            final String relativePath = resourcePath.substring(1).replace("/", java.io.File.separator);
            for (final java.nio.file.Path path : List.of(
                    java.nio.file.Paths.get("src", relativePath),
                    java.nio.file.Paths.get("bin", relativePath),
                    java.nio.file.Paths.get("build", "classes", relativePath))) {
                if (Files.exists(path)) {
                    rawIcon = new ImageIcon(path.toString());
                    break;
                }
            }
        }
        if (rawIcon == null || rawIcon.getIconWidth() <= 0 || rawIcon.getIconHeight() <= 0) {
            return null;
        }
        return new ImageIcon(rawIcon.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH));
    }

    static final class IconSelectionPanel extends JPanel {

        private final Component owner;
        private final JComboBox<IconChoice> presetCombo;
        private final JLabel previewLabel = new JLabel();
        private final JLabel statusLabel = new JLabel();
        private File pendingCustomFile;
        private String selectedReference;

        IconSelectionPanel(final Component owner, final List<IconChoice> choices,
                final String initialIcon) {
            super(new java.awt.BorderLayout(0, 8));
            this.owner = owner;
            setOpaque(false);
            setPreferredSize(new Dimension(390, 150));

            presetCombo = createIconChoiceCombo(choices, initialIcon);
            add(presetCombo, java.awt.BorderLayout.NORTH);

            final JPanel dropArea = new JPanel(new java.awt.BorderLayout(10, 0));
            dropArea.setOpaque(true);
            dropArea.setBackground(new Color(248, 250, 252));
            dropArea.setBorder(BorderFactory.createDashedBorder(
                    AppTheme.BORDER, 1.5f, 5.0f, 3.0f, true));
            dropArea.setPreferredSize(new Dimension(390, 96));
            dropArea.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            previewLabel.setPreferredSize(new Dimension(62, 62));
            previewLabel.setHorizontalAlignment(SwingConstants.CENTER);
            previewLabel.setVerticalAlignment(SwingConstants.CENTER);
            previewLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

            final JPanel instructions = new JPanel();
            instructions.setOpaque(false);
            instructions.setLayout(new BoxLayout(instructions, BoxLayout.Y_AXIS));

            final JLabel title = new JLabel("Trascina qui una tua immagine");
            title.setFont(new Font("SansSerif", Font.BOLD, 12));
            title.setForeground(AppTheme.TEXT);

            statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
            statusLabel.setForeground(AppTheme.TEXT_MUTED);

            final JButton chooseButton = new JButton("Scegli file");
            chooseButton.setFocusable(false);
            chooseButton.addActionListener(e -> chooseCustomImage());

            instructions.add(Box.createVerticalGlue());
            instructions.add(title);
            instructions.add(Box.createVerticalStrut(4));
            instructions.add(statusLabel);
            instructions.add(Box.createVerticalStrut(6));
            instructions.add(chooseButton);
            instructions.add(Box.createVerticalGlue());

            dropArea.add(previewLabel, java.awt.BorderLayout.WEST);
            dropArea.add(instructions, java.awt.BorderLayout.CENTER);
            add(dropArea, java.awt.BorderLayout.CENTER);

            final TransferHandler dropHandler = createImageDropHandler();
            installTransferHandler(dropArea, dropHandler);

            selectedReference = initialIcon;
            if (IconStorage.isCustomIconReference(initialIcon)) {
                final ImageIcon currentIcon = loadIconFile(initialIcon, 50);
                if (currentIcon != null) {
                    previewLabel.setIcon(currentIcon);
                    statusLabel.setText("Icona personalizzata attuale");
                } else {
                    selectPreset((IconChoice) presetCombo.getSelectedItem());
                }
            } else {
                selectPreset((IconChoice) presetCombo.getSelectedItem());
            }

            presetCombo.addActionListener(e ->
                    selectPreset((IconChoice) presetCombo.getSelectedItem()));
        }

        private void selectPreset(final IconChoice choice) {
            if (choice == null) {
                return;
            }
            pendingCustomFile = null;
            selectedReference = choice.fileName;
            previewLabel.setIcon(loadIconFile(choice.fileName, 50));
            statusLabel.setText("Predefinita: " + choice.label
                    + " - oppure trascina PNG/JPG/GIF/BMP");
        }

        private void chooseCustomImage() {
            final JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Scegli icona personalizzata");
            chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                    "Immagini (PNG, JPG, GIF, BMP)", "png", "jpg", "jpeg", "gif", "bmp"));
            if (chooser.showOpenDialog(owner) == JFileChooser.APPROVE_OPTION) {
                selectCustomFile(chooser.getSelectedFile());
            }
        }

        private TransferHandler createImageDropHandler() {
            return new TransferHandler() {
                @Override
                public boolean canImport(final TransferSupport support) {
                    return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
                }

                @Override
                public boolean importData(final TransferSupport support) {
                    if (!canImport(support)) {
                        return false;
                    }
                    try {
                        final Object data = support.getTransferable().getTransferData(
                                DataFlavor.javaFileListFlavor);
                        if (!(data instanceof List<?>)) {
                            return false;
                        }
                        final List<?> files = (List<?>) data;
                        if (files.isEmpty() || !(files.get(0) instanceof File)) {
                            return false;
                        }
                        selectCustomFile((File) files.get(0));
                        return true;
                    } catch (final Exception ex) {
                        showImageSelectionError(ex.getMessage());
                        return false;
                    }
                }
            };
        }

        private void installTransferHandler(final Component component,
                final TransferHandler handler) {
            if (component instanceof JComponent) {
                ((JComponent) component).setTransferHandler(handler);
            }
            if (component instanceof Container) {
                for (final Component child : ((Container) component).getComponents()) {
                    installTransferHandler(child, handler);
                }
            }
        }

        private void selectCustomFile(final File file) {
            try {
                if (file == null || !file.isFile()) {
                    throw new IOException("File non valido");
                }
                if (file.length() > 10L * 1024L * 1024L) {
                    throw new IOException("L'immagine supera il limite di 10 MB");
                }
                final ImageIcon rawIcon = new ImageIcon(file.getAbsolutePath());
                if (rawIcon.getIconWidth() <= 0 || rawIcon.getIconHeight() <= 0) {
                    throw new IOException("Formato immagine non supportato");
                }
                pendingCustomFile = file;
                selectedReference = null;
                previewLabel.setIcon(new ImageIcon(rawIcon.getImage().getScaledInstance(
                        50, 50, Image.SCALE_SMOOTH)));
                statusLabel.setText("Personalizzata: " + file.getName());
            } catch (final IOException ex) {
                showImageSelectionError(ex.getMessage());
            }
        }

        private void showImageSelectionError(final String message) {
            JOptionPane.showMessageDialog(owner,
                    "Immagine non valida: " + message,
                    "Errore immagine",
                    JOptionPane.ERROR_MESSAGE);
        }

        String resolveIconReference() throws IOException {
            if (pendingCustomFile != null) {
                return IconStorage.saveCustomIcon(pendingCustomFile);
            }
            if (selectedReference == null || selectedReference.isBlank()) {
                final IconChoice choice = (IconChoice) presetCombo.getSelectedItem();
                if (choice == null) {
                    throw new IOException("Seleziona un'icona");
                }
                return choice.fileName;
            }
            return selectedReference;
        }
    }

    static final class IconChoice {
        private final String label;
        private final String fileName;

        private IconChoice(final String label, final String fileName) {
            this.label = label;
            this.fileName = fileName;
        }

        @Override
        public String toString() {
            return label;
        }
    }
}
