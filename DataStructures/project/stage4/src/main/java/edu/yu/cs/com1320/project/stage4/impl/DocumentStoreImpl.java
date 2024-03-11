package edu.yu.cs.com1320.project.stage4.impl;

import edu.yu.cs.com1320.project.HashTable;
import edu.yu.cs.com1320.project.Stack;
import edu.yu.cs.com1320.project.impl.HashTableImpl;
import edu.yu.cs.com1320.project.impl.StackImpl;
import edu.yu.cs.com1320.project.stage4.Document;
import edu.yu.cs.com1320.project.stage4.DocumentStore;
import edu.yu.cs.com1320.project.undo.Command;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

public class DocumentStoreImpl implements DocumentStore {
    private final HashTable<URI, Document> documents = new HashTableImpl<>();
    private final Stack<Command> stack = new StackImpl<>();

    /**
     * set the given key-value metadata pair for the document at the given uri
     * @param uri;
     * @param key;
     * @param value;
     * @return the old value, or null if there was no previous value
     * @throws IllegalArgumentException if the uri is null or blank, if there is no document stored at that uri, or if the key is null or blank
     */
    public String setMetadata(URI uri, String key, String value){
        if (uri == null || uri.toString().isEmpty() || key == null || key.isEmpty() || !this.documents.containsKey(uri)) {
            throw new IllegalArgumentException();
        }

        if (getMetadata(uri, key) != null) {
            Document previousDocument = documents.get(uri);
            String previousValue = previousDocument.getMetadataValue(key);
            Consumer<URI> consumer = revertMetadata -> previousDocument.setMetadataValue(key, previousValue);
            stack.push(new Command(uri, consumer));
        }

        else{
            Document previousDocument = documents.get(uri);
            Consumer<URI> consumer = revertMetadata -> previousDocument.setMetadataValue(key, null);
            stack.push(new Command(uri, consumer));
        }

        return documents.get(uri).setMetadataValue(key, value);
    }

    /**
     * get the value corresponding to the given metadata key for the document at the given uri
     *
     * @param uri;
     * @param key;
     * @return the value, or null if there was no value
     * @throws IllegalArgumentException if the uri is null or blank, if there is no document stored at that uri, or if the key is null or blank
     */
    public String getMetadata(URI uri, String key){
        if (uri == null || uri.toString().isEmpty() || key == null || key.isEmpty() || !this.documents.containsKey(uri)) {
            throw new IllegalArgumentException();
        }

        return documents.get(uri).getMetadataValue(key);
    }

    /**
     * @param input  the document being put
     * @param uri    unique identifier for the document
     * @param format indicates which type of document format is being passed
     * @return if there is no previous doc at the given URI, return 0. If there is a previous doc, return the hashCode of the previous doc. If InputStream is null, this is a call to delete, and thus return either the hashCode of the deleted doc or 0 if there is no doc to delete.
     * @throws IOException              if there is an issue reading input
     * @throws IllegalArgumentException if uri is null or empty, or format is null
     */
    public int put(InputStream input, URI uri, DocumentFormat format) throws IOException{
        if (uri == null || uri.toString().isEmpty() || format == null){
            throw new IllegalArgumentException();
        }

        if (input == null){
            if(documents.containsKey(uri)) {
                int hashCode = documents.get(uri).hashCode();
                delete(uri);
                return hashCode;
            }

            else{
                return 0;
            }
        }

        if (format == DocumentFormat.BINARY){
            Document document = new DocumentImpl(uri, input.readAllBytes());
            return add(uri, document);
        }

        else{
            byte[] bytes = input.readAllBytes();
            String text = new String(bytes, StandardCharsets.UTF_8);
            Document document = new DocumentImpl(uri, text);
            return add(uri, document);
        }
    }

    /**
     * @param url the unique identifier of the document to get
     * @return the given document
     */
    public Document get(URI url) {
        return this.documents.get(url);
    }
    /**
     * @param url the unique identifier of the document to delete
     * @return true if the document is deleted, false if no document exists with that URI
     */
    public boolean delete(URI url){

        Document previousDocument = documents.get(url);
        Consumer<URI> consumer = restoreDocument -> documents.put(url, previousDocument);
        stack.push(new Command(url, consumer));

        return documents.put(url, null) != null;
    }

    private int add(URI uri, Document document){
        if (documents.containsKey(uri)){

            Document previousDocument = documents.get(uri);
            Consumer<URI> consumer = restoreDocument -> documents.put(uri, previousDocument);
            stack.push(new Command(uri, consumer));

            return documents.put(uri, document).hashCode();
        }

        Consumer<URI> consumer = restoreDocument -> documents.put(uri, null);
        Command command = new Command(uri, consumer);
        stack.push(command);

        documents.put(uri, document);
        return 0;
    }

