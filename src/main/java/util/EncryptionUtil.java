package util;


import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

// SecureRandom: Für sichere Initialisierungsvektoren (IV)
import java.security.SecureRandom;

// Base64: Für sichere Textspeicherung der verschlüsselten Daten
import java.util.Base64;


public final class EncryptionUtil {

    // ---------- Konstanten ----------
    private static final int GCM_IV_BYTES = 12;   // Länge des IV in Bytes
    private static final int GCM_TAG_BITS = 128;  // Länge des Authentifizierungs-Tags

    // Privater Konstruktor verhindert Instanziierung
    private EncryptionUtil() {}


    public static String encrypt(String plain, byte[] dataKey) {
        try {
            // IV generieren
            byte[] iv = new byte[GCM_IV_BYTES];
            new SecureRandom().nextBytes(iv);

            // Cipher initialisieren (AES-GCM)
            Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
            c.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(dataKey, "AES"), new GCMParameterSpec(GCM_TAG_BITS, iv));

            // Klartext verschlüsseln
            byte[] ct = c.doFinal(plain.getBytes(java.nio.charset.StandardCharsets.UTF_8));

            // IV + Ciphertext kombinieren
            byte[] out = new byte[iv.length + ct.length];
            System.arraycopy(iv, 0, out, 0, iv.length);
            System.arraycopy(ct, 0, out, iv.length, ct.length);

            // Ergebnis als Base64 zurückgeben
            return Base64.getEncoder().encodeToString(out);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public static String decrypt(String blobB64, byte[] dataKey) {
        try {
            // Base64 in Byte-Array dekodieren
            byte[] blob = Base64.getDecoder().decode(blobB64);

            // IV und Ciphertext extrahieren
            byte[] iv = java.util.Arrays.copyOfRange(blob, 0, GCM_IV_BYTES);
            byte[] ct = java.util.Arrays.copyOfRange(blob, GCM_IV_BYTES, blob.length);

            // Cipher initialisieren (AES-GCM)
            Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
            c.init(Cipher.DECRYPT_MODE, new SecretKeySpec(dataKey, "AES"), new GCMParameterSpec(GCM_TAG_BITS, iv));

            // Ciphertext entschlüsseln
            byte[] pt = c.doFinal(ct);

            // Ergebnis als UTF-8-String zurückgeben
            return new String(pt, java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
