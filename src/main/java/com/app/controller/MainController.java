package com.app.controller;

import com.app.model.*;
import com.app.util.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.concurrent.Task;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import javafx.scene.image.ImageView;


public class MainController {

    // Éléments de l'interface
    @FXML private TextField txtRecherche;
    @FXML private ComboBox<String> comboMarque;
    @FXML private Slider sliderPrixMax;
    @FXML private ComboBox<String> comboTri;
    @FXML private ListView<Annonce> listAnnonces;
    @FXML private ListView<Annonce> listFavoris;
    @FXML private ImageView imgVoiture;


    // Éléments du panneau détail
    @FXML private Label lblTitre;
    @FXML private Label lblPrix;
    @FXML private Label lblKm;
    @FXML private Label lblCarburant;
    @FXML private Label lblTransmission;
    @FXML private Label lblVille;
    @FXML private Label lblPrixKm;
    @FXML private Label lblDescription;
    @FXML private Label lblBenchmark;

    private CatalogueManager manager = new CatalogueManager();
    private List<Annonce> listeFiltreeEtTriee;
    private int pageActuelle = 0;

@FXML
public void initialize() {
    // 1. Charger le fichier CSV depuis les ressources du projet
    var is = getClass().getResourceAsStream("/com/app/data/annonces.csv");
    if (is != null) {
        manager.setToutesLesAnnonces(CSVReader.chargerAnnonces(is));
    }

    // 2. Extraire dynamiquement TOUTES les marques uniques du CSV
    List<String> marquesUniques = manager.getToutesLesAnnonces().stream()
                                         .map(Annonce::getMarque)
                                         .distinct()
                                         .sorted()
                                         .collect(java.util.stream.Collectors.toList());

    // 3. Remplir la ComboBox des marques avec la liste dynamique
    comboMarque.getItems().clear();
    comboMarque.getItems().add("Toutes");
    comboMarque.getItems().addAll(marquesUniques);
    comboMarque.setValue("Toutes");

    // 4. CONSERVER VOS OPTIONS DE TRI EXISTANTES (Prix croissant, décroissant, année)
    comboTri.getItems().clear();
    comboTri.getItems().addAll("Prix croissant", "Prix décroissant", "Année (Plus récent)");
    comboTri.setValue("Prix croissant");

    // 5. Écouter les changements des filtres pour rafraîchir en temps réel
    txtRecherche.textProperty().addListener((obs, old, nv) -> { pageActuelle = 0; rafraichirDonnees(); });
    comboMarque.valueProperty().addListener((obs, old, nv) -> { pageActuelle = 0; rafraichirDonnees(); });
    sliderPrixMax.valueProperty().addListener((obs, old, nv) -> { pageActuelle = 0; rafraichirDonnees(); });
    comboTri.valueProperty().addListener((obs, old, nv) -> rafraichirDonnees());

    // 6. Écouter la sélection d'une voiture dans la liste pour afficher ses détails
    listAnnonces.getSelectionModel().selectedItemProperty().addListener((obs, old, selection) -> afficherDetails(selection));

    // Premier affichage au démarrage
    rafraichirDonnees();
}


