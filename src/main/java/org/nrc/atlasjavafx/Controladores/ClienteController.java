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
import org.nrc.atlasjavafx.Bean.Direccion;
import org.nrc.atlasjavafx.Servicios.ServicioRespaldo;


import java.io.IOException;
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
    @FXML private TableColumn<Cliente, String> colCalle;
    @FXML private TableColumn<Cliente, String> colCiudad;
    @FXML private TableColumn<Cliente, String> colCP;

    @FXML private TextField txtNombre;
    @FXML private TextField txtApellidos;
    @FXML private TextField txtEdad;
    @FXML private TextField txtGenero;
    @FXML private TextField txtContacto;

    @FXML private TextField txtCalle;
    @FXML private TextField txtCiudad;
    @FXML private TextField txtCP;

    private static final String MONGO_URI = "mongodb+srv://reynacancioneru:Neru2275@basenube.2av5n18.mongodb.net/?retryWrites=true&w=majority&appName=BaseNube";
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
            // Establecer explícitamente el ancho de las columnas
            colId.setPrefWidth(75);
            colNombre.setPrefWidth(182);
            colApellidos.setPrefWidth(123);
            colEdad.setPrefWidth(98);
            colGenero.setPrefWidth(147);
            colContacto.setPrefWidth(193);
            colCalle.setPrefWidth(150);
            colCiudad.setPrefWidth(150);
            colCP.setPrefWidth(100);

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
                // Crear el subdocumento de dirección
                Document direccionDoc = new Document()
                        .append("calle", txtCalle.getText())
                        .append("ciudad", txtCiudad.getText())
                        .append("cp", txtCP.getText());

                Document doc = new Document()
                        .append("nombre", txtNombre.getText())
                        .append("apellidos", txtApellidos.getText())
                        .append("edad", Integer.parseInt(txtEdad.getText()))
                        .append("genero", txtGenero.getText())
                        .append("contacto", txtContacto.getText())
                        .append("direccion", direccionDoc);

                // Insertar en MongoDB
                collection.insertOne(doc);
                Direccion direccion = new Direccion(
                        txtCalle.getText(),
                        txtCiudad.getText(),
                        txtCP.getText()
                );

                // Crear nuevo objeto Cliente
                Cliente nuevoCliente = new Cliente(
                        doc.getObjectId("_id").toString(),
                        txtNombre.getText(),
                        txtApellidos.getText(),
                        Integer.parseInt(txtEdad.getText()),
                        txtGenero.getText(),
                        txtContacto.getText(),
                        direccion
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
                Document direccionDoc = doc.get("direccion", Document.class);
                Direccion direccion = null;
                if (direccionDoc != null) {
                    direccion = new Direccion(
                            direccionDoc.getString("calle"),
                            direccionDoc.getString("ciudad"),
                            direccionDoc.getString("cp")
                    );
                }
                Cliente cliente = new Cliente(
                        doc.getObjectId("_id").toString(),
                        doc.getString("nombre"),
                        doc.getString("apellidos"),
                        doc.getInteger("edad"),
                        doc.getString("genero"),
                        doc.getString("contacto"),
                        direccion
                );
                tempList.add(cliente);
                System.out.println("Cliente cargado: " + cliente.getNombre() + " " + cliente.getApellidos());
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
        if (cliente.getDireccion() != null) {
            txtCalle.setText(cliente.getDireccion().getCalle());
            txtCiudad.setText(cliente.getDireccion().getCiudad());
            txtCP.setText(cliente.getDireccion().getCp());
        } else {
            txtCalle.clear();
            txtCiudad.clear();
            txtCP.clear();
        }
    }

    @FXML
    private void modificarCliente() {
        try {
            Cliente clienteSeleccionado = tablaClientes.getSelectionModel().getSelectedItem();
            if (clienteSeleccionado != null && validarCampos()) {
                Document direccionDoc = new Document()
                        .append("calle", txtCalle.getText())
                        .append("ciudad", txtCiudad.getText())
                        .append("cp", txtCP.getText());

                collection.updateOne(
                        Filters.eq("_id", new ObjectId(clienteSeleccionado.getId())),
                        Updates.combine(
                                Updates.set("nombre", txtNombre.getText()),
                                Updates.set("apellidos", txtApellidos.getText()),
                                Updates.set("edad", Integer.parseInt(txtEdad.getText())),
                                Updates.set("genero", txtGenero.getText()),
                                Updates.set("contacto", txtContacto.getText()),
                                Updates.set("direccion", direccionDoc)
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

    // Solo letras (sin acentos ni diéresis) y espacios
    private boolean soloLetras(String texto) {
        return texto.matches("[a-zA-Z\\s]+");
    }

    // Solo números
    private boolean soloNumeros(String texto) {
        return texto.matches("\\d+");
    }

    // Sin caracteres especiales, solo letras, números y espacios (sin acentos ni diéresis)
    private boolean sinCaracteresEspeciales(String texto) {
        return texto.matches("[a-zA-Z0-9\\s]+");
    }

    // Modifica validarCampos()
    private boolean validarCampos() {
        if (txtNombre.getText().isEmpty() || txtApellidos.getText().isEmpty() ||
                txtEdad.getText().isEmpty() || txtGenero.getText().isEmpty() ||
                txtContacto.getText().isEmpty() || txtCalle.getText().isEmpty() ||
                txtCiudad.getText().isEmpty() || txtCP.getText().isEmpty()) {
            mostrarAlerta("Error", "Todos los campos son obligatorios", Alert.AlertType.WARNING);
            return false;
        }

        if (!soloLetras(txtNombre.getText())) {
            mostrarAlerta("Error", "El nombre solo debe contener letras y espacios, sin acentos ", Alert.AlertType.WARNING);
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
        if (!soloNumeros(txtEdad.getText())) {
            mostrarAlerta("Error", "La edad solo debe contener números", Alert.AlertType.WARNING);
            return false;
        }
        int edad = Integer.parseInt(txtEdad.getText());
        if (edad < 1 || edad > 99) {
            mostrarAlerta("Error", "La edad debe estar entre 1 y 99 años", Alert.AlertType.WARNING);
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
        txtEdad.clear();
        txtGenero.clear();
        txtContacto.clear();
        tablaClientes.getSelectionModel().clearSelection();
        txtCalle.clear();
        txtCiudad.clear();
        txtCP.clear();
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