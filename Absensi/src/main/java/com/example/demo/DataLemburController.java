package com.example.demo;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

import org.bson.Document;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class DataLemburController {

    @FXML private TableView<KaryawanLembur> tabelLembur;
    @FXML private TableColumn<KaryawanLembur, String> colNo;
    @FXML private TableColumn<KaryawanLembur, String> colNama;
    @FXML private TableColumn<KaryawanLembur, String> colLembur;
    @FXML private TableColumn<KaryawanLembur, String> colGaji;
    @FXML private ComboBox<String> cbBulan;
    @FXML private ComboBox<String> cbTahun;

    private MongoDatabase database;

    public DataLemburController() {
        database = MongoDBConnection.getDatabase();
    }

    @FXML
    private void initialize() {
        // Inisialisasi kolom no otomatis
        colNo.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : String.valueOf(getIndex() + 1));
            }
        });

        colNama.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getNama()));
        colLembur.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getTotalJam() + " jam"));
        colGaji.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty("Rp" + data.getValue().getGajiLembur()));

        // Isi ComboBox Bulan (1-12)
        for (int i = 1; i <= 12; i++) {
            cbBulan.getItems().add(String.valueOf(i));
        }
        // Isi ComboBox Tahun (misal 2023-2025)
        int tahunSekarang = LocalDate.now().getYear();
        for (int t = tahunSekarang - 2; t <= tahunSekarang + 1; t++) {
            cbTahun.getItems().add(String.valueOf(t));
        }

        // Set default bulan dan tahun ke bulan & tahun sekarang
        cbBulan.setValue(String.valueOf(LocalDate.now().getMonthValue()));
        cbTahun.setValue(String.valueOf(tahunSekarang));

        // Listener comboBox supaya load data saat bulan/tahun berubah
        cbBulan.setOnAction(e -> loadDataCombo());
        cbTahun.setOnAction(e -> loadDataCombo());

        // Load data awal
        loadDataCombo();
    }

    private void loadDataCombo() {
        try {
            int bulan = Integer.parseInt(cbBulan.getValue());
            int tahun = Integer.parseInt(cbTahun.getValue());
            loadLemburData(bulan, tahun);
        } catch (NumberFormatException e) {
            tabelLembur.setItems(FXCollections.observableArrayList());
            tabelLembur.setPlaceholder(new Label("Pilih bulan dan tahun dengan benar."));
        }
    }

    private boolean isAkhirBulan() {
        LocalDate today = LocalDate.now();
        return today.equals(today.withDayOfMonth(today.lengthOfMonth()));
    }

    private void loadLemburData(int bulan, int tahun) {
        // Jika bulan & tahun sekarang tapi bukan akhir bulan
        if (bulan == LocalDate.now().getMonthValue() && tahun == LocalDate.now().getYear() && !isAkhirBulan()) {
            tabelLembur.setItems(FXCollections.observableArrayList());
            tabelLembur.setPlaceholder(new Label("Data lembur akan ditampilkan di akhir bulan."));
            return;
        }

        // Jika pilih bulan/tahun selain bulan sekarang, load data dari rekapLembur
        if (bulan != LocalDate.now().getMonthValue() || tahun != LocalDate.now().getYear()) {
            loadRekapDariDatabase(bulan, tahun);
            return;
        }

        // Jika bulan & tahun sekarang dan hari ini akhir bulan, load data PengajuanLembur dan simpan ke rekap
        MongoCollection<Document> lemburCollection = database.getCollection("PengajuanLembur");
        Map<String, KaryawanLembur> dataMap = new HashMap<>();

        for (Document doc : lemburCollection.find()) {
            String nama = doc.getString("nama");
            String rfid = doc.getString("rfid");
            String durasi = doc.getString("durasi");
            String tanggalStr = doc.getString("tanggal");

            if (tanggalStr == null) continue;

            LocalDate tanggal;
            try {
                tanggal = LocalDate.parse(tanggalStr);
            } catch (DateTimeParseException e) {
                continue;
            }

            if (tanggal.getMonthValue() != bulan || tanggal.getYear() != tahun) continue;

            String key = nama + "|" + rfid;
            KaryawanLembur existing = dataMap.get(key);

            if (existing != null) {
                int total = existing.getTotalJam() + Integer.parseInt(durasi);
                int gaji = total * 50000;
                dataMap.put(key, new KaryawanLembur(nama, rfid, total, gaji));
            } else {
                int durasiInt = Integer.parseInt(durasi);
                dataMap.put(key, new KaryawanLembur(nama, rfid, durasiInt, durasiInt * 50000));
            }
        }

        ObservableList<KaryawanLembur> lemburList = FXCollections.observableArrayList(dataMap.values());
        tabelLembur.setItems(lemburList);

        // Simpan ke rekapLembur hanya jika hari ini akhir bulan
        if (isAkhirBulan()) {
            simpanRekapKeDatabase(dataMap, bulan, tahun);
        }
    }

    private void loadRekapDariDatabase(int bulan, int tahun) {
        MongoCollection<Document> rekapCollection = database.getCollection("RekapLembur");
        ObservableList<KaryawanLembur> list = FXCollections.observableArrayList();

        for (Document doc : rekapCollection.find(Filters.and(
                Filters.eq("bulan", bulan),
                Filters.eq("tahun", tahun)
        ))) {
            String nama = doc.getString("nama");
            String rfid = doc.getString("rfid");
            int totalJam = doc.getInteger("totalJam", 0);
            int totalGaji = doc.getInteger("totalGaji", 0);

            list.add(new KaryawanLembur(nama, rfid, totalJam, totalGaji));
        }

        tabelLembur.setItems(list);

        if (list.isEmpty()) {
            tabelLembur.setPlaceholder(new Label("Tidak ada data lembur untuk bulan dan tahun ini."));
        }
    }

    private void simpanRekapKeDatabase(Map<String, KaryawanLembur> dataMap, int bulan, int tahun) {
        MongoCollection<Document> rekapCollection = database.getCollection("RekapLembur");

        for (KaryawanLembur data : dataMap.values()) {
            String nama = data.getNama();
            String rfid = data.getRfid();
            int totalJam = data.getTotalJam();
            int totalGaji = data.getGajiLembur();

            Document existing = rekapCollection.find(Filters.and(
                    Filters.eq("nama", nama),
                    Filters.eq("rfid", rfid),
                    Filters.eq("bulan", bulan),
                    Filters.eq("tahun", tahun)
            )).first();

            if (existing != null) {
                rekapCollection.updateOne(
                        Filters.and(
                                Filters.eq("nama", nama),
                                Filters.eq("rfid", rfid),
                                Filters.eq("bulan", bulan),
                                Filters.eq("tahun", tahun)
                        ),
                        new Document("$set", new Document("totalJam", totalJam).append("totalGaji", totalGaji))
                );
            } else {
                Document newDoc = new Document("nama", nama)
                        .append("rfid", rfid)
                        .append("totalJam", totalJam)
                        .append("totalGaji", totalGaji)
                        .append("bulan", bulan)
                        .append("tahun", tahun);

                rekapCollection.insertOne(newDoc);
            }
        }
    }
}
