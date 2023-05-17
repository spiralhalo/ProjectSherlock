package projectlogger.persist;

import java.io.Serializable;
import java.time.ZonedDateTime;

public class Project implements Serializable {
	public static final long serialVersionUID = 2L;

	private long hash;
	private int color;
	private String tag;
	private String[] tags;
	private String group;
	private String category;
	private ZonedDateTime startDate;
	private ZonedDateTime finishedDate;
	private boolean isFinished;
	private float pay;

	public long getHash() {
		return hash;
	}

	public String getTag() {
		return tag;
	}

	public String[] getTags() {
		return tags;
	}

	public String getGroup() {
		return group;
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

	public float getPay() {
		return pay;
	}
}
