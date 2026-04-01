<template>
  <div class="modal-overlay" @click="$emit('close')">
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
        <button type="button" @click="$emit('close')">取消</button>
      </form>
    </div>
  </div>
</template>

<script>
export default {
  name: 'LoginModal',
  data() {
    return {
      loginForm: {
        account: '',
        password: ''
      }
    }
  },
  emits: ['close', 'login'],
  methods: {
    login() {
      this.$emit('login', this.loginForm.account, this.loginForm.password)
      this.loginForm = { account: '', password: '' }
    }
  }
}
</script>

<style scoped>
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
  background: #f8fafc;
  padding: 20px;
  border: 1px solid #4a90e2;
  border-radius: 8px;
  width: 300px;
  text-align: center;
  box-shadow: 0 4px 8px rgba(74, 144, 226, 0.2);
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
