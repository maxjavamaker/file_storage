package edu.yu.cs.com1320.project.stage5.impl;

import edu.yu.cs.com1320.project.HashTable;
import edu.yu.cs.com1320.project.MinHeap;
import edu.yu.cs.com1320.project.Stack;
import edu.yu.cs.com1320.project.impl.HashTableImpl;
import edu.yu.cs.com1320.project.impl.MinHeapImpl;
import edu.yu.cs.com1320.project.impl.StackImpl;
import edu.yu.cs.com1320.project.impl.TrieImpl;
import edu.yu.cs.com1320.project.stage5.Document;
import edu.yu.cs.com1320.project.stage5.DocumentStore;
import edu.yu.cs.com1320.project.undo.CommandSet;
import edu.yu.cs.com1320.project.undo.GenericCommand;
import edu.yu.cs.com1320.project.undo.Undoable;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;

public class DocumentStoreImpl implements DocumentStore {
    private final HashTable<URI, Document> documents = new HashTableImpl<>();
    private final TrieImpl<Document> documentTrie = new TrieImpl<>();
    private final Stack<Undoable> stack = new StackImpl<>();
    private final MinHeap<Document> minHeap = new MinHeapImpl<>();
    private Integer docLimit = null;
    private boolean calledDocLimit = false;
    private Integer memoryLimit = null;
    private boolean calledMemoryLimit = false;

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

        this.undoSetMetaDataLogic(this.documents.get(uri), key);
        documents.get(uri).setLastUseTime(System.nanoTime()); //update last used time
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

