package com.example.demo;

import java.io.IOException;

import org.bson.Document;
import org.mindrot.jbcrypt.BCrypt;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class LoginController {
    @FXML
    private Button loginButton; // Harus sama dengan yang ada di FXML
    @FXML
    private Button SignUpButton, btnLogOut; // Harus sama dengan yang ada di FXML
    @FXML
    private TextField UsernameField;
    @FXML
    private PasswordField PasswordField;
    @FXML
    private Text messageText;
    
    private MongoDatabase database;

    public LoginController() {
        database = MongoDBConnection.getDatabase();
    }

    @FXML
    public void initialize() {
        loginButton.setOnAction(event ->handleLogin());
                
                SignUpButton.setOnAction(event -> loadPage("/com/example/demo/Login/SignUp.fxml"));
                btnLogOut.setOnAction(event -> {
                    loadPage("/com/example/demo/absensi karyawan/absen.fxml");
                    });
                
                }
    private void handleLogin() {
        String username = UsernameField.getText().trim();
        String password = PasswordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Username dan password harus diisi!", AlertType.ERROR);
            return;
        }

        MongoCollection<Document> users = database.getCollection("users");

        // Cari user berdasarkan username
        Document user = users.find(Filters.eq("username", username)).first();

        if (user != null) {
            String storedHashedPassword = user.getString("password");

            // Verifikasi password yang dimasukkan dengan yang ada di database
            if (BCrypt.checkpw(password, storedHashedPassword)) {
                showAlert("Login berhasil! Selamat datang, " + username + "!", AlertType.INFORMATION);
                UsernameField.clear();
                PasswordField.clear();
                loadDashboard();
            } else {
                showAlert("Login gagal! Password salah.", AlertType.ERROR);
                UsernameField.clear();
                PasswordField.clear();
            }
        } else {
            showAlert("Login gagal! Username tidak ditemukan.", AlertType.ERROR);
            UsernameField.clear();
            PasswordField.clear();
        }
        }
                
                
        
        private void loadDashboard() {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo/menu/submenu.fxml")); // ganti path sesuai letak dashboard.fxml kamu
                Parent root = loader.load();
        
                // Ambil stage dari elemen GUI saat ini (misalnya dari UsernameField)
                Stage stage = (Stage) UsernameField.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("BICOPI");
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
                showAlert("Gagal memuat halaman dashboard.", AlertType.ERROR);
            }
        }

        private void loadPage(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();
            Stage stage = (Stage) SignUpButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String message, AlertType type) {
    Alert alert = new Alert(type);
    alert.setTitle("Notification");
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
}

}