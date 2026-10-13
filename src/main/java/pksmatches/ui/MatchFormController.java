package pksmatches.ui;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import pksmatches.model.Match;
import pksmatches.model.Tournament;
import pksmatches.model.enums.MatchStatus;
import pksmatches.model.enums.TournamentStage;
import pksmatches.service.MatchService;
import pksmatches.service.TournamentService;
import pksmatches.util.AlertUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class MatchFormController {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    @FXML private Label titleLabel;
    @FXML private ComboBox<Tournament> tournamentBox;
    @FXML private TextField team1Field;
    @FXML private TextField team2Field;
    @FXML private DatePicker datePicker;
    @FXML private TextField timeField;
    @FXML private ComboBox<TournamentStage> stageBox;
    @FXML private ComboBox<MatchStatus> statusBox;
    @FXML private TextField score1Field;
    @FXML private TextField score2Field;
    @FXML private Label errorLabel;

    private MatchService matchService;
    private TournamentService tournamentService;

    private Match editing; // null — добавление
    private boolean saved = false;

    public void setServices(MatchService matchService, TournamentService tournamentService) {
        this.matchService = matchService;
        this.tournamentService = tournamentService;
        loadTournaments();
        loadEnums();
    }

    public void setMatch(Match match) {
        this.editing = match;
        if (match == null) {
            titleLabel.setText("Новый матч");
            datePicker.setValue(LocalDate.now());
            timeField.setText("19:00");
            statusBox.setValue(MatchStatus.SCHEDULED);
            stageBox.setValue(TournamentStage.GROUP);
            score1Field.setText("0");
            score2Field.setText("0");
            if (!tournamentBox.getItems().isEmpty()) {
                tournamentBox.getSelectionModel().selectFirst();
            }
        } else {
            titleLabel.setText("Редактирование матча #" + match.getId());
            tournamentBox.setValue(findTournament(match));
            team1Field.setText(match.getTeam1());
            team2Field.setText(match.getTeam2());
            datePicker.setValue(match.getMatchDate().toLocalDate());
            timeField.setText(match.getMatchDate().toLocalTime().format(TIME_FMT));
            stageBox.setValue(match.getStage());
            statusBox.setValue(match.getStatus());
            score1Field.setText(String.valueOf(match.getScore1()));
            score2Field.setText(String.valueOf(match.getScore2()));
        }
    }

    public boolean isSaved() {
        return saved;
    }

    private Tournament findTournament(Match match) {
        for (Tournament t : tournamentBox.getItems()) {
            if (t.getId() == match.getTournament().getId()) {
                return t;
            }
        }
        return match.getTournament();
    }

    private void loadTournaments() {
        try {
            List<Tournament> all = tournamentService.getAll();
            tournamentBox.setItems(FXCollections.observableArrayList(all));
        } catch (RuntimeException e) {
            AlertUtil.error("Ошибка загрузки турниров", e.getMessage());
        }
    }

    private void loadEnums() {
        stageBox.setItems(FXCollections.observableArrayList(TournamentStage.values()));
        statusBox.setItems(FXCollections.observableArrayList(MatchStatus.values()));
    }

    @FXML
    private void onSaveClick() {
        hideError();

        try {
            Tournament tournament = tournamentBox.getValue();
            if (tournament == null) {
                showError("Выберите турнир");
                return;
            }

            String team1 = text(team1Field);
            String team2 = text(team2Field);
            if (team1.isEmpty()) {
                showError("Введите название первой команды");
                return;
            }
            if (team2.isEmpty()) {
                showError("Введите название второй команды");
                return;
            }
            if (team1.equalsIgnoreCase(team2)) {
                showError("Команды должны различаться");
                return;
            }

            LocalDate date = datePicker.getValue();
            if (date == null) {
                showError("Выберите дату матча");
                return;
            }

            LocalTime time;
            try {
                time = LocalTime.parse(text(timeField), TIME_FMT);
            } catch (DateTimeParseException e) {
                showError("Неверный формат времени. Ожидается HH:mm, например 19:00");
                return;
            }

            TournamentStage stage = stageBox.getValue();
            if (stage == null) {
                showError("Выберите этап турнира");
                return;
            }
            MatchStatus status = statusBox.getValue();
            if (status == null) {
                showError("Выберите статус матча");
                return;
            }

            int score1;
            int score2;
            try {
                score1 = parseIntSafe(text(score1Field), "счёт первой команды");
                score2 = parseIntSafe(text(score2Field), "счёт второй команды");
            } catch (IllegalArgumentException e) {
                showError(e.getMessage());
                return;
            }
            if (score1 < 0 || score2 < 0) {
                showError("Счёт не может быть отрицательным");
                return;
            }

            LocalDateTime dateTime = LocalDateTime.of(date, time);

            if (editing == null) {
                Match m = new Match(tournament, team1, team2, dateTime, stage);
                m.setStatus(status);
                m.setScore1(score1);
                m.setScore2(score2);
                matchService.addMatch(m);
            } else {
                editing.setTournament(tournament);
                editing.setTeam1(team1);
                editing.setTeam2(team2);
                editing.setMatchDate(dateTime);
                editing.setStage(stage);
                editing.setStatus(status);
                editing.setScore1(score1);
                editing.setScore2(score2);
                matchService.updateMatch(editing);
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
        Stage stage = (Stage) team1Field.getScene().getWindow();
        stage.close();
    }

    private String text(TextField field) {
        return field.getText() == null ? "" : field.getText().trim();
    }

    private int parseIntSafe(String value, String fieldName) {
        if (value.isEmpty()) return 0;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Поле \"" + fieldName + "\" должно быть числом");
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
