package pksmatches.ui;

import com.example.match_agreagtor.MatchAggregatorApplication;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import pksmatches.model.User;
import pksmatches.repository.UserRepository;
import pksmatches.repository.impl.UserRepositoryJdbc;
import pksmatches.service.UserService;

import java.io.IOException;
import java.util.Objects;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    private UserService userService;

    @FXML
    private void initialize() {
        UserRepository userRepository = new UserRepositoryJdbc();
        userService = new UserService(userRepository);
        hideError();
    }

    @FXML
    private void onLoginClick() {
        hideError();

        String username = usernameField.getText() == null ? "" : usernameField.getText().trim();
        String password = passwordField.getText() == null ? "" : passwordField.getText();

        if (username.isEmpty()) {
            showError("Введите логин");
            return;
        }
        if (password.isEmpty()) {
            showError("Введите пароль");
            return;
        }

        try {
            User user = userService.authenticate(username, password);
            if (user == null) {
                showError("Неверный логин или пароль");
                passwordField.clear();
                return;
            }
            openMainWindow(user);
        } catch (RuntimeException e) {
            showError("Ошибка подключения к базе данных: " + e.getMessage());
        }
    }

    private void openMainWindow(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    MatchAggregatorApplication.class.getResource("/fxml/main-view.fxml")
            );
            Parent root = loader.load();

            MainController controller = loader.getController();
            controller.setCurrentUser(user);

            Scene scene = new Scene(root, 1200, 720);
            scene.getStylesheets().add(
                    Objects.requireNonNull(
                            getClass().getResource("/css/style.css")
                    ).toExternalForm()
            );

            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setTitle("Агрегатор матчей — " + user.getUsername() + " (" + user.getRole() + ")");
            stage.setScene(scene);
            stage.setResizable(true);
            stage.centerOnScreen();
        } catch (IOException e) {
            showError("Не удалось открыть главное окно: " + e.getMessage());
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void hideError() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }
}