        documents.get(uri).setLastUseTime(System.nanoTime());  //update last used time
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
                this.delete(uri);
                return hashCode;
            }

            else{
                return 0;
            }
        }

        if (format == DocumentFormat.BINARY){
            Document document = new DocumentImpl(uri, input.readAllBytes());
            return add(uri, document); //don't add to trie if it's a binary document
        }

        else{
            byte[] bytes = input.readAllBytes();
            String text = new String(bytes, StandardCharsets.UTF_8);
            Document document = new DocumentImpl(uri, text);
            this.addDocumentWordsToTrie(document); //add every word in the document to the trie
            return add(uri, document);
        }
    }

    /**
     * @param url the unique identifier of the document to get
     * @return the given document
     */
    public Document get(URI url) {
        documents.get(url).setLastUseTime(System.nanoTime());
        return this.documents.get(url);
    }
    /**
     * @param url the unique identifier of the document to delete
     * @return true if the document is deleted, false if no document exists with that URI
     */
    public boolean delete(URI url){
        if (this.documents.get(url) == null){  //check if the document is in the hashtable, if not return false
            return false;
        }

        this.undoDeleteLogic(url);
        this.removeDocumentWordsFromTrie(this.documents.get(url));  //remove every word in the document from the trie
        return documents.put(url, null) != null;
    }

    private void removeDocumentWordsFromTrie(Document document){
        for (String word : document.getWords()){  //removing every word in the document from the trie
            documentTrie.delete(word, document);
        }
    }

    private int add(URI uri, Document document){
        document.setLastUseTime(System.nanoTime());  //update last used time
        minHeap.reHeapify(document);

        if (documents.containsKey(uri)){
            this.removeDocumentWordsFromTrie(this.documents.get(uri));
            this.undoDeleteLogic(uri);  //logic to undo changing document is the same as the logic for undoing delete

            return documents.put(uri, document).hashCode();
        }

        documents.put(uri, document);
        this.undoSetDocumentLogic(uri);
        return 0;
    }

    private void addDocumentWordsToTrie(Document document){
        for (String word : document.getWords()){  //adding every word in the document to the trie
            documentTrie.put(word, document);
        }
    }

    /**
     * undo the last put or delete command
     * @throws IllegalStateException if there are no actions to be undone, i.e. the command stack is empty
     */
    public void undo() throws IllegalStateException {
        if (stack.size() == 0) {
            throw new IllegalStateException();
        }

        if (stack.peek() instanceof CommandSet<?>){
            CommandSet<?> temp = (CommandSet<?>) stack.pop();
            temp.undoAll();
        }

        else {
            stack.pop().undo();
        }
    }

    /**
     * undo the last put or delete that was done with the given URI as its key
     * @param url;
     * @throws IllegalStateException if there are no actions on the command stack for the given URI
     */
    public void undo(URI url) throws IllegalStateException{
        boolean undidAction = false;

        Stack<Undoable> helper = new StackImpl<>();

        while(stack.size() != 0 && !undidAction){
            if (stack.peek() instanceof CommandSet<?>){
                if (this.undoURLCommandSet(stack.peek(), url)){
                    undidAction = true;
                } else {
                    helper.push(stack.pop());
                }
            } else{
                if (this.undoURLGenericCommand(stack.peek(), url)){
                    undidAction = true;
                } else{
                    helper.push(stack.pop());
                }
            }
        }
        while(helper.size() != 0) {
            stack.push(helper.pop());
        }

        if (!undidAction){
            throw new IllegalStateException("uri not found on the stack");
        }
    }

    private boolean undoURLCommandSet(Undoable undo, URI url){
        boolean undidAction = false;
        CommandSet<?> genericSet;
        CommandSet<URI> uriSet;

        genericSet = (CommandSet<?>) undo;
        uriSet = (CommandSet<URI>) genericSet;

        if (uriSet.undo(url)){  //if it contains the target undo just the genericCommand on the target
            undidAction = true;
            if (uriSet.size() == 0){  //if all commands were undone remove command set from the stack
                stack.pop();
            }
        }

        return undidAction;
    }

    private boolean undoURLGenericCommand(Undoable undo, URI url){
        boolean undidAction = false;
        GenericCommand<?> genericCommand;
        GenericCommand<URI> genericURI;

        genericCommand = (GenericCommand<?>) undo;
        genericURI = (GenericCommand<URI>) genericCommand;

        if (genericURI.getTarget().equals(url)){
            genericURI.undo();
            undidAction = true;
            stack.pop();
        }

        return undidAction;
    }

    private void undoSetMetaDataLogic(Document document, String key){
        if (this.getMetadata(document.getKey(), key) != null) {  //check if the metadata previously existed
            String previousValue = document.getMetadataValue(key);

            Consumer<URI> consumer = revertMetadata -> document.setMetadataValue(key, previousValue);
            stack.push(new GenericCommand<>(document.getKey(), consumer));
        }

        else{
            Consumer<URI> consumer = revertMetadata -> document.setMetadataValue(key, null);
            stack.push(new GenericCommand<>(document.getKey(), consumer));
        }
    }

    private void undoSetDocumentLogic(URI uri){
        Consumer<URI> consumer = restoreDocument -> {
            this.removeDocumentWordsFromTrie(this.documents.get(uri));
            documents.put(uri, null);
        };

        stack.push(new GenericCommand<>(uri, consumer));
    }

    private void undoDeleteLogic(URI url){  //same logic for changing a document
        Document previousDocument = documents.get(url);
        Consumer<URI> consumer = restoreDocument -> {
            documents.put(url, previousDocument);
            this.addDocumentWordsToTrie(this.documents.get(url));
        };
        stack.push(new GenericCommand<>(url, consumer));
    }

    /**
     * Retrieve all documents whose text contains the given keyword.
     * Documents are returned in sorted, descending order, sorted by the number of times the keyword appears in the document.
     * Search is CASE SENSITIVE.
     * @param keyword;
     * @return a List of the matches. If there are no matches, return an empty list.
     */
    public List<Document> search(String keyword) {
        if (keyword == null) {
            throw new IllegalArgumentException();
        }

        return documentTrie.getSorted(keyword, createComparator(keyword));  //create comparator and pass it to tries' get sorted method
    }

    private Comparator<Document> createComparator(String keyword){
        Comparator<Document> wordCountComparator = Comparator.comparing(document -> document.wordCount(keyword));
        return wordCountComparator.reversed();
    }
    /**
     * Retrieve all documents that contain text which starts with the given prefix
     * Documents are returned in sorted, descending order, sorted by the number of times the prefix appears in the document.
     * Search is CASE SENSITIVE.
     * @param keywordPrefix;
     * @return a List of the matches. If there are no matches, return an empty list.
     */
    public List<Document> searchByPrefix(String keywordPrefix){
        return documentTrie.getAllWithPrefixSorted(keywordPrefix, createPrefixComparator(keywordPrefix)); //create comparator and pass it to tries' getAllWithPrefixSorted method
    }

    private Comparator<Document> createPrefixComparator(String keyword){
        Comparator<Document> wordCountComparator = Comparator.comparingInt(document -> {  //create a list that sort documents in order of the number of times a prefix appears
            int wordCount = 0;
            for (String word : document.getWords()){
                if (word.startsWith(keyword)){
                    wordCount++;
                }
            }

            return wordCount;
        });

        return wordCountComparator.reversed(); //return the comparator so its sorts in descending order
    }

    /**
     * Completely remove any trace of any document which contains the given keyword
     * Search is CASE SENSITIVE.
     * @param keyword;
     * @return a Set of URIs of the documents that were deleted.
     */
    public Set<URI> deleteAll(String keyword){
        List<Document> docs = this.search(keyword);  //get list of documents with the keyword
        return this.deleteAndUndoLogic(docs);  //remove all traces of the document, create undo logic
    }

    /**
     * Completely remove any trace of any document which contains a word that has the given prefix
     * Search is CASE SENSITIVE.
     * @param keywordPrefix;
     * @return a Set of URIs of the documents that were deleted.
     */
    public Set<URI> deleteAllWithPrefix(String keywordPrefix){
        List<Document> docs = this.searchByPrefix(keywordPrefix);  //get list of documents with the keyword
        return this.deleteAndUndoLogic(docs);  //remove all traces of the document, create undo logic
    }

    /**
     * @param keysValues metadata key-value pairs to search for
     * @return a List of all documents whose metadata contains all the given values for the given keys. If no documents contain all the given key-value pairs, return an empty list.
     */
    public List<Document> searchByMetadata(Map<String,String> keysValues){
        List<Document>  docList = new ArrayList<>();

        for (Document doc : this.documents.values()){  //check each document in the hashtable to see if its metadata matches the keysValues map
            boolean addToList = true;
            for (String key : keysValues.keySet()){
                if (doc.getMetadata().get(key) == null || !doc.getMetadata().get(key).equals(keysValues.get(key))){
                    addToList = false;
                    break;
                }
            }

            if (addToList){  //if the metadata matches add the doc to the arrayList
                docList.add(doc);
            }
        }

        return docList;
    }

    /**
     * Retrieve all documents whose text contains the given keyword AND which has the given key-value pairs in its metadata
     * Documents are returned in sorted, descending order, sorted by the number of times the keyword appears in the document.
     * Search is CASE SENSITIVE.
     * @param keyword;
     * @param keysValues;
     * @return a List of the matches. If there are no matches, return an empty list.
     */
    public List<Document> searchByKeywordAndMetadata(String keyword, Map<String,String> keysValues){
        List<Document> metadataDocList = this.searchByMetadata(keysValues);  //List of documents with matching metadata
        List<Document> keywordDocList = this.search(keyword);  //List of documents with the keyword sorted by number of occurrences
        List<Document> finalList = new ArrayList<>();

        for (Document doc : metadataDocList){  //if a doc has the keyword and metadata add to the finalList
            if (keywordDocList.contains(doc)){
                finalList.add(doc);
            }
        }

        return finalList;
    }

    /**
     * Retrieve all documents that contain text which starts with the given prefix AND which has the given key-value pairs in its metadata
     * Documents are returned in sorted, descending order, sorted by the number of times the prefix appears in the document.
     * Search is CASE SENSITIVE.
     * @param keywordPrefix;
     * @return a List of the matches. If there are no matches, return an empty list.
     */
    public List<Document> searchByPrefixAndMetadata(String keywordPrefix,Map<String,String> keysValues){
        List<Document> metadataDocList = this.searchByMetadata(keysValues);  //List of documents with matching metadata
        List<Document> keywordDocList = this.searchByPrefix(keywordPrefix);  //List of documents with the Prefix sorted by number of occurrences
        List<Document> finalList = new ArrayList<>();

        for (Document doc : metadataDocList){  //if a doc has the Prefix and metadata add to the finalList
            if (keywordDocList.contains(doc)){
                finalList.add(doc);
            }
        }

        return finalList;
    }

    /**
     * Completely remove any trace of any document which has the given key-value pairs in its metadata
     * Search is CASE SENSITIVE.
     * @return a Set of URIs of the documents that were deleted.
     */
    public Set<URI> deleteAllWithMetadata(Map<String,String> keysValues){
        List<Document> docs = this.searchByMetadata(keysValues); //get documents with matching metadata
        return this.deleteAndUndoLogic(docs);  //remove all traces of the documents, create undo logic
    }

    /**
     * Completely remove any trace of any document which contains the given keyword AND which has the given key-value pairs in its metadata
     * Search is CASE SENSITIVE.
     * @param keyword;
     * @return a Set of URIs of the documents that were deleted.
     */
    public Set<URI> deleteAllWithKeywordAndMetadata(String keyword,Map<String,String> keysValues){
        List<Document>  docs = this.searchByKeywordAndMetadata(keyword, keysValues); //get docs with matching metadata and keyword
        return this.deleteAndUndoLogic(docs);  //remove all traces of the document, create undo logic
    }

    /**
     * Completely remove any trace of any document which contains a word that has the given prefix AND which has the given key-value pairs in its metadata
     * Search is CASE SENSITIVE.
     * @param keywordPrefix;
     * @return a Set of URIs of the documents that were deleted.
     */
    public Set<URI> deleteAllWithPrefixAndMetadata(String keywordPrefix,Map<String,String> keysValues){
        List<Document> docs = this.searchByPrefixAndMetadata(keywordPrefix, keysValues); //get docs with matching metadata
        return this.deleteAndUndoLogic(docs);  //remove all traces of the document, create undo logic
    }

    private Set<URI> deleteAndUndoLogic(List<Document> docs){
        boolean createCommandSet = (docs.size() > 1);
        Set<URI> uriSet = new HashSet<>();

        if (docs.isEmpty()){  //if there are no documents to delete return an empty set
            return uriSet;
        }

        CommandSet<URI> commandSet = new CommandSet<>();
        GenericCommand<URI> genericCommand;

        for (Document document : docs){  //cycle through every document
            document.setLastUseTime(System.nanoTime());  //update last used time
            for (String word : document.getWords()) {  //cycle through every word in the document
                this.documentTrie.delete(word, document);  //delete the document at the word
            }

            Consumer<URI> consumer = documentPut -> {  //undo logic
                for (String word : document.getWords()) {
                    this.documentTrie.put(word, document);
                }
                document.setLastUseTime(System.nanoTime());  //update last used time
                this.documents.put(document.getKey(), document);
            };

            uriSet.add(document.getKey());  //add uri to the set

            this.documents.put(document.getKey(), null);  //delete document from hashtable
            genericCommand = new GenericCommand<>(document.getKey(), consumer);

            if (createCommandSet) { //add generic command to the command set
                commandSet.addCommand(genericCommand);
            }

            else{  //push the generic command set to the stack
                this.stack.push(genericCommand);
                return uriSet;
            }
        }

        this.stack.push(commandSet);
        return uriSet;
    }

    /**
     * set maximum number of documents that may be stored
     * @param limit;
     * @throws IllegalArgumentException if limit < 1
     */
    public void setMaxDocumentCount(int limit){
        if (limit < 1){
            throw new IllegalArgumentException("limit cannot be less than 1");
        }

        if (!this.calledDocLimit){
            this.docLimit = limit;
        }
    }

    /**
     * set maximum number of bytes of memory that may be used by all the documents in memory combined
     * @param limit;
     * @throws IllegalArgumentException if limit < 1
     */
    public void setMaxDocumentBytes(int limit){
        if (limit < 1){
            throw new IllegalArgumentException("limit cannot be less than 1");
        }

        if (!this.calledMemoryLimit){
            this.memoryLimit = limit;
        }
    }
}
