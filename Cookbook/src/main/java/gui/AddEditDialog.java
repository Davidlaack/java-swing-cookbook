package main.java.gui;

import main.java.data.RezeptManager;
import main.java.model.Rezept;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Modal-Dialog zum Anlegen und Bearbeiten von Rezepten.
 * Wird aus der MainFrame heraus aufgerufen.
 */
public class AddEditDialog extends JDialog {

    private boolean saved = false;                    // Gibt an, ob das Rezept gespeichert wurde
    private RezeptManager manager;                    // Referenz zum RezeptManager
    private Rezept originalRezept;                    // Das zu bearbeitende Rezept (null bei neuem Rezept)

    // Eingabefelder
    private JTextField titelField;
    private JComboBox<String> kategorieBox;
    private JSpinner portionenSpinner;
    private JSpinner zeitSpinner;
    private JTextArea zutatenArea;
    private JTextArea zubereitungArea;
    private JTextField bildUrlField;
    private JButton generateImageBtn;                 // Button für KI-Bildgenerierung

    private final String apiKey;                      // API-Key für Flux-Bildgenerierung

    /**
     * Konstruktor
     * @param owner Das übergeordnete Fenster (MainFrame)
     * @param original Das zu bearbeitende Rezept (null bei neuem Rezept)
     * @param manager Der RezeptManager
     * @param apiKey Der fal.ai API-Key
     */
    public AddEditDialog(Frame owner, Rezept original, RezeptManager manager, String apiKey) {
        super(owner, original == null ? "Neues Rezept" : "Rezept bearbeiten", true);
        this.manager = manager;
        this.originalRezept = original;
        this.apiKey = apiKey;
        initUI();
        if (original != null) fillFields(original);
    }

