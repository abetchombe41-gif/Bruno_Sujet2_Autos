package com.app.controller;

import com.app.model.*;
import com.app.util.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.util.Comparator;
import java.util.List;

public class MainController {

    // Éléments de l'interface
    @FXML private TextField txtRecherche;
    @FXML private ComboBox<String> comboMarque;
    @FXML private Slider sliderPrixMax;
    @FXML private ComboBox<String> comboTri;
    @FXML private ListView<Annonce> listAnnonces;
    @FXML private ListView<Annonce> listFavoris;

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
        //Chargement du fichier CSV depuis les ressources
        var is = getClass().getResourceAsStream("/com/app/data/annonces.csv");
        if (is != null) {
            manager.setToutesLesAnnonces(CSVReader.chargerAnnonces(is));
        }

        //les ComboBox avec les choix de base requis
        comboMarque.getItems().addAll("Toutes", "Toyota", "Honda", "Tesla", "Ford", "BMW");
        comboMarque.setValue("Toutes");

        comboTri.getItems().addAll("Prix croissant", "Prix décroissant", "Année (Plus récent)");
        comboTri.setValue("Prix croissant");

        // Écouter les changements des filtres pour rafraîchir en temps réel
        txtRecherche.textProperty().addListener((obs, old, nv) -> { pageActuelle = 0; rafraichirDonnees(); });
        comboMarque.valueProperty().addListener((obs, old, nv) -> { pageActuelle = 0; rafraichirDonnees(); });
        sliderPrixMax.valueProperty().addListener((obs, old, nv) -> { pageActuelle = 0; rafraichirDonnees(); });
        comboTri.valueProperty().addListener((obs, old, nv) -> rafraichirDonnees());

        // Écouter la sélection d'une voiture dans la liste pour afficher ses détails
        listAnnonces.getSelectionModel().selectedItemProperty().addListener((obs, old, selection) -> afficherDetails(selection));

        // Affichage au démarrage
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
        if (a == null) return;
        lblTitre.setText(a.getMarque() + " " + a.getModele() + " (" + a.getAnneeModele() + ")");
        lblPrix.setText(a.getPrix() + " $");
        lblKm.setText(a.getKilometrage() + " km");
        lblCarburant.setText("Carburant : " + a.getCarburant());
        lblTransmission.setText("Boîte : " + a.getTransmission());
        lblVille.setText("Ville : " + a.getVille());
        lblDescription.setText(a.getDescription());
        
        // Calcul du prix au kilomètre exigé (Exigence 4.4)
        lblPrixKm.setText(String.format("Rapport Prix/Km : %.3f $ / km", a.getPrixAuKilometre()));
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
