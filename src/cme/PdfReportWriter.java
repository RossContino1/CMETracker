package cme;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class PdfReportWriter {
    private static final int PAGE_WIDTH = 612;
    private static final int PAGE_HEIGHT = 792;
    private static final int LEFT_MARGIN = 54;
    private static final int TOP_Y = 738;
    private static final int FONT_SIZE = 11;
    private static final int LINE_HEIGHT = 14;
    private static final int MAX_LINES_PER_PAGE = 49;
    private static final int MAX_CHARS_PER_LINE = 92;

    private PdfReportWriter() {
    }

    public static void writeReport(Path pdfPath, String reportText) throws IOException {
        List<List<String>> pages = paginate(wrapLines(reportText));
        List<byte[]> objects = new ArrayList<>();
        int pageCount = pages.size();
        int pagesObjectNumber = 2;
        int fontObjectNumber = 3 + pageCount * 2;

        objects.add("<< /Type /Catalog /Pages 2 0 R >>".getBytes(StandardCharsets.ISO_8859_1));

        StringBuilder kids = new StringBuilder();
        for (int i = 0; i < pageCount; i++) {
            kids.append(3 + i * 2).append(" 0 R ");
        }
        objects.add(("<< /Type /Pages /Kids [" + kids + "] /Count " + pageCount + " >>")
                .getBytes(StandardCharsets.ISO_8859_1));

        for (int i = 0; i < pageCount; i++) {
            int pageObjectNumber = 3 + i * 2;
            int contentObjectNumber = pageObjectNumber + 1;
            objects.add(("<< /Type /Page /Parent " + pagesObjectNumber
                    + " 0 R /MediaBox [0 0 " + PAGE_WIDTH + " " + PAGE_HEIGHT
                    + "] /Resources << /Font << /F1 " + fontObjectNumber
                    + " 0 R >> >> /Contents " + contentObjectNumber + " 0 R >>")
                    .getBytes(StandardCharsets.ISO_8859_1));

            byte[] stream = buildContentStream(pages.get(i));
            objects.add(("<< /Length " + stream.length + " >>\nstream\n").getBytes(StandardCharsets.ISO_8859_1));
            objects.set(objects.size() - 1, join(objects.get(objects.size() - 1), stream,
                    "\nendstream".getBytes(StandardCharsets.ISO_8859_1)));
        }

        objects.add("<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>"
                .getBytes(StandardCharsets.ISO_8859_1));

        Files.write(pdfPath, buildPdf(objects));
    }

    private static List<String> wrapLines(String text) {
        List<String> wrapped = new ArrayList<>();
        for (String line : text.split("\\R", -1)) {
            if (line.length() <= MAX_CHARS_PER_LINE) {
                wrapped.add(line);
                continue;
            }

            String remaining = line;
            while (remaining.length() > MAX_CHARS_PER_LINE) {
                int breakAt = remaining.lastIndexOf(' ', MAX_CHARS_PER_LINE);
                if (breakAt < 1) {
                    breakAt = MAX_CHARS_PER_LINE;
                }
                wrapped.add(remaining.substring(0, breakAt));
                remaining = remaining.substring(breakAt).stripLeading();
            }
            wrapped.add(remaining);
        }
        return wrapped;
    }

    private static List<List<String>> paginate(List<String> lines) {
        List<List<String>> pages = new ArrayList<>();
        for (int i = 0; i < lines.size(); i += MAX_LINES_PER_PAGE) {
            pages.add(lines.subList(i, Math.min(i + MAX_LINES_PER_PAGE, lines.size())));
        }
        if (pages.isEmpty()) {
            pages.add(List.of(""));
        }
        return pages;
    }

    private static byte[] buildContentStream(List<String> lines) {
        StringBuilder content = new StringBuilder();
        content.append("BT\n");
        content.append("/F1 ").append(FONT_SIZE).append(" Tf\n");
        content.append(LEFT_MARGIN).append(" ").append(TOP_Y).append(" Td\n");
        content.append(LINE_HEIGHT).append(" TL\n");
        for (String line : lines) {
            content.append("(").append(escapePdfString(line)).append(") Tj\n");
            content.append("T*\n");
        }
        content.append("ET");
        return content.toString().getBytes(StandardCharsets.ISO_8859_1);
    }

    private static byte[] buildPdf(List<byte[]> objects) throws IOException {
        ByteArrayOutputStream pdf = new ByteArrayOutputStream();
        List<Integer> offsets = new ArrayList<>();
        write(pdf, "%PDF-1.4\n");

        for (int i = 0; i < objects.size(); i++) {
            offsets.add(pdf.size());
            write(pdf, (i + 1) + " 0 obj\n");
            pdf.write(objects.get(i));
            write(pdf, "\nendobj\n");
        }

        int xrefStart = pdf.size();
        write(pdf, "xref\n");
        write(pdf, "0 " + (objects.size() + 1) + "\n");
        write(pdf, "0000000000 65535 f \n");
        for (int offset : offsets) {
            write(pdf, String.format("%010d 00000 n \n", offset));
        }
        write(pdf, "trailer\n");
        write(pdf, "<< /Size " + (objects.size() + 1) + " /Root 1 0 R >>\n");
        write(pdf, "startxref\n");
        write(pdf, Integer.toString(xrefStart));
        write(pdf, "\n%%EOF\n");
        return pdf.toByteArray();
    }

    private static String escapePdfString(String value) {
        return value.replace("\\", "\\\\")
                .replace("(", "\\(")
                .replace(")", "\\)")
                .chars()
                .mapToObj(ch -> ch > 255 ? "?" : Character.toString((char) ch))
                .reduce("", String::concat);
    }

    private static byte[] join(byte[] first, byte[] second, byte[] third) {
        byte[] joined = new byte[first.length + second.length + third.length];
        System.arraycopy(first, 0, joined, 0, first.length);
        System.arraycopy(second, 0, joined, first.length, second.length);
        System.arraycopy(third, 0, joined, first.length + second.length, third.length);
        return joined;
    }

    private static void write(ByteArrayOutputStream output, String text) throws IOException {
        output.write(text.getBytes(StandardCharsets.ISO_8859_1));
    }
}
