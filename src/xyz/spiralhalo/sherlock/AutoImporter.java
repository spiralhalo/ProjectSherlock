package xyz.spiralhalo.sherlock;

import xyz.spiralhalo.sherlock.persist.project.ProjectList;
import xyz.spiralhalo.sherlock.util.Debug;
import xyz.spiralhalo.sherlock.util.PathUtil;

import java.io.*;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Scanner;

public class AutoImporter {
    public static final String OLD_LOG_FILENAME = "record.txt";
    public static void importRecord(ProjectList projectList) {
        File file = new File(PathUtil.getSaveDir(), OLD_LOG_FILENAME);
        File file2 = new File(PathUtil.getSaveDir(), Tracker.LOG_FILENAME);
        if (file.exists() && !file2.exists()) {
            try (FileInputStream fis = new FileInputStream(file);
                 Scanner sc = new Scanner(fis)) {
                String[] temp;
                ZonedDateTime d;
                long x;
                final FlexibleBuffer y = new FlexibleBuffer();
                while (sc.hasNext()){
                    temp = sc.nextLine().split(Tracker.SPLIT_DIVIDER);
                    d = ZonedDateTime.parse(temp[0], Tracker.DTF);
                    x = projectList.getProjectOf(temp[2],d);
                    y.log(d,x, Integer.parseInt(temp[1]));
                }
                y.close();
            } catch (IOException | NumberFormatException e) {
                Debug.log(AutoImporter.class, e);
            }
        }
    }

    private static class FlexibleBuffer{
        private final int MARGIN_OF_ERROR = 15;
        private FileWriter fw;
        private BufferedWriter bw;
        private PrintWriter out;
        private long lastHash;
        private ZonedDateTime lastZDT;
        private ZonedDateTime prevZDT;
        private long accuS;

        public FlexibleBuffer() {
            try {
                fw = new FileWriter(Tracker.getRecordFile(), true);
                bw = new BufferedWriter(fw);
                out = new PrintWriter(bw);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public void log(ZonedDateTime time, long hash, int delayS){
            if(prevZDT!= null && (time.toEpochSecond()-prevZDT.toEpochSecond() > delayS + MARGIN_OF_ERROR
                    || hash!= lastHash) && lastHash !=0){
                flush(lastZDT);
                lastZDT = time;
            }
            if(lastZDT==null) lastZDT = time;
            prevZDT = time;
            lastHash = hash;
            accuS += delayS;
        }

        public void flush(ZonedDateTime time){
            StringBuilder buffer = new StringBuilder();
            buffer.append(Tracker.DTF.format(time)).append(Tracker.SPLIT_DIVIDER)
                    .append(accuS).append(Tracker.SPLIT_DIVIDER).append(lastHash);
            out.println(buffer.toString());
            accuS = 0;
        }

        public void close() throws IOException {
            out.flush();
            out.close();
            bw.close();
            fw.close();
        }
    }
}
