package xyz.spiralhalo.sherlock.persist.cache;

import xyz.spiralhalo.sherlock.Debug;
import xyz.spiralhalo.sherlock.Application;

import java.io.*;

class CacheHandler {
    static CachedObj writeCache(String name, Serializable y){
        File cacheFile = new File(Application.getCacheDir(), name);
        try (FileOutputStream fos = new FileOutputStream(cacheFile);
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            CachedObj x = new CachedObj(y);
            oos.writeObject(x);
            return x;
        } catch (IOException e) {
            Debug.log(e);
        }
        return null;
    }

    static CachedObj readCache(String name){
        File cacheFile = new File(Application.getCacheDir(), name);
        if(cacheFile.exists()) {
            try (FileInputStream fis = new FileInputStream(cacheFile);
                 ObjectInputStream ois = new ObjectInputStream(fis)) {
                return (CachedObj) ois.readObject();
            } catch (ClassCastException | ClassNotFoundException | IOException e) {
                Debug.log(e);
            }
        }
        return null;
    }
}
