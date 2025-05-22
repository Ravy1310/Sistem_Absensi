package com.example.demo;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.bson.Document;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.ComboBox;

public class performaControl {

    @FXML
    private ComboBox<String> comboNama;

    @FXML
    private ComboBox<String> comboBulan;

    @FXML
    private PieChart pieChart;

    private MongoDatabase database;

    public performaControl() {
        database = MongoDBConnection.getDatabase();
    }

    @FXML
    private void initialize() {
        loadNamaKaryawan();
        loadBulan();

        comboNama.setOnAction(event -> triggerLoadChart());
        comboBulan.setOnAction(event -> triggerLoadChart());
    }

    private void triggerLoadChart() {
        String selectedNama = comboNama.getValue();
        String selectedBulan = comboBulan.getValue();
        if (selectedNama != null && !selectedNama.isEmpty() &&
            selectedBulan != null && !selectedBulan.isEmpty()) {
            resetChart();
            loadChartData(selectedNama, selectedBulan);
        }
    }

    private void loadNamaKaryawan() {
        MongoCollection<Document> collection = database.getCollection("rekapAbsensi");
        Set<String> namaSet = new HashSet<>();

        try (MongoCursor<Document> cursor = collection.find().iterator()) {
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                String nama = doc.getString("nama");
                if (nama != null && !nama.isEmpty()) {
                    namaSet.add(nama);
                }
            }
        }

