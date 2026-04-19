package cme;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class CmeReports {
    public static List<CmeRecord> filterByDate(List<CmeRecord> records, LocalDate start, LocalDate end) {
        return records.stream()
                .filter(record -> start == null || !record.getDate().isBefore(start))
                .filter(record -> end == null || !record.getDate().isAfter(end))
                .sorted(Comparator.comparing(CmeRecord::getDate).thenComparing(CmeRecord::getTitle))
                .toList();
    }

    public static Map<String, Double> totalHoursByCategory(List<CmeRecord> records) {
        return records.stream()
                .collect(Collectors.groupingBy(
                        CmeRecord::getCategory,
                        TreeMap::new,
                        Collectors.summingDouble(CmeRecord::getHours)));
    }

    public static Map<String, Double> totalHoursByCreditType(List<CmeRecord> records) {
        return records.stream()
                .collect(Collectors.groupingBy(
                        CmeRecord::getCreditType,
                        TreeMap::new,
                        Collectors.summingDouble(CmeRecord::getHours)));
    }

    public static double totalHours(List<CmeRecord> records) {
        return records.stream().mapToDouble(CmeRecord::getHours).sum();
    }

    public static String buildReport(List<CmeRecord> records, LocalDate start, LocalDate end) {
        StringBuilder report = new StringBuilder();
        report.append("CME Report\n");
        report.append("Date range: ")
                .append(start == null ? "Any" : start)
                .append(" through ")
                .append(end == null ? "Any" : end)
                .append("\n\n");

        report.append("Entries\n");
        for (CmeRecord record : records) {
            report.append(record.getDate())
                    .append(" | ")
                    .append(record.getCreditType())
                    .append(" | Category ")
                    .append(record.getCategory())
                    .append(" | ")
                    .append(String.format("%.2f", record.getHours()))
                    .append(" | ")
                    .append(record.getTitle())
                    .append("\n");
        }

        report.append("\nTotals by Category\n");
        totalHoursByCategory(records).forEach((category, hours) ->
                report.append("Category ")
                        .append(category)
                        .append(": ")
                        .append(String.format("%.2f", hours))
                        .append("\n"));

        report.append("\nTotals by Credit Type\n");
        totalHoursByCreditType(records).forEach((type, hours) ->
                report.append(type)
                        .append(": ")
                        .append(String.format("%.2f", hours))
                        .append("\n"));

        report.append("\nTotal hours: ").append(String.format("%.2f", totalHours(records))).append("\n");

        return report.toString();
    }
}
