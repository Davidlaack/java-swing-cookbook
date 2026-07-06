package main.java.gui;

import main.java.model.Rezept;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.net.URL;

/**
 * Die Detailansicht (rechte Seite) der Hauptfenster.
 * Zeigt alle Informationen zu einem ausgewählten Rezept an.
 */
public class RezeptPanel extends JPanel {

    private JLabel titelLabel;          // Großer Titel des Rezepts
    private JTextArea zutatenArea;      // Liste der Zutaten
    private JTextArea zubereitungArea;  // Zubereitungsanleitung
    private JLabel infoLabel;           // Zusatzinfos (Kategorie, Portionen, Zeit)
    private JLabel bildLabel;           // Anzeige des Rezeptbildes

    /**
     * Konstruktor: Baut die Oberfläche auf.
     */
    public RezeptPanel() {
        initUI();
    }

    /**
     * Initialisiert die grafische Oberfläche mit BorderLayout.
     */
    private void initUI() {
        setLayout(new BorderLayout(12, 15));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Titel (groß und zentriert)
        titelLabel = new JLabel("Kein Rezept ausgewählt", JLabel.CENTER);
        titelLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titelLabel.setForeground(new Color(40, 40, 60));

        // Bildanzeige
        bildLabel = new JLabel("Kein Bild", JLabel.CENTER);
        bildLabel.setPreferredSize(new Dimension(320, 220));
        bildLabel.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 2));
        bildLabel.setOpaque(true);
        bildLabel.setBackground(new Color(248, 248, 248));

        // Info-Zeile (Kategorie, Portionen, Zeit)
        infoLabel = new JLabel("", JLabel.CENTER);
        infoLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        infoLabel.setForeground(new Color(100, 100, 120));

        // Zutaten-Bereich
        zutatenArea = new JTextArea();
        zutatenArea.setEditable(false);
        zutatenArea.setFont(new Font("Arial", Font.PLAIN, 14));
        zutatenArea.setLineWrap(true);
        zutatenArea.setBackground(new Color(250, 250, 255));

        // Zubereitungs-Bereich
        zubereitungArea = new JTextArea();
        zubereitungArea.setEditable(false);
        zubereitungArea.setFont(new Font("Arial", Font.PLAIN, 14));
        zubereitungArea.setLineWrap(true);
        zubereitungArea.setWrapStyleWord(true);
        zubereitungArea.setBackground(new Color(250, 250, 255));

        // Layout-Aufbau
        JPanel bildPanel = new JPanel(new BorderLayout());
        bildPanel.setBackground(Color.WHITE);
        bildPanel.add(bildLabel, BorderLayout.CENTER);

        JPanel content = new JPanel(new GridLayout(2, 1, 0, 20));
        content.setBackground(Color.WHITE);

        JPanel zutatenPanel = createSection("Zutaten:", zutatenArea);
        JPanel zubereitungPanel = createSection("Zubereitung:", zubereitungArea);

        content.add(zutatenPanel);
        content.add(zubereitungPanel);

        add(titelLabel, BorderLayout.NORTH);
        add(bildPanel, BorderLayout.WEST);
        add(content, BorderLayout.CENTER);
        add(infoLabel, BorderLayout.SOUTH);
    }

    /**
     * Hilfsmethode zum Erstellen eines Abschnitts (Zutaten oder Zubereitung).
     */
    private JPanel createSection(String title, JTextArea area) {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(Color.WHITE);
        panel.add(new JLabel(title), BorderLayout.NORTH);
        panel.add(new JScrollPane(area), BorderLayout.CENTER);
        return panel;
    }

    /**
     * Zeigt die Daten eines Rezepts in der Detailansicht an.
     * Wird aufgerufen, wenn in der Liste ein Rezept ausgewählt wird.
     */
    public void showRezept(Rezept rezept) {
        if (rezept == null) {
            clear();
            return;
        }

        titelLabel.setText(rezept.getTitel());
        zutatenArea.setText(rezept.getZutatenAlsText());
        zubereitungArea.setText(rezept.getZubereitung() != null ? rezept.getZubereitung() : "Keine Anleitung vorhanden.");

        String info = String.format("Kategorie: %s • Portionen: %d • Zubereitungszeit: %d Minuten",
                rezept.getKategorie(), rezept.getPortionen(), rezept.getZubereitungszeit());
        infoLabel.setText(info);

        ladeBild(rezept.getBildPfad());
    }

    /**
     * Lädt und skaliert das Bild aus einem Pfad oder einer URL.
     * Unterstützt sowohl relative als auch absolute Pfade.
     */
    private void ladeBild(String path) {
        if (path == null || path.trim().isEmpty()) {
            bildLabel.setText("Kein Bild");
            bildLabel.setIcon(null);
            return;
        }

        try {
            File file = new File(path);
            if (!file.isAbsolute()) {
                file = new File(System.getProperty("user.dir"), path); // Relativen Pfad absolut machen
            }

            if (file.exists()) {
                ImageIcon icon = new ImageIcon(file.getAbsolutePath());
                Image img = icon.getImage().getScaledInstance(300, 210, Image.SCALE_SMOOTH);
                bildLabel.setIcon(new ImageIcon(img));
                bildLabel.setText("");
            } else {
                bildLabel.setText("Bild nicht gefunden");
                bildLabel.setIcon(null);
            }
        } catch (Exception e) {
            bildLabel.setText("Bild konnte nicht geladen werden");
            bildLabel.setIcon(null);
            e.printStackTrace();
        }
    }

    /**
     * Setzt alle Felder zurück, wenn kein Rezept ausgewählt ist.
     */
    private void clear() {
        titelLabel.setText("Kein Rezept ausgewählt");
        zutatenArea.setText("");
        zubereitungArea.setText("");
        infoLabel.setText("");
        bildLabel.setText("Kein Bild");
        bildLabel.setIcon(null);
    }
}