module com.example.demo {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.mongodb.driver.sync.client;
    requires org.mongodb.bson;
    requires org.mongodb.driver.core;
    requires java.xml;
    requires jbcrypt;
    requires javafx.base;
    requires javafx.graphics;


    opens com.example.demo to javafx.fxml;
    exports com.example.demo;
}