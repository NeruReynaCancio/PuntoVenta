package org.nrc.atlasjavafx.Controladores;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.nrc.atlasjavafx.Servicios.Encriptado;

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

    private static final String MONGO_URI = "mongodb+srv://reynacancioneru:Neru2275@basenube.2av5n18.mongodb.net/?retryWrites=true&w=majority&appName=BaseNube";
    private static final String DATABASE_NAME = "Punto_Venta";
    private static final String COLLECTION_NAME = "Login";

    @FXML
    public void onIniciarSesionClick(ActionEvent event) {
        String usuarioIngresado = usuario.getText();
        String contraseñaIngresada = contraseña.getText();

        if (usuarioIngresado.isEmpty() || contraseñaIngresada.isEmpty()) {
            mostrarAlerta("Error", "Por favor, complete todos los campos.");
        } else {
            String tipoUsuario = obtenerTipoUsuario(usuarioIngresado, contraseñaIngresada);
            if (tipoUsuario != null) {
                mostrarAlerta("Éxito", "Inicio de sesión correcto.");
                abrirVentanaPorTipo(event, tipoUsuario);
            } else {
                mostrarAlerta("Error", "Usuario o contraseña incorrectos.");
            }
        }
    }

    private String obtenerTipoUsuario(String usuario, String contraseñaIngresada) {
        try (MongoClient mongoClient = MongoClients.create(MONGO_URI)) {
            MongoDatabase database = mongoClient.getDatabase(DATABASE_NAME);
            MongoCollection<Document> collection = database.getCollection(COLLECTION_NAME);

            Document query = new Document("usuario", usuario);
            Document resultado = collection.find(query).first();

            if (resultado != null) {
                String hashAlmacenado = resultado.getString("contraseña");
                if (Encriptado.checkPassword(contraseñaIngresada, hashAlmacenado)) {
                    return resultado.getString("tipo");
                }
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo conectar a la base de datos.");
            return null;
        }
    }

    private void abrirVentanaPorTipo(ActionEvent event, String tipoUsuario) {
        try {
            String fxmlPath;
            String titulo;
            if ("admin".equalsIgnoreCase(tipoUsuario)) {
                fxmlPath = "/org/nrc/atlasjavafx/VistaAdmin.fxml";
                titulo = "Administrador";
            } else {
                fxmlPath = "/org/nrc/atlasjavafx/VentanaPrincipal.fxml";
                titulo = "Empleado";
            }
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle(titulo);
            stage.show();

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