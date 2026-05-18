package it.unibo.sage.view;

import it.unibo.sage.controller.DocumentiController;
import it.unibo.sage.model.DocumentoDettaglio;
import it.unibo.sage.model.SpesaDocumentabile;
import it.unibo.sage.model.Utente;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

public class DocumentiPanel extends JPanel {

    private final Utente currentUser;
    private final DocumentiController documentiController;
    private final DocumentiListPanel documentiListPanel;
    private final NotificationGlassPanel notificationPanel;
    private final SoftButton addButton;
    private List<SpesaDocumentabile> speseDocumentabili;

    public DocumentiPanel(final Utente currentUser) {
        super(new BorderLayout(0, 18));
        this.currentUser = currentUser;
        documentiController = new DocumentiController();
        speseDocumentabili = Collections.emptyList();

        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        notificationPanel = new NotificationGlassPanel();
        documentiListPanel = new DocumentiListPanel(
                this::openDocument,
                this::showEditDocumentDialog,
                this::confirmDeleteDocument);

        addButton = createPrimaryButton("Aggiungi documento");
        addButton.addActionListener(event -> showAddDocumentDialog());

        add(createTopPanel(), BorderLayout.NORTH);
        add(documentiListPanel, BorderLayout.CENTER);

        refreshData();
    }

    private JPanel createTopPanel() {
        final JPanel topPanel = new JPanel(new BorderLayout(0, 12));
        topPanel.setOpaque(false);
        topPanel.add(createHeader(), BorderLayout.NORTH);
        topPanel.add(notificationPanel, BorderLayout.CENTER);
        return topPanel;
    }

