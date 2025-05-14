package com.example.demo;

import org.bson.Document;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import static com.mongodb.client.model.Filters.eq;
import com.mongodb.client.result.UpdateResult;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;


public class DataKelompokControl {
    @FXML private StackPane Popkelompok;
    @FXML private TextField txtcari;
    @FXML private Label lbpopup;
    @FXML private ComboBox<String> cmbnama,cmbshift,cmbhari;
    @FXML private Button btntambah, btnbatal, btnsimpan, btnhapus, btnedit, btncari;
    @FXML private ImageView imtambah, imcari;
    @FXML private TableView<Kelompok> tabelkelompok;
    @FXML private TableColumn<Kelompok, String> colNo, colNama, colShift, colHari;    

    private ObservableList<Kelompok> listKelompok = FXCollections.observableArrayList();

    private MongoDatabase database;

    public DataKelompokControl() {
        database = MongoDBConnection.getDatabase();
    }

public void initialize() {
    ObservableList<String> Hari = FXCollections.observableArrayList(
        "Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu", "Minggu"
        
    );
    Popkelompok.setVisible(false);
    populateComboBox(cmbnama, "Karyawan", "nama");
    populateComboBox(cmbshift, "JamKerja", "shift");
    cmbhari.setItems(Hari);

        colNo.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : String.valueOf(getIndex() + 1));
            }
        });

        colNama.setCellValueFactory(cellData -> cellData.getValue().namaProperty());
        colShift.setCellValueFactory(cellData -> cellData.getValue().shiftProperty());
        colHari.setCellValueFactory(cellData -> cellData.getValue().hariProperty());
    btnedit.setVisible(false); 
    btnhapus.setVisible(false);
    

    btntambah.setOnAction((actionEvent) -> {
        showPopup(Popkelompok);
    });
    imtambah.setOnMouseClicked((actionEvent) -> {
        showPopup(Popkelompok);
    });
    btnbatal.setOnAction((actionEvent) -> {
        hidePopup(Popkelompok);
    });
    btncari.setOnAction(event -> {
        handlecari();
    });
    imcari.setOnMouseClicked((ActionEvent) -> {
        handlecari();
    });
    btnsimpan.setOnAction(event -> {
        if (lbpopup.getText().equals("Data Kelompok Kerja")) {
            handleSimpan();
            hidePopup(Popkelompok);
            clearFields();
        } else {
            handleUpdate();
            hidePopup(Popkelompok);
            clearFields();
        }
    });
   

    tabelkelompok.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
        if (newSelection != null) {
            showButtons(btnedit, btnhapus);
            btnedit.setOnAction(event -> {
                HandleEdit();
            });
            btnhapus.setOnAction(event -> handleHapus());
        } else {
            hideButtons(btnedit, btnhapus);
        }
    });

    tabelkelompok.focusedProperty().addListener((obs, oldVal, newVal) -> {
        if (!newVal) { 
            hideButtons(btnedit, btnhapus);
        }
        else {
            if (tabelkelompok.getSelectionModel().getSelectedItem() != null) {
                showButtons(btnedit, btnhapus);
            }
        }
    });

    loadDataKaryawan();
}

    private void HandleEdit() {
        Kelompok selectedKelompok = tabelkelompok.getSelectionModel().getSelectedItem();
        if (selectedKelompok != null) {
            lbpopup.setText("Edit  Data Kelompok Kerja");
            cmbnama.setValue(selectedKelompok.getNama());
            cmbshift.setValue(selectedKelompok.getshift());
            cmbhari.setValue(selectedKelompok.getHari());
            showPopup(Popkelompok);
        
        } else {
            showAlert("Pilih kelompok yang ingin diedit!", Alert.AlertType.WARNING);
        }
        
    }

 private void handleUpdate() {
    Kelompok selectedKelompok = tabelkelompok.getSelectionModel().getSelectedItem();
    if (selectedKelompok != null) {
        String nama = cmbnama.getValue();
        String shift = cmbshift.getValue();
        String hari = cmbhari.getValue();

        if (nama == null || shift == null || hari == null ||
            nama.isEmpty() || shift.isEmpty() || hari.isEmpty()) {
            showAlert("Semua field harus diisi!", Alert.AlertType.ERROR);
            return;
        }

        MongoCollection<Document> kelompokCollection = database.getCollection("KelompokKerja");

        // Filter berdasarkan nama dan hari
        Document filter = new Document("nama", nama).append("hari", hari);

        // Data yang akan diupdate hanya shift-nya
        Document updatedData = new Document("$set", new Document("shift", shift));

        UpdateResult result = kelompokCollection.updateOne(filter, updatedData);

        System.out.println("Matched: " + result.getMatchedCount() + ", Modified: " + result.getModifiedCount());

        if (result.getMatchedCount() == 0) {
            showAlert("Data tidak ditemukan untuk diperbarui!", Alert.AlertType.WARNING);
            return;
        }

        // Update local object dan refresh tabel
        selectedKelompok.setNama(nama);
        selectedKelompok.setshift(shift);
        selectedKelompok.setHari(hari);
        tabelkelompok.refresh();

        hidePopup(Popkelompok);
        showAlert("Data kelompok berhasil diperbarui!", Alert.AlertType.INFORMATION);
    } else {
        showAlert("Pilih kelompok yang ingin diperbarui!", Alert.AlertType.WARNING);
    }
}

    private void handleHapus() {
    Kelompok selectedKelompok = tabelkelompok.getSelectionModel().getSelectedItem();
    if (selectedKelompok != null) {
        // Membuat dialog konfirmasi
        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationAlert.setTitle("Konfirmasi Penghapusan");
        confirmationAlert.setHeaderText("Yakin ingin menghapus data kelompok kerja?");
        confirmationAlert.setContentText("Nama: " + selectedKelompok.getNama() + "\nRFID: " + selectedKelompok.getRfid());
        
        // Menunggu pilihan pengguna
        if (confirmationAlert.showAndWait().get() == ButtonType.OK) {
            // Pengguna memilih OK
            database.getCollection("KelompokKerja").deleteOne(new Document("rfid", selectedKelompok.getRfid()));
            listKelompok.remove(selectedKelompok);
            tabelkelompok.setItems(listKelompok);
            showAlert("Data Kelompok berhasil dihapus!", Alert.AlertType.INFORMATION);
        } else {
            // Pengguna memilih CANCEL
            showAlert("Penghapusan dibatalkan!", Alert.AlertType.INFORMATION);
        }
    } else {
        showAlert("Pilih kelompok yang ingin dihapus!", Alert.AlertType.WARNING);
    }
}

    private void handleSimpan() {
    String nama = cmbnama.getValue();
    String shift = cmbshift.getValue();
    String hari = cmbhari.getValue();
    if (nama.isEmpty() || shift.isEmpty() || hari.isEmpty()) {
        // Tampilkan pesan kesalahan jika ada field yang kosong
        showAlert("Semua field harus diisi!", Alert.AlertType.ERROR);
        return;
    }

    MongoCollection<Document> karyawanCollection = database.getCollection("Karyawan");
    
Document karyawan = karyawanCollection.find(eq("nama", nama)).first();



    MongoCollection<Document> kelompokCollection = database.getCollection("KelompokKerja");

    if(karyawan != null) {
        String rfidTag = karyawan.getString("rfid");
    
        Document doc = new Document("rfid", rfidTag)
                .append("nama", nama)
                .append("shift", shift)
                .append("hari", hari);
    
        kelompokCollection.insertOne(doc);
        listKelompok.add(new Kelompok(nama, shift, hari, rfidTag));
        tabelkelompok.setItems(listKelompok);
        clearFields();
        showAlert("Data Kelompok berhasil disimpan!", Alert.AlertType.INFORMATION);
        hidePopup(Popkelompok);
    }
}

