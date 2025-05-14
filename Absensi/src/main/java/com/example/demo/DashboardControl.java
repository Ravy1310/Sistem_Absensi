package com.example.demo;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import static com.mongodb.client.model.Filters.eq;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
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
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

public class DashboardControl {
    @FXML private StackPane popAbsen;
    @FXML private Pane ContentArea;
    @FXML private ComboBox<String> cbNama, cbHari, cbStatus, cbShift, cbJenis;
    @FXML private DatePicker dpTanggal;
    @FXML private TextField txtMasuk, txtKeluar;
    @FXML private Button btnKaryawan, btnShift, btnKelompok, btnTambah, btnBatal, btnSimpan, btnEdit, btnHapus;
    @FXML private Label lbKaryawan, lbShift, lbKelompok, lbMasuk, lbKeluar, lbPopUp , lbShiftpop, lbStatus;
    @FXML private Pane pnMasuk, pnShift, pnStatus;
    @FXML private TableView<RekapAbsensi> tabelAbsensi;
    @FXML private TableColumn<RekapAbsensi, String> colNo, colNama, colShift, colTanggal, colWaktuMasuk, colWaktuKeluar,colJenis, colStatus, colDurasi;
    

    private MongoDatabase database;
    public DashboardControl() {
        database = MongoDBConnection.getDatabase();
    }

    @FXML
    private void initialize() {
        cbHari.getItems().addAll("Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu", "Minggu");
        cbStatus.getItems().addAll("Masuk", "Izin", "Sakit", "Alfa");
        cbJenis.getItems().addAll("tepat waktu", "terlambat", "Keluar");

    populateComboBox(cbNama, "Karyawan", "nama");

        lbStatus.setVisible(false);
        cbJenis.setVisible(false);
        pnStatus.setVisible(false);
        lbMasuk.setVisible(false);
        lbKeluar.setVisible(false);
        pnMasuk.setVisible(false);
        txtMasuk.setVisible(false);
        txtKeluar.setVisible(false);
        popAbsen.setVisible(false);
        lbShiftpop.setVisible(false);
        cbShift.setVisible(false);
        pnShift.setVisible(false);
        btnEdit.setVisible(false);
        btnHapus.setVisible(false);
        // Initialize the dashboard with data from the database
        lbKaryawan.setText( getTotalKaryawan());
        lbShift.setText(getTotalShift());
        lbKelompok.setText(getTotalKelompok());

        btnKaryawan.setOnAction(event -> {
            loadPage("/com/example/demo/Data Karyawan/datakaryawan.fxml");

        });
        btnShift.setOnAction(event -> {
            loadPage("/com/example/demo/Data Shift/datajamkerja.fxml");
        });
        btnKelompok.setOnAction(event -> {
            loadPage("/com/example/demo/Data Kelompok/datakelompokkerja.fxml");
        });

        btnTambah.setOnAction(event -> {
            showPopup(popAbsen);
        });
        btnBatal.setOnAction(event -> {
            hidePopup(popAbsen);
        });
        btnSimpan.setOnAction(event -> {
            if(lbPopUp.getText().equals("Tambah Absen")) {
                handleSimpan();
                hidePopup(popAbsen);
                clearFields();
            }else {
                HandleUpdate();
                hidePopup(popAbsen);
                clearFields();
            }
        });
        colNo.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : String.valueOf(getIndex() + 1));
            }
        });
        colNama.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getNama()));
        colShift.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getShift()));
        colTanggal.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getTanggal()));
        colWaktuMasuk.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getJamMasuk()));
        colWaktuKeluar.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getJamKeluar()));
        colStatus.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getStatus()));
        colJenis.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getJenisAbsen()));
        colDurasi.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getDurasiKerja()));
        
   cbStatus.setOnAction(event -> {
    String selectedStatus = cbStatus.getSelectionModel().getSelectedItem();

    if ("Masuk".equals(selectedStatus)) {
       txtMasuk.setVisible(true);
         lbMasuk.setVisible(true);
        lbShiftpop.setVisible(true);
        cbShift.setVisible(true);
        pnShift.setVisible(true);
        lbStatus.setVisible(true);
        cbJenis.setVisible(true);
        pnStatus.setVisible(true);
        // lbKeluar.setVisible(true);
        // txtKeluar.setVisible(true);
        // pnMasuk.setVisible(true);
    } else {
        // lbKeluar.setVisible(false);
        // txtKeluar.setVisible(false);
        // pnMasuk.setVisible(false);
        lbStatus.setVisible(false);
        cbJenis.setVisible(false);
        pnStatus.setVisible(false);
        txtMasuk.setVisible(false);
         lbMasuk.setVisible(false);
        lbShiftpop.setVisible(false);
        cbShift.setVisible(false);
        pnShift.setVisible(false);
    }
});

