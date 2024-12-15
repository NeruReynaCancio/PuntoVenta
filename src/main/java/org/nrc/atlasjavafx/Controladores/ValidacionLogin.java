package org.nrc.atlasjavafx.Controladores;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class ValidacionLogin {

    @FXML
    private TextField usuario;

    @FXML
    private PasswordField contraseña;

    // URI de conexión a MongoDB Atlas
    private static final String MONGO_URI = "mongodb+srv://reynacancioneru:Neru2275@bda.4jnr8.mongodb.net/?retryWrites=true&w=majority&appName=BDA";

    // Nombre de la base de datos y la colección
    private static final String DATABASE_NAME = "Punto_Venta";
    private static final String COLLECTION_NAME = "Login";

    @FXML
    public void onIniciarSesionClick(ActionEvent event) {
        String usuarioIngresado = usuario.getText();
        String contraseñaIngresada = contraseña.getText();

        if (usuarioIngresado.isEmpty() || contraseñaIngresada.isEmpty()) {
            mostrarAlerta("Error", "Por favor, complete todos los campos.");
        } else if (validarCredenciales(usuarioIngresado, contraseñaIngresada)) {
            mostrarAlerta("Éxito", "Inicio de sesión correcto.");
            abrirVentanaPrincipal(event);
        } else {
            mostrarAlerta("Error", "Usuario o contraseña incorrectos.");
        }
    }


    @FXML
    public void onCrearCuentaclick(ActionEvent event) {
        try {
            // Asegúrate de que la ruta al archivo FXML sea correcta
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/nrc/atlasjavafx/Registrar.fxml"));
            Parent root = loader.load();

            // Crea una nueva escena y etapa para la ventana de registro
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Crear Cuenta");
            stage.show();

            // Cierra la ventana actual
            Stage currentStage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            currentStage.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean validarCredenciales(String usuario, String contraseña) {
        try (MongoClient mongoClient = MongoClients.create(MONGO_URI)) {
            // Accede a la base de datos y la colección
            MongoDatabase database = mongoClient.getDatabase(DATABASE_NAME);
            MongoCollection<Document> collection = database.getCollection(COLLECTION_NAME);

            // Busca el usuario en la base de datos
            Document query = new Document("usuario", usuario).append("contraseña", contraseña);
            Document resultado = collection.find(query).first();

            return resultado != null; // Devuelve true si el usuario existe
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo conectar a la base de datos.");
            return false;
        }
    }


    @FXML
    private void abrirVentanaPrincipal(ActionEvent event) {
        try {
            // Ensure the path to the FXML file is correct
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/nrc/atlasjavafx/VentanaPrincipal.fxml"));
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

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
