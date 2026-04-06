package model;



// Nitrite-Datenbank-Annotation
import org.dizitart.no2.objects.Id; // Kennzeichnung des Primärschlüssels (hier: website)


public class VaultEntry {

    // ---------- Felder ----------
    @Id
    private String website;                // Primärschlüssel: Domain/Website
    private String username;               // Benutzername für die Website
    private String passwordEncrypted;      // Passwort (AES-verschlüsselt gespeichert)



    // Standardkonstruktor (für Nitrite zwingend erforderlich)
    public VaultEntry() { }

    // Konstruktor für das Anlegen neuer Einträge
    public VaultEntry(String website, String username, String passwordEncrypted) {
        this.website = website;
        this.username = username;
        this.passwordEncrypted = passwordEncrypted;
    }

    // ---------- Getter & Setter ----------

    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordEncrypted() { return passwordEncrypted; }
    public void setPasswordEncrypted(String passwordEncrypted) { this.passwordEncrypted = passwordEncrypted; }




    public String getPasswordMasked() { return "••••••"; }
}
