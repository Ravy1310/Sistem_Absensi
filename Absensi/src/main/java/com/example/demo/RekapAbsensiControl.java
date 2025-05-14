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

import org.bson.Document;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;

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

        btnCari.setOnAction(event -> {
            handleCari();
        });

        imCari.setOnMouseClicked(event -> {
            handleCari();
        });

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

        filterRekapBulanan();
        calculateAndUpdateDailyDuration(); // Hitung dan update durasi harian
        rekapBulanan(); // Proses otomatis  
        loadRekapData(); // Tampilkan ke tabel
    }

    /**
     * Metode baru untuk menghitung dan memperbarui durasi harian
     * dengan mengurangi waktu keluar dengan waktu masuk
     */
    private void calculateAndUpdateDailyDuration() {
        MongoCollection<Document> absensiCollection = database.getCollection("Absensi");
        
        // Mencari semua data absen dengan jenisAbsen "keluar" karena data tersebut sudah memiliki waktuKeluar
        for (Document doc : absensiCollection.find(Filters.eq("jenisAbsen", "keluar"))) {
            String idAbsensi = doc.getString("id_absensi");
            // Jika id_absensi tidak ada, gunakan _id sebagai alternatif
            if (idAbsensi == null) {
                Object objId = doc.getObjectId("_id");
                if (objId != null) {
                    idAbsensi = objId.toString();
                } else {
                    continue; // Tidak ada identifier yang dapat digunakan
                }
            }
            
            String waktuMasukStr = doc.getString("waktu");
            String waktuKeluarStr = doc.getString("waktuKeluar");
            
            // Skip jika waktu masuk atau waktu keluar tidak ada
            if (waktuMasukStr == null || waktuKeluarStr == null || waktuMasukStr.isEmpty() || waktuKeluarStr.isEmpty()) {
                continue;
            }
            
            try {
                // Konversi string waktu ke LocalTime dengan penanganan format yang berbeda
                LocalTime waktuMasuk;
                LocalTime waktuKeluar;
                
                // Mendeteksi format waktu dan menggunakan format yang sesuai (HH:mm:ss atau HH:mm)
                if (waktuMasukStr.matches("\\d{2}:\\d{2}:\\d{2}")) {
                    waktuMasuk = LocalTime.parse(waktuMasukStr, DateTimeFormatter.ofPattern("HH:mm:ss"));
                } else {
                    waktuMasuk = LocalTime.parse(waktuMasukStr, DateTimeFormatter.ofPattern("HH:mm"));
                }
                
                if (waktuKeluarStr.matches("\\d{2}:\\d{2}:\\d{2}")) {
                    waktuKeluar = LocalTime.parse(waktuKeluarStr, DateTimeFormatter.ofPattern("HH:mm:ss"));
                } else {
                    waktuKeluar = LocalTime.parse(waktuKeluarStr, DateTimeFormatter.ofPattern("HH:mm"));
                }
                
                // Hitung durasi dalam menit
                long durasiMenit;
                if (waktuKeluar.isBefore(waktuMasuk)) {
                    // Asumsi shift malam yang melewati tengah malam
                    // Tambahkan 24 jam (1440 menit) ke waktu keluar
                    durasiMenit = Duration.between(waktuMasuk, waktuKeluar.plusHours(24)).toMinutes();
                } else {
                    durasiMenit = Duration.between(waktuMasuk, waktuKeluar).toMinutes();
                }
                
                // Format durasi ke format jam:menit
                String durasiFormatted = formatDuration(durasiMenit);
                
                // Update dokumen dengan durasi yang dihitung
                Document update = new Document("$set", new Document("durasi", durasiFormatted)
                        .append("durasiMenit", durasiMenit));
                
                // Gunakan _id jika id_absensi tidak ada
                if (idAbsensi.contains("ObjectId")) {
                    absensiCollection.updateOne(Filters.eq("_id", doc.getObjectId("_id")), update);
                } else {
                    absensiCollection.updateOne(Filters.eq("id_absensi", idAbsensi), update);
                }
                
                System.out.println("Updated duration for " + (idAbsensi.contains("ObjectId") ? "_id" : "id_absensi") + 
                                  ": " + idAbsensi + 
                                  " - Masuk: " + waktuMasukStr + 
                                  " - Keluar: " + waktuKeluarStr + 
                                  " - Durasi: " + durasiFormatted + 
                                  " (" + durasiMenit + " menit)");
            } catch (Exception e) {
                System.out.println("Error calculating duration for ID: " + idAbsensi + 
                                  " - Masuk: " + waktuMasukStr + 
                                  " - Keluar: " + waktuKeluarStr + 
                                  " - Error: " + e.getMessage());
            }
        }
    }

    private void handleCari() {
        String keyword = txtCari.getText().toLowerCase();
        String selectedBulan = cbBulan.getValue();
        String selectedTahun = cbTahun.getValue();

        // Jika bulan dan tahun tidak dipilih, tampilkan semua data
        if (selectedBulan != null && selectedTahun != null) {
            // Ambil semua data rekap dari database
            ObservableList<RekapBulanan> allData = FXCollections.observableArrayList();
            MongoCollection<Document> rekapCollection = database.getCollection("rekapAbsensi");
            String bulanFinal = selectedTahun + "-" + selectedBulan;

            for (Document doc : rekapCollection.find(Filters.eq("bulan", bulanFinal))) {
                String nama = doc.getString("nama");
                int masuk = doc.getInteger("masuk", 0);
                int izin = doc.getInteger("izin", 0);
                int sakit = doc.getInteger("sakit", 0);
                int alfa = doc.getInteger("alfa", 0);
                String durasi = doc.getString("durasi");
                if (durasi == null) durasi = "00:00";

                allData.add(new RekapBulanan(nama, bulanFinal, masuk, izin, sakit, alfa, durasi));
            }

            // Filter data berdasarkan nama dan bulan/tahun yang dipilih
            ObservableList<RekapBulanan> filteredData = FXCollections.observableArrayList();

            for (RekapBulanan data : allData) {
                boolean matchesNama = data.getNama().toLowerCase().contains(keyword);
                boolean matchesBulanTahun = data.getBulan().equals(bulanFinal);

                if (matchesNama && matchesBulanTahun) {
                    filteredData.add(data);
                }
            }

            // Update tabel dengan data yang sudah difilter
            tblRekap.setItems(filteredData);
        }
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

        Map<String, String> namaToRfid = new HashMap<>();
        for (Document doc : karyawanCollection.find()) {
            String nama = doc.getString("nama");
            String rfid = doc.getString("rfid");
            namaToRfid.put(nama, rfid);
        }

        Map<String, int[]> rekapMap = new HashMap<>();
        Map<String, Map<String, Integer>> kehadiranPerTanggal = new HashMap<>();
        Map<String, Long> totalDurasiMap = new HashMap<>(); // Untuk menyimpan total durasi kerja dalam menit

        // Proses data kehadiran dari koleksi Absensi
        for (Document doc : absensiCollection.find()) {
            String tanggal = doc.getString("tanggal");
            if (tanggal == null || !tanggal.startsWith(bulanIni)) continue;

            String nama = doc.getString("nama");
            String status = doc.getString("jenisAbsen").toLowerCase();
            
            // Ambil durasi yang sudah dihitung
String waktuMasukStr = doc.getString("waktu");
String waktuKeluarStr = doc.getString("waktuKeluar");

Duration waktuMasuk = parseWaktu(waktuMasukStr);
Duration waktuKeluar = parseWaktu(waktuKeluarStr);

long durasiMenit = 0;
if (waktuMasuk != null && waktuKeluar != null) {
    if (waktuKeluar.compareTo(waktuMasuk) < 0) {
        // Shift malam, keluar di hari berikutnya
        durasiMenit = waktuKeluar.plusHours(24).minus(waktuMasuk).toMinutes();
    } else {
        durasiMenit = waktuKeluar.minus(waktuMasuk).toMinutes();
    }
}
doc.put("durasiMenit", durasiMenit);

         
            
            // Tambahkan durasi ke total
            if (durasiMenit > 0) {
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

        if (today.getDayOfMonth() == daysInMonth) {
            for (String nama : namaToRfid.keySet()) {
                String rfid = namaToRfid.get(nama);
                
                // Himpunan tanggal di mana karyawan hadir (setidaknya sekali)
                Set<String> hariHadir = new HashSet<>();
                if (kehadiranPerTanggal.containsKey(nama)) {
                    hariHadir.addAll(kehadiranPerTanggal.get(nama).keySet());
                }

                // Ambil semua shift berdasarkan rfid dan hari dari koleksi KelompokKerja
                Map<String, Integer> shiftPerHari = new HashMap<>(); // Menyimpan jumlah shift per hari
                Map<String, Boolean> liburPerHari = new HashMap<>(); // Menyimpan status libur per hari
                
                for (Document shiftDoc : kelompokKerjaCollection.find(Filters.eq("rfid", rfid))) {
                    String hari = shiftDoc.getString("hari").toLowerCase(); // e.g. "kamis"
                    String shift = shiftDoc.getString("shift");              // e.g. "libur"
                    
                    // Increment jumlah shift untuk hari tersebut
                    shiftPerHari.put(hari, shiftPerHari.getOrDefault(hari, 0) + 1);
                    
                    // Tandai jika ada shift libur pada hari tersebut
                    if (shift.equalsIgnoreCase("libur")) {
                        liburPerHari.put(hari, true);
                    }
                }

                int liburHari = 0;
                int totalShiftWajib = 0;
                
                for (String tanggal : semuaTanggal) {
                    LocalDate tgl = LocalDate.parse(tanggal);
                    String hariInggris = tgl.getDayOfWeek().name().toLowerCase(); // e.g. "thursday"
                    String hariIndonesia = getHariIndonesia(hariInggris);        // e.g. "kamis"
                    
                    // Jika hari ini adalah hari libur, tambahkan ke hitungan hari libur
                    if (liburPerHari.getOrDefault(hariIndonesia, false)) {
                        liburHari++;
                        continue; // Lanjut ke tanggal berikutnya
                    }
                    
                    // Hitung total shift wajib untuk hari ini
                    int shiftHariIni = shiftPerHari.getOrDefault(hariIndonesia, 1); // Default 1 shift jika tidak ada konfigurasi
                    totalShiftWajib += shiftHariIni;
                }
                
                // Hitung total kehadiran (accounting for multiple shifts in a day)
                int totalKehadiran = 0;
                if (kehadiranPerTanggal.containsKey(nama)) {
                    Map<String, Integer> kehadiranHarian = kehadiranPerTanggal.get(nama);
                    for (int count : kehadiranHarian.values()) {
                        totalKehadiran += count;
                    }
                }
                
                // Hitung alfa berdasarkan total shift wajib dikurangi total kehadiran
                int alfa = totalShiftWajib - totalKehadiran;
                if (alfa < 0) alfa = 0;

                System.out.println("Nama: " + nama);
                System.out.println("  Total hari dalam bulan: " + semuaTanggal.size());
                System.out.println("  Total hari hadir: " + hariHadir.size());
                System.out.println("  Total shift wajib: " + totalShiftWajib);
                System.out.println("  Total kehadiran: " + totalKehadiran);
                System.out.println("  Hari libur: " + liburHari);
                System.out.println("  Alfa dihitung: " + alfa);
                System.out.println("-----------------------------------");

                // Update rekap alfa
                rekapMap.putIfAbsent(nama, new int[]{0, 0, 0, 0});
                rekapMap.get(nama)[3] = alfa; // Set nilai alfa yang benar
            }
        }

        for (Map.Entry<String, int[]> entry : rekapMap.entrySet()) {
            String nama = entry.getKey();
            String rfid = namaToRfid.get(nama);
            int[] data = entry.getValue();

            if (rfid == null) continue;

            Document filter = new Document("rfid", rfid).append("bulan", bulanIni);
            
            // Format durasi ke format jam:menit
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

        // Dapatkan bulan dan tahun saat ini dalam format "yyyy-MM"
        LocalDate today = LocalDate.now();
        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("yyyy-MM");
        String currentMonth = today.format(monthFormatter);

        // Filter hanya data untuk bulan ini
        for (Document doc : rekapCollection.find()) {
            String bulan = doc.getString("bulan");

            if (bulan != null && bulan.equals(currentMonth)) {
                String nama = doc.getString("nama");
                int masuk = doc.getInteger("masuk", 0);
                int izin = doc.getInteger("izin", 0);
                int sakit = doc.getInteger("sakit", 0);
                int alfa = doc.getInteger("alfa", 0);
                String durasi = doc.getString("durasi");
                if (durasi == null) durasi = "00:00";

                data.add(new RekapBulanan(nama, bulan, masuk, izin, sakit, alfa, durasi));
            }
        }

        tblRekap.setItems(data);
    }

    private void filterRekapBulanan() {
        String bulan = cbBulan.getValue();
        String tahun = cbTahun.getValue();
        if (bulan == null || tahun == null) return;

        String bulanFinal = tahun + "-" + bulan; // contoh: "2025-05"

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

  private Duration parseWaktu(String waktu) {
    if (waktu == null || waktu.isEmpty()) {
        return Duration.ZERO;
    }

    String[] parts = waktu.split(":");
    int jam = Integer.parseInt(parts[0]);
    int menit = Integer.parseInt(parts[1]);
    return Duration.ofHours(jam).plusMinutes(menit);
}


}