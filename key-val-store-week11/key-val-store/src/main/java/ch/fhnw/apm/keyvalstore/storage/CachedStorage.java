package ch.fhnw.apm.keyvalstore.storage;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class CachedStorage implements Storage{

    private final ConcurrentHashMap<Integer,Entry> cache = new ConcurrentHashMap<>();
    private final Storage storage;
    private int size;
    private int counter=0;

    public CachedStorage(int cacheSize, Storage storageToBeCached){
        this.storage = storageToBeCached;
        this.size = cacheSize;
    }
    @Override
    public boolean store(int key, String value) {
        this.storage.store(key,value);

       if(this.counter<this.size){
            this.cache.put(key,new Entry(key,value));
            this.counter++;
            return true;
       }else{
           int replaceKey = this.getRandomKey();
           this.cache.remove(replaceKey);
           this.cache.put(key,new Entry(key,value));
        return true;
       }
    }

    private int getRandomKey(){
        int randomIndex = ThreadLocalRandom.current().nextInt(this.cache.size());
        int currentIndex = 0;
        for (Map.Entry<Integer, Entry> entry : this.cache.entrySet()) {
            if (currentIndex == randomIndex) {
                return entry.getKey();
            }
            currentIndex++;
        }
        return -1;
    }

    @Override
    public String load(int key) {
         Entry e = this.cache.get(key);
         if(e == null){
             return this.storage.load(key);
         }else{
             return e.value;
         }
    }
    private class Entry{
        int key;
        String value;

        public Entry(int key, String value) {
            this.key = key;
            this.value = value;
        }
    }
}
