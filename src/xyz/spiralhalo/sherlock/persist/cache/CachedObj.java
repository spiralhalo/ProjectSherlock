package xyz.spiralhalo.sherlock.persist.cache;

import java.io.Serializable;
import java.time.Instant;

public class CachedObj implements Serializable {
    public static final long serialVersionUID = 1L;

    private final Serializable object;
    private final Instant created;

    CachedObj(Serializable object){
        this(object, Instant.now());
    }

    CachedObj(Serializable object, Instant created){
        this.object = object;
        this.created = created;
    }

    public Serializable getObject() {
        return object;
    }

    public Instant getCreated() {
        return created;
    }

    public long getElapsed() {
        return Instant.now().getEpochSecond() - created.getEpochSecond();
    }
}
