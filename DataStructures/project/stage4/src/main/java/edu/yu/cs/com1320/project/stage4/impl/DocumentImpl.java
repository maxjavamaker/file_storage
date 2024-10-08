package edu.yu.cs.com1320.project.stage4.impl;

import edu.yu.cs.com1320.project.HashTable;
import edu.yu.cs.com1320.project.impl.HashTableImpl;
import edu.yu.cs.com1320.project.stage4.Document;

import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;


public class DocumentImpl implements Document {
    private boolean isBinary;
    private String text;
    private URI uri;
    private byte[] binaryData;
    private HashTable<String, String> metadata = new HashTableImpl<>();
    private HashMap<String, Integer> words = new HashMap<>();

    public DocumentImpl(URI uri, String txt) {
        if (uri == null || txt == null || uri.toString().isEmpty() || txt.isEmpty()) {
            throw new IllegalArgumentException();
        }

        this.uri = uri;
        this.text = txt;
        this.isBinary = false;
        this.addWordsToMap(txt); //add every word to a hashmap
    }

    public DocumentImpl(URI uri, byte[] binaryData) {
        if (uri == null || binaryData == null || uri.toString().isEmpty() || binaryData.length == 0) {
            throw new IllegalArgumentException();
        }

        this.uri = uri;
        this.binaryData = binaryData;
        this.isBinary = true;
    }

    /**
     * @param key   key of document metadata to store a value for
     * @param value value to store
     * @return old value, or null if there was no old value
     * @throws IllegalArgumentException if the key is null or blank
     */
    public String setMetadataValue(String key, String value) {
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException();
        }

        return this.metadata.put(key, value);

    }

    /**
     * @param key metadata key whose value we want to retrieve
     * @return corresponding value, or null if there is no such key
     * @throws IllegalArgumentException if the key is null or blank
     */
    public String getMetadataValue(String key) {
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException();
        }

        return metadata.get(key);
    }

    /**
     * @return a COPY of the metadata saved in this document
     */
    public HashTable<String, String> getMetadata() {
        HashTable<String, String> copy = new HashTableImpl<>(); //create a copy of the hashtable holding all the metadata
        String key;
        String value;
        for (String keys : metadata.keySet()){  //add all the metadata to the copy
            key = keys;
            value = metadata.get(keys);
            copy.put(key, value);
        }

        return copy;
    }

    /**
     * @return content of text document
     */
    public String getDocumentTxt() {
        return this.text;
    }

    /**
     * @return content of binary data document
     */
    public byte[] getDocumentBinaryData() {
        return this.binaryData;
    }

    /**
     * @return URI which uniquely identifies this document
     */
    public URI getKey() {
        return this.uri;

    }

    /**
     * how many times does the given word appear in the document?
     * @param word
     * @return the number of times the given words appears in the document. If it's a binary document, return 0.
     */
    public int wordCount(String word) {
        return this.isBinary ? 0 : this.words.get(word); //if doc is binary return 0, else return word count

    }
    /**
     * @return all the words that appear in the document
     */
    public Set<String> getWords(){
        return this.words.keySet();
    }

    private void addWordsToMap(String txt){
        String[] txtArray = txt.split("");
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < txtArray.length; i++){
            if (!txtArray[i].matches("[^a-zA-Z0-9\\s]")){  //remove any characters that are not words or numbers
                builder.append(txtArray[i]);
            }
        }
        String textString = builder.toString();
        String[] textArray = textString.split(" "); //split at whitespace
        for (String word : textArray){
            this.words.compute(word, (key, oldValue) -> (oldValue == null) ? 1 : oldValue + 1);  //if the value is null set to one, otherwise increase by 1
        }
    }

    @Override
    public int hashCode() {
        int result = uri.hashCode();
        result = 31 * result + (text != null ? text.hashCode() : 0);
        result = 31 * result + Arrays.hashCode(binaryData);
        return Math.abs(result);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Document s)) {
            return false;
        }
        return this.hashCode() == s.hashCode();
    }
}
