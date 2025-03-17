module cnt.rfid {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires javafx.graphics;
    requires com.fazecast.jSerialComm;
       

    opens cnt.rfid to javafx.fxml;
    exports cnt.rfid;
}
