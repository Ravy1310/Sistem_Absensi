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
        MongoCollection<Document> rekapCollection = database.getCollection("rekapAbsensi");
        MongoCollection<Document> absenCollection = database.getCollection("Absensi");

        int hadir = 0, izin = 0, sakit = 0, alpha = 0;
        int tepatWaktu = 0, terlambat = 0;

        Document rekapQuery = new Document("nama",
            new Document("$regex", "^" + Pattern.quote(namaKaryawan.trim()) + "$")
                .append("$options", "i"))
            .append("bulan", selectedBulan);

        try (MongoCursor<Document> cursor = rekapCollection.find(rekapQuery).iterator()) {
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                hadir += doc.getInteger("masuk", 0);
                izin += doc.getInteger("izin", 0);
                sakit += doc.getInteger("sakit", 0);
                alpha += doc.getInteger("alfa", 0);
            }
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        LocalDate selectedDate;
        try {
            selectedDate = LocalDate.parse(selectedBulan + "-01", formatter);
        } catch (Exception e) {
            System.err.println("Format selectedBulan tidak valid: " + selectedBulan);
            return;
        }

        try (MongoCursor<Document> cursor = absenCollection.find(
            new Document("nama", new Document("$regex", "^" + Pattern.quote(namaKaryawan.trim()) + "$")
                .append("$options", "i"))
        ).iterator()) {
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                String tanggalStr = doc.getString("tanggal");
                String status = doc.getString("status");

                if (tanggalStr != null && status != null) {
                    try {
                        LocalDate tanggal = LocalDate.parse(tanggalStr, formatter);

                        if (tanggal.getMonth() == selectedDate.getMonth() &&
                            tanggal.getYear() == selectedDate.getYear()) {
                            
                            status = status.trim().toLowerCase();
                            if (status.equals("tepat waktu")) {
                                tepatWaktu++;
                            } else if (status.equals("terlambat")) {
                                terlambat++;
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("Format tanggal tidak valid: " + tanggalStr);
                    }
                }
            }
        }

        ObservableList<PieChart.Data> chartData = FXCollections.observableArrayList();

        if (hadir + izin + sakit + alpha + tepatWaktu + terlambat == 0) {
            chartData.add(new PieChart.Data("Tidak Ada Data", 1));
        } else {
            if (izin > 0) chartData.add(new PieChart.Data("Izin", izin));
            if (sakit > 0) chartData.add(new PieChart.Data("Sakit", sakit));
            if (alpha > 0) chartData.add(new PieChart.Data("Alpha", alpha));
            if (tepatWaktu > 0) chartData.add(new PieChart.Data("Hadir (Tepat Waktu)", tepatWaktu));
            if (terlambat > 0) chartData.add(new PieChart.Data("Hadir (Terlambat)", terlambat));
        }

        pieChart.setData(chartData);
        pieChart.setTitle("Statistik: " + namaKaryawan + " (" + selectedBulan + ")");
        pieChart.setLabelsVisible(true);
        pieChart.setLegendVisible(true);

        // Atur warna slice PieChart langsung lewat kode Java:
        String[] colors = {
             "#e74c3c", // Alpha - merah
            "#3498db", // Izin - biru
            "#4CAF50", // Hadir (Tepat Waktu) - hijau toska
             "#9b59b6", // Sakit - ungu
            "#e67e22"  // Hadir (Terlambat) - oranye
        };

        for (int i = 0; i < chartData.size(); i++) {
            PieChart.Data data = chartData.get(i);
            String color = colors[i % colors.length];
            data.getNode().setStyle("-fx-pie-color: " + color + ";");
        }

        System.out.println("Hasil Rekap:");
        System.out.println("Hadir: " + hadir + ", Izin: " + izin + ", Sakit: " + sakit + ", Alpha: " + alpha);
        System.out.println("Tepat Waktu: " + tepatWaktu + ", Terlambat: " + terlambat);
    }

    private void resetChart() {
        pieChart.getData().clear();
        pieChart.setTitle("");
        pieChart.setLabelsVisible(false);
        pieChart.setLegendVisible(false);
        System.out.println("Chart di-reset.");
    }
}
