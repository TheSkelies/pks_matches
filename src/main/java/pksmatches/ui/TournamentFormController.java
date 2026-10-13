package pksmatches.ui;

import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import pksmatches.model.Tournament;
import pksmatches.service.TournamentService;
import pksmatches.util.AlertUtil;

import java.time.LocalDate;

public class TournamentFormController {

    @FXML private Label titleLabel;
    @FXML private TextField nameField;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private Label errorLabel;

    private TournamentService tournamentService;
    private Tournament editing;
    private boolean saved = false;

    public void setService(TournamentService service) {
        this.tournamentService = service;
    }

    public void setTournament(Tournament tournament) {
        this.editing = tournament;
        if (tournament == null) {
            titleLabel.setText("Новый турнир");
            startDatePicker.setValue(LocalDate.now());
        } else {
            titleLabel.setText("Редактирование турнира #" + tournament.getId());
            nameField.setText(tournament.getName());
            startDatePicker.setValue(tournament.getStartDate());
            endDatePicker.setValue(tournament.getEndDate());
        }
    }

    public boolean isSaved() {
        return saved;
    }

    @FXML
    private void onSaveClick() {
        hideError();

        String name = nameField.getText() == null ? "" : nameField.getText().trim();
        if (name.isEmpty()) {
            showError("Введите название турнира");
            return;
        }

        LocalDate start = startDatePicker.getValue();
        if (start == null) {
            showError("Выберите дату начала");
            return;
        }

        LocalDate end = endDatePicker.getValue();
        if (end != null && end.isBefore(start)) {
            showError("Дата окончания не может быть раньше даты начала");
            return;
        }

        try {
            if (editing == null) {
                Tournament t = new Tournament(name, start, end);
                tournamentService.add(t);
            } else {
                editing.setName(name);
                editing.setStartDate(start);
                editing.setEndDate(end);
                tournamentService.update(editing);
            }
            saved = true;
            closeDialog();
        } catch (IllegalArgumentException | IllegalStateException e) {
            showError(e.getMessage());
        } catch (RuntimeException e) {
            AlertUtil.error("Ошибка сохранения", e.getMessage());
        }
    }

    @FXML
    private void onCancelClick() {
        closeDialog();
    }

    private void closeDialog() {
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
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
