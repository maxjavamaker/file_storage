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
    String text2 = "Hello, world!";
    String text3 = "Hello";
    byte[] byteArray = text.getBytes(StandardCharsets.UTF_8);
    URI uri;
    URI uri2;

    @BeforeEach
    public void setup() {
        try {
            uri = new URI("file");
            uri2 = new URI("file");
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

    @Test
    public void hashCodeTest(){
        Document document1 = new DocumentImpl(uri, text);
        Document document2 = new DocumentImpl(uri2, text2);
        assertTrue(document1.hashCode() == document2.hashCode());

    }

    @Test
    public void getBinaryFromTextDocument(){
        Document document = new DocumentImpl(uri, text);
        assertNull(document.getDocumentBinaryData());
    }

    @Test
    public void getTextFromBinaryDocument(){
        byte[] bytes = text.getBytes();
        Document document = new DocumentImpl(uri, bytes);
        assertNull(document.getDocumentTxt());
    }

    @Test
    public void equals(){
        Document document1 = new DocumentImpl(uri, text);
        Document document2 = new DocumentImpl(uri, text);
        Document document3 = null;
        Document document4 = new DocumentImpl(uri2, text3);
        assertFalse(document1.equals(document3));
        assertTrue(document1.equals(document1));
        assertTrue(document1.equals(document2));
        assertNotEquals(document4, document1);
    }
}
