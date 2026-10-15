package com.example.match_agreagtor;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class MatchAggregatorApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                MatchAggregatorApplication.class.getResource("/fxml/login-view.fxml")
        );
        Scene scene = new Scene(loader.load(), 500, 580);
        scene.getStylesheets().add(
                Objects.requireNonNull(
                        getClass().getResource("/css/style.css")
                ).toExternalForm()
        );
        stage.setTitle("Агрегатор матчей — Вход");
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