    private JPanel createHeader() {
        final JPanel header = new JPanel(new BorderLayout(16, 0));
        header.setOpaque(false);

        final JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));

        final JLabel title = new JLabel("Documenti Digitali");
        title.setForeground(AppTheme.TEXT);
        title.setFont(new Font("SansSerif", Font.BOLD, 26));

        final JLabel subtitle = new JLabel("Scontrini e fatture associati alle spese");
        subtitle.setForeground(AppTheme.TEXT_MUTED);
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 14));

        titlePanel.add(title);
        titlePanel.add(Box.createVerticalStrut(6));
        titlePanel.add(subtitle);

        header.add(titlePanel, BorderLayout.WEST);
        header.add(addButton, BorderLayout.EAST);
        return header;
    }

    private SoftButton createPrimaryButton(final String text) {
        final SoftButton button = new SoftButton(text);
        button.setBackground(AppTheme.ACCENT);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("SansSerif", Font.BOLD, 13));
        button.setBorder(BorderFactory.createEmptyBorder(11, 18, 11, 18));
        button.setPreferredSize(new Dimension(190, 42));
        button.setArc(16);
        button.addMouseListener(new ButtonHoverAdapter(
                button,
                AppTheme.ACCENT,
                AppTheme.ACCENT_HOVER));
        return button;
    }

    public void refreshData() {
        refreshData(true);
    }

    private void refreshData(final boolean clearNotification) {
        if (clearNotification) {
            notificationPanel.clear();
        }
        documentiListPanel.showLoading();
        addButton.setEnabled(false);

        new SwingWorker<DocumentiData, Void>() {
            @Override
            protected DocumentiData doInBackground() throws Exception {
                return new DocumentiData(
                        documentiController.caricaDocumentiUtente(currentUser.getEmail()),
                        documentiController.caricaSpeseDocumentabili(currentUser.getEmail()));
            }

            @Override
            protected void done() {
                try {
                    final DocumentiData data = get();
                    speseDocumentabili = data.speseDocumentabili;
                    documentiListPanel.setDocumenti(data.documenti);
                    addButton.setEnabled(true);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    showError("Caricamento documenti interrotto.");
                    documentiListPanel.setDocumenti(Collections.emptyList());
                    addButton.setEnabled(true);
                } catch (ExecutionException ex) {
                    showError("Impossibile caricare i documenti dal database.");
                    documentiListPanel.setDocumenti(Collections.emptyList());
                    addButton.setEnabled(true);
                }
            }
        }.execute();
    }

    private void showAddDocumentDialog() {
        if (speseDocumentabili.isEmpty()) {
            showInfoDialog("Non ci sono spese senza documento per questo utente.");
            return;
        }

        final JComboBox<SpesaDocumentabile> spesaComboBox =
                new JComboBox<>(speseDocumentabili.toArray(new SpesaDocumentabile[0]));
        final JTextField pathField = new JTextField();
        pathField.setEditable(false);
        final JButton browseButton = new JButton("Sfoglia");
        final File[] selectedFile = new File[1];

        browseButton.addActionListener(event -> {
            final JFileChooser fileChooser = new JFileChooser();
            final int result = fileChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                selectedFile[0] = fileChooser.getSelectedFile();
                pathField.setText(selectedFile[0].getAbsolutePath());
            }
        });

        final JPanel form = createDocumentForm(spesaComboBox, pathField, browseButton);
        final int option = JOptionPane.showConfirmDialog(
                this,
                form,
                "Aggiungi documento",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (option == JOptionPane.OK_OPTION) {
            final SpesaDocumentabile selectedSpesa =
                    (SpesaDocumentabile) spesaComboBox.getSelectedItem();
            addDocument(selectedSpesa, selectedFile[0]);
        }
    }

    private JPanel createDocumentForm(final JComboBox<SpesaDocumentabile> spesaComboBox,
            final JTextField pathField, final JButton browseButton) {
        final JPanel form = new JPanel(new GridBagLayout());
        final GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(0, 0, 8, 0);
        form.add(new JLabel("Spesa"), constraints);

        constraints.gridy = 1;
        constraints.weightx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        form.add(spesaComboBox, constraints);

        constraints.gridy = 2;
        constraints.weightx = 0;
        constraints.fill = GridBagConstraints.NONE;
        constraints.insets = new Insets(14, 0, 8, 0);
        form.add(new JLabel("File"), constraints);

        final JPanel filePanel = new JPanel(new BorderLayout(8, 0));
        filePanel.add(pathField, BorderLayout.CENTER);
        filePanel.add(browseButton, BorderLayout.EAST);

        constraints.gridy = 3;
        constraints.weightx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(0, 0, 0, 0);
        form.add(filePanel, constraints);
        return form;
    }

    private void addDocument(final SpesaDocumentabile spesa, final File file) {
        if (spesa == null || file == null) {
            showError("Seleziona una spesa e un file valido.");
            return;
        }

        addButton.setEnabled(false);
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                documentiController.aggiungiDocumento(currentUser.getEmail(), spesa, file);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    showSuccess("Documento aggiunto correttamente.");
                    refreshData(false);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    showError("Inserimento documento interrotto.");
                    addButton.setEnabled(true);
                } catch (ExecutionException ex) {
                    showError(resolveErrorMessage(ex));
                    addButton.setEnabled(true);
                }
            }
        }.execute();
    }

    private void showEditDocumentDialog(final DocumentoDettaglio documento) {
        final JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Seleziona nuovo file");
        final File currentFile = new File(documento.getPathFile());
        if (currentFile.isFile()) {
            fileChooser.setSelectedFile(currentFile);
        }

        final int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            updateDocument(documento, fileChooser.getSelectedFile());
        }
    }

    private void updateDocument(final DocumentoDettaglio documento, final File file) {
        if (file == null) {
            showError("Seleziona un file valido.");
            return;
        }

        addButton.setEnabled(false);
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                documentiController.modificaDocumento(currentUser.getEmail(), documento, file);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    showSuccess("Documento modificato correttamente.");
                    refreshData(false);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    showError("Modifica documento interrotta.");
                    addButton.setEnabled(true);
                } catch (ExecutionException ex) {
                    showError(resolveErrorMessage(ex));
                    addButton.setEnabled(true);
                }
            }
        }.execute();
    }

    private void confirmDeleteDocument(final DocumentoDettaglio documento) {
        final int option = JOptionPane.showConfirmDialog(
                this,
                "Eliminare definitivamente questo documento?",
                "Conferma eliminazione",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (option == JOptionPane.YES_OPTION) {
            deleteDocument(documento);
        }
    }

    private void deleteDocument(final DocumentoDettaglio documento) {
        addButton.setEnabled(false);
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                documentiController.eliminaDocumento(
                        currentUser.getEmail(),
                        documento.getIdDocumento());
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    showSuccess("Documento eliminato correttamente.");
                    refreshData(false);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    showError("Eliminazione documento interrotta.");
                    addButton.setEnabled(true);
                } catch (ExecutionException ex) {
                    showError(resolveErrorMessage(ex));
                    addButton.setEnabled(true);
                }
            }
        }.execute();
    }

    private void openDocument(final DocumentoDettaglio documento) {
        try {
            documentiController.apriDocumento(documento.getPathFile());
        } catch (IOException ex) {
            showError(ex.getMessage());
        }
    }

    private String resolveErrorMessage(final ExecutionException exception) {
        final Throwable cause = exception.getCause();
        if (cause instanceof IllegalArgumentException || cause instanceof SQLException) {
            return cause.getMessage();
        }
        return "Operazione non riuscita.";
    }

    private void showSuccess(final String message) {
        notificationPanel.showSuccess(message);
    }

    private void showError(final String message) {
        notificationPanel.showError(message);
    }

    private void showInfoDialog(final String message) {
        JOptionPane.showMessageDialog(this, message, "Documenti", JOptionPane.INFORMATION_MESSAGE);
    }

    private static final class DocumentiData {

        private final List<DocumentoDettaglio> documenti;
        private final List<SpesaDocumentabile> speseDocumentabili;

        private DocumentiData(final List<DocumentoDettaglio> documenti,
                final List<SpesaDocumentabile> speseDocumentabili) {
            this.documenti = documenti;
            this.speseDocumentabili = speseDocumentabili;
        }
    }
}
