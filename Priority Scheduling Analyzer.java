package main;
import javax.swing.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.*;

public class Main extends JFrame {

    private JTable table;
    private DefaultTableModel model;
    private JTextField processField;
    private JButton submitButton, calcButton;
    private JButton compareButton;
    private JPanel inputPanel, tablePanel, ganttPanel;
    private JScrollPane ganttScrollPane; // Declared as a member variable
    private ArrayList<Process> processes;

    // Updated color scheme and styling constants
    private static final Color DARK_BACKGROUND = new Color(18, 18, 24);
    private static final Color MEDIUM_BACKGROUND = new Color(30, 35, 40);
    private static final Color ACCENT_COLOR = new Color(0, 150, 136); // Teal
    private static final Color LIGHT_ACCENT = new Color(77, 208, 225);
    private static final Color TEXT_COLOR = new Color(240, 240, 240); // This is our "white" for text and lines
    private static final Color GANTT_BACKGROUND = new Color(25, 25, 30);
    private static final Color IDLE_COLOR = new Color(80, 80, 80);
    private static final Color TABLE_HEADER = new Color(0, 120, 110);
    private static final Color TABLE_ROW_EVEN = new Color(40, 45, 50);
    private static final Color TABLE_ROW_ODD = new Color(50, 55, 60);
    private static final Color HIGHLIGHT_COLOR = new Color(0, 180, 170);


