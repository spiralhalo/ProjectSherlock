package xyz.spiralhalo.sherlock.record;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.Arrays;

public class RecordEntry {
    public static final int BYTES_UNIVERSAL = 22;
    public static final byte CURRENT_VERSION = 1;

    private static final byte UTILITY = 0b01;
    private static final byte PRODUCTIVE = 0b10;

    public static RecordEntry create(Instant time, int elapsed, long hash, boolean isUtility, boolean isProductive){
        return new RecordEntry(time, elapsed, hash, isUtility, isProductive);
    }

    public static RecordEntry deserialize(byte[] data){
        byte version = data[0]; //check for different length for future versions
        if(data.length != BYTES_UNIVERSAL)throw new IllegalArgumentException("Record data length mismatch.");
        byte[] t = Arrays.copyOfRange(data,1, 9);
        byte[] el = Arrays.copyOfRange(data, 9,13);
        byte[] h = Arrays.copyOfRange(data, 13, 21);
        byte m = data[21];
        switch (version) {
            default:
            return new RecordEntry(Instant.ofEpochMilli(deserializeLong(t)), deserializeInt(el), deserializeLong(h), m);
        }
    }

    public static Instant getTimestamp(byte[] data){
        byte[] t = Arrays.copyOfRange(data,1, 9);
        return Instant.ofEpochMilli(deserializeLong(t));
    }

    public static byte[] serialize(RecordEntry entry){
        return serialize(entry.time, entry.elapsed, entry.hash, entry.meta);
    }

    public static byte[] serialize(Instant time, int elapsed, long hash, boolean isUtility, boolean isProductive) {
        return serialize(time, elapsed, hash, meta(isUtility, isProductive));
    }

    private static byte[] serialize(Instant time, int elapsed, long hash, byte meta){
        return ByteBuffer.allocate(BYTES_UNIVERSAL)
                .put(CURRENT_VERSION)
                .putLong(time.toEpochMilli())
                .putInt(elapsed)
                .putLong(hash)
                .put(meta)
                .array();
    }

    private static byte meta(boolean isUtility, boolean isProductive){
        return (byte)((isUtility?UTILITY:0) | (isProductive?PRODUCTIVE:0));
    }

    private static long deserializeLong(final byte[] ba){
        if(ba.length != Long.BYTES) throw new IllegalArgumentException("Byte array needs to represent a long getValue.");
        return ((ByteBuffer)ByteBuffer.allocate(Long.BYTES).put(ba).flip()).getLong();
    }

    private static int deserializeInt(final byte[] ba){
        if(ba.length != Integer.BYTES) throw new IllegalArgumentException("Byte array needs to represent a long getValue.");
        return ((ByteBuffer)ByteBuffer.allocate(Integer.BYTES).put(ba).flip()).getInt();
    }
    private final Instant time;
    private final int elapsed;
    private final long hash;
    private final byte meta;

    private RecordEntry(Instant time, int elapsed, long hash, byte meta) {
        this.time = time;
        this.elapsed = elapsed;
        this.hash = hash;
        this.meta = meta;
    }

    private RecordEntry(Instant time, int elapsed, long hash, boolean isUtility, boolean isProductive) {
        this(time, elapsed, hash, meta(isUtility, isProductive));
    }

    public Instant getTime() {
        return time;
    }

    public int getElapsed() {
        return elapsed;
    }

    public long getHash() {
        return hash;
    }

    public boolean isUtility() {
        return (meta & UTILITY) != 0;
    }

    public boolean isProductive() {
        return (meta & PRODUCTIVE) != 0;
    }
}
