package edu.yu.cs.com1320.project.stage4.impl;

import edu.yu.cs.com1320.project.Trie;
import edu.yu.cs.com1320.project.impl.TrieImpl;
import java.util.*;
import edu.yu.cs.com1320.project.stage4.Document;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TrieImplTest {
    Trie<Integer> trie;

    @BeforeEach
    public void setup(){
        trie = new TrieImpl<>();  //create a trie of integers

    }

    @Test
    public void putNullValue(){
        trie.put("happy", 5);
        assert(trie.get("happy").contains(5));
    }

    @Test
    public void differentiateUpperCase(){
        trie.put("happy", 5);
        trie.put("Happy", 6);
        trie.put("HAppy", 7);
        assert(trie.get("happy").contains(5));
        assert(trie.get("Happy").contains(6));
        assert(trie.get("HAppy").contains(7));
    }

    @Test
    public void getSorted(){
        Comparator<Integer> comparator =  Comparator.comparingInt(number -> {
            return number;
        });
        trie.put("happy", 2);
        trie.put("happy", -3);
        trie.put("happy", 7);
        List<Integer> list = new ArrayList<>(List.of(7, 2, -3));
        assertEquals(list, trie.getSorted("happy", comparator.reversed()));
    }

    @Test
    public void getAllWithPrefixSorted(){
        Comparator<Integer> comparator =  Comparator.comparingInt(number -> {
            return number;
        });
        trie.put("happy", 2);
        trie.put("happy", -3);
        trie.put("happy", 7);
        List<Integer> list = new ArrayList<>(List.of(7, 2, -3));
        assertEquals(0, trie.getAllWithPrefixSorted("Happy", comparator.reversed()).size());
        assertEquals(3, trie.getAllWithPrefixSorted("happy", comparator.reversed()).size());
        assertEquals(list, trie.getAllWithPrefixSorted("happy", comparator.reversed()));

    }

    @Test
    public void deleteAllWithPrefix(){
        trie.put("happiness", 2);
        trie.put("happen", -3);
        trie.put("Happy", 7);
        trie.put("happy", 7);
        trie.put("danny", 4);
        Set<Integer> list = new HashSet<>(Arrays.asList(7, 2, -3));
        Set<Integer> list2 = new HashSet<>((List.of(7)));
        Set<Integer> empty = new HashSet<>();

        assertEquals(list, trie.deleteAllWithPrefix("hap"));
        assertEquals(empty, trie.deleteAllWithPrefix("hap"));
        assertEquals(list2, trie.deleteAllWithPrefix("Hap"));
        assertEquals(empty, trie.deleteAllWithPrefix("dor"));
        assertEquals(empty, trie.deleteAllWithPrefix("Hap"));
    }

    @Test
    public void deleteAll(){
        Comparator<Integer> comparator =  Comparator.comparingInt(number -> {
            return number;
        });
        trie.put("happen", -3);
        trie.put("happening", 7);
        trie.put("happen", 4);
        Set<Integer> list = new HashSet<>(Arrays.asList(7, 2, -3));
        Set<Integer> happen = new HashSet<>((List.of(4, -3)));
        Set<Integer> empty = new HashSet<>();

        assertEquals(happen, trie.deleteAll("happen"));
        assertEquals(empty, trie.deleteAll("happen"));
        assertEquals(empty, trie.deleteAll("ho"));
    }

    @Test
    public void delete(){
        trie.put("7", 2);
        trie.put("7", -3);
        trie.put("Happy", 7);
        trie.put("happening", 7);
        trie.put("happen", 4);
        Set<Integer> list = new HashSet<>(List.of(2));
        Set<Integer> happen = new HashSet<>((List.of(4, -3)));
        Set<Integer> empty = new HashSet<>();

        assertEquals(-3, trie.delete("7", -3));
        assertEquals(list, trie.get("7"));
        assert(trie.get("71").isEmpty());
        trie.delete("happening", 7);
        assert(trie.get("happening").isEmpty());
        assert(trie.get("happeni").isEmpty());
    }

    @Test
    public void getAllWithSpace(){
        trie.put("7", 2);
        trie.put("7", -3);
        trie.put("Happy", 7);
        trie.put("happening", 7);
        trie.put("happen", 4);
    }
}
