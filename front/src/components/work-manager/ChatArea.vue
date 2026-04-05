<template>
  <main class="content">
    <div v-if="!selectedCommunicationId" class="welcome">
      <h1>{{ greeting }}</h1>
    </div>
    <div v-else class="chat-area">
      <div class="chat-header">
        <h2>{{ selectedCommunicationName }}</h2>
      </div>
      <div ref="messagesContainer" class="messages-container">
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
      <!-- 输入区域组件 -->
      <WorkManagerInputArea 
        :loading="loading"
        :stagedFiles="stagedFiles"
        @send-message="handleSendMessage"
        @generate-docx="$emit('generate-docx', ...arguments)"
        @generate-xlsx="$emit('generate-xlsx', ...arguments)"
        @add-file="$emit('add-file', ...arguments)"
        @remove-file="$emit('remove-file', ...arguments)"
      />
    </div>
  </main>
</template>

<script>
import { formatTime, getGreeting } from '../../utils/utils'
import WorkManagerInputArea from './InputArea.vue'

export default {
  name: 'WorkManagerChatArea',
  components: {
    WorkManagerInputArea
  },
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
    handleSendMessage(content, files) {
      console.log('ChatArea: 接收到发送消息事件，内容:', content, '文件:', files)
      this.$emit('send-message', content, files)
    },
    scrollToBottom() {
      // 等待DOM更新后再滚动
      this.$nextTick(() => {
        const container = this.$refs.messagesContainer
        if (container) {
          container.scrollTop = container.scrollHeight
        }
      })
    }
  },
  watch: {
    // 当消息列表变化时，滚动到底部
    messages: {
      handler() {
        this.scrollToBottom()
      },
      deep: true
    },
    // 当选择的对话变化时，滚动到底部
    selectedCommunicationId() {
      this.scrollToBottom()
    }
  },
  // 组件挂载后，滚动到底部
  mounted() {
    this.scrollToBottom()
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
</style>