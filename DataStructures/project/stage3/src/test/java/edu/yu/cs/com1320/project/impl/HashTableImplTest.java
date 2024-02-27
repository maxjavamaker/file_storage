package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.impl.HashTableImpl;
import edu.yu.cs.com1320.project.stage3.Document;
import edu.yu.cs.com1320.project.HashTable;
import edu.yu.cs.com1320.project.stage3.impl.DocumentImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.*;

public class HashTableImplTest {
    String text1;
    String key1;
    String value1;
    URI uri1;
    URI uri2;
    Document document1;
    Document document2;
    HashTable<String, String> stringTable;
    HashTable<Document, URI> documentTable;
    HashTable<Integer, Document> integerTable;
    @BeforeEach
    public void setup(){
        text1 = "text1";
        key1 = "key";
        value1 = "value";
        stringTable = new HashTableImpl<>();
        documentTable = new HashTableImpl<>();
        integerTable = new HashTableImpl<>();
        try {
            uri1 = new URI("uri1");
            uri2 = new URI("12334");
        }
        catch(URISyntaxException e){

        }
        document1 = new DocumentImpl(uri1, text1);
        document2 = new DocumentImpl(uri1, key1);
    }

    @Test
    public void putNoPreviousValue(){
        assertNull(documentTable.put(document1, uri1));
        assertNull(stringTable.put(key1, value1));
    }

    @Test
    public void previousValue(){
        Integer number = 42;
        integerTable.put(number, document2);
        assertEquals(document2, integerTable.put(number, document1));
    }

    @Test
    public void nullValueDeleteKey(){
        documentTable.put(document1, uri1);
        assertEquals(uri1, documentTable.put(document1, null));

        assertNull(documentTable.put(document1, null));

    }

    @Test
    public void getNoKeyReturnNull(){
        documentTable.put(document1, uri1);
        assertNull(documentTable.get(document2));
    }

    @Test
    public void get(){
        documentTable.put(document1, uri1);
        assertEquals(uri1, documentTable.get(document1));
    }

    @Test
    public void containsKeyNullPointerException(){
        assertThrows(NullPointerException.class, () -> documentTable.containsKey(null));
    }

    @Test
    public void containsKey(){
        documentTable.put(document1, uri1);
        documentTable.put(document2, uri1);

        assertTrue(documentTable.containsKey(document1));
        assertTrue(documentTable.containsKey(document2));
    }

    @Test
    public void doesNotContainKey(){
        documentTable.put(document1, uri1);
        assertFalse(documentTable.containsKey(document2));
    }

    @Test
    public void size(){
        documentTable.put(document1, uri1);
        assertEquals(1, documentTable.size());
        documentTable.put(document2, uri2);
        assertEquals(2, documentTable.size());
        documentTable.put(document2, null);
        assertEquals(1, documentTable.size());
        documentTable.put(document1, null);
        assertEquals(0, documentTable.size());

    }

    @Test
    public void keySet(){
        documentTable.put(document1, uri1);
        documentTable.put(document2, uri2);
        System.out.println(documentTable.keySet());
    }

    @Test
    public void values(){
        documentTable.put(document1, uri1);
        documentTable.put(document2, uri2);
        System.out.println(documentTable.values());
    }

}
