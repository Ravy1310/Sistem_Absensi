package com.example.absensi;

import com.example.absensi.model.Absensi;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.*;
import java.time.LocalDate;
import java.time.Month;
import java.util.*;

public class RekapAbsensiController {

    @FXML
    private TableView<Absensi> tableAbsensi;
    @FXML
    private TableColumn<Absensi, String> colNama;
    @FXML
    private TableColumn<Absensi, String> colHari;
    @FXML
    private TableColumn<Absensi, LocalDate> colTanggal;
    @FXML
    private TableColumn<Absensi, String> colMasuk;
    @FXML
    private TableColumn<Absensi, String> colPulang;

    @FXML
    private ComboBox<String> comboBulan;
    @FXML
    private ComboBox<Integer> comboTahun;

    private final ObservableList<Absensi> dataAbsensi = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colNama.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getNama()));
        colHari.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getHari()));
        colTanggal.setCellValueFactory(cell -> new javafx.beans.property.SimpleObjectProperty<>(cell.getValue().getTanggal()));
        colMasuk.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getJamMasuk()));
        colPulang.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getJamPulang()));

        // Isi Combo Bulan statis
        comboBulan.setItems(FXCollections.observableArrayList(
            "Januari", "Februari", "Maret", "April", "Mei", "Juni",
            "Juli", "Agustus", "September", "Oktober", "November", "Desember"
        ));

        
        loadDataAbsensiDariDatabase();
        isiComboTahunDariDatabase();

       
        LocalDate today = LocalDate.now();
        comboBulan.setValue(convertMonthToString(today.getMonth()));
        comboTahun.setValue(today.getYear());

       
        tableAbsensi.setItems(dataAbsensi);
    }

    private void loadDataAbsensiDariDatabase() {
        dataAbsensi.clear();
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM absensi")) {

            while (rs.next()) {
                String nama = rs.getString("nama");
                String hari = rs.getString("hari");
                LocalDate tanggal = rs.getDate("tanggal").toLocalDate();
                String jamMasuk = rs.getString("jam_masuk");
                String jamPulang = rs.getString("jam_pulang");

                dataAbsensi.add(new Absensi(nama, hari, tanggal, jamMasuk, jamPulang));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void isiComboTahunDariDatabase() {
        Set<Integer> tahunUnik = new TreeSet<>();
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT DISTINCT YEAR(tanggal) as tahun FROM absensi")) {

            while (rs.next()) {
                tahunUnik.add(rs.getInt("tahun"));
            }

            comboTahun.setItems(FXCollections.observableArrayList(tahunUnik));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onFilterClicked() {
        String selectedBulan = comboBulan.getValue();
        Integer selectedTahun = comboTahun.getValue();

        if (selectedBulan == null || selectedTahun == null) return;

        int bulanIndex = comboBulan.getItems().indexOf(selectedBulan) + 1;

        ObservableList<Absensi> filtered = FXCollections.observableArrayList();
        for (Absensi abs : dataAbsensi) {
            if (abs.getTanggal().getMonthValue() == bulanIndex && abs.getTanggal().getYear() == selectedTahun) {
                filtered.add(abs);
            }
        }

        tableAbsensi.setItems(filtered);
    }

    private String convertMonthToString(Month month) {
        switch (month) {
            case JANUARY: return "Januari";
            case FEBRUARY: return "Februari";
            case MARCH: return "Maret";
            case APRIL: return "April";
            case MAY: return "Mei";
            case JUNE: return "Juni";
            case JULY: return "Juli";
            case AUGUST: return "Agustus";
            case SEPTEMBER: return "September";
            case OCTOBER: return "Oktober";
            case NOVEMBER: return "November";
            case DECEMBER: return "Desember";
            default: return "";
        }
    }
}
