import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import model.KaryawanLembur;
import service.MongoDBService;

public class DataLemburController {

    @FXML
    private TextField searchField;

    @FXML
    private TableView<KaryawanLembur> lemburTable;

    @FXML
    private TableColumn<KaryawanLembur, Integer> colNo;

    @FXML
    private TableColumn<KaryawanLembur, String> colNama;

    @FXML
    private TableColumn<KaryawanLembur, Integer> colJam;

    @FXML
    private Button calcButton;

    private final ObservableList<KaryawanLembur> lemburList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Setup kolom
        colNo.setCellValueFactory(data -> data.getValue().noProperty().asObject());
        colNama.setCellValueFactory(data -> data.getValue().namaProperty());
        colJam.setCellValueFactory(data -> data.getValue().jamLemburProperty().asObject());

        // Load data awal dari MongoDB
        loadDataLembur();
    }

    private void loadDataLembur() {
        lemburList.clear();
        lemburList.addAll(MongoDBService.getAllDataLembur()); // asumsi service return List<KaryawanLembur>
        lemburTable.setItems(lemburList);
    }

    @FXML
    private void onSearch() {
        String keyword = searchField.getText().toLowerCase();
        if (keyword.isEmpty()) {
            loadDataLembur();
            return;
        }

        lemburList.clear();
        lemburList.addAll(MongoDBService.searchLembur(keyword)); // pencarian berdasarkan nama/ID
        lemburTable.setItems(lemburList);
    }

    @FXML
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

