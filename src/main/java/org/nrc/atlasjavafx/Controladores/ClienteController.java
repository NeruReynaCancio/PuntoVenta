package org.nrc.atlasjavafx.Controladores;

import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.application.Platform;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.nrc.atlasjavafx.Bean.Cliente;


import java.net.URL;
import java.util.ResourceBundle;

public class ClienteController implements Initializable {

    @FXML private TableView<Cliente> tablaClientes;
    @FXML private TableColumn<Cliente, String> colId;
    @FXML private TableColumn<Cliente, String> colNombre;
    @FXML private TableColumn<Cliente, String> colApellidos;
    @FXML private TableColumn<Cliente, Integer> colEdad;
    @FXML private TableColumn<Cliente, String> colGenero;
    @FXML private TableColumn<Cliente, String> colContacto;

    @FXML private TextField txtNombre;
    @FXML private TextField txtApellidos;
    @FXML private TextField txtEdad;
    @FXML private TextField txtGenero;
    @FXML private TextField txtContacto;

    private static final String MONGO_URI = "mongodb+srv://reynacancioneru:Neru2275@bda.4jnr8.mongodb.net/?retryWrites=true&w=majority&appName=BDA";
    private static final String DATABASE_NAME = "Punto_Venta";
    private static final String COLLECTION_NAME = "Cliente";

    private MongoClient mongoClient;
    private MongoDatabase database;
    private MongoCollection<Document> collection;
    private ObservableList<Cliente> listaClientes;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            // Inicializar MongoDB
            mongoClient = MongoClients.create(MONGO_URI);
            database = mongoClient.getDatabase(DATABASE_NAME);
            collection = database.getCollection(COLLECTION_NAME);

            // Inicializar la lista observable
            listaClientes = FXCollections.observableArrayList();

