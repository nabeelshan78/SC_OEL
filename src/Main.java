import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class Main {
    private DefaultListModel<TaskItem> listModel;
    private JList<TaskItem> taskList;
    private JTextField taskField;
    private JTextField dateField;
    private JComboBox<String> priorityBox;
    private List<TaskItem> data = new ArrayList<>();

    public Main() {
        UIManager.put("Button.font", new Font("Segoe UI", Font.BOLD, 13));
        UIManager.put("Label.font", new Font("Segoe UI", Font.PLAIN, 13));
        UIManager.put("TextField.font", new Font("Segoe UI", Font.PLAIN, 13));
        UIManager.put("ComboBox.font", new Font("Segoe UI", Font.PLAIN, 13));
        UIManager.put("List.font", new Font("Segoe UI", Font.PLAIN, 13));

        JFrame frame = new JFrame("📝 Smart To-Do List");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout(15, 15));
        root.setBackground(new Color(245, 247, 250));
        root.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel title = new JLabel("🗓️ My To-Do Tasks");
        title.setFont(new Font("Segoe UI Semibold", Font.BOLD, 22));
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setForeground(new Color(44, 62, 80));
        root.add(title, BorderLayout.NORTH);

        listModel = new DefaultListModel<>();
        taskList = new JList<>(listModel);
        taskList.setCellRenderer(new EnhancedRenderer());
        JScrollPane scroll = new JScrollPane(taskList);
        scroll.setBorder(BorderFactory.createTitledBorder("All Tasks"));
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        root.add(scroll, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBackground(Color.WHITE);
        inputPanel.setBorder(new CompoundBorder(
                new LineBorder(new Color(220, 220, 220), 1, true),
                new EmptyBorder(15, 15, 15, 15)
        ));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        inputPanel.add(new JLabel("Task:"), gbc);
        gbc.gridx = 1;
        taskField = new JTextField();
        taskField.setBorder(new RoundedBorder(8));
        inputPanel.add(taskField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        inputPanel.add(new JLabel("Due Date (yyyy-MM-dd):"), gbc);
        gbc.gridx = 1;
        dateField = new JTextField();
        dateField.setBorder(new RoundedBorder(8));
        inputPanel.add(dateField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        inputPanel.add(new JLabel("Priority:"), gbc);
        gbc.gridx = 1;
        priorityBox = new JComboBox<>(new String[]{"Low", "Medium", "High"});
        priorityBox.setBorder(new RoundedBorder(8));
        inputPanel.add(priorityBox, gbc);

        root.add(inputPanel, BorderLayout.WEST);

        JPanel buttonPanel = new JPanel(new GridLayout(1, 5, 10, 10));
        buttonPanel.setBackground(new Color(245, 247, 250));
        JButton addBtn = createStyledButton("Add");
        JButton removeBtn = createStyledButton("Remove");
        JButton editBtn = createStyledButton("Edit");
        JButton saveBtn = createStyledButton("Save");
        JButton toggleBtn = createStyledButton("Change Status");

        buttonPanel.add(addBtn);
        buttonPanel.add(removeBtn);
        buttonPanel.add(editBtn);
        buttonPanel.add(saveBtn);
        buttonPanel.add(toggleBtn);

        root.add(buttonPanel, BorderLayout.SOUTH);

        addBtn.addActionListener(e -> addTask());
        removeBtn.addActionListener(e -> removeTask());
        editBtn.addActionListener(e -> editTask());
        saveBtn.addActionListener(e -> saveTasks());
        toggleBtn.addActionListener(e -> toggleTaskStatus());

        frame.add(root);
        frame.setVisible(true);
    }

    private void addTask() {
        String taskText = taskField.getText().trim();
        String dueDateText = dateField.getText().trim();
        String priority = (String) priorityBox.getSelectedItem();

        if (taskText.isEmpty() || dueDateText.isEmpty()) {
            showToast("Please enter both task and due date.", false);
            return;
        }
        if (!isValidFutureDate(dueDateText)) {
            showToast("Please enter a valid current or future date.", false);
            return;
        }

        TaskItem task = new PriorityTask(taskText, dueDateText, priority);
        listModel.addElement(task);
        data.add(task);
        showToast("Task added successfully ✅", true);

        taskField.setText("");
        dateField.setText("");
        priorityBox.setSelectedIndex(0);
    }

    private void removeTask() {
        int i = taskList.getSelectedIndex();
        if (i != -1) {
            listModel.remove(i);
            data.remove(i);
            showToast("Task removed 🗑️", true);
        } else showToast("Please select a task first.", false);
    }

    private void editTask() {
        int i = taskList.getSelectedIndex();
        if (i == -1) {
            showToast("Select a task to edit.", false);
            return;
        }
        TaskItem task = listModel.getElementAt(i);
        String newTask = JOptionPane.showInputDialog("Edit task:", task.getTask());
        String newDate = JOptionPane.showInputDialog("Edit date (yyyy-MM-dd):", task.getDueDate());
        String newPriority = (String) JOptionPane.showInputDialog(
                null, "Change priority:", "Priority",
                JOptionPane.QUESTION_MESSAGE, null,
                new String[]{"Low", "Medium", "High"},
                ((PriorityTask) task).getPriority()
        );

        if (newTask == null || newDate == null || newPriority == null) return;
        if (!isValidFutureDate(newDate)) {
            showToast("Invalid date format or past date.", false);
            return;
        }

        task.setTask(newTask);
        task.setDueDate(newDate);
        ((PriorityTask) task).setPriority(newPriority);
        taskList.repaint();
        showToast("Task updated ✏️", true);
    }

    private void toggleTaskStatus() {
        int i = taskList.getSelectedIndex();
        if (i != -1) {
            TaskItem t = listModel.getElementAt(i);
            t.toggleCompleted();
            taskList.repaint();
            showToast(t.isCompleted() ? "Marked complete ✅" : "Marked pending ⏳", true);
        } else showToast("Select a task first.", false);
    }

    private void saveTasks() {
        showToast("Tasks saved in memory 💾", true);
    }

    private boolean isValidFutureDate(String date) {
        try {
            LocalDate input = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            return !input.isBefore(LocalDate.now());
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    private void showToast(String message, boolean success) {
        JDialog toast = new JDialog();
        toast.setUndecorated(true);
        toast.setLayout(new FlowLayout());
        JLabel msg = new JLabel(message);
        msg.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        msg.setForeground(success ? new Color(34, 153, 84) : Color.RED);
        msg.setBorder(new EmptyBorder(10, 20, 10, 20));
        toast.add(msg);
        toast.pack();
        toast.setLocationRelativeTo(null);
        new Timer(1500, e -> toast.dispose()).start();
        toast.setVisible(true);
    }

    private JButton createStyledButton(String text) {
        JButton btn = new JButton(text);
        // btn.setBackground(new Color(52, 152, 219));
        btn.setBackground(new Color(142, 68, 173));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(new RoundedBorder(10));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) { btn.setBackground(new Color(108, 52, 131)); }
            @Override
            public void mouseExited(MouseEvent e) { btn.setBackground(new Color(142, 68, 173)); }
        });
        return btn;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::new);
    }
}

