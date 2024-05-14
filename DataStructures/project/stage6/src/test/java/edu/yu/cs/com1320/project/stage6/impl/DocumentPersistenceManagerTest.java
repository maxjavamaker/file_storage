package edu.yu.cs.com1320.project.stage6.impl;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import edu.yu.cs.com1320.project.stage6.Document;
import edu.yu.cs.com1320.project.stage6.PersistenceManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


public class DocumentPersistenceManagerTest {

    private PersistenceManager<URI, Document> persistenceManager;
    private URI uri1, uri2, uri3;
    private Document document1, document2, document3;

    @BeforeEach
    public void setup() {
        persistenceManager = new DocumentPersistenceManager(new File(System.getProperty("user.dir")));

        String text1 = "text1";
        String text2 = "text2";
        String text3 = "text3";
        String key1 = "key1";
        String key2 = "key2";
        String key3 = "key3";
        String value1 = "value1";
        String value2 = "value2";
        String value3 = "value3";

        try {
            uri1 = new URI("https://yu/edu/documents/Document1");
            uri2 = new URI("https://yu/edu/documents/Document2");
            uri3 = new URI("https://yu/edu/documents/Document3");

            document1 = new DocumentImpl(uri1, text1, null);
            document1.setMetadataValue(key1, value1);

            document2 = new DocumentImpl(uri2, text2.getBytes());
            document2.setMetadataValue(key2, value2);

            document3 = new DocumentImpl(uri3, text3.getBytes());
            document3.setMetadataValue(key3, value3);

        } catch (URISyntaxException e) {
            System.out.println("URI syntax exception");
        }
    }

    @Test
    public void testSerializeAndDeserializeOneDocument(){
        try {
            persistenceManager.serialize(uri1, document1);

            // Deserialize documents
            Document deserializedDoc1 = persistenceManager.deserialize(uri1);

            // Check if deserialized documents are equal to the originals
            assertEquals(document1, deserializedDoc1);

        }catch(IOException e){
            e.printStackTrace(System.out);
        }
    }

    @Test
    public void testSerializeAndDeserializeDocument() {
        // Serialize documents
        try {
            persistenceManager.serialize(uri1, document1);
            persistenceManager.serialize(uri2, document2);
            persistenceManager.serialize(uri3, document3);

            // Deserialize documents
            Document deserializedDoc1 = persistenceManager.deserialize(uri1);
            Document deserializedDoc2 = persistenceManager.deserialize(uri2);
            Document deserializedDoc3 = persistenceManager.deserialize(uri3);

            // Check if deserialized documents are equal to the originals
            assertEquals(document1, deserializedDoc1);
            assertEquals(document2, deserializedDoc2);
            assertEquals(document3, deserializedDoc3);
        }catch(IOException e){
            e.printStackTrace(System.out);
        }
    }

    @Test
    public void testDeleteDocument() {
        try {
            // Serialize documents
            persistenceManager.serialize(uri1, document1);
            persistenceManager.serialize(uri2, document2);
            persistenceManager.serialize(uri3, document3);

            // Delete one document
            persistenceManager.delete(uri1);

            // Try to deserialize the deleted document
            Document deletedDoc = persistenceManager.deserialize(uri1);

            // Check if the deleted document is null
            assertNull(deletedDoc);

            // Check if other documents are still deserializable
            Document deserializedDoc2 = persistenceManager.deserialize(uri2);
            Document deserializedDoc3 = persistenceManager.deserialize(uri3);

            assertNotNull(deserializedDoc2);
            assertNotNull(deserializedDoc3);
        }catch(IOException e){
            e.printStackTrace(System.out);
        }
    }

    // Add more test cases as needed
}
