-- Suppression des tables si elles existent pour rendre le script rejouable
DROP TABLE IF EXISTS voiture;
DROP TABLE IF EXISTS marque;

-- Table liée (Clé étrangère)
CREATE TABLE marque (
    nom VARCHAR(50) PRIMARY KEY
);

-- Table principale avec contraintes d'intégrité (Section 4.1)
CREATE TABLE voiture (
    id VARCHAR(50) PRIMARY KEY,
    marque_nom VARCHAR(50) NOT NULL,
    modele VARCHAR(50) NOT NULL,
    annee_modele INT NOT NULL CHECK (annee_modele >= 1900 AND annee_modele <= 2027),
    kilometrage INT NOT NULL CHECK (kilometrage >= 0),
    prix NUMERIC(10, 2) NOT NULL CHECK (prix >= 0.0),
    carburant VARCHAR(30) NOT NULL,
    transmission VARCHAR(30) NOT NULL,
    couleur VARCHAR(30) NOT NULL,
    ville VARCHAR(50) NOT NULL,
    date_publication DATE NOT NULL,
    description TEXT,
    CONSTRAINT fk_voiture_marque FOREIGN KEY (marque_nom) 
        REFERENCES marque(nom) ON DELETE RESTRICT -- Justification : On refuse de supprimer une marque si des voitures y sont rattachées
);
