package xyz.spiralhalo.sherlock.audit;

import xyz.spiralhalo.sherlock.Application;
import xyz.spiralhalo.sherlock.Debug;
import xyz.spiralhalo.sherlock.audit.persist.AuditList;
import xyz.spiralhalo.sherlock.audit.persist.CategoryMilestoneList;
import xyz.spiralhalo.sherlock.audit.persist.DayAudit;

import java.io.*;
import java.lang.ref.WeakReference;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;

public class AuditListMgr {

    public static boolean exists(ZoneId z, LocalDate date)
    {
        LocalDate d = convertToUTC(z, date);
        AuditList list = getList();
        return list.containsKey(d);
    }

    public static DayAudit getDayAudit(ZoneId z, LocalDate date)
    {
        LocalDate d = convertToUTC(z, date);
        AuditList list = getList();
        return list.get(d);
    }

    public static void setDayAuditAndSave(ZoneId z, LocalDate date, DayAudit dayAudit)
    {
        LocalDate d = convertToUTC(z, date);
        AuditList list = getList();
        list.put(d, dayAudit);
        saveList(list);
    }

    public ArrayList getCategoryMilestoneList(String category) {
        HashMap<String, Object> extras = getList().getExtras();
        if(extras == null || !extras.containsKey("CategoryMilestones")
                || !(extras.get("CategoryMilestones") instanceof CategoryMilestoneList)){
            return null;
        }
        CategoryMilestoneList categoryMilestoneList = (CategoryMilestoneList) extras.get("CategoryMilestones");
        return categoryMilestoneList.get(category);
    }

    private static WeakReference<AuditList> cache;

    private static void saveList(AuditList auditList){
        try(ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(getAuditsFile()))){
            oos.writeObject(auditList);
            cache = new WeakReference<>(auditList);
        } catch (IOException e) {
            Debug.log(e);
        }
    }

    private static AuditList getList()
    {
        if (cache != null && cache.get() != null) return cache.get();
        AuditList yn;
        try(ObjectInputStream ois = new ObjectInputStream(new FileInputStream(getAuditsFile()))){
            yn = (AuditList)ois.readObject();
        } catch (ClassCastException | ClassNotFoundException | IOException e) {
            yn = new AuditList();
            Debug.log(e);
        }
        cache = new WeakReference<>(yn);
        return yn;
    }

    private static LocalDate convertToUTC(ZoneId z, LocalDate adaptToThis){
        if(z.equals(ZoneOffset.UTC)) return adaptToThis;
        return adaptToThis.atTime(12, 0).atZone(z).withZoneSameInstant(ZoneOffset.UTC).toLocalDate();
    }

    private static File getAuditsFile(){
        return new File(Application.getSaveDir(), "audit.dat");
    }
}
