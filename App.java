package org.slfx;

import javafx.application.Application;
import javafx.concurrent.Worker;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import netscape.javascript.JSObject;

public class App extends Application {

    @Override
    public void start(Stage stage) {
        WebView webView = new WebView();
        WebEngine webEngine = webView.getEngine();

        // Load halaman HTML
        String url = getClass().getResource("/org/openjfx/Login dan Sign Up/login.html").toExternalForm();
        webEngine.load(url);

        // Tambahkan JavaBridge setelah halaman selesai dimuat
        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                JSObject window = (JSObject) webEngine.executeScript("window");
                JavaBridge javaBridge = new JavaBridge(webEngine);
                window.setMember("javaBridge", javaBridge);
                System.out.println("JavaBridge berhasil terhubung!");
                System.out.println ("javaBridge initialized: " + javaBridge); // Debugging statement

                // Check if javaBridge is accessible
                Object testBridge = window.eval("typeof javaBridge");
                System.out.println("Is javaBridge accessible? " + testBridge);
            }
        });

        // Set listener untuk alert di WebEngine
        webEngine.setOnAlert(event -> {
            if (event.getData().equals("reload")) {
                webEngine.reload(); // Reload the page after successful login or signup
            }
        });

        StackPane root = new StackPane(webView);
        Scene scene = new Scene(root, 800, 600);

        stage.setTitle("JavaFX Login");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        System.out.println("Memulai aplikasi...");
        
        // ✅ Pastikan database diinisialisasi sebelum JavaFX dimulai
        DatabaseSQLite.initialize();
        System.out.println("Database diinisialisasi!");

        launch();
    }
}
