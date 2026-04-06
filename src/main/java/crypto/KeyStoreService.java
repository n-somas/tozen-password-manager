package crypto;

// ---------- Imports ----------

// Hilfsfunktionen (Base64)
import util.HashUtil;                        // Base64 encode/decode

// Javax Crypto: AES-GCM & PBKDF2
import javax.crypto.Cipher;                  // Ver-/Entschlüsselung
import javax.crypto.KeyGenerator;            // Schlüsselgenerator (AES)
import javax.crypto.SecretKeyFactory;        // PBKDF2-Fabrik
import javax.crypto.spec.GCMParameterSpec;   // GCM-Tag/IV-Parameter
import javax.crypto.spec.PBEKeySpec;         // PBKDF2-Spezifikation
import javax.crypto.spec.SecretKeySpec;      // AES-Key aus Bytes

// Zufall
import java.security.SecureRandom;           // kryptografisch sicherer PRNG

public final class KeyStoreService {

    // Parameter

    private static final int GCM_IV_BYTES = 12;
    private static final int GCM_TAG_BITS = 128;
    private static final int AES_KEY_BITS = 256;
    private static final int PBKDF2_ITERS = 150_000;


    private KeyStoreService() {}

    // Zufallsbytes
    public static byte[] random(int n) {
        byte[] b = new byte[n];
        new SecureRandom().nextBytes(b);
        return b;
        // Hinweis: SecureRandom pro Aufruf neu – kurzlebig und thread-sicher.
    }

    // DEK generieren (AES-256)
    public static byte[] generateDataKey() throws Exception {
        KeyGenerator kg = KeyGenerator.getInstance("AES");
        kg.init(AES_KEY_BITS);
        return kg.generateKey().getEncoded();
    }

    // PBKDF2(HMAC-SHA256) -> KEK (32 Byte)
    public static byte[] deriveKek(char[] secret, byte[] salt) throws Exception {
        PBEKeySpec spec = new PBEKeySpec(secret, salt, PBKDF2_ITERS, AES_KEY_BITS);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        return skf.generateSecret(spec).getEncoded();
    }

    // DEK mit KEK verpacken (AES/GCM)
    public static Wrapped wrapKey(byte[] dek, byte[] kek) throws Exception {
        byte[] iv = random(GCM_IV_BYTES);
        Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
        c.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(kek, "AES"), new GCMParameterSpec(GCM_TAG_BITS, iv));
        byte[] ct = c.doFinal(dek);
        return new Wrapped(ct, iv);
    }

    // DEK mit KEK auspacken (AES/GCM)
    public static byte[] unwrapKey(Wrapped w, byte[] kek) throws Exception {
        Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
        c.init(Cipher.DECRYPT_MODE, new SecretKeySpec(kek, "AES"), new GCMParameterSpec(GCM_TAG_BITS, w.iv));
        return c.doFinal(w.ct);
    }

    // Container: Ciphertext + IV
    public static final class Wrapped {
        private final byte[] ct;
        private final byte[] iv;

        public Wrapped(byte[] ct, byte[] iv) { this.ct = ct; this.iv = iv; }

        // Byte-Getter
        public byte[] ct() { return ct; }
        public byte[] iv() { return iv; }

        // Base64-Getter (DB-Felder)
        public String ctB64() { return HashUtil.encodeBase64(ct); }
        public String ivB64() { return HashUtil.encodeBase64(iv); }

        public static Wrapped fromB64(String ctB64, String ivB64) {
            return new Wrapped(HashUtil.decodeBase64(ctB64), HashUtil.decodeBase64(ivB64));
        }
    }
}

