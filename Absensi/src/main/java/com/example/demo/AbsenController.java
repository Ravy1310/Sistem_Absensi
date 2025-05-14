package com.example.demo;

import java.io.IOException;
import java.net.URL;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.UUID;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class AbsenController implements Initializable {
    @FXML
    private Pane panelogin, btnriwayat;
    @FXML TextField txtRFID;

    @FXML
private TableView<RekapAbsensi> tabelAbsensi;
@FXML
private TableColumn<RekapAbsensi, String> colNo;
@FXML
private TableColumn<RekapAbsensi, String> colNama;
@FXML
private TableColumn<RekapAbsensi, String> colShift;
@FXML
private TableColumn<RekapAbsensi, String> colTanggal;
@FXML
private TableColumn<RekapAbsensi, String> colJam;
@FXML
private TableColumn<RekapAbsensi, String> colJenis;
@FXML
private TableColumn<RekapAbsensi, String> colStatus;

    private MongoDatabase database;
    public AbsenController() {
        database = MongoDBConnection.getDatabase();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    
       
        colNo.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : String.valueOf(getIndex() + 1));
            }
        });

colNama.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNama()));
    colShift.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getShift()));
    colTanggal.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTanggal()));
    colJam.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getJam()));
    colJenis.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getJenisAbsen()));
    colStatus.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus()));


     panelogin.setOnMouseClicked(event -> {
        loadPage("/com/example/demo/Login/login.fxml", event);
    });

    btnriwayat.setOnMouseClicked(event -> {
        loadPage("/com/example/demo/riwayat/rekapabsensi.fxml", event);
    });
    txtRFID.textProperty().addListener((observable, oldValue, newValue) -> {
    if (newValue.length() == 10) { // Panjang RFID
        if (rfidTerdaftar(newValue)) {
            simpanKeRekapAbsensi(newValue);
        } else {
            Platform.runLater(() -> {
                Alert alert = new Alert(AlertType.WARNING);
                alert.setTitle("RFID Tidak Terdaftar");
                alert.setHeaderText(null);
                alert.setContentText("RFID ini belum terdaftar sebagai karyawan.");
                alert.showAndWait();
            });
        }

        Platform.runLater(() -> txtRFID.clear());
    }
   
});
loadAbsen();
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
        String jam = doc.getString("waktu");
        String jenis = doc.getString("jenisAbsen");
        String status = doc.getString("status");
        String shift = doc.getString("shift");

        if(jenis.equals("keluar")) {
            jam = doc.getString("waktuKeluar");
            status = "keluar";
        }

        // Memeriksa apakah data memiliki tanggal yang sama dengan hari ini
        if (tanggal.equals(todayString)) {
            // Ambil nama dan shift berdasarkan RFID dari koleksi "KelompokKerja"
            MongoCollection<Document> kelompokKerjaCollection = database.getCollection("KelompokKerja");
            Document kelompokDoc = kelompokKerjaCollection.find(new Document("rfid", rfid)).first();
            String nama = kelompokDoc != null ? kelompokDoc.getString("nama") : "Nama Tidak Ditemukan";
           

if (kelompokDoc != null) {
    System.out.println("Shift ditemukan untuk RFID " + rfid );
}

            // Masukkan data ke dalam listAbsensi
            listAbsensi.add(new RekapAbsensi(nama, shift, tanggal,null, jam, null, null, status, null, jenis));
        }
    }

    // Set listAbsensi ke tabel Anda, misalnya melalui TableView
    tabelAbsensi.setItems(listAbsensi);
}

