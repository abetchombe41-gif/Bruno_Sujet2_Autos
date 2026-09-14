package com.app.model;

import java.time.LocalDate;

public class Annonce {
    private String id;
    private String marque;
    private String modele;
    private int anneeModele;
    private int kilometrage;
    private double prix;
    private TypeCarburant carburant;
    private Transmission transmission;
    private String couleur;
    private String ville;
    private LocalDate datePublication;
    private String description;

    public Annonce(String id, String marque, String modele, int anneeModele, int kilometrage, 
                   double prix, TypeCarburant carburant, Transmission transmission, 
                   String couleur, String ville, LocalDate datePublication, String description) {
        this.id = id;
        this.marque = marque;
        this.modele = modele;
        this.anneeModele = anneeModele;
        this.kilometrage = kilometrage;
        this.prix = prix;
        this.carburant = carburant;
        this.transmission = transmission;
        this.couleur = couleur;
        this.ville = ville;
        this.datePublication = datePublication;
        this.description = description;
    }

    // Getters pour le tri et l'affichage
    public String getId() { return id; }
    public String getMarque() { return marque; }
    public String getModele() { return modele; }
    public int getAnneeModele() { return anneeModele; }
    public int getKilometrage() { return kilometrage; }
    public double getPrix() { return prix; }
    public TypeCarburant getCarburant() { return carburant; }
    public Transmission getTransmission() { return transmission; }
    public String getVille() { return ville; }
    public LocalDate getDatePublication() { return datePublication; }
    public String getDescription() { return description; }

    // Calcul du prix au kilomètre
    public double getPrixAuKilometre() {
        if (this.kilometrage <= 0) return 0.0;
        return this.prix / this.kilometrage;
    }

    @Override
    public String toString() {
        return marque + " " + modele + " (" + anneeModele + ") - " + prix + " $";
    }
}
