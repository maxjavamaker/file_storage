package edu.yu.cs.com1320.project.stage6.impl;

import edu.yu.cs.com1320.project.Stack;
import edu.yu.cs.com1320.project.impl.StackImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class StackImplTest {
    String element1 = "element1";
    String element2 = "element2";
    String element3 = "element3";
    String element4 = "element4";
    Stack<String> stack;
    @BeforeEach
    public void setup(){
         stack = new StackImpl<>();
    }

    @Test
    public void nullHeadPop(){
        assertNull(stack.pop());
    }

    @Test
    public void pop(){
        stack.push(element1);
        assertEquals("element1", stack.pop());
    }

    @Test
    public void peekNull(){
        assertNull(stack.peek());
    }

    @Test
    public void peek(){
        stack.push(element2);
        assertEquals("element2", stack.peek());
        assertEquals("element2", stack.pop());
    }

    @Test
    public void size(){
        stack.push(element1);
        assertEquals(1, stack.size());
        stack.push(element2);
        assertEquals(2, stack.size());
        stack.push(element3);
        assertEquals(3, stack.size());
        stack.pop();
        assertEquals(2, stack.size());
    }
}
