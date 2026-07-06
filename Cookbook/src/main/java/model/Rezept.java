package main.java.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Die zentrale Model-Klasse des Projekts.
 * Repräsentiert ein einzelnes Rezept mit allen relevanten Eigenschaften.
 * 
 * Diese Klasse ist serialisierbar, damit Rezepte dauerhaft gespeichert werden können.
 */
public class Rezept implements Serializable {

    // Serialisierung der Klasse 
    private static final long serialVersionUID = 1L;

    private String titel;
    private String kategorie;
    private int portionen;
    private int zubereitungszeit;
    private List<String> zutaten;
    private String zubereitung;
    private String bildPfad;           // entweder URL oder lokaler Pfad zum Bild
    private String notizen;

    /**
     * Standard-Konstruktor für neue Rezepte.
     * Initialisiert die Zutaten-Liste.
     */
    public Rezept() {
        this.zutaten = new ArrayList<>();
    }

    /**
     * Vollständiger Konstruktor.
     * Wird beim Anlegen oder Bearbeiten eines Rezepts verwendet.
     */
    public Rezept(String titel, String kategorie, int portionen, int zubereitungszeit,
                  List<String> zutaten, String zubereitung, String bildPfad, String notizen) {
        this.titel = titel;
        this.kategorie = kategorie;
        this.portionen = portionen;
        this.zubereitungszeit = zubereitungszeit;
        this.zutaten = zutaten != null ? new ArrayList<>(zutaten) : new ArrayList<>();
        this.zubereitung = zubereitung;
        this.bildPfad = bildPfad;
        this.notizen = notizen;
    }

    // === Getter und Setter ===

    public String getTitel() { return titel; }
    public void setTitel(String titel) { this.titel = titel; }

    public String getKategorie() { return kategorie; }
    public void setKategorie(String kategorie) { this.kategorie = kategorie; }

    public int getPortionen() { return portionen; }
    public void setPortionen(int portionen) { this.portionen = portionen; }

    public int getZubereitungszeit() { return zubereitungszeit; }
    public void setZubereitungszeit(int zubereitungszeit) { this.zubereitungszeit = zubereitungszeit; }

    /**
     * Gibt eine Kopie der Zutaten-Liste zurück (defensive copy),
     * damit externe Änderungen die interne Liste nicht beeinflussen.
     */
    public List<String> getZutaten() { return new ArrayList<>(zutaten); }

    public void setZutaten(List<String> zutaten) {
        this.zutaten = zutaten != null ? new ArrayList<>(zutaten) : new ArrayList<>();
    }

    public String getZubereitung() { return zubereitung; }
    public void setZubereitung(String zubereitung) { this.zubereitung = zubereitung; }

    public String getBildPfad() { return bildPfad; }
    public void setBildPfad(String bildPfad) { this.bildPfad = bildPfad; }

    public String getNotizen() { return notizen; }
    public void setNotizen(String notizen) { this.notizen = notizen; }

    /**
     * Hilfsmethode: Gibt alle Zutaten als einen String zurück (eine pro Zeile).
     * Wird in der GUI und beim Export verwendet.
     */
    public String getZutatenAlsText() {
        return String.join("\n", zutaten);
    }

    /**
     * Wird von der JList verwendet, um den Titel des Rezepts anzuzeigen.
     */
    @Override
    public String toString() {
        return titel;
    }
}