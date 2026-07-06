package main.java.gui;

import main.java.data.RezeptManager;
import main.java.model.Rezept;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.HashSet;
import java.util.Set;

/**
 * Die zentrale Hauptklasse der grafischen Benutzeroberfläche.
 * Erbt von JFrame und stellt das Hauptfenster der Anwendung dar.
 * 
 * Verantwortlich für:
 * - Aufbau und Layout der gesamten GUI
 * - Verwaltung der Rezeptliste und Detailansicht
 * - Such- und Filterfunktionen
 * - Umsetzen der Dialoge (Anlegen, Bearbeiten, Import)
 */
public class MainFrame extends JFrame {

    private RezeptManager manager;                    // Verwaltet alle Rezepte
    private DefaultListModel<Rezept> listModel;       // Datenmodell für die JList
    private JList<Rezept> rezeptListe;                // Die eigentliche Rezeptliste (links)
    private RezeptPanel detailPanel;                  // Detailansicht (rechts)
    private JTextField suchfeld;                      // Suchfeld
    private JComboBox<String> kategorieFilter;        // Filter nach Kategorie

    // Der API-Key für die Flux-Bildgenerierung (fal.ai) wurde entfernt, da Zugriff auf meine kostenpflichtigen Credits
    private final String apiKey = "DeinAPI-KEY";

    /**
     * Konstruktor: Initialisiert den Manager und baut die GUI auf.
     */
    public MainFrame() {
        manager = new RezeptManager();
        initUI();
        loadRezepte();
    }

    /**
     * Baut die gesamte Benutzeroberfläche auf.
     * Verwendet ein BorderLayout mit Top-Panel und SplitPane.
     */
    private void initUI() {
        setTitle("Mein Kochbuch");
        setSize(1150, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Oberes Panel mit Suchleiste, Filter und Buttons
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));

        suchfeld = new JTextField(25);
        kategorieFilter = new JComboBox<>();
        kategorieFilter.addItem("Alle Kategorien");

        JButton suchenBtn = new JButton("Suchen");
        JButton neuBtn = new JButton("Neues Rezept +");
        JButton bearbeitenBtn = new JButton("Bearbeiten");
        JButton loeschenBtn = new JButton("Löschen");
        JButton importBtn = new JButton("Importieren");

        suchenBtn.addActionListener(e -> aktualisiereListe());
        neuBtn.addActionListener(e -> openAddDialog());
        bearbeitenBtn.addActionListener(e -> openEditDialog());
        loeschenBtn.addActionListener(e -> loeschen());
        kategorieFilter.addActionListener(e -> aktualisiereListe());
        importBtn.addActionListener(e -> importRezepte());

        topPanel.add(new JLabel("Suche nach Zutat:"));
        topPanel.add(suchfeld);
        topPanel.add(suchenBtn);
        topPanel.add(new JLabel("Kategorie:"));
        topPanel.add(kategorieFilter);
        topPanel.add(neuBtn);
        topPanel.add(bearbeitenBtn);
        topPanel.add(loeschenBtn);
        topPanel.add(importBtn);

        add(topPanel, BorderLayout.NORTH);

        // Rezeptliste (linke Seite)
        listModel = new DefaultListModel<>();
        rezeptListe = new JList<>(listModel);
        rezeptListe.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        rezeptListe.setCellRenderer(new RezeptListRenderer());

        JScrollPane listScroll = new JScrollPane(rezeptListe);
        listScroll.setPreferredSize(new Dimension(360, 0));

        detailPanel = new RezeptPanel();

        // SplitPane teilt die Ansicht in Liste und Detailbereich
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, listScroll, detailPanel);
        splitPane.setDividerLocation(380);
        add(splitPane, BorderLayout.CENTER);

        // Listener: Bei Auswahl eines Rezepts Detailansicht aktualisieren
        rezeptListe.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                detailPanel.showRezept(rezeptListe.getSelectedValue());
            }
        });
    }

    private void loadRezepte() {
        aktualisiereListe();
    }

    /**
     * Aktualisiert die Rezeptliste basierend auf Suche und Kategoriefilter.
     */
    private void aktualisiereListe() {
        listModel.clear();
        String suchtext = suchfeld.getText().trim().toLowerCase();
        String gewaehlteKat = (String) kategorieFilter.getSelectedItem();

        for (Rezept r : manager.getAllRezepte()) {
            boolean passtSuche = suchtext.isEmpty() ||
                r.getTitel().toLowerCase().contains(suchtext) ||
                r.getZutatenAlsText().toLowerCase().contains(suchtext);

            boolean passtKategorie = "Alle Kategorien".equals(gewaehlteKat) || r.getKategorie().equals(gewaehlteKat);

            if (passtSuche && passtKategorie) {
                listModel.addElement(r);
            }
        }
        aktualisiereKategorieFilter();
    }

    /**
     * Aktualisiert die Einträge im Kategorie-Filter (dynamisch).
     */
    private void aktualisiereKategorieFilter() {
        Set<String> kategorien = new HashSet<>();
        for (Rezept r : manager.getAllRezepte()) {
            kategorien.add(r.getKategorie());
        }

        String aktuell = (String) kategorieFilter.getSelectedItem();
        kategorieFilter.removeAllItems();
        kategorieFilter.addItem("Alle Kategorien");
        for (String k : kategorien) {
            kategorieFilter.addItem(k);
        }
        if (aktuell != null) kategorieFilter.setSelectedItem(aktuell);
    }

    private void openAddDialog() {
        AddEditDialog dialog = new AddEditDialog(this, null, manager, apiKey);
        dialog.setVisible(true);
        if (dialog.isSaved()) aktualisiereListe();
    }

    private void openEditDialog() {
        Rezept selected = rezeptListe.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Bitte zuerst ein Rezept auswählen!", "Hinweis", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        AddEditDialog dialog = new AddEditDialog(this, selected, manager, apiKey);
        dialog.setVisible(true);
        if (dialog.isSaved()) aktualisiereListe();
    }

    private void loeschen() {
        Rezept selected = rezeptListe.getSelectedValue();
        if (selected != null) {
            int confirm = JOptionPane.showConfirmDialog(this,
                "Rezept wirklich löschen?", "Bestätigen", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                manager.removeRezept(selected);
                aktualisiereListe();
            }
        }
    }

    /**
     * Importiert Rezepte aus einer JSON-Datei.
     * Optional mit automatischer Bildgenerierung.
     */
    private void importRezepte() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("JSON Dateien", "json"));

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();

            int choice = JOptionPane.showConfirmDialog(this,
                "Möchtest du für alle importierten Rezepte automatisch Bilder generieren?\n\n" +
                "Das kann länger dauern und verbraucht Credits.",
                "Bilder generieren?",
                JOptionPane.YES_NO_OPTION);

            boolean generateImages = (choice == JOptionPane.YES_OPTION);

            try {
                manager.importFromJson(file, generateImages, apiKey);
                aktualisiereListe();
                JOptionPane.showMessageDialog(this,
                    "Import abgeschlossen!",
                    "Erfolg", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                    "Fehler beim Import:\n" + ex.getMessage(),
                    "Fehler", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}