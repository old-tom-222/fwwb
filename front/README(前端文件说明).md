# 前端项目结构说明

## 项目概览

本前端项目是一个基于Vue 3的智能文档处理助手，主要功能包括用户管理、对话管理、文件处理和AI交互。项目采用前后端分离架构，前端通过Axios调用后端API。

## 目录结构

```
front/
├── public/              # 静态资源（不用管）
│   ├── favicon.ico      # 网站图标（不用管）
│   └── index.html       # HTML模板（不用管）
├── src/                 # 源代码
│   ├── assets/          # 静态资源（不用管）
│   │   └── logo.png     # 项目logo（不用管）
│   ├── components/      # 组件
│   │   ├── work-manager/        # 工作管理相关组件
│   │   │   ├── ChatArea.vue     # 聊天区域组件
│   │   │   ├── DeleteModal.vue  # 删除确认模态框
│   │   │   ├── LoginModal.vue   # 登录模态框
│   │   │   ├── RenameModal.vue  # 重命名模态框
│   │   │   ├── RepositoryModal.vue # 仓库模态框
│   │   │   └── Sidebar.vue      # 侧边栏组件
│   │   ├── work-manager-core/   # 工作管理核心文件
│   │   │   ├── WorkManager.css  # 样式文件
│   │   │   ├── WorkManager.html # 模板文件
│   │   │   └── WorkManager.js   # 逻辑文件
│   │   ├── UserManager.vue      # 用户管理组件
│   │   └── WorkManager.vue      # 工作管理入口组件
│   ├── utils/           # 工具函数
│   │   ├── api.js       # API调用封装
│   │   └── utils.js     # 通用工具函数
│   ├── App.vue          # 根组件
│   └── main.js          # 应用入口
├── babel.config.js      # Babel配置（不用管）
├── jsconfig.json        # JavaScript配置（不用管）
├── package-lock.json    # 依赖锁定文件（不用管）
├── package.json         # 项目配置和依赖（不用管）
└── vue.config.js        # Vue配置（不用管）
```

## 文件说明

### 1. 入口文件

#### main.js
- **作用**：应用的入口文件，负责初始化Vue应用、配置路由
- **管理内容**：
  - 导入Vue、App组件和路由
  - 配置路由规则（首页和用户管理页面）
  - 创建Vue应用实例并挂载到DOM

### 2. 根组件

#### App.vue
- **作用**：应用的根组件，提供基本布局
- **管理内容**：
  - 包含路由视图组件 `<router-view />`
  - 定义全局样式

### 3. 主要组件

#### UserManager.vue
- **作用**：用户管理页面，用于显示和添加用户
- **管理内容**：
  - 显示用户列表
  - 提供添加用户的表单
  - 调用后端API获取和添加用户数据

#### WorkManager.vue
- **作用**：工作管理页面的入口组件
- **管理内容**：
  - 引用工作管理核心文件
  - 整合工作管理相关组件

#### WorkManager.html
- **作用**：工作管理组件的模板文件
- **管理内容**：
  - 侧边栏（Sidebar）
  - 聊天区域（ChatArea）
  - 登录模态框（LoginModal）
  - 重命名模态框（RenameModal）
  - 删除模态框（DeleteModal）
  - 仓库模态框（RepositoryModal）

#### WorkManager.js
- **作用**：工作管理组件的逻辑文件
- **管理内容**：
  - 用户登录/注销
  - 对话管理（创建、选择、重命名、删除）
  - 消息管理（发送、获取）
  - 文件管理（上传、删除、处理）
  - 文档生成（docx、xlsx）

### 4. 工作管理子组件

#### Sidebar.vue
- **作用**：侧边栏组件，显示用户信息和历史对话
- **管理内容**：
  - 显示用户登录状态
  - 提供云仓库和新对话按钮
  - 显示历史对话列表
  - 提供对话操作（选择、重命名、删除）

#### ChatArea.vue
- **作用**：聊天区域组件，用于消息交互和文件管理
- **管理内容**：
  - 显示消息列表
  - 提供消息输入框
  - 文件暂存区管理
  - 文档生成按钮

### 5. 工具文件

#### api.js
- **作用**：API调用封装，提供与后端交互的方法
- **管理内容**：
  - userApi：用户登录
  - communicationApi：对话管理
  - messageApi：消息管理
  - repositoryApi：仓库管理

#### utils.js
- **作用**：通用工具函数
- **管理内容**：
  - 时间格式化
  - 问候语生成
  - 文件格式检查
  - 登录状态管理
  - 云仓库文件处理

## 功能模块

### 1. 用户管理
- **功能**：用户登录、用户列表显示、添加用户
- **实现文件**：UserManager.vue

### 2. 对话管理
- **功能**：创建对话、选择对话、重命名对话、删除对话
- **实现文件**：WorkManager.js、Sidebar.vue

### 3. 消息管理
- **功能**：发送消息、接收消息、显示消息历史
- **实现文件**：WorkManager.js、ChatArea.vue

### 4. 文件管理
- **功能**：文件上传、文件暂存、文件删除、云仓库管理
- **实现文件**：WorkManager.js、ChatArea.vue、RepositoryModal.vue

### 5. 文档生成
- **功能**：生成docx文档、生成xlsx表格
- **实现文件**：WorkManager.js、ChatArea.vue

## 技术栈

- **框架**：Vue 3
- **路由**：Vue Router
- **HTTP客户端**：Axios
- **构建工具**：Vue CLI

## 运行项目

1. 安装依赖：`npm install`
2. 启动开发服务器：`npm run serve`
3. 构建生产版本：`npm run build`

## 注意事项

- 项目需要后端服务支持，后端API地址配置在api.js中
- 支持的文件格式：docx、md、xlsx、txt
- 文件暂存区最多支持5个文件
- 登录状态通过localStorage存储