    /**
     * undo the last put or delete command
     * @throws IllegalStateException if there are no actions to be undone, i.e. the command stack is empty
     */
    public void undo() throws IllegalStateException{
        if (stack.size() == 0){
            throw new IllegalStateException();
        }

        stack.pop().undo();

    }

    /**
     * undo the last put or delete that was done with the given URI as its key
     * @param url;
     * @throws IllegalStateException if there are no actions on the command stack for the given URI
     */
    public void undo(URI url) throws IllegalStateException{
        if (stack.size() == 0){
            throw new IllegalStateException("The stack has no commands");
        }

        Stack<Command> helper = new StackImpl<>();

        while(!stack.peek().getUri().equals(url)){
            helper.push(stack.pop());
            if (stack.size() == 0){
                throw new IllegalStateException("The stack has no commands with the specified url");
            }
        }

        stack.pop().undo();

        while(helper.size() != 0){
            stack.push(helper.pop());
        }
    }

    /**
     * Retrieve all documents whose text contains the given keyword.
     * Documents are returned in sorted, descending order, sorted by the number of times the keyword appears in the document.
     * Search is CASE SENSITIVE.
     * @param keyword
     * @return a List of the matches. If there are no matches, return an empty list.
     */
    public List<Document> search(String keyword) {

    }
    /**
     * Retrieve all documents that contain text which starts with the given prefix
     * Documents are returned in sorted, descending order, sorted by the number of times the prefix appears in the document.
     * Search is CASE SENSITIVE.
     * @param keywordPrefix
     * @return a List of the matches. If there are no matches, return an empty list.
     */
    public List<Document> searchByPrefix(String keywordPrefix){

    }

    /**
     * Completely remove any trace of any document which contains the given keyword
     * Search is CASE SENSITIVE.
     * @param keyword
     * @return a Set of URIs of the documents that were deleted.
     */
    public Set<URI> deleteAll(String keyword){

    }

    /**
     * Completely remove any trace of any document which contains a word that has the given prefix
     * Search is CASE SENSITIVE.
     * @param keywordPrefix
     * @return a Set of URIs of the documents that were deleted.
     */
    public Set<URI> deleteAllWithPrefix(String keywordPrefix){

    }

    /**
     * @param keysValues metadata key-value pairs to search for
     * @return a List of all documents whose metadata contains ALL OF the given values for the given keys. If no documents contain all the given key-value pairs, return an empty list.
     */
    public List<Document> searchByMetadata(Map<String,String> keysValues){

    }

    /**
     * Retrieve all documents whose text contains the given keyword AND which has the given key-value pairs in its metadata
     * Documents are returned in sorted, descending order, sorted by the number of times the keyword appears in the document.
     * Search is CASE SENSITIVE.
     * @param keyword
     * @param keysValues
     * @return a List of the matches. If there are no matches, return an empty list.
     */
    public List<Document> searchByKeywordAndMetadata(String keyword, Map<String,String> keysValues){

    }

    /**
     * Retrieve all documents that contain text which starts with the given prefix AND which has the given key-value pairs in its metadata
     * Documents are returned in sorted, descending order, sorted by the number of times the prefix appears in the document.
     * Search is CASE SENSITIVE.
     * @param keywordPrefix
     * @return a List of the matches. If there are no matches, return an empty list.
     */
    public List<Document> searchByPrefixAndMetadata(String keywordPrefix,Map<String,String> keysValues){

    }

    /**
     * Completely remove any trace of any document which has the given key-value pairs in its metadata
     * Search is CASE SENSITIVE.
     * @return a Set of URIs of the documents that were deleted.
     */
    public Set<URI> deleteAllWithMetadata(Map<String,String> keysValues){

    }

    /**
     * Completely remove any trace of any document which contains the given keyword AND which has the given key-value pairs in its metadata
     * Search is CASE SENSITIVE.
     * @param keyword
     * @return a Set of URIs of the documents that were deleted.
     */
    public Set<URI> deleteAllWithKeywordAndMetadata(String keyword,Map<String,String> keysValues){

    }

    /**
     * Completely remove any trace of any document which contains a word that has the given prefix AND which has the given key-value pairs in its metadata
     * Search is CASE SENSITIVE.
     * @param keywordPrefix
     * @return a Set of URIs of the documents that were deleted.
     */
    public Set<URI> deleteAllWithPrefixAndMetadata(String keywordPrefix,Map<String,String> keysValues){

    }
}
