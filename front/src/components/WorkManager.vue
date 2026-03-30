<template>
  <div class="work-manager">
    <nav class="sidebar">
      <h2>智能文档处理助手</h2>
      <button class="new-chat-btn" @click="openRepositoryModal">云仓库</button>
      <button class="new-chat-btn" @click="createNewCommunication">新对话</button>
      <div class="divider"></div>
      <h3 class="history-title">历史对话</h3>
      <div v-if="isLoggedIn && communications.length > 0" class="communication-list">
        <div 
          v-for="comm in communications" 
          :key="comm.id"
          class="communication-item"
          :class="{ active: selectedCommunicationId === comm.id }"
        >
          <div class="communication-name" @click="selectCommunication(comm.id)">
            {{ comm.name }}
          </div>
          <div class="communication-actions">
              <button class="rename-btn" @click.stop="openRenameModal(comm.id, comm.name)">重命名</button>
              <button class="delete-btn" @click.stop="openDeleteModal(comm.id, comm.name)">删除</button>
            </div>
        </div>
      </div>
      <div v-else-if="isLoggedIn" class="no-communications">
        暂无对话记录
      </div>
      <div class="login-status">
        <div v-if="isLoggedIn">
          <p>欢迎, {{ userName }}</p>
          <p class="user-id">ID: {{ userId }}</p>
          <button @click="logout" class="logout-btn">退出登录</button>
        </div>
        <div v-else>
          <button @click="showLoginModal = true" class="login-btn">登录</button>
        </div>
      </div>
    </nav>
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
            class="message"
            :class="{ 'status-1': message.status === 1 }"
          >
            <div class="message-content">{{ message.content }}</div>
            <div class="message-time">{{ formatTime(message.createdAt) }}</div>
          </div>
        </div>
        <div v-if="inputAreaVisible" class="input-area">
          <!-- 文件暂存区 -->
          <div class="file-staging-area">
            <div class="file-staging-header">
              <span>文件暂存区</span>
              <button class="import-btn" @click="triggerFileInput">导入文件</button>
            </div>
            <div class="file-list">
              <div v-for="(file, index) in stagedFiles" :key="index" class="file-item">
                <span class="file-name">{{ file.name }}</span>
                <button class="remove-btn" @click="removeFile(index)">删除</button>
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
            <button class="send-btn" @click="sendMessage" :disabled="loading">发送</button>
            <button class="hide-btn" @click="toggleInputArea" :disabled="loading">隐藏</button>
          </div>
        </div>
        <!-- 显示按钮 -->
        <div v-if="!inputAreaVisible" class="show-input-btn">
          <button class="show-btn" @click="toggleInputArea">显示</button>
        </div>
      </div>
    </main>
    <!-- 登录模态框 -->
    <div v-if="showLoginModal" class="modal-overlay" @click="showLoginModal = false">
      <div class="modal-content" @click.stop>
        <h2>登录</h2>
        <form @submit.prevent="login">
          <div>
            <label>账号:</label>
            <input v-model="loginForm.account" type="text" required>
          </div>
          <div>
            <label>密码:</label>
            <input v-model="loginForm.password" type="password" required>
          </div>
          <button type="submit">登录</button>
          <button type="button" @click="showLoginModal = false">取消</button>
        </form>
      </div>
    </div>
    <!-- 重命名模态框 -->
      <div v-if="showRenameModal" class="modal-overlay" @click="showRenameModal = false">
        <div class="modal-content" @click.stop>
          <h2>重命名对话</h2>
          <form @submit.prevent="renameCommunication">
            <div>
              <label>对话名称:</label>
              <input v-model="renameForm.name" type="text" required>
            </div>
            <button type="submit">确认</button>
            <button type="button" @click="showRenameModal = false">取消</button>
          </form>
        </div>
      </div>
      
      <!-- 删除模态框 -->
      <div v-if="showDeleteModal" class="modal-overlay" @click="showDeleteModal = false">
        <div class="modal-content" @click.stop>
          <h2>删除对话</h2>
          <p>确定要删除对话 "{{ deleteForm.name }}" 吗？</p>
          <div class="modal-actions">
            <button @click="deleteCommunication">确认删除</button>
            <button @click="showDeleteModal = false">取消</button>
          </div>
        </div>
      </div>
      
      <!-- 云仓库弹窗 -->
      <div v-if="showRepositoryModal" class="modal-overlay" @click="showRepositoryModal = false">
        <div class="repository-modal-content" @click.stop>
          <h2>云仓库</h2>
          <div class="repository-content">
            <div class="file-list">
              <div v-if="repositoryFiles.length > 0" class="files">
                <div v-for="file in repositoryFiles" :key="file.id" class="file-item">
                  <span class="file-name">{{ file.name }}</span>
                  <div class="file-actions">
                    <button class="view-btn" @click="viewFile(file)">查看</button>
                    <button class="add-btn" @click="addFileToStaged(file)">放入提问暂存区</button>
                    <button class="delete-btn" @click="deleteRepositoryFile(file.id)">删除</button>
                  </div>
                </div>
              </div>
              <div v-else class="no-files">暂无文件</div>
            </div>
            <div class="repository-actions">
              <input ref="repositoryFileInput" type="file" multiple style="display: none" @change="handleRepositoryFileInput" accept=".docx,.md,.xlsx,.txt">
              <button class="import-btn" @click="triggerRepositoryFileInput">导入文件</button>
              <button class="return-btn" @click="showRepositoryModal = false">返回</button>
            </div>
          </div>
        </div>
      </div>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'WorkManager',
  data() {
    return {
      isLoggedIn: false,
      userName: '',
      userId: '', // 添加：用户 ID
      showLoginModal: false,
      showRenameModal: false, // 重命名模态框显示状态
      showDeleteModal: false, // 删除模态框显示状态
      showRepositoryModal: false, // 云仓库弹窗显示状态
      repositoryFiles: [], // 云仓库文件列表
      loginForm: {
        account: '',
        password: ''
      },
      renameForm: {
        id: '', // 要重命名的对话ID
        name: '' // 新名称
      },
      deleteForm: {
        id: '', // 要删除的对话ID
        name: '' // 要删除的对话名称
      },
      communications: [], // 对话列表
      selectedCommunicationId: null, // 当前选中的对话ID
      selectedCommunicationName: '', // 当前选中的对话名称
      messages: [], // 消息列表
      messageInput: '', // 消息输入框内容
      stagedFiles: [], // 暂存的文件列表
      fileInput: null, // 文件输入元素引用
      inputAreaVisible: true, // 输入区域是否可见
      loading: false // 是否正在加载AI回复
    }
  },
  computed: {
    greeting() {
      const hour = new Date().getHours();
      if (hour >= 7 && hour < 12) return '早上好，有什么我能帮到你吗？';
      if (hour >= 12 && hour < 14) return '中午好，有什么我能帮到你吗？';
      if (hour >= 14 && hour < 18) return '下午好，有什么我能帮到你吗？';
      return '晚上好，有什么我能帮到你吗？';
    },
    sortedMessages() {
      // 按创建时间排序，老的在上面，新的在下面
      return [...this.messages].sort((a, b) => {
        return new Date(a.createdAt) - new Date(b.createdAt);
      });
    }
  },
  methods: {
    async login() {
      try {
        const response = await axios.post('http://localhost:8081/api/users/login', this.loginForm)
        if (response.data) {
          this.isLoggedIn = true
          this.userName = response.data.name || response.data.account
          this.userId = response.data.id  // 保存用户 ID

          // 持久化保存到 localStorage
          localStorage.setItem('userId', response.data.id)
          localStorage.setItem('userName', this.userName)

          this.showLoginModal = false
          this.loginForm = { account: '', password: '' }
          
          // 登录成功后获取用户的对话列表
          this.fetchCommunications()
        } else {
          alert('登录失败：账号或密码错误')
        }
      } catch (error) {
        alert('登录失败：' + error.message)
      }
    },

    // 从 localStorage 恢复登录状态
    checkLoginStatus() {
      const savedUserId = localStorage.getItem('userId')
      const savedUserName = localStorage.getItem('userName')

      if (savedUserId && savedUserName) {
        this.isLoggedIn = true
        this.userId = savedUserId
        this.userName = savedUserName
        
        // 恢复登录状态后获取用户的对话列表
        this.fetchCommunications()
      }
    },

    // 登出功能
    logout() {
      localStorage.removeItem('userId')
      localStorage.removeItem('userName')
      this.isLoggedIn = false
      this.userName = ''
      this.userId = ''
      this.communications = []
      this.selectedCommunicationId = null
      this.selectedCommunicationName = ''
      this.messages = []
    },

    // 获取用户的对话列表
    async fetchCommunications() {
      if (!this.userId) return
      
      try {
        const response = await axios.get(`http://localhost:8081/api/communications/user/${this.userId}`)
        this.communications = response.data
      } catch (error) {
        console.error('获取对话列表失败:', error)
      }
    },

    // 选择对话
    async selectCommunication(communicationId) {
      this.selectedCommunicationId = communicationId
      
      // 找到对应的对话名称
      const communication = this.communications.find(comm => comm.id === communicationId)
      if (communication) {
        this.selectedCommunicationName = communication.name
      }
      
      // 获取对话的消息
      await this.fetchMessages(communicationId)
    },

    // 获取对话的消息
    async fetchMessages(communicationId) {
      try {
        const response = await axios.get(`http://localhost:8081/api/messages/communication/${communicationId}`)
        this.messages = response.data
      } catch (error) {
        console.error('获取消息失败:', error)
      }
    },

    // 格式化时间
    formatTime(timeString) {
      const date = new Date(timeString)
      return date.toLocaleString()
    },

    // 创建新对话
    async createNewCommunication() {
      if (!this.isLoggedIn) {
        alert('请先登录')
        return
      }
      
      try {
        const newCommunication = {
          userId: this.userId,
          name: '未命名',
          createdAt: new Date().toISOString()
        }
        
        const response = await axios.post('http://localhost:8081/api/communications', newCommunication)
        
        // 重新获取对话列表
        await this.fetchCommunications()
        
        // 自动选中新创建的对话
        this.selectCommunication(response.data.id)
      } catch (error) {
        console.error('创建新对话失败:', error)
        alert('创建新对话失败: ' + error.message)
      }
    },

    // 发送消息
    async sendMessage() {
      if (!this.isLoggedIn) {
        alert('请先登录')
        return
      }
      
      if (!this.selectedCommunicationId) {
        alert('请先选择一个对话')
        return
      }
      
      if (!this.messageInput.trim() && this.stagedFiles.length === 0) {
        return
      }
      
      // 设置loading状态
      this.loading = true
      
      try {
        // 创建FormData对象
        const formData = new FormData()
        formData.append('communicationId', this.selectedCommunicationId)
        formData.append('content', this.messageInput)
        
        // 添加暂存的文件
        for (let i = 0; i < this.stagedFiles.length; i++) {
          const file = this.stagedFiles[i]
          // 检查是否是从云仓库添加的文件（只有name和url属性）
          if (file.url && !(file instanceof File)) {
            // 对于云仓库文件，我们需要先下载文件，然后再上传
            try {
              const response = await fetch(`http://localhost:8081${file.url}`)
              const blob = await response.blob()
              const fileObject = new File([blob], file.name)
              formData.append('files', fileObject)
            } catch (error) {
              console.error('下载文件失败:', error)
              alert('下载文件失败: ' + error.message)
              return
            }
          } else {
            // 对于本地文件，直接添加
            formData.append('files', file)
          }
        }
        
        // 调用后端聊天接口
        await axios.post('http://localhost:8081/api/messages/chat', formData, {
          headers: {
            'Content-Type': 'multipart/form-data'
          }
        })
        
        // 清空输入框
        this.messageInput = ''
        // 清空暂存的文件
        this.stagedFiles = []
        
        // 重新获取消息列表
        await this.fetchMessages(this.selectedCommunicationId)
      } catch (error) {
        console.error('发送消息失败:', error)
        alert('发送消息失败: ' + error.message)
      } finally {
        // 无论成功失败，都设置loading为false
        this.loading = false
      }
    },
    
    // 显示重命名模态框
    openRenameModal(communicationId, currentName) {
      this.renameForm.id = communicationId
      this.renameForm.name = currentName
      this.showRenameModal = true
    },
    
    // 显示删除模态框
    openDeleteModal(communicationId, currentName) {
      this.deleteForm.id = communicationId
      this.deleteForm.name = currentName
      this.showDeleteModal = true
    },
    
    // 提交重命名请求
    async renameCommunication() {
      if (!this.renameForm.name.trim()) {
        alert('请输入对话名称')
        return
      }
      
      try {
        // 调用后端更新对话名称的API
        await axios.put(`http://localhost:8081/api/communications/${this.renameForm.id}`, {
          name: this.renameForm.name
        })
        
        // 关闭模态框
        this.showRenameModal = false
        
        // 重新获取对话列表
        await this.fetchCommunications()
        
        // 如果当前选中的对话就是被重命名的对话，更新选中对话的名称
        if (this.selectedCommunicationId === this.renameForm.id) {
          this.selectedCommunicationName = this.renameForm.name
        }
      } catch (error) {
        console.error('重命名对话失败:', error)
        alert('重命名对话失败: ' + error.message)
      }
    },
    
    // 提交删除请求
    async deleteCommunication() {
      try {
        // 调用后端删除对话的API
        await axios.delete(`http://localhost:8081/api/communications/${this.deleteForm.id}`)
        
        // 关闭模态框
        this.showDeleteModal = false
        
        // 重新获取对话列表
        await this.fetchCommunications()
        
        // 如果当前选中的对话就是被删除的对话，清空选中状态和消息列表
        if (this.selectedCommunicationId === this.deleteForm.id) {
          this.selectedCommunicationId = null
          this.selectedCommunicationName = ''
          this.messages = []
        }
      } catch (error) {
        console.error('删除对话失败:', error)
        alert('删除对话失败: ' + error.message)
      }
    },
    
    // 触发文件选择对话框
    triggerFileInput() {
      this.$refs.fileInput.click()
    },
    
    // 处理文件选择
    handleFileInput(event) {
      const files = event.target.files
      if (files.length === 0) return
      
      // 检查文件数量
      if (this.stagedFiles.length + files.length > 5) {
        alert('最多只能暂存5个文件')
        return
      }
      
      // 检查文件格式
      for (let i = 0; i < files.length; i++) {
        const file = files[i]
        const extension = file.name.split('.').pop().toLowerCase()
        if (!['docx', 'md', 'xlsx', 'txt'].includes(extension)) {
          alert('只能导入docx、md、xlsx、txt格式的文件')
          return
        }
        
        // 添加文件到暂存区
        this.stagedFiles.push(file)
      }
      
      // 清空文件输入
      event.target.value = ''
    },
    
    // 删除暂存的文件
    removeFile(index) {
      this.stagedFiles.splice(index, 1)
    },
    
    // 切换输入区域的显示和隐藏
    toggleInputArea() {
      this.inputAreaVisible = !this.inputAreaVisible
    },
    
    // 打开云仓库弹窗
    async openRepositoryModal() {
      if (!this.isLoggedIn) {
        alert('请先登录')
        return
      }
      
      try {
        // 获取用户文件列表
        const response = await axios.get(`http://localhost:8081/api/repository/user/${this.userId}`)
        this.repositoryFiles = response.data
      } catch (error) {
        console.error('获取文件列表失败:', error)
        alert('获取文件列表失败: ' + error.message)
      }
      
      this.showRepositoryModal = true
    },
    
    // 触发云仓库文件选择对话框
    triggerRepositoryFileInput() {
      this.$refs.repositoryFileInput.click()
    },
    
    // 处理云仓库文件选择
    async handleRepositoryFileInput(event) {
      const files = event.target.files
      if (files.length === 0) return
      
      // 检查文件格式
      for (let i = 0; i < files.length; i++) {
        const file = files[i]
        const extension = file.name.split('.').pop().toLowerCase()
        if (!['docx', 'md', 'xlsx', 'txt'].includes(extension)) {
          alert('只能导入docx、md、xlsx、txt格式的文件')
          return
        }
      }
      
      // 上传文件
      for (let i = 0; i < files.length; i++) {
        const file = files[i]
        const formData = new FormData()
        formData.append('userId', this.userId)
        formData.append('file', file)
        
        try {
          await axios.post('http://localhost:8081/api/repository/upload', formData, {
            headers: {
              'Content-Type': 'multipart/form-data'
            }
          })
        } catch (error) {
          console.error('上传文件失败:', error)
          alert('上传文件失败: ' + error.message)
        }
      }
      
      // 重新获取文件列表
      try {
        const response = await axios.get(`http://localhost:8081/api/repository/user/${this.userId}`)
        this.repositoryFiles = response.data
      } catch (error) {
        console.error('获取文件列表失败:', error)
        alert('获取文件列表失败: ' + error.message)
      }
      
      // 清空文件输入
      event.target.value = ''
    },
    
    // 查看文件
    viewFile(file) {
      // 直接打开文件URL
      window.open(`http://localhost:8081${file.url}`, '_blank')
    },
    
    // 将文件放入提问暂存区
    addFileToStaged(file) {
      // 检查暂存区文件数量
      if (this.stagedFiles.length >= 5) {
        alert('暂存区最多只能存储5个文件')
        return
      }
      
      // 检查文件是否已经在暂存区
      const isFileExist = this.stagedFiles.some(stagedFile => stagedFile.name === file.name)
      if (isFileExist) {
        alert('该文件已在暂存区中')
        return
      }
      
      // 创建文件对象并添加到暂存区
      const fileObject = {
        name: file.name,
        url: file.url
      }
      this.stagedFiles.push(fileObject)
      alert('文件已添加到暂存区')
    },
    
    // 删除云仓库中的文件
    async deleteRepositoryFile(fileId) {
      if (confirm('确定要删除这个文件吗？')) {
        try {
          await axios.delete(`http://localhost:8081/api/repository/${fileId}`)
          // 重新获取文件列表
          const response = await axios.get(`http://localhost:8081/api/repository/user/${this.userId}`)
          this.repositoryFiles = response.data
          alert('文件删除成功')
        } catch (error) {
          console.error('删除文件失败:', error)
          alert('删除文件失败: ' + error.message)
        }
      }
    }
  },
  mounted() {
    // 页面加载时检查是否已有登录状态
    this.checkLoginStatus()
  }
}
</script>

