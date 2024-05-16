
package edu.yu.cs.com1320.project.stage6.impl;

import edu.yu.cs.com1320.project.stage6.Document;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

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
            DocumentImpl document = new DocumentImpl(uri, text, null);});
    }
    @Test
    public void nulltext() {
        assertThrows(IllegalArgumentException.class, () -> {
            text = null;
            DocumentImpl document = new DocumentImpl(uri, text, null);});
    }
    @Test
    public void emptyText() {
        assertThrows(IllegalArgumentException.class, () -> {
            text = "";
            DocumentImpl document = new DocumentImpl(uri, "", null);});
    }

    @Test
    public void nullByte(){
        byte[] byteArray = null;
        assertThrows(IllegalArgumentException.class, () -> {
            DocumentImpl document = new DocumentImpl(uri, byteArray);});
    }

    @Test
    public void instantiatedWithNullWordMap(){
        Document document = new DocumentImpl(uri, text, null);
        assertEquals(1, document.wordCount("Hello"));
        assertEquals(1, document.wordCount("world"));
        assertEquals(0, document.wordCount("doctor"));
    }

    @Test
    public void instantiatedWithWordMap(){
        Map<String, Integer> wordCountMap = new HashMap<>();
        wordCountMap.put("Hello", 1);
        wordCountMap.put("world", 1);
        Document document = new DocumentImpl(uri, text, wordCountMap);
        assertEquals(1, document.wordCount("Hello"));
        assertEquals(1, document.wordCount("world"));
        assertEquals(0, document.wordCount("doctor"));
    }

    @Test
    public void byteLengthZero(){
        byte[] byteArray = new byte[0];
        assertThrows(IllegalArgumentException.class, () -> {
            DocumentImpl document = new DocumentImpl(uri, byteArray);});
    }

    @Test
    public void getText(){
        Document document = new DocumentImpl(uri, text, null);
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
        Document document = new DocumentImpl(uri, text, null);

        assertThrows(IllegalArgumentException.class, () -> {
            document.setMetadataValue(key, value);});

        String key2 = "";
        assertThrows(IllegalArgumentException.class, () -> {
            document.setMetadataValue(key, value);});

    }

    @Test
    public void getKey(){
        Document document = new DocumentImpl(uri, text, null);
        assertEquals(uri, document.getKey());
    }

    @Test
    public void setMetaDataValue(){
        Document document = new DocumentImpl(uri, text, null);
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
        Document document = new DocumentImpl(uri, text, null);

        document.setMetadataValue(key3, value);
        assertEquals(value, document.getMetadataValue(key3));
    }

    @Test
    public void getMetaData(){
        Document document1 = new DocumentImpl(uri, text, null);
        document1.setMetadataValue("key1", "value1");
        document1.setMetadataValue("key2", "value2");
    }

    @Test
    public void hashCodeTest(){
        Document document1 = new DocumentImpl(uri, text, null);
        Document document2 = new DocumentImpl(uri2, text2, null);
        assertTrue(document1.hashCode() == document2.hashCode());

    }

    @Test
    public void getBinaryFromTextDocument(){
        Document document = new DocumentImpl(uri, text, null);
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
        Document document = new DocumentImpl(uri, text, null);
        assertEquals(7, document.getWords().size());

    }

    @Test
    public void wordCountTextDocAddWords(){
        String text = "hey how do you do, how are you do now?";
        Document document = new DocumentImpl(uri, text, null);
        assertEquals(3, document.wordCount("do"));
    }

    @Test
    public void wordCountZero(){
        String text = "hey how do you do, how are you do now?";
        Document document = new DocumentImpl(uri, text, null);
        assertEquals(0, document.wordCount("Do"));
    }

    @Test
    public void initializingLastUseTime(){
        Document document = new DocumentImpl(uri, text, null);
        assert(document.getLastUseTime() != 0);
    }

    @Test
    public void setLastUseTime(){
        Document document = new DocumentImpl(uri, text, null);
        document.setLastUseTime(100);
        assert(document.getLastUseTime() == 100);
        document.setLastUseTime(200);
        assert(document.getLastUseTime() == 200);
    }

    @Test
    public void compare(){
        Document document1 = new DocumentImpl(uri, text, null);
        Document document2 = new DocumentImpl(uri2, text, null);

        assert(document2.compareTo(document1) > 0);
        assert(document1.compareTo(document2) < 0);

        document2.setLastUseTime(0);
        assert(document2.compareTo(document1) < 0);

        document1.setLastUseTime(0);
        assert(document2.compareTo(document1) == 0);

    }
}
