package edu.yu.cs.com1320.project.stage4.impl;

import edu.yu.cs.com1320.project.stage4.Document;
import edu.yu.cs.com1320.project.stage4.DocumentStore;
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
            assertNull(documentStore.getMetadata(uri2, key2));
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

    }

    @Test
    public void deleteAllWithPrefix(){

    }
}

