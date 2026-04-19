# CME Tracker

A small standalone Java desktop app for tracking continuing medical education credits.

This first version uses plain Java Swing and saves records to `data/cme-records.csv`, so it can run in VS Code without Maven, Gradle, or a separate database server.

## Run From VS Code Terminal

```bash
javac -d out $(find src -name '*.java')
mkdir -p out/cme/resources
cp src/cme/resources/* out/cme/resources/
java -cp out cme.CmeTrackerApp
```

## Build A Runnable JAR

```bash
javac -d out $(find src -name '*.java')
mkdir -p out/cme/resources
cp src/cme/resources/* out/cme/resources/
jar --create --file CMETracker.jar --main-class cme.CmeTrackerApp -C out .
java -jar CMETracker.jar
```

The app lets you:

- Add, edit, and delete CME entries.
- Track date, title, credit type, category, and credit hours.
- Pick dates from a built-in calendar for entries and report ranges.
- Filter the table and report by date range.
- Total hours by category and credit type for the current date range.
- Print a text report or save the report directly as a PDF file.
- Use a File menu with Exit.
- Open Help Contents and About CME Tracker from the Help menu.

## Notes

Categories are editable in the entry form. The default set is `1A`, `1B`, `2A`, and `2B`.

The CSV file is meant to be simple and portable. If this grows into a product, the next likely step is replacing the CSV store with an embedded database such as SQLite or H2.
