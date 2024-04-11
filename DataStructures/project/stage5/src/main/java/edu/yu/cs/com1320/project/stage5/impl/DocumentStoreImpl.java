package edu.yu.cs.com1320.project.stage5.impl;

import edu.yu.cs.com1320.project.HashTable;
import edu.yu.cs.com1320.project.Stack;
import edu.yu.cs.com1320.project.impl.HashTableImpl;
import edu.yu.cs.com1320.project.impl.StackImpl;
import edu.yu.cs.com1320.project.impl.TrieImpl;
import edu.yu.cs.com1320.project.stage5.Document;
import edu.yu.cs.com1320.project.stage5.DocumentStore;
import edu.yu.cs.com1320.project.undo.CommandSet;
import edu.yu.cs.com1320.project.undo.GenericCommand;
import edu.yu.cs.com1320.project.undo.Undoable;
import edu.yu.cs.com1320.project.impl.MinHeapImpl;
import edu.yu.cs.com1320.project.MinHeap;


import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;

public class DocumentStoreImpl implements DocumentStore {
    private final HashTable<URI, Document> docHashTable = new HashTableImpl<>();
    private final TrieImpl<Document> docTrie = new TrieImpl<>();
    private final Stack<Undoable>undoStack = new StackImpl<>();
    private final MinHeap<Document> docHeap = new MinHeapImpl<>();
    private int docLimit;
    private int memoryLimit;

    /**
     * set the given key-value metadata pair for the document at the given uri
     * @param uri;
     * @param key;
     * @param value;
     * @return the old value, or null if there was no previous value
     * @throws IllegalArgumentException if the uri is null or blank, if there is no document stored at that uri, or if the key is null or blank
     */
    public String setMetadata(URI uri, String key, String value){
        if (uri == null || uri.toString().isEmpty() || key == null || key.isEmpty() || !this.docHashTable.containsKey(uri)) {
            throw new IllegalArgumentException();
        }

        this.updateDoc(uri);
        this.undoSetMetaDataLogic(this.docHashTable.get(uri), key);
        return docHashTable.get(uri).setMetadataValue(key, value);
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
        if (uri == null || uri.toString().isEmpty() || key == null || key.isEmpty() || !this.docHashTable.containsKey(uri)) {
            throw new IllegalArgumentException();
        }

        this.updateDoc(uri);
        return docHashTable.get(uri).getMetadataValue(key);
    }

