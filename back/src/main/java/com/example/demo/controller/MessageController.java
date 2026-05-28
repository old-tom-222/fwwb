package com.example.demo.controller;

import com.example.demo.model.Message;
import com.example.demo.model.Data;
import com.example.demo.model.Generative;
import com.example.demo.model.Communication;
import com.example.demo.repository.MessageRepository;
import com.example.demo.repository.DataRepository;
import com.example.demo.repository.GenerativeRepository;
import com.example.demo.repository.CommunicationRepository;
import com.example.demo.service.ZhipuAIService;
import com.example.demo.util.FileParser;
import com.example.demo.util.AIResponseFormatter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;

@RestController
@RequestMapping("/api/messages")
@CrossOrigin(origins = "http://localhost:8080")
@Tag(name = "消息管理", description = "消息 CRUD 操作")
public class MessageController {
    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ZhipuAIService zhipuAIService;

    @Autowired
    private DataRepository dataRepository;

    @Autowired
    private GenerativeRepository generativeRepository;

    @Autowired
    private CommunicationRepository communicationRepository;

    private String getUserIdFromCommunication(String communicationId) {
        try {
            Communication communication = communicationRepository.findById(communicationId).orElse(null);
            if (communication != null) {
                return communication.getUserId();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return communicationId;
    }

    @GetMapping
    @Operation(summary = "获取所有消息", description = "返回所有消息列表")
    public List<Message> getAllMessages() {
        return messageRepository.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取消息", description = "根据消息ID返回消息信息")
    public Message getMessageById(@PathVariable String id) {
        return messageRepository.findById(id).orElse(null);
    }

    @GetMapping("/communication/{communicationId}")
    @Operation(summary = "根据对话ID获取消息", description = "根据对话ID返回消息列表")
    public List<Message> getMessagesByCommunicationId(@PathVariable String communicationId) {
        return messageRepository.findByCommunicationId(communicationId);
    }

    @PostMapping
    @Operation(summary = "创建消息", description = "创建新消息，返回创建后的消息对象")
    public Message createMessage(@RequestBody Message message) {
        return messageRepository.save(message);
    }

    @PostMapping("/chat")
    @Operation(summary = "聊天接口", description = "处理用户聊天请求，返回AI回复")
    public Message chat(@RequestParam("communicationId") String communicationId,
                       @RequestParam("content") String content,
                       @RequestParam(value = "files", required = false) MultipartFile[] files,
                       @RequestParam(value = "fileIds", required = false) String[] fileIds) {
        System.out.println("收到聊天请求，对话ID: " + communicationId + "，内容: " + content);

        if (content.contains("生成docx") || content.contains("生成一个docx") || content.contains("生成个docx") || content.contains("生成DOCX") || content.contains("生成一个DOCX") || content.contains("生成个DOCX")) {
            String userId = getUserIdFromCommunication(communicationId);
            return generateDocx(communicationId, content, userId, files, fileIds);
        } else if (content.contains("生成xlsx") || content.contains("生成XLSX") || content.contains("生成一个xlsx") || content.contains("生成个xlsx") || content.contains("生成一个XLSX") || content.contains("生成个XLSX")) {
            String userId = getUserIdFromCommunication(communicationId);
            return generateXlsx(communicationId, content, userId, files, fileIds);
        } else if (content.contains("修改docx") || content.contains("修改一个docx") || content.contains("修改个docx") || content.contains("修改DOCX") || content.contains("修改一个DOCX") || content.contains("修改个DOCX") || (content.contains("docx") && (content.contains("重新生成") || content.contains("简化") || content.contains("修改") || content.contains("加入") || content.contains("删除") || content.contains("添加")))) {
            String userId = getUserIdFromCommunication(communicationId);
            return modifyDocx(communicationId, content, userId, files, fileIds);
        } else if (content.contains("修改xlsx") || content.contains("修改XLSX") || content.contains("修改一个xlsx") || content.contains("修改个xlsx") || content.contains("修改一个XLSX") || content.contains("修改个XLSX") || (content.contains("xlsx") && (content.contains("重新生成") || content.contains("简化") || content.contains("修改") || content.contains("加入") || content.contains("删除") || content.contains("添加")))) {
            String userId = getUserIdFromCommunication(communicationId);
            return modifyXlsx(communicationId, content, userId, files, fileIds);
        }

        List<String> fileContents = parseUploadedFiles(files);
        saveUserMessage(communicationId, content);

        StringBuilder fullContent = buildBasePrompt(communicationId, content, fileContents, fileIds);
        String aiResponse = zhipuAIService.chat(fullContent.toString(), fileContents);

        String formattedResponse = AIResponseFormatter.formatResponse(aiResponse);
        return saveAiMessage(communicationId, formattedResponse, 0);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新消息", description = "根据消息ID更新消息信息，返回更新后的消息对象")
    public Message updateMessage(@PathVariable String id, @RequestBody Message message) {
        message.setId(id);
        return messageRepository.save(message);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除消息", description = "根据消息ID删除消息")
    public void deleteMessage(@PathVariable String id) {
        messageRepository.deleteById(id);
    }

    @PostMapping("/generate-docx")
    @Operation(summary = "生成docx文档", description = "根据用户输入和文件分析关键词，生成docx文档并返回下载链接")
    public Message generateDocx(@RequestParam("communicationId") String communicationId,
                               @RequestParam("content") String content,
                               @RequestParam(value = "userId", required = false) String userId,
                               @RequestParam(value = "files", required = false) MultipartFile[] files,
                               @RequestParam(value = "fileIds", required = false) String[] fileIds) {
        if (userId == null || userId.isEmpty()) {
            userId = getUserIdFromCommunication(communicationId);
        }
        List<String> fileContents = parseUploadedFiles(files);
        saveUserMessage(communicationId, content);

        StringBuilder fullContent = buildBasePrompt(communicationId, content, fileContents, fileIds);

        List<Data> dataList = dataRepository.findByArticleId(communicationId);
        if (!dataList.isEmpty() && (fileIds == null || fileIds.length == 0)) {
            fullContent.append("以下是文件的关键词：\n");
            for (Data data : dataList) {
                fullContent.append("关键词：").append(data.getKeyword()).append("\n");
                fullContent.append("相关内容：").append(data.getContextText()).append("\n\n");
            }
        }

        String aiResponse = zhipuAIService.chat(fullContent.toString(), fileContents);
        String docxUrl = generateDocxFile(aiResponse, communicationId, userId);

        String responseWithLink = "我已经为您生成了docx文档，请点击以下链接下载：\n" + docxUrl;
        String formattedResponse = AIResponseFormatter.formatResponse(responseWithLink);
        return saveAiMessage(communicationId, formattedResponse, 0);
    }

    @PostMapping("/generate-xlsx")
    @Operation(summary = "生成xlsx表格", description = "根据用户输入和文件分析关键词，生成xlsx表格并返回下载链接")
    public Message generateXlsx(@RequestParam("communicationId") String communicationId,
                               @RequestParam("content") String content,
                               @RequestParam(value = "userId", required = false) String userId,
                               @RequestParam(value = "files", required = false) MultipartFile[] files,
                               @RequestParam(value = "fileIds", required = false) String[] fileIds) {
        if (userId == null || userId.isEmpty()) {
            userId = getUserIdFromCommunication(communicationId);
        }
        List<String> fileContents = parseUploadedFiles(files);
        saveUserMessage(communicationId, content);

        StringBuilder fullContent = buildBasePrompt(communicationId, content, fileContents, fileIds);

        List<Data> dataList = dataRepository.findByArticleId(communicationId);
        if (!dataList.isEmpty() && (fileIds == null || fileIds.length == 0)) {
            fullContent.append("以下是文件的关键词：\n");
            for (Data data : dataList) {
                fullContent.append("关键词：").append(data.getKeyword()).append("\n");
                fullContent.append("相关内容：").append(data.getContextText()).append("\n\n");
            }
        }

        String aiResponse = zhipuAIService.chat(fullContent.toString(), fileContents);
        String xlsxUrl = generateXlsxFile(aiResponse, communicationId, userId);

        String responseWithLink = "我已经为您生成了xlsx表格，请点击以下链接下载：\n" + xlsxUrl;
        String formattedResponse = AIResponseFormatter.formatResponse(responseWithLink);
        return saveAiMessage(communicationId, formattedResponse, 0);
    }

    @PostMapping("/modify-docx")
    @Operation(summary = "修改docx文档", description = "根据用户输入和现有文件内容，修改docx文档并返回下载链接")
    public Message modifyDocx(@RequestParam("communicationId") String communicationId,
                               @RequestParam("content") String content,
                               @RequestParam(value = "userId", required = false) String userId,
                               @RequestParam(value = "files", required = false) MultipartFile[] files,
                               @RequestParam(value = "fileIds", required = false) String[] fileIds) {
        if (userId == null || userId.isEmpty()) {
            userId = getUserIdFromCommunication(communicationId);
        }

        String existingFileContent = readExistingDocxContent(userId);
        List<String> fileContents = parseUploadedFiles(files);
        saveUserMessage(communicationId, content);

        StringBuilder fullContent = buildBasePrompt(communicationId, content, fileContents, fileIds);

        if (!existingFileContent.isEmpty()) {
            fullContent.append("现有文件内容：\n").append(existingFileContent).append("\n\n");
        }

        String aiResponse = zhipuAIService.chat(fullContent.toString(), fileContents);
        String docxUrl = generateDocxFile(aiResponse, communicationId, userId);

        String responseWithLink = "我已经为您修改了docx文档，请点击以下链接下载：\n" + docxUrl;
        return saveAiMessage(communicationId, responseWithLink, 0);
    }

    @PostMapping("/modify-xlsx")
    @Operation(summary = "修改xlsx表格", description = "根据用户输入和现有文件内容，修改xlsx表格并返回下载链接")
    public Message modifyXlsx(@RequestParam("communicationId") String communicationId,
                               @RequestParam("content") String content,
                               @RequestParam(value = "userId", required = false) String userId,
                               @RequestParam(value = "files", required = false) MultipartFile[] files,
                               @RequestParam(value = "fileIds", required = false) String[] fileIds) {
        if (userId == null || userId.isEmpty()) {
            userId = getUserIdFromCommunication(communicationId);
        }

        String existingFileContent = readExistingXlsxContent(userId);
        List<String> fileContents = parseUploadedFiles(files);
        saveUserMessage(communicationId, content);

        StringBuilder fullContent = buildBasePrompt(communicationId, content, fileContents, fileIds);

        if (!existingFileContent.isEmpty()) {
            fullContent.append("现有文件内容：\n").append(existingFileContent).append("\n\n");
        }

        String aiResponse = zhipuAIService.chat(fullContent.toString(), fileContents);
        String xlsxUrl = generateXlsxFile(aiResponse, communicationId, userId);

        String responseWithLink = "我已经为您修改了xlsx表格，请点击以下链接下载：\n" + xlsxUrl;
        return saveAiMessage(communicationId, responseWithLink, 0);
    }

    private List<String> parseUploadedFiles(MultipartFile[] files) {
        List<String> fileContents = new ArrayList<>();
        if (files != null && files.length > 0) {
            for (MultipartFile file : files) {
                System.out.println("上传的文件: " + file.getOriginalFilename());
                try {
                    String fileContent = FileParser.parseFile(file.getOriginalFilename(), file.getInputStream());
                    fileContents.add(fileContent);
                    System.out.println("文件内容长度: " + fileContent.length());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return fileContents;
    }

    private Message saveUserMessage(String communicationId, String content) {
        Message userMessage = new Message();
        userMessage.setCommunicationId(communicationId);
        userMessage.setContent(content);
        userMessage.setStatus(1);
        userMessage.setCreatedAt(new Date());
        Message saved = messageRepository.save(userMessage);
        System.out.println("用户消息已保存: " + saved);
        return saved;
    }

    private Message saveAiMessage(String communicationId, String content, int status) {
        Message aiMessage = new Message();
        aiMessage.setCommunicationId(communicationId);
        aiMessage.setContent(content);
        aiMessage.setStatus(status);
        aiMessage.setCreatedAt(new Date());
        Message saved = messageRepository.save(aiMessage);
        System.out.println("AI回复已保存: " + saved);
        return saved;
    }

    private StringBuilder buildBasePrompt(String communicationId, String content, List<String> fileContents, String[] fileIds) {
        List<Message> historyMessages = messageRepository.findByCommunicationId(communicationId);
        historyMessages.sort((m1, m2) -> m1.getCreatedAt().compareTo(m2.getCreatedAt()));

        StringBuilder fullContent = new StringBuilder();
        fullContent.append("以下是对话历史：\n");

        for (Message msg : historyMessages) {
            if (msg.getStatus() == 1) {
                fullContent.append("用户：").append(msg.getContent()).append("\n");
            } else {
                fullContent.append("AI：").append(msg.getContent()).append("\n");
            }
        }

        fullContent.append("\n用户需求：\n").append(content).append("\n\n");

        if (fileContents != null) {
            for (int i = 0; i < fileContents.size(); i++) {
                fullContent.append("文件").append(i + 1).append("内容：\n").append(fileContents.get(i)).append("\n\n");
            }
        }

        appendKeywordData(fullContent, fileIds);

        return fullContent;
    }

    private void appendKeywordData(StringBuilder sb, String[] fileIds) {
        if (fileIds != null && fileIds.length > 0) {
            for (int i = 0; i < fileIds.length; i++) {
                String fileId = fileIds[i];
                System.out.println("处理文件ID: " + fileId);
                List<Data> dataList = dataRepository.findByArticleId(fileId);
                System.out.println("文件" + (i + 1) + "的关键词数量: " + dataList.size());
                if (!dataList.isEmpty()) {
                    sb.append("文件").append(i + 1).append("关键词内容：\n");
                    for (Data data : dataList) {
                        sb.append("关键词：").append(data.getKeyword()).append("，句子：").append(data.getContextText()).append("\n");
                    }
                    sb.append("\n");
                }
            }
        }
    }

    private String readExistingDocxContent(String userId) {
        Generative existingGenerative = generativeRepository.findByUserIdAndType(userId, "file");
        if (existingGenerative == null) return "";

        try {
            String filePath = existingGenerative.getUrl().replace("/", "");
            java.io.File file = new java.io.File(filePath);
            if (!file.exists()) return "";

            org.apache.poi.xwpf.usermodel.XWPFDocument document = new org.apache.poi.xwpf.usermodel.XWPFDocument(new java.io.FileInputStream(file));
            StringBuilder contentBuilder = new StringBuilder();
            for (org.apache.poi.xwpf.usermodel.XWPFParagraph paragraph : document.getParagraphs()) {
                contentBuilder.append(paragraph.getText()).append("\n");
            }
            document.close();
            System.out.println("读取现有docx文件内容成功，长度: " + contentBuilder.length());
            return contentBuilder.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private String readExistingXlsxContent(String userId) {
        Generative existingGenerative = generativeRepository.findByUserIdAndType(userId, "table");
        if (existingGenerative == null) return "";

        try {
            String filePath = existingGenerative.getUrl().replace("/", "");
            java.io.File file = new java.io.File(filePath);
            if (!file.exists()) return "";

            org.apache.poi.ss.usermodel.Workbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook(new java.io.FileInputStream(file));
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.getSheetAt(0);
            StringBuilder contentBuilder = new StringBuilder();

            for (int i = 0; i <= sheet.getLastRowNum(); i++) {
                org.apache.poi.ss.usermodel.Row row = sheet.getRow(i);
                if (row != null) {
                    for (int j = 0; j < row.getLastCellNum(); j++) {
                        org.apache.poi.ss.usermodel.Cell cell = row.getCell(j);
                        if (cell != null) {
                            contentBuilder.append(cell.toString());
                        }
                        if (j < row.getLastCellNum() - 1) {
                            contentBuilder.append(",");
                        }
                    }
                    contentBuilder.append("\n");
                }
            }
            workbook.close();
            System.out.println("读取现有xlsx文件内容成功，长度: " + contentBuilder.length());
            return contentBuilder.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private String generateDocxFile(String content, String communicationId, String userId) {
        try {
            System.out.println("开始生成DOCX文件");
            String formattedContent = AIResponseFormatter.formatDocx(content);

            String fileName = "document_" + java.util.UUID.randomUUID().toString() + "_" + System.currentTimeMillis() + ".docx";
            String uploadDir = "uploads/generative";
            String filePath = uploadDir + "/" + fileName;

            java.io.File dir = new java.io.File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            org.apache.poi.xwpf.usermodel.XWPFDocument document = new org.apache.poi.xwpf.usermodel.XWPFDocument();
            org.apache.poi.xwpf.usermodel.XWPFParagraph paragraph = document.createParagraph();
            org.apache.poi.xwpf.usermodel.XWPFRun run = paragraph.createRun();
            run.setText(formattedContent);

            java.io.FileOutputStream fos = new java.io.FileOutputStream(filePath);
            document.write(fos);
            document.close();
            fos.close();

            String relativeUrl = "/" + filePath;

            Generative existingGenerative = generativeRepository.findByUserIdAndType(userId, "file");
            String fileId;
            if (existingGenerative != null) {
                existingGenerative.setName(fileName);
                existingGenerative.setUrl(relativeUrl);
                existingGenerative.setFileType(0);
                existingGenerative.setUpdatedAt(new Date());
                generativeRepository.save(existingGenerative);
                fileId = existingGenerative.getId();
            } else {
                Generative generative = new Generative(userId, fileName, relativeUrl, "file");
                generative.setFileType(0);
                generative = generativeRepository.save(generative);
                fileId = generative.getId();
            }

            String downloadUrl = "http://localhost:8081/api/generative/file/" + fileId;
            System.out.println("生成下载URL: " + downloadUrl);
            return downloadUrl;
        } catch (Exception e) {
            e.printStackTrace();
            return "生成文档失败";
        }
    }

    private String generateXlsxFile(String content, String communicationId, String userId) {
        try {
            String formattedContent = AIResponseFormatter.formatXlsx(content);

            String fileName = "table_" + java.util.UUID.randomUUID().toString() + "_" + System.currentTimeMillis() + ".xlsx";
            String uploadDir = "uploads/generative";
            String filePath = uploadDir + "/" + fileName;

            java.io.File dir = new java.io.File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            org.apache.poi.ss.usermodel.Workbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook();
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("分析结果");

            sheet.setDefaultColumnWidth(15);

            org.apache.poi.ss.usermodel.CellStyle headerStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(org.apache.poi.ss.usermodel.HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(org.apache.poi.ss.usermodel.VerticalAlignment.CENTER);
            headerStyle.setBorderTop(org.apache.poi.ss.usermodel.BorderStyle.THIN);
            headerStyle.setBorderBottom(org.apache.poi.ss.usermodel.BorderStyle.THIN);
            headerStyle.setBorderLeft(org.apache.poi.ss.usermodel.BorderStyle.THIN);
            headerStyle.setBorderRight(org.apache.poi.ss.usermodel.BorderStyle.THIN);

            org.apache.poi.ss.usermodel.CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setAlignment(org.apache.poi.ss.usermodel.HorizontalAlignment.LEFT);
            dataStyle.setVerticalAlignment(org.apache.poi.ss.usermodel.VerticalAlignment.CENTER);
            dataStyle.setBorderTop(org.apache.poi.ss.usermodel.BorderStyle.THIN);
            dataStyle.setBorderBottom(org.apache.poi.ss.usermodel.BorderStyle.THIN);
            dataStyle.setBorderLeft(org.apache.poi.ss.usermodel.BorderStyle.THIN);
            dataStyle.setBorderRight(org.apache.poi.ss.usermodel.BorderStyle.THIN);

            String[] lines = formattedContent.split("\n");
            int rowIndex = 0;
            boolean isFirstRow = true;

            for (String line : lines) {
                if (line.trim().isEmpty()) continue;
                org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowIndex++);
                String[] cells = line.split(",");
                for (int i = 0; i < cells.length; i++) {
                    org.apache.poi.ss.usermodel.Cell cell = row.createCell(i);
                    cell.setCellValue(cells[i].trim());
                    cell.setCellStyle(isFirstRow ? headerStyle : dataStyle);
                }
                isFirstRow = false;
            }

            for (int i = 0; i < 10; i++) {
                sheet.autoSizeColumn(i);
            }

            java.io.FileOutputStream fos = new java.io.FileOutputStream(filePath);
            workbook.write(fos);
            workbook.close();
            fos.close();

            String relativeUrl = "/" + filePath;

            Generative existingGenerative = generativeRepository.findByUserIdAndType(userId, "table");
            String fileId;
            if (existingGenerative != null) {
                existingGenerative.setName(fileName);
                existingGenerative.setUrl(relativeUrl);
                existingGenerative.setFileType(1);
                existingGenerative.setUpdatedAt(new Date());
                generativeRepository.save(existingGenerative);
                fileId = existingGenerative.getId();
            } else {
                Generative generative = new Generative(userId, fileName, relativeUrl, "table");
                generative.setFileType(1);
                generative = generativeRepository.save(generative);
                fileId = generative.getId();
            }

            return "http://localhost:8081/api/generative/file/" + fileId;
        } catch (Exception e) {
            e.printStackTrace();
            return "生成表格失败";
        }
    }
}
