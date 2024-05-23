package edu.yu.cs.com1320.project.stage6.impl;

import edu.yu.cs.com1320.project.stage6.Document;
import edu.yu.cs.com1320.project.stage6.DocumentStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            uri1 = new URI("https://yu/edu/documents/Document1");
            uri2 = new URI("https://yu/edu/documents/Document2");
            uri3 = new URI("https://yu/edu/documents/Document3");
            uri4 = new URI("https://yu/edu/documents/Document4");
            uri5 = new URI("https://yu/edu/documents/Document5");
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
    public void replacingAURIViolatesMemoryLimits(){
        try {
            documentStore.put(new ByteArrayInputStream(text1.getBytes()), uri1, DocumentStore.DocumentFormat.TXT);
            documentStore.put(new ByteArrayInputStream(text2.getBytes()), uri2, DocumentStore.DocumentFormat.TXT);

            documentStore.setMaxDocumentBytes(12);

            documentStore.put(new ByteArrayInputStream("thisisover".getBytes()), uri1, DocumentStore.DocumentFormat.TXT);

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
        try {
            documentStore.put(new ByteArrayInputStream(text1.getBytes()), uri1, DocumentStore.DocumentFormat.TXT);
            documentStore.put(new ByteArrayInputStream(text2.getBytes()), uri2, DocumentStore.DocumentFormat.TXT);

            documentStore.setMaxDocumentCount(1);
            documentStore.undo();

            assertNotNull(documentStore.get(uri1));
            assert(!documentStore.search(text1).isEmpty());
            assertNull(documentStore.get(uri2));
            assert(documentStore.search(text2).isEmpty());

        }
        catch(IOException e){
            e.printStackTrace(System.out);
        }
    }

    @Test
    public void getAndSetMetadata(){
        try {
            assertThrows(IllegalArgumentException.class, () ->
                    documentStore.setMetadata(uri1, key1, value1)
            );

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
            uri1 = new URI("https://yu/edu/documents/Document1");
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
            e.printStackTrace(System.out);
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

            //problem when I set max doc count to 1
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

    @Test
    public void undoDelete(){
        try{
            documentStore.put(new ByteArrayInputStream(text1.getBytes()), uri1, DocumentStore.DocumentFormat.TXT);
            documentStore.put(new ByteArrayInputStream(text2.getBytes()), uri2, DocumentStore.DocumentFormat.TXT);

            assert(!documentStore.search(text1).isEmpty());
            assert(!documentStore.search(text2).isEmpty());

            assertTrue(documentStore.delete(uri2));

            assertNotNull(documentStore.get(uri1));
            assertNull(documentStore.get(uri2));

            documentStore.undo();

            assertNotNull(documentStore.get(uri1));
            assertNotNull(documentStore.get(uri2));
        }
        catch(IOException e){

        }
    }

    @Test
    public void undoDeleteOnSpecificURI(){
        try{
            documentStore.put(new ByteArrayInputStream(text1.getBytes()), uri1, DocumentStore.DocumentFormat.TXT);
            documentStore.put(new ByteArrayInputStream(text2.getBytes()), uri2, DocumentStore.DocumentFormat.TXT);
            documentStore.put(new ByteArrayInputStream(text3.getBytes()), uri3, DocumentStore.DocumentFormat.TXT);

            assertNotNull(documentStore.get(uri1));
            assertNotNull(documentStore.get(uri2));
            assertNotNull(documentStore.get(uri3));


            assertTrue(documentStore.delete(uri1));
            assertTrue(documentStore.delete(uri3));

            assertNull(documentStore.get(uri1));
            assertNotNull(documentStore.get(uri2));
            assertNull(documentStore.get(uri3));

            documentStore.undo(uri1);

            assertNotNull(documentStore.get(uri1));
            assertNotNull(documentStore.get(uri2));
            assertNull(documentStore.get(uri3));

            documentStore.undo();

            assertNotNull(documentStore.get(uri1));
            assertNotNull(documentStore.get(uri2));
            assertNotNull(documentStore.get(uri3));

        }
        catch(IOException e){

        }
    }

    @Test
    public void undoDeleteWhichViolatesMemoryLimits(){
        try{
            documentStore.put(new ByteArrayInputStream(text1.getBytes()), uri1, DocumentStore.DocumentFormat.TXT);
            documentStore.put(new ByteArrayInputStream(text2.getBytes()), uri2, DocumentStore.DocumentFormat.TXT);
            documentStore.put(new ByteArrayInputStream(text3.getBytes()), uri3, DocumentStore.DocumentFormat.TXT);

            assertNotNull(documentStore.get(uri1));
            assertNotNull(documentStore.get(uri2));
            assertNotNull(documentStore.get(uri3));

            documentStore.setMaxDocumentBytes(5);

            assertTrue(documentStore.delete(uri2));
            documentStore.undo();
            assertNotNull(documentStore.get(uri2));
        }
        catch(IOException e){
            e.printStackTrace(System.out);
        }
    }

    @Test
    public void search(){
        try {
            documentStore.put(new ByteArrayInputStream("hello hello hello".getBytes()), uri3, DocumentStore.DocumentFormat.TXT);
            documentStore.put(new ByteArrayInputStream("Hello hello Hello".getBytes()), uri1, DocumentStore.DocumentFormat.TXT);
            documentStore.put(new ByteArrayInputStream("hello Hello hello".getBytes()), uri2, DocumentStore.DocumentFormat.TXT);
            documentStore.put(new ByteArrayInputStream("hello Hello hello".getBytes()), uri4, DocumentStore.DocumentFormat.BINARY);

            Document document1 = new DocumentImpl(uri1, "Hello hello Hello", null);
            Document document2 = new DocumentImpl(uri2, "hello Hello hello", null);
            Document document3 = new DocumentImpl(uri3, "hello hello hello", null);
            Document document4 = new DocumentImpl(uri4, "hello hello hello".getBytes());


            List<Document> docList = documentStore.search("hello");

            assertEquals(document3, docList.get(0));
            assertEquals(document2, docList.get(1));
            assertEquals(document1, docList.get(2));
            assert(!docList.contains(document4));

            assert(documentStore.search("test").isEmpty());


        } catch (IOException e){
            e.printStackTrace(System.out);
        }
    }

    @Test
    public void searchUpdatesLastUsedTime(){
        try {
            documentStore.put(new ByteArrayInputStream("hello hello elephant".getBytes()), uri1, DocumentStore.DocumentFormat.TXT);
            documentStore.put(new ByteArrayInputStream("Hello hello tiger".getBytes()), uri2, DocumentStore.DocumentFormat.TXT);
            documentStore.put(new ByteArrayInputStream("Hello Hello lion".getBytes()), uri3, DocumentStore.DocumentFormat.TXT);

            documentStore.search("elephant");
            documentStore.setMaxDocumentCount(1);

        } catch (IOException e){
            e.printStackTrace(System.out);
        }
    }

    @Test
    public void searchHandlesDocumentsInDisk(){
        try {
            documentStore.put(new ByteArrayInputStream("hello hello elephant".getBytes()), uri1, DocumentStore.DocumentFormat.TXT);
            documentStore.put(new ByteArrayInputStream("Hello hello tiger".getBytes()), uri2, DocumentStore.DocumentFormat.TXT);
            documentStore.put(new ByteArrayInputStream("Hello Hello lion".getBytes()), uri3, DocumentStore.DocumentFormat.TXT);

            documentStore.setMaxDocumentCount(1);
            documentStore.search("tiger");

        } catch (IOException e){
            e.printStackTrace(System.out);
        }
    }

    @Test
    public void searchByPrefix(){
        try {
            documentStore.put(new ByteArrayInputStream("helloa helloa helloc".getBytes()), uri3, DocumentStore.DocumentFormat.TXT);
            documentStore.put(new ByteArrayInputStream("helloa helloab helloabyy".getBytes()), uri1, DocumentStore.DocumentFormat.TXT);
            documentStore.put(new ByteArrayInputStream("helloa hellob".getBytes()), uri2, DocumentStore.DocumentFormat.TXT);

            List<Document> docList = documentStore.searchByPrefix("helloa");

            assertEquals(documentStore.get(uri1), docList.get(0));
            assertEquals(documentStore.get(uri3), docList.get(1));
            assertEquals(documentStore.get(uri2), docList.get(2));

        } catch (IOException e){
            e.printStackTrace(System.out);
        }
    }

    @Test
    public void searchByPrefixUpdatesLastUsedTime(){
        try {
            documentStore.put(new ByteArrayInputStream("hello hello elephant".getBytes()), uri1, DocumentStore.DocumentFormat.TXT);
            documentStore.put(new ByteArrayInputStream("Hello hello tiger".getBytes()), uri2, DocumentStore.DocumentFormat.TXT);
            documentStore.put(new ByteArrayInputStream("Hello Hello lion".getBytes()), uri3, DocumentStore.DocumentFormat.TXT);

            documentStore.searchByPrefix("ele");
            documentStore.setMaxDocumentCount(1);

        } catch (IOException e){
            e.printStackTrace(System.out);
        }
    }

    @Test
    public void searchByPrefixHandlesDocumentsInDisk() {
        try {
            documentStore.put(new ByteArrayInputStream("hello hello elephant".getBytes()), uri1, DocumentStore.DocumentFormat.TXT);
            documentStore.put(new ByteArrayInputStream("Hello hello eliana".getBytes()), uri2, DocumentStore.DocumentFormat.TXT);
            documentStore.put(new ByteArrayInputStream("Hello Hello lion".getBytes()), uri3, DocumentStore.DocumentFormat.TXT);

            documentStore.setMaxDocumentCount(1);
            documentStore.searchByPrefix("el");

        } catch (IOException e) {
            e.printStackTrace(System.out);
        }
    }

    @Test
    public void searchByMetadata(){
        try {
            documentStore.put(new ByteArrayInputStream(text1.getBytes()), uri1, DocumentStore.DocumentFormat.TXT);
            documentStore.put(new ByteArrayInputStream(text2.getBytes()), uri2, DocumentStore.DocumentFormat.TXT);
            documentStore.put(new ByteArrayInputStream(text3.getBytes()), uri3, DocumentStore.DocumentFormat.BINARY);

            documentStore.setMetadata(uri1, key1, value1);
            documentStore.setMetadata(uri1, key2, value2);

            documentStore.setMetadata(uri2, key1, value1);
            documentStore.setMetadata(uri2, key2, value2);

            documentStore.setMetadata(uri3, key1, value1);

            Map<String, String> myMap = new HashMap<>();
            myMap.put(key1, value1);
            myMap.put(key2, value2);

            List<Document> myList = documentStore.searchByMetadata(myMap);

            assert(myList.contains(documentStore.get(uri1)));
            assert(myList.contains(documentStore.get(uri2)));
            assert(!myList.contains(documentStore.get(uri3)));

            documentStore.setMaxDocumentCount(2);

        } catch (IOException e){
            e.printStackTrace(System.out);
        }
    }

    @Test
    public void searchByMetadataUpdatesLastUsedTime(){
        try {
            documentStore.put(new ByteArrayInputStream(text1.getBytes()), uri1, DocumentStore.DocumentFormat.TXT);
            documentStore.put(new ByteArrayInputStream(text2.getBytes()), uri2, DocumentStore.DocumentFormat.TXT);
            documentStore.put(new ByteArrayInputStream(text3.getBytes()), uri3, DocumentStore.DocumentFormat.BINARY);

            documentStore.setMetadata(uri1, key1, value1);
            documentStore.setMetadata(uri2, key1, value1);

            Map<String, String> myMap = new HashMap<>();
            myMap.put(key1, value1);

            documentStore.searchByMetadata(myMap);
            documentStore.setMaxDocumentCount(2);

        } catch (IOException e){
            e.printStackTrace(System.out);
        }
    }

    @Test
    public void searchByMetadataHandlesDocumentsInDisk(){
        try {
            documentStore.put(new ByteArrayInputStream(text1.getBytes()), uri1, DocumentStore.DocumentFormat.TXT);
            documentStore.put(new ByteArrayInputStream(text2.getBytes()), uri2, DocumentStore.DocumentFormat.TXT);
            documentStore.put(new ByteArrayInputStream(text3.getBytes()), uri3, DocumentStore.DocumentFormat.BINARY);

            documentStore.setMetadata(uri1, key1, value1);
            documentStore.setMetadata(uri2, key1, value1);

            Map<String, String> myMap = new HashMap<>();
            myMap.put(key1, value1);

            documentStore.setMaxDocumentCount(2);
            documentStore.searchByMetadata(myMap);

        } catch (IOException e){
            e.printStackTrace(System.out);
        }
    }

    @Test
    public void deleteAll(){
        try {
            documentStore.put(new ByteArrayInputStream(text1.getBytes()), uri1, DocumentStore.DocumentFormat.TXT);
            documentStore.put(new ByteArrayInputStream(text1.getBytes()), uri2, DocumentStore.DocumentFormat.TXT);
            documentStore.put(new ByteArrayInputStream(text3.getBytes()), uri3, DocumentStore.DocumentFormat.BINARY);

            documentStore.deleteAll(text1);

            assertNull(documentStore.get(uri1));
            assertNull(documentStore.get(uri2));
            assert(documentStore.search(text1).isEmpty());

        } catch (IOException e){
            e.printStackTrace(System.out);
        }
    }

    @Test
    public void deleteAllDocumentOnDisk(){
        try {
            documentStore.put(new ByteArrayInputStream(text1.getBytes()), uri1, DocumentStore.DocumentFormat.TXT);
            documentStore.put(new ByteArrayInputStream(text1.getBytes()), uri2, DocumentStore.DocumentFormat.TXT);
            documentStore.put(new ByteArrayInputStream(text3.getBytes()), uri3, DocumentStore.DocumentFormat.BINARY);

            documentStore.setMaxDocumentCount(2);
            documentStore.deleteAll(text1);

            assertNull(documentStore.get(uri1));
            assertNull(documentStore.get(uri2));
            assert(documentStore.search(text1).isEmpty());

        } catch (IOException e){
            e.printStackTrace(System.out);
        }
    }

    @Test
    public void deleteAllUndo(){
        try {
            documentStore.put(new ByteArrayInputStream(text1.getBytes()), uri1, DocumentStore.DocumentFormat.TXT);
            documentStore.put(new ByteArrayInputStream(text1.getBytes()), uri2, DocumentStore.DocumentFormat.TXT);
            documentStore.put(new ByteArrayInputStream(text3.getBytes()), uri3, DocumentStore.DocumentFormat.BINARY);

            documentStore.setMaxDocumentCount(2);
            documentStore.deleteAll(text1);

            assertNull(documentStore.get(uri1));
            assertNull(documentStore.get(uri2));
            assert(documentStore.search(text1).isEmpty());

            documentStore.undo();

            assertNotNull(documentStore.get(uri1));
            assertNotNull(documentStore.get(uri2));
            assert(documentStore.search(text1).contains(documentStore.get(uri1)));
            assert(documentStore.search(text1).contains(documentStore.get(uri2)));

        } catch (IOException e){
            e.printStackTrace(System.out);
        }
    }

    @Test
    public void deleteAllWithPrefix(){
        try {
            documentStore.put(new ByteArrayInputStream("text11".getBytes()), uri1, DocumentStore.DocumentFormat.TXT);
            documentStore.put(new ByteArrayInputStream(text2.getBytes()), uri2, DocumentStore.DocumentFormat.TXT);
            documentStore.put(new ByteArrayInputStream("text13".getBytes()), uri3, DocumentStore.DocumentFormat.TXT);

            assert(documentStore.searchByPrefix("text1").contains(documentStore.get(uri1)));
            assert(documentStore.searchByPrefix("text1").contains(documentStore.get(uri3)));
            assert(!documentStore.searchByPrefix("text1").contains(documentStore.get(uri2)));

            documentStore.deleteAllWithPrefix("text1");

            assertNull(documentStore.get(uri1));
            assertNull(documentStore.get(uri3));
            assertNotNull(documentStore.get(uri2));
            assert(documentStore.search("text11").isEmpty());
            assert(documentStore.search("text13").isEmpty());
            assert(!documentStore.search("text2").isEmpty());

        } catch (IOException e){
            e.printStackTrace(System.out);
        }
    }

    @Test
    public void deleteAllWithPrefixOnDiskAndUndoOnSpecificURI(){
        try {
            documentStore.put(new ByteArrayInputStream("text13".getBytes()), uri3, DocumentStore.DocumentFormat.TXT);
            documentStore.put(new ByteArrayInputStream(text2.getBytes()), uri2, DocumentStore.DocumentFormat.TXT);
            documentStore.put(new ByteArrayInputStream("text11".getBytes()), uri1, DocumentStore.DocumentFormat.TXT);

            documentStore.setMaxDocumentCount(2);
            documentStore.deleteAllWithPrefix("text1");

            assertNull(documentStore.get(uri1));
            assertNull(documentStore.get(uri3));
            assertNotNull(documentStore.get(uri2));
            assert(documentStore.search("text11").isEmpty());
            assert(documentStore.search("text13").isEmpty());
            assert(!documentStore.search("text2").isEmpty());

            documentStore.undo(uri1);

        } catch (IOException e){
            e.printStackTrace(System.out);
        }
    }

    @Test
    public void searchByKeyWordAndMetadata(){
        try {
            documentStore.put(new ByteArrayInputStream(text1.getBytes()), uri1, DocumentStore.DocumentFormat.TXT);
            documentStore.put(new ByteArrayInputStream(text2.getBytes()), uri2, DocumentStore.DocumentFormat.TXT);
            documentStore.put(new ByteArrayInputStream(text3.getBytes()), uri3, DocumentStore.DocumentFormat.TXT);

            documentStore.setMetadata(uri1, key1, value1);
            documentStore.setMetadata(uri1, key2, value2);

            documentStore.setMetadata(uri2, key1, value1);

            documentStore.setMetadata(uri3, key1, value1);
            documentStore.setMetadata(uri3, key2, value2);

            Map<String, String> myMap = new HashMap<>();
            myMap.put(key1, value1);
            myMap.put(key2, value2);

            List<Document> myList = documentStore.searchByKeywordAndMetadata(text1, myMap);

            assert(myList.contains(documentStore.get(uri1)));
            assert(!myList.contains(documentStore.get(uri2)));
            assert(!myList.contains(documentStore.get(uri3)));

        } catch (IOException e){
            e.printStackTrace(System.out);
        }
    }

    @Test
    public void searchByKeyWordAndMetadataInDisk(){
        try {
            documentStore.put(new ByteArrayInputStream(text1.getBytes()), uri1, DocumentStore.DocumentFormat.TXT);
            documentStore.put(new ByteArrayInputStream(text2.getBytes()), uri2, DocumentStore.DocumentFormat.TXT);

            documentStore.setMetadata(uri1, key1, value1);

            Map<String, String> myMap = new HashMap<>();
            myMap.put(key1, value1);

            documentStore.searchByKeywordAndMetadata(text1, myMap);
            documentStore.get(uri2);

            documentStore.setMaxDocumentCount(1);
            documentStore.searchByKeywordAndMetadata(text1, myMap);

        } catch (IOException e){
            e.printStackTrace(System.out);
        }
    }

    @Test
    public void searchByKeyWordMetadataCorrectOrder(){
        try {
            documentStore.put(new ByteArrayInputStream("text1 text1 text1".getBytes()), uri1, DocumentStore.DocumentFormat.TXT);
            documentStore.put(new ByteArrayInputStream("text1 text2 text2 text2 text2".getBytes()), uri2, DocumentStore.DocumentFormat.TXT);
            documentStore.put(new ByteArrayInputStream("text1 text1".getBytes()), uri3, DocumentStore.DocumentFormat.TXT);

            documentStore.setMetadata(uri1, key1, value1);
            documentStore.setMetadata(uri2, key1, value1);
            documentStore.setMetadata(uri3, key1, value1);

            Map<String, String> myMap = new HashMap<>();
            myMap.put(key1, value1);

            List<Document> myList = documentStore.searchByKeywordAndMetadata(text1, myMap);
            assert(myList.get(0).equals(documentStore.get(uri1)));
            assert(myList.get(1).equals(documentStore.get(uri3)));
            assert(myList.get(2).equals(documentStore.get(uri2)));

        } catch (IOException e){
            e.printStackTrace(System.out);
        }
    }

    @Test
    public void searchByPrefixAndMetadata(){
        try {
            documentStore.put(new ByteArrayInputStream(text1.getBytes()), uri1, DocumentStore.DocumentFormat.TXT);
            documentStore.put(new ByteArrayInputStream(text2.getBytes()), uri2, DocumentStore.DocumentFormat.TXT);
            documentStore.put(new ByteArrayInputStream(text3.getBytes()), uri3, DocumentStore.DocumentFormat.TXT);

            documentStore.setMetadata(uri1, key1, value1);
            documentStore.setMetadata(uri1, key2, value2);

            documentStore.setMetadata(uri2, key1, value1);

            documentStore.setMetadata(uri3, key1, value1);
            documentStore.setMetadata(uri3, key2, value2);

            Map<String, String> myMap = new HashMap<>();
            myMap.put(key1, value1);
            myMap.put(key2, value2);

            List<Document> myList1 = documentStore.searchByPrefixAndMetadata("text", myMap);

            assert(myList1.contains(documentStore.get(uri1)));
            assert(!myList1.contains(documentStore.get(uri2)));
            assert(myList1.contains(documentStore.get(uri3)));

            List<Document> myList2 = documentStore.searchByPrefixAndMetadata("text", myMap);
            documentStore.setMaxDocumentCount(2);
        } catch (IOException e){
            e.printStackTrace(System.out);
        }
    }

    @Test
    public void deleteAllWithMetadataAndUndo(){
        try {
            documentStore.put(new ByteArrayInputStream(text1.getBytes()), uri1, DocumentStore.DocumentFormat.TXT);
            documentStore.put(new ByteArrayInputStream(text2.getBytes()), uri2, DocumentStore.DocumentFormat.TXT);
            documentStore.put(new ByteArrayInputStream(text3.getBytes()), uri3, DocumentStore.DocumentFormat.TXT);

            documentStore.setMetadata(uri1, key1, value1);
            documentStore.setMetadata(uri1, key2, value2);

            documentStore.setMetadata(uri2, key1, value1);

            documentStore.setMetadata(uri3, key1, value1);
            documentStore.setMetadata(uri3, key2, value2);

            Map<String, String> myMap = new HashMap<>();
            myMap.put(key1, value1);
            myMap.put(key2, value2);

            documentStore.deleteAllWithMetadata(myMap);

            assertNull(documentStore.get(uri1));
            assertNotNull(documentStore.get(uri2));
            assertNull(documentStore.get(uri3));

            documentStore.undo(uri3);

            assertNull(documentStore.get(uri1));
            assertNotNull(documentStore.get(uri2));
            assertNotNull(documentStore.get(uri3));

        } catch (IOException e){
            e.printStackTrace(System.out);
        }
    }

    @Test
    public void searchByPrefixMetadataCorrectOrder(){
        try {
            documentStore.put(new ByteArrayInputStream("text1 text12 text13".getBytes()), uri1, DocumentStore.DocumentFormat.TXT);
            documentStore.put(new ByteArrayInputStream("text1 text text text text".getBytes()), uri2, DocumentStore.DocumentFormat.TXT);
            documentStore.put(new ByteArrayInputStream("text1 text12".getBytes()), uri3, DocumentStore.DocumentFormat.TXT);

            documentStore.setMetadata(uri2, key1, value1);
            documentStore.setMetadata(uri3, key1, value1);

            Map<String, String> myMap = new HashMap<>();
            myMap.put(key1, value1);

            List<Document> myList = documentStore.searchByPrefixAndMetadata(text1, myMap);

            assert(myList.get(0).equals(documentStore.get(uri3)));
            assert(myList.get(1).equals(documentStore.get(uri2)));

        } catch (IOException e){
            e.printStackTrace(System.out);
        }
    }

    @Test
    public void deleteAllWithMetadataDeleteFromDiskAndUndo(){
        try {
            documentStore.put(new ByteArrayInputStream(text1.getBytes()), uri1, DocumentStore.DocumentFormat.TXT);
            documentStore.put(new ByteArrayInputStream(text2.getBytes()), uri2, DocumentStore.DocumentFormat.TXT);
            documentStore.put(new ByteArrayInputStream(text3.getBytes()), uri3, DocumentStore.DocumentFormat.BINARY);

            documentStore.setMetadata(uri1, key1, value1);
            documentStore.setMetadata(uri1, key2, value2);

            documentStore.setMetadata(uri2, key1, value1);

            documentStore.setMetadata(uri3, key1, value1);
            documentStore.setMetadata(uri3, key2, value2);

            Map<String, String> myMap = new HashMap<>();
            myMap.put(key1, value1);
            myMap.put(key2, value2);

//            documentStore.setMaxDocumentCount(2);
//
//            documentStore.deleteAllWithMetadata(myMap);
//
//            documentStore.undo(uri3);
//            documentStore.undo(uri1);
//            assert(!documentStore.search(text1).isEmpty());

        } catch (IOException e){
            e.printStackTrace(System.out);
        }
    }

    @Test
    public void deleteAllWithKeywordAndMetadata(){
        try {
            documentStore.put(new ByteArrayInputStream("text1".getBytes()), uri1, DocumentStore.DocumentFormat.TXT);
            documentStore.put(new ByteArrayInputStream("text2".getBytes()), uri2, DocumentStore.DocumentFormat.TXT);
            documentStore.put(new ByteArrayInputStream("text3 text1".getBytes()), uri3, DocumentStore.DocumentFormat.TXT);

            documentStore.setMetadata(uri1, key1, value1);
            documentStore.setMetadata(uri1, key2, value2);

            documentStore.setMetadata(uri2, key1, value1);

            documentStore.setMetadata(uri3, key1, value1);
            documentStore.setMetadata(uri3, key2, value2);

            Map<String, String> myMap = new HashMap<>();
            myMap.put(key1, value1);
            myMap.put(key2, value2);

            documentStore.deleteAllWithKeywordAndMetadata("text1", myMap);

            assertNull(documentStore.get(uri1));
            assertNotNull(documentStore.get(uri2));
            assertNull(documentStore.get(uri3));

        } catch (IOException e){
            e.printStackTrace(System.out);
        }
    }

    @Test
    public void deleteAllWithPrefixAndMetadataUndo(){
        try {
            documentStore.put(new ByteArrayInputStream("text1".getBytes()), uri1, DocumentStore.DocumentFormat.TXT);
            documentStore.put(new ByteArrayInputStream("text2".getBytes()), uri2, DocumentStore.DocumentFormat.TXT);
            documentStore.put(new ByteArrayInputStream("text3 text1".getBytes()), uri3, DocumentStore.DocumentFormat.TXT);

            documentStore.setMetadata(uri1, key1, value1);
            documentStore.setMetadata(uri1, key2, value2);

            documentStore.setMetadata(uri2, key1, value1);

            documentStore.setMetadata(uri3, key1, value1);
            documentStore.setMetadata(uri3, key2, value2);

            Map<String, String> myMap = new HashMap<>();
            myMap.put(key1, value1);
            myMap.put(key2, value2);

            documentStore.setMaxDocumentCount(1);

            documentStore.deleteAllWithKeywordAndMetadata("text1", myMap);
            documentStore.delete(uri2);

            documentStore.undo(uri1);
            documentStore.undo(uri3);

            assertNotNull(documentStore.get(uri1));
            assertNotNull(documentStore.get(uri3));
            assertNull(documentStore.get(uri2));

            documentStore.undo();

        } catch (IOException e){
            e.printStackTrace(System.out);
        }
    }

    @Test
    public void deleteAllWithPrefixAndMetadataAndUndo(){
        try {
            documentStore.put(new ByteArrayInputStream("text11".getBytes()), uri1, DocumentStore.DocumentFormat.TXT);
            documentStore.put(new ByteArrayInputStream("text2".getBytes()), uri2, DocumentStore.DocumentFormat.TXT);
            documentStore.put(new ByteArrayInputStream("text3 text1".getBytes()), uri3, DocumentStore.DocumentFormat.TXT);

            documentStore.setMetadata(uri1, key1, value1);
            documentStore.setMetadata(uri1, key2, value2);

            documentStore.setMetadata(uri2, key1, value1);

            documentStore.setMetadata(uri3, key1, value1);
            documentStore.setMetadata(uri3, key2, value2);

            Map<String, String> myMap = new HashMap<>();
            myMap.put(key1, value1);
            myMap.put(key2, value2);

            documentStore.setMaxDocumentCount(1);

            documentStore.deleteAllWithPrefixAndMetadata("text1", myMap);

            assertNull(documentStore.get(uri1));
            assertNotNull(documentStore.get(uri2));
            assertNull(documentStore.get(uri3));

            documentStore.undo();

            assertNotNull(documentStore.get(uri1));
            assertNotNull(documentStore.get(uri2));
            assertNotNull(documentStore.get(uri3));

        } catch (IOException e){
            e.printStackTrace(System.out);
        }
    }
}