    /**
     * Baut die Benutzeroberfläche des Dialogs auf.
     * Verwendet GridBagLayout für eine flexible Anordnung der Felder.
     */
    private void initUI() {
        setSize(800, 880);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        titelField = new JTextField(45);
        kategorieBox = new JComboBox<>(new String[]{"Hauptgericht", "Vorspeise", "Dessert", "Salat",
                "Suppe", "Vegetarisch", "Backen", "Sonstiges"});
        portionenSpinner = new JSpinner(new SpinnerNumberModel(2, 1, 20, 1));
        zeitSpinner = new JSpinner(new SpinnerNumberModel(30, 5, 300, 5));
        zutatenArea = new JTextArea(10, 50);
        zubereitungArea = new JTextArea(12, 50);
        bildUrlField = new JTextField(55);

        generateImageBtn = new JButton("Bild mit Flux generieren");
        generateImageBtn.addActionListener(e -> generateImage());

        int row = 0;

        addFormRow(formPanel, "Titel:", titelField, gbc, row++);
        addFormRow(formPanel, "Kategorie:", kategorieBox, gbc, row++);
        addFormRow(formPanel, "Portionen:", portionenSpinner, gbc, row++);
        addFormRow(formPanel, "Zubereitungszeit (Min):", zeitSpinner, gbc, row++);

        gbc.gridy = row++; gbc.gridx = 0; gbc.gridwidth = 2;
        formPanel.add(new JLabel("Zutaten (eine pro Zeile):"), gbc);
        gbc.gridy = row++;
        formPanel.add(new JScrollPane(zutatenArea), gbc);

        gbc.gridy = row++;
        formPanel.add(new JLabel("Zubereitung:"), gbc);
        gbc.gridy = row++;
        formPanel.add(new JScrollPane(zubereitungArea), gbc);

        gbc.gridy = row++; gbc.gridwidth = 1;
        formPanel.add(new JLabel("Bild-URL / Pfad:"), gbc);
        gbc.gridx = 1;
        formPanel.add(bildUrlField, gbc);

        gbc.gridy = row++; gbc.gridx = 0; gbc.gridwidth = 2;
        formPanel.add(generateImageBtn, gbc);

        add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        JButton saveBtn = new JButton("Speichern");
        JButton cancelBtn = new JButton("Abbrechen");

        saveBtn.addActionListener(e -> save());
        cancelBtn.addActionListener(e -> dispose());

        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    /**
     * Hilfsmethode zum Hinzufügen einer Zeile im Formular.
     */
    private void addFormRow(JPanel panel, String label, JComponent field, GridBagConstraints gbc, int row) {
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        panel.add(new JLabel(label), gbc);
        gbc.gridx = 1;
        panel.add(field, gbc);
    }

    /**
     * Füllt die Felder beim Bearbeiten eines bestehenden Rezepts.
     */
    private void fillFields(Rezept r) {
        titelField.setText(r.getTitel());
        kategorieBox.setSelectedItem(r.getKategorie());
        portionenSpinner.setValue(r.getPortionen());
        zeitSpinner.setValue(r.getZubereitungszeit());
        zutatenArea.setText(r.getZutatenAlsText());
        zubereitungArea.setText(r.getZubereitung());
        bildUrlField.setText(r.getBildPfad() != null ? r.getBildPfad() : "");
    }

    /**
     * Generiert ein Bild mit Flux basierend auf dem aktuellen Rezepttitel.
     * Läuft asynchron, um die GUI nicht zu blockieren.
     */
    private void generateImage() {
        String titel = titelField.getText().trim();
        if (titel.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Bitte zuerst einen Titel eingeben!", "Hinweis", JOptionPane.WARNING_MESSAGE);
            return;
        }

        generateImageBtn.setEnabled(false);
        generateImageBtn.setText("Generiere Bild... (kann 3-10 Sekunden dauern)");

        new Thread(() -> {
            try {
                String imageName = titel.toLowerCase()
                        .replaceAll("[^a-z0-9äöüß]", "_")
                        + "_" + System.currentTimeMillis() + ".jpg";

                File imagesDir = new File("data/images");
                if (!imagesDir.exists()) imagesDir.mkdirs();

                String savePath = "data/images/" + imageName;

                ImageGenerator generator = new ImageGenerator(apiKey);
                String resultPath = generator.generateImage(titel, savePath);

                SwingUtilities.invokeLater(() -> {
                    if (resultPath != null) {
                        bildUrlField.setText(resultPath);
                        JOptionPane.showMessageDialog(this,
                            "Bild erfolgreich generiert und gespeichert!",
                            "Erfolg", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this,
                            "Bildgenerierung fehlgeschlagen.\nÜberprüfe die Konsole für Details.",
                            "Fehler", JOptionPane.ERROR_MESSAGE);
                    }
                    generateImageBtn.setEnabled(true);
                    generateImageBtn.setText("Bild mit Flux generieren");
                });
            } catch (Exception ex) {
                ex.printStackTrace();
                SwingUtilities.invokeLater(() -> {
                    generateImageBtn.setEnabled(true);
                    generateImageBtn.setText("Bild mit Flux generieren");
                });
            }
        }).start();
    }

    /**
     * Speichert das Rezept (neu oder bearbeitet).
     */
    private void save() {
        if (titelField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Titel darf nicht leer sein!", "Fehler", JOptionPane.ERROR_MESSAGE);
            return;
        }

        List<String> zutaten = Arrays.stream(zutatenArea.getText().split("\n"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());

        Rezept neuesRezept = new Rezept(
                titelField.getText().trim(),
                (String) kategorieBox.getSelectedItem(),
                (int) portionenSpinner.getValue(),
                (int) zeitSpinner.getValue(),
                zutaten,
                zubereitungArea.getText().trim(),
                bildUrlField.getText().trim().isEmpty() ? null : bildUrlField.getText().trim(),
                ""
        );

        if (originalRezept != null) {
            manager.updateRezept(originalRezept, neuesRezept);
        } else {
            manager.addRezept(neuesRezept);
        }

        saved = true;
        dispose();
    }

    /**
     * Gibt zurück, ob das Rezept gespeichert wurde.
     */
    public boolean isSaved() {
        return saved;
    }
}