<style scoped>
.work-manager {
  display: flex;
  min-height: 80vh;
}

.sidebar {
  position: fixed;
  left: 0;
  top: 0;
  bottom: 0;
  height: 100vh;
  width: 275px;
  background-color: #f4f4f4;
  padding: 20px;
  border-right: 1px solid #ddd;
  overflow-y: auto;
  box-sizing: border-box;
}

.sidebar h2 {
  margin-top: 0;
}

.sidebar ul {
  list-style-type: none;
  padding: 0;
}

.sidebar li {
  margin: 10px 0;
}

.sidebar a {
  text-decoration: none;
  color: #42b883;
  font-weight: bold;
}

.sidebar a:hover {
  color: #2c3e50;
}

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

.message {
  margin-bottom: 15px;
  max-width: 70%;
  padding: 10px;
  border-radius: 8px;
  background-color: #f0f0f0;
  text-align: left;
}

.message.status-1 {
  background-color: #e0e0e0;
  margin-left: auto;
  text-align: right;
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
  height: 150px;
  background-color: white;
  border: 2px solid black;
  border-radius: 8px;
  padding: 15px;
  box-sizing: border-box;
  overflow-y: hidden;
  display: flex;
  flex-direction: column;
}

/* 文件暂存区样式 */
.file-staging-area {
  background-color: #f4f4f4;
  border-radius: 4px;
  padding: 10px;
  margin-bottom: 10px;
  width: 100%;
  box-sizing: border-box;
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
  background-color: #3c8cdc;
  color: white;
  border: none;
  border-radius: 3px;
  cursor: pointer;
}

