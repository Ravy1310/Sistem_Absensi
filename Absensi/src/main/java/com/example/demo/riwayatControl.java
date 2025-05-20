package com.example.demo;


import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.bson.Document;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

public class riwayatControl {
    @FXML private DatePicker dpTanggal;
    @FXML private TableView<RekapAbsensi> tblRekap;
    @FXML private TableColumn<RekapAbsensi, String> colNama, colNo;
    @FXML private TableColumn<RekapAbsensi, String> colshift;
    @FXML private TableColumn<RekapAbsensi, String> coltanggal;
    @FXML private TableColumn<RekapAbsensi, String> colmasuk;
    @FXML private TableColumn<RekapAbsensi, String> colkeluar;
    @FXML private TableColumn<RekapAbsensi, String> coljenis, colstatus;
    @FXML private Button btnLogOut, btnCari;
    @FXML private ImageView imCari;
    @FXML private TextField txtCari;

  private final MongoDatabase database;

    public riwayatControl() {
        database = MongoDBConnection.getDatabase();
    }

    @FXML
    private void initialize() {

        btnLogOut.setOnMouseClicked(event -> {
            loadPage("/com/example/demo/absensi karyawan/absen.fxml", event);
        });

        colNo.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : String.valueOf(getIndex() + 1));
            }
        });
        btnCari.setOnAction(e -> cariData());
        imCari.setOnMouseClicked(e -> cariData());

colNama.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNama()));
    colshift.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getShift()));
    coltanggal.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTanggal()));
    colmasuk.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getJamMasuk()));
    colkeluar.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getJamKeluar()));
    colstatus.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus()));
    coljenis.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getJenisAbsen()));
 loadAbsen();

  dpTanggal.setValue(LocalDate.now());
      

        // Tampilkan data setiap kali tanggal dipilih
        dpTanggal.setOnAction(e -> tampilkanRiwayat(dpTanggal.getValue()));
    }

    private void loadAbsen() {
    ObservableList<RekapAbsensi> listAbsensi = FXCollections.observableArrayList();
    
    MongoCollection<Document> rekapAbsensiCollection = database.getCollection("Absensi");

    LocalDate today = LocalDate.now();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    String todayString = today.format(formatter);

    for (Document doc : rekapAbsensiCollection.find(new Document("tanggal", todayString))) {
        String rfid = doc.getString("rfid");
        String nama = doc.getString("nama"); // Ambil langsung dari Absensi
        String shift = doc.getString("shift"); // Ambil langsung dari Absensi
        String jamMasuk = doc.getString("waktu");
        String jamKeluar = doc.getString("waktuKeluar");
        String jenis = doc.getString("jenisAbsen");
        String status = doc.getString("status");

        if (jenis.equals("keluar")) {
            status = "keluar";
        }

        listAbsensi.add(new RekapAbsensi(nama, shift, todayString, null, jamMasuk, jamKeluar, status, null, jenis));
         tblRekap.setItems(listAbsensi);
    }

}

 private void tampilkanRiwayat(LocalDate selectedDate) {
    if (selectedDate == null) return;

    String tanggal = selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    MongoCollection<Document> absensiCollection = database.getCollection("Absensi"); // Samakan dengan yang di loadAbsen()

    MongoCursor<Document> cursor = absensiCollection.find(new Document("tanggal", tanggal)).iterator();

    ObservableList<RekapAbsensi> data = FXCollections.observableArrayList();

    try {
        while (cursor.hasNext()) {
            Document doc = cursor.next();

            RekapAbsensi rekapan = new RekapAbsensi();
            rekapan.setNama(doc.getString("nama"));
            rekapan.setShift(doc.getString("shift"));
            rekapan.setTanggal(doc.getString("tanggal"));
            rekapan.setJamMasuk(doc.getString("waktu"));         // ganti dari jamMasuk
            rekapan.setJamKeluar(doc.getString("waktuKeluar"));  // ganti dari jamKeluar
            rekapan.setJenisAbsen(doc.getString("jenisAbsen"));
            rekapan.setStatus(doc.getString("status"));

            data.add(rekapan);
        }
    } finally {
        cursor.close();
    }

    tblRekap.setItems(data);
}

 private void loadPage(String fxmlFile, MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow(); // diperbaiki di sini
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
private void cariData() {
    String namaCari = txtCari.getText().trim();
    LocalDate tanggalDipilih = dpTanggal.getValue();

    // Koneksi ke koleksi Absensi
    MongoCollection<Document> absensiCollection = database.getCollection("Absensi");

    Document query = new Document();

    // Filter berdasarkan nama jika diisi
    if (!namaCari.isEmpty()) {
        query.append("nama", new Document("$regex", namaCari).append("$options", "i")); // pencarian tidak case-sensitive
    }

    // Filter berdasarkan tanggal jika dipilih
    if (tanggalDipilih != null) {
        String tanggalString = tanggalDipilih.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        query.append("tanggal", tanggalString);
    }

    // Ambil data dari database
    MongoCursor<Document> cursor = absensiCollection.find(query).iterator();
    ObservableList<RekapAbsensi> hasilPencarian = FXCollections.observableArrayList();

    try {
        while (cursor.hasNext()) {
            Document doc = cursor.next();

            RekapAbsensi rekapan = new RekapAbsensi();
            rekapan.setNama(doc.getString("nama"));
            rekapan.setShift(doc.getString("shift"));
            rekapan.setTanggal(doc.getString("tanggal"));
            rekapan.setJamMasuk(doc.getString("waktu"));
            rekapan.setJamKeluar(doc.getString("waktuKeluar"));
            rekapan.setJenisAbsen(doc.getString("jenisAbsen"));
            rekapan.setStatus(doc.getString("status"));

            hasilPencarian.add(rekapan);
            txtCari.clear(); 
        }
    } finally {
        cursor.close();
    }

    // Tampilkan hasil
    tblRekap.setItems(hasilPencarian);
}

}