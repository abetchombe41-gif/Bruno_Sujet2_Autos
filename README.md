# Laboratoire 2 : Structures de données et Algorithmes
## Sujet 2 : Marketplace de voitures d'occasion

Ce projet implémente une application de gestion, d'exploration et de traitement de données pour un catalogue de véhicules d'occasion de 300 annonces. L'application sépare rigoureusement ses composants selon le patron d'architecture **Modèle-Vue-Contrôleur (MVC)** et intègre des algorithmes de tri écrits à la main à des fins de benchmark.

---

## 📂 Organisation de la Structure (Patron MVC)

* **Modèle (`src/main/java/com/app/model/`) :** Contient la définition structurelle des objets (`Annonce.java`), les énumérations associées (`TypeCarburant.java`, `Transmission.java`) ainsi que l'unité logique centrale (`CatalogueManager.java`).
* **Vue (`src/main/resources/com/app/`) :** Déclare la géométrie graphique (`view/MainView.fxml`) et l'identité visuelle de l'interface (`css/style.css`).
* **Contrôleur (`src/main/java/com/app/controller/`) :** `MainController.java` intercepte et traite les événements de l'UI pour mettre à jour l'affichage en temps réel.
* **Utilitaires (`src/main/java/com/app/util/`) :** Héberge le chargeur de données (`CSVReader.java`) et les moteurs algorithmiques (`SortingAlgorithms.java`).

---

## 🛠️ Spécifications Fonctionnelles Validées

* **Filtres Cumulatifs (Logique ET) :** Recherche textuelle floue combinée simultanément à la marque (générée dynamiquement), au prix maximum et aux spécifications techniques.
* **Moteurs de Tri Personnalisés :** Tri à la main selon plusieurs critères commutables (Prix croissant, Prix décroissant, Année de fabrication).
* **Pagination Stricte :** Segmentation absolue du volume de données par tranches fixes de **25 annonces par page** avec navigation sécurisée.
* **Richesse Graphique :** Panneau de détails dynamique calculant instantanément le ratio *Prix au kilomètre parcouru*.
* **Double Moteur d'Imagerie :** Génération géométrique vectorielle instantanée (`Canvas`) basée sur la couleur de la carrosserie couplée à un chargement asynchrone (`Task`) de photos réelles depuis l'API de *Wikimedia Commons*.
* **Module de Benchmark :** Calcul et affichage au millième de milliseconde près du temps d'exécution comparé entre le *Tri à Bulles* et le *Tri Fusion*.

---

## 🚀 Compilation et Lancement du Projet

### Prérequis Système
* **Java JDK 17** ou supérieur (Configuration cible : JDK 17)
* **Apache Maven** (Configuré dans vos variables d'environnement système)

### Démarrage Rapide
Exécutez la commande suivante à la racine de votre dossier de travail :
```bash
mvn clean javafx:run
```
