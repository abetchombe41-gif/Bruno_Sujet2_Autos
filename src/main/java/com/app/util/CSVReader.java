package com.app.util;

import com.app.model.*;
import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CSVReader {
    public static List<Annonce> chargerAnnonces(InputStream inputStream) {
        List<Annonce> liste = new ArrayList<>();
        if (inputStream == null) return liste;
        
        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"))) {
            String ligne = br.readLine(); // Ignore la première ligne (en-tête)
            while ((ligne = br.readLine()) != null) {
                if (ligne.isBlank()) continue;
                String[] tokens = ligne.split(",");
                if (tokens.length >= 13) {
                    liste.add(new Annonce(
                        tokens[0].trim(),                          // id
                        tokens[1].trim(),                          // marque
                        tokens[2].trim(),                          // modele
                        Integer.parseInt(tokens[3].trim()),         // anneeModele
                        Integer.parseInt(tokens[4].trim()),         // kilometrage
                        Double.parseDouble(tokens[5].trim()),       // prix
                        TypeCarburant.valueOf(tokens[6].trim().toUpperCase()), // carburant
                        Transmission.valueOf(tokens[7].trim().toUpperCase()),   // transmission
                        tokens[8].trim(),                          // couleur
                        tokens[9].trim(),                          // ville
                        LocalDate.parse(tokens[11].trim()),        // datePublication (index 11)
                        tokens[12].trim()                          // description (index 12)
                    ));
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la lecture du fichier CSV : " + e.getMessage());
            e.printStackTrace();
        }
        return liste;
    }
}
