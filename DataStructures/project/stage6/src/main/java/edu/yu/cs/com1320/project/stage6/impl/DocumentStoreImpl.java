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

    public DocumentStoreImpl() {
        directory = new File(System.getProperty("user.dir"));
        persistenceManager = new DocumentPersistenceManager(directory);
        bTree.setPersistenceManager(persistenceManager);
    }

    public DocumentStoreImpl(File dir) {
        directory = dir;
        persistenceManager = new DocumentPersistenceManager(directory);
        bTree.setPersistenceManager(persistenceManager);
    }

    /**
     * set the given key-value metadata pair for the document at the given uri
     *
     * @param uri;
     * @param key;
     * @param value;
     * @return the old value, or null if there was no previous value
     * @throws IllegalArgumentException if the uri is null or blank, if there is no document stored at that uri, or if the key is null or blank
     */
    public String setMetadata(URI uri, String key, String value) throws IOException {
        if (uri == null || uri.toString().isEmpty() || key == null || key.isEmpty() || !uriList.contains(uri) && !uriDisk.contains(uri)) {
            throw new IllegalArgumentException();
        }

        if (metadataMap.get(uri) == null) {
            Map<String, String> hashMap = new HashMap<>();
            hashMap.put(key, value);
            metadataMap.put(uri, hashMap);
        } else {
            metadataMap.get(uri).put(key, value);
        }

        undoSetMetaDataLogic(bTree.get(uri), key);
        uriPutLogic(uri);
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
    public String getMetadata(URI uri, String key) throws IOException {
        if (uri == null || uri.toString().isEmpty() || key == null || key.isEmpty() || !uriList.contains(uri) && !uriDisk.contains(uri)) {
            throw new IllegalArgumentException("uri or key was null or empty or uri does not exist in BTree");
        }

        uriPutLogic(uri);
        return bTree.get(uri).getMetadataValue(key);
    }

    /**
     * @param input  the document being put
     * @param uri    unique identifier for the document
     * @param format indicates which type of document format is being passed
     * @return if there is no previous doc at the given URI, return 0. If there is a previous doc, return the hashCode of the previous doc. If InputStream is null, this is a call to delete, and thus return either the hashCode of the deleted doc or 0 if there is no doc to delete.
     * @throws IOException              if there is an issue reading input
     * @throws IllegalArgumentException if url or format are null, OR IF THE MEMORY FOOTPRINT OF THE DOCUMENT IS > MAX DOCUMENT BYTES
     */
    public int put(InputStream input, URI uri, DocumentFormat format) throws IOException {
        if (uri == null || uri.toString().isEmpty() || format == null) {
            throw new IllegalArgumentException("uri was null or empty or format was null");
        }

        if (input == null) {
            Document document = bTree.get(uri);
            if (document != null) {
                delete(uri);
                return document.hashCode();
            } else {
                return 0;
            }
        }

        if (format == DocumentFormat.BINARY) {
            Document document = new DocumentImpl(uri, input.readAllBytes());
            return add(uri, document); //don't add to trie if it's a binary document
        } else {
            byte[] bytes = input.readAllBytes();
            String text = new String(bytes, StandardCharsets.UTF_8);
            Document document = new DocumentImpl(uri, text, null);
            return add(uri, document);
        }
    }

    private int add(URI uri, Document document){
        checkIfDocExceedsLimit(document);

        if (uriList.contains(uri)){
            removeDocumentWordsFromTrie(bTree.get(uri));
            reHeapify(uri);  //you don't need to remove and put back into heap, just update the heap

            undoDeleteLogic(uri);  //logic to undo changing document is the same as the logic for undoing delete

            int hashCode = bTree.put(uri, document).hashCode();
            this.addDocumentWordsToTrie(document); //add every word in the document to the trie
            return hashCode;

        } else if (uriDisk.contains(uri)){
            uriList.add(uri);
            uriDisk.remove(uri);
            //bring it back from disk to remove its words from the trie and then delete it
            removeDocumentWordsFromTrie(bTree.get(uri));
            int hashCode = bTree.get(uri).hashCode();
            bTree.put(uri, document);
            addDocumentWordsToTrie(document);
            reHeapify(uri);
            return hashCode;
        } else {
            bTree.put(uri, document);
            addDocumentWordsToTrie(document);
            docHeap.insert(new BTreeAccess(document.getKey()));
            uriList.add(uri);
            complyWithLimits();
            undoSetDocumentLogic(uri);
            return 0;
        }
    }

    /**
     * @param url the unique identifier of the document to get
     * @return the given document
     */
    public Document get(URI url) throws IOException {
        if (!uriList.contains(url) && !uriDisk.contains(url)) {
            return null;
        }

        return uriPutLogic(url);
    }

    /**
     * @param url the unique identifier of the document to delete
     * @return true if the document is deleted, false if no document exists with that URI
     */
    public boolean delete(URI url) {
        if (uriList.contains(url) || uriDisk.contains(url)) {
            deleteListLogic(new ArrayList<>(Collections.singletonList(url)));
            return true;
        }

        return false;
    }

    private void removeDocumentWordsFromTrie(Document document) {
        for (String word : document.getWords()) {  //removing every word in the document from the trie
            docTrie.delete(word, document.getKey());
        }
    }

    private void checkIfDocExceedsLimit(Document doc) {
        boolean typeText = doc.getDocumentTxt() != null;
        if (typeText) {
            if (this.memoryLimit != 0 && doc.getDocumentTxt().getBytes().length > this.memoryLimit) {
                throw new IllegalArgumentException();
            }
        } else {
            if (this.memoryLimit != 0 && doc.getDocumentBinaryData().length > this.memoryLimit) {
                throw new IllegalArgumentException();
            }
        }
    }

    private void addDocumentWordsToTrie(Document document) {
        for (String word : document.getWords()) {  //adding every word in the document to the trie
            docTrie.put(word, document.getKey());
        }
    }

    /**
     * undo the last put or delete command
     *
     * @throws IllegalStateException if there are no actions to be undone, i.e. the command stack is empty
     */
    public void undo() throws IllegalStateException {
        if (undoStack.size() == 0) {
            throw new IllegalStateException();
        }

        if (undoStack.peek() instanceof CommandSet<?>) {
            CommandSet<?> temp = (CommandSet<?>) undoStack.pop();
            temp.undoAll();
        } else {
            undoStack.pop().undo();
        }
    }

    /**
     * undo the last put or delete that was done with the given URI as its key
     *
     * @param url;
     * @throws IllegalStateException if there are no actions on the command stack for the given URI
     */
    public void undo(URI url) throws IllegalStateException {
        boolean undidAction = false;

        Stack<Undoable> helper = new StackImpl<>();

        while (undoStack.size() != 0 && !undidAction) {
            if (undoStack.peek() instanceof CommandSet<?>) {
                if (this.undoURLCommandSet(undoStack.peek(), url)) {
                    undidAction = true;
                } else {
                    helper.push(undoStack.pop());
                }
            } else {
                if (this.undoURLGenericCommand(undoStack.peek(), url)) {
                    undidAction = true;
                } else {
                    helper.push(undoStack.pop());
                }
            }
        }
        while (helper.size() != 0) {
            undoStack.push(helper.pop());
        }

        if (!undidAction) {
            throw new IllegalStateException("uri not found on the stack");
        }
    }

    private boolean undoURLCommandSet(Undoable undo, URI url) {
        boolean undidAction = false;
        CommandSet<?> genericSet;
        CommandSet<URI> uriSet;

        genericSet = (CommandSet<?>) undo;
        uriSet = (CommandSet<URI>) genericSet;

        if (uriSet.undo(url)) {  //if it contains the target undo just the genericCommand on the target
            undidAction = true;
            if (uriSet.size() == 0) {  //if all commands were undone remove command set from the stack
                undoStack.pop();
            }
        }

        return undidAction;
    }

    private boolean undoURLGenericCommand(Undoable undo, URI url) {
        boolean undidAction = false;
        GenericCommand<?> genericCommand;
        GenericCommand<URI> genericURI;

        genericCommand = (GenericCommand<?>) undo;
        genericURI = (GenericCommand<URI>) genericCommand;

        if (genericURI.getTarget().equals(url)) {
            genericURI.undo();
            undidAction = true;
            undoStack.pop();
        }

        return undidAction;
    }

    private void undoSetMetaDataLogic(Document document, String key) throws IOException {
        String previousMetadata = getMetadata(document.getKey(), key);

        Consumer<URI> consumer = revertMetadata -> {
            checkIfDocExceedsLimit(document);
            uriPutLogic(document.getKey());

            if (previousMetadata != null) {
                document.setMetadataValue(key, previousMetadata);
            } else {
                document.setMetadataValue(key, null);
            }
            reHeapify(document.getKey());
        };

        undoStack.push(new GenericCommand<>(document.getKey(), consumer));
    }

    private void undoSetDocumentLogic(URI uri){
        Consumer<URI> consumer = restoreDocument -> {  //don't update nanoTime because document will be deleted
            //if you are undoing a put on a document in memory
            if (uriDisk.contains(uri)){
                uriDisk.remove(uri);
            }
            //if you are undoing a put on a document in disk
            else{
                removeDocumentFromHeap(bTree.get(uri));
            }
            //if you are undoing a put on a document in disk or on memory
            removeDocumentWordsFromTrie(bTree.get(uri));
            uriList.remove(uri);
            bTree.put(uri, null);
        };

        undoStack.push(new GenericCommand<>(uri, consumer));
    }

    private void undoDeleteLogic(URI uri){  //same logic for changing a document
        Document previousDocument = bTree.get(uri);
        Consumer<URI> consumer = restoreDocument -> {
            checkIfDocExceedsLimit(previousDocument);  //make sure the document memory isn't higher than the limit

            //this logic is applicable if the call is to undo a put document
            if (bTree.get(uri) != null){
                removeDocumentWordsFromTrie(bTree.get(uri));
            }

            bTree.put(uri, previousDocument);
            addDocumentWordsToTrie(previousDocument);
            reHeapify(uri);
            uriList.add(uri);
            complyWithLimits();
        };
        undoStack.push(new GenericCommand<>(uri, consumer));
    }

    /**
     * Retrieve all documents whose text contains the given keyword.
     * Documents are returned in sorted, descending order, sorted by the number of times the keyword appears in the document.
     * Search is CASE SENSITIVE.
     *
     * @param keyword;
     * @return a List of the matches. If there are no matches, return an empty list.
     */
    public List<Document> search(String keyword) throws IOException {
        if (keyword == null) {
            throw new IllegalArgumentException();
        }

        List<Document> docList = new ArrayList<>();
        List<URI> uriList = docTrie.getSorted(keyword, createComparator(keyword));

        for (URI uri : uriList) {
            uriPutLogic(uri);
            docList.add(bTree.get(uri));
        }

        reHeapify(docList);
        return docList;
    }

    private Comparator<URI> createComparator(String keyword) {
        Comparator<URI> wordCountComparator = Comparator.comparing(uri -> {
            return bTree.get(uri).wordCount(keyword);
        });

        return wordCountComparator.reversed();
    }

    /**
     * Retrieve all documents that contain text which starts with the given prefix
     * Documents are returned in sorted, descending order, sorted by the number of times the prefix appears in the document.
     * Search is CASE SENSITIVE.
     *
     * @param keywordPrefix;
     * @return a List of the matches. If there are no matches, return an empty list.
     */
    public List<Document> searchByPrefix(String keywordPrefix) throws IOException {
        if (keywordPrefix == null) {
            throw new IllegalArgumentException();
        }

        List<Document> docList = new ArrayList<>();
        List<URI> uriList = docTrie.getAllWithPrefixSorted(keywordPrefix, createPrefixComparator(keywordPrefix));

        for (URI uri : uriList) {
            uriPutLogic(uri);
            docList.add(bTree.get(uri));
        }

        reHeapify(docList);
        return docList;
    }

    private Comparator<URI> createPrefixComparator(String keyword) {
        Comparator<URI> wordCountComparator = Comparator.comparing(uri -> {  //create a list that sort uris in order of times a prefix appears
            int wordCount = 0;
            for (String word : bTree.get(uri).getWords()) {
                if (word.startsWith(keyword)) {
                    wordCount += bTree.get(uri).wordCount(word);
                }
            }

            return wordCount;
        });

        return wordCountComparator.reversed(); //return the comparator so its sorts in descending order
    }

    /**
     * Completely remove any trace of any document which contains the given keyword
     * Search is CASE SENSITIVE.
     *
     * @param keyword;
     * @return a Set of URIs of the documents that were deleted.
     */
    public Set<URI> deleteAll(String keyword) {
        List<URI> uriList = docTrie.getSorted(keyword, createComparator(keyword));
        Set<URI> returnValue = new HashSet<>(uriList);
        try {
            deleteListLogic(uriList);
        } catch (Exception e) {
            throw new RuntimeException();
        }

        return returnValue;
    }

    /**
     * Completely remove any trace of any document which contains a word that has the given prefix
     * Search is CASE SENSITIVE.
     *
     * @param keywordPrefix;
     * @return a Set of URIs of the documents that were deleted.
     */
    public Set<URI> deleteAllWithPrefix(String keywordPrefix) {
        List<URI> uriListTemp = docTrie.getAllWithPrefixSorted(keywordPrefix, createPrefixComparator(keywordPrefix));
        Set<URI> returnValue = new HashSet<>(uriListTemp);
        try {
            deleteListLogic(uriListTemp);  //remove all traces of the document, create undo logic
        } catch (Exception e) {
            throw new RuntimeException();
        }

        return returnValue;
    }

    /**
     * @param keysValues metadata key-value pairs to search for
     * @return a List of all documents whose metadata contains all the given values for the given keys. If no documents contain all the given key-value pairs, return an empty list.
     */
    public List<Document> searchByMetadata(Map<String, String> keysValues) throws IOException {
        List<Document> docList = new ArrayList<>();

        for (URI uri : metadataMap.keySet()) {
            if ((uriList.contains(uri) || uriDisk.contains(uri)) && metadataMap.get(uri).equals(keysValues)) {
                uriPutLogic(uri);
                docList.add(bTree.get(uri));
            }
        }

        reHeapify(docList);
        return docList;
    }

    /**
     * Retrieve all documents whose text contains the given keyword AND which has the given key-value pairs in its metadata
     * Documents are returned in sorted, descending order, sorted by the number of times the keyword appears in the document.
     * Search is CASE SENSITIVE.
     *
     * @param keyword;
     * @param keysValues;
     * @return a List of the matches. If there are no matches, return an empty list.
     */

    public List<Document> searchByKeywordAndMetadata(String keyword, Map<String, String> keysValues) throws IOException {
        List<URI> keysValuesList = new ArrayList<>();

        for (URI uri : metadataMap.keySet()) {
            if ((uriList.contains(uri) || uriDisk.contains(uri)) && metadataMap.get(uri).equals(keysValues)) {
                keysValuesList.add(uri);
            }
        }

        List<URI> keywordList = docTrie.getSorted(keyword, new Comparator<URI>() {
            @Override
            public int compare(URI o1, URI o2) {
                return 0;
            }
        });

        List<Document> finalList = new ArrayList<>();

        for (URI uri : keysValuesList) {
            if (keywordList.contains(uri)) {
                finalList.add(bTree.get(uri));
                uriPutLogic(uri);
            }
        }
        Collections.sort(finalList, targetedKeywordComparator(keyword));
        return reHeapify(finalList);
    }

    private Comparator<Document> targetedKeywordComparator(String target){
        Comparator<Document> wordCountComparator = Comparator.comparing(document -> {
            int wordCount = 0;
            for (String word : document.getWords()) {
                if (word.equals(target)) {
                    wordCount += document.wordCount(word);
                }
            }
            return wordCount;
        });

        return wordCountComparator.reversed(); //return the comparator so its sorts in descending order
    }

    /**
     * Retrieve all documents that contain text which starts with the given prefix AND which has the given key-value pairs in its metadata
     * Documents are returned in sorted, descending order, sorted by the number of times the prefix appears in the document.
     * Search is CASE SENSITIVE.
     *
     * @param keywordPrefix;
     * @return a List of the matches. If there are no matches, return an empty list.
     */
    public List<Document> searchByPrefixAndMetadata(String keywordPrefix, Map<String, String> keysValues) throws IOException {
        List<URI> keysValuesList = new ArrayList<>();

        for (URI uri : metadataMap.keySet()) {
            if ((uriList.contains(uri) || uriDisk.contains(uri)) && metadataMap.get(uri).equals(keysValues)) {
                keysValuesList.add(uri);
            }
        }

        List<URI> keywordPrefixList = docTrie.getAllWithPrefixSorted(keywordPrefix, new Comparator<URI>() {
            @Override
            public int compare(URI o1, URI o2) {
                return 0;
            }
        });
        List<Document> finalList = new ArrayList<>();

        for (URI uri : keysValuesList) {
            if (keywordPrefixList.contains(uri)) {
                finalList.add(bTree.get(uri));
                uriPutLogic(uri);
            }
        }
        Collections.sort(finalList, targetedPrefixComparator(keywordPrefix));
        return reHeapify(finalList);
    }

    private Comparator<Document> targetedPrefixComparator(String target){
        Comparator<Document> wordCountComparator = Comparator.comparing(document -> {
            int wordCount = 0;
            for (String word : document.getWords()) {
                if (word.startsWith(target)) {
                    wordCount += document.wordCount(word);
                }
            }
            return wordCount;
        });

        return wordCountComparator.reversed(); //return the comparator so its sorts in descending order
    }

    /**
     * Completely remove any trace of any document which has the given key-value pairs in its metadata
     * Search is CASE SENSITIVE.
     *
     * @return a Set of URIs of the documents that were deleted.
     */
    public Set<URI> deleteAllWithMetadata(Map<String, String> keysValues) throws IOException {
        List<URI> matchingMetadata = new ArrayList<>();

        for (URI uri : metadataMap.keySet()) {
            if ((uriList.contains(uri) || uriDisk.contains(uri)) && metadataMap.get(uri).equals(keysValues)) {
                matchingMetadata.add(uri);
            }
        }

        deleteListLogic(matchingMetadata);

        return new HashSet<>(matchingMetadata);
    }

    /**
     * Completely remove any trace of any document which contains the given keyword AND which has the given key-value pairs in its metadata
     * Search is CASE SENSITIVE.
     *
     * @param keyword;
     * @return a Set of URIs of the documents that were deleted.
     */
    public Set<URI> deleteAllWithKeywordAndMetadata(String keyword, Map<String, String> keysValues) throws IOException {
        List<URI> keywordList = docTrie.getSorted(keyword, createComparator(keyword));
        List<URI> finalList = new ArrayList<>();

        for (URI uri : metadataMap.keySet()) {
            if ((uriList.contains(uri) || uriDisk.contains(uri)) && metadataMap.get(uri).equals(keysValues)) {
                if (keywordList.contains(uri)) {
                    finalList.add(uri);
                }
            }
        }

        deleteListLogic(finalList);
        return new HashSet<>(finalList);
    }

    /**
     * Completely remove any trace of any document which contains a word that has the given prefix AND which has the given key-value pairs in its metadata
     * Search is CASE SENSITIVE.
     *
     * @param keywordPrefix;
     * @return a Set of URIs of the documents that were deleted.
     */
    public Set<URI> deleteAllWithPrefixAndMetadata(String keywordPrefix, Map<String, String> keysValues) throws IOException {
        List<URI> keywordPrefixList = docTrie.getAllWithPrefixSorted(keywordPrefix, createComparator(keywordPrefix));
        List<URI> finalList = new ArrayList<>();

        for (URI uri : metadataMap.keySet()) {
            if ((uriList.contains(uri) || uriDisk.contains(uri)) && metadataMap.get(uri).equals(keysValues)) {
                if (keywordPrefixList.contains(uri)) {
                    finalList.add(uri);
                }
            }
        }

        deleteListLogic(finalList);
        return new HashSet<>(finalList);
    }

    private void deleteListLogic(List<URI> doomedURIList) {
        deleteListUndoLogic(doomedURIList);

        //logic to delete documents in both memory and disk
        for (URI uri : doomedURIList) {
            if (uriList.contains(uri)) {
                removeDocumentFromHeap(bTree.get(uri));
            }

            uriList.remove(uri);
            uriDisk.remove(uri);
            removeDocumentWordsFromTrie(bTree.get(uri));
            bTree.put(uri, null);
        }
    }

    public void deleteListUndoLogic(List<URI> doomedURIList) {
        boolean createCommandSet = doomedURIList.size() > 1;

        CommandSet<URI> commandSet = new CommandSet<>();

        for (URI uri : doomedURIList) {
            Document doc = bTree.get(uri);
            Set<URI> tempDiskSet = new HashSet<>(uriDisk);

            Consumer<URI> consumer = restoreDocument -> {
                checkIfDocExceedsLimit(doc);
                doc.setLastUseTime(doc.getLastUseTime());
                if (tempDiskSet.contains(uri)) {
                    uriDisk.add(uri);
                    bTree.put(uri, doc);
                    try {
                        bTree.moveToDisk(uri);
                    } catch (IOException e) {
                        throw new RuntimeException();
                    }
                } else {
                    uriList.add(uri);
                    bTree.put(uri, doc);
                    docHeap.insert(new BTreeAccess(uri));
                }

                addDocumentWordsToTrie(doc);
                complyWithLimits();
            };

            GenericCommand<URI> genericCommand = new GenericCommand<>(uri, consumer);

            if (createCommandSet) {
                commandSet.addCommand(genericCommand);
            } else {
                undoStack.push(genericCommand);
            }
        }

        if (createCommandSet) {
            undoStack.push(commandSet);
        }
    }

    private void removeDocumentFromHeap(Document doc) {
        doc.setLastUseTime(0);
        docHeap.reHeapify(new BTreeAccess(doc.getKey()));
        docHeap.remove();
    }

    private List<Document> reHeapify(List<Document> docList) {
        long time = System.nanoTime();
        for (Document doc : docList) {
            doc.setLastUseTime(time);  //update last used time
            docHeap.reHeapify(new BTreeAccess(doc.getKey()));  //order the heap
        }

        return docList;
    }

    private void reHeapify(URI uri) {
        bTree.get(uri).setLastUseTime(System.nanoTime());  //update last used time
        docHeap.reHeapify(new BTreeAccess(uri));  //order the heap
    }

    private int getTotalMemory() {
        int memory = 0;
        for (URI uri : uriList) {
            Document doc = bTree.get(uri);
            if (doc.getDocumentTxt() != null) {
                memory += doc.getDocumentTxt().getBytes().length;
            } else {
                memory += doc.getDocumentBinaryData().length;
            }
        }

        return memory;
    }

    private void moveLeastRecentlyUsedDocumentToDisk() {
        try {
            bTree.moveToDisk(docHeap.peek().getUri());
            uriList.remove(docHeap.peek().getUri());
            uriDisk.add(docHeap.peek().uri);
            docHeap.remove();
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }

    /**
     * set maximum number of documents that may be stored
     *
     * @param limit;
     * @throws IllegalArgumentException if limit < 1
     */
    public void setMaxDocumentCount(int limit) {
        if (limit < 1) {
            throw new IllegalArgumentException("limit cannot be less than 1");
        }
        this.docLimit = limit;
        this.complyWithLimits();
    }

    /**
     * set maximum number of bytes of memory that may be used by all the documents in memory combined
     *
     * @param limit;
     * @throws IllegalArgumentException if limit < 1
     */
    public void setMaxDocumentBytes(int limit) {
        if (limit < 1) {
            throw new IllegalArgumentException("limit cannot be less than 1");
        }
        this.memoryLimit = limit;
        this.complyWithLimits();
    }

    private void complyWithLimits() {
        if (docLimit != 0) {
            while (uriList.size() > docLimit) {
                moveLeastRecentlyUsedDocumentToDisk();
            }
        }

        if (memoryLimit != 0) {
            while (getTotalMemory() > memoryLimit) {
                moveLeastRecentlyUsedDocumentToDisk();
            }
        }
    }

    private Document uriPutLogic(URI uri) {
        if (uriDisk.contains(uri)) {
            uriDisk.remove(uri);
            uriList.add(uri);
            checkIfDocExceedsLimit(bTree.get(uri));
            docHeap.insert(new BTreeAccess(uri));
        }

        reHeapify(uri);
        complyWithLimits();
        return bTree.get(uri);
    }

    private class BTreeAccess implements Comparable<BTreeAccess> {
        private URI uri;
        private long lastUseTime;

        public BTreeAccess(URI uri) {
            this.uri = uri;
            this.lastUseTime = bTree.get(uri).getLastUseTime();
        }

        public Document getDocument() {
            return bTree.get(this.uri);
        }

        public URI getUri() {
            return this.uri;
        }

        @Override
        public int compareTo(BTreeAccess other) {
            if (this.getDocument().getLastUseTime() == other.getDocument().getLastUseTime()) {
                return 0;
            } else if (this.getDocument().getLastUseTime() > other.getDocument().getLastUseTime()) {  //the less the last use time is, the longer the document has been untouched for
                return 1;
            } else {
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
            return this.uri.equals(other.getUri());
        }
    }
}