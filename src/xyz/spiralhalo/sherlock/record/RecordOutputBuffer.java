package xyz.spiralhalo.sherlock.record;

import xyz.spiralhalo.sherlock.record.io.RecordFileAppend;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;

public class RecordOutputBuffer {

    private final SimpleByteBuffer byteBuffer;
    private int timesWritten = 0;

    public RecordOutputBuffer(int capacityBytesOnDisk) {
        byteBuffer = new SimpleByteBuffer(capacityBytesOnDisk);
    }

    public void put(byte[] pureData){
        synchronized (byteBuffer) {
            if (pureData.length != RecordEntry.BYTES_UNIVERSAL) throw new IllegalArgumentException("Length mismatch.");
            byteBuffer.put(pureData);
            timesWritten++;
        }
    }

    public void flush(RecordFileAppend rfa) throws IOException{
        synchronized (byteBuffer) {
            IOException ex;
            try {
                rfa.writeBytes(byteBuffer.array());
                ex = null;
            } catch (IOException e) {
                ex = e;
            } finally {
                byteBuffer.clear();
                timesWritten = 0;
            }
            if (ex != null) {
                throw ex;
            }
        }
    }

    public int getTimesWritten() {
        return timesWritten;
    }

    public boolean full(){
        synchronized (byteBuffer) {
            return byteBuffer.full();
        }
    }

    private static class SimpleByteBuffer{
        private final byte[] buffer;
        private int pos = 0;

        SimpleByteBuffer(int capacity) {
            this.buffer = new byte[capacity];
        }

        void put(byte[] bytes) {
            if(pos + bytes.length > buffer.length) throw new IllegalArgumentException("Byte array length exceeds capacity.");
            System.arraycopy(bytes, 0, buffer, pos, bytes.length);
            pos += bytes.length;
        }

        byte[] array() {
            return Arrays.copyOfRange(buffer, 0, pos);
        }

        boolean full() {
            return pos == buffer.length;
        }

        void clear() {
            pos = 0;
        }
    }
}
