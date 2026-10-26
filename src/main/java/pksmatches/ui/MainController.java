package pksmatches.ui;

import com.example.match_agreagtor.MatchAggregatorApplication;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import pksmatches.model.Match;
import pksmatches.model.Tournament;
import pksmatches.model.User;
import pksmatches.model.enums.MatchStatus;
import pksmatches.repository.MatchRepository;
import pksmatches.repository.TournamentRepository;
import pksmatches.repository.impl.MatchRepositoryJdbc;
import pksmatches.repository.impl.TournamentRepositoryJdbc;
import pksmatches.service.MatchService;
import pksmatches.service.TournamentService;
import pksmatches.util.AlertUtil;
import pksmatches.util.CsvExporter;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

public class MainController {

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private static final DateTimeFormatter D_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    @FXML private Label userLabel;

    @FXML private TextField searchField;
    @FXML private ComboBox<MatchStatus> statusFilter;
    @FXML private ComboBox<Tournament> tournamentFilter;
    @FXML private DatePicker dateFromFilter;
    @FXML private DatePicker dateToFilter;

    @FXML private TableView<Match> matchTable;
    @FXML private TableColumn<Match, Number> colId;
    @FXML private TableColumn<Match, String> colTournament;
    @FXML private TableColumn<Match, String> colTeam1;
    @FXML private TableColumn<Match, String> colTeam2;
    @FXML private TableColumn<Match, String> colDate;
    @FXML private TableColumn<Match, String> colStage;
    @FXML private TableColumn<Match, String> colStatus;
    @FXML private TableColumn<Match, String> colScore;

    @FXML private TableView<Tournament> tournamentTable;
    @FXML private TableColumn<Tournament, Number> colTId;
    @FXML private TableColumn<Tournament, String> colTName;
    @FXML private TableColumn<Tournament, String> colTStart;
    @FXML private TableColumn<Tournament, String> colTEnd;

    private MatchService matchService;
    private TournamentService tournamentService;

    private final ObservableList<Match> matches = FXCollections.observableArrayList();
    private final ObservableList<Tournament> tournaments = FXCollections.observableArrayList();

    private FilteredList<Match> filteredMatches;
    private SortedList<Match> sortedMatches;

    private User currentUser;

    @FXML
    private void initialize() {
        MatchRepository matchRepository = new MatchRepositoryJdbc();
        TournamentRepository tournamentRepository = new TournamentRepositoryJdbc();
        matchService = new MatchService(matchRepository);
        tournamentService = new TournamentService(tournamentRepository);

        setupMatchTable();
        setupTournamentTable();
        setupFilters();

        reloadAll();
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (userLabel != null && user != null) {
            userLabel.setText("| " + user.getUsername() + " (" + user.getRole() + ")");
        }
    }

    // ============ НАСТРОЙКА ТАБЛИЦ ============

