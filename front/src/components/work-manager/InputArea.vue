<template>
  <div>
    <div v-if="inputAreaVisible" class="input-area">
      <!-- 文件暂存区 -->
      <div v-if="stagedFiles.length > 0" class="file-staging-area">
        <div class="file-list">
          <div v-for="(file, index) in stagedFiles" :key="file.id || file.name || index" class="file-item">
            <span class="file-name">{{ file.name }}</span>
            <button class="remove-btn" @click="removeFile(index)">❌</button>
          </div>
        </div>
      </div>
      <!-- 输入区域 -->
      <div class="input-section">
        <textarea 
          class="chat-input" 
          placeholder="输入消息..."
          v-model="messageInput"
          @keyup.enter.ctrl="sendMessage"
          :disabled="internalLoading"
        ></textarea>
        <div v-if="internalLoading" class="loading-overlay">
          文件添加中...
        </div>
      </div>
      <!-- 按钮区域 -->
      <div class="button-section">
        <div class="action-buttons">
          <button class="action-btn small-btn" @click="generateDocx" :disabled="internalLoading">docx</button>
          <button class="action-btn small-btn" @click="generateXlsx" :disabled="internalLoading">xlsx</button>
          <button class="hide-btn small-btn" @click="toggleInputArea" :disabled="internalLoading">隐藏</button>
        </div>
        <div class="send-buttons">
          <button class="add-btn small-btn" @click="triggerFileInput" :disabled="internalLoading">+</button>
          <button class="send-btn small-btn" @click="sendMessage" :disabled="internalLoading">⬆</button>
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
    </div>
    <!-- 显示按钮 -->
    <div v-if="!inputAreaVisible" class="show-input-btn">
      <button class="show-btn small-btn" @click="toggleInputArea">显示</button>
    </div>
  </div>
</template>

<script>
import { isSupportedFileFormat } from '../../utils/utils'

export default {
  name: 'WorkManagerInputArea',
  props: {
    loading: {
      type: Boolean,
      default: false
    },
    stagedFiles: {
      type: Array,
      default: () => []
    }
  },
  data() {
    return {
      messageInput: '',
      inputAreaVisible: true,
      internalLoading: false
    }
  },
  watch: {
    loading: {
      handler(newVal) {
        this.internalLoading = newVal
      },
      immediate: true
    }
  },
  methods: {
    toggleInputArea() {
      this.inputAreaVisible = !this.inputAreaVisible
    },
    
    triggerFileInput() {
      this.$refs.fileInput.click()
    },
    
    async handleFileInput(event) {
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
        
        try {
          this.internalLoading = true
          // 上传文件到后端临时存储
          const formData = new FormData()
          formData.append('file', file)
          formData.append('userId', localStorage.getItem('userId') || 'anonymous')
          
          const response = await fetch('/api/temporary/upload', {
            method: 'POST',
            body: formData
          })
          
          if (response.ok) {
            const data = await response.json()
            this.$emit('add-file', data)
          } else {
            alert('文件上传失败')
          }
        } catch (error) {
          console.error('文件上传失败:', error)
          alert('文件上传失败')
        } finally {
          this.internalLoading = false
        }
      }
      
      event.target.value = ''
    },
    
    async removeFile(index) {
      const fileToRemove = this.stagedFiles[index]
      if (fileToRemove && fileToRemove.id) {
        try {
          this.internalLoading = true
          // 从后端删除文件
          await fetch(`/api/temporary/${fileToRemove.id}`, {
            method: 'DELETE'
          })
        } catch (error) {
          console.error('文件删除失败:', error)
        } finally {
          this.internalLoading = false
        }
      }
      this.$emit('remove-file', index)
    },
    
    async sendMessage() {
      if (!this.messageInput.trim() && this.stagedFiles.length === 0) {
        return
      }
      
      console.log('InputArea: 发送消息，内容:', this.messageInput, '文件:', this.stagedFiles)
      this.$emit('send-message', this.messageInput, this.stagedFiles)
      this.messageInput = ''
    },
    
    generateDocument(type) {
      if (!this.messageInput.trim() && this.stagedFiles.length === 0) {
        alert('请输入要分析的内容或上传文件')
        return
      }
      
      this.$emit(`generate-${type}`, this.messageInput, this.stagedFiles)
      this.messageInput = ''
    },
    
    async generateDocx() {
      this.generateDocument('docx')
    },
    
    async generateXlsx() {
      this.generateDocument('xlsx')
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
  min-height: 120px;
  max-height: 400px;
  background-color: #f8fafc;
  border: 2px solid #4a90e2;
  border-radius: 8px;
  padding: 15px;
  box-sizing: border-box;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 10px;
  box-shadow: 0 2px 4px rgba(74, 144, 226, 0.1);
}

/* 文件暂存区样式 */
.file-staging-area {
  background-color: #e6f0fa;
  border: 1px solid #4a90e2;
  border-radius: 4px;
  padding: 10px;
  box-sizing: border-box;
  box-shadow: inset 0 1px 3px rgba(74, 144, 226, 0.1);
}

.file-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.file-item {
  display: flex;
  align-items: center;
  background-color: white;
  border: 1px solid #d0e3f8;
  border-radius: 4px;
  padding: 4px 8px;
  font-size: 12px;
  max-width: 200px;
  overflow: hidden;
}

.file-name {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-right: 4px;
}

.remove-btn {
  background: none;
  border: none;
  font-size: 10px;
  cursor: pointer;
  padding: 0;
  width: 16px;
  height: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.remove-btn:hover {
  background-color: #f0f0f0;
  border-radius: 50%;
}

/* 输入区域样式 */
.input-section {
  position: relative;
  flex: 1;
  min-height: 60px;
}

.chat-input {
  width: 100%;
  min-height: 60px;
  max-height: 200px;
  border: none;
  outline: none;
  resize: none;
  font-size: 16px;
  line-height: 1.5;
  padding: 10px;
  box-sizing: border-box;
  border: 1px solid #e6f0fa;
  border-radius: 4px;
  background-color: white;
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
  border-radius: 4px;
}

/* 按钮区域样式 */
.button-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 5px;
}

.action-buttons {
  display: flex;
  gap: 8px;
}

.send-buttons {
  display: flex;
  gap: 8px;
  align-items: center;
}

/* 通用按钮样式 */
.small-btn {
  padding: 6px 12px;
  background-color: #4a90e2;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
  transition: background-color 0.3s ease;
}

/* 发送按钮样式 */
.send-btn {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  font-weight: bold;
  padding: 0;
  background-color: #4a90e2;
  color: white;
  border: none;
  cursor: pointer;
  transition: background-color 0.3s ease;
}

/* 加号按钮样式 */
.add-btn {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  font-weight: bold;
  padding: 0;
  background-color: #4a90e2;
  color: white;
  border: none;
  cursor: pointer;
  transition: background-color 0.3s ease;
}

.small-btn:hover {
  background-color: #357abd;
}

.send-btn:hover {
  background-color: #357abd;
}

.add-btn:hover {
  background-color: #357abd;
}

.small-btn:disabled {
  background-color: #a8c8e8;
  cursor: not-allowed;
}

.send-btn:disabled {
  background-color: #a8c8e8;
  cursor: not-allowed;
}

.add-btn:disabled {
  background-color: #a8c8e8;
  cursor: not-allowed;
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
</style>