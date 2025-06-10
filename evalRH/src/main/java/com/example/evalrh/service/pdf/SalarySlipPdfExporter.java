package com.example.evalrh.service.pdf;

import com.example.evalrh.service.company.CompanyService;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import org.springframework.stereotype.Service;

import java.io.OutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Service
public class SalarySlipPdfExporter {
    private final CompanyService companyService;

    public SalarySlipPdfExporter(CompanyService companyService) {
        this.companyService = companyService;
    }

    // --- Palette de Couleurs (Inchangée) ---
    private static final BaseColor COLOR_PRIMARY = new BaseColor(0x43, 0x38, 0xCA); // --primary-700
    private static final BaseColor COLOR_PRIMARY_LIGHT = new BaseColor(0xEE, 0xF2, 0xFF); // --primary-50
    private static final BaseColor COLOR_TABLE_HEADER_BG = new BaseColor(0xE0, 0xE7, 0xFF); // --primary-100
    private static final BaseColor COLOR_TABLE_ROW_ALT_BG = new BaseColor(0xF8, 0xF9, 0xFA); // MODIFIÉ: Encore plus subtil
    private static final BaseColor COLOR_TEXT_NORMAL = new BaseColor(0x33, 0x41, 0x55);
    private static final BaseColor COLOR_TEXT_MUTED = new BaseColor(0x64, 0x74, 0x8B);
    private static final BaseColor COLOR_BORDER = new BaseColor(0xDE, 0xE2, 0xE6);
    private static final BaseColor COLOR_WHITE = BaseColor.WHITE;

