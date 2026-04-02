<template>
  <div>
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
        <button class="send-btn main-btn" @click="sendMessage" :disabled="loading">⬆</button>
        <button class="action-btn main-btn" @click="generateDocx" :disabled="loading">docx</button>
        <button class="action-btn main-btn" @click="generateXlsx" :disabled="loading">xlsx</button>
        <button class="hide-btn main-btn" @click="toggleInputArea" :disabled="loading">隐藏</button>
      </div>
    </div>
    <!-- 显示按钮 -->
    <div v-if="!inputAreaVisible" class="show-input-btn">
      <button class="show-btn main-btn" @click="toggleInputArea">显示</button>
    </div>
  </div>
</template>

<script>
import { isSupportedFileFormat } from '../../utils/utils'

export default {
  name: 'WorkManagerInputArea',
  props: {
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
  methods: {
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

.hide-btn, .action-btn {
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

/* 发送按钮样式 */
.send-btn {
  margin-left: 10px;
  width: 50px;
  height: 50px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  font-weight: bold;
  padding: 0;
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