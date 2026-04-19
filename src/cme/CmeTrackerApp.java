package cme;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.print.PrinterException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

public final class CmeTrackerApp extends JFrame {
    private static final String ICON_RESOURCE = "/cme/resources/caduceus-icon.png";
    private static final String HELP_RESOURCE = "/cme/resources/help.txt";
    private static final String WEBSITE_URL = "https://bytesbreadbbq.com/cmetracker/";
    private static final String GITHUB_URL = "https://github.com/RossContino1/CMETracker";
    private static final String DONATE_URL = "https://www.paypal.com/donate/?hosted_button_id=XS9MXN5AE5P3S";
    private static final String APP_VERSION = "1.0.0";
    private static final Path DONATION_PROMPT_MARKER = Path.of("data", ".donation-prompt-seen");

    private final CmeRepository repository = new CmeRepository(Path.of("data", "cme-records.csv"));
    private final List<CmeRecord> records = new ArrayList<>();
    private final CmeRecordTableModel tableModel = new CmeRecordTableModel();
    private final Image appIcon = loadImageResource(ICON_RESOURCE);

    private final JTable table = new JTable(tableModel);
    private final JTextField dateField = new JTextField(10);
    private final JTextField titleField = new JTextField(24);
    private final JComboBox<String> typeBox = new JComboBox<>(new String[] {"AMA", "AOA", "Nurse", "PA"});
    private final JComboBox<String> categoryBox = new JComboBox<>(new String[] {"1A", "1B", "2A", "2B"});
    private final JTextField hoursField = new JTextField(8);
    private final JTextField startField = new JTextField(10);
    private final JTextField endField = new JTextField(10);
    private final JLabel totalsLabel = new JLabel("Totals: 0.00 hours");
    private final JTextArea reportArea = new JTextArea();

    private CmeRecord selectedRecord;

