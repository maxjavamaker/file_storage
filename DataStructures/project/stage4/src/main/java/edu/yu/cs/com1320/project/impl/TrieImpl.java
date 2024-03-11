package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.Trie;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * FOR STAGE 3
 * @param <Value>
 */
public class TrieImpl<Value> implements Trie<Value> {
    private Node<Value> root;

    private class Node<Value>{
        private Set<Value> val;
        private Node<Value>[] links = new Node[62];

    }
    /**
     * add the given value at the given key
     * @param key
     * @param val
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
     * @param key
     * @param comparator used to sort values
     * @return a List of matching Values. Empty List if no matches.
     */
    public List<Value> getSorted(String key, Comparator<Value> comparator){

    }

    /**
     * get all exact matches for the given key.
     * Search is CASE SENSITIVE.
     * @param key
     * @return a Set of matching Values. Empty set if no matches.
     */
    public Set<Value> get(String key){
        return get(this.root, key, 0);
    }

    private Set<Value> get(Node<Value> x, String key, int d){
        if (x == null){
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
     * @param prefix
     * @param comparator used to sort values
     * @return a List of all matching Values containing the given prefix, in descending order. Empty List if no matches.
     */
    public List<Value> getAllWithPrefixSorted(String prefix, Comparator<Value> comparator){

    }

    /**
     * Delete the subtree rooted at the last character of the prefix.
     * Search is CASE SENSITIVE.
     * @param prefix
     * @return a Set of all Values that were deleted.
     */
    public Set<Value> deleteAllWithPrefix(String prefix){

    }

    /**
     * Delete all values from the node of the given key (do not remove the values from other nodes in the Trie)
     * @param key
     * @return a Set of all Values that were deleted.
     */
    public Set<Value> deleteAll(String key){

    }

    /**
     * Remove the given value from the node of the given key (do not remove the value from other nodes in the Trie)
     * @param key
     * @param val
     * @return the value which was deleted. If the key did not contain the given value, return null.
     */
    public Value delete(String key, Value val){

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
