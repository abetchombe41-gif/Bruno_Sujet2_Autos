package com.app.dao;

import com.app.model.Annonce;
import java.util.List;
import java.util.Optional;

public interface AnnonceDAO {
    List<Annonce> trouverTous();
    Optional<Annonce> trouverParId(String id);
    void ajouter(Annonce annonce);
    void modifier(Annonce annonce);
    void supprimer(String id);
}
