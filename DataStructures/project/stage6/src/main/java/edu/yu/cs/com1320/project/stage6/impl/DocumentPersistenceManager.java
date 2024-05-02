package edu.yu.cs.com1320.project.stage6.impl;

import edu.yu.cs.com1320.project.stage6.PersistenceManager;

import java.io.IOException;

public class DocumentPersistenceManager<Key,Value> implements PersistenceManager {
    public void serialize(Key key, Value val) throws IOException{

    }
    public Value deserialize(Key key) throws IOException{

    }
    /**
     * delete the file stored on disk that corresponds to the given key
     * @param key
     * @return true or false to indicate if deletion occured or not
     * @throws IOException
     */
    public boolean delete(Key key) throws IOException{

    }
}