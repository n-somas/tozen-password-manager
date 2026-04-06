package util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordGeneratorTest {

    @Test
    void generate_respectsLength() {
        String pw = PasswordGenerator.generate(16, true, true, true, true);
        assertEquals(16, pw.length(), "Generiertes Passwort muss die gewünschte Länge haben.");
    }

    @Test
    void generate_onlyLowercaseWhenConfigured() {
        String pw = PasswordGenerator.generate(20, true, false, false, false);

        assertTrue(pw.matches("[a-z]+"),
                "Wenn nur Kleinbuchstaben aktiv sind, darf nichts anderes vorkommen.");
    }

    @Test
    void generate_onlyDigitsWhenConfigured() {
        String pw = PasswordGenerator.generate(20, false, false, true, false);

        assertTrue(pw.matches("[0-9]+"),
                "Wenn nur Ziffern aktiv sind, darf nichts anderes vorkommen.");
    }

    @Test
    void strengthScore_ratesWeakPasswordLowerThanStrongPassword() {
        int weak = PasswordGenerator.strengthScore("123456");
        int strong = PasswordGenerator.strengthScore("Abcdef12!");

        assertTrue(weak < strong, "Starke Passwörter müssen höher bewertet werden.");
    }

    @Test
    void strengthScore100_scalesFromStrengthScore() {
        String pw = "Abcdef12!";
        int base = PasswordGenerator.strengthScore(pw);
        int scaled = PasswordGenerator.strengthScore100(pw);

        assertEquals(base * 25, scaled, "StrengthScore100 muss linear aus StrengthScore abgeleitet werden.");
    }
}
