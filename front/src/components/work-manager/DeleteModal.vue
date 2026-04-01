<template>
  <div class="modal-overlay" @click="$emit('close')">
    <div class="modal-content" @click.stop>
      <h2>删除对话</h2>
      <p>确定要删除对话 "{{ communicationName }}" 吗？</p>
      <div class="modal-actions">
        <button @click="deleteCommunication">确认删除</button>
        <button @click="$emit('close')">取消</button>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'DeleteModal',
  props: {
    communicationId: {
      type: String,
      default: ''
    },
    communicationName: {
      type: String,
      default: ''
    }
  },
  emits: ['close', 'delete'],
  methods: {
    deleteCommunication() {
      this.$emit('delete', this.communicationId)
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

.modal-actions {
  display: flex;
  justify-content: center;
  gap: 10px;
  margin-top: 20px;
}

.modal-actions button {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.modal-actions button:first-child {
  background-color: #e53935;
  color: white;
}

.modal-actions button:last-child {
  background-color: #999;
  color: white;
}
</style>
