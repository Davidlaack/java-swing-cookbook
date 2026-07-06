package main.java.data;

import main.java.gui.ImageGenerator;
import main.java.model.Rezept;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;

/**
 * Zentrale Klasse zur Verwaltung aller Rezepte.
 * 
 * Verantwortlich für:
 * - Hinzufügen, Aktualisieren und Löschen von Rezepten
 * - Speicherung und Laden der Daten
 * - Import von Rezepten aus JSON-Dateien
 */
public class RezeptManager {

    // In-Memory-Liste aller Rezepte
    private List<Rezept> rezepte = new ArrayList<>();

    // Pfad zur Binärdatei, in der die Rezepte gespeichert werden
    private final String DATEI_PFAD = "data/rezepte.dat";

    /**
     * Konstruktor: Lädt beim Erstellen des Managers automatisch die gespeicherten Rezepte.
     */
    public RezeptManager() {
        laden();
    }

    /**
     * Fügt ein neues Rezept hinzu und speichert die Änderung sofort.
     */
    public void addRezept(Rezept rezept) {
        rezepte.add(rezept);
        speichern();
    }

    /**
     * Löscht ein Rezept und speichert die Änderung sofort.
     */
    public void removeRezept(Rezept rezept) {
        rezepte.remove(rezept);
        speichern();
    }

    /**
     * Gibt eine Kopie der aktuellen Rezeptliste zurück (defensive copy).
     */
    public List<Rezept> getAllRezepte() {
        return new ArrayList<>(rezepte);
    }

    /**
     * Speichert alle Rezepte in der Binärdatei (Serialisierung).
     */
    private void speichern() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATEI_PFAD))) {
            oos.writeObject(rezepte);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Lädt die Rezepte aus der Binärdatei beim Programmstart.
     */
    @SuppressWarnings("unchecked")
    private void laden() {
        File file = new File(DATEI_PFAD);
        if (!file.exists()) return;

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(DATEI_PFAD))) {
            rezepte = (List<Rezept>) ois.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Aktualisiert ein bestehendes Rezept.
     * Sucht das alte Rezept in der Liste und ersetzt es durch das neue.
     */
    public void updateRezept(Rezept alt, Rezept neu) {
        int index = rezepte.indexOf(alt);
        if (index != -1) {
            rezepte.set(index, neu);
            speichern();
        }
    }

    /**
     * Importiert eine Liste von Rezepten.
     * Prüft auf Duplikate (nach Titel) und speichert nur neue Rezepte.
     */
    public void importRezepte(List<Rezept> neueRezepte) {
        int added = 0;
        for (Rezept neu : neueRezepte) {
            // Duplikatprüfung nach Titel (case-insensitive)
            boolean exists = rezepte.stream()
                    .anyMatch(r -> r.getTitel().equalsIgnoreCase(neu.getTitel()));
           
            if (!exists) {
                rezepte.add(neu);
                added++;
            }
        }
        if (added > 0) {
            speichern();
        }
        System.out.println(added + " neue Rezepte importiert (" + (neueRezepte.size() - added) + " Duplikate übersprungen).");
    }

    /**
     * Importiert Rezepte aus einer JSON-Datei.
     * Optional mit automatischer Bildgenerierung für jedes neue Rezept.
     */
    public void importFromJson(File jsonFile, boolean generateImages, String apiKey) {
        try (Reader reader = new FileReader(jsonFile)) {
            Type type = new TypeToken<List<Rezept>>(){}.getType();
            List<Rezept> imported = new Gson().fromJson(reader, type);

            if (imported == null || imported.isEmpty()) return;

            int added = 0;
            ImageGenerator generator = generateImages && apiKey != null ? new ImageGenerator(apiKey) : null;

            for (Rezept neu : imported) {
                boolean exists = rezepte.stream()
                    .anyMatch(r -> r.getTitel().equalsIgnoreCase(neu.getTitel()));

                if (!exists) {
                    rezepte.add(neu);
                    added++;

                    // Optional: Bild automatisch generieren
                    if (generator != null) {
                        String imageName = neu.getTitel().toLowerCase().replaceAll("[^a-z0-9äöüß]", "_") + ".jpg";
                        String savePath = "data/images/" + imageName;

                        new File("data/images").mkdirs();

                        String generatedPath = generator.generateImage(neu.getTitel(), savePath);
                        if (generatedPath != null) {
                            neu.setBildPfad(generatedPath);
                        }
                    }
                }
            }

            if (added > 0) {
                speichern();
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Import fehlgeschlagen: " + e.getMessage());
        }
    }
}