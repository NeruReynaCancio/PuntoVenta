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
import org.nrc.atlasjavafx.Bean.Empleado;

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

    @FXML private TextField txtNombre;
    @FXML private TextField txtApellidos;
    @FXML private TextField txtContacto;
    @FXML private TextField txtGenero;
    @FXML private TextField txtDepartamento;

    private static final String MONGO_URI = "mongodb+srv://reynacancioneru:Neru2275@bda.4jnr8.mongodb.net/?retryWrites=true&w=majority&appName=BDA";
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
                // Crear nuevo documento
                Document doc = new Document()
                        .append("nombre", txtNombre.getText())
                        .append("apellidos", txtApellidos.getText())
                        .append("contacto", txtContacto.getText())
                        .append("genero", txtGenero.getText())
                        .append("departamento", txtDepartamento.getText());

                // Insertar en MongoDB
                collection.insertOne(doc);

                // Crear nuevo objeto Empleado
                Empleado nuevoEmpleado = new Empleado(
                        doc.getObjectId("_id").toString(),
                        txtNombre.getText(),
                        txtApellidos.getText(),
                        txtContacto.getText(),
                        txtGenero.getText(),
                        txtDepartamento.getText()
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
                Empleado empleado = new Empleado(
                        doc.getObjectId("_id").toString(),
                        doc.getString("nombre"),
                        doc.getString("apellidos"),
                        doc.getString("contacto"),
                        doc.getString("genero"),
                        doc.getString("departamento")
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
    }

    @FXML
    private void modificarEmpleado() {
        try {
            Empleado empleadoSeleccionado = tablaEmpleados.getSelectionModel().getSelectedItem();
            if (empleadoSeleccionado != null && validarCampos()) {
                // Actualizar en MongoDB
                collection.updateOne(
                        Filters.eq("_id", new ObjectId(empleadoSeleccionado.getId())),
                        Updates.combine(
                                Updates.set("nombre", txtNombre.getText()),
                                Updates.set("apellidos", txtApellidos.getText()),
                                Updates.set("contacto", txtContacto.getText()),
                                Updates.set("genero", txtGenero.getText()),
                                Updates.set("departamento", txtDepartamento.getText())
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

    private boolean validarCampos() {
        if (txtNombre.getText().isEmpty() || txtApellidos.getText().isEmpty() ||
                txtContacto.getText().isEmpty() || txtGenero.getText().isEmpty() ||
                txtDepartamento.getText().isEmpty()) {
            mostrarAlerta("Error", "Todos los campos son obligatorios", Alert.AlertType.WARNING);
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
}