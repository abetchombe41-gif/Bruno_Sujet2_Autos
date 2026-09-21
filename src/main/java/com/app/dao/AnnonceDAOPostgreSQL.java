package com.app.dao;

import com.app.model.Annonce;
import com.app.model.Transmission;
import com.app.model.TypeCarburant;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AnnonceDAOPostgreSQL implements AnnonceDAO {

    @Override
    public List<Annonce> trouverTous() {
        List<Annonce> liste = new ArrayList<>();
        String sql = "SELECT * FROM voiture";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                liste.add(mapResultSetToAnnonce(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération des annonces", e);
        }
        return liste;
    }

    @Override
    public Optional<Annonce> trouverParId(String id) {
        String sql = "SELECT * FROM voiture WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToAnnonce(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche de l'annonce ID: " + id, e);
        }
        return Optional.empty();
    }

    @Override
    public void ajouter(Annonce a) {
        String sql = "INSERT INTO voiture (id, marque_nom, modele, annee_modele, kilometrage, prix, carburant, transmission, couleur, ville, date_publication, description) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            assurerMarqueExiste(conn, a.getMarque());
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                setStatementParameters(stmt, a);
                stmt.executeUpdate();
            }
            conn.commit();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de l'ajout de l'annonce : " + e.getMessage(), e);
        }
    }

    @Override
    public void modifier(Annonce a) {
        String sql = "UPDATE voiture SET marque_nom=?, modele=?, annee_modele=?, kilometrage=?, prix=?, carburant=?, transmission=?, couleur=?, ville=?, date_publication=?, description=? WHERE id=?";
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            assurerMarqueExiste(conn, a.getMarque());
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, a.getMarque());
                stmt.setString(2, a.getModele());
                stmt.setInt(3, a.getAnneeModele());
                stmt.setInt(4, a.getKilometrage());
                stmt.setDouble(5, a.getPrix());
                stmt.setString(6, a.getCarburant().name());
                stmt.setString(7, a.getTransmission().name());
                stmt.setString(8, a.getCouleur());
                stmt.setString(9, a.getVille());
                stmt.setDate(10, Date.valueOf(a.getDatePublication()));
                stmt.setString(11, a.getDescription());
                stmt.setString(12, a.getId());
                stmt.executeUpdate();
            }
            conn.commit();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la modification de l'annonce ID " + a.getId() + " : " + e.getMessage(), e);
        }
    }

    private void assurerMarqueExiste(Connection conn, String nomMarque) throws SQLException {
        if (nomMarque == null || nomMarque.isBlank()) {
            throw new SQLException("La marque est obligatoire.");
        }

        String sql = "INSERT INTO marque (nom) VALUES (?) ON CONFLICT (nom) DO NOTHING";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nomMarque.trim());
            stmt.executeUpdate();
        }
    }

    @Override
    public void supprimer(String id) {
        String sql = "DELETE FROM voiture WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la suppression de l'annonce ID: " + id, e);
        }
    }

    private void setStatementParameters(PreparedStatement stmt, Annonce a) throws SQLException {
        stmt.setString(1, a.getId());
        stmt.setString(2, a.getMarque());
        stmt.setString(3, a.getModele());
        stmt.setInt(4, a.getAnneeModele());
        stmt.setInt(5, a.getKilometrage());
        stmt.setDouble(6, a.getPrix());
        stmt.setString(7, a.getCarburant().name());
        stmt.setString(8, a.getTransmission().name());
        stmt.setString(9, a.getCouleur());
        stmt.setString(10, a.getVille());
        stmt.setDate(11, Date.valueOf(a.getDatePublication()));
        stmt.setString(12, a.getDescription());
    }

    private Annonce mapResultSetToAnnonce(ResultSet rs) throws SQLException {
        return new Annonce(
            rs.getString("id"),
            rs.getString("marque_nom"),
            rs.getString("modele"),
            rs.getInt("annee_modele"),
            rs.getInt("kilometrage"),
            rs.getDouble("prix"),
            TypeCarburant.valueOf(rs.getString("carburant")),
            Transmission.valueOf(rs.getString("transmission")),
            rs.getString("couleur"),
            rs.getString("ville"),
            rs.getDate("date_publication").toLocalDate(),
            rs.getString("description")
        );
    }
}
