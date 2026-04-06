package controller;


import app.AppState;

// JavaFX: UI & Controls
import javafx.fxml.FXML;                        // @FXML-Bindings
import javafx.fxml.FXMLLoader;                  // FXML laden
import javafx.scene.Parent;                     // Root-Knoten
import javafx.scene.Scene;                      // Szene
import javafx.stage.Stage;                      // Fenster
import javafx.scene.input.MouseButton;          // Rechtsklick-Erkennung
import javafx.scene.control.*;                  // TableView, Button, Alert, etc.
import javafx.scene.control.cell.PropertyValueFactory; // Spalten-Bindings
import javafx.scene.control.TableCell;          // Custom-Zellen
import javafx.scene.control.TableRow;           // Zeilen (Kontextmenü)

// JavaFX: Collections & Filter/Sort
import javafx.collections.FXCollections;        // ObservableList-Erzeugung
import javafx.collections.ObservableList;       // Observable Daten
import javafx.collections.transformation.FilteredList; // Suche/Filter
import javafx.collections.transformation.SortedList;   // Sortierung

// Modell & DB
import model.VaultEntry;                        // Eintragsmodell
import db.DatabaseHelper;                       // CRUD auf Nitrite-DB

// Krypto/Helfer
import util.EncryptionUtil;                     // Ver-/Entschlüsselung

// Java: IO
import java.io.IOException;                     // FXMLLoader.load()

public class VaultController {

    // FXML-UI
    @FXML private TableView<VaultEntry> vaultTable;
    @FXML private TableColumn<VaultEntry, String> websiteColumn;
    @FXML private TableColumn<VaultEntry, String> usernameColumn;
    @FXML private TableColumn<VaultEntry, String> passwordColumn;
    @FXML private TableColumn<VaultEntry, Void> actionColumn;  // Button-Spalte
    @FXML private TextField searchField;                       // Suche

    private final ObservableList<VaultEntry> entries = FXCollections.observableArrayList();

    // Init: Spalten, Button-Spalte, Daten + Suche/Sortierung, Kontextmenü
    @FXML
    public void initialize() {
        // Spalten-Bindings
        websiteColumn.setCellValueFactory(new PropertyValueFactory<>("website"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        passwordColumn.setCellValueFactory(new PropertyValueFactory<>("passwordMasked"));

        // Button-Spalte „Anzeigen“ (Entschlüsseln mit DEK)
        actionColumn.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("Anzeigen");
            {
                btn.setOnAction(e -> {
                    VaultEntry entry = getTableView().getItems().get(getIndex());
                    byte[] dek = AppState.getInstance().getDataKey();
                    if (dek == null || dek.length == 0) {
                        info("Fehler", "Kein Daten-Schlüssel (neu einloggen).");
                        return;
                    }
                    try {
                        String plain = EncryptionUtil.decrypt(entry.getPasswordEncrypted(), dek);
                        info("Passwort für " + entry.getWebsite(), plain);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        info("Fehler", "Entschlüsselung fehlgeschlagen: " + ex.getMessage());
                    }
                });
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });

        // Daten laden
        reloadFromDb();

        // Suche/Filter
        FilteredList<VaultEntry> filtered = new FilteredList<>(entries, e -> true);
        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldV, newV) -> {
                String q = (newV == null ? "" : newV.trim().toLowerCase());
                filtered.setPredicate(e -> {
                    if (q.isEmpty()) return true;
                    String w = e.getWebsite()  == null ? "" : e.getWebsite().toLowerCase();
                    String u = e.getUsername() == null ? "" : e.getUsername().toLowerCase();
                    return w.contains(q) || u.contains(q);
                });
            });
        }

        // Sortierung
        SortedList<VaultEntry> sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(vaultTable.comparatorProperty());
        vaultTable.setItems(sorted);

        // Kontextmenü
        ContextMenu cm = new ContextMenu();
        MenuItem miEdit = new MenuItem("Bearbeiten");
        MenuItem miDelete = new MenuItem("Löschen");
        cm.getItems().addAll(miEdit, miDelete);
        miEdit.setOnAction(e -> onEditSelected());
        miDelete.setOnAction(e -> onDeleteSelected());

        vaultTable.setRowFactory(tv -> {
            TableRow<VaultEntry> row = new TableRow<>();
            row.setOnContextMenuRequested(ev -> {
                if (!row.isEmpty()) {
                    vaultTable.getSelectionModel().select(row.getIndex());
                    cm.show(row, ev.getScreenX(), ev.getScreenY());
                }
            });
            row.setOnMouseClicked(ev -> {
                if (ev.getButton() == MouseButton.SECONDARY && !row.isEmpty()) {
                    vaultTable.getSelectionModel().select(row.getIndex());
                    cm.show(row, ev.getScreenX(), ev.getScreenY());
                } else {
                    cm.hide();
                }
            });
            return row;
        });
    }

    // DB -> ObservableList
    private void reloadFromDb() {
        entries.clear();
        for (VaultEntry e : DatabaseHelper.findAll()) {
            entries.add(e);
        }
    }

    // Dialog: Neuer Eintrag
    @FXML
    private void onAddEntry() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/add_entry.fxml"));
            Parent root = loader.load();

            AddEntryController controller = loader.getController();
            controller.setVaultController(this);

            Stage stage = new Stage();
            stage.setTitle("Neuer Eintrag");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Kontextmenü: Bearbeiten
    private void onEditSelected() {
        VaultEntry sel = vaultTable.getSelectionModel().getSelectedItem();
        if (sel == null) {
            info("Hinweis", "Bitte einen Eintrag auswählen.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/add_entry.fxml"));
            Parent root = loader.load();

            AddEntryController controller = loader.getController();
            controller.setVaultController(this);
            controller.setEditEntry(sel); // Edit-Mode + Vorbelegung

            Stage stage = new Stage();
            stage.setTitle("Eintrag bearbeiten");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException ex) {
            ex.printStackTrace();
            info("Fehler", "Bearbeitungsdialog konnte nicht geöffnet werden.");
        }
    }

    // Kontextmenü: Löschen
    private void onDeleteSelected() {
        VaultEntry sel = vaultTable.getSelectionModel().getSelectedItem();
        if (sel == null) {
            info("Hinweis", "Bitte einen Eintrag auswählen.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Löschen bestätigen");
        confirm.setHeaderText(null);
        confirm.setContentText("Eintrag \"" + sel.getWebsite() + "\" wirklich löschen?");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                DatabaseHelper.deleteByWebsite(sel.getWebsite());
                entries.remove(sel);
            }
        });
    }

    // Aus Dialog „Neuer Eintrag“: an DB + Liste anhängen
    public void addVaultEntry(VaultEntry entry) {
        DatabaseHelper.addEntry(entry);
        entries.add(entry);
    }

    // Aus Dialog „Bearbeiten“: updaten/umbenennen
    public void updateVaultEntry(String oldWebsite, VaultEntry updated) {
        if (!oldWebsite.equals(updated.getWebsite())) {
            DatabaseHelper.deleteByWebsite(oldWebsite);
            DatabaseHelper.addEntry(updated);
            entries.removeIf(v -> v.getWebsite().equals(oldWebsite));
            entries.add(updated);
        } else {
            DatabaseHelper.upsert(updated);
            for (int i = 0; i < entries.size(); i++) {
                if (entries.get(i).getWebsite().equals(updated.getWebsite())) {
                    entries.set(i, updated);
                    break;
                }
            }
        }
    }

    // Info-Dialog
    private void info(String title, String content) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(content);
        a.showAndWait();
    }
}
