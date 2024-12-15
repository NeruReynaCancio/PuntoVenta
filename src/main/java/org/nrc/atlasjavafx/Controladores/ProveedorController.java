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
import org.nrc.atlasjavafx.Bean.Proveedor;

import java.net.URL;
import java.util.ResourceBundle;

public class ProveedorController implements Initializable {

    @FXML private TableView<Proveedor> tablaProveedores;
    @FXML private TableColumn<Proveedor, String> colId;
    @FXML private TableColumn<Proveedor, String> colNombre;
    @FXML private TableColumn<Proveedor, String> colArea;
    @FXML private TableColumn<Proveedor, String> colContacto;
    @FXML private TableColumn<Proveedor, String> colDireccion;
    @FXML private TableColumn<Proveedor, String> colAntiguedad;

    @FXML private TextField txtNombre;
    @FXML private TextField txtArea;
    @FXML private TextField txtContacto;
    @FXML private TextField txtDireccion;
    @FXML private TextField txtAntiguedad;

    private static final String MONGO_URI = "mongodb+srv://reynacancioneru:Neru2275@bda.4jnr8.mongodb.net/?retryWrites=true&w=majority&appName=BDA";
    private static final String DATABASE_NAME = "Punto_Venta";
    private static final String COLLECTION_NAME = "Proveedores";

    private MongoClient mongoClient;
    private MongoDatabase database;
    private MongoCollection<Document> collection;
    private ObservableList<Proveedor> listaProveedores;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            // Inicializar MongoDB
            mongoClient = MongoClients.create(MONGO_URI);
            database = mongoClient.getDatabase(DATABASE_NAME);
            collection = database.getCollection(COLLECTION_NAME);

            // Inicializar la lista observable
            listaProveedores = FXCollections.observableArrayList();

