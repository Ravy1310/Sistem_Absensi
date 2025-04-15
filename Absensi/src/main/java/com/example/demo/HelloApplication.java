package com.example.demo;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class HelloApplication extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/com/example/demo/absensi karyawan/absen.fxml"));

        Scene scene = new Scene(root);
        primaryStage.setTitle("BICOPI");
        primaryStage.setScene(scene);
 // Maksimalkan jendela saat startup
    primaryStage.setMaximized(true);

    // Sesuaikan ukuran dengan layar utama
    Rectangle2D screenBounds = Screen.getPrimary().getBounds();
    primaryStage.setWidth(screenBounds.getWidth());
    primaryStage.setHeight(screenBounds.getHeight());
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
       
    }
}
