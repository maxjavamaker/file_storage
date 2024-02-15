package edu.yu.cs.com1320.project.stage1.impl;

import edu.yu.cs.com1320.project.stage1.Document;
import edu.yu.cs.com1320.project.stage1.DocumentStore;
import edu.yu.cs.com1320.project.stage1.impl.DocumentStoreImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class DocumentStoreImplTest {

    String text = "Hello, world!";
    String key = "month";
    String value = "October";
    byte[] byteArray = text.getBytes(StandardCharsets.UTF_8);
    InputStream inputStream = new ByteArrayInputStream(text.getBytes());
    URI uri;
    URI uri1;
    URI uri2;
    URI uri3;
    DocumentStore documentStore = new DocumentStoreImpl();

    @BeforeEach
    public void setup() {
        try {
            uri = new URI("file");
            documentStore.put(inputStream, uri, DocumentStore.DocumentFormat.TXT);
        }
        catch(URISyntaxException | IOException e ){
        }
    }

    @Test
    public void putNullURI(){
        assertThrows(IllegalArgumentException.class, () -> {
            uri = null;
            documentStore.put(inputStream, uri, DocumentStore.DocumentFormat.TXT);});
    }
    @Test
    public void putEmptyURI(){
        assertThrows(IllegalArgumentException.class, () -> {
            try {
                uri = new URI("");
            }
            catch(URISyntaxException e){
            }
            documentStore.put(inputStream, uri, DocumentStore.DocumentFormat.TXT);});
    }

    @Test
    public void putNullFormat(){
        assertThrows(IllegalArgumentException.class, () -> {
            documentStore.put(inputStream, uri, null);});
    }

    @Test
    public void putNullInputStream(){
        inputStream = null;

        try {
            assertEquals(documentStore.get(uri).hashCode(), documentStore.put(inputStream, uri, DocumentStore.DocumentFormat.TXT));
        }
        catch(IOException e) {

        }
    }

    @Test
    public void putNullInputStreamRemoveDocument(){
        inputStream = null;
        documentStore.delete(uri);
        try {
            assertEquals(0, documentStore.put(inputStream, uri, DocumentStore.DocumentFormat.TXT));
        }
        catch(IOException e){

        }
    }
    @Test
    public void deleteTrue(){
        assertEquals(true, documentStore.delete(uri));

        assertEquals(false, documentStore.delete(uri));
    }

    @Test
    public void putTXT(){
        String text1 = "URI1";
        String text2 = "URI2";
        byte[] byte1 = text1.getBytes();
        byte[] byte2 = text2.getBytes();
        InputStream inputStream1 = new ByteArrayInputStream(byte1);
        InputStream inputStream2 = new ByteArrayInputStream(byte2);
        InputStream inputStream3 = new ByteArrayInputStream(byte2);


        try {
            uri1 = new URI("text1");
            uri2 = new URI("putTEXT2");
            uri3 = new URI("putTEXT1");
            Document document1 = new DocumentImpl(uri1, text1);

            assertEquals(0, documentStore.put(inputStream2, uri2, DocumentStore.DocumentFormat.TXT));

        }
        catch(URISyntaxException | IOException e){
            System.out.println("there was a uri syntax or io exception");
        }
    }
    @Test
    public void putBINARY(){
        String text1 = "URI1";
        String text2 = "URI2";
        byte[] byte1 = text1.getBytes();
        byte[] byte2 = text2.getBytes();
        InputStream inputStream1 = new ByteArrayInputStream(byte1);
        InputStream inputStream2 = new ByteArrayInputStream(byte2);
        InputStream inputStream3 = new ByteArrayInputStream(byte2);


        try {
            uri1 = new URI("text1");
            uri2 = new URI("putTEXT2");
            uri3 = new URI("putTEXT1");
            Document document1 = new DocumentImpl(uri1, text1);

            assertEquals(0, documentStore.put(inputStream2, uri2, DocumentStore.DocumentFormat.BINARY));
            inputStream2.reset();
            assertNotEquals(0, documentStore.put(inputStream2, uri2, DocumentStore.DocumentFormat.BINARY));

        }
        catch(URISyntaxException | IOException e){
            System.out.println("there was a uri syntax or io exception");
        }
    }

    @Test
    public void equality(){
        Document document1 = new DocumentImpl(uri, text);
        Document document2 = new DocumentImpl(uri, text);
        assertTrue(document1.equals(document2));
    }


    @Test
    public void setMetaDataNullURI(){
        assertThrows(IllegalArgumentException.class, () -> {
            key = null;
            documentStore.setMetadata(uri, key, value);});
    }

    @Test
    public void setMetaDataEmptyURI(){
        assertThrows(IllegalArgumentException.class, () -> {
            try {
                uri = new URI("");
            }
            catch(URISyntaxException e){
            }
            documentStore.setMetadata(uri, key, value);});
    }

    @Test
    public void setMetaDataNullKey(){
        assertThrows(IllegalArgumentException.class, () -> {
            key = null;
            documentStore.setMetadata(uri, key, value);});
    }

    @Test
    public void setMetaDataNullEmptyKey(){
        assertThrows(IllegalArgumentException.class, () -> {
            key = "";
            documentStore.setMetadata(uri, key, value);});
    }

    @Test
    public void setMetaData(){

    }

}