<template>
  <div class="work-manager">
    <nav class="sidebar">
      <h2>智能文档处理助手</h2>
      <button class="new-chat-btn">云仓库</button>
      <button class="new-chat-btn" @click="createNewCommunication">新对话</button>
      <div class="divider"></div>
      <h3 class="history-title">历史对话</h3>
      <div v-if="isLoggedIn && communications.length > 0" class="communication-list">
        <div 
          v-for="comm in communications" 
          :key="comm.id"
          class="communication-item"
          :class="{ active: selectedCommunicationId === comm.id }"
          @click="selectCommunication(comm.id)"
        >
          {{ comm.name }}
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
        <div class="input-area">
          <textarea 
            class="chat-input" 
            placeholder="输入消息..."
            v-model="messageInput"
            @keyup.enter.ctrl="sendMessage"
          ></textarea>
          <button class="send-btn" @click="sendMessage">发送</button>
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
      loginForm: {
        account: '',
        password: ''
      },
      communications: [], // 对话列表
      selectedCommunicationId: null, // 当前选中的对话ID
      selectedCommunicationName: '', // 当前选中的对话名称
      messages: [], // 消息列表
      messageInput: '' // 消息输入框内容
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
      
      if (!this.messageInput.trim()) {
        return
      }
      
      try {
        const requestData = {
          communicationId: this.selectedCommunicationId,
          content: this.messageInput
        }
        
        // 调用后端聊天接口
        await axios.post('http://localhost:8081/api/messages/chat', requestData)
        
        // 清空输入框
        this.messageInput = ''
        
        // 重新获取消息列表
        await this.fetchMessages(this.selectedCommunicationId)
      } catch (error) {
        console.error('发送消息失败:', error)
        alert('发送消息失败: ' + error.message)
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
  height: 80px;
  background-color: white;
  border: 2px solid black;
  border-radius: 8px;
  padding: 15px;
  box-sizing: border-box;
  overflow-y: hidden;
  display: flex;
  align-items: center;
}

.chat-input {
  flex: 1;
  height: 100%;
  border: none;
  outline: none;
  resize: none;
  font-size: 16px;
  line-height: 1;
}

.send-btn {
  margin-left: 10px;
  padding: 10px 20px;
  background-color: #3c8cdc;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  height: 100%;
}

.send-btn:hover {
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