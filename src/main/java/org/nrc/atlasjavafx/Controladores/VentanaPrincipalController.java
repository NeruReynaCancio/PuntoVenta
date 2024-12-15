package org.nrc.atlasjavafx.Controladores;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;

public class VentanaPrincipalController {

    @FXML
    private AnchorPane contentArea;

    @FXML
    void initialize() {
        // Carga la primera vista , podemos hacer otra
        try {
            loadPage("productos");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleProductos(ActionEvent event) throws IOException {
        loadPage("productos");
    }

    @FXML
    private void handleProveedores(ActionEvent event) throws IOException {
        loadPage("proveedores");
    }

    @FXML
    private void handleEmpleados(ActionEvent event) throws IOException {
        loadPage("empleados");
    }

    @FXML
    private void handleClientes(ActionEvent event) throws IOException {
        loadPage("clientes");
    }

    @FXML
    private void handleSalir(ActionEvent event) {
        try {
            // Cargamos la nueva ventana
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/nrc/atlasjavafx/hello-view.fxml"));
            Parent root = loader.load();

            // nueva escena en una nueva ventana
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Ventana Principal");
            stage.show();

            // cerramos la ventana que estaba abierta
            Stage currentStage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            currentStage.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }




    private void loadPage(String page) throws IOException {
        Parent root = null;

        root = FXMLLoader.load(getClass().getResource("/org/nrc/atlasjavafx/" + page + ".fxml"));

        contentArea.getChildren().clear();
        contentArea.getChildren().add(root);
    }
}

