package cnt.rfid;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;

public class SideMenuController {
    private static final Logger LOGGER = Logger.getLogger(SideMenuController.class.getName());

    @FXML
    private BorderPane mainPane; // Pastikan di FXML root adalah BorderPane

    @FXML
    private StackPane contentArea;

    @FXML
    private Button btnKaryawan;

    @FXML
    private Button btnRekap;

    public void initialize() {
        btnKaryawan.setOnAction(event -> {
            loadContent("/cnt/rfid/Data Karyawan/datakaryawan.fxml"); // Masuk ke menu dengan konten data karyawan
        });

        btnRekap.setOnAction(event -> {
            loadContent("/cnt/rfid/rekap absensi/rekapabsensi.fxml"); // Masuk ke menu dengan konten rekap absensi
        });
    }

    public void setContent(String content) {
        if (content == null || content.isEmpty()) {
            LOGGER.log(Level.WARNING, "Content path is null or empty");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/cnt/rfid/" + content));
            Node loadedContent = loader.load(); // Muat FXML menjadi Node
            if (contentArea != null) {
                contentArea.getChildren().setAll(loadedContent);
                Platform.runLater(() -> contentArea.getScene().getWindow().sizeToScene());

            } else {
                LOGGER.log(Level.SEVERE, "contentArea is null");
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error loading content: " + content, e);
        }
    }

    public void loadContent(String fxmlPath) {
        if (fxmlPath == null || fxmlPath.isEmpty()) {
            LOGGER.log(Level.WARNING, "FXML path is null or empty");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node content = loader.load(); // Muat FXML menjadi Node
            if (contentArea != null) {
                contentArea.getChildren().setAll(content); // Ganti konten StackPane dengan Node
            } else {
                LOGGER.log(Level.SEVERE, "contentArea is null");
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error loading FXML: " + fxmlPath, e);
        }
    }
}

