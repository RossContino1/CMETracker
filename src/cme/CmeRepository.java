package cme;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CmeRepository {
    private static final String HEADER = "id,date,title,creditType,category,hours";

    private final Path csvPath;

    public CmeRepository(Path csvPath) {
        this.csvPath = csvPath;
    }

    public List<CmeRecord> load() throws IOException {
        if (!Files.exists(csvPath)) {
            return new ArrayList<>();
        }

        List<String> lines = Files.readAllLines(csvPath, StandardCharsets.UTF_8);
        List<CmeRecord> records = new ArrayList<>();
        for (int i = 1; i < lines.size(); i++) {
            if (lines.get(i).isBlank()) {
                continue;
            }
            List<String> row = parseCsvLine(lines.get(i));
            if (row.size() != 6) {
                throw new IOException("Invalid CSV row " + (i + 1) + " in " + csvPath);
            }
            records.add(new CmeRecord(
                    row.get(0),
                    LocalDate.parse(row.get(1)),
                    row.get(2),
                    row.get(3),
                    row.get(4),
                    Double.parseDouble(row.get(5))));
        }
        return records;
    }

    public void save(List<CmeRecord> records) throws IOException {
        Files.createDirectories(csvPath.getParent());
        List<String> lines = new ArrayList<>();
        lines.add(HEADER);
        for (CmeRecord record : records) {
            lines.add(String.join(",",
                    escape(record.getId()),
                    escape(record.getDate().toString()),
                    escape(record.getTitle()),
                    escape(record.getCreditType()),
                    escape(record.getCategory()),
                    escape(Double.toString(record.getHours()))));
        }
        Files.write(csvPath, lines, StandardCharsets.UTF_8);
    }

    private static String escape(String value) {
        boolean quote = value.contains(",") || value.contains("\"") || value.contains("\n");
        String escaped = value.replace("\"", "\"\"");
        return quote ? "\"" + escaped + "\"" : escaped;
    }

    private static List<String> parseCsvLine(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean quoted = false;

        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (ch == '"') {
                if (quoted && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    quoted = !quoted;
                }
            } else if (ch == ',' && !quoted) {
                values.add(current.toString());
                current.setLength(0);
            } else {
                current.append(ch);
            }
        }
        values.add(current.toString());
        return values;
    }
}
