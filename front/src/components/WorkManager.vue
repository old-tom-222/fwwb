<template>
  <div class="work-manager">
    <nav class="sidebar">
      <h2>智能文档处理助手</h2>
      <button class="new-chat-btn">云仓库</button>
      <button class="new-chat-btn">新对话</button>
      <div class="divider"></div>
      <h3 class="history-title">历史对话</h3>
      <!-- <ul>
        <li><router-link to="/">工作管理</router-link></li>
      </ul> -->
      <div class="login-status">
        <div v-if="isLoggedIn">
          <p>欢迎, {{ userName }}</p>
        </div>
        <div v-else>
          <button @click="showLoginModal = true" class="login-btn">登录</button>
        </div>
      </div>
    </nav>
    <main class="content">
      <h1>{{ greeting }}</h1>
      <!-- <p>这是工作管理页面</p> -->
      <div class="input-area">
        <textarea class="chat-input" placeholder="输入消息..."></textarea>
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
      showLoginModal: false,
      loginForm: {
        account: '',
        password: ''
      }
    }
  },
  computed: {
    greeting() {
      const hour = new Date().getHours();
      if (hour >= 7 && hour < 12) return '早上好，有什么我能帮到你吗？';
      if (hour >= 12 && hour < 14) return '中午好，有什么我能帮到你吗？';
      if (hour >= 14 && hour < 18) return '下午好，有什么我能帮到你吗？';
      return '晚上好，有什么我能帮到你吗？';
    }
  },
  methods: {
    async login() {
      try {
        const response = await axios.post('http://localhost:8081/api/users/login', this.loginForm)
        if (response.data) {
          this.isLoggedIn = true
          this.userName = response.data.name || response.data.account
          this.showLoginModal = false
          this.loginForm = { account: '', password: '' }
        } else {
          alert('登录失败：账号或密码错误')
        }
      } catch (error) {
        alert('登录失败：' + error.message)
      }
    }
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

.content h1 {
  margin-top: 40vh;
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
}

.chat-input {
  width: 95%;
  height: 100%;
  border: none;
  outline: none;
  resize: none;
  font-size: 16px;
  line-height: 1;
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