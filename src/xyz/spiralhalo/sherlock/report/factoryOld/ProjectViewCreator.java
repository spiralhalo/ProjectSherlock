package xyz.spiralhalo.sherlock.report.factoryOld;

import xyz.spiralhalo.sherlock.Application;
import xyz.spiralhalo.sherlock.async.AsyncTask;
import xyz.spiralhalo.sherlock.persist.project.Project;
import xyz.spiralhalo.sherlock.record.RecordEntry;
import xyz.spiralhalo.sherlock.record.legacy.AutoImporter3;
import xyz.spiralhalo.sherlock.report.persist.ReportRow;
import xyz.spiralhalo.sherlock.report.persist.ReportRows;
import xyz.spiralhalo.sherlock.Debug;

import java.io.File;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;

import static xyz.spiralhalo.sherlock.report.factoryOld.Const.MINIMUM_SECOND;

public class ProjectViewCreator extends AsyncTask<ProjectViewResult> {
    private ProjectViewResult result;
    private final Project p;
    private final LocalDate from, until;

    public ProjectViewCreator(Project p) {
        this(p, null, null);
    }

    public ProjectViewCreator(Project p, LocalDate from, LocalDate until) {
        this.p = p;
        this.from = from;
        this.until = until;
    }

    @Override
    public void doRun() throws Exception {
        try (AutoImporter3.OldRecordScanner sc = new AutoImporter3.OldRecordScanner(new File(Application.getSaveDir(),"record"), from, until)) {
            RecordEntry temp;
            ZonedDateTime c2;
            LocalDate cd=null,cm=null;
            int accuS = 0;
            int accuSM = 0;
            final ReportRows dayRows = new ReportRows();
            final ReportRows monthRows = new ReportRows();
            final LocalDate today = LocalDate.now();
            while (sc.hasNext()) {
                try {
                    temp = sc.next();
                    if(p.getHash()!=temp.getHash()) continue;
                    c2 = temp.getTime().atZone(ZoneId.systemDefault());
                    if(cd==null)cd=c2.toLocalDate();
                    if(cm==null)cm=c2.toLocalDate();
                    if (c2.toLocalDate().isAfter(cd)) {
                        if(accuS >= MINIMUM_SECOND || cd.equals(today)) {
                            dayRows.add(new ReportRow(cd, accuS));
                            accuSM += accuS;
                        }
                        cd = c2.toLocalDate();
                        accuS = 0;
                    }
                    if ( cm.get(ChronoField.MONTH_OF_YEAR) != c2.get(ChronoField.MONTH_OF_YEAR)
                            || cm.get(ChronoField.YEAR) != c2.get(ChronoField.YEAR)){
                        if(accuSM >= MINIMUM_SECOND) {
                            monthRows.add(new ReportRow(cm, accuSM));
                        }
                        cm = c2.toLocalDate();
                        accuSM = 0;
                    }
                    accuS += temp.getElapsed();
                } catch (NumberFormatException e) {
                    Debug.log(e);
                }
            }
            if(accuS >= MINIMUM_SECOND) {
                dayRows.add(new ReportRow(cd, accuS));
            }
            if(accuSM >= MINIMUM_SECOND) {
                monthRows.add(new ReportRow(cm, accuSM));
            }
            result = new ProjectViewResult(p, dayRows, monthRows);
        }
    }

    @Override
    protected ProjectViewResult getResult() {
        return result;
    }
}