    public Main() {
        setTitle("Priority Scheduling Analyzer");
        setSize(1000, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        setUIFont(new Font("Segoe UI", Font.PLAIN, 14));
        processes = new ArrayList<>();
        setupUI();
    }

    // Update the setupUI() method with professional styling
    private void setupUI() {
        getContentPane().setBackground(DARK_BACKGROUND);

        // Top Input Panel with improved layout
        inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        inputPanel.setBackground(DARK_BACKGROUND);
        inputPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, ACCENT_COLOR),
                new EmptyBorder(15, 15, 15, 15)
        ));

        JLabel label = new JLabel("Enter Number of Processes:");
        label.setForeground(TEXT_COLOR);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        inputPanel.add(label);

        processField = new JTextField(5);
        processField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        processField.setBackground(MEDIUM_BACKGROUND);
        processField.setForeground(TEXT_COLOR);
        processField.setCaretColor(TEXT_COLOR); // Set cursor color to white
        processField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ACCENT_COLOR, 1),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        inputPanel.add(processField);

        submitButton = createStyledButton(" Create Table");
        calcButton = createStyledButton("Run Scheduling");
        compareButton = createStyledButton("Compare");
        calcButton.setEnabled(false);

        // Add subtle spacing between buttons
        inputPanel.add(Box.createHorizontalStrut(10));
        inputPanel.add(submitButton);
        inputPanel.add(Box.createHorizontalStrut(5));
        inputPanel.add(calcButton);
        inputPanel.add(Box.createHorizontalStrut(5));
        inputPanel.add(compareButton);

        add(inputPanel, BorderLayout.NORTH);

        // Center Table Panel with improved styling
        tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(MEDIUM_BACKGROUND);
        tablePanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        add(tablePanel, BorderLayout.CENTER);

        // Enhanced Gantt Chart Panel
        ganttPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawGanttChart(g);
            }
        };
        ganttPanel.setBackground(GANTT_BACKGROUND);
        ganttPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEmptyBorder(10, 15, 10, 15),
                "GANTT CHART",
                javax.swing.border.TitledBorder.LEFT,
                javax.swing.border.TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 12),
                ACCENT_COLOR
        ));

        // Assign to the member variable, no local declaration
        ganttScrollPane = new JScrollPane(ganttPanel);
        ganttScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        ganttScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        ganttScrollPane.getHorizontalScrollBar().setUnitIncrement(16);
        ganttScrollPane.setBorder(BorderFactory.createEmptyBorder());
        add(ganttScrollPane, BorderLayout.SOUTH);

        submitButton.addActionListener(e -> createProcessTable());
        calcButton.addActionListener(e -> runScheduling());
        compareButton.addActionListener(e -> showComparisonDialog());
    }

    // Enhanced button styling with hover effects
    private JButton createStyledButton(String text) {
        JButton btn = new JButton(text);
        btn.setFocusPainted(false);
        btn.setBackground(ACCENT_COLOR);
        btn.setForeground(TEXT_COLOR);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LIGHT_ACCENT, 1),
                BorderFactory.createEmptyBorder(8, 16, 8, 16)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Add hover effects
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(HIGHLIGHT_COLOR);
                btn.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(LIGHT_ACCENT.darker(), 1),
                        BorderFactory.createEmptyBorder(8, 16, 8, 16)
                ));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(ACCENT_COLOR);
                btn.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(LIGHT_ACCENT, 1),
                        BorderFactory.createEmptyBorder(8, 16, 8, 16)
                ));
            }
        });

        return btn;
    }

    private void createProcessTable() {
        int num;
        try {
            num = Integer.parseInt(processField.getText());
            if (num <= 0) {
                JOptionPane.showMessageDialog(this, "Please enter a positive number of processes.");
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid number.");
            return;
        }

        String[] columns = {"PID", "Arrival Time", "Burst Time", "Priority"};
        model = new DefaultTableModel(columns, num) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column > 0; // Make PID column non-editable
            }
        };
        table = new JTable(model);
        styleTable(table);

        // Initialize PID column
        for (int i = 0; i < num; i++) {
            model.setValueAt("P" + (i + 1), i, 0);
        }

        tablePanel.removeAll();
        tablePanel.add(new JScrollPane(table), BorderLayout.CENTER);
        tablePanel.revalidate();
        tablePanel.repaint();
        calcButton.setEnabled(true);
    }

    private void runScheduling() {
        processes.clear();
        for (int i = 0; i < model.getRowCount(); i++) {
            try {
                int at = Integer.parseInt(model.getValueAt(i, 1).toString());
                int bt = Integer.parseInt(model.getValueAt(i, 2).toString());
                int pr = Integer.parseInt(model.getValueAt(i, 3).toString());

                if (at < 0 || bt <= 0 || pr < 0) {
                    JOptionPane.showMessageDialog(this, "Invalid input at row " + (i + 1) + ". Arrival Time must be >= 0, Burst Time > 0, Priority >= 0.");
                    return;
                }
                processes.add(new Process(i + 1, at, bt, pr));
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Invalid input (non-numeric) at row " + (i + 1) + ". Please enter valid numbers.");
                return;
            } catch (NullPointerException e) {
                JOptionPane.showMessageDialog(this, "Missing input at row " + (i + 1) + ". Please fill all fields.");
                return;
            }
        }


        String[] options = {"Priority Preemptive", "Priority Non-Preemptive", "Dynamic Priority Boost (My Algorithm)"};
        int choice = JOptionPane.showOptionDialog(this,
                "Choose scheduling algorithm:",
                "Algorithm Selection",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[2]);

        // Set algorithm name based on selection
        String algorithmName;
        switch (choice) {
            case 0:
                algorithmName = "Priority Preemptive Scheduling";
                schedulePriorityPreemptive();
                break;
            case 1:
                algorithmName = "Priority Non-Preemptive Scheduling";
                schedulePriorityNonPreemptive();
                break;
            case 2:
            default:
                algorithmName = "Dynamic Priority Boost Scheduling";
                scheduleDynamicPriorityBoost();
                break;
        }

        updateTable();
        ganttPanel.repaint();

        // Show completion dialog
        showAlgorithmCompletionDialog(algorithmName);


        // Reset dynamic priority values and other process states for a fresh run
        for (Process p : processes) {
            p.calculatedDynamicPriority = p.priority;
            p.remainingTime = p.burstTime;
            p.completionTime = -1;
            p.waitingTime = 0;
            p.turnaroundTime = 0;
            p.firstStartTime = -1;
            p.preemptionMoments.clear();
            p.restartMoments.clear();
            p.timeLastEnteredReadyQueue = p.arrivalTime;
        }


        switch (choice) {
            case 0:
                schedulePriorityPreemptive();
                break;
            case 1:
                schedulePriorityNonPreemptive();
                break;
            case 2:
            default: // Default to Dynamic if dialog is closed or other option is chosen
                scheduleDynamicPriorityBoost();
                break;
        }

        updateTable();
        ganttPanel.repaint();
    }

    private void showAlgorithmCompletionDialog(String algorithmName) {
        JDialog completionDialog = new JDialog(this, "Algorithm Completed", true);
        completionDialog.setSize(350, 200);
        completionDialog.setLocationRelativeTo(this);
        completionDialog.getContentPane().setBackground(DARK_BACKGROUND);

        // Create main panel with GridBagLayout for better centering
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(MEDIUM_BACKGROUND);
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Create constraints for centering
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;

        // Create message label with improved alignment
        JLabel messageLabel = new JLabel("<html><div style='text-align: center;'>" +
                algorithmName +
                "<br>completed successfully!</div></html>");
        messageLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        messageLabel.setForeground(TEXT_COLOR);
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Add message label with constraints
        mainPanel.add(messageLabel, gbc);

        // Create OK button
        JButton okButton = createStyledButton("OK");
        okButton.addActionListener(e -> completionDialog.dispose());

        // Adjust constraints for button
        gbc.weighty = 0;
        gbc.insets = new Insets(15, 0, 0, 0); // Add top margin

        mainPanel.add(okButton, gbc);

        completionDialog.add(mainPanel);
        completionDialog.setVisible(true);
    }

    private void schedulePriorityPreemptive() {
        schedulePriorityPreemptive(this.processes);
    }

    private void schedulePriorityPreemptive(ArrayList<Process> processes) {
        Process.lastAlgorithmUsed = 0;
        int time = 0, completed = 0;
        ArrayList<Integer> gantt = new ArrayList<>();
        int n = processes.size();

        // Reset process states for this run
        for (Process p : processes) {
            p.remainingTime = p.burstTime;
            p.completionTime = -1;
            p.waitingTime = 0;
            p.turnaroundTime = 0;
        }

        while (completed != n) {
            Process current = null;
            int minPriority = Integer.MAX_VALUE;
            // Find highest priority process that has arrived and is not completed
            for (Process p : processes) {
                if (p.arrivalTime <= time && p.remainingTime > 0 && p.priority < minPriority) {
                    minPriority = p.priority;
                    current = p;
                }
            }

            if (current == null) {
                gantt.add(-1); // IDLE
                time++;
                continue;
            }

            gantt.add(current.pid);
            current.remainingTime--;
            time++;

            if (current.remainingTime == 0) {
                current.completionTime = time;
                current.turnaroundTime = current.completionTime - current.arrivalTime;
                current.waitingTime = current.turnaroundTime - current.burstTime;
                completed++;
            }
        }

        Process.ganttChart = gantt;
    }

    private void schedulePriorityNonPreemptive() {
        schedulePriorityNonPreemptive(this.processes);
    }

    private void schedulePriorityNonPreemptive(ArrayList<Process> processes) {
        Process.lastAlgorithmUsed = 1;
        int time = 0;
        ArrayList<Integer> gantt = new ArrayList<>();
        ArrayList<Process> readyQueue = new ArrayList<>();

        // Reset process states for this run
        for (Process p : processes) {
            p.remainingTime = p.burstTime;
            p.completionTime = -1;
            p.waitingTime = 0;
            p.turnaroundTime = 0;
        }

        ArrayList<Process> tempProcesses = new ArrayList<>(processes); // Create a mutable copy

        while (!tempProcesses.isEmpty() || !readyQueue.isEmpty()) {
            // Add arrived processes to ready queue
            Iterator<Process> it = tempProcesses.iterator();
            while (it.hasNext()) {
                Process p = it.next();
                if (p.arrivalTime <= time) {
                    readyQueue.add(p);
                    it.remove(); // Remove from temporary list once added to ready queue
                }
            }

            if (readyQueue.isEmpty()) {
                gantt.add(-1); // IDLE
                time++;
                continue;
            }

            // Sort ready queue by priority (lower priority number is higher priority)
            // If priorities are same, sort by arrival time (FCFS)
            readyQueue.sort((p1, p2) -> {
                if (p1.priority != p2.priority) {
                    return Integer.compare(p1.priority, p2.priority);
                }
                return Integer.compare(p1.arrivalTime, p2.arrivalTime);
            });

            Process current = readyQueue.remove(0); // Get the highest priority process

            // Execute the process for its full burst time
            for (int i = 0; i < current.burstTime; i++) {
                gantt.add(current.pid);
                time++; // Increment time for each unit of execution
            }

            current.completionTime = time;
            current.turnaroundTime = current.completionTime - current.arrivalTime;
            current.waitingTime = current.turnaroundTime - current.burstTime;
            current.remainingTime = 0; // Mark as completed
        }

        Process.ganttChart = gantt;
    }


    private void scheduleDynamicPriorityBoost() {
        scheduleDynamicPriorityBoost(this.processes);
    }

    private void scheduleDynamicPriorityBoost(ArrayList<Process> processes) {
        Process.lastAlgorithmUsed = 2;
        int currentTime = 0;
        ArrayList<Process> readyQueue = new ArrayList<>();
        ArrayList<Process> completedProcesses = new ArrayList<>();
        Process currentlyRunningProcess = null;
        ArrayList<Integer> gantt = new ArrayList<>();

        // Weights for dynamic priority calculation (adjust as needed)
        final double burstWeight = 0.5; // Lower value means burst time has more negative impact (higher priority for shorter burst)
        final double waitWeight = 2.0; // Higher value means waiting time has more positive impact (higher priority for longer wait)

        // Reset process states for this run
        for (Process p : processes) {
            p.remainingTime = p.burstTime;
            p.firstStartTime = -1;
            p.preemptionMoments.clear();
            p.restartMoments.clear();
            p.completionTime = -1;
            p.waitingTime = 0;
            p.turnaroundTime = 0;
            p.timeLastEnteredReadyQueue = p.arrivalTime; // Initialize
            p.calculatedDynamicPriority = p.priority; // Initialize
        }

        // Loop until all processes are completed
        while (completedProcesses.size() < processes.size()) {
            // Add newly arrived processes to the ready queue
            for (Process p : processes) {
                if (p.arrivalTime <= currentTime && p.completionTime == -1 && !readyQueue.contains(p)) {
                    readyQueue.add(p);
                    p.timeLastEnteredReadyQueue = currentTime; // Record time of entry into ready queue
                }
            }

            Process nextProcess = null;
            double bestDynamicPriority = Double.MAX_VALUE; // Lower value indicates higher priority

            // Calculate dynamic priority for processes in the ready queue
            for (Process p : readyQueue) {
                // Time spent waiting since last entered ready queue
                double waitTimeInQueue = currentTime - p.timeLastEnteredReadyQueue;

                // Dynamic Priority Formula: OriginalPriority + (RemainingBurstTime * burstWeight) - (WaitTimeInQueue * waitWeight)
                // This formula aims to:
                // - Reward processes with lower original priority number (higher original priority)
                // - Penalize processes with longer remaining burst time (lower priority for longer burst)
                // - Reward processes that have waited longer (higher priority for longer wait)
                double currentDynamicPriority = p.priority +
                        (p.remainingTime * burstWeight) -
                        (waitTimeInQueue * waitWeight);
                p.calculatedDynamicPriority = currentDynamicPriority;

                // Select the process with the lowest dynamic priority value
                if (currentDynamicPriority < bestDynamicPriority) {
                    bestDynamicPriority = currentDynamicPriority;
                    nextProcess = p;
                } else if (currentDynamicPriority == bestDynamicPriority) {
                    // Tie-breaking: FCFS for processes with same calculated dynamic priority
                    if (nextProcess == null || p.arrivalTime < nextProcess.arrivalTime) {
                        nextProcess = p;
                    }
                }
            }

            // If no process is ready, add IDLE to Gantt chart and advance time
            if (nextProcess == null) {
                gantt.add(-1); // Represents IDLE time
                currentTime++;
                continue;
            }

            // Preemption logic: if a different process is selected, record preemption
            if (currentlyRunningProcess != null && currentlyRunningProcess != nextProcess) {
                currentlyRunningProcess.preemptionMoments.add(currentTime);
                currentlyRunningProcess.timeLastEnteredReadyQueue = currentTime; // Process goes back to ready queue
                if (nextProcess.firstStartTime != -1) {
                    nextProcess.restartMoments.add(currentTime); // This process is restarting
                }
            }

            // Record first start time if it's the first time this process runs
            if (nextProcess.firstStartTime == -1) {
                nextProcess.firstStartTime = currentTime;
            }

            currentlyRunningProcess = nextProcess;
            gantt.add(currentlyRunningProcess.pid); // Add process to Gantt chart
            currentlyRunningProcess.remainingTime--; // Decrement remaining burst time
            currentTime++; // Advance current time by one unit

            // If the current process has completed execution
            if (currentlyRunningProcess.remainingTime == 0) {
                currentlyRunningProcess.completionTime = currentTime;
                readyQueue.remove(currentlyRunningProcess); // Remove from ready queue
                completedProcesses.add(currentlyRunningProcess); // Add to completed list
                currentlyRunningProcess = null; // No process is currently running
            }
        }

        // Calculate Waiting Time and Turnaround Time for completed processes
        for (Process p : completedProcesses) {
            p.turnaroundTime = p.completionTime - p.arrivalTime;

            // Calculate waiting time based on first start time and preemption/restart moments
            p.waitingTime = (p.firstStartTime - p.arrivalTime); // Initial wait before first execution

            // Add all waiting periods due to preemption
            for (int i = 0; i < p.preemptionMoments.size(); i++) {
                // Ensure there's a corresponding restart moment for each preemption
                if (i < p.restartMoments.size()) {
                    p.waitingTime += (p.restartMoments.get(i) - p.preemptionMoments.get(i));
                }
            }
            // If the process was preempted and never restarted (this shouldn't happen with correct logic leading to completion),
            // or if it was the last process running and didn't finish right at the end.
            // This is a complex part, for simplicity, sometimes (Turnaround Time - Burst Time) is used as a fallback if detailed tracking is problematic.
            // However, your current detailed tracking is more accurate for preemptive.
        }

        Process.ganttChart = gantt;
    }


    private void updateTable() {
        String[] columnNames;
        if (Process.lastAlgorithmUsed == 2) {
            columnNames = new String[]{"PID", "Arrival Time", "Burst Time", "Priority", "Waiting Time", "Turnaround Time", "Dynamic Priority"};
        } else {
            columnNames = new String[]{"PID", "Arrival Time", "Burst Time", "Priority", "Waiting Time", "Turnaround Time"};
        }

        DefaultTableModel newModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Make all cells non-editable after scheduling results are displayed
                return false;
            }
        };

        double totalWT = 0, totalTAT = 0;

        // Sort processes by PID for consistent display
        processes.sort(Comparator.comparingInt(p -> p.pid));

        for (Process p : processes) {
            totalWT += p.waitingTime;
            totalTAT += p.turnaroundTime;

            if (Process.lastAlgorithmUsed == 2) {
                newModel.addRow(new Object[]{
                        "P" + p.pid, p.arrivalTime, p.burstTime, p.priority,
                        p.waitingTime, p.turnaroundTime, String.format("%.2f", p.calculatedDynamicPriority)
                });
            } else {
                newModel.addRow(new Object[]{
                        "P" + p.pid, p.arrivalTime, p.burstTime, p.priority,
                        p.waitingTime, p.turnaroundTime
                });
            }
        }

        // Add average row
        if (!processes.isEmpty()) {
            if (Process.lastAlgorithmUsed == 2) {
                newModel.addRow(new Object[]{"Avg", "", "", "",
                        String.format("%.2f", totalWT / processes.size()),
                        String.format("%.2f", totalTAT / processes.size()), ""});
            } else {
                newModel.addRow(new Object[]{"Avg", "", "", "",
                        String.format("%.2f", totalWT / processes.size()),
                        String.format("%.2f", totalTAT / processes.size())});
            }
        }


        table.setModel(newModel);
        // table.setEnabled(true); // This would make it editable again, which we don't want after calculation
        calcButton.setEnabled(true); // Keep calculate button enabled to re-run with new data
        styleTable(table);
    }

    // Enhanced Gantt chart drawing
    private void drawGanttChart(Graphics g) {
        if (Process.ganttChart == null || Process.ganttChart.isEmpty()) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(TEXT_COLOR);
            g2d.drawString("No scheduling data available", 30, 50);
            ganttPanel.setPreferredSize(new Dimension(1000, 150)); // Increased height
            return;
        }

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int x = 40, y = 50, height = 40, width = 40; // Wider blocks for better visibility
        FontMetrics fm = g2d.getFontMetrics();

        // Draw timeline
        g2d.setColor(ACCENT_COLOR);
        g2d.setStroke(new BasicStroke(2)); // Thicker line
        g2d.drawLine(x, y + height + 15, x + Process.ganttChart.size() * width, y + height + 15);
        g2d.setStroke(new BasicStroke(1)); // Reset stroke

        // Time font for numbers below the chart
        Font timeFont = new Font("Segoe UI", Font.PLAIN, 11); // Slightly larger font for numbers
        g2d.setFont(timeFont);
        FontMetrics timeFm = g2d.getFontMetrics(timeFont);


        for (int i = 0; i < Process.ganttChart.size(); ) {
            int pid = Process.ganttChart.get(i);
            int j = i;
            while (j < Process.ganttChart.size() && Process.ganttChart.get(j) == pid) j++;

            int blockWidth = (j - i) * width;

            // Create gradient for process blocks
            GradientPaint gp;
            if (pid == -1) {
                gp = new GradientPaint(x, y, IDLE_COLOR, x, y + height, IDLE_COLOR.darker());
            } else {
                // Generate a distinct color for each process based on PID
                Color baseColor = Color.getHSBColor(((pid * 0.1618f) + 0.618f) % 1, 0.7f, 0.8f); // Golden ratio conjugate for spread
                gp = new GradientPaint(x, y, baseColor, x, y + height, baseColor.darker().darker()); // Darker gradient end
            }

            g2d.setPaint(gp);
            g2d.fillRoundRect(x, y, blockWidth, height, 8, 8); // Rounded corners

            // Border
            g2d.setColor(DARK_BACKGROUND.brighter()); // Slightly brighter border
            g2d.drawRoundRect(x, y, blockWidth, height, 8, 8);

            // Process label (PID or IDLE)
            g2d.setColor(TEXT_COLOR); // Use TEXT_COLOR for labels
            String label = pid == -1 ? "IDLE" : "P" + pid;
            int labelWidth = fm.stringWidth(label); // Using the original fm for process labels
            g2d.setFont(new Font("Segoe UI", Font.BOLD, 12)); // Slightly smaller font for label
            g2d.drawString(label, x + (blockWidth - labelWidth) / 2, y + height / 2 + fm.getAscent() / 3);

            // Time markers
            g2d.setColor(ACCENT_COLOR);
            g2d.drawLine(x, y + height + 10, x, y + height + 20); // Vertical tick
            g2d.setFont(timeFont); // Use the dedicated time font
            String timeStr = "" + i;
            int timeStrWidth = timeFm.stringWidth(timeStr);
            g2d.drawString(timeStr, x - timeStrWidth / 2, y + height + 35); // Adjusted Y position

            x += blockWidth;
            i = j;
        }

        // Final time marker
        g2d.setColor(ACCENT_COLOR);
        g2d.setFont(timeFont); // Use the dedicated time font
        String finalTimeStr = "" + Process.ganttChart.size();
        int finalTimeStrWidth = timeFm.stringWidth(finalTimeStr);
        g2d.drawString(finalTimeStr,
                x - finalTimeStrWidth / 2, // Center under the last tick
                y + height + 35); // Adjusted Y position

        // Set the preferred size for scrolling dynamically
        int currentScrollPaneWidth = (ganttScrollPane != null) ? ganttScrollPane.getWidth() : 1000;
        ganttPanel.setPreferredSize(new Dimension(
                Math.max(currentScrollPaneWidth, Process.ganttChart.size() * width + 80),
                150 // Adjusted height again to ensure numbers fit
        ));
        ganttPanel.revalidate(); // Revalidate to update scroll pane
    }


    // Enhanced table styling
    private void styleTable(JTable table) {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(28);
        table.setShowGrid(true); // Show grid lines
        table.setGridColor(TEXT_COLOR); // Set grid line color to white
        table.setIntercellSpacing(new Dimension(1, 1)); // Small spacing for visible lines
        table.setFillsViewportHeight(true);

        // Header style
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(TABLE_HEADER);
        header.setForeground(TEXT_COLOR);
        header.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 0));
        header.setReorderingAllowed(false); // Prevent column reordering
        header.setResizingAllowed(true); // Allow column resizing

        // Cell style with alternating row colors
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value,
                        isSelected, hasFocus, row, column);

                c.setForeground(TEXT_COLOR);
                if (row % 2 == 0) {
                    c.setBackground(TABLE_ROW_EVEN);
                } else {
                    c.setBackground(TABLE_ROW_ODD);
                }

                if (isSelected) {
                    c.setBackground(HIGHLIGHT_COLOR);
                    c.setForeground(DARK_BACKGROUND); // Dark text on highlight
                }

                setHorizontalAlignment(JLabel.CENTER);
                setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5)); // Cell padding

                return c;
            }
        });
    }

    public static void setUIFont(Font f) {
        Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object val = UIManager.get(key);
            if (val instanceof Font) {
                UIManager.put(key, f);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main().setVisible(true));
    }

    static class Process {

        int pid, arrivalTime, burstTime, priority;
        int remainingTime, completionTime, turnaroundTime, waitingTime;
        int firstStartTime; // Time when process first starts execution
        ArrayList<Integer> preemptionMoments; // Times when process was preempted
        ArrayList<Integer> restartMoments; // Times when process resumed after preemption
        int timeLastEnteredReadyQueue; // For dynamic priority calculation
        double calculatedDynamicPriority; // For dynamic priority display

        static ArrayList<Integer> ganttChart;
        static int lastAlgorithmUsed; // 0=preemptive, 1=non-preemptive, 2=dynamic

        public Process(int pid, int at, int bt, int pr) {
            this.pid = pid;
            this.arrivalTime = at;
            this.burstTime = bt;
            this.priority = pr;
            this.preemptionMoments = new ArrayList<>();
            this.restartMoments = new ArrayList<>();
            this.calculatedDynamicPriority = pr; // Initialize with original priority
            this.remainingTime = bt; // Important for simulation
            this.timeLastEnteredReadyQueue = at; // Important for initial state
        }
    }

    // Enhanced comparison dialog
    private void showComparisonDialog() {
        if (model == null || model.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this,
                    "No process data to compare. Please create a table and enter processes first.",
                    "Comparison Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Validate input before running comparison
        for (int i = 0; i < model.getRowCount(); i++) {
            try {
                int at = Integer.parseInt(model.getValueAt(i, 1).toString());
                int bt = Integer.parseInt(model.getValueAt(i, 2).toString());
                int pr = Integer.parseInt(model.getValueAt(i, 3).toString());

                if (at < 0 || bt <= 0 || pr < 0) {
                    JOptionPane.showMessageDialog(this, "Invalid input at row " + (i + 1) + " for comparison. Arrival Time must be >= 0, Burst Time > 0, Priority >= 0.");
                    return;
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Invalid input (non-numeric) at row " + (i + 1) + " for comparison. Please enter valid numbers.");
                return;
            } catch (NullPointerException e) {
                JOptionPane.showMessageDialog(this, "Missing input at row " + (i + 1) + " for comparison. Please fill all fields.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }


        // Create a professional-looking dialog
        JDialog dialog = new JDialog(this, "Algorithm Comparison", true); // Modal dialog
        dialog.setLayout(new BorderLayout());
        dialog.setSize(650, 550); // Increased size for better visibility
        dialog.setLocationRelativeTo(this); // Center on parent frame

        // Create content panel
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(DARK_BACKGROUND);
        contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Create title label
        JLabel titleLabel = new JLabel("Algorithm Performance Comparison", JLabel.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18)); // Larger font
        titleLabel.setForeground(ACCENT_COLOR);
        titleLabel.setBorder(new EmptyBorder(0, 0, 20, 0)); // More padding
        contentPanel.add(titleLabel, BorderLayout.NORTH);

        // Create results panel
        JPanel resultsPanel = new JPanel(new GridLayout(3, 1, 15, 15)); // More spacing
        resultsPanel.setBackground(DARK_BACKGROUND);

        // Run comparisons
        String[] algoNames = {"Priority Preemptive", "Priority Non-Preemptive", "Dynamic Priority Boost"};
        // Slightly different shades for comparison boxes
        Color[] algoColors = {
                new Color(0, 160, 140), // Teal shade
                new Color(255, 165, 0),  // Orange
                new Color(100, 180, 220) // Light Blue
        };


        for (int algo = 0; algo < 3; algo++) {
            JPanel algoPanel = new JPanel(new BorderLayout()); // Use BorderLayout for better label/stats arrangement
            algoPanel.setBackground(MEDIUM_BACKGROUND);
            algoPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(algoColors[algo], 2), // Thicker border
                    new EmptyBorder(12, 18, 12, 18) // More padding
            ));

            JLabel algoLabel = new JLabel(algoNames[algo], JLabel.LEFT); // Align left
            algoLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
            algoLabel.setForeground(algoColors[algo]);
            algoPanel.add(algoLabel, BorderLayout.NORTH);

            // Create a fresh list of processes for each algorithm run
            ArrayList<Process> testProcessesForAlgo = new ArrayList<>();
            for (int i = 0; i < model.getRowCount(); i++) {
                try {
                    int at = Integer.parseInt(model.getValueAt(i, 1).toString());
                    int bt = Integer.parseInt(model.getValueAt(i, 2).toString());
                    int pr = Integer.parseInt(model.getValueAt(i, 3).toString());
                    testProcessesForAlgo.add(new Process(i + 1, at, bt, pr));
                } catch (NumberFormatException | NullPointerException e) {
                    // This should ideally be caught by the earlier validation, but as a fallback
                    JOptionPane.showMessageDialog(dialog, "Error parsing process data for comparison.", "Data Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            // Run the specific scheduling algorithm
            switch (algo) {
                case 0: schedulePriorityPreemptive(testProcessesForAlgo); break;
                case 1: schedulePriorityNonPreemptive(testProcessesForAlgo); break;
                case 2: scheduleDynamicPriorityBoost(testProcessesForAlgo); break;
            }

            // Calculate total waiting and turnaround times
            double totalWT = 0, totalTAT = 0;
            if (!testProcessesForAlgo.isEmpty()) {
                for (Process p : testProcessesForAlgo) {
                    totalWT += p.waitingTime;
                    totalTAT += p.turnaroundTime;
                }
                totalWT /= testProcessesForAlgo.size();
                totalTAT /= testProcessesForAlgo.size();
            } else {
                // Handle case of no processes to avoid NaN
                totalWT = 0;
                totalTAT = 0;
            }

            // Numbers are now explicitly white using TEXT_COLOR's RGB
            JLabel statsLabel = new JLabel(String.format(
                    "<html>Avg Waiting Time: <b style='color: #%06X;'>%.2f</b><br>Avg Turnaround Time: <b style='color: #%06X;'>%.2f</b></html>",
                    TEXT_COLOR.getRGB() & 0xFFFFFF, totalWT, TEXT_COLOR.getRGB() & 0xFFFFFF, totalTAT
            ));
            statsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            statsLabel.setForeground(TEXT_COLOR); // Still set foreground of JLabel itself to TEXT_COLOR
            statsLabel.setBorder(new EmptyBorder(5, 0, 0, 0)); // Padding below algorithm name
            algoPanel.add(statsLabel, BorderLayout.CENTER);

            resultsPanel.add(algoPanel);
        }

        contentPanel.add(resultsPanel, BorderLayout.CENTER);
        dialog.add(contentPanel, BorderLayout.CENTER);

        // Add close button
        JButton closeButton = createStyledButton("Close");
        closeButton.addActionListener(e -> dialog.dispose());
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(DARK_BACKGROUND);
        buttonPanel.setBorder(new EmptyBorder(10, 0, 10, 0)); // Padding for button
        buttonPanel.add(closeButton);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }
}