    public CmeTrackerApp() {
        super("CME Tracker");
        typeBox.setEditable(true);
        categoryBox.setEditable(true);
        reportArea.setEditable(false);
        reportArea.setLineWrap(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(920, 540));
        if (appIcon != null) {
            setIconImage(appIcon);
        }

        loadRecords();
        setJMenuBar(buildMenuBar());
        buildLayout();
        wireEvents();
        refreshView();
        pack();
        setLocationRelativeTo(null);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CmeTrackerApp().setVisible(true));
    }

    private JMenuBar buildMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(event -> dispose());
        fileMenu.add(exitItem);

        JMenu helpMenu = new JMenu("Help");
        JMenuItem helpItem = new JMenuItem("Help Contents");
        JMenuItem aboutItem = new JMenuItem("About CME Tracker");
        helpItem.addActionListener(event -> showHelp());
        aboutItem.addActionListener(event -> showAbout());
        helpMenu.add(helpItem);
        helpMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(helpMenu);
        return menuBar;
    }

    private void buildLayout() {
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        addField(form, gbc, 0, "Date", createDateInput(dateField));
        addField(form, gbc, 1, "Title", titleField);
        addField(form, gbc, 2, "Type", typeBox);
        addField(form, gbc, 3, "Category", categoryBox);
        addField(form, gbc, 4, "Hours", hoursField);

        JButton addButton = new JButton("Add");
        JButton updateButton = new JButton("Update");
        JButton deleteButton = new JButton("Delete");
        JButton clearButton = new JButton("Clear");
        JButton saveButton = new JButton("Save");
        JPanel buttons = new JPanel();
        buttons.add(addButton);
        buttons.add(updateButton);
        buttons.add(deleteButton);
        buttons.add(clearButton);
        buttons.add(saveButton);

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        form.add(buttons, gbc);

        JPanel filter = new JPanel();
        JButton applyFilterButton = new JButton("Apply Range");
        JButton clearFilterButton = new JButton("Clear Range");
        JButton printButton = new JButton("Print Report");
        filter.add(new JLabel("Start"));
        filter.add(createDateInput(startField));
        filter.add(new JLabel("End"));
        filter.add(createDateInput(endField));
        filter.add(applyFilterButton);
        filter.add(clearFilterButton);
        filter.add(printButton);
        filter.add(totalsLabel);

        JScrollPane tableScroll = new JScrollPane(table);
        JScrollPane reportScroll = new JScrollPane(reportArea);
        table.setPreferredScrollableViewportSize(new Dimension(900, 220));
        reportArea.setRows(9);
        reportArea.setColumns(88);
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tableScroll, reportScroll);
        split.setResizeWeight(0.58);

        add(form, BorderLayout.NORTH);
        add(split, BorderLayout.CENTER);
        add(filter, BorderLayout.SOUTH);

        addButton.addActionListener(this::addRecord);
        updateButton.addActionListener(this::updateRecord);
        deleteButton.addActionListener(this::deleteRecord);
        clearButton.addActionListener(event -> clearForm());
        saveButton.addActionListener(event -> saveRecords());
        applyFilterButton.addActionListener(event -> refreshView());
        clearFilterButton.addActionListener(event -> {
            startField.setText("");
            endField.setText("");
            refreshView();
        });
        printButton.addActionListener(event -> printReport());
    }

    private JPanel createDateInput(JTextField field) {
        JPanel panel = new JPanel(new BorderLayout(2, 0));
        JButton pickerButton = new JButton("...");
        pickerButton.setToolTipText("Choose date");
        pickerButton.addActionListener(event -> chooseDate(field));
        panel.add(field, BorderLayout.CENTER);
        panel.add(pickerButton, BorderLayout.EAST);
        return panel;
    }

    private void chooseDate(JTextField field) {
        LocalDate initialDate;
        try {
            initialDate = parseOptionalDate(field.getText(), "Date");
        } catch (IllegalArgumentException ex) {
            initialDate = LocalDate.now();
        }

        LocalDate selectedDate = DatePickerDialog.pickDate(this, initialDate);
        if (selectedDate != null) {
            field.setText(selectedDate.toString());
        }
    }

    private static void addField(JPanel panel, GridBagConstraints gbc, int row, String label, java.awt.Component field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        panel.add(new JLabel(label), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(field, gbc);
    }

    private void wireEvents() {
        table.getSelectionModel().addListSelectionListener(event -> {
            if (event.getValueIsAdjusting() || table.getSelectedRow() < 0) {
                return;
            }
            selectedRecord = tableModel.getRecordAt(table.convertRowIndexToModel(table.getSelectedRow()));
            dateField.setText(selectedRecord.getDate().toString());
            titleField.setText(selectedRecord.getTitle());
            typeBox.setSelectedItem(selectedRecord.getCreditType());
            categoryBox.setSelectedItem(selectedRecord.getCategory());
            hoursField.setText(Double.toString(selectedRecord.getHours()));
        });
    }

    private void addRecord(ActionEvent event) {
        try {
            records.add(readForm(null));
            clearForm();
            boolean saved = saveRecords();
            refreshView();
            if (saved) {
                showDonationPromptIfNeeded();
            }
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }

    private void updateRecord(ActionEvent event) {
        if (selectedRecord == null) {
            showError("Select a record to update.");
            return;
        }
        try {
            readForm(selectedRecord);
            clearForm();
            saveRecords();
            refreshView();
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }

    private void deleteRecord(ActionEvent event) {
        if (selectedRecord == null) {
            showError("Select a record to delete.");
            return;
        }
        records.removeIf(record -> record.getId().equals(selectedRecord.getId()));
        clearForm();
        saveRecords();
        refreshView();
    }

    private CmeRecord readForm(CmeRecord existing) {
        LocalDate date = parseDate(dateField.getText(), "Date");
        String title = titleField.getText();
        String type = String.valueOf(typeBox.getSelectedItem());
        String category = String.valueOf(categoryBox.getSelectedItem());
        double hours;
        try {
            hours = Double.parseDouble(hoursField.getText().trim());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Hours must be a number.");
        }
        if (hours <= 0) {
            throw new IllegalArgumentException("Hours must be greater than zero.");
        }

        if (existing == null) {
            return new CmeRecord(date, title, type, category, hours);
        }
        existing.setDate(date);
        existing.setTitle(title);
        existing.setCreditType(type);
        existing.setCategory(category);
        existing.setHours(hours);
        return existing;
    }

    private void refreshView() {
        try {
            LocalDate start = parseOptionalDate(startField.getText(), "Start date");
            LocalDate end = parseOptionalDate(endField.getText(), "End date");
            if (start != null && end != null && end.isBefore(start)) {
                throw new IllegalArgumentException("End date cannot be before start date.");
            }
            List<CmeRecord> filtered = CmeReports.filterByDate(records, start, end);
            tableModel.setRecords(filtered);
            reportArea.setText(CmeReports.buildReport(filtered, start, end));
            totalsLabel.setText(String.format(
                    "Totals: %.2f hours%s",
                    CmeReports.totalHours(filtered),
                    formatTypeTotals(filtered)));
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }

    private static String formatTypeTotals(List<CmeRecord> records) {
        Map<String, Double> totalsByType = CmeReports.totalHoursByCreditType(records);
        if (totalsByType.isEmpty()) {
            return "";
        }
        return totalsByType.entrySet().stream()
                .map(entry -> entry.getKey() + ": " + String.format("%.2f", entry.getValue()))
                .collect(Collectors.joining(" | ", " | ", ""));
    }

    private void clearForm() {
        selectedRecord = null;
        table.clearSelection();
        dateField.setText(LocalDate.now().toString());
        titleField.setText("");
        typeBox.setSelectedItem("AOA");
        categoryBox.setSelectedItem("1A");
        hoursField.setText("");
    }

    private void loadRecords() {
        try {
            records.addAll(repository.load());
        } catch (IOException | RuntimeException ex) {
            showError("Could not load saved CME records: " + ex.getMessage());
        }
    }

    private boolean saveRecords() {
        try {
            repository.save(records);
            return true;
        } catch (IOException ex) {
            showError("Could not save CME records: " + ex.getMessage());
            return false;
        }
    }

    private void showDonationPromptIfNeeded() {
        if (Files.exists(DONATION_PROMPT_MARKER)) {
            return;
        }

        Object[] options = {"Donate", "Cancel"};
        int choice = JOptionPane.showOptionDialog(
                this,
                "If you found this application helpful, consider supporting the project.",
                "Support CME Tracker",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                options,
                options[0]);

        markDonationPromptSeen();
        if (choice == 0) {
            openLink(DONATE_URL);
        }
    }

    private void markDonationPromptSeen() {
        try {
            Files.createDirectories(DONATION_PROMPT_MARKER.getParent());
            Files.writeString(DONATION_PROMPT_MARKER, "seen\n", StandardCharsets.UTF_8);
        } catch (IOException ex) {
            showError("Could not save donation prompt preference: " + ex.getMessage());
        }
    }

    private void printReport() {
        Object[] options = {"Printer", "PDF File", "Cancel"};
        int choice = JOptionPane.showOptionDialog(
                this,
                "Send the report to a printer or save it as a PDF file?",
                "Report Output",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[1]);

        if (choice == 0) {
            printReportToPrinter();
        } else if (choice == 1) {
            saveReportAsPdf();
        }
    }

    private void printReportToPrinter() {
        try {
            reportArea.print();
        } catch (PrinterException ex) {
            showError("Could not print report: " + ex.getMessage());
        }
    }

    private void saveReportAsPdf() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save CME Report PDF");
        chooser.setFileFilter(new FileNameExtensionFilter("PDF Files", "pdf"));
        chooser.setSelectedFile(new java.io.File("cme-report.pdf"));

        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        Path pdfPath = chooser.getSelectedFile().toPath();
        if (!pdfPath.toString().toLowerCase().endsWith(".pdf")) {
            pdfPath = Path.of(pdfPath.toString() + ".pdf");
        }

        try {
            PdfReportWriter.writeReport(pdfPath, reportArea.getText());
            JOptionPane.showMessageDialog(
                    this,
                    "Saved PDF report to:\n" + pdfPath,
                    "CME Report PDF",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            showError("Could not save PDF report: " + ex.getMessage());
        }
    }

    private void showHelp() {
        JTextArea helpText = new JTextArea(loadTextResource(HELP_RESOURCE));
        helpText.setEditable(false);
        helpText.setLineWrap(true);
        helpText.setWrapStyleWord(true);
        helpText.setColumns(52);
        helpText.setRows(14);
        JOptionPane.showMessageDialog(
                this,
                new JScrollPane(helpText),
                "CME Tracker Help",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void showAbout() {
        JDialog dialog = new JDialog(this, "About CME Tracker", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        if (appIcon != null) {
            dialog.setIconImage(appIcon);
        }

        JPanel aboutPanel = new JPanel();
        aboutPanel.setLayout(new BoxLayout(aboutPanel, BoxLayout.Y_AXIS));
        aboutPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(16, 18, 10, 18));

        if (appIcon != null) {
            JLabel iconLabel = new JLabel(new ImageIcon(appIcon.getScaledInstance(128, 128, Image.SCALE_SMOOTH)));
            iconLabel.setAlignmentX(CENTER_ALIGNMENT);
            aboutPanel.add(iconLabel);
        }

        JLabel title = new JLabel("CME Tracker");
        title.setAlignmentX(CENTER_ALIGNMENT);
        aboutPanel.add(title);

        JLabel description = new JLabel("Continuing medical education tracking for license cycles.");
        description.setAlignmentX(CENTER_ALIGNMENT);
        aboutPanel.add(description);

        JLabel storage = new JLabel("Records are saved locally in data/cme-records.csv.");
        storage.setAlignmentX(CENTER_ALIGNMENT);
        aboutPanel.add(storage);

        JLabel version = new JLabel("Version " + APP_VERSION);
        version.setAlignmentX(CENTER_ALIGNMENT);
        aboutPanel.add(version);

        JLabel copyright = new JLabel("© 2026 Ross Contino");
        copyright.setAlignmentX(CENTER_ALIGNMENT);
        aboutPanel.add(copyright);

        JLabel license = new JLabel("MIT License");
        license.setAlignmentX(CENTER_ALIGNMENT);
        aboutPanel.add(license);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton githubButton = new JButton("GitHub");
        JButton websiteButton = new JButton("Website");
        JButton donateButton = new JButton("Donate");
        JButton closeButton = new JButton("Close");
        githubButton.addActionListener(event -> openLink(GITHUB_URL));
        websiteButton.addActionListener(event -> openLink(WEBSITE_URL));
        donateButton.addActionListener(event -> openLink(DONATE_URL));
        closeButton.addActionListener(event -> dialog.dispose());
        buttonPanel.add(githubButton);
        buttonPanel.add(websiteButton);
        buttonPanel.add(donateButton);
        buttonPanel.add(closeButton);

        dialog.add(aboutPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void openLink(String url) {
        if (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            showError("Opening web links is not supported on this computer.");
            return;
        }

        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (IOException | URISyntaxException ex) {
            showError("Could not open link: " + ex.getMessage());
        }
    }

    private static Image loadImageResource(String resourcePath) {
        try (InputStream input = CmeTrackerApp.class.getResourceAsStream(resourcePath)) {
            return input == null ? null : ImageIO.read(input);
        } catch (IOException ex) {
            return null;
        }
    }

    private static String loadTextResource(String resourcePath) {
        try (InputStream input = CmeTrackerApp.class.getResourceAsStream(resourcePath)) {
            if (input == null) {
                return "Help is not available.";
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(input))) {
                return reader.lines().collect(Collectors.joining(System.lineSeparator()));
            }
        } catch (IOException ex) {
            return "Help could not be loaded: " + ex.getMessage();
        }
    }

    private static LocalDate parseOptionalDate(String text, String label) {
        if (text == null || text.isBlank()) {
            return null;
        }
        return parseDate(text, label);
    }

    private static LocalDate parseDate(String text, String label) {
        try {
            return LocalDate.parse(text.trim());
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException(label + " must use yyyy-mm-dd format.");
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "CME Tracker", JOptionPane.ERROR_MESSAGE);
    }
}
