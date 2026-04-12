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
    
    @Autowired
    private GenerativeRepository generativeRepository;
    
    @Autowired
    private CommunicationRepository communicationRepository;
    
    public MessageController() {
        // 空构造函数
    }
    
    /**
     * 从Communication对象中获取用户ID
     * @param communicationId 对话ID
     * @return 用户ID
     */
    private String getUserIdFromCommunication(String communicationId) {
        try {
            Communication communication = communicationRepository.findById(communicationId).orElse(null);
            if (communication != null) {
                return communication.getUserId();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 如果获取失败，返回communicationId作为默认值
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
        System.out.println("文件数量: " + (files != null ? files.length : 0));
        System.out.println("文件ID数量: " + (fileIds != null ? fileIds.length : 0));
        
        // 检查用户是否请求生成文件
        if (content.contains("生成docx") || content.contains("生成一个docx") || content.contains("生成个docx") || content.contains("生成DOCX") || content.contains("生成一个DOCX") || content.contains("生成个DOCX")) {
            System.out.println("用户请求生成docx文件");
            // 从Communication对象中获取真实的userId
            String userId = getUserIdFromCommunication(communicationId);
            return generateDocx(communicationId, content, userId, files, fileIds);
        } else if (content.contains("生成xlsx") || content.contains("生成XLSX") || content.contains("生成一个xlsx") || content.contains("生成个xlsx") || content.contains("生成一个XLSX") || content.contains("生成个XLSX")) {
            System.out.println("用户请求生成xlsx文件");
            // 从Communication对象中获取真实的userId
            String userId = getUserIdFromCommunication(communicationId);
            return generateXlsx(communicationId, content, userId, files, fileIds);
        } else if (content.contains("修改docx") || content.contains("修改一个docx") || content.contains("修改个docx") || content.contains("修改DOCX") || content.contains("修改一个DOCX") || content.contains("修改个DOCX") || (content.contains("docx") && (content.contains("重新生成") || content.contains("简化") || content.contains("修改") || content.contains("加入") || content.contains("删除") || content.contains("添加")))) {
            System.out.println("用户请求修改docx文件");
            // 从Communication对象中获取真实的userId
            String userId = getUserIdFromCommunication(communicationId);
            return modifyDocx(communicationId, content, userId, files, fileIds);
        } else if (content.contains("修改xlsx") || content.contains("修改XLSX") || content.contains("修改一个xlsx") || content.contains("修改个xlsx") || content.contains("修改一个XLSX") || content.contains("修改个XLSX") || (content.contains("xlsx") && (content.contains("重新生成") || content.contains("简化") || content.contains("修改") || content.contains("加入") || content.contains("删除") || content.contains("添加")))) {
            System.out.println("用户请求修改xlsx文件");
            // 从Communication对象中获取真实的userId
            String userId = getUserIdFromCommunication(communicationId);
            return modifyXlsx(communicationId, content, userId, files, fileIds);
        }
        
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
    @Operation(summary = "生成docx文档", description = "根据用户输入和文件分析关键词，生成docx文档并返回下载链接")
    public Message generateDocx(@RequestParam("communicationId") String communicationId,
                               @RequestParam("content") String content,
                               @RequestParam(value = "userId", required = false) String userId,
                               @RequestParam(value = "files", required = false) MultipartFile[] files,
                               @RequestParam(value = "fileIds", required = false) String[] fileIds) {
        // 如果没有提供userId，从Communication对象中获取
        if (userId == null || userId.isEmpty()) {
            userId = getUserIdFromCommunication(communicationId);
        }
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
        
        // 保存用户消息（status=1）
        Message userMessage = new Message();
        userMessage.setCommunicationId(communicationId);
        userMessage.setContent(content);
        userMessage.setStatus(1);
        userMessage.setCreatedAt(new java.util.Date());
        messageRepository.save(userMessage);
        
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
        
        // 添加文件内容
        if (!fileContents.isEmpty()) {
            fullContent.append("以下是上传的文件内容：\n");
            for (String fileContent : fileContents) {
                fullContent.append(fileContent).append("\n\n");
            }
        }
        
        // 添加文件对应的关键词
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
        
        // 尝试从communicationId获取关键词（兼容旧数据）
        List<Data> dataList = dataRepository.findByArticleId(communicationId);
        if (!dataList.isEmpty() && (fileIds == null || fileIds.length == 0)) {
            fullContent.append("以下是文件的关键词：\n");
            for (Data data : dataList) {
                fullContent.append("关键词：").append(data.getKeyword()).append("\n");
                fullContent.append("相关内容：").append(data.getContextText()).append("\n\n");
            }
        }
        
        // 调用智谱API获取AI回复，传递完整内容和文件内容
        String aiResponse = zhipuAIService.chat(fullContent.toString(), fileContents);
        
        // 生成docx文档
        String docxUrl = generateDocxFile(aiResponse, communicationId, userId);
        
        // 构建包含下载链接的回复
        String responseWithLink = "我已经为您生成了docx文档，请点击以下链接下载：\n" + docxUrl;
        
        // 使用AIResponseFormatter美化回复，将下载链接转换为可点击的按钮
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
    private String generateDocxFile(String content, String communicationId, String userId) {
        try {
            System.out.println("开始生成DOCX文件");
            System.out.println("当前工作目录: " + System.getProperty("user.dir"));
            
            // 使用DocxFormatter美化内容
            System.out.println("开始美化内容");
            String formattedContent = AIResponseFormatter.formatDocx(content);
            System.out.println("美化内容完成，长度: " + formattedContent.length());
            
            // 使用UUID生成唯一的文件名称，避免使用可能包含中文字符的communicationId
            String fileName = "document_" + java.util.UUID.randomUUID().toString() + "_" + System.currentTimeMillis() + ".docx";
            String uploadDir = "uploads/generative";
            String filePath = uploadDir + "/" + fileName;
            System.out.println("文件路径: " + filePath);
            
            // 确保上传目录存在
            java.io.File dir = new java.io.File(uploadDir);
            System.out.println("上传目录存在: " + dir.exists());
            if (!dir.exists()) {
                System.out.println("创建上传目录");
                boolean created = dir.mkdirs();
                System.out.println("创建上传目录结果: " + created);
            }
            
            // 使用Apache POI创建真正的docx文件
            System.out.println("开始创建DOCX文件");
            org.apache.poi.xwpf.usermodel.XWPFDocument document = new org.apache.poi.xwpf.usermodel.XWPFDocument();
            org.apache.poi.xwpf.usermodel.XWPFParagraph paragraph = document.createParagraph();
            org.apache.poi.xwpf.usermodel.XWPFRun run = paragraph.createRun();
            run.setText(formattedContent);
            
            // 保存文件
            System.out.println("开始保存文件");
            java.io.FileOutputStream fos = new java.io.FileOutputStream(filePath);
            document.write(fos);
            document.close();
            fos.close();
            
            // 检查文件是否存在
            java.io.File file = new java.io.File(filePath);
            System.out.println("文件保存成功: " + file.exists());
            System.out.println("文件大小: " + file.length() + " bytes");
            
            // 构建文件相对路径
            String relativeUrl = "/" + filePath;
            System.out.println("文件相对路径: " + relativeUrl);
            
            // 检查是否已存在同类型的文件
            Generative existingGenerative = generativeRepository.findByUserIdAndType(userId, "file");
            String fileId;
            if (existingGenerative != null) {
                // 更新现有记录，但保留旧文件
                existingGenerative.setName(fileName);
                existingGenerative.setUrl(relativeUrl);
                existingGenerative.setFileType(0); // 0 for docx
                existingGenerative.setUpdatedAt(new java.util.Date());
                generativeRepository.save(existingGenerative);
                fileId = existingGenerative.getId();
                System.out.println("更新现有文件记录，ID: " + fileId);
            } else {
                // 创建新记录
                Generative generative = new Generative(userId, fileName, relativeUrl, "file");
                generative.setFileType(0); // 0 for docx
                generative = generativeRepository.save(generative);
                fileId = generative.getId();
                System.out.println("创建新文件记录，ID: " + fileId);
            }
            
            // 返回基于GenerativeController的文件下载URL
            String downloadUrl = "http://localhost:8081/api/generative/file/" + fileId;
            System.out.println("生成下载URL: " + downloadUrl);
            return downloadUrl;
        } catch (Exception e) {
            System.out.println("生成文档失败: " + e.getMessage());
            e.printStackTrace();
            return "生成文档失败";
        }
    }
    
    @PostMapping("/generate-xlsx")
    @Operation(summary = "生成xlsx表格", description = "根据用户输入和文件分析关键词，生成xlsx表格并返回下载链接")
    public Message generateXlsx(@RequestParam("communicationId") String communicationId,
                               @RequestParam("content") String content,
                               @RequestParam(value = "userId", required = false) String userId,
                               @RequestParam(value = "files", required = false) MultipartFile[] files,
                               @RequestParam(value = "fileIds", required = false) String[] fileIds) {
        // 如果没有提供userId，从Communication对象中获取
        if (userId == null || userId.isEmpty()) {
            userId = getUserIdFromCommunication(communicationId);
        }
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
        
        // 添加文件内容
        if (!fileContents.isEmpty()) {
            fullContent.append("以下是上传的文件内容：\n");
            for (String fileContent : fileContents) {
                fullContent.append(fileContent).append("\n\n");
            }
        }
        
        // 添加文件对应的关键词
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
        
        // 尝试从communicationId获取关键词（兼容旧数据）
        List<Data> dataList = dataRepository.findByArticleId(communicationId);
        if (!dataList.isEmpty() && (fileIds == null || fileIds.length == 0)) {
            fullContent.append("以下是文件的关键词：\n");
            for (Data data : dataList) {
                fullContent.append("关键词：").append(data.getKeyword()).append("\n");
                fullContent.append("相关内容：").append(data.getContextText()).append("\n\n");
            }
        }
        
        // 调用智谱API获取AI回复，传递完整内容和文件内容
        String aiResponse = zhipuAIService.chat(fullContent.toString(), fileContents);
        
        String xlsxUrl = generateXlsxFile(aiResponse, communicationId, userId);
        
        String responseWithLink = "我已经为您生成了xlsx表格，请点击以下链接下载：\n" + xlsxUrl;
        
        // 使用AIResponseFormatter美化回复，将下载链接转换为可点击的按钮
        String formattedResponse = AIResponseFormatter.formatResponse(responseWithLink);
        
        Message aiMessage = new Message();
        aiMessage.setCommunicationId(communicationId);
        aiMessage.setContent(formattedResponse);
        aiMessage.setStatus(0);
        aiMessage.setCreatedAt(new java.util.Date());
        messageRepository.save(aiMessage);
        
        return aiMessage;
    }
    
    @PostMapping("/modify-docx")
    @Operation(summary = "修改docx文档", description = "根据用户输入和现有文件内容，修改docx文档并返回下载链接")
    public Message modifyDocx(@RequestParam("communicationId") String communicationId,
                               @RequestParam("content") String content,
                               @RequestParam(value = "userId", required = false) String userId,
                               @RequestParam(value = "files", required = false) MultipartFile[] files,
                               @RequestParam(value = "fileIds", required = false) String[] fileIds) {
        // 如果没有提供userId，从Communication对象中获取
        if (userId == null || userId.isEmpty()) {
            userId = getUserIdFromCommunication(communicationId);
        }
        
        // 获取用户现有的docx文件
        Generative existingGenerative = generativeRepository.findByUserIdAndType(userId, "file");
        String existingFileContent = "";
        if (existingGenerative != null) {
            // 读取现有文件内容
            try {
                String filePath = existingGenerative.getUrl().replace("/", "");
                java.io.File file = new java.io.File(filePath);
                if (file.exists()) {
                    org.apache.poi.xwpf.usermodel.XWPFDocument document = new org.apache.poi.xwpf.usermodel.XWPFDocument(new java.io.FileInputStream(file));
                    StringBuilder contentBuilder = new StringBuilder();
                    for (org.apache.poi.xwpf.usermodel.XWPFParagraph paragraph : document.getParagraphs()) {
                        contentBuilder.append(paragraph.getText()).append("\n");
                    }
                    existingFileContent = contentBuilder.toString();
                    document.close();
                    System.out.println("读取现有docx文件内容成功，长度: " + existingFileContent.length());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
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
        
        // 获取该对话的历史消息，按照时间顺序排序
        List<Message> historyMessages = messageRepository.findByCommunicationId(communicationId);
        historyMessages.sort((m1, m2) -> m1.getCreatedAt().compareTo(m2.getCreatedAt()));
        
        // 构建完整的消息内容，包含历史对话、用户需求、现有文件内容和新上传的文件内容
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
        
        if (!existingFileContent.isEmpty()) {
            fullContent.append("现有文件内容：\n").append(existingFileContent).append("\n\n");
        }
        
        // 添加文件内容
        if (!fileContents.isEmpty()) {
            fullContent.append("以下是上传的文件内容：\n");
            for (String fileContent : fileContents) {
                fullContent.append(fileContent).append("\n\n");
            }
        }
        
        // 添加文件对应的关键词
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
        
        // 调用智谱API获取AI回复，传递完整内容和文件内容
        String aiResponse = zhipuAIService.chat(fullContent.toString(), fileContents);
        
        // 生成修改后的docx文档
        String docxUrl = generateDocxFile(aiResponse, communicationId, userId);
        
        // 构建包含下载链接的回复
        String responseWithLink = "我已经为您修改了docx文档，请点击以下链接下载：\n" + docxUrl;
        
        // 直接使用原始链接，不使用AIResponseFormatter
        // 这样可以确保链接格式正确，不会被HTML标签破坏
        
        // 保存AI回复（status=0）
        Message aiMessage = new Message();
        aiMessage.setCommunicationId(communicationId);
        aiMessage.setContent(responseWithLink);
        aiMessage.setStatus(0);
        aiMessage.setCreatedAt(new java.util.Date());
        messageRepository.save(aiMessage);
        
        return aiMessage;
    }
    
    @PostMapping("/modify-xlsx")
    @Operation(summary = "修改xlsx表格", description = "根据用户输入和现有文件内容，修改xlsx表格并返回下载链接")
    public Message modifyXlsx(@RequestParam("communicationId") String communicationId,
                               @RequestParam("content") String content,
                               @RequestParam(value = "userId", required = false) String userId,
                               @RequestParam(value = "files", required = false) MultipartFile[] files,
                               @RequestParam(value = "fileIds", required = false) String[] fileIds) {
        // 如果没有提供userId，从Communication对象中获取
        if (userId == null || userId.isEmpty()) {
            userId = getUserIdFromCommunication(communicationId);
        }
        
        // 获取用户现有的xlsx文件
        Generative existingGenerative = generativeRepository.findByUserIdAndType(userId, "table");
        String existingFileContent = "";
        if (existingGenerative != null) {
            // 读取现有文件内容
            try {
                String filePath = existingGenerative.getUrl().replace("/", "");
                java.io.File file = new java.io.File(filePath);
                if (file.exists()) {
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
                    existingFileContent = contentBuilder.toString();
                    workbook.close();
                    System.out.println("读取现有xlsx文件内容成功，长度: " + existingFileContent.length());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
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
        
        // 获取该对话的历史消息，按照时间顺序排序
        List<Message> historyMessages = messageRepository.findByCommunicationId(communicationId);
        historyMessages.sort((m1, m2) -> m1.getCreatedAt().compareTo(m2.getCreatedAt()));
        
        // 构建完整的消息内容，包含历史对话、用户需求、现有文件内容和新上传的文件内容
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
        
        if (!existingFileContent.isEmpty()) {
            fullContent.append("现有文件内容：\n").append(existingFileContent).append("\n\n");
        }
        
        // 添加文件内容
        if (!fileContents.isEmpty()) {
            fullContent.append("以下是上传的文件内容：\n");
            for (String fileContent : fileContents) {
                fullContent.append(fileContent).append("\n\n");
            }
        }
        
        // 添加文件对应的关键词
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
        
        // 调用智谱API获取AI回复，传递完整内容和文件内容
        String aiResponse = zhipuAIService.chat(fullContent.toString(), fileContents);
        
        // 生成修改后的xlsx文档
        String xlsxUrl = generateXlsxFile(aiResponse, communicationId, userId);
        
        // 构建包含下载链接的回复
        String responseWithLink = "我已经为您修改了xlsx表格，请点击以下链接下载：\n" + xlsxUrl;
        
        // 直接使用原始链接，不使用AIResponseFormatter
        // 这样可以确保链接格式正确，不会被HTML标签破坏
        
        // 保存AI回复（status=0）
        Message aiMessage = new Message();
        aiMessage.setCommunicationId(communicationId);
        aiMessage.setContent(responseWithLink);
        aiMessage.setStatus(0);
        aiMessage.setCreatedAt(new java.util.Date());
        messageRepository.save(aiMessage);
        
        return aiMessage;
    }
    
    private String generateXlsxFile(String content, String communicationId, String userId) {
        try {
            // 使用XlsxFormatter美化内容
            String formattedContent = AIResponseFormatter.formatXlsx(content);
            
            // 使用UUID生成唯一的文件名称，避免使用可能包含中文字符的communicationId
            String fileName = "table_" + java.util.UUID.randomUUID().toString() + "_" + System.currentTimeMillis() + ".xlsx";
            String uploadDir = "uploads/generative";
            String filePath = uploadDir + "/" + fileName;
            
            // 确保上传目录存在
            java.io.File dir = new java.io.File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            
            org.apache.poi.ss.usermodel.Workbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook();
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("分析结果");
            
            // 设置默认列宽
            sheet.setDefaultColumnWidth(15);
            
            // 创建单元格样式
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
                    // 为第一行设置标题样式
                    if (isFirstRow) {
                        cell.setCellStyle(headerStyle);
                    } else {
                        cell.setCellStyle(dataStyle);
                    }
                }
                isFirstRow = false;
            }
            
            // 自动调整列宽
            for (int i = 0; i < 10; i++) { // 假设最多10列
                sheet.autoSizeColumn(i);
            }
            
            java.io.FileOutputStream fos = new java.io.FileOutputStream(filePath);
            workbook.write(fos);
            workbook.close();
            fos.close();
            
            // 构建文件相对路径
            String relativeUrl = "/" + filePath;
            
            // 检查是否已存在同类型的文件
            Generative existingGenerative = generativeRepository.findByUserIdAndType(userId, "table");
            String fileId;
            if (existingGenerative != null) {
                // 更新现有记录，但保留旧文件
                existingGenerative.setName(fileName);
                existingGenerative.setUrl(relativeUrl);
                existingGenerative.setFileType(1); // 1 for xlsx
                existingGenerative.setUpdatedAt(new java.util.Date());
                generativeRepository.save(existingGenerative);
                fileId = existingGenerative.getId();
            } else {
                // 创建新记录
                Generative generative = new Generative(userId, fileName, relativeUrl, "table");
                generative.setFileType(1); // 1 for xlsx
                generative = generativeRepository.save(generative);
                fileId = generative.getId();
            }
            
            // 返回基于GenerativeController的文件下载URL
            return "http://localhost:8081/api/generative/file/" + fileId;
        } catch (Exception e) {
            e.printStackTrace();
            return "生成表格失败";
        }
    }
}