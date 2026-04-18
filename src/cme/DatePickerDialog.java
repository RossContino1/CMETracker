package cme;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Locale;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class DatePickerDialog extends JDialog {
    private final JLabel monthLabel = new JLabel("", JLabel.CENTER);
    private final JPanel daysPanel = new JPanel(new GridLayout(0, 7, 2, 2));

    private YearMonth visibleMonth;
    private LocalDate selectedDate;

    private DatePickerDialog(Frame owner, LocalDate initialDate) {
        super(owner, "Choose Date", true);
        LocalDate displayDate = initialDate == null ? LocalDate.now() : initialDate;
        this.visibleMonth = YearMonth.from(displayDate);

        setLayout(new BorderLayout(6, 6));
        add(buildHeader(), BorderLayout.NORTH);
        add(daysPanel, BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);

        refreshDays();
        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    public static LocalDate pickDate(Frame owner, LocalDate initialDate) {
        DatePickerDialog dialog = new DatePickerDialog(owner, initialDate);
        dialog.setVisible(true);
        return dialog.selectedDate;
    }

    private JPanel buildHeader() {
        JButton previous = new JButton("<");
        JButton next = new JButton(">");
        previous.addActionListener(event -> {
            visibleMonth = visibleMonth.minusMonths(1);
            refreshDays();
        });
        next.addActionListener(event -> {
            visibleMonth = visibleMonth.plusMonths(1);
            refreshDays();
        });

        JPanel header = new JPanel(new BorderLayout());
        header.add(previous, BorderLayout.WEST);
        header.add(monthLabel, BorderLayout.CENTER);
        header.add(next, BorderLayout.EAST);
        return header;
    }

    private JPanel buildFooter() {
        JButton today = new JButton("Today");
        JButton cancel = new JButton("Cancel");
        today.addActionListener(event -> {
            selectedDate = LocalDate.now();
            dispose();
        });
        cancel.addActionListener(event -> {
            selectedDate = null;
            dispose();
        });

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.add(today);
        footer.add(cancel);
        return footer;
    }

    private void refreshDays() {
        daysPanel.removeAll();
        monthLabel.setText(visibleMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault())
                + " " + visibleMonth.getYear());

        for (DayOfWeek day : DayOfWeek.values()) {
            daysPanel.add(new JLabel(day.getDisplayName(TextStyle.SHORT, Locale.getDefault()), JLabel.CENTER));
        }

        int firstDayColumn = visibleMonth.atDay(1).getDayOfWeek().getValue();
        for (int i = 1; i < firstDayColumn; i++) {
            daysPanel.add(new JLabel(""));
        }

        int daysInMonth = visibleMonth.lengthOfMonth();
        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = visibleMonth.atDay(day);
            JButton dayButton = new JButton(Integer.toString(day));
            dayButton.addActionListener(event -> {
                selectedDate = date;
                dispose();
            });
            daysPanel.add(dayButton);
        }

        daysPanel.revalidate();
        daysPanel.repaint();
        pack();
    }
}
