// 格式化时间
export const formatTime = (timeString) => {
  const date = new Date(timeString)
  return date.toLocaleString()
}

// 获取问候语
export const getGreeting = () => {
  const hour = new Date().getHours()
  if (hour >= 7 && hour < 12) return '早上好，有什么我能帮到你吗？'
  if (hour >= 12 && hour < 14) return '中午好，有什么我能帮到你吗？'
  if (hour >= 14 && hour < 18) return '下午好，有什么我能帮到你吗？'
  return '晚上好，有什么我能帮到你吗？'
}

// 检查文件格式是否支持
export const isSupportedFileFormat = (fileName) => {
  const extension = fileName.split('.').pop().toLowerCase()
  return ['docx', 'md', 'xlsx', 'txt'].includes(extension)
}

// 从云仓库文件创建文件对象
export const createFileFromRepository = async (file) => {
  try {
    // 使用新的文件获取接口
    const response = await fetch(`http://localhost:8081/api/repository/file/${file.id}`)
    const blob = await response.blob()
    return new File([blob], file.name)
  } catch (error) {
    console.error('下载文件失败:', error)
    throw error
  }
}

// 检查登录状态
export const checkLoginStatus = () => {
  const savedUserId = localStorage.getItem('userId')
  const savedUserName = localStorage.getItem('userName')
  
  if (savedUserId && savedUserName) {
    return {
      isLoggedIn: true,
      userId: savedUserId,
      userName: savedUserName
    }
  }
  
  return {
    isLoggedIn: false,
    userId: '',
    userName: ''
  }
}

// 保存登录状态
export const saveLoginStatus = (userId, userName) => {
  localStorage.setItem('userId', userId)
  localStorage.setItem('userName', userName)
}

// 清除登录状态
export const clearLoginStatus = () => {
  localStorage.removeItem('userId')
  localStorage.removeItem('userName')
}