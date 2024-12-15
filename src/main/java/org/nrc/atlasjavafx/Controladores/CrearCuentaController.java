package org.nrc.atlasjavafx.Controladores;


import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import org.bson.Document;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class CrearCuentaController {

    @FXML
    private TextField txtUsuario;

    @FXML
    private PasswordField txtContrasena;

    private static final String MONGO_URI = "mongodb+srv://reynacancioneru:Neru2275@bda.4jnr8.mongodb.net/?retryWrites=true&w=majority&appName=BDA";
    private static final String DATABASE_NAME = "Punto_Venta";
    private static final String COLLECTION_NAME = "Login";

    @FXML
    public void registrarUsuario(ActionEvent event) {
        String usuario = txtUsuario.getText();
        String contrasena = txtContrasena.getText();

        if (usuario.isEmpty() || contrasena.isEmpty()) {
            mostrarAlerta("Error", "Por favor, complete todos los campos.");
        } else {
            if (guardarUsuarioEnBaseDeDatos(usuario, contrasena)) {
                mostrarAlerta("Éxito", "Usuario registrado exitosamente.");
                limpiarCampos();
            } else {
                mostrarAlerta("Error", "No se pudo registrar el usuario. Es posible que ya exista.");
            }
        }
    }

    private boolean guardarUsuarioEnBaseDeDatos(String usuario, String contrasena) {
        try (MongoClient mongoClient = MongoClients.create(MONGO_URI)) {
            MongoDatabase database = mongoClient.getDatabase(DATABASE_NAME);
            MongoCollection<Document> collection = database.getCollection(COLLECTION_NAME);

            // Verifica si el usuario ya existe
            Document query = new Document("usuario", usuario);
            if (collection.find(query).first() != null) {
                return false; // Usuario ya existe
            }

            // Crea un nuevo documento y lo guarda en la base de datos
            Document nuevoUsuario = new Document("usuario", usuario)
                    .append("contraseña", contrasena);
            collection.insertOne(nuevoUsuario);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void limpiarCampos() {
        txtUsuario.clear();
        txtContrasena.clear();
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }


    @FXML
    protected void onRegresarClick(ActionEvent event) {
        try {
            // Ensure the path to the FXML file is correct
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/nrc/atlasjavafx/hello-view.fxml"));
            Parent root = loader.load();

            // Create a new scene and stage for the main window
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Ventana Principal");
            stage.show();

            // Close the current window
            Stage currentStage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            currentStage.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