        comboNama.setItems(FXCollections.observableArrayList(namaSet));
    }

    private void loadBulan() {
        MongoCollection<Document> collection = database.getCollection("rekapAbsensi");
        Set<String> bulanSet = new HashSet<>();

        try (MongoCursor<Document> cursor = collection.find().iterator()) {
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                String bulan = doc.getString("bulan"); // format "yyyy-MM"
                if (bulan != null && !bulan.isEmpty()) {
                    bulanSet.add(bulan);
                }
            }
        }

        comboBulan.setItems(FXCollections.observableArrayList(bulanSet));
    }

    private void loadChartData(String namaKaryawan, String selectedBulan) {
    System.out.println("Loading chart for: " + namaKaryawan + " - " + selectedBulan);
    
    MongoCollection<Document> rekapCollection = database.getCollection("rekapAbsensi");
    MongoCollection<Document> absenCollection = database.getCollection("Absensi");

    int hadir = 0, izin = 0, sakit = 0, alpha = 0;
    int tepatWaktu = 0, terlambat = 0;

    // Query untuk rekapAbsensi
    Document rekapQuery = new Document("nama",
        new Document("$regex", "^" + Pattern.quote(namaKaryawan.trim()) + "$")
            .append("$options", "i"))
        .append("bulan", selectedBulan);

    System.out.println("Rekap Query: " + rekapQuery.toJson());

    // Ambil data dari rekapAbsensi
    try (MongoCursor<Document> cursor = rekapCollection.find(rekapQuery).iterator()) {
        boolean foundRekapData = false;
        while (cursor.hasNext()) {
            foundRekapData = true;
            Document doc = cursor.next();
            System.out.println("Rekap Document: " + doc.toJson());
            
            hadir += doc.getInteger("masuk", 0);
            izin += doc.getInteger("izin", 0);
            sakit += doc.getInteger("sakit", 0);
            alpha += doc.getInteger("alfa", 0);
        }
        
        if (!foundRekapData) {
            System.out.println("Tidak ada data rekap ditemukan untuk: " + namaKaryawan + " - " + selectedBulan);
        }
    } catch (Exception e) {
        System.err.println("Error querying rekapAbsensi: " + e.getMessage());
    }

    // Parse tanggal untuk filter absensi
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    LocalDate selectedDate;
    try {
        selectedDate = LocalDate.parse(selectedBulan + "-01", formatter);
    } catch (Exception e) {
        System.err.println("Format selectedBulan tidak valid: " + selectedBulan);
        return;
    }

    // Query untuk Absensi
    Document absenQuery = new Document("nama", 
        new Document("$regex", "^" + Pattern.quote(namaKaryawan.trim()) + "$")
            .append("$options", "i"));
    
    System.out.println("Absen Query: " + absenQuery.toJson());

    // Ambil data dari Absensi
    try (MongoCursor<Document> cursor = absenCollection.find(absenQuery).iterator()) {
        boolean foundAbsenData = false;
        while (cursor.hasNext()) {
            Document doc = cursor.next();
            String tanggalStr = doc.getString("tanggal");
            String status = doc.getString("status");

            if (tanggalStr != null && status != null) {
                try {
                    LocalDate tanggal = LocalDate.parse(tanggalStr, formatter);

                    if (tanggal.getMonth() == selectedDate.getMonth() &&
                        tanggal.getYear() == selectedDate.getYear()) {
                        
                        foundAbsenData = true;
                        status = status.trim().toLowerCase();
                        
                        if (status.equals("tepat waktu")) {
                            tepatWaktu++;
                        } else if (status.equals("terlambat")) {
                            terlambat++;
                        }
                        
                        System.out.println("Processed: " + tanggalStr + " - " + status);
                    }
                } catch (Exception e) {
                    System.err.println("Format tanggal tidak valid: " + tanggalStr);
                }
            }
        }
        
        if (!foundAbsenData) {
            System.out.println("Tidak ada data absensi ditemukan untuk bulan: " + selectedBulan);
        }
    } catch (Exception e) {
        System.err.println("Error querying Absensi: " + e.getMessage());
    }

    // Debug output
    System.out.println("Final counts - Hadir: " + hadir + ", Izin: " + izin + 
                      ", Sakit: " + sakit + ", Alpha: " + alpha + 
                      ", Tepat Waktu: " + tepatWaktu + ", Terlambat: " + terlambat);

    // Buat data chart
    ObservableList<PieChart.Data> chartData = FXCollections.observableArrayList();

    // Gunakan total hadir dari rekap, bukan dari detail absensi
    int totalData = hadir + izin + sakit + alpha;
    
    if (totalData == 0) {
        System.out.println("Tidak ada data untuk ditampilkan");
        chartData.add(new PieChart.Data("Tidak Ada Data", 1));
    } else {
        // Prioritaskan data dari rekapAbsensi
        if (hadir > 0) {
            // Jika ada breakdown tepat waktu/terlambat, gunakan itu
            if (tepatWaktu + terlambat > 0) {
                if (tepatWaktu > 0) chartData.add(new PieChart.Data("Hadir (Tepat Waktu)", tepatWaktu));
                if (terlambat > 0) chartData.add(new PieChart.Data("Hadir (Terlambat)", terlambat));
                
                // Jika masih ada sisa hadir yang tidak tercatat detail
                int sisaHadir = hadir - (tepatWaktu + terlambat);
                if (sisaHadir > 0) {
                    chartData.add(new PieChart.Data("Hadir (Lainnya)", sisaHadir));
                }
            } else {
                // Jika tidak ada breakdown, tampilkan total hadir
                chartData.add(new PieChart.Data("Hadir", hadir));
            }
        }
        
        if (izin > 0) chartData.add(new PieChart.Data("Izin", izin));
        if (sakit > 0) chartData.add(new PieChart.Data("Sakit", sakit));
        if (alpha > 0) chartData.add(new PieChart.Data("Alpha", alpha));
    }

    System.out.println("Chart data size: " + chartData.size());

    // Update chart
    Platform.runLater(() -> {
        pieChart.setData(chartData);
        pieChart.setTitle("Statistik: " + namaKaryawan + " (" + selectedBulan + ")");
        pieChart.setLabelsVisible(true);
        pieChart.setLegendVisible(true);

        // Atur warna
        if (!chartData.isEmpty() && !chartData.get(0).getName().equals("Tidak Ada Data")) {
            String[] colors = {
                "#4CAF50", // Hadir - hijau
                "#e67e22", // Hadir (Terlambat) - oranye  
                "#3498db", // Izin - biru
                "#9b59b6", // Sakit - ungu
                "#e74c3c", // Alpha - merah
                "#95a5a6"  // Lainnya - abu
            };

            for (int i = 0; i < chartData.size() && i < colors.length; i++) {
                PieChart.Data data = chartData.get(i);
                if (data.getNode() != null) {
                    data.getNode().setStyle("-fx-pie-color: " + colors[i] + ";");
                }
            }
        }
    });

    System.out.println("Chart loading completed");
}

    private void resetChart() {
        pieChart.getData().clear();
        pieChart.setTitle("");
        pieChart.setLabelsVisible(false);
        pieChart.setLegendVisible(false);
        System.out.println("Chart di-reset.");
    }
}
