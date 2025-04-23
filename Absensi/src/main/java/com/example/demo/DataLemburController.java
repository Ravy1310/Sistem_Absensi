import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import model.KaryawanLembur;
import org.bson.Document;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import util.MongoDBConnection;

public class DataLemburController {

    @FXML private TextField searchField;
    @FXML private TableView<KaryawanLembur> lemburTable;
    @FXML private TableColumn<KaryawanLembur, Integer> colNo;
    @FXML private TableColumn<KaryawanLembur, String> colNama;
    @FXML private TableColumn<KaryawanLembur, Integer> colJam;
    @FXML private Button calcButton;

    private final ObservableList<KaryawanLembur> lemburList = FXCollections.observableArrayList();
    private final MongoDatabase database = MongoDBConnection.getDatabase(); // langsung konek MongoDB

    @FXML
    public void initialize() {
        colNo.setCellValueFactory(data -> data.getValue().noProperty().asObject());
        colNama.setCellValueFactory(data -> data.getValue().namaProperty());
        colJam.setCellValueFactory(data -> data.getValue().jamLemburProperty().asObject());

        loadDataLembur();

        searchField.setOnAction(e -> onSearch()); // enter untuk cari
        calcButton.setOnAction(e -> onCalculation()); // klik tombol untuk hitung
    }

    private void loadDataLembur() {
        lemburList.clear();
        MongoCollection<Document> collection = database.getCollection("Lembur");
        int no = 1;
        for (Document doc : collection.find()) {
            KaryawanLembur lembur = new KaryawanLembur(
                no++,
                doc.getString("nama"),
                doc.getInteger("jamLembur")
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
                doc.getInteger("jamLembur")
            );
            lemburList.add(lembur);
        }

        lemburTable.setItems(lemburList);
        searchField.clear();
    }

    private void onCalculation() {
        int totalJam = lemburList.stream()
                .mapToInt(KaryawanLembur::getJamLembur)
                .sum();

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Perhitungan Jam Lembur");
        alert.setHeaderText(null);
        alert.setContentText("Total jam lembur: " + totalJam + " jam");
        alert.showAndWait();
    }
}
