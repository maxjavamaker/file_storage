package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.MinHeap;

import java.util.NoSuchElementException;

public class MinHeapImpl<E extends Comparable<E>> extends MinHeap<E> {
    public MinHeapImpl(){
        this.elements = (E[]) new Comparable[2];
    }

    @Override
    public void reHeapify(E element){
        int k = this.getArrayIndex(element);
        if (k != 1 && !this.isGreater(k, k/2)){  //if it is not more recent than its parent upHeap
            this.upHeap(k);
        }
        else if (this.elements.length > 2*k && this.elements[2*k] != null && this.isGreater(k, 2*k)){  //if k is more recent than its kids downHeap
            this.downHeap(k);
        }

    }

    @Override
    protected int getArrayIndex(E element){
        for (int i = 0; i < this.elements.length; i++){
            if (this.elements[i] != null && this.elements[i].equals(element)){
                return i;
            }
        }

        throw new NoSuchElementException("the element was not found in the heap");
    }

    @Override
    protected void doubleArraySize() {
        E[] temp = (E[]) new Comparable[this.elements.length * 2];  //double length of array

        for (int i = 0; i < this.elements.length; i++) {  //copy over the values
            temp[i] = this.elements[i];
        }

        this.elements = temp;
    }
}