// cbNama.setOnAction(event -> {
//     String selectNama = cbNama.getSelectionModel().getSelectedItem();
//     String hari = cbHari.getSelectionModel().getSelectedItem();

//     // Periksa apakah pengguna sudah memilih nama dan hari
//     if (selectNama == null || hari == null) {
//         cbShift.setValue(null);
//         return;
//     }

//     MongoCollection<Document> KelompokCollection = database.getCollection("KelompokKerja");
//     Document kelompokDoc = KelompokCollection.find(new Document("nama", selectNama).append("hari", hari)).first();

//     // Jalankan pembaruan UI di thread JavaFX jika diperlukan
//     Platform.runLater(() -> {
//         if (kelompokDoc != null) {
//             String shift = kelompokDoc.getString("shift");
//                 populateComboBox(cbShift, "Karyawan", "nama");

//         } else {
//             cbShift.setValue(null);
//             System.out.println("Shift tidak ditemukan untuk " + selectNama + " pada hari " + hari);
//         }
//     });
// });
cbHari.setOnAction(event -> {
    String selectNama = cbNama.getSelectionModel().getSelectedItem();
    String hari = cbHari.getSelectionModel().getSelectedItem();

    // Periksa apakah pengguna sudah memilih nama dan hari
    if (selectNama == null || hari == null) {
        cbShift.getItems().clear(); // Kosongkan ComboBox jika tidak ada pilihan
        return;
    }

    MongoCollection<Document> KelompokCollection = database.getCollection("KelompokKerja");
    FindIterable<Document> kelompokDocs = KelompokCollection.find(new Document("nama", selectNama).append("hari", hari));

    List<String> shiftList = new ArrayList<>();
    
    // Ambil semua shift dari dokumen yang cocok
    for (Document doc : kelompokDocs) {
        String shift = doc.getString("shift");
        if (shift != null) {
            shiftList.add(shift);
        }
    }

    // Jalankan pembaruan UI di thread JavaFX jika diperlukan
    Platform.runLater(() -> {
        cbShift.getItems().setAll(shiftList); // Set semua shift ke ComboBox
        if (!shiftList.isEmpty()) {
            cbShift.setValue(shiftList.get(0)); // Atur nilai default ke shift pertama
        } else {
            System.out.println("Shift tidak ditemukan untuk " + selectNama + " pada hari " + hari);
        }
    });
});




        tabelAbsensi.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                showButtons(btnEdit, btnHapus);
                btnEdit.setOnAction(event -> {
                    HandleEdit();
                });
                btnHapus.setOnAction(event -> HandleHapus());
            } else {
                hideButtons(btnEdit, btnHapus);
            }
        });

        tabelAbsensi.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) { 
                hideButtons(btnEdit, btnHapus);
            }
            else {
                if (tabelAbsensi.getSelectionModel().getSelectedItem() != null) {
                    showButtons(btnEdit, btnHapus);
                }
            }
        });
        loadAbsen();
    }

    private String getTotalKaryawan() {
        long count = database.getCollection("Karyawan").countDocuments();
        return String.valueOf(count);
    }
    
    private String getTotalShift() {
        long count = database.getCollection("JamKerja").countDocuments();
        return String.valueOf(count);
    }
    
    private String getTotalKelompok() {
        long count = database.getCollection("KelompokKerja").countDocuments();
        return String.valueOf(count);
    }
    
    private void loadPage(String fxmlPath) {
    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent page = loader.load();

        // Set transparan dulu halaman baru
        page.setOpacity(0);

        // Tambahkan halaman baru ke StackPane
        ContentArea.getChildren().setAll(page);

        // Buat efek fade in
        FadeTransition fadeIn = new FadeTransition(Duration.millis(600), page);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();

    } catch (IOException e) {
        e.printStackTrace();
    }
}