private void loadDataKaryawan() {
    listKelompok.clear();
    for (Document doc : database.getCollection("KelompokKerja").find()) {
        listKelompok.add(new Kelompok( doc.getString("nama"), doc.getString("shift"), doc.getString("hari"), doc.getString("rfid")));
    }

    tabelkelompok.setItems(listKelompok);
}


 private void showPopup(StackPane popup) {
        popup.setVisible(true);
        FadeTransition fadeIn = new FadeTransition(Duration.millis(500), popup);
        TranslateTransition slideDown = new TranslateTransition(Duration.millis(500), popup);
        fadeIn.setFromValue(0); fadeIn.setToValue(1);
        slideDown.setFromY(-80); slideDown.setToY(0);
        new ParallelTransition(fadeIn, slideDown).play();
        
    }

    private void hidePopup(StackPane popup) {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(500), popup);
        TranslateTransition slideUp = new TranslateTransition(Duration.millis(500), popup);
        fadeOut.setFromValue(1); fadeOut.setToValue(0);
        slideUp.setFromY(0); slideUp.setToY(-80);
        ParallelTransition hide = new ParallelTransition(fadeOut, slideUp);
        hide.setOnFinished(e -> {
            popup.setVisible(false);
            lbpopup.setText("Data Kelompok Kerja"); 
        });
    
        hide.play();
        
    }
    private void populateComboBox(ComboBox<String> comboBox, String collectionName, String fieldName) {
    try {
        MongoCollection<Document> collection = database.getCollection(collectionName);
        ObservableList<String> items = FXCollections.observableArrayList();

        for (Document doc : collection.find()) {
            items.add(doc.getString(fieldName));
        }

        comboBox.setItems(items);
    } catch (Exception e) {
        e.printStackTrace();
    }
}


