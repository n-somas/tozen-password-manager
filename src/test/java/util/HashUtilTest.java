package util;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class HashUtilTest {

    @Test
    void hashWithSalt_sameInputSameSalt_sameHash() {
        byte[] salt = HashUtil.generateSalt();
        String pw = "Test123!";

        String h1 = HashUtil.hashWithSalt(pw, salt);
        String h2 = HashUtil.hashWithSalt(pw, salt);

        assertEquals(h1, h2, "Gleicher Input und gleiches Salt müssen den gleichen Hash liefern.");
    }

    @Test
    void hashWithSalt_differentSalt_differentHash() {
        String pw = "Test123!";

        String h1 = HashUtil.hashWithSalt(pw, HashUtil.generateSalt());
        String h2 = HashUtil.hashWithSalt(pw, HashUtil.generateSalt());

        assertNotEquals(h1, h2, "Verschiedene Salts sollen unterschiedliche Hashes erzeugen.");
    }

    @Test
    void pbkdf2_generatesKeyWithExpectedLength() {
        char[] pw = "MasterPasswort!123".toCharArray();
        byte[] salt = HashUtil.generateSalt();

        // bewusst moderat gewählt, damit der Test nicht ewig läuft
        int iterations = 10_000;
        int keyBits = 256;

        byte[] key = HashUtil.pbkdf2(pw, salt, iterations, keyBits);

        assertEquals(keyBits / 8, key.length, "PBKDF2-Keylänge muss 256 Bit entsprechen.");
    }

    @Test
    void aesGcm_encryptDecrypt_roundtrip() {
        byte[] key = HashUtil.randomBytes(32);
        byte[] iv = HashUtil.randomBytes(12);
        byte[] plain = "Hallo Tozen".getBytes(StandardCharsets.UTF_8);

        String cipherB64 = HashUtil.encryptAesGcmToBase64(plain, key, iv);
        byte[] decrypted = HashUtil.decryptAesGcmFromBase64(cipherB64, key, iv);

        assertArrayEquals(plain, decrypted, "AES-GCM muss den Klartext wiederherstellen.");
    }

    @Test
    void aesGcm_wrongKey_throwsRuntimeException() {
        byte[] key = HashUtil.randomBytes(32);
        byte[] iv = HashUtil.randomBytes(12);
        byte[] plain = "Hallo Tozen".getBytes(StandardCharsets.UTF_8);

        String cipherB64 = HashUtil.encryptAesGcmToBase64(plain, key, iv);
        byte[] wrongKey = HashUtil.randomBytes(32);

        assertThrows(RuntimeException.class,
                () -> HashUtil.decryptAesGcmFromBase64(cipherB64, wrongKey, iv),
                "Entschlüsselung mit falschem Key muss fehlschlagen.");
    }
}