            // Configurar las columnas
            colId.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getId()));
            colNombre.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getNombre()));
            colArea.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getArea()));
            colContacto.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getContacto()));
            colDireccion.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getDireccion()));
            colAntiguedad.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getAntiguedad()));

            // Establecer ancho de columnas
            colId.setPrefWidth(75);
            colNombre.setPrefWidth(182);
            colArea.setPrefWidth(123);
            colContacto.setPrefWidth(98);
            colDireccion.setPrefWidth(147);
            colAntiguedad.setPrefWidth(193);

            // Asignar la lista observable a la tabla
            tablaProveedores.setItems(listaProveedores);

            // Agregar listener de selección
            tablaProveedores.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    mostrarDetallesProveedor(newSelection);
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
    private void agregarProveedor() {
        try {
            if (validarCampos()) {
                // Crear nuevo documento
                Document doc = new Document()
                        .append("nombre", txtNombre.getText())
                        .append("area", txtArea.getText())
                        .append("contacto", txtContacto.getText())
                        .append("direccion", txtDireccion.getText())
                        .append("antiguedad", txtAntiguedad.getText());

                // Insertar en MongoDB
                collection.insertOne(doc);

                // Crear nuevo objeto Proveedor
                Proveedor nuevoProveedor = new Proveedor(
                        doc.getObjectId("_id").toString(),
                        txtNombre.getText(),
                        txtArea.getText(),
                        txtContacto.getText(),
                        txtDireccion.getText(),
                        txtAntiguedad.getText()
                );

                // Actualizar la UI en el hilo de JavaFX
                Platform.runLater(() -> {
                    listaProveedores.add(nuevoProveedor);
                    tablaProveedores.refresh();
                    limpiarCampos();
                    mostrarAlerta("Éxito", "Proveedor agregado correctamente", Alert.AlertType.INFORMATION);
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "Error al agregar proveedor: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void cargarDatos() {
        try {
            // Realizar la consulta a MongoDB
            FindIterable<Document> documents = collection.find();

            // Crear una lista temporal
            ObservableList<Proveedor> tempList = FXCollections.observableArrayList();

            // Poblar la lista temporal
            for (Document doc : documents) {
                Proveedor proveedor = new Proveedor(
                        doc.getObjectId("_id").toString(),
                        doc.getString("nombre"),
                        doc.getString("area"),
                        doc.getString("contacto"),
                        doc.getString("direccion"),
                        doc.getString("antiguedad")
                );
                tempList.add(proveedor);
                System.out.println("Proveedor cargado: " + proveedor.getNombre()); // Log para debug
            }

            // Actualizar la UI en el hilo de JavaFX
            Platform.runLater(() -> {
                listaProveedores.clear();
                listaProveedores.addAll(tempList);
                tablaProveedores.setItems(listaProveedores);
                tablaProveedores.refresh();
                System.out.println("Tabla actualizada con " + listaProveedores.size() + " proveedores");
            });

        } catch (Exception e) {
            e.printStackTrace();
            Platform.runLater(() -> {
                mostrarAlerta("Error", "Error al cargar datos: " + e.getMessage(), Alert.AlertType.ERROR);
            });
        }
    }

    private void mostrarDetallesProveedor(Proveedor proveedor) {
        txtNombre.setText(proveedor.getNombre());
        txtArea.setText(proveedor.getArea());
        txtContacto.setText(proveedor.getContacto());
        txtDireccion.setText(proveedor.getDireccion());
        txtAntiguedad.setText(proveedor.getAntiguedad());
    }

    @FXML
    private void modificarProveedor() {
        try {
            Proveedor proveedorSeleccionado = tablaProveedores.getSelectionModel().getSelectedItem();
            if (proveedorSeleccionado != null && validarCampos()) {
                // Actualizar en MongoDB
                collection.updateOne(
                        Filters.eq("_id", new ObjectId(proveedorSeleccionado.getId())),
                        Updates.combine(
                                Updates.set("nombre", txtNombre.getText()),
                                Updates.set("area", txtArea.getText()),
                                Updates.set("contacto", txtContacto.getText()),
                                Updates.set("direccion", txtDireccion.getText()),
                                Updates.set("antiguedad", txtAntiguedad.getText())
                        )
                );

                // Recargar datos y actualizar UI
                cargarDatos();
                limpiarCampos();
                mostrarAlerta("Éxito", "Proveedor modificado correctamente", Alert.AlertType.INFORMATION);
            }
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "Error al modificar proveedor: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void eliminarProveedor() {
        Proveedor proveedorSeleccionado = tablaProveedores.getSelectionModel().getSelectedItem();
        if (proveedorSeleccionado != null) {
            Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
            confirmacion.setTitle("Confirmar eliminación");
            confirmacion.setContentText("¿Está seguro de eliminar este proveedor?");

            confirmacion.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    try {
                        // Eliminar de MongoDB
                        collection.deleteOne(Filters.eq("_id", new ObjectId(proveedorSeleccionado.getId())));

                        // Eliminar de la lista y actualizar UI
                        Platform.runLater(() -> {
                            listaProveedores.remove(proveedorSeleccionado);
                            tablaProveedores.refresh();
                            limpiarCampos();
                            mostrarAlerta("Éxito", "Proveedor eliminado correctamente", Alert.AlertType.INFORMATION);
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        mostrarAlerta("Error", "Error al eliminar proveedor: " + e.getMessage(), Alert.AlertType.ERROR);
                    }
                }
            });
        }
    }

    private boolean validarCampos() {
        if (txtNombre.getText().isEmpty() || txtArea.getText().isEmpty() ||
                txtContacto.getText().isEmpty() || txtDireccion.getText().isEmpty() ||
                txtAntiguedad.getText().isEmpty()) {
            mostrarAlerta("Error", "Todos los campos son obligatorios", Alert.AlertType.WARNING);
            return false;
        }
        return true;
    }

    private void limpiarCampos() {
        txtNombre.clear();
        txtArea.clear();
        txtContacto.clear();
        txtDireccion.clear();
        txtAntiguedad.clear();
        tablaProveedores.getSelectionModel().clearSelection();
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}