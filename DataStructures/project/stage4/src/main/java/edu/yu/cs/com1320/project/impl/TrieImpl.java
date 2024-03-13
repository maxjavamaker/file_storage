package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.Trie;
import edu.yu.cs.com1320.project.stage4.Document;

import java.util.*;

/**
 * FOR STAGE 3
 * @param <Value>
 */
public class TrieImpl<Value> implements Trie<Value> {
    private Node<Value> root = null;
    private final int linkSize = 62;


    private class Node<Value>{
        private Set<Value> val;
        private Node<Value>[] links = new Node[62];
    }

    /**
     * add the given value at the given key
     * @param key;
     * @param val;
     */
    public void put(String key, Value val){
        put(this.root, key, val, 0);
    }

    private Node<Value> put(Node<Value> x, String key, Value val, int d){
        if (x == null){
            x = new Node<>();
        }

        if (d == key.length()){
            x.val.add(val);
            return x;
        }


        int c = asciiValue(key.charAt(d));
        x.links[c] = this.put(x.links[c], key, val, d + 1);

        return x;
    }

    /**
     * Get all exact matches for the given key, sorted in descending order, where "descending" is defined by the comparator.
     * NOTE FOR COM1320 PROJECT: FOR PURPOSES OF A *KEYWORD* SEARCH, THE COMPARATOR SHOULD DEFINE ORDER AS HOW MANY TIMES THE KEYWORD APPEARS IN THE DOCUMENT.
     * Search is CASE SENSITIVE.
     * @param key;
     * @param comparator used to sort values
     * @return a List of matching Values. Empty List if no matches.
     */
    public List<Value> getSorted(String key, Comparator<Value> comparator){
        List<Value> valueList = new ArrayList<>(this.get(key));  //get a list of the values stored at the key
        Collections.sort(valueList, comparator);
        return valueList;
    }

    /**
     * get all exact matches for the given key.
     * Search is CASE SENSITIVE.
     * @param key;
     * @return a Set of matching Values. Empty set if no matches.
     */
    public Set<Value> get(String key) {
        return get(this.root, key, 0);
    }

    private Set<Value> get(Node<Value> x, String key, int d) {
        if (x == null) {
            return x.val;
        }

        if (d == key.length()){
            return x.val;
        }

        int c = asciiValue(key.charAt(d));
        return this.get(x.links[c], key, d + 1);
    }

    /**
     * get all matches which contain a String with the given prefix, sorted in descending order, where "descending" is defined by the comparator.
     * NOTE FOR COM1320 PROJECT: FOR PURPOSES OF A *KEYWORD* SEARCH, THE COMPARATOR SHOULD DEFINE ORDER AS HOW MANY TIMES THE KEYWORD APPEARS IN THE DOCUMENT.
     * For example, if the key is "Too", you would return any value that contains "Tool", "Too", "Tooth", "Toodle", etc.
     * Search is CASE SENSITIVE.
     * @param prefix;
     * @param comparator used to sort values
     * @return a List of all matching Values containing the given prefix, in descending order. Empty List if no matches.
     */

    public List<Value> getAllWithPrefixSorted(String prefix, Comparator<Value> comparator){
        List<Value> sortedWords = new ArrayList<>();
        Node<Value> subtree = this.getNode(this.root, prefix, 0); //get the node of the subtree to get prefixes from
        getPrefixes(subtree, sortedWords);
    }

    private void getPrefixes(Node<Value> x, List<Value> words){
        words.addAll(x.val); //add all the nodes values to the list

        for (Node<Value> child : x.links){ //cycle through the tree and add all values
            getPrefixes(child, words);
        }
    }

    /**
     * Delete the subtree rooted at the last character of the prefix.
     * Search is CASE SENSITIVE.
     * @param prefix;
     * @return a Set of all Values that were deleted.
     */

    public Set<Value> deleteAllWithPrefix(String prefix){
        Set<Value> deletedValues = new HashSet<>(); //set of all the deleted values
        Node<Value> subtree = this.getNode(this.root, prefix, 0); //get the node of the subtree to delete from
        deleteSubtree(subtree, deletedValues); //delete the subtree
        return deletedValues;
    }

    private Node<Value> deleteSubtree(Node<Value> x, Set<Value> deletedValues){
        deletedValues.addAll(x.val); //add all the nodes values to the set

        for (Node<Value> child : x.links){ //cycle through the tree and remove all nodes
            deleteSubtree(child, deletedValues);
        }

        return null;
    }

    private Node<Value> getNode(Node<Value> x, String key, int d){
        if (x == null) {
            return null;
        }

        if (d == key.length()){
            return x;
        }

        int c = asciiValue(key.charAt(d));
        return this.getNode(x.links[c], key, d + 1);
    }

    /**
     * Delete all values from the node of the given key (do not remove the values from other nodes in the Trie)
     * @param key;
     * @return a Set of all Values that were deleted.
     */

    public Set<Value> deleteAll(String key){
        Set<Value> temp = new HashSet<>(this.get(key));  //temp set to store deleted values
        this.get(key).clear();  //delete all the values
        trimIfEmpty(this.root, key, 0); //delete the node because it doesn't contain any values
        return temp;
    }

    /**
     * Remove the given value from the node of the given key (do not remove the value from other nodes in the Trie)
     * @param key;
     * @param val;
     * @return the value which was deleted. If the key did not contain the given value, return null.
     */

    public Value delete(String key, Value val){
        if (this.get(key).remove(val)) { //if the value exists return it
            trimIfEmpty(this.root, key, 0); //if deleting value results in an empty node, delete it
            return val;
        }
        else {
            return null;
        }
    }

    private Node<Value> trimIfEmpty(Node<Value> x, String key, int d){
        if (d == key.length()) {
            return checkToDelete(x); //if node has no value or has no non-null children delete it
        }

        int c = asciiValue(key.charAt(d));
        x.links[c] = this.trimIfEmpty(x.links[c], key, d + 1);

        return checkToDelete(x);
    }

    private Node<Value> checkToDelete(Node<Value> x){
        if (!x.val.isEmpty()){ //check if it has any values
            return x;
        }

        for (Node<Value> value: x.links){ //check if it has any children
            if (value != null){
                return x;
            }
        }

        return null;
    }

    private int asciiValue(char c){
        if (Character.isDigit(c)){
            return c - 48;
        }

        else if (Character.isUpperCase(c)){
            return c - 55;
        }

        else{
            return c - 61;
        }
    }
}
