package com.example.absensi;

import com.example.absensi.model.Absensi;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalDate;

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

    private final ObservableList<Absensi> dataAbsensi = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Binding kolom
        colNama.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getNama()));
        colHari.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getHari()));
        colTanggal.setCellValueFactory(cell -> new javafx.beans.property.SimpleObjectProperty<>(cell.getValue().getTanggal()));
        colMasuk.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getJamMasuk()));
        colPulang.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getJamPulang()));

        // Data dummy
        dataAbsensi.addAll(
                new Absensi("Bryan", "Senin", LocalDate.of(2025, 4, 1), "08:00", "17:00"),
                new Absensi("Dika", "Selasa", LocalDate.of(2025, 4, 2), "08:05", "17:01")
        );

        tableAbsensi.setItems(dataAbsensi);
    }
}
