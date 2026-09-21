# Marketplace de Voitures d'Occasion
## Projet Évolutif - Laboratoire 2 & Laboratoire 3
**Cours : Algorithmes et Structures de Données**

Ce projet implémente une application complète de gestion et d'exploration d'un catalogue de véhicules d'occasion (300 annonces). Initialement basé sur un fichier plat, le système a été migré vers une persistance relationnelle cloud PostgreSQL à l'aide du patron d'architecture **DAO (Data Access Object)**.

---

## 📂 Architecture du Projet (Patron MVC + DAO)

Le code source respecte scrupuleusement le découplage des responsabilités (Règle 5) :
* **Modèle (`src/main/java/com/app/model/`) :** `Annonce.java` (données métiers), structures énumérées (`TypeCarburant`, `Transmission`) et `CatalogueManager.java` (logique d'état des filtres et favoris).
* **Contrôleur (`src/main/java/com/app/controller/`) :** `MainController.java` qui intercepte les actions graphiques, orchestre les rafraîchissements et applique la validation des saisies.
* **Accès aux Données (`src/main/java/com/app/dao/`) :** Contrat d'interface `AnnonceDAO.java` et implémentation JDBC robuste `AnnonceDAOPostgreSQL.java`. Aucune instruction SQL ne sort de ce package.
* **Vue et Style (`src/main/resources/com/app/`) :** Maquette graphique structurelle (`view/MainView.fxml`) et identité visuelle personnalisée (`css/style.css`).
* **Utilitaires (`src/main/java/com/app/util/`) :** Algorithmes de tri personnalisés (`SortingAlgorithms.java`) et chargeur de secours (`CSVReader.java`).

---

## 🛠️ Fonctionnalités Clés Implémentées

### 📈 Laboratoire 2 : Algorithmique et Richesse Visuelle
* **Filtres Cumulatifs ("ET") :** Combinaison simultanée de la recherche textuelle, de la marque, du prix maximum et des propriétés mécaniques.
* **Tri Manuel & Benchmark :** Comparaison temporelle en temps réel au tableau de bord entre le **Tri à Bulles (O(n²))** et le **Tri Fusion (\(O(n \log n)\))**.
* **Pagination Stricte :** Affichage segmenté par tranches immuables de **25 annonces par page**.
* **Système d'Imagerie Hybride :** Rendu graphique vectoriel immédiat (`Canvas`) dessiné selon les attributs de la voiture, doublé d'un chargement asynchrone (`Task`) de photos réelles depuis l'API de *Wikimedia Commons*.

### 🗄️ Laboratoire 3 : Persistance et Robustesse CRUD
* **Base de Données Cloud :** Connexion JDBC sécurisée à une instance PostgreSQL hébergée sur **Neon** (`neon.tech`).
* **Contraintes d'Intégrité :** Normalisation en 2 tables (`voiture` et `marque`) avec clauses `CHECK` de sécurité et protection relationnelle `ON DELETE RESTRICT`.
* **Sécurité Anti-Injection :** Utilisation systématique de `PreparedStatement` paramétrés pour neutraliser les injections SQL et de structures `try-with-resources` pour prévenir les fuites de mémoire.
* **Interface CRUD Complète :** Fenêtres de dialogue graphiques pour l'**Ajout**, la **Modification** et la **Suppression** de fiches avec validation stricte des formats numériques.
* **Mode Dégradé Automatique :** En cas d'indisponibilité ou de coupure du serveur PostgreSQL distant, l'application intercepte l'exception et bascule automatiquement sur le fichier local `annonces.csv` de secours.

---

## 🚀 Guide d'Installation et de Lancement

### 1. Configuration de la Base de Données
Créez un fichier nommé **`database.properties`** à la racine absolue du projet (ce fichier est ignoré par Git pour des raisons de sécurité) et complétez vos accès JDBC sous le format suivant :
```properties
db.url=jdbc:postgresql://<votre_hote_neon>/neondb?sslmode=require&channel_binding=require
db.user=votre_utilisateur
db.password=votre_mot_de_passe_secret
```

### 2. Initialisation des Tables
Exécutez dans l'ordre les scripts SQL situés à la racine du projet sur votre instance PostgreSQL :
1. `schema.sql` (Création de la structure et des contraintes)
2. `donnees.sql` (Insertion et peuplement initial du catalogue)

### 3. Exécution de l'Application
Lancez la commande suivante à la racine du projet dans votre terminal :
```bash
mvn clean javafx:run
```

---

## 📊 Résumé des Performances observées (Benchmark des Tris)
Sur notre échantillon de 300 véhicules d'occasion, les mesures de temps d'exécution moyennes sont :
* **Tri à bulles (Bubble Sort) :** ~`11,42 ms` (Complexité quadratique)
* **Tri Fusion (Merge Sort) :** ~`2,19 ms` (Complexité quasi-linéaire)

Le Tri Fusion s'exécute en moyenne **5 fois plus vite** sur ce volume, confirmant l'efficacité de la stratégie "diviser pour régner" face à un tri par comparaisons adjacentes directes.
