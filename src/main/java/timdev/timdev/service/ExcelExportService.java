package timdev.timdev.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
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

    
    public ByteArrayInputStream exportToExcel(List<ServiceChecker> serviceCheckers) {
    try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
        
        // Create single sheet
        Sheet sheet = workbook.createSheet("Service Checker Report");
        
        // Create styles
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle subHeaderStyle = createSubHeaderStyle(workbook);
        CellStyle dateStyle = createDateStyle(workbook);
        CellStyle categoryStyle = createCategoryStyle(workbook);
        CellStyle passedStyle = createPassedStyle(workbook);
        CellStyle failedStyle = createFailedStyle(workbook);
        CellStyle serviceInfoStyle = createServiceInfoStyle(workbook);
        CellStyle noteStyle = createNoteStyle(workbook);
        
        // Create header row
        String[] headers = {
            "ID", "Date", "License Plate", "Inspector",
            "Category", "Inspection Item", "Result", "Comments", "Photo"
        };
        
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
            sheet.setColumnWidth(i, 256 * 15); // Set default width
        }
        
        // Set specific column widths
        sheet.setColumnWidth(0, 256 * 8);  // ID
        sheet.setColumnWidth(1, 256 * 12); // Date
        sheet.setColumnWidth(2, 256 * 15); // License Plate
        sheet.setColumnWidth(3, 256 * 15); // Inspector
        sheet.setColumnWidth(4, 256 * 25); // Category
        sheet.setColumnWidth(5, 256 * 40); // Inspection Item
        sheet.setColumnWidth(6, 256 * 10); // Result
        sheet.setColumnWidth(7, 256 * 30); // Comments
        sheet.setColumnWidth(8, 256 * 8);  // Photo
        
        // Add data rows
        int rowNum = 1;
        
        for (ServiceChecker sc : serviceCheckers) {
            int startRow = rowNum;
            boolean hasItems = false;
            
            // Service Checker Header (collapsible group)
            if (sc.getItems() != null && !sc.getItems().isEmpty()) {
                for (ServiceCheckerItem item : sc.getItems()) {
                    if (item.getNotes() != null && !item.getNotes().isEmpty()) {
                        hasItems = true;
                        
                        // Category header (only once per category)
                        boolean firstNoteInCategory = true;
                        
                        for (ServiceCheckerItemNote note : item.getNotes()) {
                            Row row = sheet.createRow(rowNum++);
                            
                            // Service Checker Info (only on first row of this service check)
                            if (rowNum - 1 == startRow) {
                                // ID
                                Cell idCell = row.createCell(0);
                                idCell.setCellValue(sc.getId() != null ? sc.getId().toString() : "");
                                idCell.setCellStyle(serviceInfoStyle);
                                
                                // Date
                                Cell dateCell = row.createCell(1);
                                if (sc.getDate() != null) {
                                    dateCell.setCellValue(sc.getDate());
                                    dateCell.setCellStyle(dateStyle);
                                }
                                
                                // License Plate
                                Cell plateCell = row.createCell(2);
                                plateCell.setCellValue(sc.getLicensePlate() != null ? sc.getLicensePlate() : "");
                                plateCell.setCellStyle(serviceInfoStyle);
                                
                                // Inspector
                                Cell inspectorCell = row.createCell(3);
                                inspectorCell.setCellValue(sc.getCreatedBy() != null ? sc.getCreatedBy().fullName() : "");
                                inspectorCell.setCellStyle(serviceInfoStyle);
                            } else {
                                // Empty but styled cells for subsequent rows
                                for (int i = 0; i < 4; i++) {
                                    Cell cell = row.createCell(i);
                                    cell.setCellValue("");
                                    cell.setCellStyle(serviceInfoStyle);
                                }
                            }
                            
                            // Category (only first note in category shows category name)
                            if (firstNoteInCategory) {
                                Cell categoryCell = row.createCell(4);
                                if (item.getCategory() != null) {
                                    categoryCell.setCellValue(item.getCategory().getKhmerName());
                                    categoryCell.setCellStyle(categoryStyle);
                                }
                                firstNoteInCategory = false;
                            } else {
                                Cell emptyCell = row.createCell(4);
                                emptyCell.setCellValue("");
                                emptyCell.setCellStyle(categoryStyle);
                            }
                            
                            // Inspection Item
                            Cell itemCell = row.createCell(5);
                            if (note.getInspectionItem() != null) {
                                itemCell.setCellValue(note.getInspectionItem().getKhmerName());
                            }
                            itemCell.setCellStyle(noteStyle);
                            
                            // Result (Passed/Failed with icons)
                            Cell resultCell = row.createCell(6);
                            String result = note.isPassed() ? "✓ PASSED" : "✗ FAILED";
                            resultCell.setCellValue(result);
                            resultCell.setCellStyle(note.isPassed() ? passedStyle : failedStyle);
                            
                            // Comments
                            Cell commentCell = row.createCell(7);
                            commentCell.setCellValue(note.getNote() != null ? note.getNote() : "");
                            commentCell.setCellStyle(noteStyle);
                            
                            // Photo
                            Cell photoCell = row.createCell(8);
                            String photoStatus = sc.getImagePath() != null ? "✓" : "—";
                            photoCell.setCellValue(photoStatus);
                            photoCell.setCellStyle(noteStyle);
                        }
                        
                        // Add thin border after each category
                        Row borderRow = sheet.createRow(rowNum++);
                        for (int i = 0; i < headers.length; i++) {
                            Cell cell = borderRow.createCell(i);
                            cell.setCellValue("");
                            cell.setCellStyle(createBorderStyle(workbook));
                        }
                    }
                }
            }
            
            // If no items, show a summary row
            if (!hasItems) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(sc.getId() != null ? sc.getId().toString() : "");
                
                Cell dateCell = row.createCell(1);
                if (sc.getDate() != null) {
                    dateCell.setCellValue(sc.getDate());
                    dateCell.setCellStyle(dateStyle);
                }
                
                row.createCell(2).setCellValue(sc.getLicensePlate() != null ? sc.getLicensePlate() : "");
                row.createCell(3).setCellValue(sc.getCreatedBy() != null ? sc.getCreatedBy().fullName() : "");
                row.createCell(4).setCellValue("No inspection items");
                row.createCell(5).setCellValue("—");
                row.createCell(6).setCellValue("N/A");
                row.createCell(7).setCellValue("");
                row.createCell(8).setCellValue(sc.getImagePath() != null ? "✓" : "—");
            }
            
            // Add service checker summary row with statistics
            if (hasItems) {
                Row summaryRow = sheet.createRow(rowNum++);
                Cell summaryCell = summaryRow.createCell(0);
                summaryCell.setCellValue("SUMMARY");
                summaryCell.setCellStyle(subHeaderStyle);
                
                // Calculate stats for this service check
                long totalItems = 0;
                long passedItems = 0;
                long failedItems = 0;
                
                if (sc.getItems() != null) {
                    for (ServiceCheckerItem item : sc.getItems()) {
                        if (item.getNotes() != null) {
                            totalItems += item.getNotes().size();
                            passedItems += item.getNotes().stream().filter(ServiceCheckerItemNote::isPassed).count();
                            failedItems += item.getNotes().stream().filter(n -> !n.isPassed()).count();
                        }
                    }
                }
                
                summaryRow.createCell(4).setCellValue(String.format("Total: %d items", totalItems));
                Cell passedTotalCell = summaryRow.createCell(5);
                passedTotalCell.setCellValue(String.format("✓ Passed: %d", passedItems));
                passedTotalCell.setCellStyle(passedStyle);
                
                Cell failedTotalCell = summaryRow.createCell(6);
                failedTotalCell.setCellValue(String.format("✗ Failed: %d", failedItems));
                failedTotalCell.setCellStyle(failedStyle);
            }
            
            // Add two blank rows between service checkers
            for (int i = 0; i < 2; i++) {
                Row blankRow = sheet.createRow(rowNum++);
                for (int j = 0; j < headers.length; j++) {
                    blankRow.createCell(j).setCellValue("");
                }
            }
        }
        
        // Add filter
        sheet.setAutoFilter(new CellRangeAddress(0, 0, 0, headers.length - 1));
        
        // Freeze header row
        sheet.createFreezePane(0, 1);
        
        // Add grouping for better readability
        for (int i = 1; i < rowNum; i++) {
            if (i % 15 == 0) { // Group every ~15 rows
                sheet.groupRow(i, Math.min(i + 10, rowNum - 1));
            }
        }
        
        workbook.write(out);
        return new ByteArrayInputStream(out.toByteArray());
        
    } catch (IOException e) {
        throw new RuntimeException("Failed to export data to Excel file", e);
    }
}

