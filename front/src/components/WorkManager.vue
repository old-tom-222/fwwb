<template>
  <div class="work-manager">
    <nav class="sidebar">
      <h2>导航</h2>
      <ul>
        <li><router-link to="/">工作管理</router-link></li>
      </ul>
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
      <h1>Work Manager</h1>
      <p>这是工作管理页面</p>
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
  min-height: 100vh;
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
  margin-left: 250px;
  padding: 20px;
  min-height: 100vh;
  overflow-y: auto;
  text-align: center;
}

.login-status {
  position: absolute;
  bottom: 20px;
  left: 20px;
  right: 20px;
}

.login-btn {
  width: 100%;
  padding: 10px;
  background-color: #42b883;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.login-btn:hover {
  background-color: #2c3e50;
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
  background-color: #42b883;
  color: white;
}

.modal-content button[type="button"] {
  background-color: #ddd;
}
</style>