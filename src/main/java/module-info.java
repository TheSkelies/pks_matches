module com.example.match_agreagtor {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.slf4j;

    exports com.example.match_agreagtor;
    exports pksmatches;
    exports pksmatches.model;
    exports pksmatches.model.enums;
    exports pksmatches.service;
    exports pksmatches.repository;
    exports pksmatches.repository.impl;
    exports pksmatches.util;
    exports pksmatches.ui;

    opens com.example.match_agreagtor to javafx.fxml;
    opens pksmatches.ui to javafx.fxml;
    opens pksmatches.model to javafx.base;
    opens pksmatches.model.enums to javafx.fxml;
}

