package edu.yu.cs.com1320.project.stage6.impl;

import edu.yu.cs.com1320.project.BTree;
import edu.yu.cs.com1320.project.impl.BTreeImpl;
import edu.yu.cs.com1320.project.stage6.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.*;

public class BTreeTest {
    private final BTree<URI, Document> bTree = new BTreeImpl<>();
    URI uri1, uri2, uri3, uri4, uri5, uri6, uri7, uri8, uri9, uri10;
    Document document1, document2, document3;

    @BeforeEach
    public void setup(){
        try {
            bTree.setPersistenceManager(new DocumentPersistenceManager(new File(System.getProperty("user.dir"))));
            uri1 = new URI("https://yu/edu/documents/Document1");
            uri2 = new URI("https://yu/edu/documents/Document2");
            uri3 = new URI("https://yu/edu/documents/Document3");
            uri4 = new URI("https://yu/edu/documents/Document4");
            uri5 = new URI("https://yu/edu/documents/Document5");
            uri6 = new URI("https://yu/edu/documents/Document6");
            uri7 = new URI("https://yu/edu/documents/Document7");
            uri8 = new URI("https://yu/edu/documents/Document8");
            uri9 = new URI("https://yu/edu/documents/Document9");
            uri10 = new URI("https://yu/edu/documents/Document10");

            document1 = new DocumentImpl(uri1, "text1", null);
            document2 = new DocumentImpl(uri2, "text2", null);
            document3 = new DocumentImpl(uri3, "text3".getBytes());


        }catch(URISyntaxException e){
            e.printStackTrace(System.out);
        }
    }

    @Test
    public void putNullKeyThrowsIllegalArgumentException(){
        assertThrows(IllegalArgumentException.class, () -> {
            bTree.put(null, document1);
        });
    }

    @Test
    public void putAndGet(){
        bTree.put(uri1, document1);
        bTree.put(uri2, document2);

        assertEquals(bTree.get(uri1), document1);
        assertEquals(bTree.get(uri2), document2);
        assertNull(bTree.get(uri3));

        bTree.put(uri2, document1);
        assertEquals(bTree.get(uri2), document1);

        bTree.put(uri1, document1);
        bTree.put(uri1, null);
        assertNull(bTree.get(uri1));
    }

    @Test
    public void testThatBTreeSplitsProperly(){
        bTree.put(uri1, document1);
        bTree.put(uri2, document2);
        bTree.put(uri3, document1);
        bTree.put(uri4, document2);
        bTree.put(uri5, document1);
        bTree.put(uri6, document2);
        bTree.put(uri7, document1);
        bTree.put(uri8, document2);
        bTree.put(uri9, document1);
        bTree.put(uri10, document2);

        assertEquals(document1, bTree.get(uri1));
        assertEquals(document2, bTree.get(uri2));
        assertEquals(document1, bTree.get(uri3));
        assertEquals(document2, bTree.get(uri4));
        assertEquals(document1, bTree.get(uri5));
        assertEquals(document2, bTree.get(uri6));
        assertEquals(document1, bTree.get(uri7));
        assertEquals(document2, bTree.get(uri8));
        assertEquals(document1, bTree.get(uri9));
        assertEquals(document2, bTree.get(uri10));


    }

    @Test
    public void moveToDiskAndMoveFromDiskAndDeleteFromDisk(){
        try {
            bTree.put(uri1, document1);
            bTree.put(uri2, document2);

            assertEquals(bTree.get(uri1), document1);
            assertEquals(bTree.get(uri2), document2);

            bTree.moveToDisk(uri1);
            bTree.moveToDisk(uri2);

            bTree.put(uri1, document3);
            assertEquals(bTree.get(uri1), document3);

            bTree.put(uri2, null);

            bTree.moveToDisk(uri2);  //make sure the document was not serialized because it was deleted


        }catch(IOException e){
            e.printStackTrace(System.out);
        }
    }
}
