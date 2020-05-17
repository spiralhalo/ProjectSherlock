package xyz.spiralhalo.sherlock.report.factory.charts;

import org.jfree.data.category.DefaultCategoryDataset;
import xyz.spiralhalo.sherlock.persist.project.Project;
import xyz.spiralhalo.sherlock.persist.project.ProjectList;
import xyz.spiralhalo.sherlock.persist.settings.UserConfig;
import xyz.spiralhalo.sherlock.report.StaticRankDecider;
import xyz.spiralhalo.sherlock.util.ColorUtil;

import java.time.YearMonth;
import java.time.ZoneId;
import java.time.temporal.Temporal;

//special ChartBuilder for making monthly rank charts
public class RankChartBuilder extends ChartBuilder<YearMonth> {

    public RankChartBuilder(YearMonth date, ZoneId z, boolean inclTotal) {
        super(date, z, inclTotal);
    }

    @Override
    public ChartData finish(ProjectList projectList) {
        final DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        final ChartMeta meta = new ChartMeta();
        final String UNPROD = "Unrated";
        meta.put(UNPROD, ColorUtil.CONST_OTHER_GRAY);
        for (StaticRankDecider.Rank rank: StaticRankDecider.Rank.values()) {
            meta.put(rank.label, rank.color);
            dataset.setValue((Number) 0, rank.label, type.unitLabel(date, 0));
        }
        for (int i = 0; i < type.numUnits(date); i++) {
            FlexibleLocale unitLabel = type.unitLabel(date, i);
            int totalProds = 0;
            int totalUnprods = 0;
            if (units[i] != null) {
                for (Long l : units[i].keySet()) {
                    Project p = projectList.findByHash(l);
                    boolean productive = false;
                    boolean recreational = false;
                    int s = units[i].get(l);
                    //two task: figure out if productive and if recreational
                    if ((p != null && p.isProductive()) || (boolean)productiveMap.getOrDefault(l, false)) {
                        productive = true;
                        totalProds += s;
                    } else {
                        if (p != null && p.isRecreational()){
                            recreational = true;
                        }
                        totalUnprods += s;
                    }

                    meta.addWorkDur(productive ? s : 0);
                    meta.addBreakDur(recreational ? s : 0);
                    meta.addLogDur(s);
                }
            }
            int score = (100 * totalProds) / UserConfig.userGInt(UserConfig.UserNode.GENERAL, UserConfig.UserInt.DAILY_TARGET_SECOND);
            StaticRankDecider.Rank rank = StaticRankDecider.decide(score);
            dataset.setValue((Number) (totalProds / type.subunitNormalizer()), rank.label, unitLabel);
            dataset.setValue((Number) (totalUnprods / type.subunitNormalizer()), UNPROD, unitLabel);
        }
        return new ChartData(meta, dataset);
    }
}
