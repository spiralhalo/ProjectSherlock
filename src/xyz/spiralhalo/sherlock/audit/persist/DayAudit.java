package xyz.spiralhalo.sherlock.audit.persist;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class DayAudit implements Serializable {
    public static final long serialVersionUID = 1L;

    private HashMap<Long, ArrayList<AuditEntry>> projectAudits;

    public DayAudit()
    {
        projectAudits = new HashMap<>();
    }

    public void addProjectAudit(long projectId, AuditEntry entry)
    {
        if(!projectAudits.containsKey(projectId)) projectAudits.put(projectId, new ArrayList<>());

        if(!projectAudits.get(projectId).contains(entry)) projectAudits.get(projectId).add(entry);
    }

    public void removeProjectAudit(long projectId, AuditEntry entry)
    {
        if(!projectAudits.containsKey(projectId)) return;

        if(projectAudits.get(projectId).contains(entry)) projectAudits.get(projectId).remove(entry);
    }

    public Long[] getProjects()
    {
        return projectAudits.keySet().toArray(new Long[]{});
    }

    public AuditEntry[] getProjectAudits(long projectId)
    {
        return projectAudits.get(projectId).toArray(new AuditEntry[]{});
    }
}
