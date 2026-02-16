package com.balancedbytes.tool.work;

import org.apache.commons.io.file.PathUtils;

import javax.swing.*;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

public class TaskTracker {

    private static final String CONFIG_PATH = "/TaskTracker.properties";

    private final Properties config;
    private final Journal journal;
    private final List<String> taskList;

    public TaskTracker() {
        this.config = new Properties();
        this.journal = new Journal();
        this.taskList = new ArrayList<>();
    }

    private boolean init() {
        try (InputStream in = TaskTracker.class.getResourceAsStream(CONFIG_PATH)) {
            this.config.load(in);
        } catch (IOException ioe) {
            ioe.printStackTrace(System.err);
            return false;
        }
        File journalFile = getJournalFile();
        if (journalFile != null) {
            try (FileReader in = new FileReader(journalFile)) {
                this.journal.readFrom(in);
            } catch (IOException ioe) {
                ioe.printStackTrace(System.err);
                return false;
            }
        }
        File tasksFile = getTasksFile();
        if (tasksFile != null) {
            try (BufferedReader in = new BufferedReader(new FileReader(tasksFile))) {
                String line;
                while ((line = in.readLine()) != null) {
                    String task = line.trim();
                    if (!task.isEmpty()) {
                        this.taskList.add(task);
                    }
                }
                Collections.sort(this.taskList);
            } catch (IOException ioe) {
                ioe.printStackTrace(System.err);
                return false;
            }
        }
        return true;
    }

    protected int getFrameSizeX() {
        return Integer.parseInt(config.getProperty("frame.size.x", "400"));
    }

    protected int getFrameSizeY() {
        return Integer.parseInt(config.getProperty("frame.size.y", "400"));
    }

    protected File getJournalFile() {
        return touchFile(System.getProperty("user.home") + File.separator + config.getProperty("journal.file"));
    }

    private File getTasksFile() {
        return touchFile(System.getProperty("user.home") + File.separator + config.getProperty("tasks.file"));
    }

    private File touchFile(String absolutePath) {
        Path path = Paths.get(absolutePath);
        try {
            PathUtils.touch(path);
        } catch (IOException ioe) {
            return null;
        }
        File journalFile = path.toFile();
        if (journalFile.isDirectory()) {
            return null;
        }
        return journalFile;

    }
    protected List<String> getTaskList() {
        return this.taskList;
    }

    protected Journal getJournal() {
        return this.journal;
    }

    /**
     * Create the GUI and show it.
     * For thread safety, this method should be invoked from the event-dispatching thread.
     */
    private void createAndShowUi() {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
            System.err.println("Can't change Look&Feel: " + e);
        }
        // make sure we have nice window decorations.
        JFrame.setDefaultLookAndFeelDecorated(true);
        TaskTrackerUi ui = new TaskTrackerUi(this);
        ui.setVisible(true);
    }

    public static void main(String[] args) {
        TaskTracker taskTracker = new TaskTracker();
        if (taskTracker.init()) {
            // schedule a job for the event-dispatching thread:
            // creating and showing this application's GUI.
            SwingUtilities.invokeLater(taskTracker::createAndShowUi);
        }
    }

}