    /**
     * @param input the document being put
     * @param uri unique identifier for the document
     * @param format indicates which type of document format is being passed
     * @return if there is no previous doc at the given URI, return 0. If there is a previous doc, return the hashCode of the previous doc. If InputStream is null, this is a delete, and thus return either the hashCode of the deleted doc or 0 if there is no doc to delete.
     * @throws IOException if there is an issue reading input
     * @throws IllegalArgumentException if url or format are null, OR IF THE MEMORY FOOTPRINT OF THE DOCUMENT IS > MAX DOCUMENT BYTES
     */
    public int put(InputStream input, URI uri, DocumentFormat format) throws IOException{
        if (uri == null || uri.toString().isEmpty() || format == null){
            throw new IllegalArgumentException();
        }

        if (input == null){
            if(docHashTable.containsKey(uri)) {
                int hashCode = docHashTable.get(uri).hashCode();
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
        if (this.docHashTable.get(url) != null){
            this.updateDoc(url);
        }
        return this.docHashTable.get(url);
    }
    /**
     * @param url the unique identifier of the document to delete
     * @return true if the document is deleted, false if no document exists with that URI
     */
    public boolean delete(URI url){
        if (this.docHashTable.get(url) == null){  //check if the document is in the hashtable, if not return false
            return false;
        }

        this.undoDeleteLogic(url);
        this.deleteDocFromHeap(this.docHashTable.get(url));  //delete doc from heap
        this.removeDocumentWordsFromTrie(this.docHashTable.get(url));  //remove every word in the document from the trie
        return docHashTable.put(url, null) != null;
    }

    private void removeDocumentWordsFromTrie(Document document){
        for (String word : document.getWords()){  //removing every word in the document from the trie
            docTrie.delete(word, document);
        }
    }

    private int add(URI uri, Document document){
        this.docExceedsLimit(document);

        if (docHashTable.containsKey(uri)){
            this.removeDocumentWordsFromTrie(this.docHashTable.get(uri));
            this.undoDeleteLogic(uri);  //logic to undo changing document is the same as the logic for undoing delete

            int hashCode = docHashTable.put(uri, document).hashCode();
            this.docHeapInsert(document);
            return hashCode;
        }

        docHashTable.put(uri, document);
        this.docHeapInsert(document);
        this.undoSetDocumentLogic(uri);
        return 0;
    }

    private void docExceedsLimit(Document doc){
        boolean typeText = doc.getDocumentTxt() != null;
        if (typeText){
            if (this.memoryLimit != 0 && doc.getDocumentTxt().getBytes().length > this.memoryLimit){
                throw new IllegalArgumentException();
            }
        }
        else{
            if (this.memoryLimit != 0 && doc.getDocumentBinaryData().length > this.memoryLimit){
                throw new IllegalArgumentException();
            }
        }
    }

    private void addDocumentWordsToTrie(Document document){
        for (String word : document.getWords()){  //adding every word in the document to the trie
            docTrie.put(word, document);
        }
    }

    /**
     * undo the last put or delete command
     * @throws IllegalStateException if there are no actions to be undone, i.e. the command stack is empty
     */
    public void undo() throws IllegalStateException {
        if (undoStack.size() == 0) {
            throw new IllegalStateException();
        }

        if (undoStack.peek() instanceof CommandSet<?>){
            CommandSet<?> temp = (CommandSet<?>) undoStack.pop();
            temp.undoAll();
        }

        else {
            undoStack.pop().undo();
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

        while(undoStack.size() != 0 && !undidAction){
            if (undoStack.peek() instanceof CommandSet<?>){
                if (this.undoURLCommandSet(undoStack.peek(), url)){
                    undidAction = true;
                } else {
                    helper.push(undoStack.pop());
                }
            } else{
                if (this.undoURLGenericCommand(undoStack.peek(), url)){
                    undidAction = true;
                } else{
                    helper.push(undoStack.pop());
                }
            }
        }
        while(helper.size() != 0) {
            undoStack.push(helper.pop());
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
                undoStack.pop();
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
            undoStack.pop();
        }

        return undidAction;
    }

    private void checkDocumentMemory(Document doc){
        if (this.memoryLimit == 0){
            return;
        }
        if (doc.getDocumentTxt() == null){  //this means its a txt document
            if ((doc.getDocumentBinaryData().length > this.memoryLimit)){
                throw new IllegalArgumentException();
            }
        }
        else{
            if (doc.getDocumentTxt().getBytes().length > this.memoryLimit){
                throw new IllegalArgumentException();
            }
        }
    }

    private void undoSetMetaDataLogic(Document document, String key){
        if (this.getMetadata(document.getKey(), key) != null) {  //check if the metadata previously existed
            String previousValue = document.getMetadataValue(key);

            Consumer<URI> consumer = revertMetadata -> {
                document.setMetadataValue(key, previousValue);
                this.updateDoc(document.getKey());
            };
            undoStack.push(new GenericCommand<>(document.getKey(), consumer));
        }

        else{
            Consumer<URI> consumer = revertMetadata -> {
                document.setMetadataValue(key, null);
                this.updateDoc(document.getKey());
            };
            undoStack.push(new GenericCommand<>(document.getKey(), consumer));
        }
    }

    private void undoSetDocumentLogic(URI uri){
        Consumer<URI> consumer = restoreDocument -> {  //don't update nanoTime because document will be deleted
            this.removeDocumentWordsFromTrie(this.docHashTable.get(uri));
            this.deleteDocFromHeap(this.docHashTable.get(uri));
            docHashTable.put(uri, null);
        };

        undoStack.push(new GenericCommand<>(uri, consumer));
    }

    private void undoDeleteLogic(URI url){  //same logic for changing a document
        Document previousDocument = docHashTable.get(url);
        Consumer<URI> consumer = restoreDocument -> {
            this.checkDocumentMemory(previousDocument);  //make sure the document memory isn't higher than the limit
            docHashTable.put(url, previousDocument);
            this.addDocumentWordsToTrie(this.docHashTable.get(url));
            this.docHashTable.get(url).setLastUseTime(System.nanoTime());
            this.docHeap.insert(this.docHashTable.get(url));
        };
        undoStack.push(new GenericCommand<>(url, consumer));
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

        return this.updateDoc(docTrie.getSorted(keyword, createComparator(keyword)));  //create comparator and pass it to tries' get sorted method, update all the docs nanoTime
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
        return this.updateDoc(docTrie.getAllWithPrefixSorted(keywordPrefix, createPrefixComparator(keywordPrefix))); //create comparator and pass it to tries' getAllWithPrefixSorted method, update all docs nanoTime
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

        for (Document doc : this.docHashTable.values()){  //check each document in the hashtable to see if its metadata matches the keysValues map
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

        return this.updateDoc(docList);
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

        return this.updateDoc(finalList);
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

        return this.updateDoc(finalList);
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
        long time = System.nanoTime();
        for (Document document : docs){  //cycle through every document
            for (String word : document.getWords()) {  //cycle through every word in the document
                this.docTrie.delete(word, document);  //delete the document at the word
            }

            Consumer<URI> consumer = documentPut -> {  //undo logic
                this.checkDocumentMemory(document);  //if document is higher than memory limit throw exception
                for (String word : document.getWords()) {
                    this.docTrie.put(word, document);
                }
                this.docHashTable.put(document.getKey(), document);
                this.docHeap.insert(document);
                document.setLastUseTime(time);
                this.docHeap.reHeapify(document);
            };

            uriSet.add(document.getKey());  //add uri to the set

            this.docHashTable.put(document.getKey(), null);  //delete document from hashtable
            this.deleteDocFromHeap(document);  //delete doc from the heap
            genericCommand = new GenericCommand<>(document.getKey(), consumer);

            if (createCommandSet) { //add generic command to the command set
                commandSet.addCommand(genericCommand);
            }

            else{  //push the generic command set to the stack
                this.undoStack.push(genericCommand);
                return uriSet;
            }
        }

        this.undoStack.push(commandSet);
        return uriSet;
    }

    private void deleteDocFromHeap(Document doc){
        doc.setLastUseTime(0);
        this.docHeap.reHeapify(doc);
        this.docHeap.remove();
    }

    private List<Document> updateDoc(List<Document> docList){
        long time = System.nanoTime();
        for (Document doc : docList){
            doc.setLastUseTime(time);  //update last used time
            this.docHeap.reHeapify(doc);  //order the heap
        }

        return docList;
    }

    private void updateDoc(URI uri){
        docHashTable.get(uri).setLastUseTime(System.nanoTime());  //update last used time
        this.docHeap.reHeapify(docHashTable.get(uri));  //order the heap
    }

    private void docHeapInsert(Document document){
        this.docHeap.insert(document);

        //while the doc limit or memory limit is violated, remove documents from the heap, trie, hashtable, and undoStack
        while((docLimit != 0 && this.docLimit < this.docHashTable.size()) || (this.memoryLimit != 0 && this.memoryLimit< this.getTotalMemory())){
            this.delete(document.getKey());
            this.docHeap.remove();
            this.deleteDocumentFromStack(document.getKey());
        }
    }

    private void deleteDocumentFromStack(URI uri){
        Stack<Undoable> helper = new StackImpl<>();

        while(undoStack.size() != 0){
            if (undoStack.peek() instanceof CommandSet<?> temp1) {
                CommandSet<URI> temp2 = (CommandSet<URI>) temp1;
                if (temp2.containsTarget(uri)){
                    undoStack.pop();
                }
                else{
                    helper.push(undoStack.pop());
                }

            }
            else{
                GenericCommand<?> temp1 = (GenericCommand<?>) undoStack.peek();
                GenericCommand<URI> temp2 = (GenericCommand<URI>) temp1;

                if (temp2.getTarget().equals(uri)){
                    undoStack.pop();
                }
                else{
                    helper.push(undoStack.pop());
                }
            }
        }
        while(helper.size() != 0) {
            undoStack.push(helper.pop());
        }

    }

    private int getTotalMemory(){
        int memory = 0;
        for (Document doc : this.docHashTable.values()) {
            if (doc.getDocumentTxt() != null) {
                memory += doc.getDocumentTxt().getBytes().length;
            }
            else {
                memory += doc.getDocumentBinaryData().length;
            }
        }

        return memory;
    }

    private void complyWithLimits(){
        if (this.docLimit != 0){
            while(this.docHashTable.keySet().size() > this.docLimit){
                this.eraseLeastUsedDoc(this.docHeap.peek());
            }
        }

        if (this.memoryLimit != 0){
            while(this.getTotalMemory() > this.memoryLimit){
                this.eraseLeastUsedDoc(this.docHeap.peek());
            }
        }
    }

    private void eraseLeastUsedDoc(Document doc){
        this.removeDocumentWordsFromTrie(doc);  //delete doc from trie
        this.deleteDocumentFromStack(doc.getKey());  //delete doc from undoStack
        this.docHeap.remove();  //delete doc from the heap
        this.docHashTable.put(doc.getKey(), null);  //delete doc from hashtable
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
        this.docLimit = limit;
        this.complyWithLimits();
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
        this.memoryLimit = limit;
        this.complyWithLimits();
    }
}
