package xyz.spiralhalo.sherlock.persist.cache;

import java.io.Serializable;
import java.time.Instant;
import java.util.WeakHashMap;

public class CacheMgr {
    public static final Instant NEVER = Instant.EPOCH;

    private final WeakHashMap<String, CachedObj> cache = new WeakHashMap<>();

    public <T extends Serializable> void put(CacheId id, T object){
        put(id.v, object);
    }

    public synchronized <T extends Serializable> void put(String id, T object){
        cache.put(id, CacheHandler.writeCache(id, object));
    }

    public Instant getCreated(CacheId id){
        return getCreated(id.v);
    }

    public long getElapsed(CacheId id){
        return getElapsed(id.v);
    }

    public <T extends Serializable> T getObj(CacheId id, Class<T> clazz){
        return getObj(id.v, clazz);
    }

    public Instant getCreated(String id){
        CachedObj x = getInternal(id);
        if(x == null || x.getObject() == null){ return NEVER; }
        return x.getCreated();
    }

    public long getElapsed(String id){
        CachedObj x = getInternal(id);
        if(x == null || x.getObject() == null){ return Long.MAX_VALUE; }
        return x.getElapsed();
    }

    public <T extends Serializable> T getObj(String id, Class<T> clazz){
        CachedObj x = getInternal(id);
        if(x == null){ return null; }
        if(clazz.isInstance(x.getObject())){
            return clazz.cast(x.getObject());
        } else return null;
    }

    private synchronized CachedObj getInternal(String id){
        CachedObj x = cache.get(id);
        if(x==null){
            x = CacheHandler.readCache(id);
            if(x!=null){ cache.put(id, x); }
        }
        return x;
    }
}
