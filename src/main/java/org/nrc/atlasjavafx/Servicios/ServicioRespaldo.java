package org.nrc.atlasjavafx.Servicios;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import org.bson.Document;

import java.io.FileWriter;
import java.io.IOException;

public class ServicioRespaldo {

    public static void respaldarColeccion(MongoCollection<Document> coleccion, String rutaArchivo) throws IOException {
        FindIterable<Document> documentos = coleccion.find();
        FileWriter escritor = new FileWriter(rutaArchivo);

        for (Document doc : documentos) {
            escritor.write(doc.toJson() + "\n");
        }

        escritor.close();
    }
}