    // --- Polices (Hiérarchie renforcée) ---
    private static final Font FONT_DOCUMENT_TITLE = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 26, COLOR_TEXT_NORMAL);
    private static final Font FONT_COMPANY_NAME = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, COLOR_PRIMARY);
    private static final Font FONT_COMPANY_INFO = FontFactory.getFont(FontFactory.HELVETICA, 9, COLOR_TEXT_MUTED);
    private static final Font FONT_SECTION_TITLE = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, COLOR_TEXT_NORMAL);
    private static final Font FONT_LABEL_UPPERCASE = FontFactory.getFont(FontFactory.HELVETICA, 8, COLOR_TEXT_MUTED); // NOUVEAU
    private static final Font FONT_VALUE = FontFactory.getFont(FontFactory.HELVETICA, 9, COLOR_TEXT_NORMAL);
    private static final Font FONT_TABLE_HEADER = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, COLOR_TEXT_NORMAL);
    private static final Font FONT_TABLE_CELL = FontFactory.getFont(FontFactory.HELVETICA, 9, COLOR_TEXT_NORMAL);
    private static final Font FONT_TOTAL_LABEL = FontFactory.getFont(FontFactory.HELVETICA, 10, COLOR_TEXT_MUTED);
    private static final Font FONT_TOTAL_VALUE = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, COLOR_TEXT_NORMAL);
    private static final Font FONT_GRAND_TOTAL_LABEL = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, COLOR_PRIMARY);
    private static final Font FONT_GRAND_TOTAL_VALUE = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, COLOR_PRIMARY);
    private static final Font FONT_NET_IN_WORDS = FontFactory.getFont(FontFactory.HELVETICA, 9, COLOR_TEXT_MUTED);
    private static final Font FONT_FOOTER = FontFactory.getFont(FontFactory.HELVETICA, 8, COLOR_TEXT_MUTED);

    private static final DecimalFormat CURRENCY_FORMAT;
    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.FRENCH);
        symbols.setGroupingSeparator(' ');
        symbols.setDecimalSeparator(',');
        CURRENCY_FORMAT = new DecimalFormat("#,##0.00", symbols);
    }
    private static final SimpleDateFormat DATE_FORMAT_INPUT = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat DATE_FORMAT_OUTPUT = new SimpleDateFormat("dd MMMM yyyy", Locale.FRENCH);


    @SuppressWarnings("unchecked")
    public void exportToPdf(Map<String, Object> salaryData, OutputStream outputStream) throws DocumentException, IOException {
        Map<String, Object> companyDetails = companyService.getCompanyDetails(getString(salaryData, "company", ""));

        // MODIFIÉ: Augmentation de la marge gauche pour la bande de couleur
        Document document = new Document(PageSize.A4, 48, 36, 36, 36);
        PdfWriter writer = PdfWriter.getInstance(document, outputStream);

        // NOUVEAU: Événement pour la bande latérale et le pied de page
        ModernPageEventHelper eventHelper = new ModernPageEventHelper(COLOR_PRIMARY);
        writer.setPageEvent(eventHelper);

        document.open();

        addHeaderSection(document, companyDetails, salaryData);
        document.add(new Paragraph("\n"));

        addEmployeeAndPeriodInfo(document, salaryData);

        document.add(createSectionTitle("DÉTAIL DES GAINS"));
        List<Map<String, Object>> earnings = (List<Map<String, Object>>) salaryData.get("earnings");
        document.add(createModernStyledTable(earnings, getString(salaryData, "currency", "")));

        document.add(createSectionTitle("DÉTAIL DES RETENUES"));
        List<Map<String, Object>> deductions = (List<Map<String, Object>>) salaryData.get("deductions");
        document.add(createModernStyledTable(deductions, getString(salaryData, "currency", "")));

        addTotalsSection(document, salaryData);

        document.close();
    }

    // MODIFIÉ: En-tête sans logo, avec une forte hiérarchie typographique
    private void addHeaderSection(Document document, Map<String, Object> companyDetails, Map<String, Object> salaryData) throws DocumentException {
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new float[]{3, 2});

        // Cellule de gauche : Titre et infos entreprise
        PdfPCell leftCell = new PdfPCell();
        leftCell.setBorder(Rectangle.NO_BORDER);
        leftCell.setPaddingRight(20f);

        Paragraph docTitle = new Paragraph("Fiche de Paie", FONT_DOCUMENT_TITLE);
        leftCell.addElement(docTitle);

        Paragraph companyName = new Paragraph(getString(companyDetails, "company_name", "Nom de l'entreprise"), FONT_COMPANY_NAME);
        companyName.setSpacingBefore(10f);
        leftCell.addElement(companyName);

        Paragraph companyInfo = new Paragraph();
        leftCell.addElement(companyInfo);

        // Cellule de droite : Période et dates
        PdfPCell rightCell = new PdfPCell();
        rightCell.setBorder(Rectangle.NO_BORDER);
        rightCell.setHorizontalAlignment(Element.ALIGN_RIGHT);

        String periode = "Période du " + formatDate(getString(salaryData, "start_date", "")) + " au " + formatDate(getString(salaryData, "end_date", ""));
        rightCell.addElement(new Paragraph(periode, FONT_VALUE));
        rightCell.addElement(new Paragraph("Émis le: " + formatDate(getString(salaryData, "posting_date", "")), FONT_COMPANY_INFO));

        headerTable.addCell(leftCell);
        headerTable.addCell(rightCell);
        document.add(headerTable);
    }

    // MODIFIÉ: Utilisation des nouveaux styles de police pour les labels
    private void addEmployeeAndPeriodInfo(Document document, Map<String, Object> salaryData) throws DocumentException {
        PdfPTable infoTable = new PdfPTable(2);
        infoTable.setWidthPercentage(100);
        infoTable.setSpacingBefore(15f);
        infoTable.setSpacingAfter(20f);

        // Colonne de gauche
        PdfPCell leftCol = new PdfPCell();
        leftCol.setBorder(Rectangle.NO_BORDER);
        addLabelValue(leftCol, "EMPLOYÉ", getString(salaryData, "employee_name", ""));
        addLabelValue(leftCol, "MATRICULE", getString(salaryData, "employee", ""));
        addLabelValue(leftCol, "POSTE", getString(salaryData, "designation", "N/A"));

        // Colonne de droite
        PdfPCell rightCol = new PdfPCell();
        rightCol.setBorder(Rectangle.NO_BORDER);
        addLabelValue(rightCol, "FRÉQUENCE DE PAIE", getString(salaryData, "payroll_frequency", ""));
        addLabelValue(rightCol, "JOURS TRAVAILLÉS", getString(salaryData, "payment_days", "") + " / " + getString(salaryData, "total_working_days", ""));
        addLabelValue(rightCol, "DEVISE", getString(salaryData, "currency", ""));

        infoTable.addCell(leftCol);
        infoTable.addCell(rightCol);
        document.add(infoTable);
    }

    // NOUVEAU: Helper pour le pattern "Label en majuscule / Valeur"
    private void addLabelValue(PdfPCell container, String label, String value) {
        Paragraph p = new Paragraph();
        p.add(new Chunk(label + "\n", FONT_LABEL_UPPERCASE));
        p.add(new Chunk(value, FONT_VALUE));
        p.setSpacingAfter(8f);
        container.addElement(p);
    }

    private Paragraph createSectionTitle(String title) {
        Paragraph p = new Paragraph(title, FONT_SECTION_TITLE);
        p.setSpacingBefore(10f);
        p.setSpacingAfter(10f);
        return p;
    }

    // NOUVEAU: Table de style moderne (ligne de séparation au lieu de fond)
    private PdfPTable createModernStyledTable(List<Map<String, Object>> items, String currency) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{3, 1});
        table.setHeaderRows(1);
        table.setSpacingAfter(20f);

        // En-têtes avec bordure inférieure
        PdfPCell header1 = new PdfPCell(new Phrase("COMPOSANT DE SALAIRE", FONT_TABLE_HEADER));
        header1.setBorder(Rectangle.BOTTOM);
        header1.setBorderColor(COLOR_PRIMARY);
        header1.setBorderWidth(1.5f);
        header1.setPadding(10f);
        table.addCell(header1);

        PdfPCell header2 = new PdfPCell(new Phrase("MONTANT (" + currency + ")", FONT_TABLE_HEADER));
        header2.setBorder(Rectangle.BOTTOM);
        header2.setBorderColor(COLOR_PRIMARY);
        header2.setBorderWidth(1.5f);
        header2.setHorizontalAlignment(Element.ALIGN_RIGHT);
        header2.setPadding(10f);
        table.addCell(header2);

        if (items != null && !items.isEmpty()) {
            boolean alternate = false;
            for (Map<String, Object> item : items) {
                table.addCell(createDataCell(getString(item, "salary_component", "-"), Element.ALIGN_LEFT, alternate));
                table.addCell(createAmountCell(getDouble(item, "amount"), alternate));
                alternate = !alternate;
            }
        } else { /* ... no data cell ... */ }

        return table;
    }

    private PdfPCell createDataCell(String text, int alignment, boolean alternate) {
        PdfPCell cell = new PdfPCell(new Phrase(text, FONT_TABLE_CELL));
        cell.setHorizontalAlignment(alignment);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(8f);
        cell.setBorder(Rectangle.NO_BORDER);
        if (alternate) cell.setBackgroundColor(COLOR_TABLE_ROW_ALT_BG);
        return cell;
    }

    private PdfPCell createAmountCell(Double amount, boolean alternate) {
        String formattedAmount = (amount != null) ? CURRENCY_FORMAT.format(amount) : "0,00";
        PdfPCell cell = createDataCell(formattedAmount, Element.ALIGN_RIGHT, alternate);
        return cell;
    }

    // MODIFIÉ: Section des totaux avec le nouveau design "carte"
    private void addTotalsSection(Document document, Map<String, Object> salaryData) throws DocumentException {
        PdfPTable mainContainer = new PdfPTable(1);
        mainContainer.setWidthPercentage(100);
        mainContainer.setSpacingBefore(15f);

        PdfPTable contentTable = new PdfPTable(2);
        contentTable.setWidthPercentage(55); // La carte prend 55%
        contentTable.setWidths(new float[]{2, 1.5f});
        contentTable.setHorizontalAlignment(Element.ALIGN_RIGHT);

        String currency = getString(salaryData, "currency", "");

        addTotalRow(contentTable, "Salaire Brut:", getDouble(salaryData, "gross_pay"), currency, FONT_TOTAL_LABEL, FONT_TOTAL_VALUE);
        addTotalRow(contentTable, "Total Retenues:", getDouble(salaryData, "total_deduction"), currency, FONT_TOTAL_LABEL, FONT_TOTAL_VALUE);

        addGrandTotalRow(contentTable, "SALAIRE NET À PAYER:", getDouble(salaryData, "net_pay"), currency, FONT_GRAND_TOTAL_LABEL, FONT_GRAND_TOTAL_VALUE);

        PdfPCell containerCell = new PdfPCell(contentTable);
        containerCell.setBorder(Rectangle.NO_BORDER);
        containerCell.setPadding(0); // Le padding est géré par l'événement
        containerCell.setCellEvent(new LeftBorderedRoundedCellEvent(COLOR_PRIMARY));

        mainContainer.addCell(containerCell);
        document.add(mainContainer);

        addNetInWords(document, salaryData);
    }

    private void addTotalRow(PdfPTable table, String label, Double value, String currency, Font labelFont, Font valueFont) {
        // ... (code inchangé, mais on ajoute du padding)
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        labelCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPadding(10f);
        table.addCell(labelCell);

        String formattedValue = (value != null ? CURRENCY_FORMAT.format(value) : "0,00");
        PdfPCell valueCell = new PdfPCell(new Phrase(formattedValue, valueFont));
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        valueCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setPadding(10f);
        table.addCell(valueCell);
    }

    // NOUVEAU: Ligne spéciale pour le grand total avec fond
    private void addGrandTotalRow(PdfPTable table, String label, Double value, String currency, Font labelFont, Font valueFont) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        labelCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setBackgroundColor(COLOR_PRIMARY_LIGHT);
        labelCell.setPaddingTop(15f);
        labelCell.setPaddingBottom(15f);
        labelCell.setPaddingRight(10f);
        table.addCell(labelCell);

        String formattedValue = (value != null ? CURRENCY_FORMAT.format(value) : "0,00") + " " + currency;
        PdfPCell valueCell = new PdfPCell(new Phrase(formattedValue, valueFont));
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        valueCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setBackgroundColor(COLOR_PRIMARY_LIGHT);
        valueCell.setPaddingTop(15f);
        valueCell.setPaddingBottom(15f);
        valueCell.setPaddingRight(10f);
        table.addCell(valueCell);
    }

    private void addNetInWords(Document document, Map<String, Object> salaryData) throws DocumentException {
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(55); // Aligné sur la carte des totaux
        table.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.setSpacingBefore(5f);

        Paragraph p = new Paragraph("Arrêté à la somme de : " + getString(salaryData, "total_in_words", ""), FONT_NET_IN_WORDS);

        PdfPCell cell = new PdfPCell(p);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cell.setPaddingRight(10f); // Petit décalage
        table.addCell(cell);

        document.add(table);
    }

    // --- Utilitaires (inchangés) ---
    private String getString(Map<String, Object> d, String k, String def) { return Optional.ofNullable(d).map(m->m.get(k)).map(Object::toString).filter(s->!s.isEmpty()).orElse(def); }
    private Double getDouble(Map<String, Object> d, String k) { if(d==null)return 0.0; Object v=d.get(k); if(v instanceof Number)return ((Number)v).doubleValue(); try{return Optional.ofNullable(v).map(o->Double.parseDouble(o.toString())).orElse(0.0);}catch(Exception e){return 0.0;} }
    private String formatDate(String ds) { if(ds==null||ds.isEmpty())return "N/A"; try{Date d=DATE_FORMAT_INPUT.parse(ds);return DATE_FORMAT_OUTPUT.format(d);}catch(ParseException e){return ds;} }
}


