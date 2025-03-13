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
import javafx.stage.Stage;

public class SignUpController {
    @FXML
    private Button SignInButton; // Harus sama dengan yang ada di FXML

    @FXML
    private Button SignUpButton; // Harus sama dengan yang ada di FXML

    @FXML
    private TextField EmailField;
    @FXML
    private TextField UsernameField;
    @FXML
    private PasswordField PasswordField;


private MongoDatabase database;

public SignUpController() {
    database = MongoDBConnection.getDatabase();
}

@FXML
public void initialize() {
    SignUpButton.setOnAction(event -> handleSignUp());
   
    SignInButton.setOnAction(event -> loadPage("/com/example/demo/Login/login.fxml"));
        // 
        }
    
            
private void handleSignUp() {
    String email = EmailField.getText().trim();
    String username = UsernameField.getText().trim();
    String password = PasswordField.getText().trim(); 
    
    if (email.isEmpty() || username.isEmpty() || password.isEmpty()) {
            showAlert("Email, username, dan password tidak boleh kosong!", AlertType.ERROR);
            return;
        }

        // Validasi format email sederhana
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            showAlert("Format email tidak valid!", AlertType.ERROR);
            return;
        }

        MongoCollection<Document> users = database.getCollection("users");

        // Cek apakah email atau username sudah digunakan
        if (users.find(Filters.or(Filters.eq("email", email), Filters.eq("username", username))).first() != null) {
            showAlert("Email atau username sudah terdaftar!", AlertType.ERROR);
            EmailField.clear();
            UsernameField.clear();
            PasswordField.clear();
            return;
        }

        // Hash password sebelum menyimpan
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        // Simpan user baru ke MongoDB
        Document userDoc = new Document("email", email)
                           .append("username", username)
                           .append("password", hashedPassword);
        users.insertOne(userDoc);

        showAlert("Akun berhasil dibuat! Silakan login.", AlertType.INFORMATION);
        loadPage("/com/example/demo/Login/login.fxml");
}   
       

        private void loadPage(String fxmlFile) {
    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
        Parent root = loader.load();
        Stage stage = (Stage) SignInButton.getScene().getWindow();
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
