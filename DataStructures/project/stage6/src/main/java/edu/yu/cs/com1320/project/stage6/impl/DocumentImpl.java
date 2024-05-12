package edu.yu.cs.com1320.project.stage6.impl;

import edu.yu.cs.com1320.project.stage6.Document;

import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public class DocumentImpl implements Document {
    private final boolean isBinary;
    private String text;
    private final URI uri;
    private byte[] binaryData;
    private long lastUseTime;
    private Map<String, String> metadata = new HashMap<>();
    private Map<String, Integer> words = new HashMap<>();

    public DocumentImpl(URI uri, byte[] binaryData) {
        if (uri == null || binaryData == null || uri.toString().isEmpty() || binaryData.length == 0) {
            throw new IllegalArgumentException();
        }

        this.uri = uri;
        this.binaryData = binaryData;
        this.isBinary = true;
        this.lastUseTime = System.nanoTime();
    }

    public DocumentImpl(URI uri, String text, Map<String, Integer> wordCountMap){
        this.uri = uri;
        this.text = text;
        this.isBinary = false;
        this.lastUseTime = System.nanoTime();

        if (wordCountMap == null){
            this.addWordsToMap(text);
        }
        else{
            this.words = wordCountMap;
        }
    }


    /**
     * @param key   key of document metadata to store a value for
     * @param value value to store
     * @return old value, or null if there was no old value
     * @throws IllegalArgumentException if the key is null or blank
     */
    public String setMetadataValue(String key, String value) {
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException();
        }

        return this.metadata.put(key, value);

    }

    /**
     * @param key metadata key whose value we want to retrieve
     * @return corresponding value, or null if there is no such key
     * @throws IllegalArgumentException if the key is null or blank
     */
    public String getMetadataValue(String key) {
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException();
        }

        return metadata.get(key);
    }

    /**
     * @return a COPY of the metadata saved in this document
     */
    public HashMap<String, String> getMetadata() {
        HashMap<String, String> metadataCopy = new HashMap<>(); //create a copy of the hashtable holding all the metadata
        for (String key : metadata.keySet()){  //add all the metadata to the copy
            metadataCopy.put(key, metadata.get(key));
        }

        return metadataCopy;
    }

    public void setMetadata(HashMap<String, String> metadata){
        this.metadata = metadata;
    }

    /**
     * @return content of text document
     */
    public String getDocumentTxt() {
        return this.text;
    }

    /**
     * @return content of binary data document
     */
    public byte[] getDocumentBinaryData() {
        return this.binaryData;
    }

    /**
     * @return URI which uniquely identifies this document
     */
    public URI getKey() {
        return this.uri;

    }

    /**
     * how many times does the given word appear in the document?
     * @param word;
     * @return the number of times the given words appears in the document. If it's a binary document, return 0.
     */
    public int wordCount(String word) {
        //if doc is binary or the word is not in the hashmap return 0, else return word count
        if (this.isBinary || this.words.get(word) == null){
            return 0;
        }
        else{
            return this.words.get(word);
        }

    }
    /**
     * @return all the words that appear in the document
     */
    public Set<String> getWords(){
        return this.words.keySet();
    }

    private void addWordsToMap(String txt){
        String[] txtArray = txt.split("");
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < txtArray.length; i++){
            if (!txtArray[i].matches("[^a-zA-Z0-9\\s]")){  //remove any characters that are not words or numbers
                builder.append(txtArray[i]);
            }
        }
        String textString = builder.toString();
        String[] textArray = textString.split(" "); //split at whitespace
        for (String word : textArray){
            this.words.compute(word, (key, oldValue) -> (oldValue == null) ? 1 : oldValue + 1);  //if the value is null set to one, otherwise increase by 1
        }
    }

    /**
     * return the last time this document was used, via put/get or via a search result
     * (for stage 4 of project)
     */

    public long getLastUseTime(){
        return this.lastUseTime;
    }

    public void setLastUseTime(long timeInNanoseconds){
        this.lastUseTime = timeInNanoseconds;
    }

    public HashMap<String, Integer> getWordMap(){
        return (HashMap) this.words;
    }

    /**
     * This must set the word to count map during deserialization
     *
     * @param wordMap;
     */
    public void setWordMap(HashMap<String, Integer> wordMap){
        this.words = wordMap;
    }

    @Override
    public int compareTo(Document other) {
        if (this.getLastUseTime() == other.getLastUseTime()){
            return 0;
        }
        else if (this.getLastUseTime() > other.getLastUseTime()){  //the less the last use time is, the longer the document has been untouched for
            return 1;
        }
        else{
            return -1;
        }
    }

    @Override
    public int hashCode() {
        int result = uri.hashCode();
        result = 31 * result + (text != null ? text.hashCode() : 0);
        result = 31 * result + Arrays.hashCode(binaryData);
        return Math.abs(result);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Document s)) {
            return false;
        }
        return this.hashCode() == s.hashCode();
    }

}
