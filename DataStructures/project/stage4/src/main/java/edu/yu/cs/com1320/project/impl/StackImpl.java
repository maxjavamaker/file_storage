package edu.yu.cs.com1320.project.impl;
import edu.yu.cs.com1320.project.Stack;

/**
 * @param <T>
 */
public class StackImpl<T> implements Stack<T>{
    private Node head = null;
    private int size = 0;

    private class Node{
        private Node next;
        private T command;

        public Node(T command, Node next){
            this.command = command;
            this.next = next;
        }
    }


    /**
     * @param element object to add to the Stack
     */
    public void push(T element){
        Node newNode = new Node(element, null);

        if(head == null){
            head = newNode;
            size++;
            return;
        }

        newNode.next = head;
        head = newNode;
        size++;
    }

    /**
     * removes and returns element at the top of the stack
     * @return element at the top of the stack, null if the stack is empty
     */
    public T pop(){
        if (head == null){
            return null;
        }

        Node oldHead = head;
        head = head.next;
        size--;

        return oldHead.command;
    }

    /**
     *
     * @return the element at the top of the stack without removing it
     */
    public T peek(){
        if (head == null){
            return null;
        }

        return head.command;
    }

    /**
     *
     * @return how many elements are currently in the stack
     */
    public int size(){
        return size;
    }
}
