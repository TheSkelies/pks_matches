package pksmatches.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import pksmatches.model.Match;
import pksmatches.model.Tournament;
import pksmatches.model.User;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Экспорт данных в Excel (.xlsx).
 * Создаёт один файл с несколькими листами:
 *   - Турниры
 *   - Матчи
 *   - Пользователи
 *   - Статистика
 */
public class ExcelExporter {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    /**
     * Экспортирует всё в один Excel-файл.
     *
     * @param filePath     путь к файлу (например, "export.xlsx")
     * @param tournaments  список турниров
     * @param matches      список матчей
     * @param users        список пользователей
     * @param statistics   карта статистики
     */
    public static void exportAll(String filePath,
                                 List<Tournament> tournaments,
                                 List<Match> matches,
                                 List<User> users,
                                 Map<String, Integer> statistics) throws IOException {

        try (Workbook workbook = new XSSFWorkbook()) {

            createTournamentsSheet(workbook, tournaments);
            createMatchesSheet(workbook, matches);
            createUsersSheet(workbook, users);
            createStatisticsSheet(workbook, statistics);

            try (FileOutputStream out = new FileOutputStream(filePath)) {
                workbook.write(out);
            }
        }
    }

    // ---------- Лист "Турниры" ----------

    private static void createTournamentsSheet(Workbook workbook, List<Tournament> tournaments) {
        Sheet sheet = workbook.createSheet("Турниры");
        String[] headers = {"ID", "Название", "Дата начала", "Дата окончания"};
        createHeaderRow(sheet, headers);

        int rowIdx = 1;
        for (Tournament t : tournaments) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(t.getId());
            row.createCell(1).setCellValue(t.getName());
            row.createCell(2).setCellValue(t.getStartDate() != null ? t.getStartDate().format(DATE_FMT) : "");
            row.createCell(3).setCellValue(t.getEndDate() != null ? t.getEndDate().format(DATE_FMT) : "—");
        }
        autoSize(sheet, headers.length);
    }

    // ---------- Лист "Матчи" ----------

    private static void createMatchesSheet(Workbook workbook, List<Match> matches) {
        Sheet sheet = workbook.createSheet("Матчи");
        String[] headers = {
                "ID", "Турнир", "Команда 1", "Команда 2",
                "Дата и время", "Этап", "Статус", "Счёт 1", "Счёт 2", "Результат"
        };
        createHeaderRow(sheet, headers);

        int rowIdx = 1;
        for (Match m : matches) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(m.getId());
            row.createCell(1).setCellValue(m.getTournament() != null ? m.getTournament().getName() : "");
            row.createCell(2).setCellValue(m.getTeam1());
            row.createCell(3).setCellValue(m.getTeam2());
            row.createCell(4).setCellValue(m.getMatchDate() != null ? m.getMatchDate().format(DATETIME_FMT) : "");
            row.createCell(5).setCellValue(m.getStage() != null ? m.getStage().name() : "");
            row.createCell(6).setCellValue(m.getStatus() != null ? m.getStatus().name() : "");
            row.createCell(7).setCellValue(m.getScore1());
            row.createCell(8).setCellValue(m.getScore2());
            row.createCell(9).setCellValue(buildResult(m));
        }
        autoSize(sheet, headers.length);
    }

    private static String buildResult(Match m) {
        if (m.getStatus() == null) return "";
        switch (m.getStatus()) {
            case SCHEDULED:
                return "Запланирован";
            case LIVE:
                return "Идёт";
            case FINISHED:
                if (m.getScore1() > m.getScore2()) return "Победа: " + m.getTeam1();
                if (m.getScore1() < m.getScore2()) return "Победа: " + m.getTeam2();
                return "Ничья";
            case CANCELLED:
                return "Отменён";
            default:
                return "";
        }
    }

    // ---------- Лист "Пользователи" ----------

    private static void createUsersSheet(Workbook workbook, List<User> users) {
        Sheet sheet = workbook.createSheet("Пользователи");
        String[] headers = {"ID", "Логин", "Роль"};
        createHeaderRow(sheet, headers);

        int rowIdx = 1;
        for (User u : users) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(u.getId());
            row.createCell(1).setCellValue(u.getUsername());
            row.createCell(2).setCellValue(u.getRole() != null ? u.getRole().name() : "");
        }
        autoSize(sheet, headers.length);
    }

    // ---------- Лист "Статистика" ----------

    private static void createStatisticsSheet(Workbook workbook, Map<String, Integer> statistics) {
        Sheet sheet = workbook.createSheet("Статистика");
        String[] headers = {"Показатель", "Значение"};
        createHeaderRow(sheet, headers);

        int rowIdx = 1;
        for (Map.Entry<String, Integer> entry : statistics.entrySet()) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(entry.getKey());
            row.createCell(1).setCellValue(entry.getValue());
        }
        autoSize(sheet, headers.length);
    }

    // ---------- Вспомогательные методы ----------

    private static void createHeaderRow(Sheet sheet, String[] headers) {
        Row headerRow = sheet.createRow(0);
        CellStyle headerStyle = sheet.getWorkbook().createCellStyle();
        Font headerFont = sheet.getWorkbook().createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
    }

    private static void autoSize(Sheet sheet, int columnCount) {
        for (int i = 0; i < columnCount; i++) {
            sheet.autoSizeColumn(i);
        }
    }
}