private void handleSimpan() {
    ObservableList<RekapAbsensi> listAbsensi = FXCollections.observableArrayList();


    String nama = cbNama.getValue();
    String hari = cbHari.getValue();
    String status = cbStatus.getValue();
    LocalDate tanggal = dpTanggal.getValue();
    String shift = cbShift.getValue();
    String jenis = cbJenis.getValue();


    if(nama == null || hari == null || status == null || tanggal == null) {
        showAlert("Field tidak boleh kosong", Alert.AlertType.WARNING);
        return;
    } else {

    MongoCollection<Document> karyawanCollection = database.getCollection("Karyawan");

    Document karyawan = karyawanCollection.find(eq("nama", nama)).first();
    if (karyawan != null) {
        String rfid = karyawan.getString("rfid");
        MongoCollection<Document> collection = database.getCollection("Absensi");
        Document doc = new Document("rfid", rfid)
                .append("nama", nama)
                .append("hari", hari)
                .append("shift", shift)
                .append("tanggal", tanggal.toString())
                .append("waktu", txtMasuk.getText())
               .append("status", jenis)
                .append("jenisAbsen", status)
                .append("waktuKeluar", txtKeluar.getText());
              
        collection.insertOne(doc);
        listAbsensi.add(new RekapAbsensi(nama, shift, tanggal.toString(),null , null, txtMasuk.getText(), txtKeluar.getText(), jenis, hari, status));
        hidePopup(popAbsen);
        showAlert("Data Absensi berhasil disimpan!", Alert.AlertType.INFORMATION);
        clearFields();
    }
}
}



private void loadAbsen() {
    ObservableList<RekapAbsensi> listAbsensi = FXCollections.observableArrayList();
    
    // Pastikan variabel database sudah terdefinisi sebagai MongoDatabase
    MongoCollection<Document> rekapAbsensiCollection = database.getCollection("Absensi");

    LocalDate today = LocalDate.now();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    String todayString = today.format(formatter);

    // Query untuk cari dokumen dengan tanggal hari ini
    for (Document doc : rekapAbsensiCollection.find(new Document("tanggal", todayString))) {
        String rfid = doc.getString("rfid");
        String nama = doc.getString("nama"); // Ambil nama langsung dari dokumen Absensi
        String shift = doc.getString("shift");
        String jamMasuk = doc.getString("waktu");
        String jamKeluar = doc.getString("waktuKeluar");
        String jenis = doc.getString("jenisAbsen");
        String status = doc.getString("status");

        // Jika jenis absen adalah "keluar", pastikan status juga "keluar"
        if ("keluar".equalsIgnoreCase(jenis)) {
            status = "keluar";
        }

        // Hitung durasi kerja dalam menit
        long durasiMenit = 0;
        if (jamMasuk != null && jamKeluar != null) {
            try {
                LocalTime waktuMasuk = parseWaktu(jamMasuk);
                LocalTime waktuKeluar = parseWaktu(jamKeluar);

                if (waktuKeluar.isBefore(waktuMasuk)) {
                    // Shift malam, keluar keesokan harinya
                    durasiMenit = ChronoUnit.MINUTES.between(waktuMasuk, waktuKeluar.plusHours(24));
                } else {
                    durasiMenit = ChronoUnit.MINUTES.between(waktuMasuk, waktuKeluar);
                }
            } catch (DateTimeParseException e) {
                System.err.println("Format waktu salah: " + e.getMessage());
            }
        }

        // Format durasi ke "HH:mm"
        String durasiFormatted = String.format("%02d:%02d", durasiMenit / 60, durasiMenit % 60);
        System.out.println("Durasi: " + durasiFormatted);

        // Buat objek RekapAbsensi dan tambahkan ke list
        listAbsensi.add(new RekapAbsensi(
            nama, shift, todayString, durasiFormatted, 
            null, jamMasuk, jamKeluar, status, null, jenis));
    }

    // Set listAbsensi ke tabel JavaFX Anda
    tabelAbsensi.setItems(listAbsensi);
}

