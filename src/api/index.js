import request from './request'

// 用户认证相关 API
export const authApi = {
  // 登录
  login(username, password) {
    return request.post('/auth/login', { username, password })
  },
  // 注册（备用：后端若实现了 POST /api/auth/register 则调用，如未实现可用于联调）
  register(data) {
    return request.post('/auth/register', data)
  },
  // 修改密码 (POST /api/users/update-password)
  updatePassword(oldPassword, newPassword) {
    return request.post('/users/update-password', { oldPassword, newPassword })
  }
}

// 项目相关 API
export const projectApi = {
  // 获取项目列表
  getProjects() {
    return request.get('/projects')
  },
  // 创建/编辑项目
  saveProject(project) {
    return request.post('/projects', project)
  },
  // 删除项目
  deleteProject(id) {
    return request.delete(`/projects/${id}`)
  }
}

// 任务相关 API
export const taskApi = {
  // 获取任务列表 (可按 projectId 筛选)
  getTasks(projectId) {
    return request.get('/tasks', {
      params: projectId ? { projectId } : {}
    })
  },
  // 创建/编辑任务
  saveTask(task) {
    return request.post('/tasks', task)
  },
  // 删除任务
  deleteTask(id) {
    return request.delete(`/tasks/${id}`)
  }
}

// 进度跟踪相关 API
export const taskLogApi = {
  // 获取进度记录列表 (可按 taskId 筛选)
  getTaskLogs(taskId) {
    return request.get('/task-logs', {
      params: taskId ? { taskId } : {}
    })
  },
  // 新增进度记录
  addTaskLog(log) {
    return request.post('/task-logs', log)
  }
}

// 总结相关 API
export const summaryApi = {
  // 获取总结列表
  getSummaries() {
    return request.get('/summaries')
  },
  // 新增总结
  addSummary(summary) {
    return request.post('/summaries', summary)
  }
}

// 总览统计 API
export const overviewApi = {
  // 获取统计信息 (用户数、项目数、任务数、总结数)
  getOverview() {
    return request.get('/overview')
  }
}

// 成员/用户相关 API
export const userApi = {
  // 获取全部用户列表 (不含密码)
  getUsers() {
    return request.get('/users')
  }
}

// AI 模块相关 API
export const aiApi = {
  // 单任务 AI 建议
  getTaskSuggestion(projectName, title, description) {
    return request.post('/ai/task-suggestion', {
      projectName,
      title,
      description
    })
  },
  // AI 任务拆解
  getTaskPlan(projectId, projectName, goal, description) {
    return request.post('/ai/task-plan', {
      projectId,
      projectName,
      goal,
      description
    })
  },
  // 导入 AI 拆解任务
  importTaskPlan(projectId, creatorId, items) {
    return request.post('/ai/task-plan/import', {
      projectId,
      creatorId,
      items
    })
  }
}
