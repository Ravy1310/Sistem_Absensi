package cnt.rfid;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import java.io.IOException;

public class HelloController {
    @FXML
    private Button dataKaryawanButton;
    @FXML
    private Button rekapAbsensiButton;

    @FXML
    public void initialize() {
        dataKaryawanButton.setOnAction(event -> {
            System.out.println("Data Karyawan clicked!");
            loadSideMenu("Data Karyawan/datakaryawan.fxml"); // Masuk ke menu dengan konten data karyawan
        });

        rekapAbsensiButton.setOnAction(event -> {
            System.out.println("Rekap Absensi clicked!");
            loadSideMenu("rekap absensi/rekapabsensi.fxml"); // Masuk ke menu dengan konten rekap absensi
        });
    }

    private void loadSideMenu(String content) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/cnt/rfid/menu/submenu.fxml"));
            Parent root = loader.load();

            // Kirim parameter ke SideMenuController
            SideMenuController sideMenuController = loader.getController();
            sideMenuController.setContent(content);

            Stage stage = (Stage) dataKaryawanButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