// NOUVEAU: Page Event Helper pour la bande latérale et le pied de page
class ModernPageEventHelper extends PdfPageEventHelper {
    private BaseColor sideBarColor;
    private Font footerFont = FontFactory.getFont(FontFactory.HELVETICA, 8, Font.NORMAL, new BaseColor(0x64, 0x74, 0x8B));

    public ModernPageEventHelper(BaseColor sideBarColor) {
        this.sideBarColor = sideBarColor;
    }

    @Override
    public void onEndPage(PdfWriter writer, Document document) {
        PdfContentByte canvas = writer.getDirectContent();

        // 1. Dessiner la bande latérale
        canvas.saveState();
        canvas.setColorFill(sideBarColor);
        canvas.rectangle(0, 0, 20, document.getPageSize().getHeight()); // Bande de 20pt de large
        canvas.fill();
        canvas.restoreState();

        // 2. Dessiner le pied de page
        try {
            PdfPTable footer = new PdfPTable(1);
            footer.setTotalWidth(document.right() - document.left());
            footer.setLockedWidth(true);

            PdfPCell cell = new PdfPCell(new Phrase(String.format("Page %d | Document confidentiel généré par Mahery", writer.getPageNumber()), footerFont));
            cell.setBorder(Rectangle.TOP);
            cell.setBorderColor(new BaseColor(0xDE, 0xE2, 0xE6));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPaddingTop(10f);
            footer.addCell(cell);

            footer.writeSelectedRows(0, -1, document.leftMargin(), document.bottom() - 10, writer.getDirectContent());
        } catch(Exception e) {
            throw new ExceptionConverter(e);
        }
    }
}


