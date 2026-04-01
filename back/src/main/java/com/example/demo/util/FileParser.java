package com.example.demo.util;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FileParser {

    /**
     * 解析文件内容，根据文件类型返回对应的文本内容
     * @param fileName 文件名
     * @param inputStream 文件输入流
     * @return 解析后的文本内容
     * @throws IOException
     */
    public static String parseFile(String fileName, InputStream inputStream) throws IOException {
        if (fileName == null || inputStream == null) {
            return "";
        }
        
        // 根据文件扩展名选择解析方法
        if (fileName.endsWith(".txt") || fileName.endsWith(".md")) {
            return parseTextFile(inputStream);
        } else if (fileName.endsWith(".docx")) {
            return parseDocxFile(inputStream);
        } else if (fileName.endsWith(".xlsx")) {
            return parseXlsxFile(inputStream);
        } else {
            return "不支持的文件类型";
        }
    }

    /**
     * 解析文本文件（.txt, .md）
     * @param inputStream 文件输入流
     * @return 解析后的文本内容
     * @throws IOException
     */
    private static String parseTextFile(InputStream inputStream) throws IOException {
        byte[] bytes = inputStream.readAllBytes();
        return new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
    }

    /**
     * 解析Word文档（.docx）
     * @param inputStream 文件输入流
     * @return 解析后的文本内容
     * @throws IOException
     */
    private static String parseDocxFile(InputStream inputStream) throws IOException {
        XWPFDocument document = new XWPFDocument(inputStream);
        StringBuilder content = new StringBuilder();
        
        // 遍历文档中的段落
        List<XWPFParagraph> paragraphs = document.getParagraphs();
        for (XWPFParagraph paragraph : paragraphs) {
            content.append(paragraph.getText()).append("\n");
        }
        
        document.close();
        return content.toString();
    }

    /**
     * 解析Excel文件（.xlsx）
     * @param inputStream 文件输入流
     * @return 解析后的文本内容
     * @throws IOException
     */
    private static String parseXlsxFile(InputStream inputStream) throws IOException {
        Workbook workbook = new XSSFWorkbook(inputStream);
        StringBuilder content = new StringBuilder();
        
        // 遍历所有工作表
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            Sheet sheet = workbook.getSheetAt(i);
            content.append("工作表: " + sheet.getSheetName()).append("\n");
            
            // 遍历所有行
            Iterator<Row> rowIterator = sheet.iterator();
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                
                // 遍历所有单元格
                Iterator<Cell> cellIterator = row.cellIterator();
                while (cellIterator.hasNext()) {
                    Cell cell = cellIterator.next();
                    content.append(cell.toString()).append("\t");
                }
                content.append("\n");
            }
            content.append("\n");
        }
        
        workbook.close();
        return content.toString();
    }
}
