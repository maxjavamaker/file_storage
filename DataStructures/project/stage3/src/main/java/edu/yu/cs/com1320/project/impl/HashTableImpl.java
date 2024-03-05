package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.HashTable;

import java.util.*;

/**
 * Instances of HashTable should be constructed with two type parameters, one for the type of the keys in the table and one for the type of the values
 *
 * @param <Key>
 * @param <Value>
 */
public class HashTableImpl<Key, Value> implements HashTable<Key, Value> {
    private float entries;
    private int length;
    private Node<Key, Value>[] nodes;

    public HashTableImpl(){

        entries = 0;
        length = 2;
        nodes = new Node[length];
    }

    private class Node<Key, Value>{
        private Key key;
        private Value value;
        private Node<Key, Value> next;

        public Node(Key key, Value value, Node<Key, Value> next) {
            this.key = key;
            this.value = value;
            this.next = next;
        }
    }

    /**
     * @param k the key whose value should be returned
     * @return the value that is stored in the HashTable for k, or null if there is no such key in the table
     */
    public Value get(Key k) {
        int hash = getHash(k);
        Node<Key, Value>currentNode = nodes[hash];

        while(currentNode != null){
            if (currentNode.key.equals(k)){
                return currentNode.value;
            }
            currentNode = currentNode.next;
        }

        return null;
    }

    /**
     * @param key the key at which to store the value
     * @param value the value to store
     *          To delete an entry, put a null value.
     * @return if the key was already present in the HashTable, return the previous value stored for the key. If the key was not already present, return null.
     */
    public Value put(Key key, Value value) {
        if (value == null) {
            return delete(key);
        }

        int hash = getHash(key);
        Node<Key, Value> newNode = new Node<>(key, value, null);

        if (nodes[hash] == null){
            nodes[hash] = newNode;
            entries++;
            checkToResize();
            return null;
        }

        Node<Key, Value> currentNode = nodes[hash];
        Node<Key, Value> previousNode = null;

        while(currentNode != null){
            if(currentNode.key.equals(key)){
                Value oldValue = currentNode.value;
                currentNode.value = value;
                return oldValue;
            }
            previousNode = currentNode;
            currentNode = currentNode.next;
        }
        previousNode.next = newNode;

        entries++;
        checkToResize();
        return null;
    }

    private Value delete(Key key) {
        int hash = getHash(key);
        Node<Key, Value> previousNode = null;
        Node<Key, Value> currentNode = nodes[hash];

        if(nodes[hash] == null){
            return null;
        }

        if (nodes[hash].key.equals(key)){
            Value oldValue = nodes[hash].value;
            nodes[hash] = nodes[hash].next;
            entries--;
            return oldValue;
        }

        while(currentNode.next != null){
            if (currentNode.key.equals(key)){
                Value oldValue = currentNode.value;
                previousNode.next = previousNode.next.next;
                entries--;
                return oldValue;
            }
            previousNode = currentNode;
            currentNode = currentNode.next;
        }

        return null;
    }

    /**
     * @param key the key whose presence in the hashtable we are inquiring about
     * @return true if the given key is present in the hashtable as a key, false if not
     * @throws NullPointerException if the specified key is null
     */
    public boolean containsKey(Key key) {
        if (key == null) {
            throw new NullPointerException();
        }
        return keySet().contains(key);
    }

    /**
     * @return an unmodifiable set of all the keys in this HashTable
     * @see java.util.Collections#unmodifiableSet(Set)
     */
    public Set<Key> keySet() {
        Set<Key> keySet = new HashSet<>();
        for (int i = 0; i < nodes.length; i++){
            Node<Key, Value> currentNode = nodes[i];
            while(currentNode != null){
                keySet.add(currentNode.key);
                currentNode = currentNode.next;
            }
        }
        return Collections.unmodifiableSet(keySet);
    }

    /**
     * @return an unmodifiable collection of all the values in this HashTable
     * @see java.util.Collections#unmodifiableCollection(Collection)
     */
    public Collection<Value> values() {
        ArrayList<Value> valueList = new ArrayList<>();
        for (Key key : keySet()){
            valueList.add(get(key));
        }
        return Collections.unmodifiableCollection(valueList);
    }

    /**
     * @return how entries there currently are in the HashTable
     */
    public int size() {
        return (int) entries;
    }

    private int getHash(Key key) {
        return Math.abs(key.hashCode()) % length;
    }

    private int getNewHash(Key key) {
        return Math.abs(key.hashCode()) % (length * 2);
    }

    private void checkToResize(){
        if (entries/length >= .75){
            resize();
        }
    }

    private void resize() {
        Node<Key, Value>[] resizedNode = new Node[length * 2];

        for (Key key : keySet()) {
            Node<Key, Value> newNode = new Node<>(key, get(key), null);

            if (resizedNode[getNewHash(key)] == null) {
                resizedNode[getNewHash(key)] = newNode;
            }

            else {
                Node<Key, Value> currentNode = resizedNode[getNewHash(key)];
                while (currentNode.next != null) {
                    currentNode = currentNode.next;
                }

                currentNode.next = newNode;
            }
        }

        length = length * 2;
        nodes = resizedNode;
    }
}