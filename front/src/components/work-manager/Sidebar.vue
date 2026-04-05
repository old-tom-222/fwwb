<template>
  <nav class="sidebar">
    <h2>智能文档处理助手</h2>
    <button class="new-chat-btn secondary-btn" @click="$emit('open-repository')">云仓库</button>
    <button class="new-chat-btn secondary-btn" @click="$emit('create-communication')">新对话</button>
    <div class="divider"></div>
    <h3 class="history-title">历史对话</h3>
    <div v-if="isLoggedIn && communications.length > 0" class="communication-list">
      <div 
        v-for="comm in communications" 
        :key="comm.id"
        class="communication-item"
        :class="{ active: selectedCommunicationId === comm.id }"
      >
        <div class="communication-name" @click="$emit('select-communication', comm.id)">
          {{ comm.name }}
        </div>
        <div class="communication-actions">
            <button class="rename-btn small-btn" @click.stop="$emit('open-rename', comm.id, comm.name)">重命名</button>
            <button class="delete-btn small-btn" @click.stop="$emit('open-delete', comm.id, comm.name)">删除</button>
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
        <button @click="$emit('logout')" class="logout-btn secondary-btn">退出登录</button>
      </div>
      <div v-else>
        <button @click="$emit('show-login')" class="login-btn main-btn">登录</button>
      </div>
    </div>
  </nav>
</template>

<script>
export default {
  name: 'WorkManagerSidebar',
  props: {
    isLoggedIn: {
      type: Boolean,
      default: false
    },
    userName: {
      type: String,
      default: ''
    },
    userId: {
      type: String,
      default: ''
    },
    communications: {
      type: Array,
      default: () => []
    },
    selectedCommunicationId: {
      type: String,
      default: null
    }
  },
  emits: ['open-repository', 'create-communication', 'select-communication', 'open-rename', 'open-delete', 'logout', 'show-login']
}
</script>

<style scoped>
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

.new-chat-btn {
  width: 100%;
  padding: 10px;
  margin-bottom: 0px;
  font-weight: bold;
  font-size: 16px;
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
  background-color: #f4f4f4;
  color: #333;
  margin-right: 5px;
}

.rename-btn:hover {
  background-color: #e0e0e0;
}

.delete-btn {
  background-color: #ffebee;
  color: #c62828;
  border-color: #ffcdd2;
}

.delete-btn:hover {
  background-color: #ffcdd2;
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
}

.logout-btn {
  width: 100%;
  padding: 10px;
  margin-top: 10px;
}

.user-id {
  font-size: 12px;
  color: #666;
  margin: 5px 0;
}

.no-communications {
  margin-bottom: 100px;
  color: #666;
  font-style: italic;
}

.communication-item:hover {
  background-color: #e6f0fa;
  border-left: 3px solid #4a90e2;
}

.communication-item.active {
  background-color: #d0e4f7;
  font-weight: bold;
  border-left: 4px solid #4a90e2;
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

.secondary-btn {
  padding: 10px 20px;
  background-color: #f4f4f4;
  color: #333;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.3s ease;
}

.secondary-btn:hover {
  background-color: #d0d0d0;
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