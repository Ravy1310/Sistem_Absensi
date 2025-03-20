package cnt.absensi;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.;

public class RekapAbsensiController {
    @FXML
    private TableView<Absensi> tblAbsensi;
    @FXML
    private TableColumn<Absensi, String> colNo;
    @FXML
    private TableColumn<Absensi, String> colNama;
    @FXML
    private TableColumn<Absensi, String> colHari;
    @FXML
    private TableColumn<Absensi, String> colTanggal;
    @FXML
    private TableColumn<Absensi, String> colJamMasuk;
    @FXML
    private TableColumn<Absensi, String> colJamPulang;
    @FXML
    private ComboBox<String> cmbBulan;
    @FXML
    private ComboBox<String> cmbTahun;
    @FXML
    private TextField txtCari;
    @FXML
    private Button btnCari;

    private ObservableList<Absensi> dataAbsensi = FXCollections.observableArrayList();

    public void initialize() {
        btnCari.setOnAction(event -> loadData());

        colNo.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(String.valueOf(getIndex() + 1));
                }
            }
        });

        colNama.setCellValueFactory(cellData -> cellData.getValue().namaProperty());
        colHari.setCellValueFactory(cellData -> cellData.getValue().hariProperty());
        colTanggal.setCellValueFactory(cellData -> cellData.getValue().tanggalProperty());
        colJamMasuk.setCellValueFactory(cellData -> cellData.getValue().jamMasukProperty());
        colJamPulang.setCellValueFactory(cellData -> cellData.getValue().jamPulangProperty());

        loadData();
    }

    private void loadData() {
        dataAbsensi.clear();
        String query = "SELECT a.nama, DAYNAME(a.tanggal) AS hari, a.tanggal, a.jam_masuk, a.jam_pulang " +
                       "FROM absensi a WHERE 1=1";

     
        if (cmbBulan.getValue() != null && !cmbBulan.getValue().isEmpty()) {
            query += " AND MONTH(a.tanggal) = " + cmbBulan.getValue();
        }
        if (cmbTahun.getValue() != null && !cmbTahun.getValue().isEmpty()) {
            query += " AND YEAR(a.tanggal) = " + cmbTahun.getValue();
        }
        if (!txtCari.getText().isEmpty()) {
            query += " AND a.nama LIKE '%" + txtCari.getText() + "%'";
        }

        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                dataAbsensi.add(new Absensi(
                        rs.getString("nama"),
                        rs.getString("hari"),
                        rs.getString("tanggal"),
                        rs.getString("jam_masuk"),
                        rs.getString("jam_pulang")
                ));
            }
            tblAbsensi.setItems(dataAbsensi);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}




