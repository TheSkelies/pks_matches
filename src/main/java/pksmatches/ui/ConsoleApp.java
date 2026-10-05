package pksmatches.ui;

import pksmatches.model.Match;
import pksmatches.model.Tournament;
import pksmatches.model.User;
import pksmatches.model.enums.MatchStatus;
import pksmatches.model.enums.TournamentStage;
import pksmatches.model.enums.UserRole;
import pksmatches.repository.MatchRepository;
import pksmatches.repository.TournamentRepository;
import pksmatches.repository.UserRepository;
import pksmatches.repository.impl.MatchRepositoryJdbc;
import pksmatches.repository.impl.TournamentRepositoryJdbc;
import pksmatches.repository.impl.UserRepositoryJdbc;
import pksmatches.service.MatchService;
import pksmatches.service.TournamentService;
import pksmatches.service.UserService;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public class ConsoleApp {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private static final int MAX_INPUT_LENGTH = 100;
    private static final int MAX_SAFE_INT = 100_000;

    private final Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8);
    private final MatchService matchService;
    private final TournamentService tournamentService;
    private final UserService userService;

    private User currentUser;

    public ConsoleApp() {
        UserRepository userRepository = new UserRepositoryJdbc();
        TournamentRepository tournamentRepository = new TournamentRepositoryJdbc();
        MatchRepository matchRepository = new MatchRepositoryJdbc();

        this.userService = new UserService(userRepository);
        this.tournamentService = new TournamentService(tournamentRepository);
        this.matchService = new MatchService(matchRepository);
    }

    public void run() {
        System.out.println("=== Агрегатор матчей ===");
        while (currentUser == null) {
            if (!login()) {
                System.out.println("Попробуйте снова.");
            }
        }

        boolean running = true;
        while (running) {
            printMenu();
            int choice = readInt("Выбор: ");
            switch (choice) {
                case 1 -> listAllMatches();
                case 2 -> listLiveMatches();
                case 3 -> listMatchesByTournament();
                case 4 -> addMatch();
                case 5 -> startMatch();
                case 6 -> finishMatch();
                case 7 -> updateScore();
                case 8 -> cancelMatch();
                case 9 -> listTournaments();
                case 10 -> addTournament();
                case 11 -> listUsers();
                case 12 -> addUser();
                case 13 -> logout();
                case 0 -> running = false;
                default -> System.out.println("Неверный пункт меню.");
            }
        }
        System.out.println("До свидания.");
    }

    private void logout() {
        System.out.println("Выход из профиля " + currentUser.getUsername() + ".");
        currentUser = null;
        while (currentUser == null) {
            System.out.println();
            System.out.println("=== Вход в систему ===");
            if (!login()) {
                System.out.println("Попробуйте снова.");
            }
        }
    }

    private void printMenu() {
        System.out.println();
        System.out.println("Пользователь: " + currentUser.getUsername()
                + " (" + currentUser.getRole() + ")");
        System.out.println("1. Все матчи");
        System.out.println("2. Live-матчи");
        System.out.println("3. Матчи турнира");
        System.out.println("4. Добавить матч");
        System.out.println("5. Начать матч");
        System.out.println("6. Завершить матч");
        System.out.println("7. Обновить счёт");
        System.out.println("8. Отменить матч");
        System.out.println("9. Список турниров");
        System.out.println("10. Добавить турнир");
        System.out.println("11. Список пользователей");
        System.out.println("12. Добавить пользователя");
        System.out.println("13. Сменить пользователя (Logout)");
        System.out.println("0. Выход");
    }

    private boolean login() {
        String username = readNonEmpty("Логин: ");
        String password = readPassword("Пароль: ");
        User user = userService.authenticate(username, password);
        if (user == null) {
            System.out.println("Неверный логин или пароль.");
            return false;
        }
        currentUser = user;
        System.out.println("Добро пожаловать, " + user.getUsername() + "!");
        return true;
    }

    private void listAllMatches() {
        printMatches(matchService.getAllMatches());
    }

    private void listLiveMatches() {
        printMatches(matchService.getLiveMatches());
    }

    private void listMatchesByTournament() {
        Tournament t = selectTournament();
        if (t == null) return;
        printMatches(matchService.getMatchesByTournament(t));
    }

    private void addMatch() {
        if (!requireAdmin()) return;
        Tournament t = selectTournament();
        if (t == null) return;
        String team1 = readNonEmpty("Команда 1: ");
        String team2 = readNonEmpty("Команда 2: ");
        if (team1.equalsIgnoreCase(team2)) {
            System.out.println("Ошибка: Команда не может играть сама с собой.");
            return;
        }
        LocalDateTime date = readDateTime("Дата и время (dd.MM.yyyy HH:mm): ");
        TournamentStage stage = readStage();
        try {
            Match m = new Match(t, team1, team2, date, stage);
            matchService.addMatch(m);
            System.out.println("Матч добавлен. ID=" + m.getId());
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }

    private void startMatch() {
        if (!requireAdmin()) return;
        Match m = selectMatch();
        if (m == null) return;
        try {
            matchService.startLive(m.getId());
            System.out.println("Матч переведён в статус LIVE.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }

    private void finishMatch() {
        if (!requireAdmin()) return;
        Match m = selectMatch();
        if (m == null) return;
        try {
            matchService.finish(m.getId());
            System.out.println("Матч завершён.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }

    private void updateScore() {
        if (!requireAdmin()) return;
        Match m = selectMatch();
        if (m == null) return;
        if (m.getStatus() == MatchStatus.FINISHED) {
            System.out.println("Ошибка: Нельзя менять счёт завершённого матча.");
            return;
        }
        if (m.getStatus() == MatchStatus.CANCELLED) {
            System.out.println("Ошибка: Нельзя менять счёт отменённого матча.");
            return;
        }
        if (m.getStatus() == MatchStatus.SCHEDULED) {
            System.out.println("Ошибка: Нельзя менять счёт запланированного матча. Сначала начните матч.");
            return;
        }
        int s1 = readInt("Счёт " + m.getTeam1() + ": ");
        int s2 = readInt("Счёт " + m.getTeam2() + ": ");
        try {
            matchService.updateScore(m.getId(), s1, s2);
            System.out.println("Счёт обновлён.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }

    private void cancelMatch() {
        if (!requireAdmin()) return;
        Match m = selectMatch();
        if (m == null) return;
        try {
            matchService.cancel(m.getId());
            System.out.println("Матч отменён.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }

    private void listTournaments() {
        List<Tournament> list = tournamentService.getAll();
        if (list.isEmpty()) {
            System.out.println("Турниров нет.");
            return;
        }
        for (Tournament t : list) {
            String end = t.getEndDate() != null ? t.getEndDate().format(DATE_FMT) : "не указана";
            System.out.println(t.getId() + ". " + t.getName()
                    + " (" + t.getStartDate().format(DATE_FMT) + " — " + end + ")");
        }
    }

    private void addTournament() {
        if (!requireAdmin()) return;
        String name = readNonEmpty("Название турнира: ");
        LocalDate start = readDate("Дата начала (dd.MM.yyyy): ");
        LocalDate end = readDate("Дата окончания (dd.MM.yyyy): ");
        try {
            Tournament t = new Tournament(name, start, end);
            tournamentService.add(t);
            System.out.println("Турнир добавлен. ID=" + t.getId());
        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }

    private void listUsers() {
        if (!requireAdmin()) return;
        for (User u : userService.getAll()) {
            System.out.println(u.getId() + ". Пользователь " + u.getUsername() + " имеет роль " + u.getRole());
        }
    }

    private void addUser() {
        if (!requireAdmin()) return;
        String username = readNonEmpty("Логин: ");
        String password = readPassword("Пароль: ");
        System.out.print("Роль (ADMIN/USER/GUEST, по умолчанию USER): ");
        String roleStr = readLineSafe("");
        UserRole role;
        try {
            role = roleStr.isEmpty() ? UserRole.USER : UserRole.valueOf(roleStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            System.out.println("Неверная роль. Установлена USER.");
            role = UserRole.USER;
        }
        try {
            User u = userService.register(username, password, role);
            System.out.println("Пользователь создан. ID=" + u.getId());
        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }

    private boolean requireAdmin() {
        if (currentUser.getRole() != UserRole.ADMIN) {
            System.out.println("Требуются права администратора.");
            return false;
        }
        return true;
    }

    private Tournament selectTournament() {
        List<Tournament> list = tournamentService.getAll();
        if (list.isEmpty()) {
            System.out.println("Турниров нет.");
            return null;
        }
        for (int i = 0; i < list.size(); i++) {
            System.out.println((i + 1) + ". " + list.get(i).getName());
        }
        int idx = readInt("Выберите турнир: ") - 1;
        if (idx < 0 || idx >= list.size()) {
            System.out.println("Неверный выбор.");
            return null;
        }
        return list.get(idx);
    }

    private Match selectMatch() {
        List<Match> list = matchService.getAllMatches();
        if (list.isEmpty()) {
            System.out.println("Матчей нет.");
            return null;
        }
        for (int i = 0; i < list.size(); i++) {
            System.out.println((i + 1) + ". Матч команд " + list.get(i).getTeam1() + " и " + list.get(i).getTeam2());
        }
        int idx = readInt("Выберите матч: ") - 1;
        if (idx < 0 || idx >= list.size()) {
            System.out.println("Неверный выбор.");
            return null;
        }
        return list.get(idx);
    }

    private void printMatches(List<Match> list) {
        if (list.isEmpty()) {
            System.out.println("Матчей не найдено.");
            return;
        }
        for (Match m : list) {
            Tournament tournament = m.getTournament();
            String team1 = m.getTeam1();
            String team2 = m.getTeam2();
            String matchDate = m.getMatchDate().format(DATETIME_FMT);
            TournamentStage stage = m.getStage();
            MatchStatus status = m.getStatus();
            int score1 = m.getScore1();
            int score2 = m.getScore2();

            String tourName = tournament != null ? tournament.getName() : "Не указан";
            switch (status) {
                case SCHEDULED -> System.out.println("Матч команд " + team1 + " и " + team2 +
                        " запланирован на " + matchDate + " стадии " + stage + "\nТурнир: " + tourName);
                case CANCELLED -> System.out.println("Матч команд " + team1 + " и " + team2 +
                        " отменён\nТурнир: " + tourName);
                case FINISHED -> System.out.println("Матч команд " + team1 + " и " + team2 +
                        " завершён со счётом " + score1 + ":" + score2 + "\nТурнир: " + tourName);
                case LIVE -> System.out.println("Матч команд " + team1 + " и " + team2 +
                        " идёт со счётом " + score1 + ":" + score2 + "\nТурнир: " + tourName);
            }
            System.out.println("===============");
        }
    }

    private String readLineSafe(String prompt) {
        while (true) {
            if (!prompt.isEmpty()) {
                System.out.print(prompt);
            }
            String line = scanner.nextLine();
            if (line.length() > MAX_INPUT_LENGTH) {
                System.out.println("Ошибка: Неверный ввод (слишком длинное предложение, лимит " +
                        MAX_INPUT_LENGTH + " символов). Ввод сброшен.");
                continue;
            }
            return line.trim();
        }
    }

    private String readPassword(String prompt) {
        if (System.console() != null) {
            char[] pass = System.console().readPassword(prompt);
            return pass != null ? new String(pass).trim() : "";
        }
        return readLineSafe(prompt);
    }

    private int readInt(String prompt) {
        while (true) {
            String line = readLineSafe(prompt);
            if (line.length() > 6) {
                System.out.println("Ошибка: Неверный ввод (число слишком большое). Ввод сброшен.");
                continue;
            }
            try {
                int val = Integer.parseInt(line);
                if (val < 0 || val > MAX_SAFE_INT) {
                    System.out.println("Ошибка: Неверный ввод (число вне допустимого диапазона 0.." + MAX_SAFE_INT + ").");
                    continue;
                }
                return val;
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: Неверный ввод. Введите целое число.");
            }
        }
    }

    private String readNonEmpty(String prompt) {
        while (true) {
            String line = readLineSafe(prompt);
            if (!line.isEmpty()) return line;
            System.out.println("Ошибка: Неверный ввод. Поле не может быть пустым.");
        }
    }

    private LocalDate readDate(String prompt) {
        while (true) {
            String line = readLineSafe(prompt);
            try {
                return LocalDate.parse(line, DATE_FMT);
            } catch (DateTimeParseException e) {
                System.out.println("Неверный формат даты (ожидается dd.MM.yyyy).");
            }
        }
    }

    private LocalDateTime readDateTime(String prompt) {
        while (true) {
            String line = readLineSafe(prompt);
            try {
                return LocalDateTime.parse(line, DATETIME_FMT);
            } catch (DateTimeParseException e) {
                System.out.println("Неверный формат даты и времени (ожидается dd.MM.yyyy HH:mm).");
            }
        }
    }

    private TournamentStage readStage() {
        while (true) {
            System.out.print("Этап (GROUP/QUARTER_FINAL/SEMI_FINAL/FINAL): ");
            String line = readLineSafe("").toUpperCase();
            try {
                return TournamentStage.valueOf(line);
            } catch (IllegalArgumentException e) {
                System.out.println("Неверный этап (GROUP/QUARTER_FINAL/SEMI_FINAL/FINAL).");
            }
        }
    }
}
