package edu.yu.cs.com1320.project.stage6.impl;

import edu.yu.cs.com1320.project.stage6.Document;
import edu.yu.cs.com1320.project.stage6.DocumentStore;
import edu.yu.cs.com1320.project.stage6.impl.DocumentStoreImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class DocumentStoreImplTest {

    String phrase1, phrase2, phrase3;
    String text1, text2, text3, text4;
    String key1, key2, key3;
    String value1, value2, value3;
    URI uri1, uri2, uri3, uri4, uri5;
    DocumentStore documentStore;

    @BeforeEach
    public void setup() {
        try {
            phrase1 = "good, morning this is text1?, Text1";
            phrase2 = "good, morning this is text1?, Text2";
            phrase3 = "good, morning this is text3?, Text3";

            text1 = "text1";
            text2 = "text2";
            text3 = "text3";
            text4 = "text4";
            key1 = "key1";
            key2 = "key2";
            key3 = "key3";
            value1 = "value1";
            value2 = "value2";
            value3 = "value3";
            uri1 = new URI("uri1");
            uri2 = new URI("uri2");
            uri3 = new URI("uri3");
            uri4 = new URI("uri4");
            uri5 = new URI("uri5");
            documentStore = new DocumentStoreImpl();

        } catch (URISyntaxException e) {
        }
    }

    @Test
    public void putNullURI(){
        URI uri1 = null; // Initialize uri1 here
        assertThrows(IllegalArgumentException.class, () -> {
            documentStore.put(new ByteArrayInputStream(text1.getBytes()), uri1, DocumentStore.DocumentFormat.TXT);
        });
    }

    @Test
    public void putEmptyURI(){
        assertThrows(IllegalArgumentException.class, () -> {
            uri1 = new URI("");
            documentStore.put(new ByteArrayInputStream(text1.getBytes()), uri1, DocumentStore.DocumentFormat.TXT);
        });
    }

    @Test
    public void putNullFormat(){
        assertThrows(IllegalArgumentException.class, () -> {
            documentStore.put(new ByteArrayInputStream(text1.getBytes()), uri1, null);
        });
    }

    @Test
    public void putNullInputStreamNoPreviousDocumentReturn0(){
        try {
            assertEquals(0, documentStore.put(null, uri1, DocumentStore.DocumentFormat.TXT));
            documentStore.put(new ByteArrayInputStream(text1.getBytes()), uri1, DocumentStore.DocumentFormat.TXT);
            documentStore.put(new ByteArrayInputStream(text2.getBytes()), uri2, DocumentStore.DocumentFormat.TXT);
            Document document1 = new DocumentImpl(uri1, text1, null);
            assertEquals(document1.hashCode(), documentStore.put(new ByteArrayInputStream(text1.getBytes()), uri1, DocumentStore.DocumentFormat.TXT));
        }
        catch(IOException e){
            e.printStackTrace(System.out);
        }

    }

    @Test
    public void putNullInputStreamPreviousDocumentReturnHashCode(){
        try {
            documentStore.put(new ByteArrayInputStream(text1.getBytes()), uri1, DocumentStore.DocumentFormat.TXT);
            documentStore.put(new ByteArrayInputStream(text2.getBytes()), uri2, DocumentStore.DocumentFormat.TXT);
            documentStore.setMaxDocumentCount(1);
            Document document1 = new DocumentImpl(uri1, text1, null);
            assertEquals(document1.hashCode(), documentStore.put(null, uri1, DocumentStore.DocumentFormat.TXT));
            assertEquals(0, documentStore.put(null, uri1, DocumentStore.DocumentFormat.TXT));

        }
        catch(IOException e){
            e.printStackTrace(System.out);
        }
    }

    @Test
    public void putWhenThereIsNoPreviousDocumentReturn0(){
        try {
            assertEquals(0, documentStore.put(new ByteArrayInputStream(text1.getBytes()), uri1, DocumentStore.DocumentFormat.TXT));
            assertEquals(0, documentStore.put(new ByteArrayInputStream(text1.getBytes()), uri2, DocumentStore.DocumentFormat.BINARY));

        }
        catch(IOException e){
            e.printStackTrace(System.out);
        }
    }

    @Test
    public void putWhenThereIsAPreviousDocumentReturnHashCode(){
        try {
            documentStore.put(new ByteArrayInputStream(text1.getBytes()), uri1, DocumentStore.DocumentFormat.BINARY);
            int hashCode1 = documentStore.get(uri1).hashCode();
            assertEquals(hashCode1, documentStore.put(new ByteArrayInputStream(text2.getBytes()), uri1, DocumentStore.DocumentFormat.TXT));

            documentStore.put(new ByteArrayInputStream(text1.getBytes()), uri2, DocumentStore.DocumentFormat.TXT);
            int hashCode2 = documentStore.get(uri2).hashCode();
            assertEquals(hashCode2, documentStore.put(new ByteArrayInputStream(text2.getBytes()), uri2, DocumentStore.DocumentFormat.BINARY));
        }
        catch(IOException e){
            e.printStackTrace(System.out);
        }
    }

    @Test
    public void putWhenThereIsADocumentInDiskReturnHashCode(){
        try {
            documentStore.put(new ByteArrayInputStream(text1.getBytes()), uri1, DocumentStore.DocumentFormat.TXT);
            documentStore.put(new ByteArrayInputStream(text2.getBytes()), uri2, DocumentStore.DocumentFormat.TXT);
            documentStore.setMaxDocumentCount(1);
            Document document1 = new DocumentImpl(uri1, text1, null);
            assertEquals(document1.hashCode(), documentStore.put(new ByteArrayInputStream(text1.getBytes()), uri1, DocumentStore.DocumentFormat.TXT));
            documentStore.put(new ByteArrayInputStream(text3.getBytes()), uri2, DocumentStore.DocumentFormat.TXT);
            assertEquals(text3, documentStore.get(uri2).getDocumentTxt());
        }
        catch(IOException e){
            e.printStackTrace(System.out);
        }
    }

    @Test
    public void putUndoNoPreviousDocument(){
        try {
            documentStore.put(new ByteArrayInputStream(text1.getBytes()), uri1, DocumentStore.DocumentFormat.TXT);
            documentStore.put(new ByteArrayInputStream(text2.getBytes()), uri2, DocumentStore.DocumentFormat.TXT);
            assertNotNull(documentStore.get(uri1));
            assert(!documentStore.search("text1").isEmpty());
            assert(!documentStore.search("text2").isEmpty());
            assertNotNull(documentStore.get(uri2));

            documentStore.undo();  //should kick out uri2
            assertNull(documentStore.get(uri2));
            assert(documentStore.search("text2").isEmpty());

            documentStore.undo();  //should kick out uri1
            assertNull(documentStore.get(uri1));
            assert(documentStore.search("text1").isEmpty());


        }
        catch(IOException e){
            e.printStackTrace(System.out);
        }
    }

    @Test
    public void undoPutWhenThereWasAPreviousDocument(){
        try {
            documentStore.put(new ByteArrayInputStream(text1.getBytes()), uri1, DocumentStore.DocumentFormat.TXT);
            documentStore.put(new ByteArrayInputStream(text2.getBytes()), uri1, DocumentStore.DocumentFormat.TXT);
            assert(documentStore.search("text1").isEmpty());
            assert(!documentStore.search("text2").isEmpty());
            assertNotNull(documentStore.get(uri1));

            documentStore.undo();

            assert(!documentStore.search("text1").isEmpty());
            assert(documentStore.search("text2").isEmpty());
            assertNotNull(documentStore.get(uri1));
        }
        catch(IOException e){
            e.printStackTrace(System.out);
        }
    }

    @Test
    public void getSpecificURIPutUndo(){
        try {
            documentStore.put(new ByteArrayInputStream(text1.getBytes()), uri1, DocumentStore.DocumentFormat.TXT);
            documentStore.put(new ByteArrayInputStream(text2.getBytes()), uri2, DocumentStore.DocumentFormat.TXT);

            documentStore.setMaxDocumentCount(1);

            documentStore.undo(uri1);
            assertNull(documentStore.get(uri1));
            assert(documentStore.search(text1).isEmpty());
            assertNotNull(documentStore.get(uri2));
            assert(!documentStore.search(text2).isEmpty());

        }
        catch(IOException e){
            e.printStackTrace(System.out);
        }
    }

    @Test
    public void undoPutRevertsDocumentsToMemory(){
        //still need to create the logic
    }

    @Test
    public void getAndSetMetadata(){
        try {
            assertThrows(IllegalArgumentException.class, () -> {
                documentStore.setMetadata(uri1, key1, value1);
            });

            documentStore.put(new ByteArrayInputStream(text1.getBytes()), uri1, DocumentStore.DocumentFormat.TXT);
            documentStore.setMetadata(uri1, key1, value1);


            uri1 = null;
            assertThrows(IllegalArgumentException.class, () -> {
                documentStore.setMetadata(uri1, key1, value1);
            });

            assertThrows(IllegalArgumentException.class, () -> {
                documentStore.getMetadata(uri1, key1);
            });

            uri1 = new URI("");
            assertThrows(IllegalArgumentException.class, () -> {
                documentStore.setMetadata(uri1, key1, value1);
            });

            assertThrows(IllegalArgumentException.class, () -> {
                documentStore.getMetadata(uri1, key1);
            });

            assertThrows(IllegalArgumentException.class, () -> {
                documentStore.setMetadata(uri1, null, value1);
            });

            assertThrows(IllegalArgumentException.class, () -> {
                documentStore.setMetadata(uri1, "", value1);
            });

            documentStore.put(new ByteArrayInputStream(text2.getBytes()), uri2, DocumentStore.DocumentFormat.TXT);
            documentStore.put(new ByteArrayInputStream(text3.getBytes()), uri3, DocumentStore.DocumentFormat.TXT);

            documentStore.setMetadata(uri2, key2, value2);
            uri1 = new URI("uri1");
            documentStore.setMetadata(uri1, key1, value2);
            assertEquals(value2, documentStore.getMetadata(uri1, key1));
            assertEquals(value2, documentStore.setMetadata(uri1, key1, value1));

        } catch(URISyntaxException | IOException e){
            e.printStackTrace(System.out);
        }
    }

    @Test
    public void setMetadataValueBringsBackFromDisk(){
        try {
            documentStore.put(new ByteArrayInputStream(text1.getBytes()), uri1, DocumentStore.DocumentFormat.TXT);
            documentStore.put(new ByteArrayInputStream(text2.getBytes()), uri2, DocumentStore.DocumentFormat.TXT);
            documentStore.put(new ByteArrayInputStream(text3.getBytes()), uri3, DocumentStore.DocumentFormat.TXT);
            documentStore.put(new ByteArrayInputStream(text4.getBytes()), uri4, DocumentStore.DocumentFormat.TXT);

            documentStore.setMaxDocumentBytes(10);

            documentStore.getMetadata(uri1, key1);
            documentStore.getMetadata(uri2, key2);

            documentStore.setMaxDocumentBytes(30);

            documentStore.getMetadata(uri3, key3);
            documentStore.getMetadata(uri4, key3);

        } catch (IOException e){
            e.printStackTrace(System.out);
        }
    }

    @Test
    public void testSetMetadataUndoRevertsValueAndMemoryProperly(){
        try {
            documentStore.put(new ByteArrayInputStream(text1.getBytes()), uri1, DocumentStore.DocumentFormat.TXT);
            documentStore.put(new ByteArrayInputStream(text2.getBytes()), uri2, DocumentStore.DocumentFormat.TXT);

            documentStore.setMetadata(uri1, key1, value1);
            assertEquals(value1, documentStore.getMetadata(uri1, key1));
            documentStore.undo();
            assertNull(documentStore.getMetadata(uri1,key1));

            documentStore.setMetadata(uri1, key1, value1);
            assertEquals(value1, documentStore.getMetadata(uri1, key1));
            documentStore.setMetadata(uri1, key1, value2);
            assertEquals(value2, documentStore.getMetadata(uri1, key1));
            documentStore.undo();
            assertEquals(value1, documentStore.getMetadata(uri1, key1));

        } catch (IOException e){
            e.printStackTrace(System.out);
        }
    }

    @Test
    public void setMetadataUndoOnASpecificURI(){
        try {
            documentStore.put(new ByteArrayInputStream(text1.getBytes()), uri1, DocumentStore.DocumentFormat.TXT);
            documentStore.put(new ByteArrayInputStream(text2.getBytes()), uri2, DocumentStore.DocumentFormat.TXT);

            documentStore.setMetadata(uri1, key1, value1);
            documentStore.setMetadata(uri2, key2, value2);

            documentStore.undo(uri1);

            assertNull(documentStore.getMetadata(uri1, key1));
            assertNotNull(documentStore.getMetadata(uri2, key2));

        } catch (IOException e){
            e.printStackTrace(System.out);
        }
    }

    @Test
    public void setMetadataUndoOnADocumentInMemory(){
        try {
            documentStore.put(new ByteArrayInputStream(text1.getBytes()), uri1, DocumentStore.DocumentFormat.TXT);
            documentStore.put(new ByteArrayInputStream("1".getBytes()), uri2, DocumentStore.DocumentFormat.TXT);

            documentStore.setMetadata(uri1, key1, value1);
            documentStore.setMetadata(uri2, key2, value2);

            documentStore.setMaxDocumentBytes(1);

            assertThrows(IllegalArgumentException.class, () -> {
                documentStore.undo(uri1);
            });

            documentStore.setMaxDocumentBytes(5);

            documentStore.undo(uri1);

            assertEquals(value1, documentStore.getMetadata(uri1, key1));
        } catch (IOException e){
            e.printStackTrace(System.out);
        }
    }

    @Test
    public void get(){
        try{
            documentStore.put(new ByteArrayInputStream(text1.getBytes()), uri1, DocumentStore.DocumentFormat.TXT);
            Document document1 = new DocumentImpl(uri1, text1, null);
            assertEquals(document1, documentStore.get(uri1));
        }
        catch(IOException e){

        }
    }

    @Test
    public void getWhenBringingOutOfMemoryViolatesLimits(){
        try{
            documentStore.put(new ByteArrayInputStream(text1.getBytes()), uri1, DocumentStore.DocumentFormat.TXT);
            documentStore.put(new ByteArrayInputStream("h".getBytes()), uri2, DocumentStore.DocumentFormat.TXT);

            documentStore.setMaxDocumentBytes(2);

            documentStore.get(uri2);
            assertThrows(IllegalArgumentException.class, () -> {
                documentStore.get(uri1);
            });
        }
        catch(IOException e){

        }
    }

    @Test
    public void getBringsAndKicksOutOfMemory(){
        try{
            documentStore.put(new ByteArrayInputStream(text1.getBytes()), uri1, DocumentStore.DocumentFormat.TXT);
            documentStore.put(new ByteArrayInputStream(text2.getBytes()), uri2, DocumentStore.DocumentFormat.TXT);

            Document document1 = new DocumentImpl(uri1, text1, null);
            Document document2 = new DocumentImpl(uri2, text2, null);

            //problem when i set max doc count to 1
            documentStore.setMaxDocumentCount(2);

            assertEquals(document1, documentStore.get(uri1));
            assertEquals(document2, documentStore.get(uri2));

        }
        catch(IOException e){

        }
    }

    @Test
    public void deleteDocumentInMemory(){
        try{
            documentStore.put(new ByteArrayInputStream(text1.getBytes()), uri1, DocumentStore.DocumentFormat.TXT);
            documentStore.put(new ByteArrayInputStream(text2.getBytes()), uri2, DocumentStore.DocumentFormat.TXT);

            assert(!documentStore.search(text1).isEmpty());
            assert(!documentStore.search(text2).isEmpty());


            assertFalse(documentStore.delete(uri3));

            assert(!documentStore.search(text1).isEmpty());
            assert(!documentStore.search(text2).isEmpty());

            assertTrue(documentStore.delete(uri1));

            assert(documentStore.search(text1).isEmpty());
            assert(!documentStore.search(text2).isEmpty());
        }
        catch(IOException e){

        }
    }
}