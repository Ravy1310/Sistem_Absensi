package com.example.demo;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.bson.Document;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import javafx.animation.FadeTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

public class DashboardControl {
    @FXML private StackPane popAbsen;
    @FXML private Pane ContentArea;
    @FXML private Button btnKaryawan, btnShift, btnKelompok;
    @FXML private Label lbKaryawan, lbShift, lbKelompok;
    @FXML private TableView<RekapAbsensi> tabelAbsensi;
    @FXML private TableColumn<RekapAbsensi, String> colNo, colNama, colShift, colTanggal, colWaktuMasuk, colWaktuKeluar, colStatus;
    

    private MongoDatabase database;
    public DashboardControl() {
        database = MongoDBConnection.getDatabase();
    }

    @FXML
    private void initialize() {
        popAbsen.setVisible(false);
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

private void loadAbsen() {
    ObservableList<RekapAbsensi> listAbsensi = FXCollections.observableArrayList();
    
    // Ambil data dari koleksi "rekapAbsensi"
    MongoCollection<Document> rekapAbsensiCollection = database.getCollection("Absensi");

    // Mendapatkan tanggal hari ini
    LocalDate today = LocalDate.now();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    String todayString = today.format(formatter);

    for (Document doc : rekapAbsensiCollection.find()) {
        // Ambil data dari kolom di database rekapAbsensi
        String rfid = doc.getString("rfid");
        String tanggal = doc.getString("tanggal");
        String jamMasuk = doc.getString("waktu");
        String jamKeluar = doc.getString("waktuKeluar");
        String jenis = doc.getString("jenisAbsen");
        String status = doc.getString("status");

        if(jenis.equals("keluar")) {
            
            status = "keluar";
        }

        // Memeriksa apakah data memiliki tanggal yang sama dengan hari ini
        if (tanggal.equals(todayString)) {
            // Ambil nama dan shift berdasarkan RFID dari koleksi "KelompokKerja"
            MongoCollection<Document> kelompokKerjaCollection = database.getCollection("KelompokKerja");
            Document kelompokDoc = kelompokKerjaCollection.find(new Document("rfid", rfid)).first();
            String nama = kelompokDoc != null ? kelompokDoc.getString("nama") : "Nama Tidak Ditemukan";
            String shift = kelompokDoc != null ? kelompokDoc.getString("shift") : "Shift Tidak Ditemukan";

            // Masukkan data ke dalam listAbsensi
            listAbsensi.add(new RekapAbsensi(nama, shift, tanggal, null, jamMasuk , jamKeluar, status, null, jenis));
        }
    }

    // Set listAbsensi ke tabel Anda, misalnya melalui TableView
    tabelAbsensi.setItems(listAbsensi);
}
}
