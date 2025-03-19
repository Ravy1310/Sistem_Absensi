package cnt.rfid;

import java.sql.;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class DataAbsensiController {
    @FXML
    private TextField txtRFID;

    public void initialize() {
        txtRFID.setOnAction(event -> processRFID(txtRFID.getText().trim()));
    }

    private void processRFID(String rfid) {
        if (rfid.isEmpty()) {
            System.out.println("RFID tidak boleh kosong!");
            return;
        }

        Connection conn = DatabaseConnection.connect();
        String checkQuery = "SELECT id, waktu_masuk, waktu_keluar FROM absensi WHERE rfid_uid = ? AND tanggal = CURDATE() ORDER BY id DESC LIMIT 1";

        try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
            checkStmt.setString(1, rfid);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                if (rs.getTimestamp("waktu_keluar") == null) {
                    updateWaktuKeluar(conn, rs.getInt("id"));
                } else {
                    insertWaktuMasuk(conn, rfid);
                }
            } else {
                insertWaktuMasuk(conn, rfid);
            }

            txtRFID.clear();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void insertWaktuMasuk(Connection conn, String rfid) throws SQLException {
        String query = "INSERT INTO absensi (rfid_uid, nama, tanggal, waktu_masuk) VALUES (?, (SELECT nama FROM karyawan WHERE rfid_uid = ?), CURDATE(), NOW())";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, rfid);
            ps.setString(2, rfid);
            ps.executeUpdate();
            System.out.println("✅ Absensi masuk tercatat.");
        }
    }

    private void updateWaktuKeluar(Connection conn, int id) throws SQLException {
        String query = "UPDATE absensi SET waktu_keluar = NOW() WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("✅ Absensi keluar tercatat.");
        }
    }
}
