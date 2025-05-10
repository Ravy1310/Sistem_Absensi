package com.example.demo;


import java.time.LocalDate;

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
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

public class DataKaryawanControl {
    @FXML private StackPane popkaryawan;
    @FXML private Label lbpopup;
    @FXML private ImageView imtambah, imcari;
    @FXML private Button btntambah, btnbatal, btnsimpan, btnedit, btnhapus, btncari;
    @FXML private TextField txtrfid, txtnama, txtalamat, txthp, txtemail, txtposisi, txtcari;
    @FXML private DatePicker txttanggal;
    @FXML private TableView<Karyawan> tableKaryawan;
    @FXML private TableColumn<Karyawan, String> colNo, colNama, colEmail, colPosisi, colAlamat, colHP, colTanggalMasuk;
    
    private MongoDatabase database;
    private ObservableList<Karyawan> listKaryawan = FXCollections.observableArrayList();

    public DataKaryawanControl() { 
        database = MongoDBConnection.getDatabase(); 
    }

    @FXML
    public void initialize() {
        btnedit.setVisible(false);
        btnhapus.setVisible(false);
        popkaryawan.setVisible(false);
        tableKaryawan.setItems(listKaryawan);

        colNo.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : String.valueOf(getIndex() + 1));
            }
        });

        colNama.setCellValueFactory(cellData -> cellData.getValue().namaProperty());
        colEmail.setCellValueFactory(cellData -> cellData.getValue().emailProperty());
        colPosisi.setCellValueFactory(cellData -> cellData.getValue().posisiProperty());
        colAlamat.setCellValueFactory(cellData -> cellData.getValue().alamatProperty());
        colHP.setCellValueFactory(cellData -> cellData.getValue().noHpProperty());
        colTanggalMasuk.setCellValueFactory(cellData -> cellData.getValue().tanggalMasukProperty().asString());
        btncari.setOnAction(event -> HandleCari());
        imcari.setOnMouseClicked(event -> HandleCari());
        imtambah.setOnMouseClicked(event -> showPopup(popkaryawan));
        btntambah.setOnAction(event -> showPopup(popkaryawan));
        btnbatal.setOnAction(event -> {
            hidePopup(popkaryawan);
            clearFields();
        });
        btnsimpan.setOnAction(event -> {
            if (lbpopup.getText().equals("Tambah Data Karyawan")) {
                HandleSimpan();
                hidePopup(popkaryawan);
                clearFields();
            } else {
                HandleUpdate();
                hidePopup(popkaryawan);
                clearFields();
            }
        });
        tableKaryawan.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
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

        tableKaryawan.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) { 
                hideButtons(btnedit, btnhapus);
            }
            else {
                if (tableKaryawan.getSelectionModel().getSelectedItem() != null) {
                    showButtons(btnedit, btnhapus);
                }
            }
        });

        loadDataKaryawan();
    }

    private void HandleCari() {
        String keyword = txtcari.getText().trim();
        if (keyword.isEmpty()) {
            loadDataKaryawan(); // Load all data if search field is empty
        } else {
            listKaryawan.clear();
            MongoCollection<Document> karyawanCollection = database.getCollection("Karyawan");
            for (Document doc : karyawanCollection.find(new Document("nama", new Document("$regex", keyword).append("$options", "i")))) {
                listKaryawan.add(new Karyawan(doc.getString("nama"), doc.getString("alamat"), doc.getString("hp"),
                        doc.getString("email"), LocalDate.parse(doc.getString("tanggalMasuk")), doc.getString("posisi"), doc.getString("rfid")));
            }
        }
        tableKaryawan.setItems(listKaryawan);
        txtcari.clear(); 
    }

   private void HandleHapus() {
    Karyawan selectedKaryawan = tableKaryawan.getSelectionModel().getSelectedItem();
    if (selectedKaryawan != null) {
        // Membuat dialog konfirmasi
        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationAlert.setTitle("Konfirmasi Penghapusan");
        confirmationAlert.setHeaderText("Yakin ingin menghapus data karyawan?");
        confirmationAlert.setContentText("Nama: " + selectedKaryawan.getNama() + "\nRFID: " + selectedKaryawan.getRfid());

        // Menunggu pilihan pengguna
        if (confirmationAlert.showAndWait().get() == ButtonType.OK) {
            String rfid = selectedKaryawan.getRfid();

            // Hapus dari koleksi "Karyawan"
            database.getCollection("Karyawan").deleteOne(new Document("rfid", rfid));

            // Hapus dari koleksi "Absensi"
            database.getCollection("Absensi").deleteMany(new Document("rfid", rfid));

            // Hapus dari koleksi "KelompokKerja"
            database.getCollection("KelompokKerja").deleteMany(new Document("rfid", rfid));

            // Hapus dari koleksi "PengajuanLembur"
            database.getCollection("PengajuanLembur").deleteMany(new Document("rfid", rfid));

            // Hapus dari koleksi "rekapAbsensi"
            database.getCollection("rekapAbsensi").deleteMany(new Document("rfid", rfid));

            // Hapus dari list dan refresh table
            listKaryawan.remove(selectedKaryawan);
            tableKaryawan.setItems(listKaryawan);

            showAlert("Data Karyawan dan semua data terkait berhasil dihapus!", Alert.AlertType.INFORMATION);
        } else {
            showAlert("Penghapusan dibatalkan!", Alert.AlertType.INFORMATION);
        }
    } else {
        showAlert("Pilih karyawan yang ingin dihapus!", Alert.AlertType.WARNING);
    }
}


    private void HandleEdit() {
        Karyawan selectedKaryawan = tableKaryawan.getSelectionModel().getSelectedItem();
        if (selectedKaryawan != null) {
            lbpopup.setText("Edit  Data Karyawan");
            txtrfid.setText(selectedKaryawan.getRfid());
            txtnama.setText(selectedKaryawan.getNama());
            txtalamat.setText(selectedKaryawan.getAlamat());
            txthp.setText(selectedKaryawan.getNoHp());
            txtemail.setText(selectedKaryawan.getEmail());
            txttanggal.setValue(selectedKaryawan.getTanggalMasuk());
            txtposisi.setText(selectedKaryawan.getPosisi());

            showPopup(popkaryawan);
        
        } else {
            showAlert("Pilih karyawan yang ingin diedit!", Alert.AlertType.WARNING);
        }
       
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
            lbpopup.setText("Tambah Data Karyawan"); 
        });
    
        hide.play();
        
    }

    private void HandleSimpan() {
        String Nama = txtnama.getText().trim();
        String Alamat = txtalamat.getText().trim();
        String Hp = txthp.getText().trim();
        String Email = txtemail.getText().trim();
        LocalDate Tanggal = txttanggal.getValue();
        String Posisi = txtposisi.getText().trim();
        String rfid = txtrfid.getText().trim();
    
        if (rfid.isEmpty() || Nama.isEmpty() || Alamat.isEmpty() || Hp.isEmpty() || Email.isEmpty() || Tanggal == null || Posisi.isEmpty()) {
            showAlert("Semua field harus diisi!", Alert.AlertType.ERROR);
            return;
        }
    
    
        MongoCollection<Document> karyawanCollection = database.getCollection("Karyawan");
        Document existingKaryawan = karyawanCollection.find(new Document("rfid", rfid)).first();
    
        if (existingKaryawan != null) {
            showAlert("RFID sudah digunakan! Mohon gunakan RFID yang berbeda.", Alert.AlertType.WARNING);
            return;
        }
    
        Document doc = new Document("rfid", rfid)
                .append("nama", Nama)
                .append("alamat", Alamat)
                .append("hp", Hp)
                .append("email", Email)
                .append("tanggalMasuk", Tanggal.toString())
                .append("posisi", Posisi);
    
        karyawanCollection.insertOne(doc);
        listKaryawan.add(new Karyawan(Nama, Alamat, Hp, Email, Tanggal, Posisi, rfid));
        tableKaryawan.setItems(listKaryawan);
        clearFields();
        showAlert("Data Karyawan berhasil disimpan!", Alert.AlertType.INFORMATION);
    }

    private void loadDataKaryawan() {
        listKaryawan.clear();
        for (Document doc : database.getCollection("Karyawan").find()) {
            listKaryawan.add(new Karyawan(doc.getString("nama"), doc.getString("alamat"), doc.getString("hp"),
                    doc.getString("email"), LocalDate.parse(doc.getString("tanggalMasuk")), doc.getString("posisi"), doc.getString("rfid")));
        }
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

    private void clearFields() {
        txtnama.clear();
        txtalamat.clear();
        txthp.clear();
        txtemail.clear();
        txttanggal.setValue(null);
        txtposisi.clear();
        txtrfid.clear();
    }

    private void HandleUpdate() {
    Karyawan selectedKaryawan = tableKaryawan.getSelectionModel().getSelectedItem();
    if (selectedKaryawan != null) {
        // Ambil data terbaru dari form
        String rfid = txtrfid.getText().trim();
        String nama = txtnama.getText().trim();
        String alamat = txtalamat.getText().trim();
        String hp = txthp.getText().trim();
        String email = txtemail.getText().trim();
        LocalDate tanggalMasuk = txttanggal.getValue();
        String posisi = txtposisi.getText().trim();

        if (nama.isEmpty() || alamat.isEmpty() || hp.isEmpty() || email.isEmpty() || tanggalMasuk == null || posisi.isEmpty()) {
            showAlert("Semua field harus diisi!", Alert.AlertType.ERROR);
            return;
        }
        MongoCollection<Document> karyawanCollection = database.getCollection("Karyawan");
        Document updatedData = new Document("$set", new Document("nama", nama)
                .append("alamat", alamat)
                .append("hp", hp)
                .append("email", email)
                .append("tanggalMasuk", tanggalMasuk.toString())
                .append("posisi", posisi));

        karyawanCollection.updateOne(new Document("rfid", rfid), updatedData);

        selectedKaryawan.setNama(nama);
        selectedKaryawan.setAlamat(alamat);
        selectedKaryawan.setNoHp(hp);
        selectedKaryawan.setEmail(email);
        selectedKaryawan.setTanggalMasuk(tanggalMasuk);
        selectedKaryawan.setPosisi(posisi);

        tableKaryawan.refresh(); 

        hidePopup(popkaryawan);
        showAlert("Data karyawan berhasil diperbarui!", Alert.AlertType.INFORMATION);
    } else {
        showAlert("Pilih karyawan yang ingin diperbarui!", Alert.AlertType.WARNING);
    }
}
}