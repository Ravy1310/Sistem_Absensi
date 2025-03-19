package cnt.rfid;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.;

public class rekapController {
    @FXML
    private TableView<RekapAbsensi> tblRekapAbsensi;
    @FXML
    private TableColumn<RekapAbsensi, String> colNo;
    @FXML
    private TableColumn<RekapAbsensi, String> colNama;
    @FXML
    private TableColumn<RekapAbsensi, String> colBulan;
    @FXML
    private TableColumn<RekapAbsensi, String> colTahun;
    @FXML
    private TableColumn<RekapAbsensi, Integer> colTotalHadir;

       private ObservableList<RekapAbsensi> dataRekapAbsensi = FXCollections.observableArrayList();

      public void initialize() {
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
        colBulan.setCellValueFactory(cellData -> cellData.getValue().bulanProperty());
        colTahun.setCellValueFactory(cellData -> cellData.getValue().tahunProperty());
        colTotalHadir.setCellValueFactory(cellData -> cellData.getValue().totalHadirProperty().asObject());

        loadData();
    }

    private void loadData() {
        dataRekapAbsensi.clear();
        String query = "SELECT nama, MONTH(tanggal) AS bulan, YEAR(tanggal) AS tahun, COUNT(*) AS total_hadir " +
                       "FROM absensi GROUP BY nama, bulan, tahun ORDER BY tahun DESC, bulan DESC";

         while (rs.next()) {
                dataRekapAbsensi.add(new RekapAbsensi(
                        rs.getString("nama"),
                        String.valueOf(rs.getInt("bulan")),
                        String.valueOf(rs.getInt("tahun")),
                        rs.getInt("total_hadir")
                ));
            }
            tblRekapAbsensi.setItems(dataRekapAbsensi);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