private void simpanKeRekapAbsensi(String rfid) {
    try {
        MongoCollection<Document> jamKerjaCollection = database.getCollection("JamKerja");
        MongoCollection<Document> kelompokKerjaCollection = database.getCollection("KelompokKerja");
        MongoCollection<Document> rekapCollection = database.getCollection("Absensi");
        MongoCollection<Document> pengajuanLemburCollection = database.getCollection("PengajuanLembur");

        // Ambil semua shift
        List<Document> shifts = jamKerjaCollection.find().into(new ArrayList<>());

        if (shifts.isEmpty()) {
            System.out.println("Data shift tidak ditemukan di database.");
            return;
        }

        // Waktu sekarang
        LocalTime sekarang = LocalTime.now();
        String tanggalSekarang = LocalDate.now().toString();

        // Cek apakah sudah pernah absen masuk
        Document existingAbsensi = rekapCollection.find(and(eq("rfid", rfid), eq("tanggal", tanggalSekarang))).first();

        String shiftAktual = null;
        LocalTime waktuMulaiShift = null;
        LocalTime waktuSelesaiShift = null;

        if (existingAbsensi != null && "masuk".equals(existingAbsensi.getString("jenisAbsen"))) {
            // Gunakan shift dari absensi masuk
            shiftAktual = existingAbsensi.getString("shift");

            Document shift = jamKerjaCollection.find(eq("shift", shiftAktual)).first();
            if (shift != null) {
                waktuMulaiShift = LocalTime.parse(shift.getString("waktuMulai"));
                waktuSelesaiShift = LocalTime.parse(shift.getString("waktuSelesai"));
            } else {
                System.out.println("Shift tidak ditemukan di jamKerja.");
                return;
            }
        } else {
            // Cari shift berdasarkan waktu sekarang (untuk absen masuk)
            for (Document shift : shifts) {
                String shiftName = shift.getString("shift");
                LocalTime mulai = LocalTime.parse(shift.getString("waktuMulai"));
                LocalTime selesai = LocalTime.parse(shift.getString("waktuSelesai"));

                if ((sekarang.isAfter(mulai) || sekarang.equals(mulai)) && sekarang.isBefore(selesai)) {
                    shiftAktual = shiftName;
                    waktuMulaiShift = mulai;
                    waktuSelesaiShift = selesai;
                    break;
                }
            }

            if (shiftAktual == null || waktuMulaiShift == null) {
                System.out.println("Tidak ada shift yang cocok dengan waktu sekarang.");
                return;
            }
        }

        // Cari data karyawan sesuai shift dan RFID
        Bson filter = and(eq("rfid", rfid), eq("shift", shiftAktual));
        Document karyawan = kelompokKerjaCollection.find(filter).first();

        if (karyawan != null) {
            if (existingAbsensi == null) {
                // Absen masuk
                long diffInMinutes = Duration.between(waktuMulaiShift, sekarang).toMinutes();
                String statusAbsen = (diffInMinutes <= 30) ? "tepat waktu" : "terlambat";

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
                String waktuFormatted = LocalDateTime.now().format(formatter);

                String idAbsensi = UUID.randomUUID().toString();
                String hariDalamBahasaIndonesia = getHariDalamBahasaIndonesia(LocalDate.now().getDayOfWeek());

                Document absenMasuk = new Document("id_absensi", idAbsensi)
                        .append("nama", karyawan.getString("nama"))
                        .append("rfid", rfid)
                        .append("shift", shiftAktual)
                        .append("tanggal", tanggalSekarang)
                        .append("hari", hariDalamBahasaIndonesia)
                        .append("waktu", waktuFormatted)
                        .append("jenisAbsen", "masuk")
                        .append("status", statusAbsen);

                rekapCollection.insertOne(absenMasuk);
                System.out.println("Absen masuk berhasil disimpan untuk shift: " + shiftAktual + " pada tanggal: " + tanggalSekarang + " dengan status: " + statusAbsen);
            } else if ("masuk".equals(existingAbsensi.getString("jenisAbsen"))) {
                // Cek pengajuan lembur
                Document pengajuanLembur = pengajuanLemburCollection.find(
                        and(eq("rfid", rfid), eq("tanggal", tanggalSekarang))
                ).first();

                LocalTime waktuMinimalKeluar = waktuSelesaiShift;

                if (pengajuanLembur != null) {
                    String durasiLembur = pengajuanLembur.getString("durasi");
                    int durasiLemburInt = Integer.parseInt(durasiLembur);
                    waktuMinimalKeluar = waktuSelesaiShift.plusHours(durasiLemburInt);
                }

if (sekarang.isBefore(waktuMinimalKeluar)) {
    Platform.runLater(() -> {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Peringatan");
        alert.setHeaderText(null);
        alert.setContentText("Belum waktunya keluar");
        alert.getButtonTypes().setAll(ButtonType.OK);
        alert.initModality(Modality.APPLICATION_MODAL);
        
        alert.showAndWait();
        
        alert.setOnCloseRequest(event -> {
            System.out.println("Alert ditutup");
        });
    });
    return; 
}
                // Simpan absen keluar
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
                String waktuFormatted = LocalDateTime.now().format(formatter);

                rekapCollection.updateOne(
                        and(eq("rfid", rfid), eq("tanggal", tanggalSekarang)),
                        new Document("$set", new Document("jenisAbsen", "keluar")
                                .append("waktuKeluar", waktuFormatted))
                );

                System.out.println("Absen keluar berhasil disimpan untuk shift: " + shiftAktual + " pada tanggal: " + tanggalSekarang);
            }
        }

    } catch (Exception e) {
        e.printStackTrace();
    }

    loadAbsen();
}



// Fungsi untuk mengonversi nama hari ke Bahasa Indonesia
private String getHariDalamBahasaIndonesia(DayOfWeek dayOfWeek) {
    switch (dayOfWeek) {
        case MONDAY:
            return "Senin";
        case TUESDAY:
            return "Selasa";
        case WEDNESDAY:
            return "Rabu";
        case THURSDAY:
            return "Kamis";
        case FRIDAY:
            return "Jumat";
        case SATURDAY:
            return "Sabtu";
        case SUNDAY:
            return "Minggu";
        default:
            return "Tidak diketahui";
    }
}
    

    private boolean rfidTerdaftar(String rfid) {
        MongoCollection<Document> karyawanCollection = database.getCollection("KelompokKerja");
        Document query = new Document("rfid", rfid);
        return karyawanCollection.find(query).first() != null;}

   

  

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

    private void showAlert(String message, AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle("Notification");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.getButtonTypes().setAll(ButtonType.OK);
        alert.showAndWait();
    }
    

    

    


    
    

    
}
