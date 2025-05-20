package com.example.demo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.bson.Document;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

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

public class JamKerjaControl {
    @FXML private StackPane popjam;
    @FXML private Label lbpopup;
    @FXML private ImageView imtambah, imcari;
    @FXML private Button btntambah, btnbatal, btnsimpan, btnedit, btnhapus, btncari;
    @FXML private TextField txtshift, txtcari;
    @FXML private ComboBox<String> cbmulai, cbselesai;
    @FXML private TableView<jamkerja> tabeljam;
    @FXML private TableColumn<jamkerja, String>  colNo, colShift, colMulai, colSelesai, colDurasi;
    private MongoDatabase database;
    private ObservableList<jamkerja> JamKerja = FXCollections.observableArrayList();

    public JamKerjaControl() {
        database = MongoDBConnection.getDatabase(); 
    }

    @FXML
    private void initialize() {
    ObservableList<String> jamKerjaList = FXCollections.observableArrayList(
        "00:00", "01:00", "02:00", "03:00", "04:00",
        "05:00", "06:00", "07:00", "08:00", "09:00", "10:00",
        "11:00", "12:00", "13:00", "14:00", "15:00", "16:00",
        "17:00", "18:00", "19:00", "20:00", "21:00", "22:00",
        "23:00"
    );
    cbmulai.setValue("Pilih Waktu Mulai");
    cbselesai.setValue("Pilih Waktu Selesai");
    cbmulai.setItems(jamKerjaList);
    cbselesai.setItems(jamKerjaList);
    btnedit.setVisible(false);
    btnhapus.setVisible(false);
    popjam.setVisible(false);
    tabeljam.setItems(JamKerja);
    btncari.setOnAction(event -> handleCari());
    btntambah.setOnAction(event -> showPopup(popjam));
    btnbatal.setOnAction(event -> hidePopup(popjam));
    btnsimpan.setOnAction(event -> { 
        if (lbpopup.getText().equals("Tambah Data Jam Kerja")) {
            handleSimpan();
            hidePopup(popjam);
            clearFields();
        } else {
            HandleUpdate();
            hidePopup(popjam);
            clearFields();
        }
    });
    imtambah.setOnMouseClicked(event -> showPopup(popjam));
    imcari.setOnMouseClicked(event -> handleCari());

    colNo.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : String.valueOf(getIndex() + 1));
            }
        });
        colMulai.setCellValueFactory(cellData -> cellData.getValue().mulaiProperty());
        colSelesai.setCellValueFactory(cellData -> cellData.getValue().selesaiProperty());
        colDurasi.setCellValueFactory(cellData -> cellData.getValue().durasiProperty());
        colShift.setCellValueFactory(cellData -> cellData.getValue().shiftProperty());

        loadDataJam();

        tabeljam.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                showButtons(btnedit, btnhapus);
                btnedit.setOnAction(event -> {
                    HandleEdit();
                });
                btnhapus.setOnAction(event -> HandleHapus());
            } else {
                hideButtons(btnedit, btnhapus);
            }
        });

        tabeljam.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) { 
                hideButtons(btnedit, btnhapus);
            }
            else {
                if (tabeljam.getSelectionModel().getSelectedItem() != null) {
                    showButtons(btnedit, btnhapus);
                }
            }
        });
    }


    private void handleCari() {
        String keyword = txtcari.getText().trim();
        if (keyword.isEmpty()) {
            loadDataJam(); // Load all data if search field is empty
        } else {
            JamKerja.clear();
            MongoCollection<Document> jamkerjaCollection = database.getCollection("JamKerja");
            for (Document doc : jamkerjaCollection.find(new Document("shift", new Document("$regex", keyword).append("$options", "i")))) {
                JamKerja.add(new jamkerja(doc.getString("shift"), doc.getString("waktuMulai"), doc.getString("waktuSelesai"),
                        doc.getString("durasi")));
            }
        }
        tabeljam.setItems(JamKerja);
        txtcari.clear(); 
    }

    private void HandleHapus() {
        jamkerja selectedJamkerja = tabeljam.getSelectionModel().getSelectedItem();
    if (selectedJamkerja != null) {
        // Membuat dialog konfirmasi
        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationAlert.setTitle("Konfirmasi Penghapusan");
        confirmationAlert.setHeaderText("Yakin ingin menghapus data jam kerja?");
        confirmationAlert.setContentText("Shift: " + selectedJamkerja.getShift());
        
        // Menunggu pilihan pengguna
        if (confirmationAlert.showAndWait().get() == ButtonType.OK) {
            // Pengguna memilih OK
            database.getCollection("JamKerja").deleteOne(new Document("shift", selectedJamkerja.getShift()));
             JamKerja.remove(selectedJamkerja);
            tabeljam.setItems(JamKerja);
            showAlert("Data Jam Kerja berhasil dihapus!", Alert.AlertType.INFORMATION);
        } else {
            // Pengguna memilih CANCEL
            showAlert("Penghapusan dibatalkan!", Alert.AlertType.INFORMATION);
        }
    } else {
        showAlert("Pilih Jam Kerja yang ingin dihapus!", Alert.AlertType.WARNING);
    }
    }

    private void HandleUpdate() {
        jamkerja selectedJamkerja = tabeljam.getSelectionModel().getSelectedItem();
    if (selectedJamkerja != null) {
        String shift = txtshift.getText().trim();
        String mulai = cbmulai.getValue();
        String selesai = cbselesai.getValue();

        if (shift.isEmpty() || mulai.isEmpty() ||selesai.isEmpty()) {
            showAlert("Semua field harus diisi!", Alert.AlertType.ERROR);
            return;
        }

        try {
            LocalTime waktuMulai = LocalTime.parse(mulai);
            LocalTime waktuSelesai = LocalTime.parse(selesai);

            LocalDateTime mulaiDateTime = LocalDateTime.of(LocalDate.now(), waktuMulai);
            LocalDateTime selesaiDateTime = LocalDateTime.of(LocalDate.now(), waktuSelesai);

            if (selesaiDateTime.isBefore(mulaiDateTime)) {
                selesaiDateTime = selesaiDateTime.plusDays(1);
            }
            java.time.Duration durasi = java.time.Duration.between(mulaiDateTime, selesaiDateTime);
    
            String hasilDurasi = durasi.toHours() + " jam ";
            MongoCollection<Document> jamkerjaCollection = database.getCollection("JamKerja");
            Document updatedData = new Document("$set", new Document("shift", shift)
                    .append("waktuMulai", mulai)
                    .append("waktuSelesai", selesai)
                    .append("durasi", hasilDurasi));
        
    
            jamkerjaCollection.updateOne(new Document("shift", shift), updatedData);
    
        
            selectedJamkerja.setShift(shift);
            selectedJamkerja.setMulai(mulai);
            selectedJamkerja.setSelesai(selesai);
            selectedJamkerja.setDurasi(hasilDurasi);
        
    
            tabeljam.refresh(); 
    
            hidePopup(popjam);
            showAlert("Data Jam Kerja berhasil diperbarui!", Alert.AlertType.INFORMATION);
        clearFields();
        
    } catch (Exception e) {

        showAlert("Format waktu tidak valid!", Alert.AlertType.ERROR);
    }
    
    } else {
        showAlert("Pilih karyawan yang ingin diperbarui!", Alert.AlertType.WARNING);
    }

}

    

    private void HandleEdit() {
            jamkerja selectedJamkerja = tabeljam.getSelectionModel().getSelectedItem();
            if (selectedJamkerja != null) {
                lbpopup.setText("Edit  Data Jam Kerja");
                txtshift.setText(selectedJamkerja.getShift());
                cbmulai.setValue(selectedJamkerja.getMulai());
                cbselesai.setValue(selectedJamkerja.getSelesai());
                showPopup(popjam);
            
            
            } else {
                showAlert("Pilih jam kerja yang ingin diedit!", Alert.AlertType.WARNING);
            }
    }

    private void clearFields() {
        cbmulai.setValue("Pilih Waktu ");
        cbselesai.setValue("Pilih Waktu ");
        txtshift.clear();
        
    }

  private void handleSimpan() {
    String shift = txtshift.getText().trim();
    String mulai = cbmulai.getValue();
    String selesai = cbselesai.getValue();

    if (mulai == null || selesai == null || shift.isEmpty()) {
        System.out.println("Semua field harus diisi!");
        return;
    }

    try {
    LocalTime waktuMulai = LocalTime.parse(mulai);
    LocalTime waktuSelesai = LocalTime.parse(selesai);
    LocalDateTime mulaiDateTime = LocalDateTime.of(LocalDate.now(), waktuMulai);
    LocalDateTime selesaiDateTime = LocalDateTime.of(LocalDate.now(), waktuSelesai);

    
    if (selesaiDateTime.isBefore(mulaiDateTime)) {
        selesaiDateTime = selesaiDateTime.plusDays(1);
    }
    java.time.Duration durasi = java.time.Duration.between(mulaiDateTime, selesaiDateTime);
    
    
    String hasilDurasi = durasi.toHours() + " jam ";

    
    MongoCollection<Document> jamKerja = database.getCollection("JamKerja");
    Document doc = new Document("shift", shift)
            .append("waktuMulai", mulai)
            .append("waktuSelesai", selesai)
            .append("durasi", hasilDurasi);

    jamKerja.insertOne(doc);

    
    JamKerja.add(new jamkerja(shift, mulai, selesai, hasilDurasi));
    tabeljam.setItems(JamKerja);

    clearFields();
    showAlert("Data Karyawan berhasil disimpan!", Alert.AlertType.INFORMATION);
    }
    catch (Exception e) {
        showAlert("Format waktu tidak valid!", Alert.AlertType.ERROR);
    }
}


    private void hidePopup(StackPane popup) {
        FadeTransition fadeOut = new FadeTransition(javafx.util.Duration.millis(500), popup);

        TranslateTransition slideUp = new TranslateTransition(javafx.util.Duration.millis(500), popup);

        fadeOut.setFromValue(1); fadeOut.setToValue(0);
        slideUp.setFromY(0); slideUp.setToY(-80);
        ParallelTransition hide = new ParallelTransition(fadeOut, slideUp);
        hide.setOnFinished(e -> {
            popup.setVisible(false);
            lbpopup.setText("Tambah Data Jam Kerja"); 
        });
    
        hide.play();

        loadDataJam();
        cbmulai.setValue("Pilih Waktu");
        cbselesai.setValue("Pilih Waktu");
        clearFields();
        
    }

    private void loadDataJam() {
        JamKerja.clear();
        for (Document doc : database.getCollection("JamKerja").find()) {
            JamKerja.add(new jamkerja(doc.getString("shift"), doc.getString("waktuMulai"), doc.getString("waktuSelesai"), doc.getString("durasi")));
        
        }
    }

    private void showPopup(StackPane popup) {
        popup.setVisible(true);
        FadeTransition fadeIn = new FadeTransition(javafx.util.Duration.millis(500), popup);
        TranslateTransition slideDown = new TranslateTransition(javafx.util.Duration.millis(500), popup);

        fadeIn.setFromValue(0); fadeIn.setToValue(1);
        slideDown.setFromY(-80); slideDown.setToY(0);
        new ParallelTransition(fadeIn, slideDown).play();
    }

    private void showAlert(String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle("Notification");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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
}
