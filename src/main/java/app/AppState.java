package app;

import java.util.Arrays;

public final class AppState {


    private AppState() {}
    private static class Holder { private static final AppState INSTANCE = new AppState(); }
    public static AppState getInstance() { return Holder.INSTANCE; }

    // --- State ---
    private volatile String currentUser;
    private char[] masterPassword; // im Speicher nur so kurz wie nötig halten!
    private byte[] dataKey;

    // --- User ---
    public String getCurrentUser() { return currentUser; }

    public void setCurrentUser(String username) {
        this.currentUser = (username == null || username.isBlank()) ? null : username;
    }

    public boolean isLoggedIn() {
        return currentUser != null && !currentUser.isBlank();
    }


    public void login(String username, char[] masterPw) {
        setCurrentUser(username);
        setMasterPassword(masterPw);
    }

    public void logout() {
        clearSensitive();
    }

    // --- Master-Passwort ---
    public void setMasterPassword(char[] pw) {

        if (this.masterPassword != null) {
            Arrays.fill(this.masterPassword, '\0');
        }
        if (pw == null) {
            this.masterPassword = null;
        } else {
            this.masterPassword = Arrays.copyOf(pw, pw.length);
        }
    }


    public char[] getMasterPassword() {
        return masterPassword == null ? null : Arrays.copyOf(masterPassword, masterPassword.length);
    }

    // --- Data Key (DEK) ---
    public void setDataKey(byte[] key) {
        // altes sicher löschen
        if (this.dataKey != null) {
            Arrays.fill(this.dataKey, (byte) 0);
        }
        if (key == null) {
            this.dataKey = null;
        } else {
            this.dataKey = Arrays.copyOf(key, key.length);
        }
    }


    public byte[] getDataKey() {
        return dataKey == null ? null : Arrays.copyOf(dataKey, dataKey.length);
    }

    /**
     * Löscht ALLE sensiblen Daten aus dem Speicher.
     */
    public void clearSensitive() {
        if (masterPassword != null) {
            Arrays.fill(masterPassword, '\0');
            masterPassword = null;
        }
        if (dataKey != null) {
            Arrays.fill(dataKey, (byte) 0);
            dataKey = null;
        }
        currentUser = null;
    }
}


