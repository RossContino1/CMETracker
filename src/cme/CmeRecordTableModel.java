package cme;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

public class CmeRecordTableModel extends AbstractTableModel {
    private static final String[] COLUMNS = {"Date", "Title", "Type", "Category", "Hours"};
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;

    private List<CmeRecord> records = new ArrayList<>();

    public void setRecords(List<CmeRecord> records) {
        this.records = new ArrayList<>(records);
        fireTableDataChanged();
    }

    public CmeRecord getRecordAt(int row) {
        return records.get(row);
    }

    @Override
    public int getRowCount() {
        return records.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMNS.length;
    }

    @Override
    public String getColumnName(int column) {
        return COLUMNS[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        CmeRecord record = records.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> DATE_FORMAT.format(record.getDate());
            case 1 -> record.getTitle();
            case 2 -> record.getCreditType();
            case 3 -> record.getCategory();
            case 4 -> String.format("%.2f", record.getHours());
            default -> "";
        };
    }
}
