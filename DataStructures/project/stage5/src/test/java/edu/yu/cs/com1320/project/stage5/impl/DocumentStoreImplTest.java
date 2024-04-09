package edu.yu.cs.com1320.project.stage5.impl;

import edu.yu.cs.com1320.project.stage5.Document;
import edu.yu.cs.com1320.project.stage5.DocumentStore;
import edu.yu.cs.com1320.project.stage5.impl.DocumentStoreImpl;
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

    String apple1;
    String apple2;
    String apples;
    String Apple;

    byte[] byteapple;
    byte[] byteapples;
    byte[] byteApple;

    InputStream applestream;
    InputStream applesstream;
    InputStream Applestream;

    String text1;
    String text2;
    String key1;
    String key2;
    String value1;
    String value2;
    byte[] byteArray1;
    byte[] byteArray2;
    InputStream inputStream1;
    InputStream inputStream2;
    URI uri1;
    URI uri2;
    URI uri3;
    DocumentStore documentStore;

    @BeforeEach
    public void setup() {
        try {
            apple1 = "apple here and apple and apples there, keep the doctor away";
            apple2 = "apple here and apple and apples there, keep the doctor away";
            apples = "apple here and an applesty theres, keep the doctors away?";
            Apple = "Apple Here And";

            byteapple = apple1.getBytes();
            byteapples = apples.getBytes();
            byteApple = Apple.getBytes();

            applestream = new ByteArrayInputStream(byteapple);
            applesstream = new ByteArrayInputStream(byteapples);
            Applestream = new ByteArrayInputStream(byteApple);


            text1 = "text1";
            text2 = "text2";
            key1 = "key1";
            key2 = "key2";
            value1 = "value1";
            value2 = "value2";
            byteArray1 = text1.getBytes();
            byteArray2 = text2.getBytes();
            inputStream1 = new ByteArrayInputStream(byteArray1);
            inputStream2 = new ByteArrayInputStream(byteArray2);
            uri1 = new URI("uri1");
            uri2 = new URI("uri2");
            uri3 = new URI("");
            documentStore = new DocumentStoreImpl();

        }
        catch(URISyntaxException e){
        }
    }

    @Test
    public void putNullURI(){
        assertThrows(IllegalArgumentException.class, () -> {
            uri1 = null;
            documentStore.put(inputStream1, uri1, DocumentStore.DocumentFormat.TXT);});
    }
    @Test
    public void putEmptyURI(){
        assertThrows(IllegalArgumentException.class, () -> {
            documentStore.put(inputStream1, uri3, DocumentStore.DocumentFormat.TXT);});
    }

    @Test
    public void putNullFormat(){
        assertThrows(IllegalArgumentException.class, () -> {
            documentStore.put(inputStream1, uri1, null);});
    }

    @Test
    public void putNullInputStreamReturn0(){
        inputStream1 = null;
        try {
            assertEquals(0, documentStore.put(inputStream1, uri1, DocumentStore.DocumentFormat.TXT));
        }
        catch(IOException e){

        }

    }

    @Test
    public void putNullInputStreamReturnHashcode(){
        try {
            documentStore.put(inputStream1, uri1, DocumentStore.DocumentFormat.TXT);
            inputStream1 = null;
            assertEquals(documentStore.get(uri1).hashCode(), documentStore.put(inputStream2, uri1, DocumentStore.DocumentFormat.TXT));
        }
        catch(IOException e){

        }
    }
    @Test
    public void putBinaryReturn0(){
        try {
            assertEquals(0, documentStore.put(inputStream1, uri1, DocumentStore.DocumentFormat.BINARY));
        }
        catch(IOException e){

        }
    }

    @Test
    public void putBinaryReturnHashcode(){
        try {
            documentStore.put(inputStream1, uri1, DocumentStore.DocumentFormat.BINARY);
            assertEquals(documentStore.get(uri1).hashCode(), documentStore.put(inputStream2, uri1, DocumentStore.DocumentFormat.BINARY));
        }
        catch(IOException e){

        }
    }

    @Test
    public void putTextReturnNull(){
        try {
            assertEquals(0, documentStore.put(inputStream1, uri1, DocumentStore.DocumentFormat.TXT));
        }
        catch(IOException e){

        }
    }
    @Test
    public void putTextReturnHashcode(){
        try {
            documentStore.put(inputStream1, uri1, DocumentStore.DocumentFormat.BINARY);
            assertEquals(documentStore.get(uri1).hashCode(), documentStore.put(inputStream2, uri1, DocumentStore.DocumentFormat.TXT));
        }
        catch(IOException e){

        }
    }

    @Test
    public void setMetaDataPreviousValue(){
        try{
            documentStore.put(inputStream1, uri1, DocumentStore.DocumentFormat.TXT);
            documentStore.setMetadata(uri1, key1, value1);
            assertEquals(value1, documentStore.setMetadata(uri1, key1, value2));
        }
        catch(IOException e){

        }
    }

    @Test
    public void setMetaDataNoPreviousValue(){
        try {
            documentStore.put(inputStream1, uri1, DocumentStore.DocumentFormat.TXT);
            assertEquals(null, documentStore.setMetadata(uri1, key1, value1));
        }
        catch(IOException e){

        }
    }

    @Test
    public void setMetaDataNullURI(){
        uri1 = null;
        assertThrows(IllegalArgumentException.class, () -> {
            documentStore.setMetadata(uri1, key1, value1);});
    }

    @Test
    public void setMetaDataEmptyURI(){
        assertThrows(IllegalArgumentException.class, () -> {
            documentStore.setMetadata(uri3, key1, value1);});
    }

    @Test
    public void setMetaDataNullKey(){
        assertThrows(IllegalArgumentException.class, () -> {
            key1 = null;
            documentStore.setMetadata(uri1, key1, value1);});
    }

    @Test
    public void setMetaDataNullEmptyKey(){
        assertThrows(IllegalArgumentException.class, () -> {
            key1 = "";
            documentStore.setMetadata(uri1, key1, value1);});
    }

    @Test
    public void getMetaDataNullURI(){
        try{
            documentStore.put(inputStream1, uri1, DocumentStore.DocumentFormat.TXT);
            documentStore.setMetadata(uri1, key1, value1);
        }
        catch(IOException e){

        }
        uri1 = null;
        assertThrows(IllegalArgumentException.class, () ->
                documentStore.getMetadata(uri1, key1));
    }

    @Test
    public void getMetaDataEmptyURI(){
        try{
            documentStore.put(inputStream1, uri1, DocumentStore.DocumentFormat.TXT);
            documentStore.setMetadata(uri1, key1, value1);
        }
        catch(IOException e){

        }
        assertThrows(IllegalArgumentException.class, () ->
                documentStore.getMetadata(uri3, key1));
    }

    @Test
    public void getMetaDataNullKey(){
        try{
            documentStore.put(inputStream1, uri1, DocumentStore.DocumentFormat.TXT);
            documentStore.setMetadata(uri1, key1, value1);
        }
        catch(IOException e){

        }
        key1 = null;
        assertThrows(IllegalArgumentException.class, () ->
                documentStore.getMetadata(uri1, key1));
    }

    @Test
    public void getMetaDataEmptyKey(){
        try{
            documentStore.put(inputStream1, uri1, DocumentStore.DocumentFormat.TXT);
            documentStore.setMetadata(uri1, key1, value1);
        }
        catch(IOException e){

        }
        key1 = "";
        assertThrows(IllegalArgumentException.class, () ->
                documentStore.getMetadata(uri1, key1));

    }
    @Test
    public void getMetaDataNoDocument(){
        try{
            documentStore.put(inputStream1, uri1, DocumentStore.DocumentFormat.TXT);
            documentStore.setMetadata(uri1, key1, value1);
        }
        catch(IOException e){

        }
        assertThrows(IllegalArgumentException.class, () ->
                documentStore.getMetadata(uri2, key1));
    }

    @Test
    public void getMetaDataValue(){
        try{
            documentStore.put(inputStream1, uri1, DocumentStore.DocumentFormat.TXT);
            documentStore.setMetadata(uri1, key1, value1);
        }
        catch(IOException e){

        }
        assertEquals(value1, documentStore.getMetadata(uri1, key1));
    }

    @Test
    public void getMetaDataNoValue(){
        try{
            documentStore.put(inputStream1, uri1, DocumentStore.DocumentFormat.TXT);
            documentStore.setMetadata(uri1, key1, value1);
        }
        catch(IOException e){

        }
        assertEquals(null, documentStore.getMetadata(uri1, key2));

    }

    @Test
    public void get(){
        try{
            documentStore.put(inputStream1, uri1, DocumentStore.DocumentFormat.TXT);
            Document document = new DocumentImpl(uri1, text1);
            assertEquals(document, documentStore.get(uri1));
        }
        catch(IOException e){

        }
    }

    @Test
    public void deleteTrue(){
        try{
            documentStore.put(inputStream1, uri1, DocumentStore.DocumentFormat.TXT);
            assertTrue(documentStore.delete(uri1));
        }
        catch(IOException e){

        }
    }

    @Test
    public void deleteFalse(){
        assertFalse(documentStore.delete(uri1));
    }

    @Test
    public void undoCreateMetadata(){
        try {
            documentStore.put(inputStream1, uri1, DocumentStore.DocumentFormat.TXT);
            documentStore.setMetadata(uri1, key1, value1);
            documentStore.undo();
            assertNull(documentStore.getMetadata(uri1, key1));
        }
        catch(IOException e){

        }
    }

    @Test
    public void undoChangeMetaData(){
        try {
            documentStore.put(inputStream1, uri1, DocumentStore.DocumentFormat.TXT);
            documentStore.setMetadata(uri1, key1, value1);
            documentStore.setMetadata(uri1, key1, value2);
            documentStore.undo();
            assertEquals(value1, documentStore.getMetadata(uri1, key1));
        }
        catch(IOException e){

        }
    }

    @Test
    public void undoCreateDocument(){
        try {
            documentStore.put(inputStream1, uri1, DocumentStore.DocumentFormat.TXT);
            documentStore.undo();
            assertNull(documentStore.get(uri1));
        }
        catch(IOException e){

        }
    }

    @Test
    public void undoChangeDocument(){
        try {
            documentStore.put(inputStream1, uri1, DocumentStore.DocumentFormat.TXT);
            documentStore.put(inputStream2, uri1, DocumentStore.DocumentFormat.TXT);
            documentStore.undo();
            Document document1 = new DocumentImpl(uri1, "text1");
            assertEquals(document1, documentStore.get(uri1));
        }
        catch(IOException e){

        }
    }

    @Test
    public void undoDelete(){
        try {
            documentStore.put(inputStream1, uri1, DocumentStore.DocumentFormat.TXT);
            documentStore.delete(uri1);
            documentStore.undo();
            assertNotNull(documentStore.get(uri1));
        }
        catch(IOException e){

        }
    }

    @Test
    public void undoLastURI(){
        try {
            documentStore.put(inputStream1, uri1, DocumentStore.DocumentFormat.TXT);
            documentStore.put(inputStream2, uri2, DocumentStore.DocumentFormat.TXT);
            documentStore.setMetadata(uri2, key1, value1);
            documentStore.setMetadata(uri2, key2, value1);
            documentStore.undo(uri1);
            assertNull(documentStore.get(uri1));
            documentStore.undo(uri2);
            //assertNull(documentStore.getMetadata(uri2, key2));
        }
        catch(IOException e){

        }
    }

    @Test
    public void search(){
        try {
            documentStore.put(applestream, uri1, DocumentStore.DocumentFormat.TXT);
            documentStore.put(applesstream, uri2, DocumentStore.DocumentFormat.TXT);

            List<Document> docList = this.documentStore.search("apple");

            Document appleDoc = new DocumentImpl(uri1,"apple here and apple and apples there, keep the doctor away");
            Document applesDoc = new DocumentImpl(uri2, "apple here and an applesty theres, keep the doctors away?");

            assertEquals(appleDoc, docList.get(0));
            assertEquals(applesDoc, docList.get(1));
        }
        catch(IOException e){

        }
    }

    @Test
    public void searchEmptyList(){
        try {
            documentStore.put(applestream, uri1, DocumentStore.DocumentFormat.TXT);
            documentStore.put(applesstream, uri2, DocumentStore.DocumentFormat.TXT);

            List<Document> docList = this.documentStore.search("dumble");

            assert(docList.isEmpty());
        }
        catch(IOException e){

        }
    }

    @Test
    public void searchIsCaseSensitive(){

        try {
            documentStore.put(applestream, uri1, DocumentStore.DocumentFormat.TXT);
            documentStore.put(Applestream, uri2, DocumentStore.DocumentFormat.TXT);

            List<Document> docList = this.documentStore.search("Apple");

            Document appleDoc = new DocumentImpl(uri1,"apple here and apple and apples there, keep the doctor away");
            Document AppleDoc = new DocumentImpl(uri2, "Apple Here And");

            assert(!docList.contains(appleDoc));
            assert(docList.contains(AppleDoc));
        }
        catch(IOException e){

        }
    }

    @Test
    public void searchByPrefix(){
        try {
            documentStore.put(applestream, uri1, DocumentStore.DocumentFormat.TXT);
            documentStore.put(applesstream, uri2, DocumentStore.DocumentFormat.TXT);

            List<Document> docList = this.documentStore.searchByPrefix("apples");
            List<Document> docList2 = this.documentStore.searchByPrefix("apple");
            List<Document> docList3 = this.documentStore.searchByPrefix("aPple");

            Document appleDoc = new DocumentImpl(uri1,"apple here and apple and apples there, keep the doctor away");
            Document applesDoc = new DocumentImpl(uri2, "apple here and an applesty theres, keep the doctors away?");

            assert(docList.contains(appleDoc));
            assert(docList.contains(applesDoc));

            assertEquals(appleDoc, docList.get(0));
            assertEquals(applesDoc, docList.get(1));

            assert(docList3.isEmpty());

        }
        catch(IOException e){

        }
    }

    @Test
    public void deleteAll(){
        try {
            documentStore.put(applestream, uri1, DocumentStore.DocumentFormat.TXT);
            documentStore.put(applesstream, uri2, DocumentStore.DocumentFormat.TXT);


            Set<URI> docList = this.documentStore.deleteAll("doctor");
            Set<URI> docList2 = this.documentStore.deleteAll("habanero");


            assert(docList.contains(uri1));
            assert(!docList.contains(uri2));
            assert(docList2.size() == 0);

            assertEquals(0, this.documentStore.search("apples").size());
            assertEquals(1, this.documentStore.search("doctors").size());
            assertEquals(1, this.documentStore.search("applesty").size());






            Document appleDoc = new DocumentImpl(uri1,"apple here and apple and apples there, keep the doctor away");
            Document applesDoc = new DocumentImpl(uri2, "apple here and an applesty theres, keep the doctors away?");


        }
        catch(IOException e){

        }
    }

    @Test
    public void undoDeleteAll(){
        try {
            documentStore.put(applestream, uri1, DocumentStore.DocumentFormat.TXT);
            documentStore.put(applesstream, uri2, DocumentStore.DocumentFormat.TXT);


            Set<URI> docList = this.documentStore.deleteAll("doctor");
            assertEquals(0, this.documentStore.search("doctor").size());

            Document appleDoc = new DocumentImpl(uri1,"apple here and apple and apples there, keep the doctor away");
            Document applesDoc = new DocumentImpl(uri2, "apple here and an applesty theres, keep the doctors away?");

            this.documentStore.undo(uri1);
            assertEquals(1, this.documentStore.search("doctor").size());
        }
        catch(IOException e){

        }
    }

    @Test
    public void deleteAllWithPrefixAndUndoLogic(){
        try {
            documentStore.put(applestream, uri1, DocumentStore.DocumentFormat.TXT);
            documentStore.put(applesstream, uri2, DocumentStore.DocumentFormat.TXT);

            Set<URI> docList2 = this.documentStore.deleteAllWithPrefix("habanero");
            Set<URI> docList = this.documentStore.deleteAllWithPrefix("doct");

            assert(docList.contains(uri1));
            assert(docList.contains(uri2));
            assert(docList2.size() == 0);

            assertEquals(0, this.documentStore.search("apples").size());
            assertEquals(0, this.documentStore.search("doctors").size());
            assertEquals(0, this.documentStore.search("applesty").size());

            assertEquals(0, this.documentStore.searchByPrefix("doct").size());
            this.documentStore.undo();
            assertEquals(2, this.documentStore.searchByPrefix("doct").size());

            this.documentStore.deleteAllWithPrefix("Doct");
            assertEquals(2, this.documentStore.searchByPrefix("doct").size());

            assert(this.documentStore.deleteAllWithPrefix("Doct").isEmpty());



            Document appleDoc = new DocumentImpl(uri1,"apple here and apple and apples there, keep the doctor away");
            Document applesDoc = new DocumentImpl(uri2, "apple here and an applesty theres, keep the doctors away?");


        }
        catch(IOException e){

        }

    }

    @Test
    public void searchByMetadata(){
        try {
            Document doc1 = new DocumentImpl(uri1, "text1");
            Document doc2 = new DocumentImpl(uri2, "text2");


            this.documentStore.put(inputStream1, uri1, DocumentStore.DocumentFormat.TXT);
            this.documentStore.put(inputStream2, uri2, DocumentStore.DocumentFormat.TXT);

            this.documentStore.setMetadata(uri1, "key1", "value1");
            this.documentStore.setMetadata(uri2, "key2", "value1");

            Map<String, String> values = new HashMap<>();
            values.put("key1", "value1");

            List<Document> docs = this.documentStore.searchByMetadata(values);
            assert (docs.contains(doc1));
            assert (!docs.contains(doc2));

            Map<String, String> values2 = new HashMap<>();
            values2.put("derrick", "henry");
            assert(this.documentStore.searchByMetadata(values2).isEmpty());

        }
        catch(IOException e){

        }
    }

    @Test
    public void searchByKeywordAndMetadata(){
        try {
            Document doc1 = new DocumentImpl(uri1, "text1");
            Document doc2 = new DocumentImpl(uri2, "text2");


            this.documentStore.put(inputStream1, uri1, DocumentStore.DocumentFormat.TXT);
            this.documentStore.put(inputStream2, uri2, DocumentStore.DocumentFormat.TXT);

            this.documentStore.setMetadata(uri1, "key1", "value1");
            this.documentStore.setMetadata(uri2, "key2", "value1");

            Map<String, String> values = new HashMap<>();
            values.put("key1", "value1");

            List<Document> docs = this.documentStore.searchByKeywordAndMetadata("text1", values);
            assert (docs.contains(doc1));
            assert (!docs.contains(doc2));

            Map<String, String> values2 = new HashMap<>();
            values2.put("derrick", "henry");
            assert(this.documentStore.searchByKeywordAndMetadata("text1", values2).isEmpty());

        }
        catch(IOException e){

        }
    }

    @Test
    public void searchByPrefixAndMetadata(){
        try {
            Document doc1 = new DocumentImpl(uri1, "text1");
            Document doc2 = new DocumentImpl(uri2, "text2");


            this.documentStore.put(inputStream1, uri1, DocumentStore.DocumentFormat.TXT);
            this.documentStore.put(inputStream2, uri2, DocumentStore.DocumentFormat.TXT);

            this.documentStore.setMetadata(uri1, "key1", "value1");
            this.documentStore.setMetadata(uri2, "key2", "value1");

            Map<String, String> values = new HashMap<>();
            values.put("key1", "value1");

            List<Document> docs = this.documentStore.searchByPrefixAndMetadata("tex", values);
            assert (docs.contains(doc1));
            assert (!docs.contains(doc2));

            Map<String, String> values2 = new HashMap<>();
            values2.put("derrick", "henry");
            assert(this.documentStore.searchByPrefixAndMetadata("text1", values2).isEmpty());

        }
        catch(IOException e){

        }
    }

    @Test
    public void deleteAllWithMetadata(){
        try {
            Document doc1 = new DocumentImpl(uri1, "text1");
            Document doc2 = new DocumentImpl(uri2, "text2");


            this.documentStore.put(inputStream1, uri1, DocumentStore.DocumentFormat.TXT);
            this.documentStore.put(inputStream2, uri2, DocumentStore.DocumentFormat.TXT);

            this.documentStore.setMetadata(uri1, "key1", "value1");
            this.documentStore.setMetadata(uri1, "key3", "value3");
            this.documentStore.setMetadata(uri2, "key2", "value1");

            Map<String, String> values = new HashMap<>();
            values.put("key1", "value1");
            values.put("key3", "value3");


            Set<URI> docs = this.documentStore.deleteAllWithMetadata(values);
            assert (docs.contains(uri1));
            assert (!docs.contains(uri2));

            Map<String, String> values2 = new HashMap<>();
            values2.put("derrick", "henry");
            assert(this.documentStore.deleteAllWithMetadata(values2).isEmpty());

        }
        catch(IOException e){

        }
    }

    @Test
    public void deleteAllWithMetadataUndo(){
        try {
            Document doc1 = new DocumentImpl(uri1, "text1");
            Document doc2 = new DocumentImpl(uri2, "text2");


            this.documentStore.put(inputStream1, uri1, DocumentStore.DocumentFormat.TXT);
            this.documentStore.put(inputStream2, uri2, DocumentStore.DocumentFormat.TXT);

            this.documentStore.setMetadata(uri1, "key1", "value1");
            this.documentStore.setMetadata(uri1, "key3", "value3");
            this.documentStore.setMetadata(uri2, "key2", "value1");

            Map<String, String> values = new HashMap<>();
            values.put("key1", "value1");
            values.put("key3", "value3");


            Set<URI> docs = this.documentStore.deleteAllWithMetadata(values);
            assertEquals(0, this.documentStore.search("text1").size());
            assert (docs.contains(uri1));
            assert (!docs.contains(uri2));

            this.documentStore.undo();
            assertEquals(1, this.documentStore.search("text1").size());

            Map<String, String> values2 = new HashMap<>();
            values2.put("derrick", "henry");
            assert(this.documentStore.deleteAllWithMetadata(values2).isEmpty());

        }

        catch(IOException e){

        }
    }

    @Test
    public void deleteAllWithPrefixAndMetadata(){
        try {
            Document doc1 = new DocumentImpl(uri1, "text1");
            Document doc2 = new DocumentImpl(uri2, "text2");


            this.documentStore.put(inputStream1, uri1, DocumentStore.DocumentFormat.TXT);
            this.documentStore.put(inputStream2, uri2, DocumentStore.DocumentFormat.TXT);

            this.documentStore.setMetadata(uri1, "key1", "value1");
            this.documentStore.setMetadata(uri1, "key3", "value3");
            this.documentStore.setMetadata(uri2, "key2", "value1");

            Map<String, String> values = new HashMap<>();
            values.put("key1", "value1");
            values.put("key3", "value3");


            Set<URI> docs = this.documentStore.deleteAllWithPrefixAndMetadata("hex", values);

            assert (!docs.contains(uri1));
            assert (!docs.contains(uri2));

            Set<URI> docs2 = this.documentStore.deleteAllWithPrefixAndMetadata("tex", values);

            assert (docs2.contains(uri1));
            assertEquals(0, this.documentStore.search("text1").size());
            assert (!docs2.contains(uri2));

            this.documentStore.undo();

            assertEquals(1, this.documentStore.search("text1").size());

        }

        catch(IOException e){

        }
    }

    @Test
    public void replaceDocumentInTrie(){
        try{
            this.documentStore.put(inputStream1, uri1, DocumentStore.DocumentFormat.TXT);
            assertEquals(1, this.documentStore.search("text1").size());

            this.documentStore.put(inputStream2, uri1, DocumentStore.DocumentFormat.TXT);
            assertEquals(0, this.documentStore.search("text1").size());


        }
        catch(IOException e){

        }
    }

    @Test
    public void undoOneCommandSet(){
        try{
            URI uri5 = new URI("applesauce");
            URI uri6 = new URI("howyadoin");

            this.documentStore.put(inputStream1, uri5, DocumentStore.DocumentFormat.TXT);
            this.documentStore.put(inputStream2, uri6, DocumentStore.DocumentFormat.TXT);


            this.documentStore.deleteAllWithPrefix("tex");

            this.documentStore.undo(uri5);

            assertNotNull(this.documentStore.get(uri5)); //these tests are the issues
            assertNull(this.documentStore.get(uri6));

            assert(!this.documentStore.search("text1").isEmpty());
            assert(this.documentStore.search("text2").isEmpty());
        }
        catch(IOException | URISyntaxException e){

        }
    }

    @Test
    public void deleteDocs(){
        try{
            this.documentStore.put(inputStream1, uri1, DocumentStore.DocumentFormat.TXT);
            this.documentStore.put(inputStream2, uri2, DocumentStore.DocumentFormat.TXT);

            this.documentStore.deleteAllWithPrefix("tex");
            assert(this.documentStore.get(uri1) == null);
            assert(this.documentStore.get(uri2) == null);

            this.documentStore.undo();

            assert(this.documentStore.get(uri1) != null);
            assert(this.documentStore.get(uri2) != null);


        }
        catch(IOException e){

        }
    }

    @Test
    public void undoDeletePutWordsBack(){
        try{
            this.documentStore.put(inputStream1, uri1, DocumentStore.DocumentFormat.TXT);


            this.documentStore.put(null, uri1, DocumentStore.DocumentFormat.TXT);

            assert(this.documentStore.get(uri1) == null);
            assert(this.documentStore.search("text1").isEmpty());

            this.documentStore.undo();

            assert(!this.documentStore.search("text1").isEmpty());



        }
        catch(IOException e){

        }
    }

    @Test
    public void  undoAllOnCommandStack(){
        try{
            this.documentStore.put(inputStream1, uri1, DocumentStore.DocumentFormat.TXT);
            this.documentStore.put(inputStream2, uri2, DocumentStore.DocumentFormat.TXT);

            this.documentStore.deleteAllWithPrefix("tex");


            this.documentStore.undo(uri1);
            this.documentStore.undo(uri2);


            assert(this.documentStore.get(uri1) != null);
            assert(this.documentStore.get(uri2) != null);


        }
        catch(IOException e){

        }
    }

    /*@Test
    public void putAddsDocToHeap() {
        try {
            Document document1 = new DocumentImpl(uri1, text1);
            documentStore.put(inputStream1, uri1, DocumentStore.DocumentFormat.TXT);
            assert(this.documentStore.peek().equals(document1));
            documentStore.put(inputStream2, uri2, DocumentStore.DocumentFormat.TXT);
            assert(this.documentStore.peek().equals(document1));
        }
        catch(IOException e){

        }

    }

    @Test
    public void setLimitUnderOne(){
        assertThrows(IllegalArgumentException.class, () -> {
            this.documentStore.setMaxDocumentCount(0);});
        assertThrows(IllegalArgumentException.class, () -> {
            this.documentStore.setMaxDocumentBytes(0);});
    }

    @Test
    public void complyWithDocLimits(){
        try {
            Document document1 = new DocumentImpl(uri1, text1);
            Document document2 = new DocumentImpl(uri2, text2);
            documentStore.put(inputStream1, uri1, DocumentStore.DocumentFormat.TXT);
            documentStore.put(inputStream2, uri2, DocumentStore.DocumentFormat.TXT);
            assert(this.documentStore.peek().equals(document1));
            this.documentStore.setMaxDocumentCount(1);
            assert(this.documentStore.peek().equals(document2));
            assert(this.documentStore.get(uri1) == null);
            assert(this.documentStore.search("text1").isEmpty());
        }
        catch(IOException e){

        }
    }

    @Test
    public void complyWithMemoryLimits(){
        try {
            Document document1 = new DocumentImpl(uri1, text1);
            Document document2 = new DocumentImpl(uri2, text2);
            documentStore.put(inputStream1, uri1, DocumentStore.DocumentFormat.TXT);
            documentStore.put(inputStream2, uri2, DocumentStore.DocumentFormat.TXT);
            assert(this.documentStore.peek().equals(document1));
            this.documentStore.setMaxDocumentBytes(9);
            assert(this.documentStore.peek().equals(document2));
            assert(this.documentStore.get(uri1) == null);
            assert(this.documentStore.search("text1").isEmpty());
        }
        catch(IOException e){

        }
    }

    @Test
    public void complyWithMemoryLimitsMultipleDocs(){
        try {
            URI uri3 = new URI("3");
            URI uri4 = new URI("4");
            URI uri5 = new URI("5");

            InputStream inputStream3 = new ByteArrayInputStream("h".getBytes());
            InputStream inputStream4 = new ByteArrayInputStream("g".getBytes());
            InputStream inputStream5 = new ByteArrayInputStream("k".getBytes());

            Document document1 = new DocumentImpl(uri1, text1);
            Document document2 = new DocumentImpl(uri2, text2);
            Document document3 = new DocumentImpl(uri3, "h");
            Document document4 = new DocumentImpl(uri4, "g");
            Document document5 = new DocumentImpl(uri5, "k");

            documentStore.put(inputStream1, uri1, DocumentStore.DocumentFormat.TXT);
            documentStore.put(inputStream2, uri2, DocumentStore.DocumentFormat.TXT);
            documentStore.put(inputStream3, uri3, DocumentStore.DocumentFormat.TXT);
            documentStore.put(inputStream4, uri4, DocumentStore.DocumentFormat.TXT);
            documentStore.put(inputStream5, uri5, DocumentStore.DocumentFormat.TXT);

            assert(this.documentStore.peek().equals(document1));
            this.documentStore.setMaxDocumentBytes(1);
            assert(this.documentStore.peek().equals(document5));
            assert(this.documentStore.get(uri1) == null);
            assert(this.documentStore.search("text1").isEmpty());
            assert(this.documentStore.peek().equals(document5));
            assertThrows(IllegalStateException.class, () -> {
                this.documentStore.undo(uri4);});
            assert(this.documentStore.get(uri5) != null);
            this.documentStore.undo(uri5);
            assert(this.documentStore.get(uri5) == null);
        }
        catch(IOException | URISyntaxException e){

        }
    }

    @Test
    public void metadataUpdatesDoc(){
        try {
            Document document1 = new DocumentImpl(uri1, text1);
            Document document2 = new DocumentImpl(uri2, text2);
            this.documentStore.put(inputStream1, uri1, DocumentStore.DocumentFormat.TXT);
            this.documentStore.put(inputStream2, uri2, DocumentStore.DocumentFormat.TXT);
            assert(this.documentStore.peek().equals(document1));
            this.documentStore.setMetadata(uri1, "key", "value");
            assert(this.documentStore.peek().equals(document2));

        }
        catch(IOException e){

        }
    }

    @Test
    public void undoUpdatesDoc(){
        try {
            Document document1 = new DocumentImpl(uri1, text1);
            Document document2 = new DocumentImpl(uri2, text2);
            this.documentStore.put(inputStream1, uri1, DocumentStore.DocumentFormat.TXT);
            this.documentStore.put(inputStream2, uri2, DocumentStore.DocumentFormat.TXT);
            this.documentStore.setMetadata(uri2, "key", "value");
            this.documentStore.setMetadata(uri1, "key", "value");
            assert(this.documentStore.peek().equals(document2));
            this.documentStore.undo(uri2);
            assert(this.documentStore.peek().equals(document1));
        }
        catch(IOException e){

        }
    }

    @Test
    public void undoPutRemovesFromHeap(){
        try {
            Document document1 = new DocumentImpl(uri1, text1);
            this.documentStore.put(inputStream1, uri1, DocumentStore.DocumentFormat.TXT);
            assert(this.documentStore.peek() != null);
            assert(!this.documentStore.search("text1").isEmpty());
            this.documentStore.undo();
            assert(this.documentStore.peek() == null);
            assert(this.documentStore.search("text1").isEmpty());
        }
        catch(IOException e){

        }
    }

    @Test
    public void searchUpdatesDoc(){
        try {
            Document document1 = new DocumentImpl(uri1, text1);
            Document document2 = new DocumentImpl(uri2, text2);
            this.documentStore.put(inputStream1, uri1, DocumentStore.DocumentFormat.TXT);
            this.documentStore.put(inputStream2, uri2, DocumentStore.DocumentFormat.TXT);

            long i = this.documentStore.get(uri1).getLastUseTime();
            long j = this.documentStore.get(uri2).getLastUseTime();
            this.documentStore.search("text1");
            assert(this.documentStore.get(uri1).getLastUseTime() != i);
            assert(this.documentStore.get(uri2).getLastUseTime() == j);


        }
        catch(IOException e){

        }
    }

    @Test
    public void deleteDeletesFromHeap(){
        try {
            Document document1 = new DocumentImpl(uri1, text1);
            Document document2 = new DocumentImpl(uri2, text2);
            this.documentStore.put(inputStream1, uri1, DocumentStore.DocumentFormat.TXT);
            this.documentStore.put(inputStream2, uri2, DocumentStore.DocumentFormat.TXT);

            this.documentStore.deleteAllWithPrefix("text1");
            assert(this.documentStore.peek().equals(document2));
            this.documentStore.deleteAllWithPrefix("text");
            assert(this.documentStore.peek() == null);


        }
        catch(IOException e){

        }
    }

    @Test
    public void docsHaveSameNanoTime(){
        try {
            Document document1 = new DocumentImpl(uri1, text1);
            Document document2 = new DocumentImpl(uri2, text2);
            this.documentStore.put(inputStream1, uri1, DocumentStore.DocumentFormat.TXT);
            this.documentStore.put(inputStream2, uri2, DocumentStore.DocumentFormat.TXT);

            List<Document> doc  = documentStore.searchByPrefix("t");
            assert(doc.get(0).getLastUseTime() == doc.get(1).getLastUseTime());




        }
        catch(IOException e){

        }
    }

    @Test
    public void undoDocsHaveSameNanoTime(){
        try {
            Document document1 = new DocumentImpl(uri1, text1);
            Document document2 = new DocumentImpl(uri2, text2);
            this.documentStore.put(inputStream1, uri1, DocumentStore.DocumentFormat.TXT);
            this.documentStore.put(inputStream2, uri2, DocumentStore.DocumentFormat.TXT);
            this.documentStore.deleteAllWithPrefix("t");
            this.documentStore.undo();
            assert(this.documentStore.get(uri1).getLastUseTime() == this.documentStore.get(uri2).getLastUseTime());




        }
        catch(IOException e){

        }
    }*/

    @Test
    public void addingViolatesMemoryLimit(){
        try {
            this.documentStore.setMaxDocumentBytes(9);
            this.documentStore.put(inputStream1, uri1, DocumentStore.DocumentFormat.TXT);
            assertThrows(IllegalArgumentException.class, () -> {
                this.documentStore.put(inputStream2, uri2, DocumentStore.DocumentFormat.TXT);});
            this.documentStore.setMaxDocumentCount(2);
            assertThrows(IllegalArgumentException.class, () -> {
                this.documentStore.put(inputStream2, uri2, DocumentStore.DocumentFormat.TXT);});
        }
        catch(IOException e){

        }
    }
}

