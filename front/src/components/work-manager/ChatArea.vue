<template>
  <main class="content">
    <div v-if="!selectedCommunicationId" class="welcome">
      <h1>{{ greeting }}</h1>
    </div>
    <div v-else class="chat-area">
      <div class="chat-header">
        <h2>{{ selectedCommunicationName }}</h2>
      </div>
      <div class="messages-container">
        <div
          v-for="message in sortedMessages"
          :key="message.id"
          class="message-container"
          :class="{ 'status-1': message.status === 1 }"
        >
          <div class="message-avatar">
            <div v-if="message.status === 0" class="ai-avatar">🤖</div>
            <div v-else class="user-avatar">👤</div>
          </div>
          <div class="message">
            <div class="message-content" v-html="message.content"></div>
            <div class="message-time">{{ formatTime(message.createdAt) }}</div>
          </div>
        </div>
      </div>
      <div v-if="inputAreaVisible" class="input-area">
        <!-- 文件暂存区 -->
        <div class="file-staging-area">
          <div class="file-staging-header">
            <span>文件暂存区</span>
            <button class="import-btn main-btn" @click="triggerFileInput">导入文件</button>
          </div>
          <div class="file-list">
            <div v-for="(file, index) in stagedFiles" :key="index" class="file-item">
              <span class="file-name">{{ file.name }}</span>
              <button class="remove-btn small-btn" @click="removeFile(index)">删除</button>
            </div>
            <div v-if="stagedFiles.length === 0" class="no-files">
              暂无文件
            </div>
          </div>
        </div>
        <!-- 隐藏的文件输入元素 -->
        <input 
          ref="fileInput" 
          type="file" 
          multiple 
          style="display: none" 
          @change="handleFileInput"
          accept=".docx,.md,.xlsx,.txt"
        />
        <!-- 聊天输入区域 -->
        <div class="chat-input-container">
          <textarea 
            class="chat-input" 
            placeholder="输入消息..."
            v-model="messageInput"
            @keyup.enter.ctrl="sendMessage"
            :disabled="loading"
          ></textarea>
          <div v-if="loading" class="loading-overlay">
            正在思考中...
          </div>
          <button class="send-btn main-btn" @click="sendMessage" :disabled="loading">发送</button>
          <button class="send-btn main-btn" @click="generateDocx" :disabled="loading">docx</button>
          <button class="send-btn main-btn" @click="generateXlsx" :disabled="loading">xlsx</button>
          <button class="hide-btn main-btn" @click="toggleInputArea" :disabled="loading">隐藏</button>
        </div>
      </div>
      <!-- 显示按钮 -->
      <div v-if="!inputAreaVisible" class="show-input-btn">
        <button class="show-btn main-btn" @click="toggleInputArea">显示</button>
      </div>
    </div>
  </main>
</template>

<script>
import { formatTime, getGreeting, isSupportedFileFormat } from '../../utils/utils'

export default {
  name: 'WorkManagerChatArea',
  props: {
    selectedCommunicationId: {
      type: String,
      default: null
    },
    selectedCommunicationName: {
      type: String,
      default: ''
    },
    messages: {
      type: Array,
      default: () => []
    },
    stagedFiles: {
      type: Array,
      default: () => []
    },
    loading: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      messageInput: '',
      inputAreaVisible: true
    }
  },
  computed: {
    greeting() {
      return getGreeting()
    },
    sortedMessages() {
      return [...this.messages].sort((a, b) => {
        return new Date(a.createdAt) - new Date(b.createdAt)
      })
    }
  },
  methods: {
    formatTime,
    
    triggerFileInput() {
      this.$refs.fileInput.click()
    },
    
    handleFileInput(event) {
      const files = event.target.files
      if (files.length === 0) return
      
      if (this.stagedFiles.length + files.length > 5) {
        alert('最多只能暂存5个文件')
        return
      }
      
      for (let i = 0; i < files.length; i++) {
        const file = files[i]
        if (!isSupportedFileFormat(file.name)) {
          alert('只能导入docx、md、xlsx、txt格式的文件')
          return
        }
        
        this.$emit('add-file', file)
      }
      
      event.target.value = ''
    },
    
    removeFile(index) {
      this.$emit('remove-file', index)
    },
    
    toggleInputArea() {
      this.inputAreaVisible = !this.inputAreaVisible
    },
    
    async sendMessage() {
      if (!this.messageInput.trim() && this.stagedFiles.length === 0) {
        return
      }
      
      this.$emit('send-message', this.messageInput, this.stagedFiles)
      this.messageInput = ''
    },
    
    async generateDocx() {
      if (!this.messageInput.trim() && this.stagedFiles.length === 0) {
        alert('请输入要分析的内容或上传文件')
        return
      }
      
      this.$emit('generate-docx', this.messageInput, this.stagedFiles)
      this.messageInput = ''
    },
    
    async generateXlsx() {
      if (!this.messageInput.trim() && this.stagedFiles.length === 0) {
        alert('请输入要分析的内容或上传文件')
        return
      }
      
      this.$emit('generate-xlsx', this.messageInput, this.stagedFiles)
      this.messageInput = ''
    }
  }
}
</script>

