package com.app.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CatalogueManager {
    private List<Annonce> toutesLesAnnonces = new ArrayList<>();
    private List<Annonce> favoris = new ArrayList<>();

    public void setToutesLesAnnonces(List<Annonce> liste) { this.toutesLesAnnonces = liste; }
    public List<Annonce> getToutesLesAnnonces() { return toutesLesAnnonces; }
    public List<Annonce> getFavoris() { return favoris; }

    public void ajouterAuxFavoris(Annonce a) {
        if (!favoris.contains(a)) favoris.add(a); // Pas de doublons
    }

    public void retirerDesFavoris(Annonce a) { favoris.remove(a); }

    //Implémentation des critères combinés en "ET"
    public List<Annonce> filtrerEtRechercher(String texte, String marque, Double prixMax, String transmission) {
        return toutesLesAnnonces.stream()
            .filter(a -> texte == null || texte.isBlank() || a.getMarque().toLowerCase().contains(texte.toLowerCase()) || a.getModele().toLowerCase().contains(texte.toLowerCase()))
            .filter(a -> marque == null || marque.equals("Toutes") || a.getMarque().equalsIgnoreCase(marque))
            .filter(a -> prixMax == null || a.getPrix() <= prixMax)
            .filter(a -> transmission == null || transmission.equals("Toutes") || a.getTransmission().toString().equalsIgnoreCase(transmission))
            .collect(Collectors.toList());
    }

    //Pagination 
    public List<Annonce> obtenirPage(List<Annonce> listeFiltree, int numPage) {
        int indexDebut = numPage * 25;
        if (indexDebut >= listeFiltree.size() || indexDebut < 0) return new ArrayList<>();
        int indexFin = Math.min(indexDebut + 25, listeFiltree.size());
        return listeFiltree.subList(indexDebut, indexFin);
    }
}
