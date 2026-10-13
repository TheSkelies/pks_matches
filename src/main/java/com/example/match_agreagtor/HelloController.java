package com.example.match_agreagtor;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class HelloController {
    @FXML
    private Label welcomeText;

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Дима, ты остаёшся на все пары сегодня!");
    }
}
