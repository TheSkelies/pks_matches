module com.example.match_agreagtor {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    exports com.example.match_agreagtor;

    opens com.example.match_agreagtor to javafx.fxml;
    opens pksmatches to javafx.fxml;

    exports pksmatches;
    exports pksmatches.repository;
    exports pksmatches.repository.impl;
    exports pksmatches.util;
}