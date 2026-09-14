package com.app.util;

import com.app.model.*;
import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CSVReader {
    public static List<Annonce> chargerAnnonces(InputStream inputStream) {
        List<Annonce> liste = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            String ligne = br.readLine(); // Ignore la ligne d'en-tête
            while ((ligne = br.readLine()) != null) {
                String[] tokens = ligne.split(",");
                if (tokens.length >= 13) {
                    liste.add(new Annonce(
                        tokens[0], tokens[1], tokens[2],
                        Integer.parseInt(tokens[3]), Integer.parseInt(tokens[4]),
                        Double.parseDouble(tokens[5]),
                        TypeCarburant.valueOf(tokens[6]), Transmission.valueOf(tokens[7]),
                        tokens[8], tokens[9], LocalDate.parse(tokens[11]), tokens[12]
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return liste;
    }
}
