package edu.yu.cs.com1320.project.stage6.impl;

import edu.yu.cs.com1320.project.HashTable;
import edu.yu.cs.com1320.project.impl.HashTableImpl;
import edu.yu.cs.com1320.project.impl.MinHeapImpl;
import edu.yu.cs.com1320.project.stage6.Document;
import edu.yu.cs.com1320.project.MinHeap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.NoSuchElementException;


import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.*;

public class MinHeapImplTest {
    MinHeap<Integer> docHeap;
    @BeforeEach
    public void setup(){
        docHeap = new MinHeapImpl<>();
    }
    /*
    @Test
    public void getArrayIndex(){
        docHeap.insert(9);
        docHeap.insert(6);
        docHeap.insert(5);
        docHeap.insert(4);
        docHeap.insert(2);

        assertEquals(1, docHeap.getArrayIndex(2));
    }

    @Test
    public void isEmpty(){
        assertTrue(docHeap.isEmpty());
        docHeap.insert(9);
        assertFalse(docHeap.isEmpty());
    }

    @Test
    public void removeException(){
        assertThrows(NoSuchElementException.class, () -> {
            docHeap.remove();
        });
    }

    @Test
    public void removeMaintainsOrder(){
        docHeap.insert(9);
        docHeap.insert(6);
        docHeap.insert(5);
        docHeap.insert(4);
        docHeap.insert(2);

        docHeap.remove();
        assertEquals(1, docHeap.getArrayIndex(4));
        docHeap.remove();
        assertEquals(5, docHeap.peek());
    }


    @Test
    public void resizeArray(){
        docHeap.insert(9);
        docHeap.insert(6);
        docHeap.insert(5);
        docHeap.insert(4);
        docHeap.insert(2);
        assertEquals(1, docHeap.getArrayIndex(2));
    }*/
}
