package com.example.demo;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.bson.Document;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;

public class RekapAbsensiControl {
    @FXML private ComboBox<String> cbBulan, cbTahun;
    @FXML private Button btnCari;
    @FXML private TextField txtCari;
    @FXML private ImageView imCari;

    @FXML private TableView<RekapBulanan> tblRekap;
    @FXML private TableColumn<RekapBulanan, String> colNama, colNo;
    @FXML private TableColumn<RekapBulanan, String> colBulan;
    @FXML private TableColumn<RekapBulanan, String> colMasuk;
    @FXML private TableColumn<RekapBulanan, String> colIzin;
    @FXML private TableColumn<RekapBulanan, String> colSakit;
    @FXML private TableColumn<RekapBulanan, String> colAlfa, colDurasi;

    private final MongoDatabase database;

    public RekapAbsensiControl() {
        database = MongoDBConnection.getDatabase();
    }

    @FXML
    private void initialize() {
        setupComboBoxes();
        setupEventHandlers();
        setupTableColumns();
        setupTableListeners();
        
        // Load data existing dulu untuk response yang cepat
        loadRekapData();
        
        // Jalankan perhitungan di background thread
        CompletableFuture.runAsync(() -> {
            try {
                calculateAndUpdateDailyDuration();
                rekapBulanan();
                
                // Update UI di JavaFX thread
                Platform.runLater(() -> loadRekapData());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void setupComboBoxes() {
        cbBulan.getItems().addAll(
            "01", "02", "03", "04", "05", "06", 
            "07", "08", "09", "10", "11", "12"
        );
        int tahunSekarang = LocalDate.now().getYear();
        for (int i = tahunSekarang - 5; i <= tahunSekarang + 1; i++) {
            cbTahun.getItems().add(String.valueOf(i));
        }

        cbBulan.setValue(LocalDate.now().format(DateTimeFormatter.ofPattern("MM")));
        cbTahun.setValue(String.valueOf(tahunSekarang));
    }

    private void setupEventHandlers() {
        btnCari.setOnAction(event -> handleCari());
        imCari.setOnMouseClicked(event -> handleCari());
    }

    private void setupTableColumns() {
        colNo.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : String.valueOf(getIndex() + 1));
            }
        });

        colNama.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getNama()));
        colBulan.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getBulan()));
        colMasuk.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(String.valueOf(cell.getValue().getMasuk())));
        colIzin.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(String.valueOf(cell.getValue().getIzin())));
        colSakit.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(String.valueOf(cell.getValue().getSakit())));
        colAlfa.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(String.valueOf(cell.getValue().getAlfa())));
        colDurasi.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getDurasi()));
    }

    private void setupTableListeners() {
        cbBulan.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (cbTahun.getValue() != null && newVal != null) {
                filterRekapBulanan();
            }
        });

        cbTahun.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (cbBulan.getValue() != null && newVal != null) {
                filterRekapBulanan();
            }
        });
    }

    private void calculateAndUpdateDailyDuration() {
        MongoCollection<Document> absensiCollection = database.getCollection("Absensi");
        
        // Optimasi: hanya proses data bulan ini dan yang belum memiliki durasi
        String bulanIni = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        
        Document filter = new Document("jenisAbsen", "keluar")
            .append("tanggal", new Document("$regex", "^" + bulanIni))
            .append("$or", List.of(
                new Document("durasi", new Document("$exists", false)),
                new Document("durasi", null),
                new Document("durasi", "")
            ));
        
        for (Document doc : absensiCollection.find(filter)) {
            String waktuMasukStr = doc.getString("waktu");
            String waktuKeluarStr = doc.getString("waktuKeluar");
            
            if (waktuMasukStr == null || waktuKeluarStr == null || 
                waktuMasukStr.isEmpty() || waktuKeluarStr.isEmpty()) {
                continue;
            }
            
            try {
                LocalTime waktuMasuk = parseLocalTime(waktuMasukStr);
                LocalTime waktuKeluar = parseLocalTime(waktuKeluarStr);
                
                long durasiMenit = calculateDurationInMinutes(waktuMasuk, waktuKeluar);
                String durasiFormatted = formatDuration(durasiMenit);
                
                Document update = new Document("$set", new Document("durasi", durasiFormatted)
                        .append("durasiMenit", durasiMenit));
                
                absensiCollection.updateOne(Filters.eq("_id", doc.getObjectId("_id")), update);
                
            } catch (Exception e) {
                System.err.println("Error calculating duration for ID: " + doc.getObjectId("_id") + " - " + e.getMessage());
            }
        }
    }

    private LocalTime parseLocalTime(String timeStr) {
        if (timeStr.matches("\\d{2}:\\d{2}:\\d{2}")) {
            return LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("HH:mm:ss"));
        } else {
            return LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("HH:mm"));
        }
    }

    private long calculateDurationInMinutes(LocalTime start, LocalTime end) {
        if (end.isBefore(start)) {
            // Shift malam yang melewati tengah malam
            return Duration.between(start, end.plusHours(24)).toMinutes();
        } else {
            return Duration.between(start, end).toMinutes();
        }
    }

    private void handleCari() {
        String keyword = txtCari.getText().toLowerCase();
        String selectedBulan = cbBulan.getValue();
        String selectedTahun = cbTahun.getValue();

        if (selectedBulan == null || selectedTahun == null) {
            return;
        }

        String bulanFinal = selectedTahun + "-" + selectedBulan;
        ObservableList<RekapBulanan> filteredData = FXCollections.observableArrayList();
        MongoCollection<Document> rekapCollection = database.getCollection("rekapAbsensi");

        // Optimasi: gunakan regex MongoDB untuk search
        Document query = new Document("bulan", bulanFinal);
        if (!keyword.isEmpty()) {
            query.append("nama", new Document("$regex", keyword).append("$options", "i"));
        }

        for (Document doc : rekapCollection.find(query)) {
            String nama = doc.getString("nama");
            int masuk = doc.getInteger("masuk", 0);
            int izin = doc.getInteger("izin", 0);
            int sakit = doc.getInteger("sakit", 0);
            int alfa = doc.getInteger("alfa", 0);
            String durasi = doc.getString("durasi");
            if (durasi == null) durasi = "00:00";

            filteredData.add(new RekapBulanan(nama, bulanFinal, masuk, izin, sakit, alfa, durasi));
        }

        tblRekap.setItems(filteredData);
    }

    private void rekapBulanan() {
        MongoCollection<Document> absensiCollection = database.getCollection("Absensi");
        MongoCollection<Document> rekapCollection = database.getCollection("rekapAbsensi");
        MongoCollection<Document> karyawanCollection = database.getCollection("Karyawan");
        MongoCollection<Document> kelompokKerjaCollection = database.getCollection("KelompokKerja");

        LocalDate today = LocalDate.now();
        String bulanIni = today.format(DateTimeFormatter.ofPattern("yyyy-MM"));

        LocalDate firstDay = today.withDayOfMonth(1);
        int daysInMonth = firstDay.lengthOfMonth();
        List<String> semuaTanggal = new ArrayList<>();
        for (int i = 1; i <= daysInMonth; i++) {
            semuaTanggal.add(firstDay.withDayOfMonth(i).toString());
        }

        // Cache nama to RFID mapping
        Map<String, String> namaToRfid = new HashMap<>();
        for (Document doc : karyawanCollection.find()) {
            String nama = doc.getString("nama");
            String rfid = doc.getString("rfid");
            if (nama != null && rfid != null) {
                namaToRfid.put(nama, rfid);
            }
        }

        Map<String, int[]> rekapMap = new HashMap<>();
        Map<String, Map<String, Integer>> kehadiranPerTanggal = new HashMap<>();
        Map<String, Long> totalDurasiMap = new HashMap<>();

        // Optimasi: filter hanya data bulan ini
        Document absensiFilter = new Document("tanggal", new Document("$regex", "^" + bulanIni));
        
        for (Document doc : absensiCollection.find(absensiFilter)) {
            String tanggal = doc.getString("tanggal");
            String nama = doc.getString("nama");
            String status = doc.getString("jenisAbsen").toLowerCase();
            
            // Ambil durasi yang sudah dihitung
            Long durasiMenit = doc.getLong("durasiMenit");
            if (durasiMenit != null && durasiMenit > 0) {
                totalDurasiMap.put(nama, totalDurasiMap.getOrDefault(nama, 0L) + durasiMenit);
            }

            // Inisialisasi data rekap jika belum ada
            rekapMap.putIfAbsent(nama, new int[]{0, 0, 0, 0});
            
            // Inisialisasi tracking kehadiran per tanggal jika belum ada
            kehadiranPerTanggal.putIfAbsent(nama, new HashMap<>());
            Map<String, Integer> kehadiranHarian = kehadiranPerTanggal.get(nama);
            
            // Increment jumlah kehadiran untuk tanggal tersebut
            kehadiranHarian.put(tanggal, kehadiranHarian.getOrDefault(tanggal, 0) + 1);
            
            // Update rekap berdasarkan jenis absensi
            int[] data = rekapMap.get(nama);
            switch (status) {
                case "keluar" -> data[0]++;
                case "izin" -> data[1]++;
                case "sakit" -> data[2]++;
                case "alfa" -> data[3]++;
            }
        }

        // Hitung alfa hanya jika akhir bulan
        if (today.getDayOfMonth() == daysInMonth) {
            calculateAlfa(namaToRfid, kehadiranPerTanggal, rekapMap, semuaTanggal, kelompokKerjaCollection);
        }

        // Simpan ke database
        saveRekapToDatabase(rekapMap, totalDurasiMap, namaToRfid, bulanIni, rekapCollection);
    }

    private void calculateAlfa(Map<String, String> namaToRfid, 
                             Map<String, Map<String, Integer>> kehadiranPerTanggal,
                             Map<String, int[]> rekapMap,
                             List<String> semuaTanggal,
                             MongoCollection<Document> kelompokKerjaCollection) {
        
        for (String nama : namaToRfid.keySet()) {
            String rfid = namaToRfid.get(nama);
            
            // Himpunan tanggal di mana karyawan hadir
            Set<String> hariHadir = new HashSet<>();
            if (kehadiranPerTanggal.containsKey(nama)) {
                hariHadir.addAll(kehadiranPerTanggal.get(nama).keySet());
            }

            // Cache shift data per hari
            Map<String, Integer> shiftPerHari = new HashMap<>();
            Map<String, Boolean> liburPerHari = new HashMap<>();
            
            for (Document shiftDoc : kelompokKerjaCollection.find(Filters.eq("rfid", rfid))) {
                String hari = shiftDoc.getString("hari").toLowerCase();
                String shift = shiftDoc.getString("shift");
                
                shiftPerHari.put(hari, shiftPerHari.getOrDefault(hari, 0) + 1);
                
                if (shift.equalsIgnoreCase("libur")) {
                    liburPerHari.put(hari, true);
                }
            }

            int totalShiftWajib = 0;
            
            for (String tanggal : semuaTanggal) {
                LocalDate tgl = LocalDate.parse(tanggal);
                String hariInggris = tgl.getDayOfWeek().name().toLowerCase();
                String hariIndonesia = getHariIndonesia(hariInggris);
                
                if (liburPerHari.getOrDefault(hariIndonesia, false)) {
                    continue; // Skip hari libur
                }
                
                int shiftHariIni = shiftPerHari.getOrDefault(hariIndonesia, 1);
                totalShiftWajib += shiftHariIni;
            }
            
            // Hitung total kehadiran
            int totalKehadiran = 0;
            if (kehadiranPerTanggal.containsKey(nama)) {
                Map<String, Integer> kehadiranHarian = kehadiranPerTanggal.get(nama);
                for (int count : kehadiranHarian.values()) {
                    totalKehadiran += count;
                }
            }
            
            // Hitung alfa
            int alfa = totalShiftWajib - totalKehadiran;
            if (alfa < 0) alfa = 0;

            // Update rekap alfa
            rekapMap.putIfAbsent(nama, new int[]{0, 0, 0, 0});
            rekapMap.get(nama)[3] = alfa;
        }
    }

    private void saveRekapToDatabase(Map<String, int[]> rekapMap, 
                                   Map<String, Long> totalDurasiMap,
                                   Map<String, String> namaToRfid,
                                   String bulanIni,
                                   MongoCollection<Document> rekapCollection) {
        
        for (Map.Entry<String, int[]> entry : rekapMap.entrySet()) {
            String nama = entry.getKey();
            String rfid = namaToRfid.get(nama);
            int[] data = entry.getValue();

            if (rfid == null) continue;

            Document filter = new Document("rfid", rfid).append("bulan", bulanIni);
            
            long totalDurasi = totalDurasiMap.getOrDefault(nama, 0L);
            String durasiFormatted = formatDuration(totalDurasi);
            
            Document update = new Document("$set", new Document("rfid", rfid)
                    .append("nama", nama)
                    .append("bulan", bulanIni)
                    .append("masuk", data[0])
                    .append("izin", data[1])
                    .append("sakit", data[2])
                    .append("alfa", data[3])
                    .append("durasi", durasiFormatted)
                    .append("durasiMenit", totalDurasi)
            );

            rekapCollection.updateOne(filter, update, new UpdateOptions().upsert(true));
        }
    }

    private void loadRekapData() {
        ObservableList<RekapBulanan> data = FXCollections.observableArrayList();
        MongoCollection<Document> rekapCollection = database.getCollection("rekapAbsensi");

        LocalDate today = LocalDate.now();
        String currentMonth = today.format(DateTimeFormatter.ofPattern("yyyy-MM"));

        for (Document doc : rekapCollection.find(Filters.eq("bulan", currentMonth))) {
            String nama = doc.getString("nama");
            int masuk = doc.getInteger("masuk", 0);
            int izin = doc.getInteger("izin", 0);
            int sakit = doc.getInteger("sakit", 0);
            int alfa = doc.getInteger("alfa", 0);
            String durasi = doc.getString("durasi");
            if (durasi == null) durasi = "00:00";

            data.add(new RekapBulanan(nama, currentMonth, masuk, izin, sakit, alfa, durasi));
        }

        tblRekap.setItems(data);
    }

    private void filterRekapBulanan() {
        String bulan = cbBulan.getValue();
        String tahun = cbTahun.getValue();
        if (bulan == null || tahun == null) return;

        String bulanFinal = tahun + "-" + bulan;

        ObservableList<RekapBulanan> data = FXCollections.observableArrayList();
        MongoCollection<Document> rekapCollection = database.getCollection("rekapAbsensi");

        for (Document doc : rekapCollection.find(Filters.eq("bulan", bulanFinal))) {
            String nama = doc.getString("nama");
            int masuk = doc.getInteger("masuk", 0);
            int izin = doc.getInteger("izin", 0);
            int sakit = doc.getInteger("sakit", 0);
            int alfa = doc.getInteger("alfa", 0);
            String durasi = doc.getString("durasi");
            if (durasi == null) durasi = "00:00";

            data.add(new RekapBulanan(nama, bulanFinal, masuk, izin, sakit, alfa, durasi));
        }

        tblRekap.setItems(data);
    }

    private String formatDuration(long durationInMinutes) {
        long hours = durationInMinutes / 60;
        long minutes = durationInMinutes % 60;
        return String.format("%02d:%02d", hours, minutes);
    }

    private String getHariIndonesia(String englishDay) {
        return switch (englishDay.toLowerCase()) {
            case "monday" -> "senin";
            case "tuesday" -> "selasa";
            case "wednesday" -> "rabu";
            case "thursday" -> "kamis";
            case "friday" -> "jumat";
            case "saturday" -> "sabtu";
            case "sunday" -> "minggu";
            default -> englishDay;
        };
    }
}