package edu.yu.cs.com1320.project.stage6.impl;

import edu.yu.cs.com1320.project.impl.BTreeImpl;
import edu.yu.cs.com1320.project.Stack;
import edu.yu.cs.com1320.project.impl.StackImpl;
import edu.yu.cs.com1320.project.impl.TrieImpl;
import edu.yu.cs.com1320.project.stage6.Document;
import edu.yu.cs.com1320.project.stage6.DocumentStore;
import edu.yu.cs.com1320.project.stage6.PersistenceManager;
import edu.yu.cs.com1320.project.undo.CommandSet;
import edu.yu.cs.com1320.project.undo.GenericCommand;
import edu.yu.cs.com1320.project.undo.Undoable;
import edu.yu.cs.com1320.project.impl.MinHeapImpl;
import edu.yu.cs.com1320.project.MinHeap;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;

public class DocumentStoreImpl implements DocumentStore {
    private final BTreeImpl<URI, Document> bTree = new BTreeImpl<>();
    private final TrieImpl<URI> docTrie = new TrieImpl<>();
    private final Stack<Undoable> undoStack = new StackImpl<>();
    private final MinHeap<BTreeAccess> docHeap = new MinHeapImpl<>();
    private final PersistenceManager<URI, Document> persistenceManager;
    private final Set<URI> uriList = new HashSet<>();
    private final Set<URI> uriDisk = new HashSet<>();
    private final Map<URI, Map<String, String>> metadataMap = new HashMap<>();


    private int docLimit;
    private int memoryLimit;
    private final File directory;

    public DocumentStoreImpl(){
        directory = new File(System.getProperty("user.dir"));
        persistenceManager = new DocumentPersistenceManager(directory);
        bTree.setPersistenceManager(persistenceManager);
    }

    public DocumentStoreImpl(File dir){
        directory = dir;
        persistenceManager = new DocumentPersistenceManager(directory);
        bTree.setPersistenceManager(persistenceManager);
    }

    /**
     * set the given key-value metadata pair for the document at the given uri
     * @param uri;
     * @param key;
     * @param value;
     * @return the old value, or null if there was no previous value
     * @throws IllegalArgumentException if the uri is null or blank, if there is no document stored at that uri, or if the key is null or blank
     */
    public String setMetadata(URI uri, String key, String value) throws IOException{
        if (uri == null || uri.toString().isEmpty() || key == null || key.isEmpty() || bTree.get(uri) == null) {
            throw new IllegalArgumentException();
        }
        Map<String, String> hashMap = new HashMap<>();
        hashMap.put(key, value);
        metadataMap.put(uri, hashMap);
        this.updateDoc(uri);
        this.undoSetMetaDataLogic(uriPutLogic(uri), key);
        return bTree.get(uri).setMetadataValue(key, value);
    }

    /**
     * get the value corresponding to the given metadata key for the document at the given uri
     *
     * @param uri;
     * @param key;
     * @return the value, or null if there was no value
     * @throws IllegalArgumentException if the uri is null or blank, if there is no document stored at that uri, or if the key is null or blank
     */
    public String getMetadata(URI uri, String key) throws IOException{
        if (uri == null || uri.toString().isEmpty() || key == null || key.isEmpty() || this.bTree.get(uri) == null) {
            throw new IllegalArgumentException("uri or key was null or empty or uri does not exist in BTree");
        }

        this.updateDoc(uri);
        return bTree.get(uri).getMetadataValue(key);
    }

