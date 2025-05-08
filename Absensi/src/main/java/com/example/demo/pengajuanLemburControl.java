package com.example.demo;

import java.time.LocalDate;

import org.bson.Document;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import static com.mongodb.client.model.Filters.eq;

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
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

public class pengajuanLemburControl {
    @FXML private Label lbPopUp;
    @FXML private StackPane popPengajuan;
    @FXML private TextField txtCari;
    @FXML private ComboBox<String> cbNama, cbDurasi;
    @FXML private DatePicker dpTanggal;
    @FXML private ImageView imTambah, imCari;
    @FXML private Button btnTambah, btnBatal, btnSimpan, btnhapus, btnedit, btnCari;
    @FXML private TableView<PengajuanLembur> tablePengajuanLembur;
    @FXML private TableColumn<PengajuanLembur, String> colNo, colNama, colDurasi, colTanggal;
    
    private ObservableList<PengajuanLembur> pengajuanLemburList = FXCollections.observableArrayList();


    private MongoDatabase database;
    public pengajuanLemburControl() {
        database = MongoDBConnection.getDatabase();
    }

    public void initialize() {
        ObservableList<String> durasiOptions = FXCollections.observableArrayList("1", "2", "3", "4");
        cbDurasi.setItems(durasiOptions);
    populateComboBox(cbNama, "Karyawan", "nama");

        popPengajuan.setVisible(false);
        btnhapus.setVisible(false);
        btnedit.setVisible(false);
        btnCari.setOnAction(event -> {
            handleCari();
        });
        imCari.setOnMouseClicked(event ->  {
            handleCari();
        });
        btnTambah.setOnAction(envent -> {
            showPopUp(popPengajuan);
        });
        imTambah.setOnMouseClicked(event -> {
            showPopUp(popPengajuan);
        });
        btnBatal.setOnAction(event -> {
            hidePopup(popPengajuan);
        });
        btnSimpan.setOnAction(event -> {
            if(lbPopUp.getText().equals("Data Pengajuan Lembur")) {
                handleSimpan();
                hidePopup(popPengajuan);
                clearFields();
            } else {
                handleUpdate();
                hidePopup(popPengajuan);
                clearFields();
            }

        
        });

        tablePengajuanLembur.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
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
    
        tablePengajuanLembur.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) { 
                hideButtons(btnedit, btnhapus);
            }
            else {
                if (tablePengajuanLembur.getSelectionModel().getSelectedItem() != null) {
                    showButtons(btnedit, btnhapus);
                }
            }
        });
        colNo.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : String.valueOf(getIndex() + 1));
            }
        });
        colNama.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getNama()));
        colDurasi.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getDurasi()));
        colTanggal.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getTanggal()));

        loadDataPengajuan();
    }
    
    private void handleCari() {
        String keyword = txtCari.getText().trim();
        if (keyword.isEmpty()) {
            loadDataPengajuan(); // Load all data if search field is empty
        } else {
            pengajuanLemburList.clear();
            MongoCollection<Document> KelompokCollection = database.getCollection("PengajuanLembur");
            for (Document doc : KelompokCollection.find(new Document("nama", new Document("$regex", keyword).append("$options", "i")))) {
                pengajuanLemburList.add(new PengajuanLembur(doc.getString("nama"), doc.getString("tanggal"), doc.getString("durasi"), doc.getString("rfid")));
            }
        }
        tablePengajuanLembur.setItems(pengajuanLemburList);
        txtCari.clear(); 
    } 

    private void handleUpdate() {
        PengajuanLembur selectedPengajuan = tablePengajuanLembur.getSelectionModel().getSelectedItem();
        if (selectedPengajuan != null) {
            // Ambil data terbaru dari form
            String nama = cbNama.getValue();
            String tanggal = dpTanggal.getValue().toString();
            String durasi = cbDurasi.getValue();
    
            if (nama.isEmpty() || tanggal.isEmpty() || durasi.isEmpty()) {
                showAlert("Semua field harus diisi!", Alert.AlertType.ERROR);
                return;
            }
    
            MongoCollection<Document> PengajuanCollection = database.getCollection("PengajuanLembur");
            Document updatedData = new Document("$set", new Document("nama", nama)
                    .append("tanggal", tanggal)
                    .append("durasi", durasi));
            PengajuanCollection.updateOne(new Document("nama", nama), updatedData);
    
            selectedPengajuan.setNama(nama);
            selectedPengajuan.setTanggal(tanggal);
            selectedPengajuan.setDurasi(durasi);
           
            tablePengajuanLembur.refresh(); 
    
            hidePopup(popPengajuan);
            showAlert("Data Pengajuan berhasil diperbarui!", Alert.AlertType.INFORMATION);
        } else {
            showAlert("Pilih data yang ingin diperbarui!", Alert.AlertType.WARNING);
        }
    }

    private void loadDataPengajuan() {
    pengajuanLemburList.clear();
    MongoCollection<Document> collection = database.getCollection("PengajuanLembur");
    for (Document doc : collection.find()) {
        String nama = doc.getString("nama");
        String durasi = doc.getString("durasi");
        String tanggal = doc.getString("tanggal");
        String rfid = doc.getString("rfid"); // RFID tetap disimpan tapi tidak ditampilkan
        pengajuanLemburList.add(new PengajuanLembur(nama, tanggal, durasi, rfid));
    }
    tablePengajuanLembur.setItems(pengajuanLemburList);
}

    private void handleSimpan() {
        String nama = (String) cbNama.getValue();
        String durasi = (String) cbDurasi.getValue();
        LocalDate tanggal = dpTanggal.getValue();

        if (nama == null || durasi == null || tanggal == null) {
            // Show an error message or handle the case where fields are empty
            showAlert("Semua Field harus diisi!", Alert.AlertType.ERROR);
            return;
        }
        
    MongoCollection<Document> karyawanCollection = database.getCollection("Karyawan");
    
Document karyawan = karyawanCollection.find(eq("nama", nama)).first();


        MongoCollection<Document> collection = database.getCollection("PengajuanLembur");
        if(karyawan != null) {
            String rfidTag = karyawan.getString("rfid");
            Document newDocument = new Document("rfid", rfidTag)
                    .append("nama", nama)
                    .append("durasi", durasi)
                    .append("tanggal", tanggal.toString());
            collection.insertOne(newDocument);
            pengajuanLemburList.add(new PengajuanLembur(nama, tanggal.toString(), durasi, rfidTag));
            tablePengajuanLembur.setItems(pengajuanLemburList);
            clearFields();
            hidePopup(popPengajuan);
            showAlert("Data Pengajuan Lembur Berhasil Disimpan!", Alert.AlertType.INFORMATION);
        } else {
            showAlert("Karyawan tidak ditemukan!", Alert.AlertType.ERROR);
        }

    }

    private void HandleEdit() {
        PengajuanLembur selectedPengajuan = tablePengajuanLembur.getSelectionModel().getSelectedItem();
        if (selectedPengajuan != null) {
            lbPopUp.setText("Edit  Data Pengajuan Lembur");
            cbNama.setValue(selectedPengajuan.getNama());
            dpTanggal.setValue(LocalDate.parse(selectedPengajuan.getTanggal()));
            cbDurasi.setValue(selectedPengajuan.getDurasi());
            showPopUp(popPengajuan);
        
        } else {
            showAlert("Pilih data yang ingin diedit!", Alert.AlertType.WARNING);
        }
        

    }

    private void handleHapus() {
    PengajuanLembur selectedPengajuan = tablePengajuanLembur.getSelectionModel().getSelectedItem();
    if (selectedPengajuan != null) {
        // Membuat dialog konfirmasi
        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationAlert.setTitle("Konfirmasi Penghapusan");
        confirmationAlert.setHeaderText("Yakin ingin menghapus data kelompok kerja?");
        confirmationAlert.setContentText("Nama: " + selectedPengajuan.getNama() + "\nRFID: " + selectedPengajuan.getRfid());
        
        // Menunggu pilihan pengguna
        if (confirmationAlert.showAndWait().get() == ButtonType.OK) {
            // Pengguna memilih OK
            database.getCollection("PengajuanLembur").deleteOne(new Document("rfid", selectedPengajuan.getRfid()));
            pengajuanLemburList.remove(selectedPengajuan);
            tablePengajuanLembur.setItems(pengajuanLemburList);
            showAlert("Data Pengajuan berhasil dihapus!", Alert.AlertType.INFORMATION);
        } else {
            // Pengguna memilih CANCEL
            showAlert("Penghapusan dibatalkan!", Alert.AlertType.INFORMATION);
        }
    } else {
        showAlert("Pilih data yang ingin dihapus!", Alert.AlertType.WARNING);
    }
}

    private void showPopUp(StackPane popup) {
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
            // lbpopup.setText("Tambah Data Karyawan"); 
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
    cbNama.setValue(null);
    dpTanggal.setValue(null);
    cbDurasi.setValue(null);

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

