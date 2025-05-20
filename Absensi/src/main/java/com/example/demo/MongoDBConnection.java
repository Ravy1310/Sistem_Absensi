package com.example.demo;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

public class MongoDBConnection {
    private static MongoClient mongoClient;
    private static MongoDatabase database;

    // Fungsi untuk mendapatkan koneksi ke database
    public static MongoDatabase getDatabase() {
        // Connection string ke MongoDB Atlas
        String uri = "mongodb+srv://rafi:rafi1310@cluster0.wraubz9.mongodb.net/?retryWrites=true&w=majority&appName=Cluster0";

        
        // Membuat MongoClient dengan URI
        if (mongoClient == null) {
            mongoClient = MongoClients.create(uri);
        }
        
        // Mendapatkan database
        if (database == null) {
            database = mongoClient.getDatabase("bicopi"); // Ganti dengan nama database kamu
        }
        
        return database;
    }
}