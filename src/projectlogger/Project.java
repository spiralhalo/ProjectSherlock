package projectlogger;

import java.io.Serializable;
import java.time.ZonedDateTime;

@Deprecated
public class Project implements Serializable {
    public static final long serialVersionUID = 3L;

    private String tag;
    private String supertag;
    private String category;
    private ZonedDateTime startDate;
    private ZonedDateTime finishedDate;
    private boolean isFinished;

    public String getTag() {
        return tag;
    }

    public String getSupertag() {
        return supertag;
    }

    public String getCategory() {
        return category;
    }

    public ZonedDateTime getStartDate() {
        return startDate;
    }

    public ZonedDateTime getFinishedDate() {
        return finishedDate;
    }

    public boolean isFinished() {
        return isFinished;
    }
}
