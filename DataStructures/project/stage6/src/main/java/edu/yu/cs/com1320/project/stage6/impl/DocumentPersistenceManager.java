package edu.yu.cs.com1320.project.stage6.impl;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import edu.yu.cs.com1320.project.stage6.Document;
import edu.yu.cs.com1320.project.stage6.PersistenceManager;
import jakarta.xml.bind.DatatypeConverter;


import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

public class DocumentPersistenceManager implements PersistenceManager<URI, Document>{
    private final File directory;

    public DocumentPersistenceManager(File directory){
        this.directory = directory;
    }

    private static class CustomSerialization implements JsonSerializer<Document>, JsonDeserializer<Document> {

        public JsonElement serialize(Document src, Type typeOfSrc, JsonSerializationContext context) {
            Gson gson = new Gson();
            JsonObject jsonObject = new JsonObject();

            if (!(src.getDocumentTxt() == null)) {  //serialize either document text or document bytes
                String documentText = gson.toJson(src.getDocumentTxt());
                jsonObject.addProperty("documentText", documentText);
            } else {
                String documentByte = gson.toJson(DatatypeConverter.printBase64Binary(src.getDocumentBinaryData()));
                jsonObject.addProperty("documentByte", documentByte);
            }

            String metadata = gson.toJson(src.getMetadata());
            jsonObject.addProperty("metadata", metadata);

            String uri = gson.toJson(src.getKey().toString());
            jsonObject.addProperty("uri", uri);

            String wordMap = gson.toJson(src.getWordMap());
            jsonObject.addProperty("wordMap", wordMap);

            return jsonObject;
        }

        public Document deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonSyntaxException {
            Gson gson = new Gson();
            JsonObject jsonObject = json.getAsJsonObject();

            boolean textDocument = false;
            String documentText = null;
            byte[] documentByte = null;

            if (jsonObject.has("documentText")){
                documentText = jsonObject.get("documentText").getAsString();
                textDocument = true;
            }
            else{
                String base64Encoded = jsonObject.get("documentByte").getAsString();
                documentByte = DatatypeConverter.parseBase64Binary(base64Encoded);
            }

            String uriString = jsonObject.get("uri").getAsString();

            Type metadataMapType = new TypeToken<HashMap<String, Integer>>(){}.getType();
            String metadataString = jsonObject.get("metadata").getAsString();
            HashMap<String, String> metadata = gson.fromJson(metadataString, metadataMapType);

            Type wordMapType = new TypeToken<HashMap<String, String>>(){}.getType();
            String wordMapString = jsonObject.get("wordMap").getAsString();
            HashMap<String, Integer> wordMap = gson.fromJson(wordMapString, wordMapType);

            Document document;
            try {
                if (textDocument) {
                    document = new DocumentImpl(new URI(uriString), documentText, wordMap);
                    document.setMetadata(metadata);
                    return document;
                } else{
                    document = new DocumentImpl(new URI(uriString), documentByte);
                    document.setMetadata(metadata);
                    return document;
                }
            } catch(URISyntaxException e){
                System.out.println("couldn't create URI");
            }
            return null;
        }
    }

    public void serialize(URI key, Document val) throws IOException{
        Document document = (Document) val;
        CustomSerialization customSerialization = new CustomSerialization();
        createJsonFile(key, customSerialization.serialize(document, null, null));
    }

    public Document deserialize(URI key) throws IOException{
        CustomSerialization customSerialization = new CustomSerialization();
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
        Path path = Paths.get(getFilePath(key));
        return Files.deleteIfExists(path);
    }

    private void createJsonFile(URI key, JsonElement json){
        try{
            String filePath = getFilePath(key);
            Path directory = Paths.get(filePath);
            Files.createDirectories(directory);

            Gson gson = new Gson();
            gson.toJson(json, new FileWriter(filePath));

        } catch(IOException e){
            System.out.println("error creating new file");
        }
    }

    private JsonElement getJsonFile(URI key) throws IOException {
        Gson gson = new Gson();
        try (FileReader reader = new FileReader(getFilePath(key))) {
            return gson.fromJson(reader, JsonElement.class);
        }
    }

    private String getFilePath(URI key){
        return key.toString().substring(6) + directory + ".json";
    }
}