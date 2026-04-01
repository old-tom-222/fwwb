<template>
  <div class="modal-overlay" @click="$emit('close')">
    <div class="modal-content" @click.stop>
      <h2>重命名对话</h2>
      <form @submit.prevent="rename">
        <div>
          <label>对话名称:</label>
          <input v-model="renameForm.name" type="text" required>
        </div>
        <button type="submit">确认</button>
        <button type="button" @click="$emit('close')">取消</button>
      </form>
    </div>
  </div>
</template>

<script>
export default {
  name: 'RenameModal',
  props: {
    communicationId: {
      type: String,
      default: ''
    },
    currentName: {
      type: String,
      default: ''
    }
  },
  data() {
    return {
      renameForm: {
        name: this.currentName
      }
    }
  },
  watch: {
    currentName: {
      handler(newValue) {
        this.renameForm.name = newValue
      },
      immediate: true
    }
  },
  emits: ['close', 'rename'],
  methods: {
    rename() {
      if (!this.renameForm.name.trim()) {
        alert('请输入对话名称')
        return
      }
      this.$emit('rename', this.communicationId, this.renameForm.name)
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
