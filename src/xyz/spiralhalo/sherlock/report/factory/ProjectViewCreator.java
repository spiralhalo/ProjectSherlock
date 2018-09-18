package xyz.spiralhalo.sherlock.report.factory;

import xyz.spiralhalo.sherlock.Application;
import xyz.spiralhalo.sherlock.async.AsyncTask;
import xyz.spiralhalo.sherlock.persist.project.Project;
import xyz.spiralhalo.sherlock.record.DefaultRecordWriter;
import xyz.spiralhalo.sherlock.record.RecordEntry;
import xyz.spiralhalo.sherlock.record.RecordScanner;
import xyz.spiralhalo.sherlock.record.io.RecordFileSeek;
import xyz.spiralhalo.sherlock.report.factory.table.ReportRow;
import xyz.spiralhalo.sherlock.report.factory.table.ReportRows;
import xyz.spiralhalo.sherlock.Debug;

import java.io.File;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;

public class ProjectViewCreator extends AsyncTask<ProjectViewResult> {
    private static final int MINIMUM_SECOND = 5*60;
    private ProjectViewResult result;
    private final Project p;

    public ProjectViewCreator(Project p) {
        this.p = p;
    }

    @Override
    public void doRun() throws Exception {
        File recordFile = new File(Application.getSaveDir(), DefaultRecordWriter.RECORD_FILE);
        RecordFileSeek seeker = new RecordFileSeek(recordFile, false);
        seeker.seekFirstOfDay(p.getStartDate().toLocalDate(), ZoneId.systemDefault());
        try (RecordScanner sc = new RecordScanner(seeker)) {
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
                    c2 = temp.getTime().atZone(ZoneId.systemDefault());
                    if(p.isFinished() && c2.isAfter(p.getFinishedDate())){break;}
                    if(p.getHash()!=temp.getHash()) continue;
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
