import WorkManagerSidebar from '../work-manager/Sidebar.vue'
import WorkManagerChatArea from '../work-manager/ChatArea.vue'
import LoginModal from '../work-manager/LoginModal.vue'
import RenameModal from '../work-manager/RenameModal.vue'
import DeleteModal from '../work-manager/DeleteModal.vue'
import RepositoryModal from '../work-manager/RepositoryModal.vue'
import { userApi, communicationApi, messageApi, repositoryApi } from '../../utils/api'
import { checkLoginStatus, saveLoginStatus, clearLoginStatus } from '../../utils/utils'

export default {
  name: 'WorkManager',
  components: {
    WorkManagerSidebar,
    WorkManagerChatArea,
    LoginModal,
    RenameModal,
    DeleteModal,
    RepositoryModal
  },
  data() {
    return {
      isLoggedIn: false,
      userName: '',
      userId: '',
      showLoginModal: false,
      showRenameModal: false,
      showDeleteModal: false,
      showRepositoryModal: false,
      repositoryFiles: [],
      renameForm: {
        id: '',
        name: ''
      },
      deleteForm: {
        id: '',
        name: ''
      },
      communications: [],
      selectedCommunicationId: null,
      selectedCommunicationName: '',
      messages: [],
      stagedFiles: [],
      loading: false
    }
  },
  methods: {
    async login(account, password) {
      try {
        const userData = await userApi.login(account, password)
        if (userData) {
          this.isLoggedIn = true
          this.userName = userData.name || userData.account
          this.userId = userData.id
          
          saveLoginStatus(userData.id, this.userName)
          this.showLoginModal = false
          
          await this.fetchCommunications()
        } else {
          alert('登录失败：账号或密码错误')
        }
      } catch (error) {
        alert('登录失败：' + error.message)
      }
    },

    checkLoginStatus() {
      const { isLoggedIn, userId, userName } = checkLoginStatus()
      if (isLoggedIn) {
        this.isLoggedIn = true
        this.userId = userId
        this.userName = userName
        this.fetchCommunications()
        this.fetchUserFiles()
      }
    },
    
    async fetchUserFiles() {
      console.log('WorkManager: 开始获取用户文件')
      if (!this.userId) {
        console.log('WorkManager: 未登录，无法获取用户文件')
        return
      }
      
      try {
        console.log('WorkManager: 发送请求获取用户文件')
        const response = await fetch(`/api/temporary/user/${this.userId}`)
        console.log('WorkManager: 获取用户文件响应:', response)
        if (response.ok) {
          const files = await response.json()
          console.log('WorkManager: 获取到用户文件:', files)
          this.stagedFiles = Array.isArray(files) ? files : []
          console.log('WorkManager: 暂存区文件:', this.stagedFiles)
        } else {
          console.log('WorkManager: 获取用户文件失败，状态:', response.status)
        }
      } catch (error) {
        console.error('获取用户文件失败:', error)
        this.stagedFiles = []
      }
    },

    logout() {
      clearLoginStatus()
      this.isLoggedIn = false
      this.userName = ''
      this.userId = ''
      this.communications = []
      this.selectedCommunicationId = null
      this.selectedCommunicationName = ''
      this.messages = []
      this.stagedFiles = []
    },

    async fetchCommunications() {
      if (!this.userId) return
      
      try {
        this.communications = await communicationApi.getUserCommunications(this.userId)
      } catch (error) {
        console.error('获取对话列表失败:', error)
      }
    },

    async selectCommunication(communicationId) {
      this.selectedCommunicationId = communicationId
      
      const communication = this.communications.find(comm => comm.id === communicationId)
      if (communication) {
        this.selectedCommunicationName = communication.name
      }
      
      await this.fetchMessages(communicationId)
    },

    async fetchMessages(communicationId) {
      try {
        console.log('正在获取消息列表，对话ID:', communicationId)
        this.messages = await messageApi.getCommunicationMessages(communicationId)
        console.log('获取消息列表成功:', this.messages)
      } catch (error) {
        console.error('获取消息失败:', error)
      }
    },

    async createNewCommunication() {
      if (!this.isLoggedIn) {
        alert('请先登录')
        return
      }
      
      try {
        const newCommunication = await communicationApi.createCommunication(this.userId, '未命名')
        await this.fetchCommunications()
        this.selectCommunication(newCommunication.id)
      } catch (error) {
        console.error('创建新对话失败:', error)
        alert('创建新对话失败: ' + error.message)
      }
    },

    async sendMessage(content, files) {
      if (!this.isLoggedIn) {
        alert('请先登录')
        return
      }
      
      if (!this.selectedCommunicationId) {
        alert('请先选择一个对话')
        return
      }
      
      this.loading = true
      
      try {
        // 确保content是字符串
        const contentStr = typeof content === 'string' ? content : ''
        console.log('正在发送消息，内容:', contentStr, '文件:', files)
        const processed = await this.processFiles(files)
        console.log('处理文件后:', processed)
        await messageApi.sendMessage(this.selectedCommunicationId, contentStr, processed.files, processed.ids)
        console.log('发送消息成功')
        
        // 不移除暂存区文件，让用户自己管理
        await this.fetchMessages(this.selectedCommunicationId)
      } catch (error) {
        console.error('发送消息失败:', error)
        alert('发送消息失败: ' + error.message)
      } finally {
        this.loading = false
      }
    },
    
    openRenameModal(communicationId, currentName) {
      this.renameForm.id = communicationId
      this.renameForm.name = currentName
      this.showRenameModal = true
    },
    
    openDeleteModal(communicationId, currentName) {
      this.deleteForm.id = communicationId
      this.deleteForm.name = currentName
      this.showDeleteModal = true
    },
    
    async renameCommunication(communicationId, name) {
      try {
        await communicationApi.renameCommunication(communicationId, name)
        this.showRenameModal = false
        await this.fetchCommunications()
        
        if (this.selectedCommunicationId === communicationId) {
          this.selectedCommunicationName = name
        }
      } catch (error) {
        console.error('重命名对话失败:', error)
        alert('重命名对话失败: ' + error.message)
      }
    },
    
    async deleteCommunication(communicationId) {
      try {
        await communicationApi.deleteCommunication(communicationId)
        this.showDeleteModal = false
        await this.fetchCommunications()
        
        if (this.selectedCommunicationId === communicationId) {
          this.selectedCommunicationId = null
          this.selectedCommunicationName = ''
          this.messages = []
        }
      } catch (error) {
        console.error('删除对话失败:', error)
        alert('删除对话失败: ' + error.message)
      }
    },
    
    addFile() {
      console.log('WorkManager: 收到add-file事件，触发重新获取用户文件')
      
      // 直接从后端获取用户文件，确保暂存区与temporary集合同步
      this.fetchUserFiles()
    },
    
    removeFile() {
      console.log('WorkManager: 移除文件，触发重新获取用户文件')
      
      // 直接从后端获取用户文件，确保暂存区与temporary集合同步
      this.fetchUserFiles()
    },
    
    async openRepositoryModal() {
      if (!this.isLoggedIn) {
        alert('请先登录')
        return
      }
      
      try {
        this.repositoryFiles = await repositoryApi.getUserFiles(this.userId)
        this.showRepositoryModal = true
      } catch (error) {
        console.error('获取文件列表失败:', error)
        alert('获取文件列表失败: ' + error.message)
      }
    },
    
    async uploadFiles(files) {
      for (let i = 0; i < files.length; i++) {
        const file = files[i]
        try {
          await repositoryApi.uploadFile(this.userId, file)
        } catch (error) {
          console.error('上传文件失败:', error)
          alert('上传文件失败: ' + error.message)
        }
      }
      
      try {
        this.repositoryFiles = await repositoryApi.getUserFiles(this.userId)
      } catch (error) {
        console.error('获取文件列表失败:', error)
        alert('获取文件列表失败: ' + error.message)
      }
    },
    
    async deleteRepositoryFile(fileId) {
      try {
        await repositoryApi.deleteFile(fileId)
        this.repositoryFiles = await repositoryApi.getUserFiles(this.userId)
        alert('文件删除成功')
      } catch (error) {
        console.error('删除文件失败:', error)
        alert('删除文件失败: ' + error.message)
      }
    },
    
    async processFiles(files) {
      const processedFiles = []
      const fileIds = []
      if (!files || !Array.isArray(files)) {
        return { files: processedFiles, ids: fileIds }
      }
      
      for (const file of files) {
        if (file instanceof File) {
          processedFiles.push(file)
        } else if (file && file.url && file.id) {
          try {
            // 对于从temporary集合返回的文件对象，我们需要从后端下载
            const response = await fetch(`/api/temporary/${file.id}`)
            if (response.ok) {
              const blob = await response.blob()
              processedFiles.push(new File([blob], file.name || 'unknown.txt'))
              fileIds.push(file.id)
            } else {
              console.error('获取临时文件失败:', response.status)
              throw new Error('获取临时文件失败')
            }
          } catch (error) {
            console.error('处理文件失败:', error)
            alert('处理文件失败: ' + error.message)
            throw error
          }
        }
      }
      return { files: processedFiles, ids: fileIds }
    }
  },
  mounted() {
    this.checkLoginStatus()
  }
}