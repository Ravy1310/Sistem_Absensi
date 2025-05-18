package com.example.demo;

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
                String bulan = doc.getString("bulan"); // Pastikan field-nya "bulan"
                if (bulan != null && !bulan.isEmpty()) {
                    bulanSet.add(bulan);
                }
            }
        }

        comboBulan.setItems(FXCollections.observableArrayList(bulanSet));
    }

    private void loadChartData(String namaKaryawan, String bulan) {
        MongoCollection<Document> rekapCollection = database.getCollection("rekapAbsensi");
        MongoCollection<Document> absenCollection = database.getCollection("absensi");

        int hadir = 0, izin = 0, sakit = 0, alpha = 0;
        int tepatWaktu = 0, terlambat = 0;

        Document namaBulanQuery = new Document("nama",
            new Document("$regex", "^" + Pattern.quote(namaKaryawan.trim()) + "$")
                .append("$options", "i"))
            .append("bulan", bulan); // Filter berdasarkan bulan

        // Data kehadiran
        try (MongoCursor<Document> cursor = rekapCollection.find(namaBulanQuery).iterator()) {
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                hadir += doc.getInteger("masuk", 0);
                izin += doc.getInteger("izin", 0);
                sakit += doc.getInteger("sakit", 0);
                alpha += doc.getInteger("alfa", 0);
            }
        }

        // Data ketepatan waktu
        try (MongoCursor<Document> cursor = absenCollection.find(namaBulanQuery).iterator()) {
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                String status = doc.getString("statusAbsen");
                if (status != null) {
                    status = status.trim().toLowerCase();
                    if (status.equals("tepat waktu")) {
                        tepatWaktu++;
                    } else if (status.equals("terlambat")) {
                        terlambat++;
                    }
                }
            }
        }

        ObservableList<PieChart.Data> chartData = FXCollections.observableArrayList();

        if (hadir + izin + sakit + alpha + tepatWaktu + terlambat == 0) {
            chartData.add(new PieChart.Data("Tidak Ada Data", 1));
        } else {
            if (hadir > 0) chartData.add(new PieChart.Data("Hadir", hadir));
            if (izin > 0) chartData.add(new PieChart.Data("Izin", izin));
            if (sakit > 0) chartData.add(new PieChart.Data("Sakit", sakit));
            if (alpha > 0) chartData.add(new PieChart.Data("Alpha", alpha));
            if (tepatWaktu > 0) chartData.add(new PieChart.Data("Tepat Waktu", tepatWaktu));
            if (terlambat > 0) chartData.add(new PieChart.Data("Terlambat", terlambat));
        }

        pieChart.setData(chartData);
        pieChart.setTitle("Statistik: " + namaKaryawan + " (" + bulan + ")");
        pieChart.setLabelsVisible(true);
        pieChart.setLegendVisible(true);

        System.out.println("Data untuk " + namaKaryawan + " bulan " + bulan);
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
