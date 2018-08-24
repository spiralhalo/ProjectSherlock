//package xyz.spiralhalo.sherlock.persist.cache;
//
//import java.io.Serializable;
//import java.time.Instant;
//import java.time.ZoneId;
//import java.time.ZonedDateTime;
//
//public class Cached implements Serializable {
//    public static final long serialVersionUID = 1L;
//
//    private Object cached;
//    private Class cachedClass;
//    private Instant created;
//
//    public Cached(Object cached, Class cachedClass) {
//        this.cached = cached;
//        this.cachedClass = cachedClass;
//        this.created = Instant.now();
//    }
//
//    public ZonedDateTime getCreatedTime(){
//        return created.atZone(ZoneId.systemDefault());
//    }
//
//    public long getSecondSinceCreated() {
//        return Instant.now().getEpochSecond() - created.getEpochSecond();
//    }
//
//    public Object getCached(Class e) {
//        if(e.equals(cachedClass))
//            return cached;
//        return null;
//    }
//}
