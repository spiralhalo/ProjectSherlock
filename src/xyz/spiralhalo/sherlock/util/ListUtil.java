package xyz.spiralhalo.sherlock.util;

import java.util.Iterator;
import java.util.List;

public class ListUtil {
    private ListUtil(){}
    public static <T> Iterable<T> extensiveIterator(List<? extends T> list1, List<? extends T> list2){
        return new ExtensiveIterator<>(new List[]{list1, list2});
    }
    public static <T> Iterable<T> extensiveIterator(List<? extends T> list1, List<? extends T> list2, List<? extends T> list3){
        return new ExtensiveIterator<>(new List[]{list1, list2, list3});
    }

    private static class ExtensiveIterator<T> implements Iterable<T>, Iterator<T>{
        private Iterator<T>[] iterators;

        private ExtensiveIterator(List<T>[] lists){
            this.iterators = new Iterator[lists.length];
            for (int i = 0; i < lists.length; i++) {
                this.iterators[i] = lists[i].iterator();
            }
        }

        @Override
        public boolean hasNext() {
            for (Iterator<T> i:iterators) {
                if(i.hasNext())return true;
            }
            return false;
        }

        @Override
        public T next() {
            for (Iterator<T> i:iterators) {
                if(i.hasNext())return i.next();
            }
            return null;
        }

        @Override
        public Iterator<T> iterator() {
            return this;
        }
    }
}
