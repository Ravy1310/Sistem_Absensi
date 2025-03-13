package com.example.demo;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class SignUpController {
    @FXML
    private Button SignInButton; // Harus sama dengan yang ada di FXML

    @FXML
    private Button SignUpButton; // Harus sama dengan yang ada di FXML



@FXML
public void initialize() {
    SignUpButton.setOnAction(event -> {
        System.out.println("SignUp button clicked!");
    });
   
    SignInButton.setOnAction(event -> loadPage("/com/example/demo/Login/login.fxml"));
        // 
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
}