    /**
     * @param input the document being put
     * @param uri unique identifier for the document
     * @param format indicates which type of document format is being passed
     * @return if there is no previous doc at the given URI, return 0. If there is a previous doc, return the hashCode of the previous doc. If InputStream is null, this is a call to delete, and thus return either the hashCode of the deleted doc or 0 if there is no doc to delete.
     * @throws IOException if there is an issue reading input
     * @throws IllegalArgumentException if url or format are null, OR IF THE MEMORY FOOTPRINT OF THE DOCUMENT IS > MAX DOCUMENT BYTES
     */
    public int put(InputStream input, URI uri, DocumentFormat format) throws IOException{
        if (uri == null || uri.toString().isEmpty() || format == null){
            throw new IllegalArgumentException("uri was null or empty or format was null");
        }

        if (input == null || uriDisk.contains(uri)){
            int hashCode = bTree.get(uri).hashCode();
            if (hashCode != 0){
                delete(uri);
                return hashCode;
            } else{
                return hashCode;
            }
        }

        if (format == DocumentFormat.BINARY){
            Document document = new DocumentImpl(uri, input.readAllBytes());
            return add(uri, document); //don't add to trie if it's a binary document
        }

        else{
            byte[] bytes = input.readAllBytes();
            String text = new String(bytes, StandardCharsets.UTF_8);
            Document document = new DocumentImpl(uri, text, null);
            return add(uri, document);
        }
    }

    /**
     * @param url the unique identifier of the document to get
     * @return the given document
     */
    public Document get(URI url) throws IOException{
        if (!uriDisk.contains(url) && !uriList.contains(url)){
            return null;
        } else{
            return uriPutLogic(url);
        }
    }

    /**
     * @param url the unique identifier of the document to delete
     * @return true if the document is deleted, false if no document exists with that URI
     */
    public boolean delete(URI url){
        if (uriList.contains(url)){
            uriList.remove(url);
            undoDeleteLogic(url);
            deleteDocFromHeap(bTree.get(url));  //delete doc from heap
            removeDocumentWordsFromTrie(bTree.get(url));  //remove every word in the document from the trie
            bTree.put(url, null);
            return true;
        }else if (uriDisk.contains(url)){
            undoDeleteFromDiskLogic(url);
            uriDisk.remove(url);
            removeDocumentWordsFromTrie(bTree.get(url));  //bTree.get call takes it out of disk
            bTree.put(url, null);
            return true;
        } else{
            return false;
        }
    }

    private void removeDocumentWordsFromTrie(Document document){
        for (String word : document.getWords()){  //removing every word in the document from the trie
            docTrie.delete(word, document.getKey());
        }
    }

    private int add(URI uri, Document document){
        checkIfDocExceedsLimit(document);

        if (uriList.contains(uri)){
            removeDocumentWordsFromTrie(bTree.get(uri));
            deleteDocFromHeap(bTree.get(uri));

            undoDeleteLogic(uri);  //logic to undo changing document is the same as the logic for undoing delete

            int hashCode = bTree.put(uri, document).hashCode();
            this.addDocumentWordsToTrie(document); //add every word in the document to the trie
            docHeapInsert(document);
            complyWithLimits();
            return hashCode;
        }

        uriList.add(uri);
        bTree.put(uri, document);
        docHeapInsert(document);
        undoSetDocumentLogic(uri);
        complyWithLimits();
        return 0;
    }

