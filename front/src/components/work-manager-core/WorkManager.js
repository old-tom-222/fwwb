import WorkManagerSidebar from '../work-manager/Sidebar.vue'
import WorkManagerChatArea from '../work-manager/ChatArea.vue'
import LoginModal from '../work-manager/LoginModal.vue'
import RenameModal from '../work-manager/RenameModal.vue'
import DeleteModal from '../work-manager/DeleteModal.vue'
import RepositoryModal from '../work-manager/RepositoryModal.vue'
import { userApi, communicationApi, messageApi, repositoryApi } from '../../utils/api'
import { checkLoginStatus, saveLoginStatus, clearLoginStatus, createFileFromRepository } from '../../utils/utils'

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
        this.messages = await messageApi.getCommunicationMessages(communicationId)
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
        const processedFiles = await this.processFiles(files)
        await messageApi.sendMessage(this.selectedCommunicationId, content, processedFiles)
        
        this.stagedFiles = []
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
    
    addFile(file) {
      if (this.stagedFiles.length >= 5) {
        alert('暂存区最多只能存储5个文件')
        return
      }
      
      const isFileExist = this.stagedFiles.some(stagedFile => stagedFile.name === file.name)
      if (isFileExist) {
        alert('该文件已在暂存区中')
        return
      }
      
      this.stagedFiles.push(file)
      if (file.url) {
        alert('文件已添加到暂存区')
      }
    },
    
    removeFile(index) {
      this.stagedFiles.splice(index, 1)
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
    
    async generateDocx(content, files) {
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
        const processedFiles = await this.processFiles(files)
        await messageApi.generateDocx(this.selectedCommunicationId, content, processedFiles)
        
        alert('文档生成成功，请在对话中查看下载链接')
        this.stagedFiles = []
        await this.fetchMessages(this.selectedCommunicationId)
      } catch (error) {
        console.error('生成文档失败:', error)
        alert('生成文档失败: ' + error.message)
      } finally {
        this.loading = false
      }
    },
    
    async generateXlsx(content, files) {
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
        const processedFiles = await this.processFiles(files)
        await messageApi.generateXlsx(this.selectedCommunicationId, content, processedFiles)
        
        alert('表格生成成功，请在对话中查看下载链接')
        this.stagedFiles = []
        await this.fetchMessages(this.selectedCommunicationId)
      } catch (error) {
        console.error('生成表格失败:', error)
        alert('生成表格失败: ' + error.message)
      } finally {
        this.loading = false
      }
    },
    
    async processFiles(files) {
      const processedFiles = []
      for (const file of files) {
        if (file.url && !(file instanceof File)) {
          try {
            const fileObject = await createFileFromRepository(file)
            processedFiles.push(fileObject)
          } catch (error) {
            console.error('下载文件失败:', error)
            alert('下载文件失败: ' + error.message)
            throw error
          }
        } else {
          processedFiles.push(file)
        }
      }
      return processedFiles
    }
  },
  mounted() {
    this.checkLoginStatus()
  }
}