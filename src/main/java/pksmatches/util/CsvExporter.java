package pksmatches.util;

import pksmatches.model.Match;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Экспорт списка матчей в CSV.
 * Разделитель — точка с запятой (удобно для Excel в русской локали).
 * Кодировка — UTF-8 с BOM, чтобы Excel корректно открыл русские буквы.
 */
public final class CsvExporter {

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private CsvExporter() {
    }

    public static void exportMatches(List<Match> matches, File file) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {

            // BOM для Excel
            writer.write('\uFEFF');

            writer.write("ID;Турнир;Команда 1;Команда 2;Дата и время;Этап;Статус;Счёт 1;Счёт 2");
            writer.newLine();

            for (Match m : matches) {
                writer.write(String.valueOf(m.getId()));
                writer.write(';');
                writer.write(escape(m.getTournament().getName()));
                writer.write(';');
                writer.write(escape(m.getTeam1()));
                writer.write(';');
                writer.write(escape(m.getTeam2()));
                writer.write(';');
                writer.write(m.getMatchDate().format(DT_FMT));
                writer.write(';');
                writer.write(m.getStage().name());
                writer.write(';');
                writer.write(m.getStatus().name());
                writer.write(';');
                writer.write(String.valueOf(m.getScore1()));
                writer.write(';');
                writer.write(String.valueOf(m.getScore2()));
                writer.newLine();
            }
        }
    }

    private static String escape(String value) {
        if (value == null) return "";
        String v = value.replace("\"", "\"\"");
        if (v.contains(";") || v.contains("\"") || v.contains("\n")) {
            return "\"" + v + "\"";
        }
        return v;
    }
}
