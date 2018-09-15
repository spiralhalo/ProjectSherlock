package xyz.spiralhalo.sherlock.record;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.Arrays;

public class RecordData {
    public static final int BYTES = Byte.BYTES + Long.BYTES + Integer.BYTES + Long.BYTES + Byte.BYTES;
    public static final byte VERSION = 1;

    private static final byte UTILITY = 0b01;
    private static final byte PRODUCTIVE = 0b10;

    public static RecordData create(Instant time, int elapsed, long hash, boolean isUtility, boolean isProductive){
        return new RecordData(time, elapsed, hash, isUtility, isProductive);
    }

    public static RecordData deserialize(byte[] data){
        if(data.length != BYTES)throw new IllegalArgumentException("Record data length mismatch.");
        byte version = data[0];
        byte[] t = Arrays.copyOfRange(data,1, Long.BYTES + 1);
        byte[] el = Arrays.copyOfRange(data, 1 + Long.BYTES,Long.BYTES + Integer.BYTES + 1);
        byte[] h = Arrays.copyOfRange(data, 1 + Long.BYTES + Integer.BYTES, 2 * Long.BYTES + Integer.BYTES + 1);
        byte m = data[BYTES - 1];
        switch (version) {
            default:
            return new RecordData(Instant.ofEpochMilli(deserializeLong(t)), deserializeInt(el), deserializeLong(h), m);
        }
    }

    public static byte[] serialize(Instant time, int elapsed, long hash, boolean isUtility, boolean isProductive) {
        return serialize(time, elapsed, hash, meta(isUtility, isProductive));
    }

    private static byte[] serialize(Instant time, int elapsed, long hash, byte meta){
        return ByteBuffer.allocate(BYTES)
                .put(VERSION)
                .putLong(time.toEpochMilli())
                .putInt(elapsed)
                .putLong(hash)
                .put(meta)
                .array();
    }

    private static byte meta(boolean isUtility, boolean isProductive){
        return (byte)((isUtility?UTILITY:0) & (isProductive?PRODUCTIVE:0));
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

    private RecordData(Instant time, int elapsed, long hash, byte meta) {
        this.time = time;
        this.elapsed = elapsed;
        this.hash = hash;
        this.meta = meta;
    }

    private RecordData(Instant time, int elapsed, long hash, boolean isUtility, boolean isProductive) {
        this(time, elapsed, hash, meta(isUtility, isProductive));
    }

    public byte[] serialize(){
        return serialize(time, elapsed, hash, meta);
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
