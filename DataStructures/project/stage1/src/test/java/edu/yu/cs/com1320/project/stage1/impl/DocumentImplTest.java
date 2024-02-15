package edu.yu.cs.com1320.project.stage1.impl;

import edu.yu.cs.com1320.project.stage1.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class DocumentImplTest {

    String text = "Hello, world!";
    byte[] byteArray = text.getBytes(StandardCharsets.UTF_8);
    URI uri;

    @BeforeEach
    public void setup() {
        try {
            uri = new URI("file");
        }
        catch(URISyntaxException e){
        }
    }

    @Test
    public void nullURI() {
        assertThrows(IllegalArgumentException.class, () -> {
            uri = null;
            DocumentImpl document = new DocumentImpl(uri, text);});
    }
    @Test
    public void nulltext() {
        assertThrows(IllegalArgumentException.class, () -> {
            text = null;
            DocumentImpl document = new DocumentImpl(uri, text);});
    }
    @Test
    public void emptytext() {
        assertThrows(IllegalArgumentException.class, () -> {
            text = "";
            DocumentImpl document = new DocumentImpl(uri, "");});
    }

    @Test
    public void nullbyte(){
        byte[] byteArray = null;
        assertThrows(IllegalArgumentException.class, () -> {
            DocumentImpl document = new DocumentImpl(uri, byteArray);});
    }

    @Test
    public void byteLengthZero(){
        byte[] byteArray = new byte[0];
        assertThrows(IllegalArgumentException.class, () -> {
            DocumentImpl document = new DocumentImpl(uri, byteArray);});
    }

    @Test
    public void getText(){
        Document document = new DocumentImpl(uri, text);
        assert (text.equals(document.getDocumentTxt()));
    }

    @Test
    public void getBinaryData(){
        Document document = new DocumentImpl(uri, byteArray);
        assertArrayEquals(byteArray, document.getDocumentBinaryData());
    }

    @Test
    public void setKey(){
        String key = null;
        String value = "October";
        Document document = new DocumentImpl(uri, text);

        assertThrows(IllegalArgumentException.class, () -> {
            document.setMetadataValue(key, value);});

        String key2 = "";
        assertThrows(IllegalArgumentException.class, () -> {
            document.setMetadataValue(key, value);});
    }

    @Test
    public void getKey(){
        Document document = new DocumentImpl(uri, text);
        assertEquals(uri, document.getKey());
    }

    @Test
    public void setMetaDataValue(){
        String key1 = "month";
        String key2 = null;
        String key3 = "";
        String value = "October";
        Document document = new DocumentImpl(uri, text);
        document.setMetadataValue(key1, value);

        assertThrows(IllegalArgumentException.class, () -> {
            document.getMetadataValue(key2);});

        assertThrows(IllegalArgumentException.class, () -> {
            document.getMetadataValue(key3);});

        assertNull(document.getMetadataValue("date"));

        assertEquals("October", document.getMetadataValue("month"));
    }

    @Test
    public void getMetaDataValue(){
        String key1 = null;
        String key2 = "";
        String key3 = "month";
        Document document = new DocumentImpl(uri, text);

        assertThrows(IllegalArgumentException.class, () -> {
            document.getMetadataValue(key1);});

        assertThrows(IllegalArgumentException.class, () -> {
            document.getMetadataValue(key2);});

        assertNull(document.getMetadataValue("month"));

        document.setMetadataValue("month", "October");
        assertEquals("October", document.getMetadataValue("month"));
    }

    @Test
    public void getMetaData(){
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("month", "October");
        hashMap.put("author", "james patterson");

        Document document  = new DocumentImpl(uri, text);
        document.setMetadataValue("month", "October");
        document.setMetadataValue("author", "james patterson");

        assertEquals(hashMap, document.getMetadata());
    }
}
