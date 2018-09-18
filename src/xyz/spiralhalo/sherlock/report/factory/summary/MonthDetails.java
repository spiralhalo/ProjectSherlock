package xyz.spiralhalo.sherlock.report.factory.summary;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class MonthDetails extends ArrayList<DetailsRow> implements Serializable {
    public static final long serialVersionUID = 1L;
    private final HashMap<Long,ArrayList<Integer>> index;

    public MonthDetails() {
        this.index = new HashMap<>();
    }

    @Override
    public boolean add(DetailsRow detailsRow) {
        if(super.add(detailsRow)){
            int pos = size()-1;
            long hash = detailsRow.getSummary().getHash();
            index.putIfAbsent(hash, new ArrayList<>());
            index.get(hash).add(pos);
            return true;
        } else return false;
    }

    @Override
    public boolean addAll(Collection<? extends DetailsRow> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void add(int index, DetailsRow element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(int index, Collection<? extends DetailsRow> c) {
        throw new UnsupportedOperationException();
    }

    public HashMap<Long, ArrayList<Integer>> getIndex() {
        return index;
    }
}
