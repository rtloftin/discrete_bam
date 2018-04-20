package bam.human;

import java.util.HashSet;

/**
 * A simple class for managing a collection
 * of objects, and ensuring that the number
 * of objects present does not exceed a
 * given maximum.
 */
public class Pool {

    private final int max_items;
    private final HashSet<Object> items;

    private Pool(int max_items) {
        this.max_items = max_items;

        items = new HashSet<>();
    }

    public static Pool maxItems(int max_items) {
        return new Pool(max_items);
    }

    public boolean full() {
        return (items.size() >= max_items);
    }

    public void add(Object item) {
        if(items.size() < max_items)
            items.add(item);
    }

    public void remove(Object item) {
        items.remove(item);
    }
}
