package cme;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public class CmeRecord {
    private final String id;
    private LocalDate date;
    private String title;
    private String creditType;
    private String category;
    private double hours;

    public CmeRecord(LocalDate date, String title, String creditType, String category, double hours) {
        this(UUID.randomUUID().toString(), date, title, creditType, category, hours);
    }

    public CmeRecord(String id, LocalDate date, String title, String creditType, String category, double hours) {
        this.id = Objects.requireNonNull(id);
        this.date = Objects.requireNonNull(date);
        this.title = requireText(title, "title");
        this.creditType = requireText(creditType, "credit type");
        this.category = requireText(category, "category");
        setHours(hours);
    }

    public String getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = Objects.requireNonNull(date);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = requireText(title, "title");
    }

    public String getCreditType() {
        return creditType;
    }

    public void setCreditType(String creditType) {
        this.creditType = requireText(creditType, "credit type");
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = requireText(category, "category");
    }

    public double getHours() {
        return hours;
    }

    public void setHours(double hours) {
        if (hours <= 0) {
            throw new IllegalArgumentException("Hours must be greater than zero.");
        }
        this.hours = hours;
    }

    private static String requireText(String value, String label) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("The " + label + " field is required.");
        }
        return value.trim();
    }
}
