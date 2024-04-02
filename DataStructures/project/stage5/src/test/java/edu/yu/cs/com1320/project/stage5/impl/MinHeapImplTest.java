package edu.yu.cs.com1320.project.stage5.impl;

import edu.yu.cs.com1320.project.HashTable;
import edu.yu.cs.com1320.project.impl.HashTableImpl;
import edu.yu.cs.com1320.project.impl.MinHeapImpl;
import edu.yu.cs.com1320.project.stage5.Document;
import edu.yu.cs.com1320.project.MinHeap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.*;

public class MinHeapImplTest {
    MinHeap<Integer> docHeap;
    @BeforeEach
    public void setup(){
             docHeap = new MinHeapImpl<>();
    }

    @Test
    public void getArrayIndex(){
        docHeap.insert(5);
        docHeap.insert(4);
        docHeap.insert(3);
        docHeap.insert(7);
        docHeap.insert(9);

        System.out.println(docHeap.getArrayIndex(7));
    }
}