            // Configurar las columnas usando un enfoque más explícito
            colId.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getId()));

            colNombre.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getNombre()));

            colApellidos.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getApellidos()));

            colEdad.setCellValueFactory(cellData ->
                    new SimpleIntegerProperty(cellData.getValue().getEdad()).asObject());

            colGenero.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getGenero()));

            colContacto.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getContacto()));

            // Establecer explícitamente el ancho de las columnas
            colId.setPrefWidth(75);
            colNombre.setPrefWidth(182);
            colApellidos.setPrefWidth(123);
            colEdad.setPrefWidth(98);
            colGenero.setPrefWidth(147);
            colContacto.setPrefWidth(193);

            // Asignar la lista observable a la tabla
            tablaClientes.setItems(listaClientes);

            // Agregar el listener de selección
            tablaClientes.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    mostrarDetallesCliente(newSelection);
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
    private void agregarCliente() {
        try {
            if (validarCampos()) {
                // Crear nuevo documento
                Document doc = new Document()
                        .append("nombre", txtNombre.getText())
                        .append("apellidos", txtApellidos.getText())
                        .append("edad", Integer.parseInt(txtEdad.getText()))
                        .append("genero", txtGenero.getText())
                        .append("contacto", txtContacto.getText());

                // Insertar en MongoDB
                collection.insertOne(doc);

                // Crear nuevo objeto Cliente
                Cliente nuevoCliente = new Cliente(
                        doc.getObjectId("_id").toString(),
                        txtNombre.getText(),
                        txtApellidos.getText(),
                        Integer.parseInt(txtEdad.getText()),
                        txtGenero.getText(),
                        txtContacto.getText()
                );

                // Actualizar la UI en el hilo de JavaFX
                Platform.runLater(() -> {
                    listaClientes.add(nuevoCliente);
                    tablaClientes.refresh();
                    limpiarCampos();
                    mostrarAlerta("Éxito", "Cliente agregado correctamente", Alert.AlertType.INFORMATION);
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "Error al agregar cliente: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void cargarDatos() {
        try {
            // Realizar la consulta a MongoDB
            FindIterable<Document> documents = collection.find();

            // Crear una lista temporal
            ObservableList<Cliente> tempList = FXCollections.observableArrayList();

            // Poblar la lista temporal
            for (Document doc : documents) {
                Cliente cliente = new Cliente(
                        doc.getObjectId("_id").toString(),
                        doc.getString("nombre"),
                        doc.getString("apellidos"),
                        doc.getInteger("edad"),
                        doc.getString("genero"),
                        doc.getString("contacto")
                );
                tempList.add(cliente);
                System.out.println("Cliente cargado: " + cliente.getNombre()); // Log para debug
            }

            // Actualizar la UI en el hilo de JavaFX
            Platform.runLater(() -> {
                listaClientes.clear();
                listaClientes.addAll(tempList);
                tablaClientes.setItems(listaClientes);
                tablaClientes.refresh();
                System.out.println("Tabla actualizada con " + listaClientes.size() + " clientes");
            });

        } catch (Exception e) {
            e.printStackTrace();
            Platform.runLater(() -> {
                mostrarAlerta("Error", "Error al cargar datos: " + e.getMessage(), Alert.AlertType.ERROR);
            });
        }
    }

    private void mostrarDetallesCliente(Cliente cliente) {
        txtNombre.setText(cliente.getNombre());
        txtApellidos.setText(cliente.getApellidos());
        txtEdad.setText(String.valueOf(cliente.getEdad()));
        txtGenero.setText(cliente.getGenero());
        txtContacto.setText(cliente.getContacto());
    }

    @FXML
    private void modificarCliente() {
        try {
            Cliente clienteSeleccionado = tablaClientes.getSelectionModel().getSelectedItem();
            if (clienteSeleccionado != null && validarCampos()) {
                // Actualizar en MongoDB
                collection.updateOne(
                        Filters.eq("_id", new ObjectId(clienteSeleccionado.getId())),
                        Updates.combine(
                                Updates.set("nombre", txtNombre.getText()),
                                Updates.set("apellidos", txtApellidos.getText()),
                                Updates.set("edad", Integer.parseInt(txtEdad.getText())),
                                Updates.set("genero", txtGenero.getText()),
                                Updates.set("contacto", txtContacto.getText())
                        )
                );

                // Recargar datos y actualizar UI
                cargarDatos();
                limpiarCampos();
                mostrarAlerta("Éxito", "Cliente modificado correctamente", Alert.AlertType.INFORMATION);
            }
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "Error al modificar cliente: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void eliminarCliente() {
        Cliente clienteSeleccionado = tablaClientes.getSelectionModel().getSelectedItem();
        if (clienteSeleccionado != null) {
            Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
            confirmacion.setTitle("Confirmar eliminación");
            confirmacion.setContentText("¿Está seguro de eliminar este cliente?");

            confirmacion.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    try {
                        // Eliminar de MongoDB
                        collection.deleteOne(Filters.eq("_id", new ObjectId(clienteSeleccionado.getId())));

                        // Eliminar de la lista y actualizar UI
                        Platform.runLater(() -> {
                            listaClientes.remove(clienteSeleccionado);
                            tablaClientes.refresh();
                            limpiarCampos();
                            mostrarAlerta("Éxito", "Cliente eliminado correctamente", Alert.AlertType.INFORMATION);
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        mostrarAlerta("Error", "Error al eliminar cliente: " + e.getMessage(), Alert.AlertType.ERROR);
                    }
                }
            });
        }
    }

    private boolean validarCampos() {
        if (txtNombre.getText().isEmpty() || txtApellidos.getText().isEmpty() ||
                txtEdad.getText().isEmpty() || txtGenero.getText().isEmpty() ||
                txtContacto.getText().isEmpty()) {
            mostrarAlerta("Error", "Todos los campos son obligatorios", Alert.AlertType.WARNING);
            return false;
        }

        try {
            Integer.parseInt(txtEdad.getText());
        } catch (NumberFormatException e) {
            mostrarAlerta("Error", "La edad debe ser un número válido", Alert.AlertType.WARNING);
            return false;
        }

        return true;
    }

    private void limpiarCampos() {
        txtNombre.clear();
        txtApellidos.clear();
        txtEdad.clear();
        txtGenero.clear();
        txtContacto.clear();
        tablaClientes.getSelectionModel().clearSelection();
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}