// NOUVEAU: Cell Event pour la carte des totaux avec bordure gauche
class LeftBorderedRoundedCellEvent implements PdfPCellEvent {
    private BaseColor borderColor;

    public LeftBorderedRoundedCellEvent(BaseColor borderColor) {
        this.borderColor = borderColor;
    }

    @Override
    public void cellLayout(PdfPCell cell, Rectangle position, PdfContentByte[] canvases) {
        PdfContentByte canvas = canvases[PdfPTable.BACKGROUNDCANVAS];
        float radius = 8f;
        float padding = 2f; // Petit espace pour éviter que le contenu ne touche le bord

        // Fond blanc de la carte
        canvas.saveState();
        canvas.setColorFill(BaseColor.WHITE);
        canvas.roundRectangle(
                position.getLeft() + padding,
                position.getBottom() + padding,
                position.getWidth() - (2 * padding),
                position.getHeight() - (2 * padding),
                radius
        );
        canvas.fill();

        // Bordure gauche colorée
        canvas.setColorFill(this.borderColor);
        canvas.rectangle(
                position.getLeft() + padding,
                position.getBottom() + padding,
                5f, // Épaisseur de la bordure
                position.getHeight() - (2 * padding)
        );
        canvas.fill();
        canvas.restoreState();

        // (Optionnel) Ajoute une ombre légère pour un effet de profondeur
        PdfContentByte foreground = canvases[PdfPTable.BACKGROUNDCANVAS];
        foreground.saveState();
        foreground.setColorStroke(new BaseColor(0, 0, 0, 30)); // Ombre noire transparente
        foreground.setLineWidth(1f);
        foreground.roundRectangle(
                position.getLeft() + padding + 1,
                position.getBottom() + padding -1,
                position.getWidth() - (2 * padding),
                position.getHeight() - (2 * padding),
                radius
        );
        foreground.stroke();
        foreground.restoreState();
    }
}