package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.MinHeap;

import java.util.NoSuchElementException;

public class MinHeapImpl<E extends Comparable<E>> extends MinHeap<E> {
    public MinHeapImpl(){
        this.elements = (E[]) new Comparable[10];
    }

    @Override
    public void reHeapify(E element){
        int k = this.getArrayIndex(element);
        if (!this.isGreater(k, k/2)){  //if k is smaller than its parent upheap
            this.upHeap(k);
        }
        else if (this.isGreater(k, 2*k) || this.isGreater(k, 2*k + 1)){  //if k is greater than its children downheap
            this.downHeap(k);
        }

    }

    @Override
    public int getArrayIndex(E element){
        for (int i = 0; i < this.elements.length; i++){
            if (this.elements[i] != null && this.elements[i].equals(element)){
                return i;
            }
        }

        throw new NoSuchElementException("the element was not found in the heap");
    }

    @Override
    protected void doubleArraySize(){
        E[] temp = (E[]) new Comparable[this.count * 2];  //double length of array

        for (int i = 0; i < this.count; i++){  //copy over the values
            temp[i] = this.elements[i];
        }

        this.elements = temp;
    }
}
