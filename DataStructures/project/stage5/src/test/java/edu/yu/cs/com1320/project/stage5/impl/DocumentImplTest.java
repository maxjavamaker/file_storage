package edu.yu.cs.com1320.project.stage5.impl;

import edu.yu.cs.com1320.project.stage5.Document;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

public class DocumentImplTest {

    String text = "Hello, world!";
    String text2 = "Hello, world!";
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
        Document document = new DocumentImpl(uri, text);
        assertThrows(IllegalArgumentException.class, () -> document.setMetadataValue(null, text));
        assertThrows(IllegalArgumentException.class, () -> document.setMetadataValue("", text));
        assertNull(document.setMetadataValue("value1", "key1"));
        assertEquals("key1", document.setMetadataValue("value1", "key2"));

    }

    @Test
    public void getMetaDataValue(){
        String key1 = null;
        String key2 = "";
        String key3 = "month";
        String value = "value";
        Document document = new DocumentImpl(uri, text);

        document.setMetadataValue(key3, value);
        assertEquals(value, document.getMetadataValue(key3));
    }

    @Test
    public void getMetaData(){
        Document document1 = new DocumentImpl(uri, text);
        document1.setMetadataValue("key1", "value1");
        document1.setMetadataValue("key2", "value2");
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
    public void wordCountBinaryDoc(){
        byte[] bytes = text.getBytes();
        Document document = new DocumentImpl(uri, bytes);
        assertEquals(0, document.wordCount(text));
    }

    @Test
    public void wordCountTextDoc(){
        String text = "hey how're do you do, how are you?> do now?";
        Document document = new DocumentImpl(uri, text);
        assertEquals(7, document.getWords().size());

    }

    @Test
    public void wordCountTextDocAddWords(){
        String text = "hey how do you do, how are you do now?";
        Document document = new DocumentImpl(uri, text);
        assertEquals(3, document.wordCount("do"));
    }

    @Test
    public void wordCountZero(){
        String text = "hey how do you do, how are you do now?";
        Document document = new DocumentImpl(uri, text);
        assertEquals(0, document.wordCount("Do"));
    }

    @Test
    public void initializingLastUseTime(){
        Document document = new DocumentImpl(uri, text);
        assert(document.getLastUseTime() != 0);
    }

    @Test
    public void setLastUseTime(){
        Document document = new DocumentImpl(uri, text);
        document.setLastUseTime(100);
        assert(document.getLastUseTime() == 100);
        document.setLastUseTime(200);
        assert(document.getLastUseTime() == 200);
    }

    @Test
    public void compare(){
        Document document1 = new DocumentImpl(uri, text);
        Document document2 = new DocumentImpl(uri2, text);

        assert(document2.compareTo(document1) > 0);
        assert(document1.compareTo(document2) < 0);

        document2.setLastUseTime(0);
        assert(document2.compareTo(document1) < 0);

        document1.setLastUseTime(0);
        assert(document2.compareTo(document1) == 0);

    }
}

