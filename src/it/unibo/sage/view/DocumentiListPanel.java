package it.unibo.sage.view;

import it.unibo.sage.model.DocumentoDettaglio;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class DocumentiListPanel extends JPanel {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final NumberFormat MONEY_FORMATTER =
            NumberFormat.getCurrencyInstance(Locale.ITALY);
    private static final int PREVIEW_SIZE = 92;
    private static final int ACTION_SIZE = 42;

    private final Consumer<DocumentoDettaglio> onOpen;
    private final Consumer<DocumentoDettaglio> onEdit;
    private final Consumer<DocumentoDettaglio> onDelete;
    private final JPanel listPanel;

    public DocumentiListPanel(final Consumer<DocumentoDettaglio> onOpen,
            final Consumer<DocumentoDettaglio> onEdit,
            final Consumer<DocumentoDettaglio> onDelete) {
        super(new BorderLayout());
        this.onOpen = onOpen;
        this.onEdit = onEdit;
        this.onDelete = onDelete;

        setOpaque(false);
        listPanel = new JPanel();
        listPanel.setOpaque(false);
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));

        final JScrollPane scrollPane = new JScrollPane(listPanel);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(18);
        add(scrollPane, BorderLayout.CENTER);
    }

    public void showLoading() {
        showInfo("Caricamento documenti...");
    }

    public void setDocumenti(final List<DocumentoDettaglio> documenti) {
        listPanel.removeAll();

        if (documenti.isEmpty()) {
            listPanel.add(createInfoCard("Nessun documento associato alle spese."));
        } else {
            for (final DocumentoDettaglio documento : documenti) {
                listPanel.add(createDocumentoCard(documento));
                listPanel.add(Box.createVerticalStrut(12));
            }
        }

        refreshListPanel();
    }

    public void showInfo(final String message) {
        listPanel.removeAll();
        listPanel.add(createInfoCard(message));
        refreshListPanel();
    }

    private JPanel createDocumentoCard(final DocumentoDettaglio documento) {
        final JPanel card = new GlassPanel(new BorderLayout(18, 0));
        card.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 20));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 132));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        final JPanel details = new JPanel();
        details.setOpaque(false);
        details.setLayout(new BoxLayout(details, BoxLayout.Y_AXIS));

        final JLabel title = new JLabel(documento.getDescrizioneSpesa());
        title.setForeground(AppTheme.TEXT);
        title.setFont(new Font("SansSerif", Font.BOLD, 18));

        final JLabel meta = new JLabel(
                DATE_FORMATTER.format(documento.getDataSpesa())
                + "  -  "
                + MONEY_FORMATTER.format(documento.getImportoSpesa())
                + "  -  "
                + documento.getTipoFile());
        meta.setForeground(AppTheme.TEXT_MUTED);

        final JLabel path = new JLabel(documento.getPathFile());
        path.setForeground(AppTheme.TEXT_MUTED);
        path.setFont(new Font("SansSerif", Font.PLAIN, 12));
        path.setToolTipText(documento.getPathFile());

        final JLabel acquired = new JLabel(
                "Acquisito il " + DATE_FORMATTER.format(documento.getDataAcquisizione()));
        acquired.setForeground(AppTheme.TEXT_MUTED);
        acquired.setFont(new Font("SansSerif", Font.PLAIN, 12));

        details.add(title);
        details.add(Box.createVerticalStrut(8));
        details.add(meta);
        details.add(Box.createVerticalStrut(6));
        details.add(path);
        details.add(Box.createVerticalStrut(4));
        details.add(acquired);

        card.add(createPreviewButton(documento), BorderLayout.WEST);
        card.add(details, BorderLayout.CENTER);
        card.add(createActionPanel(documento), BorderLayout.EAST);
        return card;
    }

    private JPanel createActionPanel(final DocumentoDettaglio documento) {
        final JPanel actions = new JPanel();
        actions.setOpaque(false);
        actions.setLayout(new BoxLayout(actions, BoxLayout.X_AXIS));
        actions.add(createActionButton("A", "Apri file", AppTheme.PRIMARY,
                event -> onOpen.accept(documento)));
        actions.add(Box.createHorizontalStrut(8));
        actions.add(createActionButton("M", "Modifica documento", AppTheme.ACCENT,
                event -> onEdit.accept(documento)));
        actions.add(Box.createHorizontalStrut(8));
        actions.add(createActionButton("X", "Elimina documento", AppTheme.EXPENSE,
                event -> onDelete.accept(documento)));
        return actions;
    }

    private SoftButton createActionButton(final String text, final String tooltip,
            final Color color, final java.awt.event.ActionListener listener) {
        final SoftButton button = new SoftButton(text);
        button.setBackground(AppTheme.SURFACE_MUTED);
        button.setForeground(color);
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        button.setToolTipText(tooltip);
        button.setPreferredSize(new Dimension(ACTION_SIZE, ACTION_SIZE));
        button.setMinimumSize(new Dimension(ACTION_SIZE, ACTION_SIZE));
        button.setMaximumSize(new Dimension(ACTION_SIZE, ACTION_SIZE));
        button.setBorder(BorderFactory.createEmptyBorder());
        button.setArc(12);
        button.addMouseListener(new ButtonHoverAdapter(
                button,
                AppTheme.SURFACE_MUTED,
                AppTheme.BACKGROUND_COOL_GLOW));
        button.addActionListener(listener);
        return button;
    }

    private SoftButton createPreviewButton(final DocumentoDettaglio documento) {
        final SoftButton button = new SoftButton("");
        button.setLayout(new BorderLayout());
        button.setBackground(AppTheme.SURFACE_MUTED);
        button.setForeground(AppTheme.PRIMARY);
        button.setPreferredSize(new Dimension(PREVIEW_SIZE, PREVIEW_SIZE));
        button.setMinimumSize(new Dimension(PREVIEW_SIZE, PREVIEW_SIZE));
        button.setMaximumSize(new Dimension(PREVIEW_SIZE, PREVIEW_SIZE));
        button.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        button.setArc(14);
        button.setToolTipText("Apri file");
        button.addMouseListener(new ButtonHoverAdapter(
                button,
                AppTheme.SURFACE_MUTED,
                AppTheme.BACKGROUND_COOL_GLOW));
        button.add(createPreviewContent(documento), BorderLayout.CENTER);
        button.addActionListener(event -> onOpen.accept(documento));
        return button;
    }

    private JLabel createPreviewContent(final DocumentoDettaglio documento) {
        final JLabel preview = new JLabel("", JLabel.CENTER);
        preview.setForeground(AppTheme.PRIMARY);
        preview.setFont(new Font("SansSerif", Font.BOLD, 18));

        final ImageIcon imagePreview = loadImagePreview(documento.getPathFile());
        if (imagePreview != null) {
            preview.setIcon(imagePreview);
            return preview;
        }

        preview.setText(normalizeFileType(documento.getTipoFile()));
        return preview;
    }

    private ImageIcon loadImagePreview(final String pathFile) {
        final File file = new File(pathFile);
        if (!file.isFile()) {
            return null;
        }

        try {
            final BufferedImage image = ImageIO.read(file);
            if (image == null) {
                return null;
            }
            final Image scaled = image.getScaledInstance(
                    PREVIEW_SIZE - 18,
                    PREVIEW_SIZE - 18,
                    Image.SCALE_SMOOTH);
            return new ImageIcon(scaled);
        } catch (IOException ex) {
            return null;
        }
    }

    private String normalizeFileType(final String tipoFile) {
        if (tipoFile == null || tipoFile.isBlank()) {
            return "FILE";
        }
        return tipoFile.length() > 5 ? tipoFile.substring(0, 5) : tipoFile;
    }

    private JPanel createInfoCard(final String message) {
        final JPanel card = new GlassPanel(new GridBagLayout());
        card.setBorder(BorderFactory.createEmptyBorder(32, 24, 32, 24));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        final JLabel label = new JLabel(message);
        label.setForeground(AppTheme.TEXT_MUTED);
        label.setFont(new Font("SansSerif", Font.BOLD, 15));
        card.add(label);
        return card;
    }

    private void refreshListPanel() {
        listPanel.revalidate();
        listPanel.repaint();
    }
}
