package com.balancedbytes.tool.work;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class JournalEntry {

    private static final String COMMA = ",";
    private static final String BLANK = " ";
    private static final String MINUS = "-";
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat DATETIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private Date startDate;
    private Date stopDate;
    private String task;

    public JournalEntry(Date startDate, String task) {
        setStartDate(startDate);
        setTask(task);
    }

    public Date getStartDate() {
        return this.startDate;
    }

    private void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getStopDate() {
        return this.stopDate;
    }

    public void setStopDate(Date stopDate) {
        this.stopDate = stopDate;
    }

    public String getTask() {
        return this.task;
    }

    private void setTask(String task) {
        this.task = task;
    }

    public int getMinutesTotal() {
        if ((this.startDate == null) || (this.stopDate == null)) {
            return 0;
        }
        long timeInMs = this.stopDate.getTime() - this.startDate.getTime();
        if (timeInMs <= 0) {
            return 0;
        }
        return (int) Math.floorDiv(timeInMs, 60000);
    }

    public boolean hasStartedToday() {
        String today = DATE_FORMAT.format(Calendar.getInstance().getTime());
        String started = DATE_FORMAT.format(getStartDate());
        return started.equals(today);
    }

    public String toListEntry() {
        String listEntry = "<html>";
        if (this.stopDate == null) {
            listEntry += TIME_FORMAT.format(this.startDate) + " <b>" + this.task + "</b>";
        } else {
            listEntry += TIME_FORMAT.format(this.startDate) + MINUS + TIME_FORMAT.format(this.stopDate)
                + " <b>" + this.task + "</b>" + " (" + Util.getVariableTimeFormat(getMinutesTotal()) + ")";
        }
        listEntry += "</html>";
        return listEntry;
    }

    public String toCsv() {
        String result = "";
        result += DATE_FORMAT.format(getStartDate());
        result += COMMA;
        result += TIME_FORMAT.format(getStartDate());
        result += COMMA;
        if (stopDate != null) {
            result += TIME_FORMAT.format(getStopDate());
        }
        result += COMMA;
        result += getTask();
        result += COMMA;
        result += getMinutesTotal();
        return result;
    }

    public static JournalEntry fromCsv(String csv) {
        String[] columns = csv.split(COMMA);
        if (columns.length < 4) {
            return null;
        }
        try {
            Date startDate = DATETIME_FORMAT.parse(columns[0] + BLANK + columns[1]);
            String task = columns[3];
            JournalEntry entry = new JournalEntry(startDate, task);
            Date stopDate = DATETIME_FORMAT.parse(columns[0] + BLANK + columns[2]);
            entry.setStopDate(stopDate);
            return entry;
        } catch (ParseException pe) {
            return null;
        }
    }

}
