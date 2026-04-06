package model;


import java.util.Arrays;

public class User {


    private String username;       // Benutzername
    private String passwordHash;   // Passwort-Hash (mit Salt generiert)
    private byte[] salt;           // Salt für das Hashing des Passworts
    private String recoveryKey;    // Recovery-Key für Passwortwiederherstellung


    public User() { }


    public User(String username, String passwordHash, byte[] salt, String recoveryKey) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.salt = salt;
        this.recoveryKey = recoveryKey;
    }


    public User(String username, String passwordHash, byte[] salt) {
        this(username, passwordHash, salt, null);
    }

    // ---------- Getter & Setter ----------

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public byte[] getSalt() { return salt; }
    public void setSalt(byte[] salt) { this.salt = salt; }

    public String getRecoveryKey() { return recoveryKey; }
    public void setRecoveryKey(String recoveryKey) { this.recoveryKey = recoveryKey; }



    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", passwordHash='" + passwordHash + '\'' +
                ", salt=" + Arrays.toString(salt) +
                ", recoveryKey='" + recoveryKey + '\'' +
                '}';
    }
}

