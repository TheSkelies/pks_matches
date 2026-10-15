package pksmatches.util;

/**
 * Простые проверки для полей формы.
 */
public final class ValidationUtil {

    private ValidationUtil() {
    }

    public static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    public static String requireNonBlank(String value, String fieldName) {
        if (isBlank(value)) {
            throw new IllegalArgumentException("Поле \"" + fieldName + "\" обязательно для заполнения");
        }
        return value.trim();
    }

    public static int parseScore(String raw, String fieldName) {
        if (isBlank(raw)) {
            throw new IllegalArgumentException("Поле \"" + fieldName + "\" обязательно для заполнения");
        }
        try {
            int v = Integer.parseInt(raw.trim());
            if (v < 0) {
                throw new IllegalArgumentException("Счёт не может быть отрицательным");
            }
            if (v > 999) {
                throw new IllegalArgumentException("Счёт слишком большой (макс. 999)");
            }
            return v;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Поле \"" + fieldName + "\" должно быть целым числом");
        }
    }

    public static int parseScoreOrZero(String raw) {
        if (isBlank(raw)) return 0;
        try {
            int v = Integer.parseInt(raw.trim());
            return Math.max(0, v);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Счёт должен быть целым числом");
        }
    }
}
