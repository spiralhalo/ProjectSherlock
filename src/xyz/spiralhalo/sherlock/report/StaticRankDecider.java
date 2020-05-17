package xyz.spiralhalo.sherlock.report;

import java.awt.*;

import static xyz.spiralhalo.sherlock.util.ColorUtil.*;
public class StaticRankDecider {
    public enum Rank{
        BREAK_DAY (0, 00, "Break Day", light_gray),
        LIGHT_WORK(1, 20, "Light Work", lite),
        WELL_DONE (2, 40, "Well Done", med),
        EXCELLENT (3, 90, "Excellent", excel);
        public final int index;
        public final int score;
        public final String label;
        public final Color color;
        Rank(int index, int score, String label, Color color){
            this.index = index;
            this.score = score;
            this.label = label;
            this.color = color;
        }
    }
    
    public static Rank decide(int score){
        Rank rankz = Rank.BREAK_DAY;
        for (Rank rank:Rank.values()) {
            if(score >= rank.score){
                rankz = rank;
            } else break;
        }
        return rankz;
    }
}
