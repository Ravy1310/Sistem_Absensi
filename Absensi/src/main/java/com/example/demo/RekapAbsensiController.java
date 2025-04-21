package com.example.demo;

import com.example.absensi.model.Absensi;
import com.mongodb.client.*;
import org.bson.Document;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalDate;
import java.time.Month;
import java.util.*;

public class RekapAbsensiController {

    @FXML private TableView<Absensi> tableAbsensi;
    @FXML private TableColumn<Absensi, String> colNama;
    @FXML private TableColumn<Absensi, String> colHari;
    @FXML private TableColumn<Absensi, LocalDate> colTanggal;
    @FXML private TableColumn<Absensi, String> colMasuk;
    @FXML private TableColumn<Absensi, String> colPulang;

    @FXML private MenuButton menuBulan;
    @FXML private MenuButton menuTahun;

    private final ObservableList<Absensi> dataAbsensi = FXCollections.observableArrayList();
    private Integer tahunDipilih = null;
    private Integer bulanDipilih = null;

    @FXML
    public void initialize() {
        colNama.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getNama()));
        colHari.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getHari()));
        colTanggal.setCellValueFactory(cell -> new javafx.beans.property.SimpleObjectProperty<>(cell.getValue().getTanggal()));
        colMasuk.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getJamMasuk()));
        colPulang.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getJamPulang()));

        // Ambil data dulu baru isi tahun
        loadDataAbsensiDariMongo();
        isiMenuBulan();
        isiMenuTahunDariMongo();

        // Set default bulan dan tahun
        LocalDate today = LocalDate.now();
        bulanDipilih = today.getMonthValue();
        tahunDipilih = today.getYear();
        menuBulan.setText(convertMonthToString(today.getMonth()));
        menuTahun.setText(String.valueOf(tahunDipilih));

        // Filter langsung agar data tampil
        onFilterClicked();
    }

    private void loadDataAbsensiDariMongo() {
        dataAbsensi.clear();
        MongoCollection<Document> collection = Database.getDatabase().getCollection("RekapAbsensi");

        FindIterable<Document> docs = collection.find();
        System.out.println("Dokumen ditemukan: " + collection.countDocuments());

        for (Document doc : docs) {
            System.out.println("Dokumen: " + doc.toJson());

            String nama = doc.getString("nama");
            String hari = doc.getString("hari");
            LocalDate tanggal = LocalDate.parse(doc.getString("tanggal"));
            String jamMasuk = doc.getString("jam_masuk");
            String jamPulang = doc.getString("jam_pulang");

            dataAbsensi.add(new Absensi(nama, hari, tanggal, jamMasuk, jamPulang));
        }
    }

    private void isiMenuBulan() {
        menuBulan.getItems().clear();
        String[] bulanIndo = {
            "Januari", "Februari", "Maret", "April", "Mei", "Juni",
            "Juli", "Agustus", "September", "Oktober", "November", "Desember"
        };
        for (int i = 0; i < bulanIndo.length; i++) {
            final int bulanIndex = i + 1;
            MenuItem item = new MenuItem(bulanIndo[i]);
            item.setOnAction(e -> {
                bulanDipilih = bulanIndex;
                menuBulan.setText(bulanIndo[i]);
                onFilterClicked(); // tampilkan ulang saat ganti bulan
            });
            menuBulan.getItems().add(item);
        }
    }

    private void isiMenuTahunDariMongo() {
        Set<Integer> tahunUnik = new TreeSet<>();
        for (Absensi abs : dataAbsensi) {
            tahunUnik.add(abs.getTanggal().getYear());
        }

        menuTahun.getItems().clear();
        for (Integer tahun : tahunUnik) {
            MenuItem item = new MenuItem(String.valueOf(tahun));
            item.setOnAction(e -> {
                tahunDipilih = tahun;
                menuTahun.setText(String.valueOf(tahun));
                onFilterClicked(); // tampilkan ulang saat ganti tahun
            });
            menuTahun.getItems().add(item);
        }
    }

    @FXML
    private void onFilterClicked() {
        if (bulanDipilih == null || tahunDipilih == null) return;

        ObservableList<Absensi> filtered = FXCollections.observableArrayList();
        for (Absensi abs : dataAbsensi) {
            if (abs.getTanggal().getMonthValue() == bulanDipilih &&
                abs.getTanggal().getYear() == tahunDipilih) {
                filtered.add(abs);
            }
        }

        tableAbsensi.setItems(filtered);
    }

    private String convertMonthToString(Month month) {
        return switch (month) {
            case JANUARY -> "Januari";
            case FEBRUARY -> "Februari";
            case MARCH -> "Maret";
            case APRIL -> "April";
            case MAY -> "Mei";
            case JUNE -> "Juni";
            case JULY -> "Juli";
            case AUGUST -> "Agustus";
            case SEPTEMBER -> "September";
            case OCTOBER -> "Oktober";
            case NOVEMBER -> "November";
            case DECEMBER -> "Desember";
        };
    }
}
