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

package xyz.spiralhalo.sherlock.persist.project;

import java.io.Serializable;

public class UtilityTag extends Project implements Serializable {
    public static final long serialVersionUID = 1L;

    private boolean productive;

    public UtilityTag(String name, String category, String tags, int ptype) {
        super(name, category, tags, ptype);
        this.productive = (ptype == PTYPE_PRODUCTIVE);
    }

    @Override
    public boolean isProductive() {
        if(_appendix().containsKey(APPEND_PTYPE)){
            return ((int) _appendix().get(APPEND_PTYPE) == PTYPE_PRODUCTIVE);
        }
        return productive;
    }
//
//    public void edit(String name, String category, String tags, int productive){
//        super.edit(name, category, tags, productive);
//        this.productive = productive;
//    }
}
