package xyz.spiralhalo.sherlock;

import xyz.spiralhalo.sherlock.async.AsyncTask;
import xyz.spiralhalo.sherlock.bookmark.AutoBookmarker;
import xyz.spiralhalo.sherlock.bookmark.BookmarkMgr;
import xyz.spiralhalo.sherlock.persist.cache.CacheMgr;
import xyz.spiralhalo.sherlock.persist.project.ProjectList;
import xyz.spiralhalo.sherlock.report.factory.ReportOnRefresh;

import java.time.*;

// Async task for refreshing the charts + project table (AllReportRows). Garbage collected when the task ends.
// Actual chart creation happens in SummaryBuilder and ChartBuilder classes.
public class Refresher extends AsyncTask<Boolean> {

    private final CacheMgr cache;
    private final ProjectList projectList;
    private final boolean forceReconstruct;
    private final boolean forceDelete;
    private final ZoneId z;
    private final BookmarkMgr bookmarkMgr;
    private Boolean result = false;

    public Refresher(CacheMgr cache, ProjectList projectList, BookmarkMgr bookmarkMgr) {
        this(cache, projectList, bookmarkMgr, false, false);
    }

    public Refresher(CacheMgr cache, ProjectList projectList, BookmarkMgr bookmarkMgr, boolean forceReconstruct, boolean deleteUnused) {
        this.cache = cache;
        this.projectList = projectList;
        this.bookmarkMgr = bookmarkMgr;
        this.forceReconstruct = forceReconstruct;
        this.forceDelete = deleteUnused;
        this.z = ZoneId.systemDefault();
    }

    @Override
    protected void doRun() {
        result = ReportOnRefresh.refreshReports(forceReconstruct, forceDelete, cache, z, projectList);
        result = result && AutoBookmarker.scanOnRefresh(projectList.getActiveProjects(), bookmarkMgr) ;
    }

    @Override
    protected Boolean getResult() {
        return result;
    }
}

