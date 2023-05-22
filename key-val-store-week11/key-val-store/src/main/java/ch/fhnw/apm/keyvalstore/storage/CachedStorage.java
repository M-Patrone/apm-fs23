package ch.fhnw.apm.keyvalstore.storage;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Stack;

public class CachedStorage implements Storage{

    private final Stack<Storage> cache = new Stack<>();
    private final Storage storage;
    private int size;
    private int counter=0;

    public CachedStorage(int cacheSize, Storage storageToBeCached){
        this.storage = storageToBeCached;
        this.size = cacheSize;
    }
    @Override
    public boolean store(int key, String value) {
       if(this.counter<this.size){
           this.cache.forEach(f-> f.store(key,value));
           this.counter++;
       }else{

       }
    }

    @Override
    public String load(int key) {
        return this.storage.load(key);
    }
}
