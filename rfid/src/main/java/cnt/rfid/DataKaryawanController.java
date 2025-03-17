package cnt.rfid;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class DataKaryawanController {
    @FXML
    private TableView<Karyawan> tblKaryawan;
    @FXML
    private TableColumn<Karyawan, String> colNo;
    @FXML
    private TableColumn<Karyawan, String> colNama;
    @FXML
    private TableColumn<Karyawan, String> colEmail;
    @FXML
    private TableColumn<Karyawan, String> colPosisi;
    @FXML
    private TableColumn<Karyawan, String> colAlamat;
    @FXML
    private TableColumn<Karyawan, String> colHP;
    @FXML
    private TableColumn<Karyawan, String> colTanggalMasuk;
    @FXML
    private TextField txtID; // Untuk membaca input dari kartu RFID
    @FXML
    private TextField txtNama;
    @FXML
    private TextField txtEmail;
    @FXML
    private TextField txtPosisi;
    @FXML
    private TextField txtAlamat;
    @FXML
    private TextField txtHP;
    @FXML
    private TextField txtTanggalMasuk;
    @FXML
    private Button btnSimpan;
    @FXML
    private Button btnBatal;

    private ObservableList<Karyawan> dataKaryawan = FXCollections.observableArrayList();

    public void initialize() {
        // Set nomor urut otomatis di TableView
        btnSimpan.setOnAction(event -> {
        simpanData();  });
        btnBatal.setOnAction(event -> {
            BatalSimpan(); });
        
      

        colNo.setCellFactory(tc -> new javafx.scene.control.TableCell<Karyawan, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(String.valueOf(getIndex() + 1)); // Menampilkan nomor urut
                }
            }
        });

        // Menghubungkan kolom dengan property di Karyawan.java
        colNama.setCellValueFactory(cellData -> cellData.getValue().namaProperty());
        colEmail.setCellValueFactory(cellData -> cellData.getValue().emailProperty());
        colPosisi.setCellValueFactory(cellData -> cellData.getValue().posisiProperty());
        colAlamat.setCellValueFactory(cellData -> cellData.getValue().alamatProperty());
        colHP.setCellValueFactory(cellData -> cellData.getValue().noHpProperty());
        colTanggalMasuk.setCellValueFactory(cellData -> cellData.getValue().tanggalMasukProperty());

        // Load data dari database
        loadData();
    }

    private void loadData() {
        dataKaryawan.clear();
        Connection conn = DatabaseConnection.connect(); // Pastikan DatabaseConnection.java ada
        String query = "SELECT nama, email, posisi, alamat, no_hp, tanggal_masuk FROM karyawan";
        
        try (PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                dataKaryawan.add(new Karyawan(
                        rs.getString("nama"),
                        rs.getString("email"),
                        rs.getString("posisi"),
                        rs.getString("alamat"),
                        rs.getString("no_hp"),
                        rs.getString("tanggal_masuk")
                ));
            }
            tblKaryawan.setItems(dataKaryawan);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Fungsi untuk menyimpan data dari RFID ke database
    @FXML
    private void simpanData() {
        String id = txtID.getText();
        if (id.isEmpty()) {
            System.out.println("ID RFID tidak boleh kosong!");
            return;
        }

        Connection conn = DatabaseConnection.connect();
        String query = "INSERT INTO karyawan (rfid_uid, nama, email, posisi, alamat, no_hp, tanggal_masuk) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, id);
            ps.setString(2, txtNama.getText()); // Bisa diganti dengan input
            ps.setString(3, txtEmail.getText()); // Bisa diganti dengan input
            ps.setString(4, txtPosisi.getText()); // Bisa diganti dengan input
            ps.setString(5, txtAlamat.getText()); // Bisa diganti dengan input
            ps.setString(6, txtHP.getText()); // Bisa diganti dengan input
            ps.setString(7, txtTanggalMasuk.getText()); // Bisa diganti dengan input

            ps.executeUpdate();
            System.out.println("Data berhasil disimpan!");
            txtID.clear();
            txtNama.clear();
            txtEmail.clear();
            txtPosisi.clear();
            txtAlamat.clear();
            txtHP.clear();
            txtTanggalMasuk.clear();

            loadData(); // Refresh tabel setelah menyimpan
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void BatalSimpan() {
       txtID.clear();
       txtNama.clear();
         txtEmail.clear();
            txtPosisi.clear();
            txtAlamat.clear();
            txtHP.clear();
            txtTanggalMasuk.clear();
            
    }
}
