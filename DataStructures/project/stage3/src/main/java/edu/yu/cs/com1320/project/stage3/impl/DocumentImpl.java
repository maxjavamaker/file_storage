package edu.yu.cs.com1320.project.stage3.impl;

import edu.yu.cs.com1320.project.impl.HashTableImpl;
import edu.yu.cs.com1320.project.stage3.Document;
import edu.yu.cs.com1320.project.HashTable;

import java.net.URI;
import java.util.Arrays;


public class DocumentImpl implements Document {
    private String text;
    private URI uri;
    private byte[] binaryData;
    private HashTable<String, String> metadata = new HashTableImpl<>();

    public DocumentImpl(URI uri, String txt) {
        if (uri == null || txt == null || uri.toString().isEmpty() || txt.isEmpty()) {
            throw new IllegalArgumentException();
        }

        this.uri = uri;
        this.text = txt;
    }

    public DocumentImpl(URI uri, byte[] binaryData) {
        if (uri == null || binaryData == null || uri.toString().isEmpty() || binaryData.length == 0) {
            throw new IllegalArgumentException();
        }

        this.uri = uri;
        this.binaryData = binaryData;
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
        HashTable<String, String> copy = new HashTableImpl<>();
        String key;
        String value;
        for (String keys : metadata.keySet()){
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