.import-btn:hover {
  background-color: #2c3e50;
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
  padding: 10px 20px;
  background-color: #3c8cdc;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  height: 80%;
}

.send-btn:hover {
  background-color: #2c3e50;
}

.send-btn:disabled {
  background-color: #cccccc;
  cursor: not-allowed;
}

.hide-btn {
  margin-left: 10px;
  padding: 10px 20px;
  background-color: #3c8cdc;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  height: 80%;
}

.hide-btn:hover {
  background-color: #2c3e50;
}

.hide-btn:disabled {
  background-color: #cccccc;
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
  padding: 10px 20px;
  background-color: #3c8cdc;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

.show-btn:hover {
  background-color: #2c3e50;
}

.login-status {
  position: absolute;
  bottom: 10px;
  left: 20px;
  right: 20px;
  border-top: 1px solid #666;
  padding-top: 10px;
  font-weight: bold;
}

.login-btn {
  width: 100%;
  padding: 10px;
  background-color: #3c8cdc;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.login-btn:hover {
  background-color: #2c3e50;
}

.logout-btn {
  width: 100%;
  padding: 10px;
  background-color: #e0e0e0;
  color: #333;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  margin-top: 10px;
}

.logout-btn:hover {
  background-color: #d0d0d0;
}

.user-id {
  font-size: 12px;
  color: #666;
  margin: 5px 0;
}

.new-chat-btn {
  width: 100%;
  padding: 10px;
  background-color: #f4f4f4;
  color: #333;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  margin-bottom: 0px;
  font-weight: bold;
  font-size: 16px;
}

.new-chat-btn:hover {
  background-color: #d0d0d0;
}

.divider {
  width: 100%;
  height: 1px;
  background-color: #666;
  margin: 15px 0;
}

.history-title {
  width: 100%;
  color: #000000;
  font-size: 15px;
  margin: 0 0 10px 0;
  font-weight: normal;
}

.communication-list {
  margin-bottom: 100px;
}

.communication-item {
  padding: 10px;
  margin-bottom: 5px;
  border-radius: 4px;
  cursor: pointer;
  text-align: left;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.communication-name {
  flex: 1;
  cursor: pointer;
}

.communication-actions {
  display: none;
}

.communication-item:hover .communication-actions {
  display: block;
}

.rename-btn {
  padding: 2px 6px;
  font-size: 12px;
  background-color: #f4f4f4;
  color: #333;
  border: 1px solid #ddd;
  border-radius: 3px;
  cursor: pointer;
  margin-right: 5px;
}

.rename-btn:hover {
  background-color: #e0e0e0;
}

.delete-btn {
  padding: 2px 6px;
  font-size: 12px;
  background-color: #ffebee;
  color: #c62828;
  border: 1px solid #ffcdd2;
  border-radius: 3px;
  cursor: pointer;
}

.delete-btn:hover {
  background-color: #ffcdd2;
}

/* 云仓库弹窗样式 */
.repository-modal-content {
  width: 800px;
  height: 500px;
  background-color: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 0 20px rgba(0, 0, 0, 0.2);
  display: flex;
  flex-direction: column;
}

.repository-content {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.file-list {
  flex: 1;
  overflow-y: auto;
}

.files {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.file-item {
  padding: 10px;
  background-color: #f4f4f4;
  border-radius: 4px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.file-name {
  font-size: 14px;
  flex: 1;
}

.file-actions {
  display: flex;
  gap: 5px;
}

.view-btn {
  padding: 2px 6px;
  font-size: 12px;
  background-color: #e3f2fd;
  color: #1976d2;
  border: 1px solid #bbdefb;
  border-radius: 3px;
  cursor: pointer;
}

.view-btn:hover {
  background-color: #bbdefb;
}

.add-btn {
  padding: 2px 6px;
  font-size: 12px;
  background-color: #e8f5e8;
  color: #2e7d32;
  border: 1px solid #c8e6c9;
  border-radius: 3px;
  cursor: pointer;
}

.add-btn:hover {
  background-color: #c8e6c9;
}

.file-actions .delete-btn {
  padding: 2px 6px;
  font-size: 12px;
  background-color: #ffebee;
  color: #c62828;
  border: 1px solid #ffcdd2;
  border-radius: 3px;
  cursor: pointer;
}

.file-actions .delete-btn:hover {
  background-color: #ffcdd2;
}

.repository-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: auto;
  padding-top: 20px;
}

.import-btn {
  padding: 8px 16px;
  background-color: #3c8cdc;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.import-btn:hover {
  background-color: #2c3e50;
}

.return-btn {
  padding: 8px 16px;
  background-color: #f4f4f4;
  color: #333;
  border: 1px solid #ddd;
  border-radius: 4px;
  cursor: pointer;
}

.return-btn:hover {
  background-color: #e0e0e0;
}

.communication-item:hover {
  background-color: #e0e0e0;
}

.communication-item.active {
  background-color: #d0d0d0;
  font-weight: bold;
}

.no-communications {
  margin-bottom: 100px;
  color: #666;
  font-style: italic;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  padding: 20px;
  border-radius: 8px;
  width: 300px;
  text-align: center;
}

.modal-content h2 {
  margin-top: 0;
}

.modal-content form div {
  margin: 10px 0;
}

.modal-content label {
  display: block;
  margin-bottom: 5px;
}

.modal-content input {
  width: 100%;
  padding: 8px;
  box-sizing: border-box;
}

.modal-content button {
  margin: 10px 5px;
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.modal-content button[type="submit"] {
  background-color: #3c8cdc;
  color: white;
}

.modal-content button[type="button"] {
  background-color: #999;
}
</style>