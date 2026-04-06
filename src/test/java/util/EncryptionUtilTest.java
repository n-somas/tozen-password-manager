package util;

import org.junit.jupiter.api.Test;

import java.security.SecureRandom;

import static org.junit.jupiter.api.Assertions.*;

class EncryptionUtilTest {

    private static byte[] randomKey() {
        byte[] key = new byte[32]; // 256 Bit
        new SecureRandom().nextBytes(key);
        return key;
    }

    @Test
    void encryptDecrypt_roundtrip_returnsOriginalPlaintext() {
        String plain = "MeinTestPasswort-äöüß";
        byte[] key = randomKey();

        String encrypted = EncryptionUtil.encrypt(plain, key);
        assertNotEquals(plain, encrypted, "Ciphertext darf nicht wie Klartext aussehen.");

        String decrypted = EncryptionUtil.decrypt(encrypted, key);
        assertEquals(plain, decrypted, "Entschlüsselung muss den Originaltext liefern.");
    }

    @Test
    void decryptWithWrongKey_throwsRuntimeException() {
        String plain = "Noch ein Passwort";
        byte[] key = randomKey();
        byte[] wrongKey = randomKey();

        String encrypted = EncryptionUtil.encrypt(plain, key);

        assertThrows(RuntimeException.class,
                () -> EncryptionUtil.decrypt(encrypted, wrongKey),
                "Entschlüsselung mit falschem Key muss fehlschlagen.");
    }

    @Test
    void encrypt_sameInputSameKey_producesDifferentCiphertexts() {
        String plain = "GleichesPasswort";
        byte[] key = randomKey();

        String c1 = EncryptionUtil.encrypt(plain, key);
        String c2 = EncryptionUtil.encrypt(plain, key);

        assertNotEquals(c1, c2, "Durch zufällige IVs darf der Ciphertext nicht identisch sein.");
    }
}
