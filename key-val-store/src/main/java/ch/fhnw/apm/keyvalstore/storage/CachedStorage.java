package ch.fhnw.apm.keyvalstore.storage;

import java.util.Arrays;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class CachedStorage implements Storage {

    private Item[] localCache;
    private Storage s;
    private int counter = 0;

    public CachedStorage(int cacheSize, Storage storageToBeCached) {
        this.localCache = new Item[cacheSize];

        this.s = storageToBeCached;
    }

    @Override
    public boolean store(int key, String value) {
        return this.s.store(key, value);
    }

    @Override
    public String load(int key) {

        for (int i = 0; i < this.localCache.length; i++) {
            if (this.localCache[i]!= null &&this.localCache[i].getK() == key) {
                System.out.println("******************************IM CACHE*********************************");
                return this.localCache[i].content;
            }
        }

        if (counter < localCache.length) {
            String v = this.s.load(key);
            localCache[counter] = new Item(key, v);
            counter++;
            return v;
        } else {
            Random random = new Random();
            int randomPosition = random.nextInt(localCache.length);
            String v = this.s.load(key);
            localCache[randomPosition] = new Item(key, v);
            return v;
        }
    }

    private class Item {
        int key1;
        String content;

        public Item(int k, String sc) {
            this.content = sc;
            this.key1 = k;
        }

        public int getK() {
            return key1;
        }
    }
}