<style scoped>
.content {
  position: absolute;
  left: 285px;
  top: 0;
  right: 10px;
  bottom: 0;
  padding: 5px;
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.welcome h1 {
  margin-top: 40vh;
}

.chat-area {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.chat-header {
  padding: 10px;
  border-bottom: 1px solid #ddd;
  text-align: left;
}

.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  text-align: left;
}

.message-container {
  display: flex;
  align-items: flex-start;
  margin-bottom: 15px;
  max-width: 80%;
}

.message-container.status-1 {
  margin-left: auto;
  flex-direction: row-reverse;
}

.message-avatar {
  margin: 0 10px;
}

.ai-avatar, .user-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  background-color: #f0f0f0;
  border: 1px solid #e0e0e0;
}

.ai-avatar {
  background-color: #e6f0fa;
  border-color: #4a90e2;
}

.user-avatar {
  background-color: #f8fafc;
  border-color: #4a90e2;
}

.message {
  max-width: 70%;
  padding: 10px;
  border-radius: 8px;
  background-color: #f8fafc;
  text-align: left;
  border: 1px solid #e6f0fa;
}

.message-container.status-1 .message {
  background-color: #e6f0fa;
  text-align: right;
  border: 1px solid #4a90e2;
}

.message-content {
  font-size: 16px;
  margin-bottom: 5px;
}

.message-time {
  font-size: 12px;
  color: #666;
}

.input-area {
  position: absolute;
  bottom: 50px;
  left: 100px;
  right: 100px;
  height: 200px;
  background-color: #f8fafc;
  border: 2px solid #4a90e2;
  border-radius: 8px;
  padding: 15px;
  box-sizing: border-box;
  overflow-y: hidden;
  display: flex;
  flex-direction: column;
  box-shadow: 0 2px 4px rgba(74, 144, 226, 0.1);
}

/* 文件暂存区样式 */
.file-staging-area {
  background-color: #e6f0fa;
  border: 1px solid #4a90e2;
  border-radius: 4px;
  padding: 10px;
  margin-bottom: 10px;
  width: 100%;
  box-sizing: border-box;
  box-shadow: inset 0 1px 3px rgba(74, 144, 226, 0.1);
}

.file-staging-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 5px;
  font-weight: bold;
}

.import-btn {
  padding: 2px 6px;
  font-size: 12px;
  border-radius: 3px;
}

.file-list {
  max-height: 60px;
  overflow-y: auto;
}

.file-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 3px;
  font-size: 14px;
}

.file-name {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.remove-btn {
  padding: 1px 4px;
  font-size: 10px;
  background-color: #e0e0e0;
  color: #333;
  border: none;
  border-radius: 2px;
  cursor: pointer;
}

.remove-btn:hover {
  background-color: #d0d0d0;
}

.no-files {
  font-style: italic;
  color: #666;
  font-size: 14px;
}

/* 聊天输入区域样式 */
.chat-input-container {
  display: flex;
  align-items: stretch;
  flex: 1;
  position: relative;
}

.chat-input {
  flex: 1;
  height: 100%;
  border: none;
  outline: none;
  resize: none;
  font-size: 16px;
  line-height: 1.5;
  padding: 5px;
  box-sizing: border-box;
}

/* 加载中覆盖层样式 */
.loading-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(255, 255, 255, 0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  color: #666;
  z-index: 10;
}

.send-btn {
  margin-left: 10px;
  height: 80%;
}

.hide-btn {
  margin-left: 10px;
  height: 80%;
}

/* 显示按钮样式 */
.show-input-btn {
  position: fixed;
  bottom: 20px;
  right: 20px;
  z-index: 1000;
}

.show-btn {
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

/* 通用按钮样式 */
.main-btn {
  padding: 10px 20px;
  background-color: #4a90e2;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.3s ease;
}

.main-btn:hover {
  background-color: #357abd;
}

.main-btn:disabled {
  background-color: #a8c8e8;
  cursor: not-allowed;
}

.small-btn {
  padding: 2px 6px;
  font-size: 12px;
  border: 1px solid #ddd;
  border-radius: 3px;
  cursor: pointer;
  transition: background-color 0.3s ease;
}
</style>