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
import org.nrc.atlasjavafx.Bean.Producto;
import org.nrc.atlasjavafx.Servicios.ServicioRespaldo;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class ProductoController implements Initializable {

    @FXML private TableView<Producto> tablaProductos;
    @FXML private TableColumn<Producto, String> colId;
    @FXML private TableColumn<Producto, String> colNombre;
    @FXML private TableColumn<Producto, String> colPrecio;
    @FXML private TableColumn<Producto, String> colStock;
    @FXML private TableColumn<Producto, String> colCategoria;

    @FXML private TextField txtNombre;
    @FXML private TextField txtPrecio;
    @FXML private TextField txtStock;
    @FXML private TextField txtCategoria;

    private static final String MONGO_URI = "mongodb+srv://reynacancioneru:Neru2275@basenube.2av5n18.mongodb.net/?retryWrites=true&w=majority&appName=BaseNube";
    private static final String DATABASE_NAME = "Punto_Venta";
    private static final String COLLECTION_NAME = "Productos";

    private MongoClient mongoClient;
    private MongoDatabase database;
    private MongoCollection<Document> collection;
    private ObservableList<Producto> listaProductos;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            // Inicializar MongoDB
            mongoClient = MongoClients.create(MONGO_URI);
            database = mongoClient.getDatabase(DATABASE_NAME);
            collection = database.getCollection(COLLECTION_NAME);

            // Inicializar la lista observable
            listaProductos = FXCollections.observableArrayList();

            // Configurar las columnas
            colId.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getId()));
            colNombre.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getNombre()));
            colPrecio.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getPrecio()));
            colStock.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getStock()));
            colCategoria.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getCategoria()));

            // Establecer ancho de columnas según el FXML
            colId.setPrefWidth(75.0);
            colNombre.setPrefWidth(182.0);
            colPrecio.setPrefWidth(123.0);
            colStock.setPrefWidth(98.0);
            colCategoria.setPrefWidth(147.0);

            // Asignar la lista observable a la tabla
            tablaProductos.setItems(listaProductos);

            // Agregar listener de selección
            tablaProductos.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    mostrarDetallesProducto(newSelection);
                }
            });

            // Cargar datos iniciales
            cargarDatos();

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "Error al inicializar: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void agregarProducto() {
        try {
            if (validarCampos()) {
                // Crear nuevo documento
                Document doc = new Document()
                        .append("nombre", txtNombre.getText())
                        .append("precio", txtPrecio.getText())
                        .append("stock", txtStock.getText())
                        .append("categoria", txtCategoria.getText());

                // Insertar en MongoDB
                collection.insertOne(doc);

                // Crear nuevo objeto Producto
                Producto nuevoProducto = new Producto(
                        doc.getObjectId("_id").toString(),
                        txtNombre.getText(),
                        txtPrecio.getText(),
                        txtStock.getText(),
                        txtCategoria.getText()
                );

                // Actualizar la UI en el hilo de JavaFX
                Platform.runLater(() -> {
                    listaProductos.add(nuevoProducto);
                    tablaProductos.refresh();
                    limpiarCampos();
                    mostrarAlerta("Éxito", "Producto agregado correctamente", Alert.AlertType.INFORMATION);
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "Error al agregar producto: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void cargarDatos() {
        try {
            // Realizar la consulta a MongoDB
            FindIterable<Document> documents = collection.find();

            // Crear una lista temporal
            ObservableList<Producto> tempList = FXCollections.observableArrayList();

            // Poblar la lista temporal
            for (Document doc : documents) {
                Producto producto = new Producto(
                        doc.getObjectId("_id").toString(),
                        doc.getString("nombre"),
                        doc.getString("precio"),
                        doc.getString("stock"),
                        doc.getString("categoria")
                );
                tempList.add(producto);
            }

            // Actualizar la UI en el hilo de JavaFX
            Platform.runLater(() -> {
                listaProductos.clear();
                listaProductos.addAll(tempList);
                tablaProductos.setItems(listaProductos);
                tablaProductos.refresh();
            });

        } catch (Exception e) {
            e.printStackTrace();
            Platform.runLater(() -> {
                mostrarAlerta("Error", "Error al cargar datos: " + e.getMessage(), Alert.AlertType.ERROR);
            });
        }
    }

    private void mostrarDetallesProducto(Producto producto) {
        txtNombre.setText(producto.getNombre());
        txtPrecio.setText(producto.getPrecio());
        txtStock.setText(producto.getStock());
        txtCategoria.setText(producto.getCategoria());
    }

    @FXML
    private void modificarProducto() {
        try {
            Producto productoSeleccionado = tablaProductos.getSelectionModel().getSelectedItem();
            if (productoSeleccionado != null && validarCampos()) {
                // Actualizar en MongoDB
                collection.updateOne(
                        Filters.eq("_id", new ObjectId(productoSeleccionado.getId())),
                        Updates.combine(
                                Updates.set("nombre", txtNombre.getText()),
                                Updates.set("precio", txtPrecio.getText()),
                                Updates.set("stock", txtStock.getText()),
                                Updates.set("categoria", txtCategoria.getText())
                        )
                );

                // Recargar datos y actualizar UI
                cargarDatos();
                limpiarCampos();
                mostrarAlerta("Éxito", "Producto modificado correctamente", Alert.AlertType.INFORMATION);
            }
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "Error al modificar producto: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void eliminarProducto() {
        Producto productoSeleccionado = tablaProductos.getSelectionModel().getSelectedItem();
        if (productoSeleccionado != null) {
            Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
            confirmacion.setTitle("Confirmar eliminación");
            confirmacion.setContentText("¿Está seguro de eliminar este producto?");

            confirmacion.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    try {
                        // Eliminar de MongoDB
                        collection.deleteOne(Filters.eq("_id", new ObjectId(productoSeleccionado.getId())));

                        // Eliminar de la lista y actualizar UI
                        Platform.runLater(() -> {
                            listaProductos.remove(productoSeleccionado);
                            tablaProductos.refresh();
                            limpiarCampos();
                            mostrarAlerta("Éxito", "Producto eliminado correctamente", Alert.AlertType.INFORMATION);
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        mostrarAlerta("Error", "Error al eliminar producto: " + e.getMessage(), Alert.AlertType.ERROR);
                    }
                }
            });
        }
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

    private boolean soloLetras(String texto) {
        return texto.matches("[a-zA-Z\\s]+");
    }

    private boolean soloNumeros(String texto) {
        return texto.matches("\\d+");
    }

    private boolean sinCaracteresEspeciales(String texto) {
        return texto.matches("[a-zA-Z0-9\\s]+");
    }

    private boolean validarCampos() {
        if (txtNombre.getText().isEmpty() || txtPrecio.getText().isEmpty() ||
                txtStock.getText().isEmpty() || txtCategoria.getText().isEmpty()) {
            mostrarAlerta("Error", "Todos los campos son obligatorios", Alert.AlertType.WARNING);
            return false;
        }

        if (!soloLetras(txtNombre.getText())) {
            mostrarAlerta("Error", "El nombre solo debe contener letras y espacios, sin acentos", Alert.AlertType.WARNING);
            return false;
        }
        if (!soloLetras(txtCategoria.getText())) {
            mostrarAlerta("Error", "La categoría solo debe contener letras y espacios, sin acentos", Alert.AlertType.WARNING);
            return false;
        }
        if (!soloNumeros(txtPrecio.getText())) {
            mostrarAlerta("Error", "El precio solo debe contener números", Alert.AlertType.WARNING);
            return false;
        }
        if (!soloNumeros(txtStock.getText())) {
            mostrarAlerta("Error", "El stock solo debe contener números", Alert.AlertType.WARNING);
            return false;
        }

        // Validar que no haya caracteres especiales en ningún campo
        if (!sinCaracteresEspeciales(txtNombre.getText()) ||
                !sinCaracteresEspeciales(txtPrecio.getText()) ||
                !sinCaracteresEspeciales(txtStock.getText()) ||
                !sinCaracteresEspeciales(txtCategoria.getText())) {
            mostrarAlerta("Error", "No se permiten caracteres especiales", Alert.AlertType.WARNING);
            return false;
        }

        return true;
    }

    private void limpiarCampos() {
        txtNombre.clear();
        txtPrecio.clear();
        txtStock.clear();
        txtCategoria.clear();
        tablaProductos.getSelectionModel().clearSelection();
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}