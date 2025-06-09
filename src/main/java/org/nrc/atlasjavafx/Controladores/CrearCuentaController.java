// src/main/java/org/nrc/atlasjavafx/Controladores/CrearCuentaController.java
    package org.nrc.atlasjavafx.Controladores;

    import com.mongodb.client.MongoClient;
    import com.mongodb.client.MongoClients;
    import com.mongodb.client.MongoCollection;
    import com.mongodb.client.MongoDatabase;
    import javafx.collections.FXCollections;
    import javafx.fxml.FXMLLoader;
    import javafx.scene.Parent;
    import javafx.scene.Scene;
    import org.bson.Document;
    import org.nrc.atlasjavafx.Servicios.Encriptado;

    import javafx.event.ActionEvent;
    import javafx.fxml.FXML;
    import javafx.scene.control.Alert;
    import javafx.scene.control.ComboBox;
    import javafx.scene.control.PasswordField;
    import javafx.scene.control.TextField;
    import javafx.stage.Stage;

    import java.io.IOException;

    public class CrearCuentaController {

        @FXML
        private TextField txtUsuario;

        @FXML
        private PasswordField txtContrasena;

        @FXML
        private ComboBox<String> cmbTipo;

        private static final String MONGO_URI = "mongodb+srv://reynacancioneru:Neru2275@basenube.2av5n18.mongodb.net/?retryWrites=true&w=majority&appName=BaseNube";
        private static final String DATABASE_NAME = "Punto_Venta";
        private static final String COLLECTION_NAME = "Login";

        @FXML
        public void initialize() {
            cmbTipo.setItems(FXCollections.observableArrayList("admin", "empleado"));
        }

        @FXML
        public void registrarUsuario(ActionEvent event) {
            if (!validarCamposRegistro()) {
                return;
            }
            String usuario = txtUsuario.getText();
            String contrasena = txtContrasena.getText();
            String tipo = cmbTipo.getValue();

            if (guardarUsuarioEnBaseDeDatos(usuario, contrasena, tipo)) {
                mostrarAlerta("Éxito", "Usuario registrado exitosamente.");
                limpiarCampos();
            } else {
                mostrarAlerta("Error", "No se pudo registrar el usuario. Es posible que ya exista.");
            }
        }

        private boolean guardarUsuarioEnBaseDeDatos(String usuario, String contrasena, String tipo) {
            try (MongoClient mongoClient = MongoClients.create(MONGO_URI)) {
                MongoDatabase database = mongoClient.getDatabase(DATABASE_NAME);
                MongoCollection<Document> collection = database.getCollection(COLLECTION_NAME);

                Document query = new Document("usuario", usuario);
                if (collection.find(query).first() != null) {
                    return false;
                }

                // Cifrar la contraseña antes de guardarla
                String hash = Encriptado.hashPassword(contrasena);

                Document nuevoUsuario = new Document("usuario", usuario)
                        .append("contraseña", hash)
                        .append("tipo", tipo);
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
            cmbTipo.getSelectionModel().clearSelection();
        }

        private void mostrarAlerta(String titulo, String mensaje) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(titulo);
            alert.setHeaderText(null);
            alert.setContentText(mensaje);
            alert.showAndWait();
        }

        private boolean soloLetrasYNumeros(String texto) {
            return texto.matches("[a-zA-Z0-9\\s]+");
        }

        private boolean validarCamposRegistro() {
            String usuario = txtUsuario.getText();
            String contrasena = txtContrasena.getText();
            String tipo = cmbTipo.getValue();

            if (usuario.isEmpty() || contrasena.isEmpty() || tipo == null) {
                mostrarAlerta("Error", "Por favor, complete todos los campos y seleccione un tipo.");
                return false;
            }
            if (!soloLetrasYNumeros(usuario)) {
                mostrarAlerta("Error", "El usuario solo debe contener letras, números y espacios, sin acentos ni caracteres especiales.");
                return false;
            }
            return true;
        }

        @FXML
        protected void onRegresarClick(ActionEvent event) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/nrc/atlasjavafx/VistaAdmin.fxml"));
                Parent root = loader.load();
                Stage stage = new Stage();
                stage.setScene(new Scene(root));
                stage.setTitle("Ventana Principal");
                stage.show();
                Stage currentStage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
                currentStage.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }