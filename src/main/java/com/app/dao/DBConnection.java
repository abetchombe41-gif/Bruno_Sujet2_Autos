package com.app.dao;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBConnection {
    private static final Properties props = new Properties();

    static {
        // Chargement sécurisé du fichier de configuration externe (Section 4.3)
        try (InputStream is = DBConnection.class.getClassLoader().getResourceAsStream("database.properties")) {
            if (is == null) {
                // Secours si le fichier est placé à la racine du projet plutôt que dans les ressources
                try (InputStream rootIs = java.nio.file.Files.newInputStream(java.nio.file.Paths.get("database.properties"))) {
                    props.load(rootIs);
                }
            } else {
                props.load(is);
            }
            // Forcer le chargement explicite du pilote JDBC
            Class.forName("org.postgresql.Driver");
        } catch (Exception e) {
            System.err.println("CRITIQUE : Impossible de charger la configuration de la base de données.");
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
            props.getProperty("db.url"),
            props.getProperty("db.user"),
            props.getProperty("db.password")
        );
    }
}

