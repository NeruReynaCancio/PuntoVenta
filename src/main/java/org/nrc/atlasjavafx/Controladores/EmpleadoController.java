package org.nrc.atlasjavafx.Controladores;

import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.application.Platform;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.nrc.atlasjavafx.Bean.Direccion;
import org.nrc.atlasjavafx.Bean.Empleado;
import org.nrc.atlasjavafx.Servicios.ServicioRespaldo;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class EmpleadoController implements Initializable {

    @FXML private TableView<Empleado> tablaEmpleados;
    @FXML private TableColumn<Empleado, String> colId;
    @FXML private TableColumn<Empleado, String> colNombre;
    @FXML private TableColumn<Empleado, String> colApellidos;
    @FXML private TableColumn<Empleado, String> colContacto;
    @FXML private TableColumn<Empleado, String> colGenero;
    @FXML private TableColumn<Empleado, String> colDepartamento;
    @FXML private TableColumn<Empleado, String> colCalle;
    @FXML private TableColumn<Empleado, String> colCiudad;
    @FXML private TableColumn<Empleado, String> colCP;

    @FXML private TextField txtNombre;
    @FXML private TextField txtApellidos;
    @FXML private TextField txtContacto;
    @FXML private TextField txtGenero;
    @FXML private TextField txtDepartamento;

    @FXML private TextField txtCalle;
    @FXML private TextField txtCiudad;
    @FXML private TextField txtCP;

    private static final String MONGO_URI = "mongodb+srv://reynacancioneru:Neru2275@basenube.2av5n18.mongodb.net/?retryWrites=true&w=majority&appName=BaseNube";
    private static final String DATABASE_NAME = "Punto_Venta";
    private static final String COLLECTION_NAME = "Empleado";

    private MongoClient mongoClient;
    private MongoDatabase database;
    private MongoCollection<Document> collection;
    private ObservableList<Empleado> listaEmpleados;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            // Inicializar MongoDB
            mongoClient = MongoClients.create(MONGO_URI);
            database = mongoClient.getDatabase(DATABASE_NAME);
            collection = database.getCollection(COLLECTION_NAME);

            // Inicializar la lista observable
            listaEmpleados = FXCollections.observableArrayList();

            // Configurar las columnas
            colId.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getId()));
            colNombre.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getNombre()));
            colApellidos.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getApellidos()));
            colContacto.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getContacto()));
            colGenero.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getGenero()));
            colDepartamento.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getDepartamento()));
            colCalle.setCellValueFactory(cellData ->
                    new SimpleStringProperty(
                            cellData.getValue().getDireccion() != null ? cellData.getValue().getDireccion().getCalle() : ""
                    )
            );
            colCiudad.setCellValueFactory(cellData ->
                    new SimpleStringProperty(
                            cellData.getValue().getDireccion() != null ? cellData.getValue().getDireccion().getCiudad() : ""
                    )
            );
            colCP.setCellValueFactory(cellData ->
                    new SimpleStringProperty(
                            cellData.getValue().getDireccion() != null ? cellData.getValue().getDireccion().getCp() : ""
                    )
            );

            // Establecer ancho de columnas
            colId.setPrefWidth(75);
            colNombre.setPrefWidth(182);
            colApellidos.setPrefWidth(123);
            colContacto.setPrefWidth(98);
            colGenero.setPrefWidth(147);
            colDepartamento.setPrefWidth(193);


            // Asignar la lista observable a la tabla
            tablaEmpleados.setItems(listaEmpleados);

            // Agregar listener de selección
            tablaEmpleados.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    mostrarDetallesEmpleado(newSelection);
                }
            });

            // Cargar datos iniciales
            cargarDatos();

            System.out.println("Inicialización completada");
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "Error al inicializar: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void agregarEmpleado() {
        try {
            if (validarCampos()) {
                // Crear el objeto Direccion a partir de los campos de texto
                Direccion direccion = new Direccion(
                        txtCalle.getText(),
                        txtCiudad.getText(),
                        txtCP.getText()
                );

                // Crear subdocumento de dirección
                Document direccionDoc = new Document()
                        .append("calle", direccion.getCalle())
                        .append("ciudad", direccion.getCiudad())
                        .append("cp", direccion.getCp());

                // Crear nuevo documento
                Document doc = new Document()
                        .append("nombre", txtNombre.getText())
                        .append("apellidos", txtApellidos.getText())
                        .append("contacto", txtContacto.getText())
                        .append("genero", txtGenero.getText())
                        .append("departamento", txtDepartamento.getText())
                        .append("direccion", direccionDoc);

                // Insertar en MongoDB
                collection.insertOne(doc);

                // Crear nuevo objeto Empleado
                Empleado nuevoEmpleado = new Empleado(
                        doc.getObjectId("_id").toString(),
                        txtNombre.getText(),
                        txtApellidos.getText(),
                        txtContacto.getText(),
                        txtGenero.getText(),
                        txtDepartamento.getText(),
                        direccion
                );

                // Actualizar la UI en el hilo de JavaFX
                Platform.runLater(() -> {
                    listaEmpleados.add(nuevoEmpleado);
                    tablaEmpleados.refresh();
                    limpiarCampos();
                    mostrarAlerta("Éxito", "Empleado agregado correctamente", Alert.AlertType.INFORMATION);
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "Error al agregar empleado: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void cargarDatos() {
        try {
            // Realizar la consulta a MongoDB
            FindIterable<Document> documents = collection.find();

            // Crear una lista temporal
            ObservableList<Empleado> tempList = FXCollections.observableArrayList();

            // Poblar la lista temporal
            for (Document doc : documents) {
                Document direccionDoc = doc.get("direccion", Document.class);
                Direccion direccion = null;
                if (direccionDoc != null) {
                    direccion = new Direccion(
                            direccionDoc.getString("calle"),
                            direccionDoc.getString("ciudad"),
                            direccionDoc.getString("cp")
                    );
                }

                Empleado empleado = new Empleado(
                        doc.getObjectId("_id").toString(),
                        doc.getString("nombre"),
                        doc.getString("apellidos"),
                        doc.getString("contacto"),
                        doc.getString("genero"),
                        doc.getString("departamento"),
                        direccion
                );
                tempList.add(empleado);
                System.out.println("Empleado cargado: " + empleado.getNombre()); // Log para debug
            }

            // Actualizar la UI en el hilo de JavaFX
            Platform.runLater(() -> {
                listaEmpleados.clear();
                listaEmpleados.addAll(tempList);
                tablaEmpleados.setItems(listaEmpleados);
                tablaEmpleados.refresh();
                System.out.println("Tabla actualizada con " + listaEmpleados.size() + " empleados");
            });

        } catch (Exception e) {
            e.printStackTrace();
            Platform.runLater(() -> {
                mostrarAlerta("Error", "Error al cargar datos: " + e.getMessage(), Alert.AlertType.ERROR);
            });
        }
    }

    private void mostrarDetallesEmpleado(Empleado empleado) {
        txtNombre.setText(empleado.getNombre());
        txtApellidos.setText(empleado.getApellidos());
        txtContacto.setText(empleado.getContacto());
        txtGenero.setText(empleado.getGenero());
        txtDepartamento.setText(empleado.getDepartamento());
        if (empleado.getDireccion() != null) {
            txtCalle.setText(empleado.getDireccion().getCalle());
            txtCiudad.setText(empleado.getDireccion().getCiudad());
            txtCP.setText(empleado.getDireccion().getCp());
        } else {
            txtCalle.clear();
            txtCiudad.clear();
            txtCP.clear();
        }
    }

@FXML
private void modificarEmpleado() {
    try {
        Empleado empleadoSeleccionado = tablaEmpleados.getSelectionModel().getSelectedItem();
        if (empleadoSeleccionado != null && validarCampos()) {
            // Crear subdocumento de dirección actualizado
            Document direccionDoc = new Document()
                    .append("calle", txtCalle.getText())
                    .append("ciudad", txtCiudad.getText())
                    .append("cp", txtCP.getText());

            // Actualizar en MongoDB incluyendo la dirección
            collection.updateOne(
                    Filters.eq("_id", new ObjectId(empleadoSeleccionado.getId())),
                    Updates.combine(
                            Updates.set("nombre", txtNombre.getText()),
                            Updates.set("apellidos", txtApellidos.getText()),
                            Updates.set("contacto", txtContacto.getText()),
                            Updates.set("genero", txtGenero.getText()),
                            Updates.set("departamento", txtDepartamento.getText()),
                            Updates.set("direccion", direccionDoc)
                    )
            );

            // Recargar datos y actualizar UI
            cargarDatos();
            limpiarCampos();
            mostrarAlerta("Éxito", "Empleado modificado correctamente", Alert.AlertType.INFORMATION);
        }
    } catch (Exception e) {
        e.printStackTrace();
        mostrarAlerta("Error", "Error al modificar empleado: " + e.getMessage(), Alert.AlertType.ERROR);
    }
}

    @FXML
    private void eliminarEmpleado() {
        Empleado empleadoSeleccionado = tablaEmpleados.getSelectionModel().getSelectedItem();
        if (empleadoSeleccionado != null) {
            Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
            confirmacion.setTitle("Confirmar eliminación");
            confirmacion.setContentText("¿Está seguro de eliminar este empleado?");

            confirmacion.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    try {
                        // Eliminar de MongoDB
                        collection.deleteOne(Filters.eq("_id", new ObjectId(empleadoSeleccionado.getId())));

                        // Eliminar de la lista y actualizar UI
                        Platform.runLater(() -> {
                            listaEmpleados.remove(empleadoSeleccionado);
                            tablaEmpleados.refresh();
                            limpiarCampos();
                            mostrarAlerta("Éxito", "Empleado eliminado correctamente", Alert.AlertType.INFORMATION);
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        mostrarAlerta("Error", "Error al eliminar empleado: " + e.getMessage(), Alert.AlertType.ERROR);
                    }
                }
            });
        }
    }

    // Métodos de validación
    private boolean soloLetras(String texto) {
        return texto.matches("[a-zA-Z\\s]+");
    }

    private boolean soloNumeros(String texto) {
        return texto.matches("\\d+");
    }

    private boolean sinCaracteresEspeciales(String texto) {
        return texto.matches("[a-zA-Z0-9\\s]+");
    }

    // Modifica validarCampos()
    private boolean validarCampos() {
        if (txtNombre.getText().isEmpty() || txtApellidos.getText().isEmpty() ||
                txtContacto.getText().isEmpty() || txtGenero.getText().isEmpty() ||
                txtDepartamento.getText().isEmpty() || txtCalle.getText().isEmpty() ||
                txtCiudad.getText().isEmpty() || txtCP.getText().isEmpty()) {
            mostrarAlerta("Error", "Todos los campos son obligatorios", Alert.AlertType.WARNING);
            return false;
        }

        if (!soloLetras(txtNombre.getText())) {
            mostrarAlerta("Error", "El nombre solo debe contener letras y espacios, sin acentos", Alert.AlertType.WARNING);
            return false;
        }
        if (!soloLetras(txtApellidos.getText())) {
            mostrarAlerta("Error", "Los apellidos solo deben contener letras y espacios", Alert.AlertType.WARNING);
            return false;
        }
        if (!soloLetras(txtCiudad.getText())) {
            mostrarAlerta("Error", "La ciudad solo debe contener letras y espacios", Alert.AlertType.WARNING);
            return false;
        }
        if (!soloLetras(txtCalle.getText())) {
            mostrarAlerta("Error", "La calle solo debe contener letras y espacios", Alert.AlertType.WARNING);
            return false;
        }
        if (!soloLetras(txtGenero.getText())) {
            mostrarAlerta("Error", "El género solo debe contener letras y espacios", Alert.AlertType.WARNING);
            return false;
        }
        if (!txtContacto.getText().matches("\\d{10,}")) {
            mostrarAlerta("Error", "El contacto debe contener solo números y al menos 10 dígitos", Alert.AlertType.WARNING);
            return false;
        }

        if (!soloNumeros(txtCP.getText())) {
            mostrarAlerta("Error", "El código postal solo debe contener números", Alert.AlertType.WARNING);
            return false;
        }

        // Validar que no haya caracteres especiales en ningún campo
        if (!sinCaracteresEspeciales(txtNombre.getText()) ||
                !sinCaracteresEspeciales(txtApellidos.getText()) ||
                !sinCaracteresEspeciales(txtCiudad.getText()) ||
                !sinCaracteresEspeciales(txtCalle.getText()) ||
                !sinCaracteresEspeciales(txtGenero.getText()) ||
                !sinCaracteresEspeciales(txtContacto.getText()) ||
                !sinCaracteresEspeciales(txtCP.getText())) {
            mostrarAlerta("Error", "No se permiten caracteres especiales", Alert.AlertType.WARNING);
            return false;
        }

        return true;
    }

    private void limpiarCampos() {
        txtNombre.clear();
        txtApellidos.clear();
        txtContacto.clear();
        txtGenero.clear();
        txtDepartamento.clear();
        tablaEmpleados.getSelectionModel().clearSelection();
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
    @FXML
    private void respaldarProductos() {
        try {
            String ruta = "respaldo_productos.json";
            ServicioRespaldo.respaldarColeccion(collection, ruta);
            mostrarAlerta("Éxito", "Respaldo guardado en: " + ruta, Alert.AlertType.INFORMATION);
        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo hacer el respaldo: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

}