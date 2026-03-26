<template>
  <div>
    <h1>前后端连接测试</h1>
    <h2>用户列表</h2>
    <div v-if="loading">加载中...</div>
    <div v-else-if="error">错误: {{ error }}</div>
    <div v-else>
      <div v-for="user in users" :key="user.id" class="user-card">
        <h3>{{ user.name }}</h3>
        <p>年龄: {{ user.age }}</p>
        <p>爱好: {{ user.hobbies.join(', ') }}</p>
        <p>地址: {{ user.address.city }}, {{ user.address.street }}</p>
      </div>
      <div v-if="users.length === 0">暂无用户数据</div>
    </div>
    <h2>添加用户</h2>
    <form @submit.prevent="addUser">
      <div>
        <label>姓名:</label>
        <input v-model="newUser.name" type="text" required>
      </div>
      <div>
        <label>年龄:</label>
        <input v-model.number="newUser.age" type="number" required>
      </div>
      <div>
        <label>爱好:</label>
        <input v-model="hobbiesInput" type="text" placeholder="用逗号分隔">
      </div>
      <div>
        <label>城市:</label>
        <input v-model="newUser.address.city" type="text" required>
      </div>
      <div>
        <label>街道:</label>
        <input v-model="newUser.address.street" type="text" required>
      </div>
      <button type="submit">添加用户</button>
    </form>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'UserManager',
  data() {
    return {
      users: [],
      loading: false,
      error: null,
      newUser: {
        name: '',
        age: 0,
        hobbies: [],
        address: {
          city: '',
          street: ''
        }
      },
      hobbiesInput: ''
    }
  },
  mounted() {
    this.fetchUsers()
  },
  methods: {
    async fetchUsers() {
      this.loading = true
      this.error = null
      try {
        const response = await axios.get('http://localhost:8081/api/users')
        this.users = response.data
      } catch (error) {
        this.error = '获取用户数据失败'
        console.error('Error fetching users:', error)
      } finally {
        this.loading = false
      }
    },
    async addUser() {
      this.newUser.hobbies = this.hobbiesInput.split(',').map(hobby => hobby.trim())
      this.loading = true
      this.error = null
      try {
        await axios.post('http://localhost:8081/api/users', this.newUser)
        this.fetchUsers()
        this.resetForm()
      } catch (error) {
        this.error = '添加用户失败'
        console.error('Error adding user:', error)
      } finally {
        this.loading = false
      }
    },
    resetForm() {
      this.newUser = {
        name: '',
        age: 0,
        hobbies: [],
        address: {
          city: '',
          street: ''
        }
      }
      this.hobbiesInput = ''
    }
  }
}
</script>

<style scoped>
.user-card {
  border: 1px solid #ddd;
  border-radius: 8px;
  padding: 16px;
  margin: 16px 0;
  text-align: left;
}
form {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  margin-top: 32px;
}
form div {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
  max-width: 400px;
  justify-content: space-between;
}
form input {
  flex: 1;
  padding: 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
}
button {
  margin-top: 16px;
  padding: 8px 16px;
  background-color: #4CAF50;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}
</style>
