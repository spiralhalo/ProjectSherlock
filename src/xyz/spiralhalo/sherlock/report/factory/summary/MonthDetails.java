//
//    Copyright 2020 spiralhalo <re.nanashi95@gmail.com>
//
//    This file is part of Project Sherlock.
//
//    Project Sherlock is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    Project Sherlock is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with Project Sherlock.  If not, see <https://www.gnu.org/licenses/>.
//

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

    public HashMap<Long, ArrayList<Integer>> getIndices() {
        return index;
    }
}
