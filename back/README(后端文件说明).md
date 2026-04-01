# 后端项目结构说明

## 项目概览

本后端项目是一个基于Spring Boot的智能文档处理助手服务，主要功能包括用户管理、对话管理、消息处理、文件管理和AI交互。项目使用MongoDB作为数据库，集成了智谱AI服务，支持文件解析和文档生成。

## 目录结构

```
back/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/demo/
│   │   │       ├── controller/        # 控制器层
│   │   │       │   ├── CommunicationController.java # 对话管理控制器
│   │   │       │   ├── MessageController.java        # 消息管理控制器
│   │   │       │   ├── RepositoryController.java     # 仓库管理控制器
│   │   │       │   └── UserController.java          # 用户管理控制器
│   │   │       ├── model/             # 数据模型
│   │   │       │   ├── Communication.java           # 对话模型
│   │   │       │   ├── Message.java                # 消息模型
│   │   │       │   ├── Repository.java             # 仓库模型
│   │   │       │   └── User.java                  # 用户模型
│   │   │       ├── repository/        # 数据访问层
│   │   │       │   ├── CommunicationRepository.java # 对话数据访问
│   │   │       │   ├── MessageRepository.java        # 消息数据访问
│   │   │       │   ├── RepositoryRepository.java     # 仓库数据访问
│   │   │       │   └── UserRepository.java          # 用户数据访问
│   │   │       ├── service/           # 业务逻辑层
│   │   │       │   └── ZhipuAIService.java          # 智谱AI服务
│   │   │       ├── util/              # 工具类
│   │   │       │   ├── AIResponseFormatter.java     # AI响应格式化工具
│   │   │       │   ├── AIResponseFormatter.py       # Python版本的AI响应格式化工具
│   │   │       │   └── FileParser.java              # 文件解析工具
│   │   │       └── BackApplication.java             # 应用入口
│   │   └── resources/
│   │       └── application.properties  # 配置文件
│   └── test/                          # 测试代码
├── target/                            # 构建输出
├── .classpath
├── .factorypath
├── .project
└── pom.xml                            # Maven配置
```

## 文件说明

### 1. 入口文件

#### BackApplication.java
- **作用**：应用的入口文件，负责初始化Spring Boot应用和配置资源处理器
- **管理内容**：
  - 启动Spring Boot应用
  - 配置静态资源处理器，映射上传文件目录

### 2. 控制器层

#### UserController.java
- **作用**：用户管理控制器，处理用户相关的HTTP请求
- **管理内容**：
  - 获取所有用户
  - 根据ID获取用户
  - 创建新用户
  - 更新用户信息
  - 用户登录验证

#### CommunicationController.java
- **作用**：对话管理控制器，处理对话相关的HTTP请求
- **管理内容**：
  - 获取所有对话
  - 根据ID获取对话
  - 根据用户ID获取对话
  - 创建新对话
  - 更新对话信息
  - 删除对话

#### MessageController.java
- **作用**：消息管理控制器，处理消息相关的HTTP请求
- **管理内容**：
  - 获取所有消息
  - 根据ID获取消息
  - 根据对话ID获取消息
  - 创建新消息
  - 聊天接口（与AI交互）
  - 生成docx文档
  - 生成xlsx表格
  - 更新消息
  - 删除消息

#### RepositoryController.java
- **作用**：仓库管理控制器，处理文件仓库相关的HTTP请求
- **管理内容**：
  - 根据用户ID获取文件列表
  - 上传文件
  - 删除文件

### 3. 数据模型层

#### User.java
- **作用**：用户数据模型，定义用户实体的属性
- **管理内容**：
  - id：用户唯一标识
  - account：用户账号
  - name：用户姓名
  - password：用户密码
  - create_at：创建时间

#### Communication.java
- **作用**：对话数据模型，定义对话实体的属性
- **管理内容**：
  - id：对话唯一标识
  - userId：用户ID
  - name：对话名称
  - createdAt：创建时间

