package com.example.evalrh.service.pdf;

import com.example.evalrh.service.company.CompanyService;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.OutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols; // Import for DecimalFormatSymbols
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale; // Import for Locale
import java.util.Map;
import java.util.Optional;

@Service
public class SalarySlipPdfExporter {
    private final CompanyService companyService;

    public SalarySlipPdfExporter(CompanyService companyService) {
        this.companyService = companyService;
    }

    // --- Color Palette (Inspired by your CSS) ---
    private static final BaseColor COLOR_PRIMARY_TEXT = new BaseColor(0x43, 0x38, 0xCA); // --primary-700
    private static final BaseColor COLOR_HEADER_BG = new BaseColor(0xEE, 0xF2, 0xFF); // --primary-50
    private static final BaseColor COLOR_TABLE_HEADER_BG = new BaseColor(0xE0, 0xE7, 0xFF); // --primary-100
    private static final BaseColor COLOR_TEXT_NORMAL = new BaseColor(0x33, 0x41, 0x55); // --neutral-700
    private static final BaseColor COLOR_TEXT_MUTED = new BaseColor(0x64, 0x74, 0x8B); // --neutral-500
    private static final BaseColor COLOR_BORDER = new BaseColor(0xCB, 0xD5, 0xE1); // --neutral-300
    private static final BaseColor COLOR_WHITE = BaseColor.WHITE;

