<<<<<<< HEAD
package com.example.demo;

import org.bson.Document;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

=======
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import model.KaryawanLembur;
import org.bson.Document;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import util.MongoDBConnection;
>>>>>>> 4de601c9dc722e6e06c187a227d014c4cf71e3cd

public class DataLemburController {

    @FXML private TextField searchField;
    @FXML private TableView<KaryawanLembur> lemburTable;
    @FXML private TableColumn<KaryawanLembur, Integer> colNo;
    @FXML private TableColumn<KaryawanLembur, String> colNama;
    @FXML private TableColumn<KaryawanLembur, Integer> colJam;
    @FXML private Button calcButton;

    private final ObservableList<KaryawanLembur> lemburList = FXCollections.observableArrayList();
    private final MongoDatabase database = MongoDBConnection.getDatabase();

    @FXML
    public void initialize() {
        colNo.setCellValueFactory(data -> data.getValue().noProperty().asObject());
        colNama.setCellValueFactory(data -> data.getValue().namaProperty());
        colJam.setCellValueFactory(data -> data.getValue().jamLemburProperty().asObject());

        loadDataLembur();

        searchField.setOnAction(e -> onSearch());
        calcButton.setOnAction(e -> onCalculation());
    }

    private void loadDataLembur() {
        lemburList.clear();
        MongoCollection<Document> collection = database.getCollection("Lembur");
        int no = 1;
        for (Document doc : collection.find()) {
            KaryawanLembur lembur = new KaryawanLembur(
                no++,
                doc.getString("nama"),
                doc.getInteger("jamLembur") != null ? doc.getInteger("jamLembur") : 0
            );
            lemburList.add(lembur);
        }
        lemburTable.setItems(lemburList);
    }

    private void onSearch() {
        String keyword = searchField.getText().trim().toLowerCase();
        lemburList.clear();

        if (keyword.isEmpty()) {
            loadDataLembur();
            return;
        }

        MongoCollection<Document> collection = database.getCollection("Lembur");
        int no = 1;
        for (Document doc : collection.find(new Document("nama", new Document("$regex", keyword).append("$options", "i")))) {
            KaryawanLembur lembur = new KaryawanLembur(
                no++,
                doc.getString("nama"),
                doc.getInteger("jamLembur") != null ? doc.getInteger("jamLembur") : 0
            );
            lemburList.add(lembur);
        }

        lemburTable.setItems(lemburList);
        searchField.clear();
    }

    private void onCalculation() {
        if (lemburList.isEmpty()) {
            showAlert("Data kosong", "Tidak ada data lembur untuk dihitung.");
            return;
        }

        int totalJam = lemburList.stream()
                .mapToInt(KaryawanLembur::getJamLembur)
                .sum();

        showAlert("Perhitungan Jam Lembur", "Total jam lembur: " + totalJam + " jam");
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
<<<<<<< HEAD
}
=======
}
>>>>>>> 4de601c9dc722e6e06c187a227d014c4cf71e3cd