class TaskItem {
    private String task, dueDate;
    private boolean completed;

    public TaskItem(String task, String dueDate) {
        this.task = task;
        this.dueDate = dueDate;
        this.completed = false;
    }

    public String getTask() { return task; }
    public void setTask(String task) { this.task = task; }
    public String getDueDate() { return dueDate; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate; }
    public boolean isCompleted() { return completed; }
    public void toggleCompleted() { completed = !completed; }
}

class PriorityTask extends TaskItem {
    private String priority;
    public PriorityTask(String task, String dueDate, String priority) {
        super(task, dueDate);
        this.priority = priority;
    }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
}

class EnhancedRenderer extends JPanel implements ListCellRenderer<TaskItem> {
    private JLabel titleLabel, dateLabel, priorityLabel, statusLabel;

    public EnhancedRenderer() {
        setLayout(new BorderLayout(10, 5));
        setBorder(new CompoundBorder(
                new LineBorder(new Color(230, 230, 230), 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));

        titleLabel = new JLabel();
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));

        dateLabel = new JLabel();
        dateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        dateLabel.setForeground(new Color(100, 100, 100));

        priorityLabel = new JLabel();
        priorityLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));

        statusLabel = new JLabel();
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JPanel info = new JPanel(new GridLayout(2, 1));
        info.setOpaque(false);
        info.add(titleLabel);
        info.add(dateLabel);

        JPanel right = new JPanel(new GridLayout(2, 1, 0, 4));
        right.setOpaque(false);
        right.add(priorityLabel);
        right.add(statusLabel);

        add(info, BorderLayout.CENTER);
        add(right, BorderLayout.EAST);
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends TaskItem> list, TaskItem value, int index,
                                                  boolean isSelected, boolean cellHasFocus) {
        if (value instanceof PriorityTask task) {
            titleLabel.setText(task.getTask());
            dateLabel.setText("📅 " + task.getDueDate());

            String pr = task.getPriority();
            switch (pr) {
                case "High" -> priorityLabel.setForeground(new Color(231, 76, 60));
                case "Medium" -> priorityLabel.setForeground(new Color(243, 156, 18));
                default -> priorityLabel.setForeground(new Color(39, 174, 96));
            }
            priorityLabel.setText("⚡ " + pr + " Priority");

            if (task.isCompleted()) {
                statusLabel.setText("✅ Completed");
                statusLabel.setForeground(new Color(46, 204, 113));
                titleLabel.setFont(titleLabel.getFont().deriveFont(Font.ITALIC));
            } else {
                statusLabel.setText("⏳ Pending");
                statusLabel.setForeground(new Color(231, 76, 60));
                titleLabel.setFont(titleLabel.getFont().deriveFont(Font.PLAIN));
            }
        }

        if (isSelected) {
            setBackground(new Color(52, 152, 219, 40));
        } else {
            setBackground(Color.WHITE);
        }
        return this;
    }
}

class RoundedBorder implements Border {
    private int radius;
    RoundedBorder(int radius) { this.radius = radius; }
    public Insets getBorderInsets(Component c) { return new Insets(radius + 1, radius + 1, radius + 2, radius); }
    public boolean isBorderOpaque() { return false; }
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        g.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
    }
}
