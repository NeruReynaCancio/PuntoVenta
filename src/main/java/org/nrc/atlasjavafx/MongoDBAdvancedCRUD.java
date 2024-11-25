package org.nrc.atlasjavafx;

import com.mongodb.client.*;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.Arrays;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Projections.*;
import static com.mongodb.client.model.Updates.set;

public class MongoDBAdvancedCRUD {
    private static final String CONNECTION_STRING = "mongodb+srv://reynacancioneru:Neru2275@bda.4jnr8.mongodb.net/?retryWrites=true&w=majority&appName=BDA";
    private static final String DATABASE_NAME = "testBD";
    private static final String COLLECTION_NAME = "testCollection";

    public static void main(String[] args) {
        try (MongoClient mongoClient = MongoClients.create(CONNECTION_STRING)) {
            MongoDatabase database = mongoClient.getDatabase(DATABASE_NAME);
            MongoCollection<Document> collection = database.getCollection(COLLECTION_NAME);

            // Consulta documentos con filtros
           findWithFilter(collection);

            // Insertar documento
            //insertDocument(collection);

            // Actualizar varios documentos
            //updateMultipleDocuments(collection);

            // Eliminar documentos
            //deleteDocument(collection);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void findWithFilter(MongoCollection<Document> collection) {
        System.out.println("Documentos con skills que incluyen 'Java':");
        Bson filter = eq("skills", "Java");
        Bson projection = fields(include("name", "skills"), excludeId());
        try (MongoCursor<Document> cursor = collection.find(filter).projection(projection).iterator()) {
            while (cursor.hasNext()) {
                System.out.println(cursor.next().toJson());
            }
        }
    }

    private static void insertDocument(MongoCollection<Document> collection) {
        Document newDoc = new Document("name", "Charlie Brown")
                .append("age", 40)
                .append("city", "Los Angeles")
                .append("skills", Arrays.asList("Kotlin", "Spring Boot"))
                .append("isEmployed", true)
                .append("projects", Arrays.asList(
                        new Document("name", "Mobile App Development").append("year", 2024)
                ));
        collection.insertOne(newDoc);
        System.out.println("Nuevo documento insertado: " + newDoc.toJson());
    }

    private static void updateMultipleDocuments(MongoCollection<Document> collection) {
        Bson filter = eq("isEmployed", false);
        Bson update = set("isEmployed", true);
        collection.updateMany(filter, update);
        System.out.println("Documentos actualizados: Empleo marcado como verdadero para todos.");
    }

    private static void deleteDocument(MongoCollection<Document> collection) {
        Bson filter = eq("name", "Charlie Brown");
        collection.deleteOne(filter);
        System.out.println("Documento eliminado con nombre 'Charlie Brown'.");
    }
}
