package com.example.demo.controller;

import com.example.demo.model.Message;
import com.example.demo.model.Data;
import com.example.demo.repository.MessageRepository;
import com.example.demo.repository.DataRepository;
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
import java.util.Map;

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
        System.out.println("文件数量: " + (files != null ? files.length : 0));
        System.out.println("文件ID数量: " + (fileIds != null ? fileIds.length : 0));
        
        // 处理上传的文件
        List<String> fileContents = new ArrayList<>();
        if (files != null && files.length > 0) {
            for (MultipartFile file : files) {
                // 这里可以处理文件，例如保存到服务器或解析文件内容
                System.out.println("上传的文件: " + file.getOriginalFilename());
                try {
                    // 解析文件内容
                    String fileContent = FileParser.parseFile(file.getOriginalFilename(), file.getInputStream());
                    fileContents.add(fileContent);
                    System.out.println("文件内容长度: " + fileContent.length());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                // 可以将文件内容添加到content中，或者在调用AI API时一并发送
            }
        }
        
        // 保存用户消息（status=1）
        Message userMessage = new Message();
        userMessage.setCommunicationId(communicationId);
        userMessage.setContent(content);
        userMessage.setStatus(1);
        userMessage.setCreatedAt(new java.util.Date());
        messageRepository.save(userMessage);
        System.out.println("用户消息已保存: " + userMessage);
        
<<<<<<< HEAD
        // 获取该对话的历史消息，按照时间顺序排序
        List<Message> historyMessages = messageRepository.findByCommunicationId(communicationId);
        historyMessages.sort((m1, m2) -> m1.getCreatedAt().compareTo(m2.getCreatedAt()));
        
        // 构建完整的消息内容，包含历史对话、用户需求和文件内容
        StringBuilder fullContent = new StringBuilder();
        fullContent.append("以下是对话历史：\n");
        
        // 添加历史消息
        for (Message msg : historyMessages) {
            if (msg.getStatus() == 1) {
                fullContent.append("用户：").append(msg.getContent()).append("\n");
            } else {
                fullContent.append("AI：").append(msg.getContent()).append("\n");
            }
        }
        
        fullContent.append("\n用户需求：\n").append(content).append("\n\n");
=======
        // 构建完整的消息内容，包含用户需求和文件内容
        StringBuilder fullContent = new StringBuilder();
        fullContent.append("用户需求：\n").append(content).append("\n\n");
>>>>>>> 25a4306711f13d1c9dfb67cfeed220bd3cbe8b72
        
        if (!fileContents.isEmpty()) {
            for (int i = 0; i < fileContents.size(); i++) {
                fullContent.append("文件").append(i + 1).append("内容：\n").append(fileContents.get(i)).append("\n\n");
                System.out.println("添加文件" + (i + 1) + "内容到AI请求");
            }
        }
        
        // 添加文件的关键词内容
        if (fileIds != null && fileIds.length > 0) {
            for (int i = 0; i < fileIds.length; i++) {
                String fileId = fileIds[i];
                System.out.println("处理文件ID: " + fileId);
                List<Data> dataList = dataRepository.findByArticleId(fileId);
                System.out.println("文件" + (i + 1) + "的关键词数量: " + dataList.size());
                if (!dataList.isEmpty()) {
                    fullContent.append("文件").append(i + 1).append("关键词内容：\n");
                    for (Data data : dataList) {
                        fullContent.append("关键词：").append(data.getKeyword()).append("，句子：").append(data.getContextText()).append("\n");
                        System.out.println("添加关键词: " + data.getKeyword() + "，句子: " + data.getContextText());
                    }
                    fullContent.append("\n");
                }
            }
        }
        
        System.out.println("发送给AI的完整内容长度: " + fullContent.length());
        System.out.println("发送给AI的完整内容: " + fullContent.toString());
        
        // 调用智谱API获取AI回复，传递完整内容
        String aiResponse = zhipuAIService.chat(fullContent.toString(), fileContents);
        
        // 美化AI回复
        String formattedResponse = AIResponseFormatter.formatResponse(aiResponse);
        
        // 保存AI回复（status=0）
        Message aiMessage = new Message();
        aiMessage.setCommunicationId(communicationId);
        aiMessage.setContent(formattedResponse);
        aiMessage.setStatus(0);
        aiMessage.setCreatedAt(new java.util.Date());
        messageRepository.save(aiMessage);
        System.out.println("AI回复已保存: " + aiMessage);
        
        return aiMessage;
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
    @Operation(summary = "生成docx文档", description = "根据用户输入生成docx文档并返回下载链接")
    public Message generateDocx(@RequestParam("communicationId") String communicationId, 
                               @RequestParam("content") String content,
                               @RequestParam(value = "files", required = false) MultipartFile[] files) {
        // 处理上传的文件
        List<String> fileContents = new ArrayList<>();
        if (files != null && files.length > 0) {
            for (MultipartFile file : files) {
                try {
                    // 解析文件内容
                    String fileContent = FileParser.parseFile(file.getOriginalFilename(), file.getInputStream());
                    fileContents.add(fileContent);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        
        // 保存用户消息（status=1）
        Message userMessage = new Message();
        userMessage.setCommunicationId(communicationId);
        userMessage.setContent(content);
        userMessage.setStatus(1);
        userMessage.setCreatedAt(new java.util.Date());
        messageRepository.save(userMessage);
        
<<<<<<< HEAD
        // 获取该对话的历史消息，按照时间顺序排序
        List<Message> historyMessages = messageRepository.findByCommunicationId(communicationId);
        historyMessages.sort((m1, m2) -> m1.getCreatedAt().compareTo(m2.getCreatedAt()));
        
        // 构建完整的消息内容，包含历史对话和用户需求
        StringBuilder fullContent = new StringBuilder();
        fullContent.append("以下是对话历史：\n");
        
        // 添加历史消息
        for (Message msg : historyMessages) {
            if (msg.getStatus() == 1) {
                fullContent.append("用户：").append(msg.getContent()).append("\n");
            } else {
                fullContent.append("AI：").append(msg.getContent()).append("\n");
            }
        }
        
        fullContent.append("\n用户需求：\n").append(content).append("\n\n");
        
        // 调用智谱API获取AI回复，传递完整内容和文件内容
        String aiResponse = zhipuAIService.chat(fullContent.toString(), fileContents);
=======
        // 调用智谱API获取AI回复，传递文件内容
        String aiResponse = zhipuAIService.chat(content, fileContents);
>>>>>>> 25a4306711f13d1c9dfb67cfeed220bd3cbe8b72
        
        // 生成docx文档
        String docxUrl = generateDocxFile(aiResponse, communicationId);
        
        // 构建包含下载链接的回复
        String responseWithLink = "我已经为您生成了docx文档，请点击以下链接下载：\n" + docxUrl;
        
        // 美化AI回复
        String formattedResponse = AIResponseFormatter.formatResponse(responseWithLink);
        
        // 保存AI回复（status=0）
        Message aiMessage = new Message();
        aiMessage.setCommunicationId(communicationId);
        aiMessage.setContent(formattedResponse);
        aiMessage.setStatus(0);
        aiMessage.setCreatedAt(new java.util.Date());
        messageRepository.save(aiMessage);
        
        return aiMessage;
    }
    
    /**
     * 生成docx文档
     * @param content 文档内容
     * @param communicationId 对话ID
     * @return 文档下载链接
     */
    private String generateDocxFile(String content, String communicationId) {
        try {
            String fileName = "document_" + communicationId + "_" + System.currentTimeMillis() + ".docx";
            String filePath = "uploads/" + fileName;
            
            java.io.File file = new java.io.File(filePath);
            java.io.FileWriter writer = new java.io.FileWriter(file);
            writer.write(content);
            writer.close();
            
            return "http://localhost:8081/" + filePath;
        } catch (Exception e) {
            e.printStackTrace();
            return "生成文档失败";
        }
    }
    
    @PostMapping("/generate-xlsx")
    @Operation(summary = "生成xlsx表格", description = "根据用户输入和文件分析关键词，生成xlsx表格并返回下载链接")
    public Message generateXlsx(@RequestParam("communicationId") String communicationId,
                               @RequestParam("content") String content,
                               @RequestParam(value = "files", required = false) MultipartFile[] files) {
        List<String> fileContents = new ArrayList<>();
        if (files != null && files.length > 0) {
            for (MultipartFile file : files) {
                try {
                    String fileContent = FileParser.parseFile(file.getOriginalFilename(), file.getInputStream());
                    fileContents.add(fileContent);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        
        Message userMessage = new Message();
        userMessage.setCommunicationId(communicationId);
        userMessage.setContent(content);
        userMessage.setStatus(1);
        userMessage.setCreatedAt(new java.util.Date());
        messageRepository.save(userMessage);
        
<<<<<<< HEAD
        // 获取该对话的历史消息，按照时间顺序排序
        List<Message> historyMessages = messageRepository.findByCommunicationId(communicationId);
        historyMessages.sort((m1, m2) -> m1.getCreatedAt().compareTo(m2.getCreatedAt()));
        
        // 构建完整的消息内容，包含历史对话和用户需求
        StringBuilder fullContent = new StringBuilder();
        fullContent.append("以下是对话历史：\n");
        
        // 添加历史消息
        for (Message msg : historyMessages) {
            if (msg.getStatus() == 1) {
                fullContent.append("用户：").append(msg.getContent()).append("\n");
            } else {
                fullContent.append("AI：").append(msg.getContent()).append("\n");
            }
        }
        
        fullContent.append("\n用户需求：\n").append(content).append("\n\n");
        
        // 调用智谱API获取AI回复，传递完整内容和文件内容
        String aiResponse = zhipuAIService.chat(fullContent.toString(), fileContents);
=======
        String aiResponse = zhipuAIService.chat(content, fileContents);
>>>>>>> 25a4306711f13d1c9dfb67cfeed220bd3cbe8b72
        
        String xlsxUrl = generateXlsxFile(aiResponse, communicationId);
        
        String responseWithLink = "我已经为您生成了xlsx表格，请点击以下链接下载：\n" + xlsxUrl;
        
        String formattedResponse = AIResponseFormatter.formatResponse(responseWithLink);
        
        Message aiMessage = new Message();
        aiMessage.setCommunicationId(communicationId);
        aiMessage.setContent(formattedResponse);
        aiMessage.setStatus(0);
        aiMessage.setCreatedAt(new java.util.Date());
        messageRepository.save(aiMessage);
        
        return aiMessage;
    }
    
    private String generateXlsxFile(String content, String communicationId) {
        try {
            String fileName = "table_" + communicationId + "_" + System.currentTimeMillis() + ".xlsx";
            String filePath = "uploads/" + fileName;
            
            org.apache.poi.ss.usermodel.Workbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook();
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("分析结果");
            
            String[] lines = content.split("\n");
            int rowIndex = 0;
            for (String line : lines) {
                if (line.trim().isEmpty()) continue;
                org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowIndex++);
                String[] cells = line.split("[|,，\t]");
                for (int i = 0; i < cells.length; i++) {
                    row.createCell(i).setCellValue(cells[i].trim());
                }
            }
            
            java.io.FileOutputStream fos = new java.io.FileOutputStream(filePath);
            workbook.write(fos);
            workbook.close();
            fos.close();
            
            return "http://localhost:8081/" + filePath;
        } catch (Exception e) {
            e.printStackTrace();
            return "生成表格失败";
        }
    }
}