private void showAlert(String message, Alert.AlertType type) {
    Alert alert = new Alert(type);
    alert.setTitle("Notification");
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
}

private void clearFields() {
    cmbnama.setValue(null);
cmbshift.setValue(null);
cmbhari.setValue(null);

}

 private void showButtons(Button btnEdit, Button btnHapus) {
    btnEdit.setVisible(true);
    btnHapus.setVisible(true);

    FadeTransition fadeInEdit = new FadeTransition(Duration.millis(300), btnEdit);
    fadeInEdit.setFromValue(0);
    fadeInEdit.setToValue(1);

    FadeTransition fadeInHapus = new FadeTransition(Duration.millis(300), btnHapus);
    fadeInHapus.setFromValue(0);
    fadeInHapus.setToValue(1);

    ScaleTransition scaleEdit = new ScaleTransition(Duration.millis(300), btnEdit);
    scaleEdit.setFromX(0);
    scaleEdit.setToX(1);
    scaleEdit.setFromY(0);
    scaleEdit.setToY(1);

    ScaleTransition scaleHapus = new ScaleTransition(Duration.millis(300), btnHapus);
    scaleHapus.setFromX(0);
    scaleHapus.setToX(1);
    scaleHapus.setFromY(0);
    scaleHapus.setToY(1);

    ParallelTransition transition = new ParallelTransition(fadeInEdit, fadeInHapus, scaleEdit, scaleHapus);
    transition.play();
}

private void hideButtons(Button btnEdit, Button btnHapus) {
    FadeTransition fadeOutEdit = new FadeTransition(Duration.millis(300), btnEdit);
    fadeOutEdit.setFromValue(1);
    fadeOutEdit.setToValue(0);

    FadeTransition fadeOutHapus = new FadeTransition(Duration.millis(300), btnHapus);
    fadeOutHapus.setFromValue(1);
    fadeOutHapus.setToValue(0);

    fadeOutEdit.setOnFinished(event -> btnEdit.setVisible(false));
    fadeOutHapus.setOnFinished(event -> btnHapus.setVisible(false));

    ParallelTransition transition = new ParallelTransition(fadeOutEdit, fadeOutHapus);
    transition.play();
}

    private void handlecari() {
        String keyword = txtcari.getText().trim();
        if (keyword.isEmpty()) {
            loadDataKaryawan(); // Load all data if search field is empty
        } else {
            listKelompok.clear();
            MongoCollection<Document> KelompokCollection = database.getCollection("KelompokKerja");
            for (Document doc : KelompokCollection.find(new Document("nama", new Document("$regex", keyword).append("$options", "i")))) {
                listKelompok.add(new Kelompok(doc.getString("nama"), doc.getString("shift"), doc.getString("hari"), doc.getString("rfid")));
            }
        }
        tabelkelompok.setItems(listKelompok);
        txtcari.clear(); 
    }


}