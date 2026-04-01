import axios from 'axios'

const API_BASE_URL = 'http://localhost:8081/api'

// 用户相关 API
export const userApi = {
  // 登录
  login: async (account, password) => {
    const response = await axios.post(`${API_BASE_URL}/users/login`, { account, password })
    return response.data
  }
}

// 对话相关 API
export const communicationApi = {
  // 获取用户对话列表
  getUserCommunications: async (userId) => {
    const response = await axios.get(`${API_BASE_URL}/communications/user/${userId}`)
    return response.data
  },
  
  // 创建新对话
  createCommunication: async (userId, name) => {
    const response = await axios.post(`${API_BASE_URL}/communications`, {
      userId,
      name,
      createdAt: new Date().toISOString()
    })
    return response.data
  },
  
  // 重命名对话
  renameCommunication: async (id, name) => {
    await axios.put(`${API_BASE_URL}/communications/${id}`, { name })
  },
  
  // 删除对话
  deleteCommunication: async (id) => {
    await axios.delete(`${API_BASE_URL}/communications/${id}`)
  }
}

// 消息相关 API
export const messageApi = {
  // 获取对话消息
  getCommunicationMessages: async (communicationId) => {
    const response = await axios.get(`${API_BASE_URL}/messages/communication/${communicationId}`)
    return response.data
  },
  
  // 发送消息
  sendMessage: async (communicationId, content, files) => {
    const formData = new FormData()
    formData.append('communicationId', communicationId)
    formData.append('content', content)
    
    files.forEach(file => {
      formData.append('files', file)
    })
    
    await axios.post(`${API_BASE_URL}/messages/chat`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },
  
  // 生成 docx
  generateDocx: async (communicationId, content, files) => {
    const formData = new FormData()
    formData.append('communicationId', communicationId)
    formData.append('content', content)
    
    files.forEach(file => {
      formData.append('files', file)
    })
    
    await axios.post(`${API_BASE_URL}/messages/generate-docx`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },
  
  // 生成 xlsx
  generateXlsx: async (communicationId, content, files) => {
    const formData = new FormData()
    formData.append('communicationId', communicationId)
    formData.append('content', content)
    
    files.forEach(file => {
      formData.append('files', file)
    })
    
    await axios.post(`${API_BASE_URL}/messages/generate-xlsx`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  }
}

// 云仓库相关 API
export const repositoryApi = {
  // 获取用户文件列表
  getUserFiles: async (userId) => {
    const response = await axios.get(`${API_BASE_URL}/repository/user/${userId}`)
    return response.data
  },
  
  // 上传文件
  uploadFile: async (userId, file) => {
    const formData = new FormData()
    formData.append('userId', userId)
    formData.append('file', file)
    
    await axios.post(`${API_BASE_URL}/repository/upload`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },
  
  // 删除文件
  deleteFile: async (fileId) => {
    await axios.delete(`${API_BASE_URL}/repository/${fileId}`)
  }
}
