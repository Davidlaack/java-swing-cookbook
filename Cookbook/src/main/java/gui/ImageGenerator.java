package main.java.gui;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Hilfsklasse zur Generierung von Bildern mithilfe der Flux-KI (fal.ai API).
 * 
 * Diese Klasse ist verantwortlich für:
 * - Erstellung eines optimierten Prompts aus dem Rezepttitel
 * - Senden der Anfrage an die Flux-API
 * - Herunterladen und lokales Speichern des generierten Bildes
 */
public class ImageGenerator {

    // API-Endpunkt für das Flux Dev Modell (günstiger und schneller als Flux Pro)
    private static final String FAL_API_URL = "https://fal.run/fal-ai/flux/dev";

    // Der API-Key von fal.ai (wird beim Erstellen des Objekts übergeben)
    private final String apiKey;

    //Konstruktor
    public ImageGenerator(String apiKey) {
        this.apiKey = apiKey;
    }

    /**
     * Generiert ein Bild für ein Rezept und speichert es lokal.
     * 
     * @param recipeTitle Der Titel des Rezepts (wird für den Prompt verwendet)
     * @param savePath Der vollständige Pfad, unter dem das Bild gespeichert werden soll
     * @return Der Pfad zum gespeicherten Bild oder null bei Fehler
     */
    public String generateImage(String recipeTitle, String savePath) {
        try {
            // Erstellung eines optimierten Prompts
            String prompt = "High quality food photography of " + recipeTitle +
                          " (german food title), professional culinary photo, bright lighting, realistic presentation";

            // JSON-Body für die API-Anfrage
            String jsonInput = """
                {
                  "prompt": "%s",
                  "image_size": "landscape_4_3",
                  "num_inference_steps": 20,
                  "seed": 42
                }
                """.formatted(prompt.replace("\"", "\\\""));

            // HTTP-Verbindung zur fal.ai API aufbauen
            URL url = new URL(FAL_API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Key " + apiKey);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            // JSON-Daten senden
            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonInput.getBytes("UTF-8"));
            }

            // Antwort-Code prüfen
            int responseCode = conn.getResponseCode();
            System.out.println("Response Code: " + responseCode);

            if (responseCode == 200) {
                // JSON-Antwort der API lesen
                StringBuilder response = new StringBuilder();
                try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line);
                    }
                }

                // Bild-URL aus der JSON-Antwort extrahieren
                JsonObject json = JsonParser.parseString(response.toString()).getAsJsonObject();
                String imageUrl = json.get("images").getAsJsonArray().get(0).getAsJsonObject()
                        .get("url").getAsString();

                System.out.println("Bild-URL erhalten: " + imageUrl);

                // Bild von der zurückgegebenen URL herunterladen
                URL imgUrl = new URL(imageUrl);
                try (InputStream in = imgUrl.openStream();
                     FileOutputStream out = new FileOutputStream(savePath)) {
                    in.transferTo(out);
                }

                System.out.println("Bild erfolgreich gespeichert: " + savePath);
                return savePath;
            } else {
                // Fehler-Details ausgeben
                System.out.println("Fehler-Response:");
                try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream()))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        System.out.println(line);
                    }
                }
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}