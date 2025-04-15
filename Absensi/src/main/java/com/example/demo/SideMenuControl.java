package com.example.demo;

import java.io.IOException;

import com.mongodb.client.MongoDatabase;

import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

public class SideMenuControl {
    @FXML
    private BorderPane mainPane;
    @FXML
    private StackPane contentArea;
    @FXML
    private Button btndashboard;
    @FXML
    private Button btndatakaryawan;
    @FXML
    private Button btndatajamkerja;
    @FXML
    private Button btnkelompok;
    @FXML
    private Button btnrekap;
    @FXML
    private Button btnperhitungan;

   private MongoDatabase database;

public SideMenuControl() {
    database = MongoDBConnection.getDatabase();
}
@FXML
public void initialize() {
    loadPage("/com/example/demo/Dashboard/dashboard.fxml");
    btndashboard.setOnAction(event -> loadPage("/com/example/demo/Dashboard/dashboard.fxml"));
    btndatakaryawan.setOnAction(event -> loadPage("/com/example/demo/Data Karyawan/datakaryawan.fxml"));
    btndatajamkerja.setOnAction(event -> loadPage("/com/example/demo/Data Shift/datajamkerja.fxml"));
    btnkelompok.setOnAction(event -> loadPage("/com/example/demo/Data Kelompok/datakelompokkerja.fxml"));
    btnrekap.setOnAction(event -> loadPage("/com/example/demo/Rekap Absensi/rekapabsensi.fxml"));
    btnperhitungan.setOnAction(event -> loadPage("/com/example/demo/Data Lembur/datalembur.fxml"));
  
}
 private void loadPage(String fxmlPath) {
    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent page = loader.load();

        // Set transparan dulu halaman baru
        page.setOpacity(0);

        // Tambahkan halaman baru ke StackPane
        contentArea.getChildren().setAll(page);

        // Buat efek fade in
        FadeTransition fadeIn = new FadeTransition(Duration.millis(600), page);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();

    } catch (IOException e) {
        e.printStackTrace();
    }
}

}
