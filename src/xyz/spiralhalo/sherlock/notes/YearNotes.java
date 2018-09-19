package xyz.spiralhalo.sherlock.notes;

import xyz.spiralhalo.sherlock.Application;
import xyz.spiralhalo.sherlock.Debug;

import java.io.*;
import java.time.LocalDate;
import java.time.Year;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.WeakHashMap;

public class YearNotes implements Serializable {
    public static void setNote(ZoneId z, LocalDate date, String note){
        LocalDate utcDate;
        if(!z.equals(ZoneOffset.UTC)){
            utcDate = convertToUTC(z, date);
        } else {
            utcDate = date;
        }
        YearNotes notes = load(Year.from(utcDate));
        notes.set(z, date, note);
        save(notes);
    }

    public static String getNote(ZoneId z, LocalDate date){
        LocalDate utcDate;
        if(!z.equals(ZoneOffset.UTC)){
            utcDate = convertToUTC(z, date);
        } else {
            utcDate = date;
        }
        YearNotes notes = load(Year.from(utcDate));
        return notes.get(z, date);
    }

    public static void removeNote(ZoneId z, LocalDate date){
        LocalDate utcDate;
        if(!z.equals(ZoneOffset.UTC)){
            utcDate = convertToUTC(z, date);
        } else {
            utcDate = date;
        }
        YearNotes notes = load(Year.from(utcDate));
        notes.remove(z, date);
        save(notes);
    }

    private static final WeakHashMap<Year, YearNotes> cache = new WeakHashMap<>();

    private static File getNotesFile(Year year){
        return new File(Application.getSaveDir(), String.format("note_%s.dat", year.getValue()));
    }

    private static LocalDate convertToUTC(ZoneId z, LocalDate adaptToThis){
        return adaptToThis.atTime(12, 0).atZone(z).withZoneSameInstant(ZoneOffset.UTC).toLocalDate();
    }

    private static YearNotes load(Year year){
        YearNotes yn = cache.get(year);
        if(yn != null)return yn;
        File noteFile = getNotesFile(year);
        try(ObjectInputStream ois = new ObjectInputStream(new FileInputStream(noteFile))){
            yn = (YearNotes)ois.readObject();
        } catch (ClassCastException | ClassNotFoundException | IOException e) {
            yn = new YearNotes(year);
            Debug.log(e);
        }
        cache.put(year, yn);
        return yn;
    }

    private static void save(YearNotes yearNotes){
        Year year = yearNotes.year;
        File noteFile = getNotesFile(year);
        try(ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(noteFile))){
            oos.writeObject(yearNotes);
            cache.put(year, yearNotes);
        } catch (IOException e) {
            Debug.log(e);
        }
    }

    public static final long serialVersionUID = 1L;
    private final Year year;
    private HashMap<LocalDate, String> notes;

    public YearNotes(Year year) {
        this.year = year;
        this.notes = new HashMap<>();
    }

//    private static String merge(String note1, String note2){
//        return String.format("%s\n%s", note1, note2);
//    }

//    private void reassignTimeZone(ZoneId newZ) {
//        if(newZ.equals(zoneId)) return;
//        HashMap<LocalDate, String> newNotes = new HashMap<>();
//        for (LocalDate date:notes.keySet()) {
//            LocalDate newDate = date.atTime(12,0).atZone(zoneId).withZoneSameInstant(newZ).toLocalDate();
//            if(newNotes.containsKey(newDate)){
//                newNotes.put(newDate, merge(newNotes.get(newDate), notes.get(date)));
//            } else {
//                newNotes.put(newDate, notes.get(date));
//            }
//        }
//        zoneId = newZ;
//        notes = newNotes;
//    }

    private void set(ZoneId z, LocalDate date, String note){
        if(!z.equals(ZoneOffset.UTC)){
            notes.put(convertToUTC(z, date), note);
        } else {
            notes.put(date, note);
        }
    }

    private String get(ZoneId z, LocalDate date){
        if(!z.equals(ZoneOffset.UTC)){
            return notes.get(convertToUTC(z, date));
        } else {
            return notes.get(date);
        }
    }

    private void remove(ZoneId z, LocalDate date){
        if(!z.equals(ZoneOffset.UTC)){
            notes.remove(convertToUTC(z, date));
        } else {
            notes.remove(date);
        }
    }
}
