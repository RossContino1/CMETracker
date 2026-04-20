# CME Tracker

<p align="center">
  <img src="caduceus-icon.png" alt="CME Tracker Icon" width="200"/>
</p>

<p align="center">
  <b>A lightweight desktop app for tracking Continuing Medical Education (CME) credits.</b>
</p>

<p align="center">
  <a href="https://bytesbreadbbq.com/cmetracker">🌐 Website</a> •
  <a href="https://github.com/RossContino1/CMETracker/releases">⬇️ Download</a>
</p>

---

## 🚀 Quick Start (Linux AppImage)

1. Download `CMETracker.zip` from Releases
2. Extract the zip
3. Run:

```bash
chmod +x install.sh
./install.sh
```

Then launch **CME Tracker** from your application menu.

---

## 💡 Features

* Add, edit, and delete CME entries
* Track:

  * Date
  * Title
  * Credit type
  * Category
  * Credit hours
* Built-in calendar date picker
* Filter by date range
* Totals by category and credit type
* Generate reports:

  * Print text report
  * Export to PDF
* Simple, portable CSV storage (auto-created on first use)

---

## 🌐 Website

Full details and downloads:

👉 https://bytesbreadbbq.com/cme-tracking-app-for-doctors/

---

## 💻 Development / Run From Source

```bash
javac -d out $(find src -name '*.java')
mkdir -p out/cme/resources
cp src/cme/resources/* out/cme/resources/
java -cp out cme.CmeTrackerApp
```

---

## 📦 Build Runnable JAR

```bash
javac -d out $(find src -name '*.java')
mkdir -p out/cme/resources
cp src/cme/resources/* out/cme/resources/
jar --create --file CMETracker.jar --main-class cme.CmeTrackerApp -C out .
java -jar CMETracker.jar
```

---

## 📝 Notes

* Data is stored in a simple CSV file created automatically on first use
* Default categories: `1A`, `1B`, `2A`, `2B`
* Designed to be lightweight with no database required

---

## 🔮 Future Improvements

* Optional database backend (SQLite or H2)
* Cross-platform installers (Windows / macOS)
* Cloud sync (maybe 😉)

---

## 📄 License

MIT License — see `LICENSE.txt`

---

