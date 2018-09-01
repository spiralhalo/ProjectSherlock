package xyz.spiralhalo.sherlock.record;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;

public class DiskOutput {
    public static int getBytesOnDisk(int originalLength){
        return originalLength+1;
    }

    public static byte[] read(RandomAccessFile radFile) throws IOException{
        byte meta = radFile.readByte();
        byte[] toRead = new byte[meta & 0xff]; //extreme bitwise magick!!! (`meta` becomes positive int)
        radFile.readFully(toRead);
        return toRead;
    }

    private final SimpleByteBuffer byteBuffer;

    public DiskOutput(int capacityBytesOnDisk) {
        byteBuffer = new SimpleByteBuffer(capacityBytesOnDisk);
    }

    public void put(byte[] toBeWritten){
        if(toBeWritten.length > 255) throw new IllegalArgumentException("Byte array is too big.");
        byte[] toWrite = new byte[1+toBeWritten.length];
        toWrite[0] = (byte)toBeWritten.length;
        System.arraycopy(toBeWritten, 0, toWrite, 1, toBeWritten.length);
        byteBuffer.put(toWrite);
    }

    public void flush(RandomAccessFile raf) throws IOException{
        IOException ex;
        try {
            raf.write(byteBuffer.array());
            ex = null;
        } catch (IOException e) {
            ex = e;
        } finally {
            byteBuffer.clear();
        }
        if(ex!=null){
            throw ex;
        }
    }

    public boolean full(){
        return byteBuffer.full();
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