// Enhanced styling methods with better readability
private CellStyle createHeaderStyle(Workbook workbook) {
    CellStyle style = workbook.createCellStyle();
    Font font = workbook.createFont();
    font.setBold(true);
    font.setColor(IndexedColors.WHITE.getIndex());
    font.setFontHeightInPoints((short) 12);
    style.setFont(font);
    style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
    style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    style.setBorderBottom(BorderStyle.MEDIUM);
    style.setBorderTop(BorderStyle.MEDIUM);
    style.setBorderLeft(BorderStyle.MEDIUM);
    style.setBorderRight(BorderStyle.MEDIUM);
    style.setAlignment(HorizontalAlignment.CENTER);
    style.setVerticalAlignment(VerticalAlignment.CENTER);
    return style;
}

private CellStyle createSubHeaderStyle(Workbook workbook) {
    CellStyle style = workbook.createCellStyle();
    Font font = workbook.createFont();
    font.setBold(true);
    font.setColor(IndexedColors.WHITE.getIndex());
    font.setFontHeightInPoints((short) 11);
    style.setFont(font);
    style.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
    style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    style.setBorderBottom(BorderStyle.THIN);
    style.setBorderTop(BorderStyle.THIN);
    style.setBorderLeft(BorderStyle.THIN);
    style.setBorderRight(BorderStyle.THIN);
    style.setAlignment(HorizontalAlignment.LEFT);
    return style;
}

