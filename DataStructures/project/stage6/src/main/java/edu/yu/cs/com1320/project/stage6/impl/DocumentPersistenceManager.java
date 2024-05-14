package edu.yu.cs.com1320.project.stage6.impl;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import edu.yu.cs.com1320.project.stage6.Document;
import edu.yu.cs.com1320.project.stage6.PersistenceManager;


import java.io.*;
import java.lang.reflect.Type;
import java.net.URI;
import java.nio.file.*;

public class DocumentPersistenceManager implements PersistenceManager<URI, Document>{
    CustomSerialization customSerialization = new CustomSerialization();
    private final File directory;

    public DocumentPersistenceManager(File directory){
        this.directory = directory;
    }

    private static class CustomSerialization implements JsonSerializer<Document>, JsonDeserializer<Document> {

        public JsonElement serialize(Document src, Type typeOfSrc, JsonSerializationContext context) {
            Gson gson = new Gson();
            JsonObject jsonObject = new JsonObject();

            JsonElement documentJson = gson.toJsonTree(src);
            jsonObject.add("document", documentJson);

            return jsonObject;
        }

        public Document deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
            Gson gson = new Gson();
            JsonObject jsonObject = json.getAsJsonObject();
            Type documentType = new TypeToken<DocumentImpl>(){}.getType();
            JsonElement documentJson = jsonObject.get("document");
            return gson.fromJson(documentJson, documentType);
        }
    }

    public void serialize(URI key, Document val) throws IOException{
        createJsonFile(key, customSerialization.serialize(val, null, null));
    }

    public Document deserialize(URI key) throws IOException{
        Document document = customSerialization.deserialize(getJsonFile(key), null, null);
        delete(key);
        return document;
    }

    /**
     * delete the file stored on disk that corresponds to the given key
     * @param key;
     * @return true or false to indicate if deletion occurred or not
     * @throws IOException;
     */
    public boolean delete(URI key) throws IOException{
        File doomedFile = new File(getDirectoryPath(key) + getJsonPath(key));
        boolean wasFileDeleted = doomedFile.delete();
        deleteEmptyParentDirectories(doomedFile.toPath().getParent());
        return wasFileDeleted;
    }

    private void deleteEmptyParentDirectories(Path directory) throws IOException {
        if (directory == null || !Files.isDirectory(directory)) {
            return;
        }
        if (isEmpty(directory)) {
            Files.delete(directory);
            deleteEmptyParentDirectories(directory.getParent());
        }
    }

    private boolean isEmpty(Path directory) throws IOException {
        try (var stream = Files.newDirectoryStream(directory)) {
            return !stream.iterator().hasNext();
        }
    }

    private void createJsonFile(URI key, JsonElement jsonElement){
        String directoryPath = getDirectoryPath(key);

        try {
            Files.createDirectories(Paths.get(directoryPath));
        } catch (IOException e) {
            throw new RuntimeException("couldn't create file path");
        }

        try (FileWriter writer = new FileWriter(getDirectoryPath(key) + getJsonPath(key))) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(jsonElement, writer);

        } catch (IOException e) {
            throw new RuntimeException();
        }
    }

    private JsonElement getJsonFile(URI key) throws IOException {
        try {
            FileReader reader = new FileReader(getDirectoryPath(key) + getJsonPath(key));
            JsonElement jsonElement = JsonParser.parseReader(reader);
            reader.close();
            return jsonElement;
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }

    private String getDirectoryPath(URI key){
        String[] parts = key.toString().split("/");
        String filePath = "";
        for (int i = 1; i < parts.length - 1; i++){
            filePath += parts[i] + "/";
        }
        return directory + "/" + filePath;
    }

    private String getJsonPath(URI key){
        String[] parts = key.toString().split("/");
        String lastWord = parts[parts.length - 1];

        return lastWord + ".json";
    }
}