<template>
  <div class="modal-overlay" @click="$emit('close')">
    <div class="repository-modal-content" @click.stop>
      <h2>云仓库</h2>
      <div v-if="importing" class="loading-overlay">
        正在导入，请耐心等待...
      </div>
      <div class="repository-content">
        <div class="file-list">
          <div v-if="repositoryFiles.length > 0" class="files">
            <div v-for="file in repositoryFiles" :key="file.id" class="file-item">
              <span class="file-name">{{ file.name }}</span>
              <div class="file-actions">
                <button class="view-btn small-btn" @click="viewFile(file)">查看</button>
                <button class="import-btn small-btn" @click="importFile(file)">导入此文件</button>
                <button class="delete-btn small-btn" @click="deleteRepositoryFile(file.id)">删除</button>
              </div>
            </div>
          </div>
          <div v-else class="no-files">暂无文件</div>
        </div>
        <div class="repository-actions">
          <input ref="repositoryFileInput" type="file" multiple style="display: none" @change="handleRepositoryFileInput" accept=".docx,.md,.xlsx,.txt">
          <button class="import-btn main-btn" @click="triggerRepositoryFileInput">导入文件</button>
          <button class="return-btn secondary-btn" @click="$emit('close')">返回</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { isSupportedFileFormat, createFileFromRepository } from '../../utils/utils'

export default {
  name: 'RepositoryModal',
  props: {
    repositoryFiles: {
      type: Array,
      default: () => []
    }
  },
  data() {
    return {
      importing: false
    }
  },
  emits: ['close', 'add-file', 'refresh-files'],
  methods: {
    triggerRepositoryFileInput() {
      this.$refs.repositoryFileInput.click()
    },
    
    handleRepositoryFileInput(event) {
      const files = event.target.files
      if (files.length === 0) return
      
      for (let i = 0; i < files.length; i++) {
        const file = files[i]
        if (!isSupportedFileFormat(file.name)) {
          alert('只能导入docx、md、xlsx、txt格式的文件')
          return
        }
      }
      
      this.$emit('upload-files', files)
      event.target.value = ''
    },
    
    viewFile(file) {
      window.open(`http://localhost:8081${file.url}`, '_blank')
    },
    
    async importFile(file) {
      console.log('RepositoryModal: 开始导入文件:', file)
      this.importing = true
      try {
        // 从云仓库文件创建File对象
        console.log('RepositoryModal: 从云仓库文件创建File对象')
        const fileObject = await createFileFromRepository(file)
        console.log('RepositoryModal: 成功创建File对象:', fileObject)
        
        // 上传文件到后端临时存储
        console.log('RepositoryModal: 上传文件到后端临时存储')
        const formData = new FormData()
        formData.append('file', fileObject)
        formData.append('userId', localStorage.getItem('userId') || 'anonymous')
        
        const response = await fetch('/api/temporary/upload', {
          method: 'POST',
          body: formData
        })
        
        console.log('RepositoryModal: 上传文件响应:', response)
        if (response.ok) {
          const data = await response.json()
          console.log('RepositoryModal: 上传成功，响应数据:', data)
          console.log('RepositoryModal: 触发add-file事件')
          this.$emit('add-file', data)
          // 显示成功提示
          console.log('RepositoryModal: 显示成功提示')
          alert('文件导入成功')
          // 自动关闭云仓库
          console.log('RepositoryModal: 关闭云仓库')
          this.$emit('close')
        } else {
          console.log('RepositoryModal: 上传失败，状态:', response.status)
          alert('文件导入失败')
        }
      } catch (error) {
        console.error('文件导入失败:', error)
        alert('文件导入失败')
      } finally {
        this.importing = false
      }
    },
    
    deleteRepositoryFile(fileId) {
      if (confirm('确定要删除这个文件吗？')) {
        this.$emit('delete-file', fileId)
      }
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
  background-color: #e3f2fd;
  color: #1976d2;
  border-color: #bbdefb;
}

.view-btn:hover {
  background-color: #bbdefb;
}

.import-btn {
  background-color: #e8f5e8;
  color: #2e7d32;
  border-color: #c8e6c9;
}

.import-btn:hover {
  background-color: #c8e6c9;
}

.file-actions .delete-btn {
  background-color: #ffebee;
  color: #c62828;
  border-color: #ffcdd2;
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
}

.return-btn {
  padding: 8px 16px;
  border: 1px solid #ddd;
}

.no-files {
  font-style: italic;
  color: #666;
  font-size: 14px;
  text-align: center;
  margin-top: 20px;
}

/* 加载覆盖层样式 */
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
  font-size: 16px;
  color: #4a90e2;
  z-index: 10;
  border-radius: 8px;
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