private CellStyle createDateStyle(Workbook workbook) {
    CellStyle style = workbook.createCellStyle();
    CreationHelper createHelper = workbook.getCreationHelper();
    style.setDataFormat(createHelper.createDataFormat().getFormat("dd/mm/yyyy"));
    style.setBorderBottom(BorderStyle.THIN);
    style.setBorderTop(BorderStyle.THIN);
    style.setBorderLeft(BorderStyle.THIN);
    style.setBorderRight(BorderStyle.THIN);
    style.setAlignment(HorizontalAlignment.CENTER);
    return style;
}

private CellStyle createCategoryStyle(Workbook workbook) {
    CellStyle style = workbook.createCellStyle();
    Font font = workbook.createFont();
    font.setBold(true);
    font.setColor(IndexedColors.DARK_GREEN.getIndex());
    font.setFontHeightInPoints((short) 11);
    style.setFont(font);
    style.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
    style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    style.setBorderBottom(BorderStyle.THIN);
    style.setBorderTop(BorderStyle.THIN);
    style.setBorderLeft(BorderStyle.THIN);
    style.setBorderRight(BorderStyle.THIN);
    style.setAlignment(HorizontalAlignment.LEFT);
    style.setIndention((short) 1);
    return style;
}

private CellStyle createPassedStyle(Workbook workbook) {
    CellStyle style = workbook.createCellStyle();
    Font font = workbook.createFont();
    font.setColor(IndexedColors.GREEN.getIndex());
    font.setBold(true);
    style.setFont(font);
    style.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
    style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    style.setBorderBottom(BorderStyle.THIN);
    style.setBorderTop(BorderStyle.THIN);
    style.setBorderLeft(BorderStyle.THIN);
    style.setBorderRight(BorderStyle.THIN);
    style.setAlignment(HorizontalAlignment.CENTER);
    return style;
}

private CellStyle createFailedStyle(Workbook workbook) {
    CellStyle style = workbook.createCellStyle();
    Font font = workbook.createFont();
    font.setColor(IndexedColors.RED.getIndex());
    font.setBold(true);
    style.setFont(font);
    style.setFillForegroundColor(IndexedColors.ROSE.getIndex());
    style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    style.setBorderBottom(BorderStyle.THIN);
    style.setBorderTop(BorderStyle.THIN);
    style.setBorderLeft(BorderStyle.THIN);
    style.setBorderRight(BorderStyle.THIN);
    style.setAlignment(HorizontalAlignment.CENTER);
    return style;
}

private CellStyle createServiceInfoStyle(Workbook workbook) {
    CellStyle style = workbook.createCellStyle();
    Font font = workbook.createFont();
    font.setBold(true);
    style.setFont(font);
    style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
    style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    style.setBorderBottom(BorderStyle.THIN);
    style.setBorderTop(BorderStyle.THIN);
    style.setBorderLeft(BorderStyle.THIN);
    style.setBorderRight(BorderStyle.THIN);
    style.setAlignment(HorizontalAlignment.LEFT);
    return style;
}

private CellStyle createNoteStyle(Workbook workbook) {
    CellStyle style = workbook.createCellStyle();
    style.setBorderBottom(BorderStyle.THIN);
    style.setBorderTop(BorderStyle.THIN);
    style.setBorderLeft(BorderStyle.THIN);
    style.setBorderRight(BorderStyle.THIN);
    style.setWrapText(true);
    style.setVerticalAlignment(VerticalAlignment.TOP);
    return style;
}

private CellStyle createBorderStyle(Workbook workbook) {
    CellStyle style = workbook.createCellStyle();
    style.setBorderBottom(BorderStyle.MEDIUM);
    style.setBorderTop(BorderStyle.MEDIUM);
    style.setBorderLeft(BorderStyle.MEDIUM);
    style.setBorderRight(BorderStyle.MEDIUM);
    style.setFillForegroundColor(IndexedColors.WHITE.getIndex());
    style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    return style;
}
}