    private void setupMatchTable() {
        colId.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getId()));
        colTournament.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getTournament().getName()));
        colTeam1.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTeam1()));
        colTeam2.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTeam2()));
        colDate.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getMatchDate().format(DT_FMT)));
        colStage.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getStage().name()));
        colStatus.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getStatus().name()));
        colScore.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getScore1() + " : " + c.getValue().getScore2()));

        matchTable.setPlaceholder(new Label("Матчей не найдено"));
        matchTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    }

    private void setupTournamentTable() {
        colTId.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getId()));
        colTName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getName()));
        colTStart.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getStartDate().format(D_FMT)));
        colTEnd.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getEndDate() != null ? c.getValue().getEndDate().format(D_FMT) : "—"));

        tournamentTable.setPlaceholder(new Label("Турниров нет"));
        tournamentTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        // ★ ГЛАВНОЕ ИСПРАВЛЕНИЕ БАГА С ТУРНИРАМИ:
        tournamentTable.setItems(tournaments);
    }

    private void setupFilters() {
        statusFilter.setItems(FXCollections.observableArrayList(MatchStatus.values()));

        // ▼▼▼ ДОБАВЛЕННЫЙ БЛОК: Настройка отображения названий турниров в ComboBox ▼▼▼
        tournamentFilter.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Tournament item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName()); // Выводим только название
                }
            }
        });
        // ▲▲▲ КОНЕЦ ДОБАВЛЕННОГО БЛОКА ▲▲▲

        statusFilter.valueProperty().addListener((obs, o, n) -> applyFilters());
        tournamentFilter.valueProperty().addListener((obs, o, n) -> applyFilters());
        dateFromFilter.valueProperty().addListener((obs, o, n) -> applyFilters());
        dateToFilter.valueProperty().addListener((obs, o, n) -> applyFilters());
    }

    // ============ ЗАГРУЗКА ДАННЫХ ============

    private void reloadAll() {
        try {
            matches.setAll(matchService.getAllMatches());
            tournaments.setAll(tournamentService.getAll());

            Tournament selected = tournamentFilter.getValue();
            tournamentFilter.setItems(FXCollections.observableArrayList(tournaments));
            if (selected != null) {
                tournamentFilter.setValue(selected);
            }

            if (filteredMatches == null) {
                filteredMatches = new FilteredList<>(matches, m -> true);
            } else {
                filteredMatches.setPredicate(m -> true);
            }

            if (sortedMatches == null) {
                sortedMatches = new SortedList<>(filteredMatches);
                sortedMatches.comparatorProperty().bind(matchTable.comparatorProperty());
                matchTable.setItems(sortedMatches);
            }

            applyFilters();
        } catch (RuntimeException e) {
            AlertUtil.error("Ошибка загрузки данных", e.getMessage());
        }
    }

    @FXML
    private void onRefreshClick() {
        reloadAll();
    }

    @FXML
    private void onSearchChanged() {
        applyFilters();
    }

    @FXML
    private void onResetFiltersClick() {
        searchField.clear();
        statusFilter.setValue(null);
        tournamentFilter.setValue(null);
        dateFromFilter.setValue(null);
        dateToFilter.setValue(null);
        applyFilters();
    }

    private void applyFilters() {
        if (filteredMatches == null) return;

        String query = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        MatchStatus status = statusFilter.getValue();
        Tournament tournament = tournamentFilter.getValue();
        LocalDate from = dateFromFilter.getValue();
        LocalDate to = dateToFilter.getValue();

        Predicate<Match> predicate = m -> {
            if (!query.isEmpty()) {
                boolean matchesSearch =
                        m.getTeam1().toLowerCase().contains(query) ||
                        m.getTeam2().toLowerCase().contains(query) ||
                        m.getTournament().getName().toLowerCase().contains(query);
                if (!matchesSearch) return false;
            }
            if (status != null && m.getStatus() != status) return false;
            if (tournament != null && m.getTournament().getId() != tournament.getId()) return false;

            LocalDate matchDate = m.getMatchDate().toLocalDate();
            if (from != null && matchDate.isBefore(from)) return false;
            if (to != null && matchDate.isAfter(to)) return false;

            return true;
        };

        filteredMatches.setPredicate(predicate);
    }

    // ============ CRUD МАТЧЕЙ ============

    @FXML
    private void onAddMatchClick() {
        openMatchForm(null);
    }

    @FXML
    private void onEditMatchClick() {
        Match selected = matchTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.warn("Не выбран матч", "Выберите строку в таблице для редактирования.");
            return;
        }
        openMatchForm(selected);
    }

    private void openMatchForm(Match match) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    MatchAggregatorApplication.class.getResource("/fxml/match-form.fxml")
            );
            Parent root = loader.load();

            MatchFormController controller = loader.getController();
            controller.setServices(matchService, tournamentService);
            controller.setMatch(match);

            Stage dialog = new Stage();
            dialog.initModality(Modality.WINDOW_MODAL);
            dialog.initOwner(matchTable.getScene().getWindow());
            dialog.setTitle(match == null ? "Новый матч" : "Редактирование матча");

            Scene scene = new Scene(root);
            scene.getStylesheets().add(
                    Objects.requireNonNull(getClass().getResource("/css/style.css")).toExternalForm()
            );
            dialog.setScene(scene);
            dialog.showAndWait();

            if (controller.isSaved()) {
                reloadAll();
            }
        } catch (IOException e) {
            AlertUtil.error("Ошибка открытия формы", e.getMessage());
        }
    }

    @FXML
    private void onDeleteMatchClick() {
        Match selected = matchTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.warn("Не выбран матч", "Выберите строку для удаления.");
            return;
        }
        boolean confirmed = AlertUtil.confirm(
                "Подтверждение удаления",
                "Удалить матч \"" + selected.getTeam1() + " — " + selected.getTeam2() + "\"?"
        );
        if (!confirmed) return;

        try {
            if (matchService.removeMatch(selected.getId())) {
                reloadAll();
            } else {
                AlertUtil.warn("Не найдено", "Матч уже удалён.");
            }
        } catch (RuntimeException e) {
            AlertUtil.error("Ошибка удаления", e.getMessage());
        }
    }

    // ============ СТАТУСЫ МАТЧЕЙ ============

    @FXML
    private void onStartMatchClick() {
        Match selected = matchTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.warn("Не выбран матч", "Выберите матч для запуска.");
            return;
        }
        try {
            matchService.startLive(selected.getId());
            reloadAll();
        } catch (RuntimeException e) {
            AlertUtil.error("Ошибка", e.getMessage());
        }
    }

    @FXML
    private void onFinishMatchClick() {
        Match selected = matchTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.warn("Не выбран матч", "Выберите матч для завершения.");
            return;
        }
        try {
            matchService.finish(selected.getId());
            reloadAll();
        } catch (RuntimeException e) {
            AlertUtil.error("Ошибка", e.getMessage());
        }
    }

    @FXML
    private void onCancelMatchClick() {
        Match selected = matchTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.warn("Не выбран матч", "Выберите матч для отмены.");
            return;
        }
        if (!AlertUtil.confirm("Отмена матча",
                "Отменить матч \"" + selected.getTeam1() + " — " + selected.getTeam2() + "\"?")) {
            return;
        }
        try {
            matchService.cancel(selected.getId());
            reloadAll();
        } catch (RuntimeException e) {
            AlertUtil.error("Ошибка", e.getMessage());
        }
    }

    @FXML
    private void onUpdateScoreClick() {
        Match selected = matchTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.warn("Не выбран матч", "Выберите матч для изменения счёта.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog(selected.getScore1() + ":" + selected.getScore2());
        dialog.setTitle("Изменение счёта");
        dialog.setHeaderText(selected.getTeam1() + " — " + selected.getTeam2());
        dialog.setContentText("Введите счёт в формате X:Y");
        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty()) return;

        String raw = result.get().trim();
        if (!raw.matches("\\d{1,3}\\s*:\\s*\\d{1,3}")) {
            AlertUtil.warn("Неверный формат", "Ожидается формат X:Y, например 2:1.");
            return;
        }
        String[] parts = raw.split(":");
        int s1 = Integer.parseInt(parts[0].trim());
        int s2 = Integer.parseInt(parts[1].trim());

        try {
            matchService.updateScore(selected.getId(), s1, s2);
            reloadAll();
        } catch (RuntimeException e) {
            AlertUtil.error("Ошибка", e.getMessage());
        }
    }

    // ============ CRUD ТУРНИРОВ ============

    @FXML
    private void onAddTournamentClick() {
        openTournamentForm(null);
    }

    @FXML
    private void onEditTournamentClick() {
        Tournament selected = tournamentTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.warn("Не выбран турнир", "Выберите строку для редактирования.");
            return;
        }
        openTournamentForm(selected);
    }

    @FXML
    private void onDeleteTournamentClick() {
        Tournament selected = tournamentTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.warn("Не выбран турнир", "Выберите строку для удаления.");
            return;
        }
        if (!AlertUtil.confirm("Подтверждение удаления",
                "Удалить турнир \"" + selected.getName() + "\"?\nВсе связанные матчи также будут удалены.")) {
            return;
        }
        try {
            if (tournamentService.remove(selected.getId())) {
                reloadAll();
            } else {
                AlertUtil.warn("Не найдено", "Турнир уже удалён.");
            }
        } catch (RuntimeException e) {
            AlertUtil.error("Ошибка удаления", e.getMessage());
        }
    }

    private void openTournamentForm(Tournament tournament) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    MatchAggregatorApplication.class.getResource("/fxml/tournament-form.fxml")
            );
            Parent root = loader.load();

            TournamentFormController controller = loader.getController();
            controller.setService(tournamentService);
            controller.setTournament(tournament);

            Stage dialog = new Stage();
            dialog.initModality(Modality.WINDOW_MODAL);
            dialog.initOwner(tournamentTable.getScene().getWindow());
            dialog.setTitle(tournament == null ? "Новый турнир" : "Редактирование турнира");

            Scene scene = new Scene(root);
            scene.getStylesheets().add(
                    Objects.requireNonNull(getClass().getResource("/css/style.css")).toExternalForm()
            );
            dialog.setScene(scene);
            dialog.showAndWait();

            if (controller.isSaved()) {
                reloadAll();
            }
        } catch (IOException e) {
            AlertUtil.error("Ошибка открытия формы", e.getMessage());
        }
    }

    // ============ ЭКСПОРТ ============

    @FXML
    private void onExportClick() {
        List<Match> current = matchTable.getItems();
        if (current.isEmpty()) {
            AlertUtil.warn("Нечего экспортировать", "Таблица матчей пуста.");
            return;
        }

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Сохранить CSV");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV файл (*.csv)", "*.csv")
        );
        chooser.setInitialFileName("matches_" + LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv");

        File file = chooser.showSaveDialog(matchTable.getScene().getWindow());
        if (file == null) return;

        try {
            CsvExporter.exportMatches(current, file);
            AlertUtil.info("Экспорт завершён", "Данные сохранены в файл:\n" + file.getAbsolutePath());
        } catch (IOException e) {
            AlertUtil.error("Ошибка экспорта", e.getMessage());
        }
    }

    // ============ ВЫХОД ============

    @FXML
    private void onLogoutClick() {
        if (!AlertUtil.confirm("Выход", "Выйти из учётной записи?")) return;

        try {
            FXMLLoader loader = new FXMLLoader(
                    MatchAggregatorApplication.class.getResource("/fxml/login-view.fxml")
            );
            Parent root = loader.load();

            Scene scene = new Scene(root, 500, 560);
            scene.getStylesheets().add(
                    Objects.requireNonNull(getClass().getResource("/css/style.css")).toExternalForm()
            );

            Stage stage = (Stage) matchTable.getScene().getWindow();
            stage.setTitle("Агрегатор матчей — Вход");
            stage.setScene(scene);
            stage.setResizable(false);
            stage.centerOnScreen();
        } catch (IOException e) {
            AlertUtil.error("Ошибка", e.getMessage());
        }
    }
}