#### Message.java
- **作用**：消息数据模型，定义消息实体的属性
- **管理内容**：
  - id：消息唯一标识
  - communicationId：对话ID
  - status：消息状态（0：AI回复，1：用户消息）
  - content：消息内容
  - createdAt：创建时间

#### Repository.java
- **作用**：仓库数据模型，定义文件仓库实体的属性
- **管理内容**：
  - id：文件唯一标识
  - userId：用户ID
  - name：文件名
  - url：文件路径

### 4. 数据访问层

#### UserRepository.java
- **作用**：用户数据访问接口，定义用户数据的CRUD操作
- **管理内容**：
  - 继承MongoRepository，提供基本CRUD操作
  - 自定义方法：根据账号和密码查询用户

#### CommunicationRepository.java
- **作用**：对话数据访问接口，定义对话数据的CRUD操作
- **管理内容**：
  - 继承MongoRepository，提供基本CRUD操作
  - 自定义方法：根据用户ID查询对话列表

#### MessageRepository.java
- **作用**：消息数据访问接口，定义消息数据的CRUD操作
- **管理内容**：
  - 继承MongoRepository，提供基本CRUD操作
  - 自定义方法：
    - 根据对话ID查询消息
    - 根据对话ID和状态查询消息
    - 根据对话ID查询最新消息
    - 根据状态查询消息
    - 根据时间范围查询消息

#### RepositoryRepository.java
- **作用**：仓库数据访问接口，定义文件仓库数据的CRUD操作
- **管理内容**：
  - 继承MongoRepository，提供基本CRUD操作
  - 自定义方法：根据用户ID查询文件列表

### 5. 业务逻辑层

#### ZhipuAIService.java
- **作用**：智谱AI服务，处理与智谱AI的交互
- **管理内容**：
  - 调用智谱AI API进行聊天
  - 处理文件内容，将其添加到对话中
  - 解析AI响应

### 6. 工具类

#### AIResponseFormatter.java
- **作用**：AI响应格式化工具，美化AI的回复
- **管理内容**：
  - 调用Python脚本对AI回复进行格式化
  - 处理脚本执行异常

#### FileParser.java
- **作用**：文件解析工具，解析不同类型的文件
- **管理内容**：
  - 解析文本文件（.txt, .md）
  - 解析Word文档（.docx）
  - 解析Excel文件（.xlsx）

## 功能模块

### 1. 用户管理
- **功能**：用户登录、用户CRUD操作
- **实现文件**：UserController.java, User.java, UserRepository.java

### 2. 对话管理
- **功能**：创建对话、获取对话列表、更新对话、删除对话
- **实现文件**：CommunicationController.java, Communication.java, CommunicationRepository.java

### 3. 消息管理
- **功能**：发送消息、获取消息列表、与AI交互、生成文档
- **实现文件**：MessageController.java, Message.java, MessageRepository.java, ZhipuAIService.java

### 4. 文件管理
- **功能**：上传文件、获取文件列表、删除文件
- **实现文件**：RepositoryController.java, Repository.java, RepositoryRepository.java

### 5. 文件解析
- **功能**：解析不同类型的文件内容
- **实现文件**：FileParser.java

### 6. AI交互
- **功能**：与智谱AI进行交互，处理AI响应
- **实现文件**：ZhipuAIService.java, AIResponseFormatter.java

## 技术栈

- **框架**：Spring Boot 3.0.2
- **语言**：Java 17
- **数据库**：MongoDB
- **构建工具**：Maven
- **API文档**：Springdoc OpenAPI
- **文件处理**：Apache POI
- **AI服务**：智谱AI

## 运行项目

1. 确保MongoDB服务已启动
2. 安装依赖：`mvn clean install`
3. 启动应用：`mvn spring-boot:run`
4. 访问API文档：`http://localhost:8081/swagger-ui.html`

## 注意事项

- 项目需要MongoDB数据库支持
- 智谱AI API需要API_KEY配置
- 上传文件会存储在`uploads`目录中
- 支持的文件格式：docx、md、xlsx、txt
- 前端项目需要在`http://localhost:8080`运行以避免跨域问题