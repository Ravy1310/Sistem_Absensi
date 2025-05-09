package com.example.demo;

import java.time.LocalDate;

import org.bson.Document;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import com.mongodb.client.model.Filters;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;

public class RekapAbsensiControl {
    @FXML private ImageView imCari;
    @FXML private Button btnCari;
    @FXML private TextField txtCari;
    @FXML private DatePicker  dtTanggal;
    @FXML TableView<RekapAbsensi> tblAbsensi;
    @FXML TableColumn<RekapAbsensi, String> colNo, colNama, colHari, colShift, colTanggal, colWaktuMasuk, colWaktuKeluar, colStatus;
    private final MongoDatabase database;
    // Removed unused field absensiList

    public RekapAbsensiControl() {
        database = MongoDBConnection.getDatabase();
    }
    @FXML
    private void initialize() {
        // Set up the table columns
        colNo.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : String.valueOf(getIndex() + 1));
            }
        });
        colNama.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getNama()));
        colHari.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getNamaHari()));
        colShift.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getShift()));
        colTanggal.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getTanggal()));
        colWaktuMasuk.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getJamMasuk()));
        colWaktuKeluar.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getJamKeluar()));
        colStatus.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getStatus()));
        imCari.setOnMouseClicked(event -> {
            handleCari();
        });
        btnCari.setOnAction(event -> {
            handleCari();
        });
        dtTanggal.setOnAction(event ->  {
            PilihTanggal();
        });
        // Load data from MongoDB and populate the table
        loadAbsensiData();
    }

    private void PilihTanggal() {
       LocalDate pTanggal = dtTanggal.getValue();
        if (pTanggal != null) {
            String tanggal = pTanggal.toString();
            // Filter data berdasarkan tanggal yang dipilih
            ObservableList<RekapAbsensi> filteredList = FXCollections.observableArrayList();
            MongoCollection<Document> rekapAbsensiCollection = database.getCollection("rekapAbsensi");
            for (Document doc : rekapAbsensiCollection.find()) {
                String tgl = doc.getString("tanggal");
                if (tgl.equals(tanggal)) {
                    String nama = doc.getString("nama");
                    String hari = doc.getString("hari");
                    String shift = doc.getString("shift");
                    String jamMasuk = doc.getString("waktu"); // Jam masuk
                    String jamKeluar = doc.getString("waktuKeluar"); // Jam keluar, jika tersedia
                    String status = doc.getString("status");

                    RekapAbsensi absensi = new RekapAbsensi(nama, shift, tanggal, null, jamMasuk, jamKeluar, status, hari, null);
                    filteredList.add(absensi);
                }
            }
            tblAbsensi.setItems(filteredList);
        } else {
            loadAbsensiData(); // Jika tidak ada tanggal yang dipilih, muat semua data
        }

    }
   
private void loadAbsensiData() {
    // Buat ObservableList untuk menyimpan data absensi
    ObservableList<RekapAbsensi> listAbsensi = FXCollections.observableArrayList();

    // Ambil koleksi rekapAbsensi dari database MongoDB
    MongoCollection<Document> rekapAbsensiCollection = database.getCollection("rekapAbsensi");

    // Ambil bulan dan tahun saat ini
    LocalDate today = LocalDate.now();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
    String currentMonth = today.format(formatter); // Format tanggal menjadi "YYYY-MM"

    // Filter data berdasarkan bulan saat ini
    for (Document doc : rekapAbsensiCollection.find()) {
        String tanggal = doc.getString("tanggal"); // Pastikan format tanggal sesuai dengan database
        
        if (tanggal != null && tanggal.startsWith(currentMonth)) { // Bandingkan bulan dan tahun
            // Ambil data dari dokumen
            String nama = doc.getString("nama");
            String hari = doc.getString("hari");
            String shift = doc.getString("shift");
            String jamMasuk = doc.getString("waktu"); // Jam masuk
            String jamKeluar = doc.getString("waktuKeluar"); // Jam keluar, jika tersedia
            String status = doc.getString("status");

            // Buat objek RekapAbsensi dari data dokumen
            RekapAbsensi absensi = new RekapAbsensi(nama, shift, tanggal, null, jamMasuk, jamKeluar, status, hari, null);

            // Tambahkan objek absensi ke ObservableList
            listAbsensi.add(absensi);
        }
    }

    // Set ObservableList sebagai data untuk TableView
    tblAbsensi.setItems(listAbsensi);
}

    private void handleCari() {
        String keyword = txtCari.getText().trim().toLowerCase();
        LocalDate pTanggal = dtTanggal.getValue();
        if(pTanggal == null) {
            // Jika tidak ada tanggal yang dipilih, cari berdasarkan nama
            ObservableList<RekapAbsensi> filteredList = FXCollections.observableArrayList();
            MongoCollection<Document> rekapAbsensiCollection = database.getCollection("rekapAbsensi");
            for (Document doc : rekapAbsensiCollection.find()) {
                String nama = doc.getString("nama").toLowerCase();
                if (nama.contains(keyword)) {
                    String hari = doc.getString("hari");
                    String shift = doc.getString("shift");
                    String tanggal = doc.getString("tanggal");
                    String jamMasuk = doc.getString("waktu"); // Jam masuk
                    String jamKeluar = doc.getString("waktuKeluar"); // Jam keluar, jika tersedia
                    String status = doc.getString("status");

                    RekapAbsensi absensi = new RekapAbsensi(nama, shift, tanggal, null, jamMasuk, jamKeluar, status, hari, null);
                    filteredList.add(absensi);
                }
            }
            tblAbsensi.setItems(filteredList);
        } else {
            // Jika ada tanggal yang dipilih, cari berdasarkan nama dan tanggal
            ObservableList<RekapAbsensi> filteredList = FXCollections.observableArrayList();
            MongoCollection<Document> rekapAbsensiCollection = database.getCollection("rekapAbsensi");
            for (Document doc : rekapAbsensiCollection.find()) {
                String nama = doc.getString("nama").toLowerCase();
                String tgl = doc.getString("tanggal");
                if (tgl.equals(pTanggal.toString()) && nama.contains(keyword)) {
                    String hari = doc.getString("hari");
                    String shift = doc.getString("shift");
                    String tanggal = doc.getString("tanggal");
                    String jamMasuk = doc.getString("waktu"); // Jam masuk
                    String jamKeluar = doc.getString("waktuKeluar"); // Jam keluar, jika tersedia
                    String status = doc.getString("status");

                    RekapAbsensi absensi = new RekapAbsensi(nama, shift, tanggal, null, jamMasuk, jamKeluar, status, hari, null);
                    filteredList.add(absensi);
                }
            }
            tblAbsensi.setItems(filteredList);
        }
        txtCari.clear(); // Kosongkan field pencarian setelah pencarian selesai
    }
}
