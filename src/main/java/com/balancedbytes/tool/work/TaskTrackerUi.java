package com.balancedbytes.tool.work;

import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class TaskTrackerUi extends JFrame {

    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("EEE dd.MM.yyyy");

    private JLabel timeLabel;
    private JLabel dateLabel;
    private DefaultListModel<String> listModel;
    private JButton startButton;
    private JComboBox<String> taskComboBox;
    private JournalEntry currentEntry;
    private final TaskTracker taskTracker;
    private boolean showSummary;
    private JRadioButtonMenuItem menuItemJournal;
    private JRadioButtonMenuItem menuItemSummary;

    public TaskTrackerUi(TaskTracker taskTracker) {

        super("TaskTracker");

        this.taskTracker = taskTracker;

        setSize(this.taskTracker.getFrameSizeX(), this.taskTracker.getFrameSizeY());
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setResizable(false);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                stopTracking(true);
            }
        });

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(createTimerPanel());
        panel.add(createTaskPanel());
        panel.add(createJournalPanel());
        add(panel);

        setJMenuBar(createMenuBar());

        updateTimeAndDate();

        // use a timer to update the time and date labels every second
        Timer timer = new Timer(1000, e -> updateTimeAndDate());
        timer.start();

    }

    private JPanel createTimerPanel() {

        // create a panel to hold the time and date labels
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setLayout(new BorderLayout());
        panel.setBackground(Color.BLACK);

        // create a label to display the time
        timeLabel = new JLabel();
        timeLabel.setFont(new Font("Arial", Font.PLAIN, 60));
        timeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        timeLabel.setVerticalAlignment(SwingConstants.CENTER);
        timeLabel.setForeground(Color.RED);
        panel.add(timeLabel, BorderLayout.CENTER);

        // create a label to display the date
        dateLabel = new JLabel();
        dateLabel.setFont(new Font("Arial", Font.PLAIN, 20));
        dateLabel.setHorizontalAlignment(SwingConstants.CENTER);
        dateLabel.setVerticalAlignment(SwingConstants.CENTER);
        dateLabel.setForeground(Color.WHITE);
        panel.add(dateLabel, BorderLayout.SOUTH);

        return panel;

    }

    private JPanel createTaskPanel() {

        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

        this.startButton = new JButton("Start");
        this.startButton.addActionListener(e -> {
            if (this.currentEntry == null) {
                startTracking();
            } else {
                stopTracking(false);
            }
        });
        panel.add(startButton);

        panel.add(Box.createHorizontalStrut(5));

        String[] tasks = taskTracker.getTaskList().toArray(new String[0]);
        taskComboBox = new JComboBox<>(tasks);
        taskComboBox.addActionListener(e -> {
            if (taskComboBox.getSelectedIndex() >= 0) {
                stopTracking(false);
            }
        });
        panel.add(taskComboBox);

        return panel;

    }

    private JPanel createJournalPanel() {

        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
        panel.setLayout(new BorderLayout());

        listModel = new DefaultListModel<>();
        updateListModel();
        JList<String> journalList = new JList<>(listModel);
        JScrollPane scrollPane = new JScrollPane(journalList, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;

    }

    private void updateListModel() {
        this.listModel.clear();
        java.util.List<JournalEntry> todaysEntries = this.taskTracker.getJournal().getTodaysEntries();
        if (this.showSummary) {
            Map<String, Integer> minutesPerTask = new HashMap<>();
            for (JournalEntry entry : todaysEntries) {
                if (minutesPerTask.containsKey(entry.getTask())) {
                    minutesPerTask.put(entry.getTask(), minutesPerTask.get(entry.getTask()) + entry.getMinutesTotal());
                } else {
                    minutesPerTask.put(entry.getTask(), entry.getMinutesTotal());
                }
            }
            List<String> tasks = new ArrayList<>(minutesPerTask.keySet());
            Collections.sort(tasks);
            for (String task : tasks) {
                String element = "<html>" + Util.getFixedTimeFormat(minutesPerTask.get(task)) + " <b>" + task + "</b></html>";
                this.listModel.addElement(element);
            }
        } else {
            for (JournalEntry entry : todaysEntries) {
                this.listModel.addElement(entry.toListEntry());
            }
        }
    }

    private void updateTimeAndDate() {
        Date time = Calendar.getInstance().getTime();
        timeLabel.setText(TIME_FORMAT.format(time));
        dateLabel.setText(DATE_FORMAT.format(time));
    }

    private String getCurrentTask() {
        int selectedIndex = this.taskComboBox.getSelectedIndex();
        java.util.List<String> taskList = this.taskTracker.getTaskList();
        if ((selectedIndex < 0) || (selectedIndex > taskList.size())) {
            return null;
        }
        return taskList.get(selectedIndex);
    }

    private void startTracking() {
        String task = getCurrentTask();
        if (task != null) {
            this.startButton.setText("Stop");
            Date startDate = Calendar.getInstance().getTime();
            this.currentEntry = new JournalEntry(startDate, task);
            this.listModel.add(0, this.currentEntry.toListEntry());
            this.menuItemJournal.setEnabled(false);
            this.menuItemSummary.setEnabled(false);
        }
    }

    private void stopTracking(boolean exit) {
        if (this.currentEntry != null) {
            this.startButton.setText("Start");
            Date stopDate = Calendar.getInstance().getTime();
            this.currentEntry.setStopDate(stopDate);
            this.taskTracker.getJournal().add(this.currentEntry);
            updateListModel();
            this.currentEntry = null;
            this.menuItemJournal.setEnabled(true);
            this.menuItemSummary.setEnabled(true);
            save(exit);
        } else {
            if (exit) {
                System.exit(0);
            }
        }
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu menuSettings = new JMenu("Settings");
        menuBar.add(menuSettings);
        ButtonGroup group = new ButtonGroup();
        this.menuItemJournal = new JRadioButtonMenuItem("Show Journal");
        this.menuItemJournal.setSelected(true);
        this.menuItemJournal.addActionListener(e -> {
            this.showSummary = false;
            updateListModel();
        });
        group.add(this.menuItemJournal);
        menuSettings.add(this.menuItemJournal);
        this.menuItemSummary = new JRadioButtonMenuItem("Show Summary");
        this.menuItemSummary.addActionListener(e -> {
            this.showSummary = true;
            updateListModel();
        });
        group.add(this.menuItemSummary);
        menuSettings.add(this.menuItemSummary);
        return menuBar;
    }

    private void save(boolean exit) {

        // The worker thread that takes Integer,Void
        // The first type (here Integer) is the return type of the doInBackground() method.
        // The next type (here Void) is the intermediate result type,
        // if there is no intermediate result, then it is set to Void type.

        // The return type of the get() method is the return type of the doInBackground() method

        SwingWorker<Integer, Void> worker = new SwingWorker<>() {

            // The Integer returned here is either -1 or 0 representing unsuccessful
            // and successful save respectively.
            protected Integer doInBackground() {
                startButton.setEnabled(false);
                File journalFile = taskTracker.getJournalFile();
                if (journalFile == null) {
                    return -1;
                }
                try (FileWriter out = new FileWriter(journalFile)) {
                    taskTracker.getJournal().writeTo(out);
                } catch (IOException ioe) {
                    ioe.printStackTrace(System.err);
                    return -1;
                }
                return 0;
            }

            // This method is executed once after, the doInBackground() method is executed
            // done() runs in the EDT
            protected void done() {
                startButton.setEnabled(true);
                if (exit) {
                    System.exit(0);
                }
            }

        };

        // Start the worker thread
        worker.execute();

    }

}