private void showPopup(StackPane popup) {
        popup.setVisible(true);
        FadeTransition fadeIn = new FadeTransition(javafx.util.Duration.millis(500), popup);
        TranslateTransition slideDown = new TranslateTransition(javafx.util.Duration.millis(500), popup);

        fadeIn.setFromValue(0); fadeIn.setToValue(1);
        slideDown.setFromY(-80); slideDown.setToY(0);
        new ParallelTransition(fadeIn, slideDown).play();
    }

    private void hidePopup(StackPane popup) {
        FadeTransition fadeOut = new FadeTransition(javafx.util.Duration.millis(500), popup);

        TranslateTransition slideUp = new TranslateTransition(javafx.util.Duration.millis(500), popup);

        fadeOut.setFromValue(1); fadeOut.setToValue(0);
        slideUp.setFromY(0); slideUp.setToY(-80);
        ParallelTransition hide = new ParallelTransition(fadeOut, slideUp);
        hide.setOnFinished(e -> {
            popup.setVisible(false);
            lbPopUp.setText("Tambah Absen");
           
        });
    
        hide.play();

        loadAbsen();
       
        clearFields();
        
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

private void HandleHapus() {
    RekapAbsensi selectedAbsensi = tabelAbsensi.getSelectionModel().getSelectedItem();
    if (selectedAbsensi != null) {
        // Membuat dialog konfirmasi
        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationAlert.setTitle("Konfirmasi Penghapusan");
        confirmationAlert.setHeaderText("Yakin ingin menghapus data absensi?");
        confirmationAlert.setContentText("Nama: " + selectedAbsensi.getNama());

        // Menunggu pilihan pengguna
        if (confirmationAlert.showAndWait().get() == ButtonType.OK) {
            // Pengguna memilih OK
            database.getCollection("Absensi").deleteOne(
                new Document("nama", selectedAbsensi.getNama())
                    .append("shift", selectedAbsensi.getShift())
                    .append("tanggal", selectedAbsensi.getTanggal())
            );
            tabelAbsensi.getItems().remove(selectedAbsensi);
            showAlert("Data Absensi berhasil dihapus!", Alert.AlertType.INFORMATION);
        } else {
            // Pengguna memilih CANCEL
            showAlert("Penghapusan dibatalkan!", Alert.AlertType.INFORMATION);
        }
    } else {
        showAlert("Pilih data absensi yang ingin dihapus!", Alert.AlertType.WARNING);
    }
}

 private void showAlert(String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle("Notification");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void HandleEdit() {
       
        lbKeluar.setVisible(true);
        txtKeluar.setVisible(true);
        pnMasuk.setVisible(true);
            RekapAbsensi selectedAbsensi = tabelAbsensi.getSelectionModel().getSelectedItem();
            if (selectedAbsensi != null) {
    MongoCollection<Document> absenCollection = database.getCollection("Absensi");
               FindIterable<Document> karyawanIterable = absenCollection.find(new Document("nama", selectedAbsensi.getNama()).append("waktu", selectedAbsensi.getJamMasuk()));
            Document karyawan = karyawanIterable.first();
            if (karyawan != null) {
                String hari = karyawan.getString("hari");
                // Update this block to match your RekapAbsensi fields and popup logic
                lbPopUp.setText("Edit Data Absensi");
                cbNama.setValue(selectedAbsensi.getNama());
                cbHari.setValue(hari);
                cbShift.setValue(selectedAbsensi.getShift());
                dpTanggal.setValue(LocalDate.parse(selectedAbsensi.getTanggal()));
                txtMasuk.setText(selectedAbsensi.getJamMasuk());
                txtKeluar.setText(selectedAbsensi.getJamKeluar());
                cbStatus.setValue(selectedAbsensi.getJenisAbsen());
                showPopup(popAbsen);
            } else {
                showAlert("Pilih data absensi yang ingin diedit!", Alert.AlertType.WARNING);
            }
    }
    }
    private void HandleUpdate() {
    RekapAbsensi selectedAbsensi = tabelAbsensi.getSelectionModel().getSelectedItem();
    if (selectedAbsensi != null) {
        String shift = selectedAbsensi.getShift();
        String keluar = txtKeluar.getText();
        String mulai = txtMasuk.getText();
        String hari = cbHari.getValue();
        LocalDate tanggal = dpTanggal.getValue();

        if (shift == null || hari == null || tanggal == null) {
            showAlert("Field tidak boleh kosong", Alert.AlertType.WARNING);
        } else {
            MongoCollection<Document> karyawanCollection = database.getCollection("Karyawan");
            Document karyawan = karyawanCollection.find(eq("nama", selectedAbsensi.getNama())).first();
            if (karyawan != null) {
                String rfid = karyawan.getString("rfid");

                MongoCollection<Document> collection = database.getCollection("Absensi");

// Temukan satu dokumen berdasarkan RFID dan tanggal
Document filter = collection.find(new Document("rfid", rfid)
        .append("tanggal", tanggal.toString())
        .append("shift", shift)).first();

if (filter != null) {
    // // Gunakan _id sebagai filter agar hanya satu dokumen ini yang diperbarui
    // Document filter = new Document("_id", selectedDoc.getObjectId("_id"));

    // Data yang akan diperbarui
    Document updatedDoc = new Document("$set", new Document("shift", shift)
            .append("waktu", mulai)
            .append("jenisAbsen", "keluar")
            .append("waktuKeluar", keluar));

    collection.updateOne(filter, updatedDoc);
}

                loadAbsen();
                hidePopup(popAbsen);
                showAlert("Data Absensi berhasil diperbarui!", Alert.AlertType.INFORMATION);
                clearFields();
                lbKeluar.setVisible(false);
                txtKeluar.setVisible(false);
                pnMasuk.setVisible(false);
            }
        }
    } else {
        showAlert("Pilih karyawan yang ingin diperbarui!", Alert.AlertType.WARNING);
    }
}
    private void clearFields() {
        cbNama.setValue(null);
        cbNama.setPromptText("Pilih nama");
        cbShift.setValue(null);
        cbHari.setValue(null);
        cbHari.setPromptText("Pilih hari");
        cbStatus.setValue(null);
        cbStatus.setPromptText("Pilih Jenis Absen");
        dpTanggal.setValue(null);
        dpTanggal.setPromptText("Pilih tanggal");
        txtMasuk.setText("");
        txtMasuk.setPromptText("Masukkan jam masuk");
        txtKeluar.setText("");
        cbJenis.setValue(null);
        cbJenis.setPromptText("Pilih status");
        txtKeluar.setPromptText("Masukkan jam keluar");
    }

  private LocalTime parseWaktu(String waktuStr) {
    DateTimeFormatter[] formatters = new DateTimeFormatter[] {
        DateTimeFormatter.ofPattern("HH:mm:ss"),
        DateTimeFormatter.ofPattern("HH:mm")
    };

    for (DateTimeFormatter fmt : formatters) {
        try {
            return LocalTime.parse(waktuStr, fmt);
        } catch (DateTimeParseException e) {
            // coba formatter lain
        }
    }
    throw new DateTimeParseException("Format waktu tidak dikenali", waktuStr, 0);
}


}