    private void checkIfDocExceedsLimit(Document doc){
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
            docTrie.put(word, document.getKey());
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
        if (doc.getDocumentTxt() == null){  //this means it's a txt document
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

    private void undoSetMetaDataLogic(Document document, String key) throws IOException{
        if (this.getMetadata(document.getKey(), key) != null) {  //check if the metadata previously existed
            String previousValue = document.getMetadataValue(key);

            Consumer<URI> consumer = revertMetadata -> {
                document.setMetadataValue(key, previousValue);
                updateDoc(document.getKey());
            };
            undoStack.push(new GenericCommand<>(document.getKey(), consumer));
        }

        else{
            Consumer<URI> consumer = revertMetadata -> {
                document.setMetadataValue(key, null);
                updateDoc(document.getKey());
            };
            undoStack.push(new GenericCommand<>(document.getKey(), consumer));
        }
    }

    private void undoSetDocumentLogic(URI uri){
        Consumer<URI> consumer = restoreDocument -> {  //don't update nanoTime because document will be deleted
                removeDocumentWordsFromTrie(bTree.get(uri));
                deleteDocFromHeap(bTree.get(uri));
                bTree.put(uri, null);
        };

        undoStack.push(new GenericCommand<>(uri, consumer));
    }

    private void undoDeleteLogic(URI uri){  //same logic for changing a document
        Document previousDocument = bTree.get(uri);
        Consumer<URI> consumer = restoreDocument -> {
            checkDocumentMemory(previousDocument);  //make sure the document memory isn't higher than the limit
            bTree.put(uri, previousDocument);
            addDocumentWordsToTrie(bTree.get(uri));
            bTree.get(uri).setLastUseTime(System.nanoTime());
            docHeap.insert(new BTreeAccess(uri));
            complyWithLimits();
        };
        undoStack.push(new GenericCommand<>(uri, consumer));
    }

    private void undoDeleteFromDiskLogic(URI uri){
        Document previousDocument = bTree.get(uri);
        Consumer<URI> consumer = restoreDocumentToDisk ->{
            uriDisk.add(uri);
            checkDocumentMemory(previousDocument);
            addDocumentWordsToTrie(previousDocument);
            previousDocument.setLastUseTime(System.nanoTime());
            docHeap.insert(new BTreeAccess(uri));
        };
        undoStack.push(new GenericCommand<>(uri, consumer));
    }

    /**
     * Retrieve all documents whose text contains the given keyword.
     * Documents are returned in sorted, descending order, sorted by the number of times the keyword appears in the document.
     * Search is CASE SENSITIVE.
     * @param keyword;
     * @return a List of the matches. If there are no matches, return an empty list.
     */
    public List<Document> search(String keyword) throws IOException{
        if (keyword == null) {
            throw new IllegalArgumentException();
        }
        return searchWithoutUpdating(keyword, true);
    }

    private List<Document> searchWithoutUpdating(String keyword, boolean updateDocument){

        List<URI> uriList = updateDocument ? this.updateURI(docTrie.getSorted(keyword, this.createComparator(keyword))) : docTrie.getSorted(keyword, this.createComparator(keyword));

        List<Document> docList = new ArrayList<>();  //convert the list of uri's to a list of documents
        for (URI uri : uriList){
            uriPutLogic(uri);
            docList.add(bTree.get(uri));
        }
        return docList;
    }

    private Comparator<URI> createComparator(String keyword) {
        Comparator<URI> wordCountComparator = Comparator.comparing(URI -> this.get(URI).wordCount(keyword));
        return wordCountComparator.reversed();
    }
    /**
     * Retrieve all documents that contain text which starts with the given prefix
     * Documents are returned in sorted, descending order, sorted by the number of times the prefix appears in the document.
     * Search is CASE SENSITIVE.
     * @param keywordPrefix;
     * @return a List of the matches. If there are no matches, return an empty list.
     */
    public List<Document> searchByPrefix(String keywordPrefix) throws IOException{
        List<URI> uriList = this.updateURI(docTrie.getAllWithPrefixSorted(keywordPrefix, createPrefixComparator(keywordPrefix))); //create comparator and pass it to tries' getAllWithPrefixSorted method, update all docs nanoTime

        List<Document> docList = new ArrayList<>();  //convert the list of uri's to a list of documents
        for (URI uri : uriList){
            docList.add(this.bTree.get(uri));
        }
        return docList;
    }

    private Comparator<URI> createPrefixComparator(String keyword){
        Comparator<URI> wordCountComparator = Comparator.comparingInt(URI -> {  //create a list that sort documents in order of the number of times a prefix appears
            int wordCount = 0;
            for (String word : this.bTree.get(URI).getWords()){
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
    public List<Document> searchByMetadata(Map<String,String> keysValues) throws IOException{
        return this.searchByMetadataWithoutUpdating(keysValues, true);
    }

    private List<Document> searchByMetadataWithoutUpdating(Map<String,String> keysValues, boolean updateDocuments){
        Set<URI> allURISet = new HashSet<>();
        allURISet.addAll(uriList);
        allURISet.addAll(uriDisk);

        List<Document>  docList = new ArrayList<>();

        for (URI uri : allURISet){
            Document doc = bTree.get(uri);  //could cause issues with bringing back into memory
            boolean addToList = true;
            for (String key : keysValues.keySet()){
                if (doc.getMetadata().get(key) == null || !doc.getMetadata().get(key).equals(keysValues.get(key))){
                    addToList = false;
                    break;
                }
            }

            if (addToList){  //if the metadata matches add the doc to the arrayList
                uriPutLogic(doc.getKey());
                docList.add(doc);
            }
        }

        if (updateDocuments){
            return updateDoc(docList);
        }
        else{
            return docList;
        }
    }

    /**
     * Retrieve all documents whose text contains the given keyword AND which has the given key-value pairs in its metadata
     * Documents are returned in sorted, descending order, sorted by the number of times the keyword appears in the document.
     * Search is CASE SENSITIVE.
     * @param keyword;
     * @param keysValues;
     * @return a List of the matches. If there are no matches, return an empty list.
     */
    public List<Document> searchByKeywordAndMetadata(String keyword, Map<String,String> keysValues) throws IOException{
        List<Document> metadataDocList = searchByMetadataWithoutUpdating(keysValues, false);  //List of documents with matching metadata
        List<Document> keywordDocList = searchWithoutUpdating(keyword, false);  //List of documents with the keyword sorted by number of occurrences
        List<Document> finalList = new ArrayList<>();

        for (Document doc : metadataDocList){  //if a doc has the keyword and metadata add to the finalList
            if (keywordDocList.contains(doc)){
                finalList.add(doc);
                uriPutLogic(doc.getKey());
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
    public List<Document> searchByPrefixAndMetadata(String keywordPrefix,Map<String,String> keysValues) throws IOException{
        List<Document> metadataDocList = searchByMetadataWithoutUpdating(keysValues, false);  //List of documents with matching metadata
        List<Document> keywordDocList = this.searchByPrefix(keywordPrefix);  //List of documents with the Prefix sorted by number of occurrences
        List<Document> finalList = new ArrayList<>();

        for (Document doc : metadataDocList){  //if a doc has the Prefix and metadata add to the finalList
            if (keywordDocList.contains(doc)){
                finalList.add(doc);
                uriPutLogic(doc.getKey());
            }
        }

        return this.updateDoc(finalList);
    }

    /**
     * Completely remove any trace of any document which has the given key-value pairs in its metadata
     * Search is CASE SENSITIVE.
     * @return a Set of URIs of the documents that were deleted.
     */
    public Set<URI> deleteAllWithMetadata(Map<String,String> keysValues) throws IOException{
        List<Document> docs = this.searchByMetadata(keysValues); //get documents with matching metadata
        return this.deleteAndUndoLogic(docs);  //remove all traces of the documents, create undo logic
    }

    /**
     * Completely remove any trace of any document which contains the given keyword AND which has the given key-value pairs in its metadata
     * Search is CASE SENSITIVE.
     * @param keyword;
     * @return a Set of URIs of the documents that were deleted.
     */
    public Set<URI> deleteAllWithKeywordAndMetadata(String keyword,Map<String,String> keysValues) throws IOException{
        List<Document>  docs = this.searchByKeywordAndMetadata(keyword, keysValues); //get docs with matching metadata and keyword
        return this.deleteAndUndoLogic(docs);  //remove all traces of the document, create undo logic
    }

    /**
     * Completely remove any trace of any document which contains a word that has the given prefix AND which has the given key-value pairs in its metadata
     * Search is CASE SENSITIVE.
     * @param keywordPrefix;
     * @return a Set of URIs of the documents that were deleted.
     */
    public Set<URI> deleteAllWithPrefixAndMetadata(String keywordPrefix,Map<String,String> keysValues) throws IOException{
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
            for (String word : document.getWords()) {  //cycle through every word in the document
                this.docTrie.delete(word, document.getKey());  //delete the document at the word
            }

            Consumer<URI> consumer = documentPut -> {  //undo logic
                this.checkDocumentMemory(document);  //if document is higher than memory limit throw exception
                for (String word : document.getWords()) {
                    this.docTrie.put(word, document.getKey());
                }
                this.bTree.put(document.getKey(), document);
                document.setLastUseTime(System.nanoTime());
                this.docHeap.insert(new BTreeAccess(document.getKey()));
                this.complyWithLimits();
            };

            uriSet.add(document.getKey());  //add uri to the set

            this.bTree.put(document.getKey(), null);  //delete document from hashtable
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

    private void checkToDeleteDocumentFromDisk(URI uri){
        uriDisk.remove(uri);
        persistenceManager.delete(uri);
        removeDocumentWordsFromTrie();
    }

    private void deleteDocFromHeap(Document doc){
        doc.setLastUseTime(0);
        docHeap.reHeapify(new BTreeAccess(doc.getKey()));
        docHeap.remove();
    }

    private List<URI> updateURI(List<URI> uriList){
        long time = System.nanoTime();
        for (URI uri : uriList){
            bTree.get(uri).setLastUseTime(time);  //update last used time
            docHeap.reHeapify(new BTreeAccess(uri));  //order the heap
        }

        return uriList;
    }

    private List<Document> updateDoc(List<Document> docList){
        long time = System.nanoTime();
        for (Document doc : docList){
            doc.setLastUseTime(time);  //update last used time
            docHeap.reHeapify(new BTreeAccess(doc.getKey()));  //order the heap
        }

        return docList;
    }

    private void updateDoc(URI uri){
        bTree.get(uri).setLastUseTime(System.nanoTime());  //update last used time
        docHeap.reHeapify(new BTreeAccess(uri));  //order the heap
    }

    private void docHeapInsert(Document document){
        docHeap.insert(new BTreeAccess(document.getKey()));

        //while the doc limit or memory limit is violated, remove documents from the heap, trie, hashtable, and undoStack
        while((docLimit != 0 && this.docLimit < this.bTree.size()) || (this.memoryLimit != 0 && this.memoryLimit < this.getTotalMemory())){
            this.moveDocumentToMemory();
        }
    }

    private int getTotalMemory(){
        int memory = 0;
        for (URI uri : uriList) {
            Document doc = bTree.get(uri);
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
            while(this.uriList.size() > this.docLimit){
                this.moveDocumentToMemory();
            }
        }

        if (this.memoryLimit != 0){
            while(this.getTotalMemory() > this.memoryLimit){
                this.moveDocumentToMemory();
            }
        }
    }

    private void moveDocumentToMemory(){
        bTree.moveToDisk(docHeap.peek().uri);  //delete doc from hashtable
        uriDisk.add(docHeap.peek().uri);
        docHeap.remove();  //delete doc from the heap
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

    private Document uriPutLogic(URI uri){
        if (uriDisk.contains(uri)) {

            uriDisk.remove(uri);
            uriList.add(uri);
            docHeap.insert(new BTreeAccess(uri));

        }
        return bTree.get(uri);
    }

    private class BTreeAccess implements Comparable<BTreeAccess> {
        private URI uri;

        public BTreeAccess(URI uri){
            this.uri = uri;
        }

        public Document getDocument(){
            return bTree.get(this.uri);
        }

        @Override
        public int compareTo(BTreeAccess other) {
            if (this.getDocument().getLastUseTime() == other.getDocument().getLastUseTime()){
                return 0;
            }
            else if (this.getDocument().getLastUseTime() > other.getDocument().getLastUseTime()){  //the less the last use time is, the longer the document has been untouched for
                return 1;
            }
            else{
                return -1;
            }
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) { // Check object reference
                return true;
            }
            if (obj == null) { // Check for null
                return false;
            }
            if (!(obj instanceof BTreeAccess)) { // Check object type
                return false;
            }
            BTreeAccess other = (BTreeAccess) obj; // Typecast
            return this.uri.equals(other.uri);
        }
    }
}