    private void rafraichirDonnees() {
        // 1. Appliqucation des filtres combinés en "ET"
        listeFiltreeEtTriee = manager.filtrerEtRechercher(
            txtRecherche.getText(),
            comboMarque.getValue(),
            sliderPrixMax.getValue(),
            null // On filtre sur la transmission si désiré
        );

        // 2. Choisir le comparateur selon le tri sélectionné
        Comparator<Annonce> comp = Comparator.comparingDouble(Annonce::getPrix);
        if ("Prix décroissant".equals(comboTri.getValue())) {
            comp = Comparator.comparingDouble(Annonce::getPrix).reversed();
        } else if ("Année (Plus récent)".equals(comboTri.getValue())) {
            comp = Comparator.comparingInt(Annonce::getAnneeModele).reversed();
        }

        // 3. BENCHMARK OBLIGATOIRE: Comparer Bubble vs Merge sur la liste filtrée
        List<Annonce> copiePourBubble = new java.util.ArrayList<>(listeFiltreeEtTriee);
        long debutBubble = System.nanoTime();
        SortingAlgorithms.bubbleSort(copiePourBubble, comp);
        long finBubble = System.nanoTime();

        long debutMerge = System.nanoTime();
        SortingAlgorithms.mergeSort(listeFiltreeEtTriee, comp);
        long finMerge = System.nanoTime();

        // Affichage du résultat du benchmark en millisecondes ou microsecondes
        lblBenchmark.setText(String.format("Benchmark - Bubble: %.2f ms | Merge: %.2f ms", 
            (finBubble - debutBubble) / 1_000_000.0, 
            (finMerge - debutMerge) / 1_000_000.0));

        // 4. Application de la pagination de 25 éléments et misae à jour de la vue
        List<Annonce> pageVisuelle = manager.obtenirPage(listeFiltreeEtTriee, pageActuelle);
        listAnnonces.getItems().setAll(pageVisuelle);
    }

private void afficherDetails(Annonce a) {
    if (a == null) {
        imgVoiture.setImage(null);
        return;
    }

    // 1. Remplissage de vos étiquettes de texte existantes
    lblTitre.setText(a.getMarque() + " " + a.getModele() + " (" + a.getAnneeModele() + ")");
    lblPrix.setText(a.getPrix() + " $");
    lblKm.setText(a.getKilometrage() + " km");
    lblCarburant.setText("Carburant : " + a.getCarburant());
    lblTransmission.setText("Boîte : " + a.getTransmission());
    lblVille.setText("Ville : " + a.getVille());
    lblDescription.setText(a.getDescription());
    lblPrixKm.setText(String.format("Rapport Prix/Km : %.3f $ / km", a.getPrixAuKilometre()));

    imgVoiture.setImage(creerImageVehicule(a));
    chargerPhotoReelle(a);
}

private void chargerPhotoReelle(Annonce annonce) {
    Task<Image> recherche = new Task<>() {
        @Override
        protected Image call() throws Exception {
            String url = trouverPhotoWikimedia(annonce, true);
            if (url == null) {
                url = trouverPhotoWikimedia(annonce, false);
            }
            return url == null ? null : new Image(url, 260, 160, true, true, true);
        }
    };

    recherche.setOnSucceeded(event -> {
        Image image = recherche.getValue();
        Annonce encoreSelectionnee = listAnnonces.getSelectionModel().getSelectedItem();
        if (image != null && !image.isError() && encoreSelectionnee == annonce) {
            imgVoiture.setImage(image);
        }
    });

    Thread thread = new Thread(recherche, "recherche-photo-vehicule");
    thread.setDaemon(true);
    thread.start();
}

private String trouverPhotoWikimedia(Annonce annonce, boolean recherchePrecise) throws Exception {
    String recherche = annonce.getMarque() + " " + annonce.getModele();
    if (recherchePrecise) {
        recherche += " " + annonce.getAnneeModele() + " " + annonce.getCouleur();
    }

    String api = "https://commons.wikimedia.org/w/api.php?action=query"
        + "&generator=search&gsrsearch="
        + URLEncoder.encode(recherche + " car", StandardCharsets.UTF_8)
        + "&gsrnamespace=6&gsrlimit=1&prop=imageinfo&iiprop=url"
        + "&iiurlwidth=520&format=json";
    HttpRequest request = HttpRequest.newBuilder(URI.create(api))
        .header("User-Agent", "Lab2VoituresOccasion/1.0")
        .GET()
        .build();
    HttpResponse<String> response = HttpClient.newHttpClient()
        .send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

    java.util.regex.Matcher matcher = java.util.regex.Pattern
        .compile("\\\"thumburl\\\":\\\"([^\\\"]+)")
        .matcher(response.body());
    if (!matcher.find()) {
        return null;
    }
    return matcher.group(1).replace("\\/", "/").replace("\\u0026", "&");
    }

private WritableImage creerImageVehicule(Annonce a) {
    double largeur = 520;
    double hauteur = 320;
    Canvas canvas = new Canvas(largeur, hauteur);
    GraphicsContext gc = canvas.getGraphicsContext2D();

    gc.setFill(Color.web("#eef3f7"));
    gc.fillRect(0, 0, largeur, hauteur);
    gc.setFill(Color.web("#d7e1e8"));
    gc.fillRect(0, 218, largeur, 102);

    gc.setFill(couleurVehicule(a.getCouleur()));
    gc.fillRoundRect(62, 145, 396, 82, 22, 22);
    gc.fillRoundRect(145, 103, 218, 76, 30, 30);

    gc.setFill(Color.web("#b9d7e8"));
    gc.fillRoundRect(166, 112, 82, 48, 14, 14);
    gc.fillRoundRect(258, 112, 85, 48, 14, 14);

    gc.setFill(Color.web("#202a33"));
    gc.fillOval(105, 194, 58, 58);
    gc.fillOval(357, 194, 58, 58);
    gc.setFill(Color.web("#aeb8bf"));
    gc.fillOval(119, 208, 30, 30);
    gc.fillOval(371, 208, 30, 30);

    gc.setFill(Color.web("#fff4b8"));
    gc.fillRoundRect(68, 165, 24, 18, 6, 6);
    gc.fillRoundRect(428, 165, 24, 18, 6, 6);

    gc.setFill(Color.web("#17232d"));
    gc.setFont(Font.font("Arial", 22));
    gc.fillText(a.getMarque() + " " + a.getModele(), 24, 38);
    gc.setFont(Font.font("Arial", 16));
    gc.fillText("ID " + a.getId() + "  |  " + a.getAnneeModele() + "  |  " + a.getCouleur(), 24, 65);
    gc.setFont(Font.font("Arial", 14));
    gc.fillText(a.getCarburant() + "  |  " + a.getTransmission() + "  |  " + a.getKilometrage() + " km", 24, 88);

    WritableImage image = new WritableImage((int) largeur, (int) hauteur);
    canvas.snapshot(null, image);
    return image;
}

private Color couleurVehicule(String couleur) {
    return switch (couleur.toLowerCase(Locale.ROOT)) {
        case "blanc" -> Color.WHITE;
        case "noir" -> Color.web("#252a2f");
        case "gris" -> Color.web("#737d86");
        case "argent" -> Color.web("#b8c0c8");
        case "bleu" -> Color.web("#2867a8");
        case "rouge" -> Color.web("#bb3038");
        default -> Color.web("#4d7a91");
    };
}


    @FXML
    public void actionAjouterFavoris() {
        Annonce selectionnee = listAnnonces.getSelectionModel().getSelectedItem();
        if (selectionnee != null) {
            manager.ajouterAuxFavoris(selectionnee);
            listFavoris.getItems().setAll(manager.getFavoris());
        }
    }

    @FXML
    public void actionRetirerFavoris() {
        Annonce selectionnee = listFavoris.getSelectionModel().getSelectedItem();
        if (selectionnee != null) {
            manager.retirerDesFavoris(selectionnee);
            listFavoris.getItems().setAll(manager.getFavoris());
        }
    }

    @FXML public void pageSuivante() { if ((pageActuelle + 1) * 25 < listeFiltreeEtTriee.size()) { pageActuelle++; rafraichirDonnees(); } }
    @FXML public void pagePrecedente() { if (pageActuelle > 0) { pageActuelle--; rafraichirDonnees(); } }
}
