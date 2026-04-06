package util;



// Kryptografisch sicherer Zufallszahlengenerator
import java.security.SecureRandom;

public class PasswordGenerator {

    // Zeichenvorräte
    private static final String LOWER   = "abcdefghijklmnopqrstuvwxyz";
    private static final String UPPER   = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String DIGITS  = "0123456789";
    private static final String SYMBOLS = "!@#$%^&*()-_=+[]{};:,.<>?";

    // Zufallsquelle
    private static final SecureRandom random = new SecureRandom();


    public static String generate(int length, boolean useLower, boolean useUpper, boolean useDigits, boolean useSymbols) {
        StringBuilder charPool = new StringBuilder();
        if (useLower)  charPool.append(LOWER);
        if (useUpper)  charPool.append(UPPER);
        if (useDigits) charPool.append(DIGITS);
        if (useSymbols) charPool.append(SYMBOLS);

        if (charPool.length() == 0) {
            throw new IllegalArgumentException("Mindestens ein Zeichentyp muss ausgewählt sein.");
        }

        StringBuilder password = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(charPool.length());
            password.append(charPool.charAt(index));
        }
        return password.toString();
    }

    /**
     * Einfache Passwortstärke-Bewertung 0..4
     * 0 = sehr schwach, 4 = sehr stark
     */
    public static int strengthScore(String password) {
        if (password == null || password.isEmpty()) return 0;

        int score = 0;
        if (password.length() >= 8)  score++;
        if (password.length() >= 12) score++;
        if (password.matches(".*[A-Z].*")) score++;
        if (password.matches(".*[0-9].*")) score++;
        if (password.matches(".*[!@#$%^&*()\\-_=+\\[\\]{};:,.<>?].*")) score++;

        // Maximal 4 Punkte zurückgeben
        return Math.min(score, 4);
    }

    /**
     * OPTIONAL: Stärke-Skala 0..100 (für UI-Anzeigen).
     * Skaliert die 0..4-Bewertung linear auf Prozent.
     */
    public static int strengthScore100(String password) {
        return strengthScore(password) * 25; // 0,25,50,75,100
    }
}
