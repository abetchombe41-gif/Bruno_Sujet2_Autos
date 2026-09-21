package com.app.controller;

import com.app.dao.AnnonceDAO;
import com.app.dao.AnnonceDAOPostgreSQL;
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
import javafx.scene.image.ImageView;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class MainController {

    // Éléments de l'interface graphique (liés au fichier FXML)
    @FXML private TextField txtRecherche;
    @FXML private ComboBox<String> comboMarque;
    @FXML private Slider sliderPrixMax;
    @FXML private ComboBox<String> comboTri;
    @FXML private ListView<Annonce> listAnnonces;
    @FXML private ListView<Annonce> listFavoris;
    @FXML private ImageView imgVoiture;

    // Éléments textuels du panneau latéral de détails
    @FXML private Label lblTitre;
    @FXML private Label lblPrix;
    @FXML private Label lblKm;
    @FXML private Label lblCarburant;
    @FXML private Label lblTransmission;
    @FXML private Label lblVille;
    @FXML private Label lblPrixKm;
    @FXML private Label lblDescription;
    @FXML private Label lblBenchmark;

    // Gestionnaires de données et d'état de l'application
    private CatalogueManager manager = new CatalogueManager();
    private List<Annonce> listeFiltreeEtTriee;
    private int pageActuelle = 0;

    // Couche d'abstraction DAO (Règle 5 du Lab 3 : Aucun SQL en dehors de cette implémentation)
    private final AnnonceDAO dao = new AnnonceDAOPostgreSQL();    

    @FXML
    public void initialize() {
        // Chargement initial depuis PostgreSQL avec sécurité intégrée (Section 4.6)
        synchroniserBaseDeDonnees();

        // Ajout des écouteurs (Listeners) pour l'exécution des filtres cumulatifs en temps réel
        txtRecherche.textProperty().addListener((obs, old, nv) -> { pageActuelle = 0; rafraichirDonnees(); });
        comboMarque.valueProperty().addListener((obs, old, nv) -> { pageActuelle = 0; rafraichirDonnees(); });
        sliderPrixMax.valueProperty().addListener((obs, old, nv) -> { pageActuelle = 0; rafraichirDonnees(); });
        comboTri.valueProperty().addListener((obs, old, nv) -> rafraichirDonnees());

        // Écouteur de sélection de ligne pour la mise à jour dynamique du panneau d'affichage
        listAnnonces.getSelectionModel().selectedItemProperty().addListener((obs, old, selection) -> afficherDetails(selection));
    }

    private void synchroniserBaseDeDonnees() {
        try {
            // Lecture exclusive via l'interface DAO de la base de données relationnelle
            List<Annonce> annoncesBase = dao.trouverTous();
            manager.setToutesLesAnnonces(annoncesBase);
        } catch (Exception e) {
            System.err.println("Avertissement : Serveur de base de données PostgreSQL inaccessible. Repli sur le CSV local.");
            var is = getClass().getResourceAsStream("/com/app/data/annonces.csv");
            if (is != null) {
                manager.setToutesLesAnnonces(CSVReader.chargerAnnonces(is));
            }
        }

        // Extraction dynamique de l'ensemble des marques uniques pour la ComboBox (Section 4.5)
        List<String> marquesUniques = manager.getToutesLesAnnonces().stream()
                                             .map(Annonce::getMarque)
                                             .distinct()
                                             .sorted()
                                             .collect(java.util.stream.Collectors.toList());

        comboMarque.getItems().clear();
        comboMarque.getItems().add("Toutes");
        comboMarque.getItems().addAll(marquesUniques);
        comboMarque.setValue("Toutes");

        // Initialisation des critères de tri requis
        comboTri.getItems().clear();
        comboTri.getItems().addAll("Prix croissant", "Prix décroissant", "Année (Plus récent)");
        comboTri.setValue("Prix croissant");

        rafraichirDonnees();
    }

    private void rafraichirDonnees() {
        // 1. Application des filtres cumulatifs en logique "ET" (Laboratoire 2)
        listeFiltreeEtTriee = manager.filtrerEtRechercher(
            txtRecherche.getText(),
            comboMarque.getValue(),
            sliderPrixMax.getValue(),
            null
        );

        // 2. Détermination du comparateur selon le critère de tri sélectionné
        Comparator<Annonce> comp = Comparator.comparingDouble(Annonce::getPrix);
        if ("Prix décroissant".equals(comboTri.getValue())) {
            comp = Comparator.comparingDouble(Annonce::getPrix).reversed();
        } else if ("Année (Plus récent)".equals(comboTri.getValue())) {
            comp = Comparator.comparingInt(Annonce::getAnneeModele).reversed();
        }

        // 3. Exécution obligatoire du Benchmark de performance (Tris codés à la main)
        List<Annonce> copiePourBubble = new java.util.ArrayList<>(listeFiltreeEtTriee);
        long debutBubble = System.nanoTime();
        SortingAlgorithms.bubbleSort(copiePourBubble, comp);
        long finBubble = System.nanoTime();

        long debutMerge = System.nanoTime();
        SortingAlgorithms.mergeSort(listeFiltreeEtTriee, comp);
        long finMerge = System.nanoTime();

        // Mise à jour de l'affichage du Benchmark chiffré (Section 4.1 du Rapport)
        lblBenchmark.setText(String.format("Benchmark - Bubble: %.2f ms | Merge: %.2f ms", 
            (finBubble - debutBubble) / 1_000_000.0, 
            (finMerge - debutMerge) / 1_000_000.0));

        // 4. Segmentation pour la pagination stricte par tranches fixes de 25 éléments (Test #8)
        List<Annonce> pageVisuelle = manager.obtenirPage(listeFiltreeEtTriee, pageActuelle);
        listAnnonces.getItems().setAll(pageVisuelle);
    }

    private void afficherDetails(Annonce a) {
        if (a == null) {
            imgVoiture.setImage(null);
            return;
        }

        // Remplissage des étiquettes d'informations du véhicule sélectionné
        lblTitre.setText(a.getMarque() + " " + a.getModele() + " (" + a.getAnneeModele() + ")");
        lblPrix.setText(a.getPrix() + " $");
        lblKm.setText(a.getKilometrage() + " km");
        lblCarburant.setText("Carburant : " + a.getCarburant());
        lblTransmission.setText("Boîte : " + a.getTransmission());
        lblVille.setText("Ville : " + a.getVille());
        lblDescription.setText(a.getDescription());
        
        // Richesse UI : Calcul et affichage en temps réel du coût kilométrique (Exigence 4.4)
        lblPrixKm.setText(String.format("Rapport Prix/Km : %.3f $ / km", a.getPrixAuKilometre()));

        // Double moteur d'imagerie : Affichage du Canvas vectoriel puis appel asynchrone Wikimedia
        imgVoiture.setImage(creerImageVehicule(a));
        chargerPhotoReelle(a);
    }

    private void chargerPhotoReelle(Annonce annonce) {
        // Tâche asynchrone en arrière-plan pour interroger l'API distante sans figer l'interface (Task)
        WritableImage imageSecours = creerImageVehicule(annonce);
        Task<Image> recherche = new Task<>() {
            @Override
            protected Image call() throws Exception {
                String url = trouverPhotoWikimedia(annonce, true);
                if (url == null) {
                    url = trouverPhotoWikimedia(annonce, false);
                }
                // Le chargement est bloquant dans ce thread : on ne place jamais une image encore vide dans l'ImageView.
                return url == null ? null : new Image(url, 260, 160, true, true, false);
            }
        };

        recherche.setOnSucceeded(event -> {
            Image image = recherche.getValue();
            Annonce encoreSelectionnee = listAnnonces.getSelectionModel().getSelectedItem();
            // Sécurité : On vérifie que l'utilisateur n'a pas changé de ligne entre-temps
            if (image != null && !image.isError() && encoreSelectionnee == annonce) {
                imgVoiture.setImage(image);
            } else if (encoreSelectionnee == annonce) {
                imgVoiture.setImage(imageSecours);
            }
        });

        recherche.setOnFailed(event -> {
            if (listAnnonces.getSelectionModel().getSelectedItem() == annonce) {
                imgVoiture.setImage(imageSecours);
            }
        });

        Thread thread = new Thread(recherche, "recherche-photo-vehicule");
        thread.setDaemon(true); // Fermeture propre du thread si l'application s'arrête
        thread.start();
    }

    private String trouverPhotoWikimedia(Annonce annonce, boolean recherchePrecise) throws Exception {
        String recherche = annonce.getMarque() + " " + annonce.getModele();
        if (recherchePrecise) {
            recherche += " " + annonce.getAnneeModele();
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

        // Regex d'extraction robuste de l'URL de la miniature JSON générée par Wikimedia
        java.util.regex.Matcher matcher = java.util.regex.Pattern
            .compile("\"thumburl\"\\s*:\\s*\"([^\"]+)\"")
            .matcher(response.body());
            
        if (!matcher.find()) {
            return null;
        }
        return matcher.group(1).replace("\\/", "/").replace("\\u0026", "&");
    }

    private WritableImage creerImageVehicule(Annonce a) {
        // Moteur de rendu graphique vectoriel basé sur un composant Canvas
        double largeur = 520;
        double hauteur = 320;
        Canvas canvas = new Canvas(largeur, hauteur);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Dessin du fond et du sol
        gc.setFill(Color.web("#eef3f7"));
        gc.fillRect(0, 0, largeur, hauteur);
        gc.setFill(Color.web("#d7e1e8"));
        gc.fillRect(0, 218, largeur, 102);

        // Dessin de la carrosserie adaptée dynamiquement à la couleur de l'annonce
        gc.setFill(couleurVehicule(a.getCouleur()));
        gc.fillRoundRect(62, 145, 396, 82, 22, 22);
        gc.fillRoundRect(145, 103, 218, 76, 30, 30);

        // Dessin des vitres (Bleu ciel texturé)
        gc.setFill(Color.web("#b9d7e8"));
        gc.fillRoundRect(166, 112, 82, 48, 14, 14);
        gc.fillRoundRect(258, 112, 85, 48, 14, 14);

        // Dessin des roues et des jantes argentées
        gc.setFill(Color.web("#202a33"));
        gc.fillOval(105, 194, 58, 58);
        gc.fillOval(357, 194, 58, 58);
        gc.setFill(Color.web("#aeb8bf"));
        gc.fillOval(119, 208, 30, 30);
        gc.fillOval(371, 208, 30, 30);

        // Dessin des phares fonctionnels
        gc.setFill(Color.web("#fff4b8"));
        gc.fillRoundRect(68, 165, 24, 18, 6, 6);
        gc.fillRoundRect(428, 165, 24, 18, 6, 6);

        // Dessin des métadonnées du véhicule directement incrustées sur l'image Canvas
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
        if (couleur == null) return Color.web("#4d7a91");
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

    // =========================================================================
    // OPÉRATIONS VISUELLES CRUD DE PERSISTENCE (Laboratoire 3 - Étape 6)
    // =========================================================================

    @FXML
    public void actionAjouter() {
        // Affichage de la boîte de dialogue de saisie d'ajout
        Dialog<Annonce> dialog = creerFormulaireVoiture(null);
        dialog.showAndWait().ifPresent(nouvelleAnnonce -> {
            try {
                dao.ajouter(nouvelleAnnonce); // Envoi au DAO PostgreSQL
                synchroniserBaseDeDonnees(); // Rafraîchissement automatique et forcé de la vue
            } catch (Exception e) {
                afficherFenetreErreur("Échec de l'ajout", "Impossible d'insérer la ligne en base de données : " + e.getMessage());
            }
        });
    }

    @FXML
    public void actionModifier() {
        Annonce selectionnee = listAnnonces.getSelectionModel().getSelectedItem();
        if (selectionnee == null) {
            afficherFenetreErreur("Sélection manquante", "Veuillez sélectionner un véhicule à modifier dans la liste centrale.");
            return;
        }

        // Affichage de la boîte de dialogue pré-remplie pour édition
        Dialog<Annonce> dialog = creerFormulaireVoiture(selectionnee);
        dialog.showAndWait().ifPresent(annonceModifiee -> {
            try {
                dao.modifier(annonceModifiee); // Envoi de la mise à jour (UPDATE SQL)
                synchroniserBaseDeDonnees();
            } catch (Exception e) {
                afficherFenetreErreur("Échec de la modification", "Impossible d'altérer la ligne SQL : " + e.getMessage());
            }
        });
    }

    @FXML
    public void actionSupprimer() {
        Annonce selectionnee = listAnnonces.getSelectionModel().getSelectedItem();
        if (selectionnee == null) {
            afficherFenetreErreur("Sélection manquante", "Veuillez sélectionner un véhicule à supprimer définitivement.");
            return;
        }

        // Fenêtre de dialogue de confirmation d'effacement (Section 4.6)
        Alert conf = new Alert(Alert.AlertType.CONFIRMATION);
        conf.setTitle("Validation requise");
        conf.setHeaderText("Supprimer définitivement cette annonce de la base de données PostgreSQL ?");
        conf.setContentText(selectionnee.getMarque() + " " + selectionnee.getModele() + " (ID: " + selectionnee.getId() + ")");

        if (conf.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                dao.supprimer(selectionnee.getId()); // Envoi de l'ordre d'effacement (DELETE SQL)
                synchroniserBaseDeDonnees();
            } catch (Exception e) {
                afficherFenetreErreur("Échec de la suppression", "Erreur d'intégrité ou contrainte relationnelle SQL : " + e.getMessage());
            }
        }
    }

    private Dialog<Annonce> creerFormulaireVoiture(Annonce existante) {
        Dialog<Annonce> dialog = new Dialog<>();
        dialog.setTitle(existante == null ? "Création d'Annonce" : "Mise à jour d'Annonce");
        ButtonType btnEnregistrer = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnEnregistrer, ButtonType.CANCEL);

        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10); grid.setVgap(10);

        TextField txtId = new TextField(); txtId.setPromptText("ID unique");
        TextField txtMarque = new TextField(); txtMarque.setPromptText("Ex: Toyota");
        TextField txtModele = new TextField(); txtModele.setPromptText("Ex: Corolla");
        TextField txtAnnee = new TextField(); txtAnnee.setPromptText("Ex: 2018");
        TextField txtPrix = new TextField(); txtPrix.setPromptText("Ex: 16500");
        TextField txtKm = new TextField(); txtKm.setPromptText("Ex: 85000");

        // Pré-remplissage des champs si on est en mode modification
        if (existante != null) {
            txtId.setText(existante.getId()); txtId.setDisable(true); // Clé primaire non modifiable
            txtMarque.setText(existante.getMarque());
            txtModele.setText(existante.getModele());
            txtAnnee.setText(String.valueOf(existante.getAnneeModele()));
            txtPrix.setText(String.valueOf(existante.getPrix()));
            txtKm.setText(String.valueOf(existante.getKilometrage()));
        }

        grid.add(new Label("ID unique :"), 0, 0); grid.add(txtId, 1, 0);
        grid.add(new Label("Marque :"), 0, 1); grid.add(txtMarque, 1, 1);
        grid.add(new Label("Modèle :"), 0, 2); grid.add(txtModele, 1, 2);
        grid.add(new Label("Année-Modèle :"), 0, 3); grid.add(txtAnnee, 1, 3);
        grid.add(new Label("Prix ($) :"), 0, 4); grid.add(txtPrix, 1, 4);
        grid.add(new Label("Kilométrage (KM) :"), 0, 5); grid.add(txtKm, 1, 5);

        dialog.getDialogPane().setContent(grid);

        // Convertisseur de résultat avec validation stricte intégrée (Section 4.6)
        dialog.setResultConverter(btn -> {
            if (btn == btnEnregistrer) {
                try {
                    int annee = Integer.parseInt(txtAnnee.getText().trim());
                    int km = Integer.parseInt(txtKm.getText().trim());
                    double prix = Double.parseDouble(txtPrix.getText().trim());

                    // Validation des bornes et contraintes d'intégrité applicatives
                    if (annee < 1900 || annee > 2027 || km < 0 || prix < 0 || txtId.getText().isBlank()) {
                        throw new IllegalArgumentException("Champs vides ou contraintes de valeurs violées.");
                    }

                    return new Annonce(
                        txtId.getText().trim(), txtMarque.getText().trim(), txtModele.getText().trim(),
                        annee, km, prix, TypeCarburant.ESSENCE, Transmission.AUTOMATIQUE, 
                        "Gris", "Montreal", java.time.LocalDate.now(), "Saisie via formulaire graphique JDBC."
                    );
                } catch (Exception e) {
                    afficherFenetreErreur("Saisie Invalide", "L'année (1900-2027), le prix et le kilométrage doivent être des nombres positifs.");
                    return null;
                }
            }
            return null;
        });
        return dialog;
    }

    private void afficherFenetreErreur(String titre, String contenu) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle(titre); 
        a.setHeaderText(null); 
        a.setContentText(contenu);
        a.showAndWait();
    }
}
