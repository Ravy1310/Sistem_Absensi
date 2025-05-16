package com.example.demo;

import java.time.LocalDate;
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
    @FXML private TableColumn<RekapBulanan, String> colAlfa;

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
        rekapBulanan(); // Proses otomatis
        loadRekapData(); // Tampilkan ke tabel
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

            allData.add(new RekapBulanan(nama, bulanFinal, masuk, izin, sakit, alfa));
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

        LocalDate today = LocalDate.now();
        String bulanIni = today.format(DateTimeFormatter.ofPattern("yyyy-MM"));

        LocalDate firstDay = today.withDayOfMonth(1);
        int daysInMonth = firstDay.lengthOfMonth();
        List<String> semuaTanggal = new ArrayList<>();
        for (int i = 1; i <= daysInMonth; i++) {
            semuaTanggal.add(firstDay.withDayOfMonth(i).toString());
        }

        // Simpan nama dan rfid
        Map<String, String> namaToRfid = new HashMap<>();
        for (Document doc : karyawanCollection.find()) {
            String nama = doc.getString("nama");
            String rfid = doc.getString("rfid");
            namaToRfid.put(nama, rfid);
        }

        Map<String, int[]> rekapMap = new HashMap<>();
        Map<String, Set<String>> tanggalTerisi = new HashMap<>();

        for (Document doc : absensiCollection.find()) {
            String tanggal = doc.getString("tanggal");
            if (tanggal == null || !tanggal.startsWith(bulanIni)) continue;

            String nama = doc.getString("nama");
            String status = doc.getString("jenisAbsen").toLowerCase();

            int[] data = rekapMap.getOrDefault(nama, new int[]{0, 0, 0, 0});
            switch (status) {
                case "keluar" -> data[0]++;
                case "izin" -> data[1]++;
                case "sakit" -> data[2]++;
                case "alfa" -> data[3]++;
            }
            rekapMap.put(nama, data);
            tanggalTerisi.computeIfAbsent(nama, k -> new HashSet<>()).add(tanggal);
        }

        // Hanya hitung alfa otomatis jika hari ini adalah hari terakhir di bulan
        if (today.getDayOfMonth() == daysInMonth) {
            for (String nama : namaToRfid.keySet()) {
                Set<String> hadir = tanggalTerisi.getOrDefault(nama, new HashSet<>());
                long kosong = semuaTanggal.stream().filter(tgl -> !hadir.contains(tgl)).count();
                rekapMap.putIfAbsent(nama, new int[]{0, 0, 0, 0});
                rekapMap.get(nama)[3] += (int) kosong;
            }
        }

        for (Map.Entry<String, int[]> entry : rekapMap.entrySet()) {
            String nama = entry.getKey();
            String rfid = namaToRfid.get(nama);
            int[] data = entry.getValue();

            if (rfid == null) continue; // skip jika rfid tidak ditemukan

            Document filter = new Document("rfid", rfid).append("bulan", bulanIni);
            Document update = new Document("$set", new Document("rfid", rfid)
                    .append("nama", nama)
                    .append("bulan", bulanIni)
                    .append("masuk", data[0])
                    .append("izin", data[1])
                    .append("sakit", data[2])
                    .append("alfa", data[3])
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

            data.add(new RekapBulanan(nama, bulan, masuk, izin, sakit, alfa));
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

        data.add(new RekapBulanan(nama, bulanFinal, masuk, izin, sakit, alfa));
    }

    tblRekap.setItems(data);
}

}
