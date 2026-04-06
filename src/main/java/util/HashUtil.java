package util;

import javax.crypto.Cipher;                       // Kernklasse für Ver- und Entschlüsselung
import javax.crypto.SecretKeyFactory;             // Zum Erzeugen von SecretKeys (z. B. aus Passwörtern)
import javax.crypto.spec.GCMParameterSpec;        // Spezifikation für AES-GCM (IV + Tag-Länge)
import javax.crypto.spec.PBEKeySpec;              // Passwortbasiertes Schlüsselmaterial (PBKDF2)
import javax.crypto.spec.SecretKeySpec;           // Spezifikation für AES-Keys


import java.security.SecureRandom;                // Kryptografisch sicherer Zufallszahlengenerator


import java.util.Base64;                          // Kodieren/Dekodieren von Bytes zu/von Base64

public class HashUtil {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String AES_ALGO = "AES";
    private static final String AES_GCM_ALGO = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128; // Bits

    // Zufällige Bytes generieren
    public static byte[] randomBytes(int length) {
        byte[] bytes = new byte[length];
        RANDOM.nextBytes(bytes);
        return bytes;
    }

    // Salt generieren
    public static byte[] generateSalt() {
        return randomBytes(16);
    }

    public static String encodeBase64(byte[] data) {
        return Base64.getEncoder().encodeToString(data);
    }

    public static byte[] decodeBase64(String data) {
        return Base64.getDecoder().decode(data);
    }

    public static String hashWithSalt(String password, byte[] salt) {
        try {
            byte[] hash = pbkdf2(password.toCharArray(), salt, 65536, 256);
            return encodeBase64(hash);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] pbkdf2(char[] password, byte[] salt, int iterations, int keyLength) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyLength);
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            return skf.generateSecret(spec).getEncoded();
        } catch (Exception e) {
            throw new RuntimeException("PBKDF2 derivation failed", e);
        }
    }

    // AES-GCM Verschlüsselung mit Base64-Ausgabe
    public static String encryptAesGcmToBase64(byte[] plaintext, byte[] key, byte[] iv) {
        try {
            Cipher cipher = Cipher.getInstance(AES_GCM_ALGO);
            SecretKeySpec keySpec = new SecretKeySpec(key, AES_ALGO);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmSpec);
            byte[] encrypted = cipher.doFinal(plaintext);
            return encodeBase64(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("AES-GCM encryption failed", e);
        }
    }

    // AES-GCM Entschlüsselung von Base64-Eingabe
    public static byte[] decryptAesGcmFromBase64(String base64CipherText, byte[] key, byte[] iv) {
        try {
            Cipher cipher = Cipher.getInstance(AES_GCM_ALGO);
            SecretKeySpec keySpec = new SecretKeySpec(key, AES_ALGO);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec);
            byte[] decodedCipher = decodeBase64(base64CipherText);
            return cipher.doFinal(decodedCipher);
        } catch (Exception e) {
            throw new RuntimeException("AES-GCM decryption failed", e);
        }
    }
}


