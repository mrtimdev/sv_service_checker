package timdev.timdev.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ConditionalFormattingRule;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.PatternFormatting;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.SheetConditionalFormatting;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import timdev.timdev.entity.ServiceChecker;
import timdev.timdev.entity.ServiceCheckerItem;
import timdev.timdev.entity.ServiceCheckerItemNote;

@Service
@RequiredArgsConstructor
public class ExcelExportService {

    public ByteArrayInputStream exportToExcel(List<ServiceChecker> data) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            
            // Create detailed sheet for items and notes
            Sheet detailsSheet = workbook.createSheet("Service Checker Details");
            createDetailsSheetGroupedByCategory(detailsSheet, data);
            
            // Auto-size columns
            for (int i = 0; i < 8; i++) {
                detailsSheet.autoSizeColumn(i);
            }
            
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }
    
    private void createDetailsSheetGroupedByCategory(Sheet sheet, List<ServiceChecker> data) {
    // Create header style
    CellStyle headerStyle = createCategoryHeaderStyle(sheet.getWorkbook());
    CellStyle categoryHeaderStyle = createCategoryHeaderStyle(sheet.getWorkbook());
    
    // Headers
    String[] detailHeaders = {
        "Service ID", "Date", "Driver", "Category", 
        "Item Name", "Item Khmer Name", "Status", "Note"
    };
    
    Row headerRow = sheet.createRow(0);
    for (int col = 0; col < detailHeaders.length; col++) {
        Cell cell = headerRow.createCell(col);
        cell.setCellValue(detailHeaders[col]);
        cell.setCellStyle(headerStyle);
    }
    
    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    int rowIdx = 1;
    
    for (ServiceChecker sc : data) {
        String serviceInfo = "SC-" + sc.getId();
        String driverInfo = sc.getDriver().getFirstName() + " " + sc.getDriver().getLastName() + 
                          " (" + sc.getDriver().getPlateNumber() + ")";
        
        if (sc.getItems() != null && !sc.getItems().isEmpty()) {
            for (ServiceCheckerItem item : sc.getItems()) {
                if(item.getCategory() == null) {
                    continue; // Skip items without a category
                }
                String categoryName = item.getCategory() != null ? 
                                    item.getCategory().getName() : "N/A";
                
                // Create category header row
                Row categoryRow = sheet.createRow(rowIdx++);
                for (int i = 0; i < 8; i++) {
                    Cell cell = categoryRow.createCell(i);
                    if (i == 3) { // Category column
                        cell.setCellValue(categoryName);
                    }
                    cell.setCellStyle(categoryHeaderStyle);
                }
                
                if (item.getNotes() != null && !item.getNotes().isEmpty()) {
                    for (ServiceCheckerItemNote note : item.getNotes()) {
                        Row row = sheet.createRow(rowIdx++);
                        
                        row.createCell(0).setCellValue(serviceInfo);
                        row.createCell(1).setCellValue(sc.getDate().format(dateFormatter));
                        row.createCell(2).setCellValue(driverInfo);
                        row.createCell(3).setCellValue(categoryName);
                        
                        if (note.getInspectionItem() != null) {
                            row.createCell(4).setCellValue(note.getInspectionItem().getName());
                            row.createCell(5).setCellValue(note.getInspectionItem().getKhmerName());
                        } else {
                            row.createCell(4).setCellValue("N/A");
                            row.createCell(5).setCellValue("N/A");
                        }
                        
                        row.createCell(6).setCellValue(note.isPassed() ? "PASS" : "FAIL");
                        row.createCell(7).setCellValue(note.getNote() != null ? note.getNote() : "");
                    }
                } else {
                    Row row = sheet.createRow(rowIdx++);
                    row.createCell(0).setCellValue(serviceInfo);
                    row.createCell(1).setCellValue(sc.getDate().format(dateFormatter));
                    row.createCell(2).setCellValue(driverInfo);
                    row.createCell(3).setCellValue(categoryName);
                    row.createCell(4).setCellValue("No inspection items");
                    row.createCell(5).setCellValue("N/A");
                    row.createCell(6).setCellValue("N/A");
                    row.createCell(7).setCellValue("No notes available");
                }
                
                // Empty row after each category
                sheet.createRow(rowIdx++);
            }
        } else {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(serviceInfo);
            row.createCell(1).setCellValue(sc.getDate().format(dateFormatter));
            row.createCell(2).setCellValue(driverInfo);
            row.createCell(3).setCellValue("No categories");
            row.createCell(4).setCellValue("No items");
            row.createCell(5).setCellValue("N/A");
            row.createCell(6).setCellValue("N/A");
            row.createCell(7).setCellValue("No data available");
            sheet.createRow(rowIdx++);
        }
        
        // Empty row between service checkers
        sheet.createRow(rowIdx++);
    }
    
    // Add conditional formatting
    if (rowIdx > 1) {
        addConditionalFormatting(sheet, rowIdx - 1);
    }
}

private CellStyle createCategoryHeaderStyle(Workbook workbook) {
    CellStyle style = workbook.createCellStyle();
    Font font = workbook.createFont();
    font.setBold(true);
    font.setColor(IndexedColors.BLACK.getIndex());
    style.setFont(font);
    style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
    style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    style.setBorderBottom(BorderStyle.THIN);
    style.setBorderTop(BorderStyle.THIN);
    style.setBorderLeft(BorderStyle.THIN);
    style.setBorderRight(BorderStyle.THIN);
    return style;
}
    
    // Add conditional formatting for better visualization
    private void addConditionalFormatting(Sheet sheet, int lastRow) {
        SheetConditionalFormatting sheetCF = sheet.getSheetConditionalFormatting();
        
        // Green background for PASS
        ConditionalFormattingRule passRule = sheetCF.createConditionalFormattingRule("G1=\"PASS\"");
        PatternFormatting passFmt = passRule.createPatternFormatting();
        passFmt.setFillBackgroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        passFmt.setFillPattern(PatternFormatting.SOLID_FOREGROUND);
        
        // Orange background for FAIL
        ConditionalFormattingRule failRule = sheetCF.createConditionalFormattingRule("G1=\"FAIL\"");
        PatternFormatting failFmt = failRule.createPatternFormatting();
        failFmt.setFillBackgroundColor(IndexedColors.LIGHT_ORANGE.getIndex());
        failFmt.setFillPattern(PatternFormatting.SOLID_FOREGROUND);
        
        // Apply to the Status column (column G)
        CellRangeAddress[] regions = {
            CellRangeAddress.valueOf("G2:G" + lastRow)
        };
        
        sheetCF.addConditionalFormatting(regions, passRule, failRule);
    }
}