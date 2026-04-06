package crypto;

import org.junit.jupiter.api.Test;
import util.HashUtil;

import static org.junit.jupiter.api.Assertions.*;

class KeyStoreServiceTest {

    @Test
    void deriveKek_isDeterministicAndHasCorrectLength() throws Exception {
        char[] secret = "DummySecretValue".toCharArray();
        byte[] salt = HashUtil.randomBytes(16);

        byte[] k1 = KeyStoreService.deriveKek(secret, salt);
        byte[] k2 = KeyStoreService.deriveKek(secret, salt);

        assertEquals(32, k1.length, "KEK muss 32 Byte lang sein.");
        assertArrayEquals(k1, k2, "Gleicher Input muss gleichen KEK liefern.");
    }

    @Test
    void wrapAndUnwrap_restoresDek() throws Exception {
        byte[] dek = KeyStoreService.generateDataKey();
        byte[] salt = HashUtil.randomBytes(16);
        byte[] kek = KeyStoreService.deriveKek("Secret!".toCharArray(), salt);

        KeyStoreService.Wrapped wrapped = KeyStoreService.wrapKey(dek, kek);
        byte[] unwrapped = KeyStoreService.unwrapKey(wrapped, kek);

        assertArrayEquals(dek, unwrapped, "Wrap und Unwrap müssen den gleichen DEK liefern.");
    }

    @Test
    void unwrap_withWrongKek_fails() throws Exception {
        byte[] dek = KeyStoreService.generateDataKey();
        byte[] saltOk = HashUtil.randomBytes(16);
        byte[] kekOk = KeyStoreService.deriveKek("Secret!".toCharArray(), saltOk);

        KeyStoreService.Wrapped wrapped = KeyStoreService.wrapKey(dek, kekOk);

        byte[] saltOther = HashUtil.randomBytes(16);
        byte[] kekOther = KeyStoreService.deriveKek("AnderePassphrase".toCharArray(), saltOther);

        assertThrows(Exception.class,
                () -> KeyStoreService.unwrapKey(wrapped, kekOther),
                "Unwrap mit anderem KEK muss fehlschlagen.");
    }
}
