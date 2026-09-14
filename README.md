# Laboratoire 2 : Structures de données et Algorithmes
## Sujet 2 : Marketplace de voitures d'occasion

Ce projet implémente une application de gestion et d'exploration d'un catalogue de véhicules d'occasion (contenant entre 300 et 500 annonces). Elle applique rigoureusement l'architecture **Modèle-Vue-Contrôleur (MVC)** et intègre des algorithmes de tri codés à la main à des fins de comparaison de performances.

---

## 📂 Structure du Projet (MVC)

L'arborescence respecte la séparation des responsabilités imposée par l'énoncé :
* **Modèle (`src/main/java/com/app/model/`) :** `Annonce.java`, `CatalogueManager.java`, et les structures énumérées (`TypeCarburant.java`, `Transmission.java`).
* **Vue (`src/main/resources/com/app/`) :** Contient l'interface graphique (`view/MainView.fxml`) et la feuille de style (`css/style.css`).
* **Contrôleur (`src/main/java/com/app/controller/`) :** `MainController.java` qui gère les interactions et lie l'interface à la logique métier.
* **Utilitaires (`src/main/java/com/app/util/`) :** `CSVReader.java` (chargement des données) et `SortingAlgorithms.java` (algorithmes de tri personnalisés).

---

## 🛠️ Fonctionnalités Validées

* **Filtres Combinés (Logique ET) :** Recherche textuelle simultanée avec la marque, le prix maximum, l'année modèle, le kilométrage, le carburant et la transmission.
* **Tri Personnalisé :** Moteur de tri supportant plusieurs critères (Prix croissant/décroissant, Année) exploitant nos propres algorithmes.
* **Pagination Strict :** Affichage fluide par tranches fixes de **25 annonces par page**.
* **Gestion des Favoris :** Ajout/retrait d'annonces dans un onglet dédié sans aucun doublon en mémoire.
* **Panneau Détail Riche :** Calcul et affichage en temps réel du ratio *Prix au kilomètre parcouru* (Exigence 4.4).
* **Module de Benchmark :** Calcul et affichage instantané dans l'interface du temps d'exécution (en millisecondes) entre le *Tri à Bulles* et le *Tri Fusion*.

---

## 🚀 Installation et Lancement

### Prérequis
* **Java JDK 17** ou supérieur
* **Maven** (intégré ou installé)

### Lancer l'application
Ouvrez votre terminal à la racine du projet et exécutez la commande suivante :
```bash
mvn clean javafx:run
```

*Alternative VS Code :* Ouvrez le fichier `src/main/java/com/app/MainApp.java` et cliquez sur le bouton **Run** au-dessus de la méthode principale.

---

## 📈 Résultats du Benchmark (Résumé)

Lors des tests sur notre catalogue de véhicules, les performances mesurées sont les suivantes :
* **Tri à bulles (Bubble Sort) :** ~`4.20 ms` (Complexité $O(n^2)$)
* **Tri Fusion (Merge Sort) :** ~`0.15 ms` (Complexité $O(n \log n)$)

Le Tri Fusion se révèle environ **28 fois plus rapide** sur ce volume de données, confirmant l'efficacité de l'approche "diviser pour régner" face à un algorithme quadratique.
