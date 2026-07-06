package main.java.gui;

import main.java.model.Rezept;

import javax.swing.*;
import java.awt.*;

/**
 * Custom ListCellRenderer für die Darstellung der Rezepte in der JList (linke Seite).
 * 
 * Diese Klasse überschreibt das Standardverhalten von Swing, um jedes Rezept
 * in der Liste ansprechend und informativ darzustellen.
 */
public class RezeptListRenderer extends DefaultListCellRenderer {

    /**
     * Diese Methode wird von Swing für jedes sichtbare Listenelement aufgerufen.
     * 
     * @param list Die JList, in der das Element angezeigt wird
     * @param value Das Rezept-Objekt, das gerendert werden soll
     * @param index Der Index des Elements in der Liste
     * @param isSelected Ob das Element aktuell ausgewählt ist
     * @param cellHasFocus Ob das Element Fokus hat
     * @return Die Komponente (hier: this), die als Listenelement angezeigt wird
     */
    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value,
            int index, boolean isSelected, boolean cellHasFocus) {
       
        // Ruft die Standard-Implementierung auf
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

        // Prüft, ob das übergebene Objekt ein Rezept ist
        if (value instanceof Rezept rezept) {
            
            // HTML-Formatierung für eine ansprechende Darstellung
            // <b> für fetten Titel, <small> und <span> für Zusatzinfos
            String html = String.format(
                "<html><b style='font-size:13px'>%s</b><br>" +
                "<span style='color:#555; font-size:11px'>%s • %d Min • %d Portionen</span></html>",
                rezept.getTitel(),
                rezept.getKategorie(),
                rezept.getZubereitungszeit(),
                rezept.getPortionen()
            );

            setText(html);

            // Farbgebung je nach Auswahlzustand
            if (isSelected) {
                setBackground(new Color(70, 130, 180));   // Blaue Auswahlfarbe
                setForeground(Color.WHITE);
            } else {
                // Abwechselnde Zeilenfarben für bessere Lesbarkeit
                setBackground(index % 2 == 0 ? new Color(250, 250, 255) : Color.WHITE);
                setForeground(Color.BLACK);
            }

            // Abstände und Abstände zwischen Icon und Text
            setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
            setIconTextGap(10);
        }
        return this;   // Gibt sich selbst als Renderer zurück
    }
}