    // --- Fonts ---
    // Using Helvetica as a stand-in for Inter. For true Inter, you'd need to embed the font file.
    private static final Font FONT_DOCUMENT_TITLE = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, COLOR_PRIMARY_TEXT);
    private static final Font FONT_COMPANY_NAME = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, COLOR_PRIMARY_TEXT);
    private static final Font FONT_SECTION_TITLE = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, COLOR_TEXT_NORMAL);
    private static final Font FONT_LABEL_BOLD = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, COLOR_TEXT_NORMAL);
    private static final Font FONT_VALUE = FontFactory.getFont(FontFactory.HELVETICA, 9, COLOR_TEXT_NORMAL);
    private static final Font FONT_VALUE_BOLD = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, COLOR_TEXT_NORMAL);
    private static final Font FONT_TABLE_HEADER = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, COLOR_TEXT_NORMAL);
    private static final Font FONT_TABLE_CELL = FontFactory.getFont(FontFactory.HELVETICA, 9, COLOR_TEXT_NORMAL);
    private static final Font FONT_TOTAL_LABEL = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, COLOR_TEXT_NORMAL);
    private static final Font FONT_TOTAL_VALUE = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, COLOR_PRIMARY_TEXT);
    private static final Font FONT_NET_IN_WORDS_LABEL = FontFactory.getFont(FontFactory.HELVETICA, 8, COLOR_TEXT_MUTED);
    private static final Font FONT_NET_IN_WORDS_VALUE = FontFactory.getFont(FontFactory.HELVETICA, 9, COLOR_TEXT_NORMAL);


    // --- Currency Format Setup ---
    // Create DecimalFormatSymbols for French locale (or similar) to use space as grouping and comma as decimal
    private static final DecimalFormat CURRENCY_FORMAT;
    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.FRENCH); // Use a locale that uses space for grouping and comma for decimal
        symbols.setGroupingSeparator(' '); // Set grouping separator to space
        symbols.setDecimalSeparator(','); // Set decimal separator to comma
        CURRENCY_FORMAT = new DecimalFormat("#,##0.00", symbols); // Apply the custom symbols
    }

    private static final SimpleDateFormat DATE_FORMAT_INPUT = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat DATE_FORMAT_OUTPUT = new SimpleDateFormat("dd/MM/yyyy");

    @SuppressWarnings("unchecked")
    public void exportToPdf(Map<String, Object> salaryData, OutputStream outputStream) throws DocumentException, IOException {
        Map<String, Object> companyDetails = companyService.getCompanyDetails(getString(salaryData, "company", ""));
        Document document = new Document(PageSize.A4, 36, 36, 36, 36); // Margins: left, right, top, bottom
        PdfWriter.getInstance(document, outputStream);
        document.open();

        // --- Header Section ---
        addHeaderSection(document, companyDetails, salaryData);
        addSeparatorLine(document, 10f);


        // --- Employee & Period Information ---
        addEmployeeAndPeriodInfo(document, salaryData);
        document.add(Chunk.NEWLINE);


        // --- Gains (Earnings) ---
        Paragraph earningsTitle = new Paragraph("GAINS", FONT_SECTION_TITLE);
        earningsTitle.setSpacingBefore(10f);
        earningsTitle.setSpacingAfter(5f);
        document.add(earningsTitle);
        List<Map<String, Object>> earnings = (List<Map<String, Object>>) salaryData.get("earnings");
        PdfPTable earningsTable = createStyledEarningsDeductionsTable("Composant de salaire", "Montant", earnings, getString(salaryData, "currency", ""));
        document.add(earningsTable);
        document.add(Chunk.NEWLINE);


        // --- Déductions (Deductions) ---
        Paragraph deductionsTitle = new Paragraph("RETENUES", FONT_SECTION_TITLE);
        deductionsTitle.setSpacingBefore(10f);
        deductionsTitle.setSpacingAfter(5f);
        document.add(deductionsTitle);
        List<Map<String, Object>> deductions = (List<Map<String, Object>>) salaryData.get("deductions");
        PdfPTable deductionsTable = createStyledEarningsDeductionsTable("Composant de salaire", "Montant", deductions, getString(salaryData, "currency", ""));
        document.add(deductionsTable);
        document.add(Chunk.NEWLINE);


        // --- Totaux ---
        addTotalsSection(document, salaryData);
        addSeparatorLine(document, 15f);

        // --- Net Pay in Words ---
        addNetInWords(document, salaryData);

        document.close();
    }

    private void addHeaderSection(Document document, Map<String, Object> companyDetails, Map<String, Object> salaryData) throws DocumentException {
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new float[]{2, 3}); // Adjust widths as needed

        // Company Info Cell
        PdfPCell companyCell = new PdfPCell();
        companyCell.setBorder(Rectangle.NO_BORDER);
        companyCell.setPadding(5f);

        Paragraph companyName = new Paragraph(getString(companyDetails, "company_name", "N/A Company"), FONT_COMPANY_NAME);
        companyCell.addElement(companyName);

        // Add company address or other details if available and desired
        // Example: companyCell.addElement(new Paragraph("NIF: " + getString(companyDetails, "tax_id", ""), FONT_VALUE));
        //          companyCell.addElement(new Paragraph("STAT: " + getString(companyDetails, "stat_id", ""), FONT_VALUE));
        //          companyCell.addElement(new Paragraph("RCS: " + getString(companyDetails, "rcs_id", ""), FONT_VALUE));
        //          companyCell.addElement(new Paragraph(getString(companyDetails, "address_display", ""), FONT_VALUE)); // if you have an address field

        headerTable.addCell(companyCell);

        // Document Title and Period Cell
        PdfPCell titleCell = new PdfPCell();
        titleCell.setBorder(Rectangle.NO_BORDER);
        titleCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        titleCell.setVerticalAlignment(Element.ALIGN_TOP);
        titleCell.setPadding(5f);

        Paragraph docTitle = new Paragraph("FICHE DE PAIE", FONT_DOCUMENT_TITLE);
        docTitle.setAlignment(Element.ALIGN_RIGHT);
        titleCell.addElement(docTitle);

        String periode = "Période du " + formatDate(getString(salaryData, "start_date", "")) + " au " + formatDate(getString(salaryData, "end_date", ""));
        Paragraph periodText = new Paragraph(periode, FONT_VALUE);
        periodText.setAlignment(Element.ALIGN_RIGHT);
        periodText.setSpacingBefore(5f);
        titleCell.addElement(periodText);

        Paragraph emissionDateText = new Paragraph("Date d'émission: " + formatDate(getString(salaryData, "posting_date", "")), FONT_VALUE);
        emissionDateText.setAlignment(Element.ALIGN_RIGHT);
        titleCell.addElement(emissionDateText);


        headerTable.addCell(titleCell);
        document.add(headerTable);
    }

    private void addEmployeeAndPeriodInfo(Document document, Map<String, Object> salaryData) throws DocumentException {
        PdfPTable infoTable = new PdfPTable(2); // Two columns for side-by-side info
        infoTable.setWidthPercentage(100);
        infoTable.setWidths(new float[]{1, 1}); // Equal widths
        infoTable.setSpacingBefore(10f);

        // Left Column: Employee Details
        PdfPCell employeeDetailsCell = new PdfPCell();
        employeeDetailsCell.setBorder(Rectangle.NO_BORDER);
        employeeDetailsCell.setPadding(5f);

        addLabelAndValue(employeeDetailsCell, "Nom de l'employé:", getString(salaryData, "employee_name", ""));
        addLabelAndValue(employeeDetailsCell, "Matricule:", getString(salaryData, "employee", ""));
        // You can add more employee details here if available, e.g., Department, Designation

        infoTable.addCell(employeeDetailsCell);

        // Right Column: Payroll Details
        PdfPCell payrollDetailsCell = new PdfPCell();
        payrollDetailsCell.setBorder(Rectangle.NO_BORDER);
        payrollDetailsCell.setPadding(5f);
        payrollDetailsCell.setHorizontalAlignment(Element.ALIGN_LEFT); // Align content of this cell to left

        addLabelAndValue(payrollDetailsCell, "Fréquence de paie:", getString(salaryData, "payroll_frequency", ""));
        addLabelAndValue(payrollDetailsCell, "Devise:", getString(salaryData, "currency", ""));
        addLabelAndValue(payrollDetailsCell, "Jours travaillés:", getString(salaryData, "payment_days", "") + "/" + getString(salaryData, "total_working_days", ""));
        // Add more payroll specific info if needed

        infoTable.addCell(payrollDetailsCell);

        document.add(infoTable);
    }

    private void addLabelAndValue(PdfPCell containerCell, String label, String value) {
        PdfPTable innerTable = new PdfPTable(2);
        innerTable.setWidthPercentage(100);
        try {
            innerTable.setWidths(new float[]{1, 2}); // Adjust ratio as needed
        } catch (DocumentException e) { /* Should not happen with fixed columns */ }

        PdfPCell labelCell = new PdfPCell(new Phrase(label, FONT_LABEL_BOLD));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        labelCell.setPaddingBottom(4f);
        innerTable.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, FONT_VALUE));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        valueCell.setPaddingBottom(4f);
        innerTable.addCell(valueCell);

        containerCell.addElement(innerTable);
    }


    private PdfPTable createStyledEarningsDeductionsTable(String header1, String header2, List<Map<String, Object>> items, String currency) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{3, 1}); // Description wider than amount
        table.setSpacingBefore(5f);
        table.setHeaderRows(1); // Repeats header on new page

        // Header Cell 1
        PdfPCell cellHeader1 = new PdfPCell(new Phrase(header1, FONT_TABLE_HEADER));
        cellHeader1.setBackgroundColor(COLOR_TABLE_HEADER_BG);
        cellHeader1.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellHeader1.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cellHeader1.setPadding(8f);
        cellHeader1.setBorderColor(COLOR_BORDER);
        cellHeader1.setBorderWidth(0.5f);
        table.addCell(cellHeader1);

        // Header Cell 2
        PdfPCell cellHeader2 = new PdfPCell(new Phrase(header2 + " (" + currency + ")", FONT_TABLE_HEADER));
        cellHeader2.setBackgroundColor(COLOR_TABLE_HEADER_BG);
        cellHeader2.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cellHeader2.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cellHeader2.setPadding(8f);
        cellHeader2.setBorderColor(COLOR_BORDER);
        cellHeader2.setBorderWidth(0.5f);
        table.addCell(cellHeader2);

        if (items != null && !items.isEmpty()) {
            for (Map<String, Object> item : items) {
                addStyledTableCell(table, getString(item, "salary_component", "-"), FONT_TABLE_CELL, Element.ALIGN_LEFT);
                addStyledAmountCell(table, getDouble(item, "amount"), FONT_TABLE_CELL, Element.ALIGN_RIGHT);
            }
        } else {
            PdfPCell noDataCell = new PdfPCell(new Phrase("Aucune donnée disponible", FONT_TABLE_CELL));
            noDataCell.setColspan(2);
            noDataCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            noDataCell.setPadding(10f);
            noDataCell.setBorderColor(COLOR_BORDER);
            noDataCell.setBorderWidth(0.5f);
            table.addCell(noDataCell);
        }
        return table;
    }

    private void addStyledTableCell(PdfPTable table, String text, Font font, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(alignment);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(7f);
        cell.setBorderColor(COLOR_BORDER);
        cell.setBorderWidth(0.5f);
        table.addCell(cell);
    }

    private void addStyledAmountCell(PdfPTable table, Double amount, Font font, int alignment) {
        String formattedAmount = (amount != null) ? CURRENCY_FORMAT.format(amount) : "0,00"; // Changed default to "0,00"
        PdfPCell cell = new PdfPCell(new Phrase(formattedAmount, font));
        cell.setHorizontalAlignment(alignment);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(7f);
        cell.setBorderColor(COLOR_BORDER);
        cell.setBorderWidth(0.5f);
        table.addCell(cell);
    }

    private void addTotalsSection(Document document, Map<String, Object> salaryData) throws DocumentException {
        Paragraph totalsTitle = new Paragraph("RÉCAPITULATIF", FONT_SECTION_TITLE);
        totalsTitle.setSpacingBefore(10f);
        totalsTitle.setSpacingAfter(5f);
        totalsTitle.setAlignment(Element.ALIGN_RIGHT); // Align title to the right with the table
        document.add(totalsTitle);

        PdfPTable totalsTable = new PdfPTable(2);
        totalsTable.setWidthPercentage(50); // Or adjust as needed
        totalsTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totalsTable.setWidths(new float[]{2, 1.5f}); // Label, Value
        totalsTable.setSpacingBefore(5f);


        addStyledTotalRow(totalsTable, "Salaire Brut:", getDouble(salaryData, "gross_pay"), getString(salaryData, "currency", ""), FONT_TOTAL_LABEL, FONT_VALUE_BOLD);
        addStyledTotalRow(totalsTable, "Total Déductions:", getDouble(salaryData, "total_deduction"), getString(salaryData, "currency", ""), FONT_TOTAL_LABEL, FONT_VALUE_BOLD);
        addStyledTotalRow(totalsTable, "Salaire Net:", getDouble(salaryData, "net_pay"), getString(salaryData, "currency", ""), FONT_TOTAL_LABEL, FONT_TOTAL_VALUE, true);

        document.add(totalsTable);
    }

    private void addStyledTotalRow(PdfPTable table, String label, Double value, String currency, Font labelFont, Font valueFont) {
        addStyledTotalRow(table, label, value, currency, labelFont, valueFont, false);
    }
    private void addStyledTotalRow(PdfPTable table, String label, Double value, String currency, Font labelFont, Font valueFont, boolean isGrandTotal) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        labelCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPaddingRight(10f);
        labelCell.setPaddingTop(isGrandTotal ? 8f : 4f);
        labelCell.setPaddingBottom(isGrandTotal ? 8f : 4f);
        table.addCell(labelCell);

        String formattedValue = (value != null ? CURRENCY_FORMAT.format(value) : "0,00") + " " + currency; // Changed default to "0,00"
        PdfPCell valueCell = new PdfPCell(new Phrase(formattedValue, valueFont));
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        valueCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        valueCell.setBorder(Rectangle.NO_BORDER);
        if (isGrandTotal) {
            valueCell.setBorder(Rectangle.TOP | Rectangle.BOTTOM);
            valueCell.setBorderColor(COLOR_PRIMARY_TEXT);
            valueCell.setBorderWidth(0.5f);
        }
        valueCell.setPaddingTop(isGrandTotal ? 8f : 4f);
        valueCell.setPaddingBottom(isGrandTotal ? 8f : 4f);
        table.addCell(valueCell);
    }
    private void addNetInWords(Document document, Map<String, Object> salaryData) throws DocumentException {
        Paragraph label = new Paragraph("Montant Net en Toutes Lettres:", FONT_NET_IN_WORDS_LABEL);
        label.setSpacingBefore(10f);
        document.add(label);

        Paragraph value = new Paragraph(getString(salaryData, "total_in_words", ""), FONT_NET_IN_WORDS_VALUE);
        document.add(value);
    }


    private void addSeparatorLine(Document document, float spacingBefore) throws DocumentException {
        Paragraph p = new Paragraph();
        p.setSpacingBefore(spacingBefore);
        document.add(p); // Add some space before the line

        PdfPTable lineTable = new PdfPTable(1);
        lineTable.setWidthPercentage(100);
        PdfPCell lineCell = new PdfPCell();
        lineCell.setBorder(Rectangle.BOTTOM); // Only bottom border
        lineCell.setBorderColor(COLOR_BORDER);
        lineCell.setBorderWidth(0.5f);
        lineCell.setFixedHeight(1f); // Make the cell very short, so only border is visible
        lineTable.addCell(lineCell);
        document.add(lineTable);
        // document.add(Chunk.NEWLINE); // Add space after the line
    }


    // --- Utility Methods (getString, getDouble, formatDate) ---
    // These should be mostly the same, ensure they handle nulls gracefully.

    private String getString(Map<String, Object> data, String key, String defaultValue) {
        return Optional.ofNullable(data)
                .map(d -> d.get(key))
                .map(Object::toString)
                .filter(s -> !s.isEmpty())
                .orElse(defaultValue);
    }

    private Double getDouble(Map<String, Object> data, String key) {
        if (data == null) return 0.0;
        Object val = data.get(key);
        if (val instanceof Number) {
            return ((Number) val).doubleValue();
        }
        try {
            return Optional.ofNullable(val).map(v -> Double.parseDouble(v.toString())).orElse(0.0);
        } catch (NumberFormatException e) { // Catch specifically NumberFormatException
            System.err.println("Warning: Could not parse double for key '" + key + "', value: '" + val + "'. Defaulting to 0.0.");
            return 0.0;
        }
    }

    private String formatDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return "N/A";
        }
        try {
            Date date = DATE_FORMAT_INPUT.parse(dateStr);
            return DATE_FORMAT_OUTPUT.format(date);
        } catch (ParseException e) {
            return dateStr; // Return original if parsing fails
        }
    }

}