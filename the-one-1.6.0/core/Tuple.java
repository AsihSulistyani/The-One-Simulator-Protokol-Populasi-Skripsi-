/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package core;

/**
 *
 * @author Asih
 */
public class Tuple<K, V> {

    private K key;
    private V value;

    /**
     * Creates a new tuple.
     *
     * @param key The key of the tuple
     * @param value The value of the tuple
     */
    public Tuple(K key, V value) {
        this.key = key;
        this.value = value;
    }

    /**
     * Returns the key
     *
     * @return the key
     */
    public K getKey() {
        return key;
    }

    /**
     * Returns the value
     *
     * @return the value
     */
    public V getValue() {
        return value;
    }

    /**
     * Returns a string representation of the tuple
     *
     * @return a string representation of the tuple
     */
    public String toString() {
        return key.toString() + ":" + value.toString();
    }
}
