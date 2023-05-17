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

package xyz.spiralhalo.sherlock.report.factory.table;

import java.io.Serializable;
import java.time.LocalDate;

public class ReportRow implements Serializable {
    private LocalDate timestamp;
    private int seconds;

    public ReportRow(LocalDate timestamp, int seconds) {
        this.timestamp = timestamp;
        this.seconds = seconds;
    }

    public LocalDate getTimestamp() {
        return timestamp;
    }

    public int getSeconds() {
        return seconds;
    }
}
