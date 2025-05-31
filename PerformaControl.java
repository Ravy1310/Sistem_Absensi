package com.example.demo;

import java.util.HashSet;
import java.util.Set;

import org.bson.Document;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.chart.PieChart;


public class PerformaControl {

     @FXML
    private ComboBox<String> comboNama;

    @FXML
    private PieChart pieChart;

    private MongoDatabase database;
    public PerformaControl() {
        database = MongoDBConnection.getDatabase();
    }
    @FXML 
    private void initialize() {
        loadNamaKaryawan();

        comboNama.setOnAction(event -> {
            String selectedNama = comboNama.getValue();
            if (selectedNama != null && !selectedNama.isEmpty()) {
                loadChartData(selectedNama);
            }
        });
    }
     private void loadNamaKaryawan() {
        MongoCollection<Document> collection = database.getCollection("rekap_absensi");
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

    private void loadChartData(String namaKaryawan) {
        MongoCollection<Document> collection = database.getCollection("rekap_absensi");

        int hadir = 0, izin = 0, sakit = 0, alpha = 0;

        try (MongoCursor<Document> cursor = collection.find(new Document("nama", namaKaryawan)).iterator()) {
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                String status = doc.getString("status");
                if (status == null) continue;

                switch (status.toLowerCase()) {
                    case "hadir" -> hadir++;
                    case "izin" -> izin++;
                    case "sakit" -> sakit++;
                    case "alpha", "alpa", "absen" -> alpha++;
                }
            }
        }

        ObservableList<PieChart.Data> chartData = FXCollections.observableArrayList(
            new PieChart.Data("Hadir", hadir),
            new PieChart.Data("Izin", izin),
            new PieChart.Data("Sakit", sakit),
            new PieChart.Data("Alpha", alpha)
        );

        pieChart.setData(chartData);
        pieChart.setTitle("Statistik Kehadiran: " + namaKaryawan);
    }
}
    