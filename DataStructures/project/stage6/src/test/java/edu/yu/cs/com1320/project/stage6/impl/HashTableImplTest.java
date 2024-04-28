package edu.yu.cs.com1320.project.stage6.impl;

import edu.yu.cs.com1320.project.HashTable;
import edu.yu.cs.com1320.project.impl.HashTableImpl;
import edu.yu.cs.com1320.project.stage6.Document;
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
    URI uri3;
    URI uri4;
    Document document1;
    Document document2;
    Document document3;
    Document document4;
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
            uri3 = new URI("abba");
            uri4 = new URI("ima");
        }
        catch(URISyntaxException e){

        }
        document1 = new DocumentImpl(uri1, text1);
        document2 = new DocumentImpl(uri1, key1);
        document3 = new DocumentImpl(uri2, text1);
        document4 = new DocumentImpl(uri3, text1);
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
        stringTable.put("1", "1");
        stringTable.put("2", "2");
        stringTable.put("3", "3");
        stringTable.put("4", "4");
        stringTable.put("5", "5");
    }

    @Test
    public void keySet(){
        documentTable.put(document1, uri1);
        documentTable.put(document2, uri2);
    }

    @Test
    public void values(){
        documentTable.put(document1, uri1);
        documentTable.put(document2, uri2);
    }
}
