package xyz.spiralhalo.sherlock.report.factory;

import xyz.spiralhalo.sherlock.Tracker;
import xyz.spiralhalo.sherlock.persist.project.Project;
import xyz.spiralhalo.sherlock.report.persist.ReportRow;
import xyz.spiralhalo.sherlock.report.persist.ReportRows;
import xyz.spiralhalo.sherlock.util.Debug;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.util.Scanner;
import java.util.function.Supplier;

import static xyz.spiralhalo.sherlock.report.factory.Const.MINIMUM_SECOND;

public class ProjectViewCreator implements Supplier<Object[]> {
    private final Project p;

    public ProjectViewCreator(Project p) {
        this.p = p;
    }

    @Override
    public Object[] get() {
        try (FileInputStream fis = new FileInputStream(Tracker.getRecordFile());
             Scanner sc = new Scanner(fis)) {
            String[] temp;
            LocalDateTime c2;
            LocalDate cd=null,cm=null;
            int accuS = 0;
            int accuSM = 0;
            final ReportRows dayModel = new ReportRows();
            final ReportRows monthModel = new ReportRows();
            final LocalDate today = LocalDate.now();
            while (sc.hasNext()) {
                try {
                    temp = sc.nextLine().split(Tracker.SPLIT_DIVIDER);
                    if(p.getHash()!=Long.parseLong(temp[2])) continue;
                    c2 = ZonedDateTime.parse(temp[0],Tracker.DTF).toLocalDateTime();
                    if(cd==null)cd=c2.toLocalDate();
                    if(cm==null)cm=c2.toLocalDate();
                    if (c2.toLocalDate().isAfter(cd)) {
                        if(accuS >= MINIMUM_SECOND || cd.equals(today)) {
                            dayModel.add(new ReportRow(cd, accuS));
                            accuSM += accuS;
                        }
                        cd = c2.toLocalDate();
                        accuS = 0;
                    }
                    if ( cm.get(ChronoField.MONTH_OF_YEAR) != c2.get(ChronoField.MONTH_OF_YEAR)
                            || cm.get(ChronoField.YEAR) != c2.get(ChronoField.YEAR)){
                        if(accuSM >= MINIMUM_SECOND) {
                            monthModel.add(new ReportRow(cm, accuSM));
                        }
                        cm = c2.toLocalDate();
                        accuSM = 0;
                    }
                    int x = Integer.parseInt(temp[1]);
                    accuS += x;
                } catch (NumberFormatException e) {
                    Debug.log(e);
                }
            }
            if(accuS >= MINIMUM_SECOND) {
                dayModel.add(new ReportRow(cd, accuS));
            }
            if(accuSM >= MINIMUM_SECOND) {
                monthModel.add(new ReportRow(cm, accuSM));
            }
            return new Object[]{p, dayModel, monthModel};
        } catch (IOException e1) {
            Debug.log(e1);
            throw new RuntimeException(e1);
        }
    }
}
