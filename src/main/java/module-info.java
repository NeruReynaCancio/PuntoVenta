module org.nrc.atlasjavafx {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires org.mongodb.driver.sync.client;
    requires org.mongodb.driver.core;
    requires org.mongodb.bson;
    requires java.rmi;


    opens org.nrc.atlasjavafx to javafx.fxml;
    exports org.nrc.atlasjavafx;

    opens org.nrc.atlasjavafx.Controladores to javafx.fxml; // Permite el acceso a FXML
    exports org.nrc.atlasjavafx.Controladores;
}