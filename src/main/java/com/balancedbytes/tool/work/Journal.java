package com.balancedbytes.tool.work;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Journal {

    private static final String HEADER = "Date,StartTime,StopTime,Task,Minutes";

    private final List<JournalEntry> entries;

    public Journal() {
        this.entries = new ArrayList<>();
    }

    public void add(JournalEntry entry) {
        this.entries.add(entry);
    }

    private void sort() {
        this.entries.sort((o1, o2) -> o2.getStartDate().compareTo(o1.getStartDate()));
    }

    public void writeTo(Writer out) throws IOException {
        BufferedWriter bufferedOut = new BufferedWriter(out);
        bufferedOut.write(HEADER);
        bufferedOut.newLine();
        for (JournalEntry entry : getEntries()) {
            bufferedOut.write(entry.toCsv());
            bufferedOut.newLine();
        }
        bufferedOut.flush();
    }

    public List<JournalEntry> getEntries() {
        sort();
        return this.entries;
    }

    public List<JournalEntry> getTodaysEntries() {
        List<JournalEntry> result = new ArrayList<>();
        for (JournalEntry entry : getEntries()) {
            if (entry.hasStartedToday()) {
                result.add(entry);
            }
        }
        return result;
    }

    public void readFrom(Reader in) throws IOException {
        String line;
        boolean skipHeader = true;
        clear();
        BufferedReader bufferedIn = new BufferedReader(in);
        while ((line = bufferedIn.readLine()) != null) {
            if (skipHeader) {
                skipHeader = false;
            } else {
                JournalEntry entry = JournalEntry.fromCsv(line);
                if (entry != null) {
                    add(entry);
                }
            }
        }
    }

    public void clear() {
        this.entries.clear();
    }

}
