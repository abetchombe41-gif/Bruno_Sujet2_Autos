module Lab2_VoituresOccasion {
    requires javafx.controls;
    requires javafx.fxml;
    requires transitive javafx.graphics;
    requires java.sql; // Obligatoire pour les connexions JDBC
    requires java.net.http; // Obligatoire pour la recherche des photos Wikimedia

    opens com.app.controller to javafx.fxml;
    exports com.app;
}
