<template>
  <div class="home-container">
    <!-- 左侧侧边栏 (Sidebar) -->
    <div class="sidebar">
      <div class="sidebar-brand">
        <div class="logo">FS</div>
        <div class="brand-text">
          <span class="title">FlowSync</span>
          <span class="subtitle">协同管理系统</span>
        </div>
      </div>

      <div class="user-profile-summary">
        <div class="avatar">{{ currentUser.realName ? currentUser.realName.substring(0, 1) : 'U' }}</div>
        <div class="info">
          <div class="name">{{ currentUser.realName || currentUser.username }}</div>
          <div class="role-tag" :class="currentUser.role === '负责人' ? 'role-leader' : 'role-member'">
            {{ currentUser.role }}
          </div>
        </div>
      </div>

      <el-menu :default-active="activeMenu" class="sidebar-menu" @select="handleMenuSelect">
        <div class="menu-group-title">工作台</div>
        <el-menu-item index="overview">
          <el-icon><Odometer /></el-icon>
          <span>总览统计</span>
        </el-menu-item>

        <div class="menu-group-title">业务管理</div>
        <el-menu-item index="projects">
          <el-icon><Folder /></el-icon>
          <span>项目管理</span>
        </el-menu-item>
        
        <!-- 仅负责人可见 -->
        <el-menu-item index="ai-plan" v-if="isLeader">
          <el-icon><Cpu /></el-icon>
          <span>AI 任务拆解</span>
        </el-menu-item>

        <el-menu-item index="tasks">
          <el-icon><Document /></el-icon>
          <span>任务管理</span>
        </el-menu-item>

        <el-menu-item index="logs">
          <el-icon><TrendCharts /></el-icon>
          <span>进度跟踪</span>
        </el-menu-item>

        <el-menu-item index="summaries">
          <el-icon><Notebook /></el-icon>
          <span>总结中心</span>
        </el-menu-item>

        <div class="menu-group-title">系统信息</div>
        <el-menu-item index="members">
          <el-icon><UserFilled /></el-icon>
          <span>成员列表</span>
        </el-menu-item>

        <el-menu-item index="profile">
          <el-icon><Avatar /></el-icon>
          <span>个人信息</span>
        </el-menu-item>
      </el-menu>

      <div class="sidebar-footer">
        <el-button type="danger" plain class="btn-logout" @click="handleLogout">
          <el-icon><SwitchButton /></el-icon>
          <span>退出登录</span>
        </el-button>
      </div>
    </div>

    <!-- 右侧内容区域 (Main Area) -->
    <div class="main-content">
      <!-- 页面头部 -->
      <div class="content-header">
        <h2 class="page-title">{{ menuTitles[activeMenu] }}</h2>
        <div class="header-actions">
          <el-tag v-if="isMockMode" type="warning" effect="dark" size="small" class="mock-tag">
            本地数据模拟模式 (后端未连接)
          </el-tag>
          <span class="current-date">{{ currentDateStr }}</span>
        </div>
      </div>

      <!-- 页面主体面板 -->
      <div class="content-body" v-loading="loading">
        
        <!-- ==================== 1. 总览统计面板 ==================== -->
        <div v-if="activeMenu === 'overview'" class="panel-overview">
          <el-row :gutter="20">
            <el-col :span="6">
              <el-card class="stat-card" shadow="hover">
                <div class="stat-icon icon-blue"><User /></div>
                <div class="stat-data">
                  <div class="num">{{ stats.userCount }}</div>
                  <div class="label">系统成员数</div>
                </div>
              </el-card>
            </el-col>
            <el-col :span="6">
              <el-card class="stat-card" shadow="hover">
                <div class="stat-icon icon-green"><FolderOpened /></div>
                <div class="stat-data">
                  <div class="num">{{ stats.projectCount }}</div>
                  <div class="label">项目总数</div>
                </div>
              </el-card>
            </el-col>
            <el-col :span="6">
              <el-card class="stat-card" shadow="hover">
                <div class="stat-icon icon-yellow"><List /></div>
                <div class="stat-data">
                  <div class="num">{{ stats.taskCount }}</div>
                  <div class="label">任务总数</div>
                </div>
              </el-card>
            </el-col>
            <el-col :span="6">
              <el-card class="stat-card" shadow="hover">
                <div class="stat-icon icon-red"><DocumentChecked /></div>
                <div class="stat-data">
                  <div class="num">{{ stats.summaryCount }}</div>
                  <div class="label">总结报告数</div>
                </div>
              </el-card>
            </el-col>
          </el-row>

          <el-row :gutter="20" class="overview-details">
            <el-col :span="16">
              <el-card class="box-card">
                <template #header>
                  <div class="card-header">
                    <span>我负责的任务 (进行中)</span>
                    <el-button type="primary" link @click="activeMenu = 'tasks'">查看全部任务</el-button>
                  </div>
                </template>
                <el-table :data="myOngoingTasks" style="width: 100%" empty-text="当前没有正在进行中的负责任务">
                  <el-table-column prop="title" label="任务名称" min-width="150" show-overflow-tooltip />
                  <el-table-column prop="projectName" label="所属项目" min-width="120" />
                  <el-table-column prop="priority" label="优先级" width="90">
                    <template #default="scope">
                      <el-tag :type="getPriorityTag(scope.row.priority)">{{ scope.row.priority }}</el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column prop="due_date" label="截止日期" width="120" />
                  <el-table-column label="操作" width="100">
                    <template #default="scope">
                      <el-button type="primary" link @click="openEditTaskDialog(scope.row)">更新进度</el-button>
                    </template>
                  </el-table-column>
                </el-table>
              </el-card>
            </el-col>

            <el-col :span="8">
              <el-card class="box-card">
                <template #header>
                  <div class="card-header">
                    <span>快捷入口</span>
                  </div>
                </template>
                <div class="quick-links">
                  <el-button v-if="isLeader" type="primary" class="quick-btn" @click="openAddProjectDialog">
                    <el-icon><FolderAdd /></el-icon> 新建项目
                  </el-button>
                  <el-button v-if="isLeader" type="success" class="quick-btn" @click="activeMenu = 'ai-plan'">
                    <el-icon><Cpu /></el-icon> AI 任务拆解
                  </el-button>
                  <el-button type="warning" class="quick-btn" @click="openAddLogDialog(null)">
                    <el-icon><Plus /></el-icon> 新增进度记录
                  </el-button>
                  <el-button type="info" class="quick-btn" @click="openAddSummaryDialog">
                    <el-icon><EditPen /></el-icon> 撰写总结报告
                  </el-button>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </div>

        <!-- ==================== 2. 项目管理面板 ==================== -->
        <div v-if="activeMenu === 'projects'" class="panel-projects">
          <div class="table-actions">
            <el-input 
              v-model="searchProjectQuery" 
              placeholder="搜索项目名称/描述" 
              prefix-icon="Search"
              clearable 
              style="width: 300px; margin-right: 15px;"
            />
            <el-button v-if="isLeader" type="primary" @click="openAddProjectDialog">
              <el-icon><Plus /></el-icon> 新建项目
            </el-button>
          </div>

          <el-table :data="filteredProjects" style="width: 100%">
            <el-table-column prop="id" label="ID" width="70" />
            <el-table-column prop="name" label="项目名称" min-width="150" />
            <el-table-column prop="description" label="项目描述" min-width="200" show-overflow-tooltip />
            <el-table-column prop="status" label="项目状态" width="100">
              <template #default="scope">
                <el-tag :type="getStatusTag(scope.row.status)">{{ scope.row.status }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="priority" label="优先级" width="90">
              <template #default="scope">
                <el-tag :type="getPriorityTag(scope.row.priority)">{{ scope.row.priority }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="ownerName" label="负责人" width="120" />
            <el-table-column label="起止日期" width="220">
              <template #default="scope">
                {{ scope.row.start_date || '未设置' }} 至 {{ scope.row.end_date || '未设置' }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150" v-if="isLeader">
              <template #default="scope">
                <el-button type="primary" link @click="openEditProjectDialog(scope.row)">编辑</el-button>
                <el-button type="danger" link @click="handleDeleteProject(scope.row.id)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <!-- ==================== 3. AI 任务拆解面板 ==================== -->
        <div v-if="activeMenu === 'ai-plan' && isLeader" class="panel-ai-plan">
          <el-row :gutter="24">
            <el-col :span="8">
              <el-card class="ai-input-card">
                <template #header>
                  <div class="card-header">
                    <span>千问 AI 任务拆解</span>
                  </div>
                </template>
                <el-form :model="aiForm" :rules="aiFormRules" ref="aiFormRef" label-position="top">
                  <el-form-item label="选择项目" prop="projectId">
                    <el-select v-model="aiForm.projectId" placeholder="请选择要拆解的项目" style="width: 100%;">
                      <el-option v-for="item in projects" :key="item.id" :label="item.name" :value="item.id" />
                    </el-select>
                  </el-form-item>

                  <el-form-item label="核心目标" prop="goal">
                    <el-input 
                      v-model="aiForm.goal" 
                      placeholder="例如：开发FlowSync系统的登录与注册模块前端页面，确保包含表单校验和路由拦截"
                      type="textarea"
                      :rows="3"
                    />
                  </el-form-item>

                  <el-form-item label="补充说明" prop="description">
                    <el-input 
                      v-model="aiForm.description" 
                      placeholder="输入需要补充的要求，如：限定3个子任务，需要进行密码一致性校验等"
                      type="textarea"
                      :rows="2"
                    />
                  </el-form-item>

                  <el-button type="primary" :loading="aiGenerating" style="width: 100%;" @click="handleGenerateAiPlan">
                    <el-icon><Cpu /></el-icon> AI 自动分解任务
                  </el-button>
                </el-form>
              </el-card>
            </el-col>

            <el-col :span="16">
              <div v-if="aiPlanResult" class="ai-result-section">
                <div class="ai-summary-box">
                  <h4>AI 拆解概述</h4>
                  <p>{{ aiPlanResult.summary }}</p>
                </div>

                <div class="ai-items-box">
                  <div class="items-header">
                    <h4>拆解任务草稿 (勾选确认导入)</h4>
                    <el-button type="success" size="small" @click="handleImportAiTasks">
                      导入选中任务 ({{ selectedAiItems.length }})
                    </el-button>
                  </div>

                  <el-table 
                    :data="aiPlanResult.items" 
                    style="width: 100%; margin-top: 15px;"
                    @selection-change="handleAiSelectionChange"
                  >
                    <el-table-type-column type="selection" width="55" />
                    <el-table-column prop="title" label="任务标题" min-width="120">
                      <template #default="scope">
                        <el-input v-model="scope.row.title" size="small" />
                      </template>
                    </el-table-column>
                    <el-table-column prop="description" label="任务描述" min-width="180">
                      <template #default="scope">
                        <el-input v-model="scope.row.description" type="textarea" :rows="1" size="small" />
                      </template>
                    </el-table-column>
                    <el-table-column prop="priority" label="优先级" width="90">
                      <template #default="scope">
                        <el-select v-model="scope.row.priority" size="small">
                          <el-option label="高" value="高" />
                          <el-option label="中" value="中" />
                          <el-option label="低" value="低" />
                        </el-select>
                      </template>
                    </el-table-column>
                    <el-table-column prop="suggestedDays" label="建议天数" width="90">
                      <template #default="scope">
                        <el-input-number v-model="scope.row.suggestedDays" :min="1" controls-position="right" size="small" style="width: 70px;" />
                      </template>
                    </el-table-column>
                    <el-table-column label="负责人" width="130">
                      <template #default="scope">
                        <el-select v-model="scope.row.assigneeId" placeholder="选择负责人" size="small">
                          <el-option v-for="user in users" :key="user.id" :label="user.realName" :value="user.id" />
                        </el-select>
                      </template>
                    </el-table-column>
                  </el-table>
                </div>
              </div>
              
              <div v-else class="ai-empty-box">
                <el-empty description="在左侧输入项目和目标，开启大模型 AI 智能任务拆分功能" />
              </div>
            </el-col>
          </el-row>
        </div>

        <!-- ==================== 4. 任务管理面板 ==================== -->
        <div v-if="activeMenu === 'tasks'" class="panel-tasks">
          <div class="table-actions">
            <el-select v-model="taskFilterProjectId" placeholder="按项目筛选" clearable style="width: 200px; margin-right: 15px;">
              <el-option v-for="item in projects" :key="item.id" :label="item.name" :value="item.id" />
            </el-select>
            <el-select v-model="taskFilterStatus" placeholder="按状态筛选" clearable style="width: 150px; margin-right: 15px;">
              <el-option label="未开始" value="未开始" />
              <el-option label="进行中" value="进行中" />
              <el-option label="已完成" value="已完成" />
            </el-select>
            <el-button v-if="isLeader" type="primary" @click="openAddTaskDialog">
              <el-icon><Plus /></el-icon> 新建任务
            </el-button>
          </div>

          <el-table :data="filteredTasks" style="width: 100%" row-key="id" default-expand-all>
            <el-table-column prop="id" label="ID" width="70" />
            <el-table-column prop="title" label="任务标题" min-width="150" />
            <el-table-column prop="projectName" label="所属项目" min-width="120" />
            <el-table-column prop="assigneeName" label="负责人" width="100" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="scope">
                <el-tag :type="getStatusTag(scope.row.status)">{{ scope.row.status }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="priority" label="优先级" width="90">
              <template #default="scope">
                <el-tag :type="getPriorityTag(scope.row.priority)">{{ scope.row.priority }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="due_date" label="截止日期" width="110" />
            <el-table-column label="操作" width="280">
              <template #default="scope">
                <!-- 负责人可以编辑全部，成员只能在属于自己负责的任务上点击更新状态 -->
                <el-button 
                  v-if="isLeader || scope.row.assigneeId === currentUser.id" 
                  type="primary" 
                  link 
                  @click="openEditTaskDialog(scope.row)"
                >
                  {{ isLeader ? '编辑' : '更新状态' }}
                </el-button>
                <el-button type="warning" link @click="handleGetAiSuggestion(scope.row)">AI建议</el-button>
                <el-button type="info" link @click="openAddLogDialog(scope.row.id)">进度反馈</el-button>
                <el-button v-if="isLeader" type="danger" link @click="handleDeleteTask(scope.row.id)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <!-- ==================== 5. 进度跟踪面板 ==================== -->
        <div v-if="activeMenu === 'logs'" class="panel-logs">
          <div class="table-actions">
            <el-select v-model="logFilterTaskId" placeholder="按关联任务筛选" clearable style="width: 250px; margin-right: 15px;">
              <el-option v-for="item in tasks" :key="item.id" :label="item.title" :value="item.id" />
            </el-select>
            <el-button type="primary" @click="openAddLogDialog(null)">
              <el-icon><Plus /></el-icon> 新增进度记录
            </el-button>
          </div>

          <el-table :data="filteredLogs" style="width: 100%">
            <el-table-column prop="id" label="ID" width="70" />
            <el-table-column prop="taskTitle" label="关联任务" min-width="150" />
            <el-table-column prop="progress_percent" label="进度百分比" width="120">
              <template #default="scope">
                <el-progress :percentage="scope.row.progress_percent" />
              </template>
            </el-table-column>
            <el-table-column prop="content" label="进展说明" min-width="250" show-overflow-tooltip />
            <el-table-column prop="operatorName" label="汇报人" width="100" />
            <el-table-column prop="create_time" label="汇报时间" width="160" />
          </el-table>
        </div>

        <!-- ==================== 6. 总结中心面板 ==================== -->
        <div v-if="activeMenu === 'summaries'" class="panel-summaries">
          <div class="table-actions">
            <el-button type="primary" @click="openAddSummaryDialog">
              <el-icon><Plus /></el-icon> 新撰写总结报告
            </el-button>
          </div>

          <el-table :data="summaries" style="width: 100%">
            <el-table-column prop="id" label="ID" width="70" />
            <el-table-column prop="projectName" label="所属项目" min-width="120" />
            <el-table-column prop="taskTitle" label="关联任务" min-width="120">
              <template #default="scope">
                {{ scope.row.taskTitle || '全局项目总结' }}
              </template>
            </el-table-column>
            <el-table-column prop="summary_type" label="总结类型" width="100">
              <template #default="scope">
                <el-tag :type="scope.row.summary_type === '最终总结' ? 'success' : 'info'">
                  {{ scope.row.summary_type }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="content" label="总结内容" min-width="300" show-overflow-tooltip />
            <el-table-column prop="creatorName" label="总结人" width="100" />
            <el-table-column prop="create_time" label="提交时间" width="160" />
          </el-table>
        </div>

        <!-- ==================== 7. 成员列表面板 ==================== -->
        <div v-if="activeMenu === 'members'" class="panel-members">
          <el-table :data="users" style="width: 100%">
            <el-table-column prop="id" label="用户ID" width="90" />
            <el-table-column prop="realName" label="真实姓名" min-width="120" />
            <el-table-column prop="username" label="登录账号 (用户名)" min-width="120" />
            <el-table-column prop="role" label="系统角色" width="120">
              <template #default="scope">
                <span class="role-tag" :class="scope.row.role === '负责人' ? 'role-leader' : 'role-member'">
                  {{ scope.row.role }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="phone" label="联系电话" width="140" />
            <el-table-column prop="email" label="电子邮箱" min-width="160" />
            <el-table-column prop="create_time" label="注册时间" width="160" />
          </el-table>
        </div>

        <!-- ==================== 8. 个人信息与修改密码面板 ==================== -->
        <div v-if="activeMenu === 'profile'" class="panel-profile">
          <el-row :gutter="24">
            <el-col :span="12">
              <el-card class="box-card">
                <template #header>
                  <div class="card-header">
                    <span>个人信息</span>
                  </div>
                </template>
                <el-descriptions :column="1" border>
                  <el-descriptions-item label="登录账号">{{ currentUser.username }}</el-descriptions-item>
                  <el-descriptions-item label="真实姓名">{{ currentUser.realName }}</el-descriptions-item>
                  <el-descriptions-item label="系统角色">{{ currentUser.role }}</el-descriptions-item>
                  <el-descriptions-item label="联系电话">{{ currentUser.phone || '未设置' }}</el-descriptions-item>
                  <el-descriptions-item label="电子邮箱">{{ currentUser.email || '未设置' }}</el-descriptions-item>
                </el-descriptions>
              </el-card>
            </el-col>

            <el-col :span="12">
              <el-card class="box-card">
                <template #header>
                  <div class="card-header">
                    <span>安全设置 (修改密码)</span>
                  </div>
                </template>
                <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-position="top">
                  <el-form-item label="旧密码" prop="oldPassword">
                    <el-input v-model="passwordForm.oldPassword" type="password" placeholder="请输入当前密码" show-password />
                  </el-form-item>

                  <el-form-item label="新密码" prop="newPassword">
                    <el-input v-model="passwordForm.newPassword" type="password" placeholder="请输入新密码" show-password />
                  </el-form-item>

                  <el-form-item label="确认新密码" prop="confirmNewPassword">
                    <el-input v-model="passwordForm.confirmNewPassword" type="password" placeholder="请再次输入新密码" show-password />
                  </el-form-item>

                  <el-button type="primary" style="width: 100%;" @click="handleChangePassword">
                    更新密码
                  </el-button>
                </el-form>
              </el-card>
            </el-col>
          </el-row>
        </div>

      </div>
    </div>

    <!-- ==================== A. 项目弹窗 (新增/修改) ==================== -->
    <el-dialog :title="projectDialogTitle" v-model="projectDialogVisible" width="500px">
      <el-form :model="projectForm" :rules="projectFormRules" ref="projectFormRef" label-position="top">
        <el-form-item label="项目名称" prop="name">
          <el-input v-model="projectForm.name" placeholder="请输入项目名称" />
        </el-form-item>
        
        <el-form-item label="项目描述" prop="description">
          <el-input v-model="projectForm.description" type="textarea" :rows="3" placeholder="请输入项目内容或主要背景" />
        </el-form-item>

        <el-row :gutter="15">
          <el-col :span="12">
            <el-form-item label="项目状态" prop="status">
              <el-select v-model="projectForm.status" placeholder="选择状态" style="width: 100%;">
                <el-option label="未开始" value="未开始" />
                <el-option label="进行中" value="进行中" />
                <el-option label="已完成" value="已完成" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="优先级" prop="priority">
              <el-select v-model="projectForm.priority" placeholder="选择优先级" style="width: 100%;">
                <el-option label="低" value="低" />
                <el-option label="中" value="中" />
                <el-option label="高" value="高" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="15">
          <el-col :span="12">
            <el-form-item label="开始日期" prop="start_date">
              <el-date-picker v-model="projectForm.start_date" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束日期" prop="end_date">
              <el-date-picker v-model="projectForm.end_date" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%;" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="更换负责人" prop="owner_id">
          <el-select v-model="projectForm.owner_id" placeholder="选择项目负责人" style="width: 100%;">
            <el-option v-for="item in users" :key="item.id" :label="item.realName" :value="item.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="projectDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveProject">确定保存</el-button>
      </template>
    </el-dialog>

    <!-- ==================== B. 任务弹窗 (新增/编辑/成员修改状态) ==================== -->
    <el-dialog :title="taskDialogTitle" v-model="taskDialogVisible" width="550px">
      <el-form :model="taskForm" :rules="taskFormRules" ref="taskFormRef" label-position="top">
        
        <el-form-item label="所属项目" prop="project_id">
          <el-select 
            v-model="taskForm.project_id" 
            placeholder="请选择项目" 
            style="width: 100%;" 
            :disabled="!isLeader"
            @change="handleTaskProjectChange"
          >
            <el-option v-for="item in projects" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>

        <el-form-item label="父级任务 (可选，创建任务子层级)" prop="parent_id">
          <el-select 
            v-model="taskForm.parent_id" 
            placeholder="无 (设为顶层任务)" 
            clearable 
            style="width: 100%;" 
            :disabled="!isLeader"
          >
            <el-option 
              v-for="item in currentProjectTasks" 
              :key="item.id" 
              :label="item.title" 
              :value="item.id"
              :disabled="item.id === taskForm.id" 
            />
          </el-select>
        </el-form-item>

        <el-form-item label="任务标题" prop="title">
          <el-input v-model="taskForm.title" placeholder="请输入任务标题" :disabled="!isLeader" />
        </el-form-item>

        <el-form-item label="任务说明" prop="description">
          <el-input v-model="taskForm.description" type="textarea" :rows="3" placeholder="请输入任务具体描述" :disabled="!isLeader" />
        </el-form-item>

        <el-row :gutter="15">
          <el-col :span="12">
            <el-form-item label="负责人" prop="assignee_id">
              <el-select v-model="taskForm.assignee_id" placeholder="选择任务负责人" style="width: 100%;" :disabled="!isLeader">
                <el-option v-for="item in users" :key="item.id" :label="item.realName" :value="item.id" />
              </el-select>
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="优先级" prop="priority">
              <el-select v-model="taskForm.priority" placeholder="选择优先级" style="width: 100%;" :disabled="!isLeader">
                <el-option label="低" value="低" />
                <el-option label="中" value="中" />
                <el-option label="高" value="高" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="15">
          <el-col :span="12">
            <el-form-item label="任务状态" prop="status">
              <el-select v-model="taskForm.status" placeholder="选择任务状态" style="width: 100%;">
                <el-option label="未开始" value="未开始" />
                <el-option label="进行中" value="进行中" />
                <el-option label="已完成" value="已完成" />
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="截止日期" prop="due_date">
              <el-date-picker v-model="taskForm.due_date" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%;" :disabled="!isLeader" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="taskDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveTask">保存</el-button>
      </template>
    </el-dialog>

    <!-- ==================== C. 新增进度汇报弹窗 ==================== -->
    <el-dialog title="新增进度跟踪汇报" v-model="logDialogVisible" width="460px">
      <el-form :model="logForm" :rules="logFormRules" ref="logFormRef" label-position="top">
        <el-form-item label="汇报对应任务" prop="task_id">
          <el-select v-model="logForm.task_id" placeholder="选择要反馈进度的任务" style="width: 100%;">
            <el-option 
              v-for="item in tasks" 
              :key="item.id" 
              :label="`[${item.projectName}] ${item.title}`" 
              :value="item.id" 
            />
          </el-select>
        </el-form-item>

        <el-form-item label="当前完成进度 (%)" prop="progress_percent">
          <div style="display: flex; align-items: center; gap: 15px; width: 100%;">
            <el-slider v-model="logForm.progress_percent" :min="0" :max="100" style="flex: 1;" />
            <span style="font-weight: bold; width: 40px; text-align: right;">{{ logForm.progress_percent }}%</span>
          </div>
        </el-form-item>

        <el-form-item label="工作进展说明" prop="content">
          <el-input v-model="logForm.content" type="textarea" :rows="3" placeholder="请输入目前所完成的具体工作和成果" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="logDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveLog">汇报提交</el-button>
      </template>
    </el-dialog>

    <!-- ==================== D. 新撰写总结报告弹窗 ==================== -->
    <el-dialog title="新建项目总结" v-model="summaryDialogVisible" width="500px">
      <el-form :model="summaryForm" :rules="summaryFormRules" ref="summaryFormRef" label-position="top">
        <el-form-item label="所属项目" prop="project_id">
          <el-select v-model="summaryForm.project_id" placeholder="选择项目" style="width: 100%;" @change="handleSummaryProjectChange">
            <el-option v-for="item in projects" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>

        <el-form-item label="关联任务 (可选)" prop="task_id">
          <el-select v-model="summaryForm.task_id" placeholder="选择任务" clearable style="width: 100%;">
            <el-option v-for="item in currentProjectTasks" :key="item.id" :label="item.title" :value="item.id" />
          </el-select>
        </el-form-item>

        <el-form-item label="总结类型" prop="summary_type">
          <el-radio-group v-model="summaryForm.summary_type">
            <el-radio label="阶段总结">阶段工作总结</el-radio>
            <el-radio label="最终总结">项目终结报告</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="总结说明内容" prop="content">
          <el-input v-model="summaryForm.content" type="textarea" :rows="5" placeholder="请输入小组成果、经验总结或面临的问题" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="summaryDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveSummary">提交总结</el-button>
      </template>
    </el-dialog>

    <!-- ==================== E. AI 建议抽屉展示 ==================== -->
    <el-drawer 
      title="千问 AI 任务建议助手" 
      v-model="aiDrawerVisible" 
      size="380px"
      direction="rtl"
    >
      <div v-loading="aiSuggestionLoading" class="ai-drawer-content">
        <div v-if="aiSuggestion" class="ai-suggestion-body">
          <div class="ai-badge">
            <el-icon><Cpu /></el-icon> AI 智能赋能建议
          </div>
          <div class="suggestion-text">
            {{ aiSuggestion }}
          </div>
          <div class="ai-disclaimer">
            * 建议由千问大模型计算得出，旨在提供指导，具体执行方案仍由负责人把控。
          </div>
        </div>
        <div v-else class="ai-no-suggestion">
          <el-empty description="点击开始，获取千问大模型对于当前任务的拆分、顺序与风险预测建议。" />
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { 
  projectApi, taskApi, taskLogApi, summaryApi, 
  overviewApi, userApi, aiApi, authApi 
} from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'

export default {
  name: 'HomeView',
  setup() {
    const router = useRouter()
    
    // ==================== 0. 用户状态管理与初始化 ====================
    const currentUser = ref({ id: null, username: '', realName: '', role: '', phone: '', email: '' })
    const activeMenu = ref('overview')
    const loading = ref(false)
    const isMockMode = ref(false) // 判定是否启用前端 Mock 数据降级模式
    
    const isLeader = computed(() => currentUser.value.role === '负责人')
    
    const menuTitles = {
      overview: '工作台总览',
      projects: '项目信息管理',
      'ai-plan': 'AI 大模型智能任务拆解',
      tasks: '团队任务协同看板',
      logs: '工作进度汇报跟踪',
      summaries: '总结与交付报告中心',
      members: '团队协同成员列表',
      profile: '个人中心与系统安全'
    }

    const currentDateStr = computed(() => {
      const now = new Date()
      return `${now.getFullYear()}年${now.getMonth() + 1}月${now.getDate()}日`
    })

    // ==================== 本地 Mock 数据库 (LocalStorage 兜底) ====================
    const mockDB = {
      users: [
        { id: 1, username: 'admin', realName: '系统管理员', role: '负责人', phone: '13800000000', email: 'admin@flowsync.com', create_time: '2026-07-01 10:00:00' },
        { id: 2, username: 'leader', realName: '项目负责人', role: '负责人', phone: '13811111111', email: 'leader@flowsync.com', create_time: '2026-07-01 10:05:00' },
        { id: 3, username: 'member1', realName: '张三', role: '成员', phone: '13922222222', email: 'zhangsan@flowsync.com', create_time: '2026-07-02 09:30:00' },
        { id: 4, username: 'member2', realName: '李四', role: '成员', phone: '13933333333', email: 'lisi@flowsync.com', create_time: '2026-07-02 10:15:00' }
      ],
      projects: [
        { id: 1, name: 'FlowSync 小组协同系统', description: '面向教学场景的小组协同任务管理平台，集成阿里云千问大模型进行辅助任务拆分与建议。', status: '进行中', priority: '高', owner_id: 2, ownerName: '项目负责人', start_date: '2026-07-01', end_date: '2026-07-30' },
        { id: 2, name: '人工智能图像算法实践', description: '小组实践：基于PyTorch搭建残差卷积神经网络进行植物叶片病害识别。', status: '未开始', priority: '中', owner_id: 2, ownerName: '项目负责人', start_date: '2026-08-01', end_date: '2026-08-25' }
      ],
      tasks: [
        { id: 1, project_id: 1, projectName: 'FlowSync 小组协同系统', parent_id: null, title: '编写需求规格说明书', description: '梳理和编写系统的功能模块、ER关系设计、API详细说明书文档。', assignee_id: 2, assigneeName: '项目负责人', creator_id: 2, status: '已完成', priority: '高', due_date: '2026-07-08' },
        { id: 2, project_id: 1, projectName: 'FlowSync 小组协同系统', parent_id: null, title: '前端登录与注册页面开发', description: '利用HTML/JS/Vue3以及Element Plus绘制美观的登录和注册页面，并且完成密码的一致性验证和路由跳转。', assignee_id: 3, assigneeName: '张三', creator_id: 2, status: '进行中', priority: '高', due_date: '2026-07-15' },
        { id: 3, project_id: 1, projectName: 'FlowSync 小组协同系统', parent_id: 2, title: '实现密码一致性校验逻辑', description: '子任务：在注册界面的前端表单增加自定义 validator 限制两次密码不匹配时报错。', assignee_id: 3, assigneeName: '张三', creator_id: 2, status: '进行中', priority: '中', due_date: '2026-07-12' },
        { id: 4, project_id: 1, projectName: 'FlowSync 小组协同系统', parent_id: null, title: '后端核心接口与数据库建表', description: '设计 sys_user, project_info, task_info, task_log, task_summary 等5张业务表并利用Spring Boot提供接口。', assignee_id: 4, assigneeName: '李四', creator_id: 2, status: '进行中', priority: '高', due_date: '2026-07-18' }
      ],
      logs: [
        { id: 1, task_id: 1, taskTitle: '编写需求规格说明书', progress_percent: 100, content: '需求规格说明书已完成评审并输出为 Markdown 规格文档。', operator_id: 2, operatorName: '项目负责人', create_time: '2026-07-08 09:00:00' },
        { id: 2, task_id: 2, taskTitle: '前端登录与注册页面开发', progress_percent: 60, content: '已完成登录注册的视觉布局以及表单验证规则，接下来编写 API 对接代码。', operator_id: 3, operatorName: '张三', create_time: '2026-07-08 10:20:00' }
      ],
      summaries: [
        { id: 1, project_id: 1, projectName: 'FlowSync 小组协同系统', task_id: 1, taskTitle: '编写需求规格说明书', summary_type: '阶段总结', content: '第一阶段的需求梳理非常顺利，明确了项目、任务、进度、总结的核心流转闭环，为后续前后端并发编码打下了坚实基础。', created_by: 2, creatorName: '项目负责人', create_time: '2026-07-08 09:30:00' }
      ]
    }

    const initMockData = () => {
      if (!localStorage.getItem('fs_users')) localStorage.setItem('fs_users', JSON.stringify(mockDB.users))
      if (!localStorage.getItem('fs_projects')) localStorage.setItem('fs_projects', JSON.stringify(mockDB.projects))
      if (!localStorage.getItem('fs_tasks')) localStorage.setItem('fs_tasks', JSON.stringify(mockDB.tasks))
      if (!localStorage.getItem('fs_logs')) localStorage.setItem('fs_logs', JSON.stringify(mockDB.logs))
      if (!localStorage.getItem('fs_summaries')) localStorage.setItem('fs_summaries', JSON.stringify(mockDB.summaries))
    }

    // ==================== 1. 数据模型与列表定义 ====================
    const stats = ref({ userCount: 0, projectCount: 0, taskCount: 0, summaryCount: 0 })
    const projects = ref([])
    const tasks = ref([])
    const logs = ref([])
    const summaries = ref([])
    const users = ref([])

    const myOngoingTasks = computed(() => {
      return tasks.value.filter(t => t.assignee_id === currentUser.value.id && t.status !== '已完成')
    })

    // 筛选过滤器
    const searchProjectQuery = ref('')
    const filteredProjects = computed(() => {
      if (!searchProjectQuery.value) return projects.value
      const query = searchProjectQuery.value.toLowerCase()
      return projects.value.filter(p => 
        p.name.toLowerCase().includes(query) || 
        (p.description && p.description.toLowerCase().includes(query))
      )
    })

    const taskFilterProjectId = ref('')
    const taskFilterStatus = ref('')
    const filteredTasks = computed(() => {
      let result = tasks.value
      if (taskFilterProjectId.value) {
        result = result.filter(t => t.project_id === taskFilterProjectId.value)
      }
      if (taskFilterStatus.value) {
        result = result.filter(t => t.status === taskFilterStatus.value)
      }
      return result
    })

    const logFilterTaskId = ref('')
    const filteredLogs = computed(() => {
      if (!logFilterTaskId.value) return logs.value
      return logs.value.filter(l => l.task_id === logFilterTaskId.value)
    })

    // ==================== 2. 全局数据装载 (API 加载，出错则自动切换至 Mock 模式) ====================
    const loadAllData = async () => {
      loading.value = true
      try {
        // 尝试加载真实用户
        const userRes = await userApi.getUsers()
        if (userRes.success) {
          users.value = userRes.data || []
          isMockMode.value = false
        }
      } catch (err) {
        console.warn('后端 API 连接失败，将自动降级为本地独立演示模式 (Mock Mode)。')
        isMockMode.value = true
        initMockData()
      }

      if (isMockMode.value) {
        // 读取 LocalStorage 模拟数据
        users.value = JSON.parse(localStorage.getItem('fs_users') || '[]')
        projects.value = JSON.parse(localStorage.getItem('fs_projects') || '[]')
        tasks.value = JSON.parse(localStorage.getItem('fs_tasks') || '[]')
        logs.value = JSON.parse(localStorage.getItem('fs_logs') || '[]')
        summaries.value = JSON.parse(localStorage.getItem('fs_summaries') || '[]')
        
        // 计算统计信息
        stats.value = {
          userCount: users.value.length,
          projectCount: projects.value.length,
          taskCount: tasks.value.length,
          summaryCount: summaries.value.length
        }
      } else {
        // 真实 API 加载
        try {
          const [projRes, taskRes, logRes, sumRes, statRes] = await Promise.all([
            projectApi.getProjects(),
            taskApi.getTasks(),
            taskLogApi.getTaskLogs(),
            summaryApi.getSummaries(),
            overviewApi.getOverview()
          ])
          
          if (projRes.success) projects.value = projRes.data || []
          if (taskRes.success) tasks.value = taskRes.data || []
          if (logRes.success) logs.value = logRes.data || []
          if (sumRes.success) summaries.value = sumRes.data || []
          if (statRes.success && statRes.data) {
            stats.value = statRes.data
          } else {
            stats.value = {
              userCount: users.value.length,
              projectCount: projects.value.length,
              taskCount: tasks.value.length,
              summaryCount: summaries.value.length
            }
          }
        } catch (error) {
          console.error('加载业务数据失败:', error)
        }
      }
      
      // 为项目、任务、进度、总结匹配关联名称
      enhanceDataBindings()
      loading.value = false
    }

    const enhanceDataBindings = () => {
      // 匹配负责人名字
      projects.value.forEach(p => {
        const u = users.value.find(user => user.id === p.owner_id)
        p.ownerName = u ? u.realName : `用户ID:${p.owner_id}`
      })
      
      tasks.value.forEach(t => {
        const p = projects.value.find(proj => proj.id === t.project_id)
        t.projectName = p ? p.name : `项目ID:${t.project_id}`
        
        const u = users.value.find(user => user.id === t.assignee_id)
        t.assigneeName = u ? u.realName : `用户ID:${t.assignee_id}`
      })

      logs.value.forEach(l => {
        const t = tasks.value.find(task => task.id === l.task_id)
        l.taskTitle = t ? t.title : `任务ID:${l.task_id}`
        
        const u = users.value.find(user => user.id === l.operator_id)
        l.operatorName = u ? u.realName : `汇报人ID:${l.operator_id}`
      })

      summaries.value.forEach(s => {
        const p = projects.value.find(proj => proj.id === s.project_id)
        s.projectName = p ? p.name : `项目ID:${s.project_id}`
        
        if (s.task_id) {
          const t = tasks.value.find(task => task.id === s.task_id)
          s.taskTitle = t ? t.title : `任务ID:${s.task_id}`
        }
        
        const u = users.value.find(user => user.id === s.created_by)
        s.creatorName = u ? u.realName : `用户ID:${s.created_by}`
      })
    }

    // ==================== 3. 页面核心操作交互 ====================
    
    // --- A. 项目增改 ---
    const projectDialogVisible = ref(false)
    const projectDialogTitle = ref('新建项目')
    const projectFormRef = ref(null)
    const projectForm = ref({ id: null, name: '', description: '', status: '未开始', priority: '中', owner_id: null, start_date: '', end_date: '' })
    const projectFormRules = {
      name: [{ required: true, message: '请输入项目名称', trigger: 'blur' }],
      status: [{ required: true, message: '请选择状态', trigger: 'change' }],
      priority: [{ required: true, message: '请选择优先级', trigger: 'change' }],
      owner_id: [{ required: true, message: '请选择项目负责人', trigger: 'change' }]
    }

    const openAddProjectDialog = () => {
      projectDialogTitle.value = '新建项目'
      projectForm.value = { id: null, name: '', description: '', status: '未开始', priority: '中', owner_id: currentUser.value.id, start_date: '', end_date: '' }
      projectDialogVisible.value = true
    }

    const openEditProjectDialog = (row) => {
      projectDialogTitle.value = '编辑项目'
      projectForm.value = { ...row }
      projectDialogVisible.value = true
    }

    const handleSaveProject = () => {
      projectFormRef.value.validate(async (valid) => {
        if (!valid) return
        
        if (isMockMode.value) {
          let list = JSON.parse(localStorage.getItem('fs_projects') || '[]')
          if (projectForm.value.id) {
            const index = list.findIndex(p => p.id === projectForm.value.id)
            if (index !== -1) list[index] = { ...projectForm.value }
            ElMessage.success('本地模拟修改项目成功')
          } else {
            const newId = list.length > 0 ? Math.max(...list.map(p => p.id)) + 1 : 1
            projectForm.value.id = newId
            list.push({ ...projectForm.value })
            ElMessage.success('本地模拟创建项目成功')
          }
          localStorage.setItem('fs_projects', JSON.stringify(list))
          projectDialogVisible.value = false
          loadAllData()
        } else {
          try {
            const res = await projectApi.saveProject(projectForm.value)
            if (res.success) {
              ElMessage.success(res.message || '项目保存成功')
              projectDialogVisible.value = false
              loadAllData()
            }
          } catch (e) { console.error(e) }
        }
      })
    }

    const handleDeleteProject = (id) => {
      ElMessageBox.confirm('确定要删除该项目吗？这不会影响已有任务数据（极速教学精简机制）。', '安全确认', {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        if (isMockMode.value) {
          let list = JSON.parse(localStorage.getItem('fs_projects') || '[]')
          list = list.filter(p => p.id !== id)
          localStorage.setItem('fs_projects', JSON.stringify(list))
          ElMessage.success('本地模拟删除项目成功')
          loadAllData()
        } else {
          try {
            const res = await projectApi.deleteProject(id)
            if (res.success) {
              ElMessage.success(res.message || '项目删除成功')
              loadAllData()
            }
          } catch (e) { console.error(e) }
        }
      }).catch(() => {})
    }

    // --- B. 任务拆解 (AI 能力) ---
    const aiForm = ref({ projectId: '', goal: '', description: '' })
    const aiFormRules = {
      projectId: [{ required: true, message: '请选择项目', trigger: 'change' }],
      goal: [{ required: true, message: '请输入核心任务目标', trigger: 'blur' }]
    }
    const aiGenerating = ref(false)
    const aiPlanResult = ref(null)
    const selectedAiItems = ref([])

    const handleGenerateAiPlan = () => {
      aiFormRef.value.validate(async (valid) => {
        if (!valid) return
        aiGenerating.value = true
        aiPlanResult.value = null
        
        const proj = projects.value.find(p => p.id === aiForm.value.projectId)
        const projName = proj ? proj.name : ''
        
        try {
          if (isMockMode.value) {
            // 延迟模拟千问调用
            await new Promise(resolve => setTimeout(resolve, 2000))
            // 构造模拟千问的返回
            aiPlanResult.value = {
              summary: `针对项目「${projName}」下任务目标「${aiForm.value.goal}」，阿里云千问模型为您细分了以下可执行的工作项，并结合小组成员专长推荐了第一责任人。`,
              items: [
                { title: '搭建项目脚手架与登录界面开发', description: '初始化 Vue3 工程，引入 Element Plus，并开发包含账号、密码、手机号校验的登录与注册界面。', priority: '高', suggestedDays: 3, assigneeId: 3 },
                { title: '设计后端数据表及编写登录认证接口', description: '设计 sys_user 用户表，明文存储（教学简化），提供 POST /api/auth/login 的校验能力。', priority: '高', suggestedDays: 2, assigneeId: 4 },
                { title: '前后台联调测试与功能上线总结', description: '进行跨域代理调试，保障 Axios 请求自动携带 currentUserId，并撰写项目第一阶段总结。', priority: '中', suggestedDays: 2, assigneeId: currentUser.value.id }
              ]
            }
            ElMessage.success('千问大模型 AI 任务拆解完成！')
          } else {
            const res = await aiApi.getTaskPlan(aiForm.value.projectId, projName, aiForm.value.goal, aiForm.value.description)
            if (res.success && res.data) {
              aiPlanResult.value = res.data
              ElMessage.success('大模型拆解成功')
            }
          }
        } catch (e) {
          console.error(e)
          // 兜底方案
          aiPlanResult.value = {
            summary: '千问调用失败，系统已触发 Fallback 备用方案，为您生成3个标准任务模板：准备资料、执行主体、检查总结。',
            items: [
              { title: '准备项目基础资料与环境搭建', description: '搜集开发需要的资料文档，配置运行环境。', priority: '中', suggestedDays: 1, assigneeId: currentUser.value.id },
              { title: '主体功能编写与开发实现', description: '实现模块的核心业务逻辑编码工作。', priority: '高', suggestedDays: 4, assigneeId: users.value[0]?.id || currentUser.value.id },
              { title: '功能测试及撰写阶段总结报告', description: '测试功能连通性，将总结写入总结中心。', priority: '低', suggestedDays: 1, assigneeId: currentUser.value.id }
            ]
          }
          ElMessage.warning('千问接口异常，已为您展示兜底任务草稿。')
        } finally {
          aiGenerating.value = false
        }
      })
    }

    const handleAiSelectionChange = (val) => {
      selectedAiItems.value = val
    }

    const handleImportAiTasks = async () => {
      if (selectedAiItems.value.length === 0) {
        ElMessage.warning('请至少勾选一个任务进行导入')
        return
      }
      
      // 校验负责人非空
      const hasEmptyAssignee = selectedAiItems.value.some(item => !item.assigneeId)
      if (hasEmptyAssignee) {
        ElMessage.warning('勾选导入的任务中包含未分配负责人的项，请分配后再试')
        return
      }

      if (isMockMode.value) {
        let taskList = JSON.parse(localStorage.getItem('fs_tasks') || '[]')
        const baseId = taskList.length > 0 ? Math.max(...taskList.map(t => t.id)) : 0
        
        selectedAiItems.value.forEach((item, index) => {
          const userObj = users.value.find(u => u.id === item.assigneeId)
          const assigneeName = userObj ? userObj.realName : `用户ID:${item.assigneeId}`
          
          taskList.push({
            id: baseId + index + 1,
            project_id: aiForm.value.projectId,
            parent_id: null,
            title: item.title,
            description: item.description,
            assignee_id: item.assigneeId,
            assigneeName: assigneeName,
            creator_id: currentUser.value.id,
            status: '未开始',
            priority: item.priority || '中',
            due_date: new Date(Date.now() + item.suggestedDays * 24 * 3600 * 1000).toISOString().split('T')[0]
          })
        })
        
        localStorage.setItem('fs_tasks', JSON.stringify(taskList))
        ElMessage.success(`成功导入 ${selectedAiItems.value.length} 个任务至当前项目！`)
        aiPlanResult.value = null
        activeMenu.value = 'tasks'
        loadAllData()
      } else {
        try {
          const res = await aiApi.importTaskPlan(
            aiForm.value.projectId, 
            currentUser.value.id, 
            selectedAiItems.value.map(item => ({
              title: item.title,
              description: item.description,
              priority: item.priority,
              suggestedDays: item.suggestedDays,
              assigneeId: item.assigneeId
            }))
          )
          if (res.success) {
            ElMessage.success(res.message || 'AI拆分任务导入成功！')
            aiPlanResult.value = null
            activeMenu.value = 'tasks'
            loadAllData()
          }
        } catch (e) { console.error(e) }
      }
    }

    // --- C. 任务增改与树层级 ---
    const taskDialogVisible = ref(false)
    const taskDialogTitle = ref('新建任务')
    const taskFormRef = ref(null)
    const taskForm = ref({ id: null, project_id: '', parent_id: null, title: '', description: '', assignee_id: null, creator_id: null, status: '未开始', priority: '中', due_date: '' })
    const taskFormRules = {
      project_id: [{ required: true, message: '请选择项目', trigger: 'change' }],
      title: [{ required: true, message: '请输入任务标题', trigger: 'blur' }],
      assignee_id: [{ required: true, message: '请分配任务负责人', trigger: 'change' }],
      status: [{ required: true, message: '请选择状态', trigger: 'change' }],
      priority: [{ required: true, message: '请选择优先级', trigger: 'change' }]
    }

    const currentProjectTasks = ref([]) // 用于二级层级下拉项展示
    
    const handleTaskProjectChange = (val) => {
      taskForm.value.parent_id = null
      if (val) {
        currentProjectTasks.value = tasks.value.filter(t => t.project_id === val && !t.parent_id)
      } else {
        currentProjectTasks.value = []
      }
    }

    const openAddTaskDialog = () => {
      taskDialogTitle.value = '新建任务'
      taskForm.value = { id: null, project_id: '', parent_id: null, title: '', description: '', assignee_id: null, creator_id: currentUser.value.id, status: '未开始', priority: '中', due_date: '' }
      currentProjectTasks.value = []
      taskDialogVisible.value = true
    }

    const openEditTaskDialog = (row) => {
      taskForm.value = { ...row }
      if (!isLeader.value) {
        taskDialogTitle.value = '更新任务状态'
      } else {
        taskDialogTitle.value = '编辑任务'
        // 装载当前项目的顶层任务供自关联选择
        currentProjectTasks.value = tasks.value.filter(t => t.project_id === row.project_id && !t.parent_id)
      }
      taskDialogVisible.value = true
    }

    const handleSaveTask = () => {
      taskFormRef.value.validate(async (valid) => {
        if (!valid) return
        
        if (isMockMode.value) {
          let list = JSON.parse(localStorage.getItem('fs_tasks') || '[]')
          if (taskForm.value.id) {
            const index = list.findIndex(t => t.id === taskForm.value.id)
            if (index !== -1) list[index] = { ...taskForm.value }
            ElMessage.success('本地模拟修改任务成功')
          } else {
            const newId = list.length > 0 ? Math.max(...list.map(t => t.id)) + 1 : 1
            taskForm.value.id = newId
            list.push({ ...taskForm.value })
            ElMessage.success('本地模拟创建任务成功')
          }
          localStorage.setItem('fs_tasks', JSON.stringify(list))
          taskDialogVisible.value = false
          loadAllData()
        } else {
          try {
            const res = await taskApi.saveTask(taskForm.value)
            if (res.success) {
              ElMessage.success(res.message || '任务保存成功')
              taskDialogVisible.value = false
              loadAllData()
            }
          } catch (e) { console.error(e) }
        }
      })
    }

    const handleDeleteTask = (id) => {
      ElMessageBox.confirm('确定要删除该任务吗？（极速教学精简机制，不级联删除进度记录）', '安全确认', {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        if (isMockMode.value) {
          let list = JSON.parse(localStorage.getItem('fs_tasks') || '[]')
          list = list.filter(t => t.id !== id)
          localStorage.setItem('fs_tasks', JSON.stringify(list))
          ElMessage.success('本地模拟删除任务成功')
          loadAllData()
        } else {
          try {
            const res = await taskApi.deleteTask(id)
            if (res.success) {
              ElMessage.success(res.message || '任务删除成功')
              loadAllData()
            }
          } catch (e) { console.error(e) }
        }
      }).catch(() => {})
    }

    // --- D. 单任务 AI 建议 ---
    const aiDrawerVisible = ref(false)
    const aiSuggestionLoading = ref(false)
    const aiSuggestion = ref('')

    const handleGetAiSuggestion = async (row) => {
      aiDrawerVisible.value = true
      aiSuggestionLoading.value = true
      aiSuggestion.value = ''
      
      try {
        if (isMockMode.value) {
          await new Promise(resolve => setTimeout(resolve, 1500))
          aiSuggestion.value = `💡针对任务「${row.title}」，千问 AI 给出的协同建议：
1. 建议拆分：
   - 【第一天】搭建基础路由与结构，配置 webpack 代理规避跨域。
   - 【第二天】开发表单元素与校验器，编写确认密码校验规则。
   - 【第三天】对接登录注册 API，并在 router 全局守卫拦截非法访问。
2. 执行顺序：基础框架 -> 表单交互设计 -> API 拦截联调。
3. 风险防范：防范 axios 请求中 currentUserId 参数漏传，请务必检验 axios 拦截器的可靠性。`
        } else {
          const res = await aiApi.getTaskSuggestion(row.projectName, row.title, row.description)
          if (res.success && res.data) {
            aiSuggestion.value = res.data.suggestion || '大模型暂时未能给出合理建议，请稍后再试。'
          }
        }
      } catch (e) {
        console.error(e)
        aiSuggestion.value = 'AI 建议获取失败。请检查后台阿里云千问的 DashScope API Key 配置是否正确。'
      } finally {
        aiSuggestionLoading.value = false
      }
    }

    // --- E. 进度跟踪反馈 ---
    const logDialogVisible = ref(false)
    const logFormRef = ref(null)
    const logForm = ref({ task_id: null, progress_percent: 0, content: '', operator_id: null })
    const logFormRules = {
      task_id: [{ required: true, message: '请选择反馈的任务', trigger: 'change' }],
      content: [{ required: true, message: '请输入进展说明内容', trigger: 'blur' }]
    }

    const openAddLogDialog = (taskId) => {
      logForm.value = {
        task_id: taskId || null,
        progress_percent: taskId ? (tasks.value.find(t => t.id === taskId)?.progress_percent || 50) : 0,
        content: '',
        operator_id: currentUser.value.id
      }
      logDialogVisible.value = true
    }

    const handleSaveLog = () => {
      logFormRef.value.validate(async (valid) => {
        if (!valid) return
        
        if (isMockMode.value) {
          let logList = JSON.parse(localStorage.getItem('fs_logs') || '[]')
          const newId = logList.length > 0 ? Math.max(...logList.map(l => l.id)) + 1 : 1
          
          const now = new Date()
          const createTime = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')} ${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}:${String(now.getSeconds()).padStart(2, '0')}`
          
          logList.push({
            id: newId,
            task_id: logForm.value.task_id,
            progress_percent: logForm.value.progress_percent,
            content: logForm.value.content,
            operator_id: currentUser.value.id,
            create_time: createTime
          })
          localStorage.setItem('fs_logs', JSON.stringify(logList))
          
          // 顺便修改关联任务的进度百分比和状态
          let taskList = JSON.parse(localStorage.getItem('fs_tasks') || '[]')
          const tIndex = taskList.findIndex(t => t.id === logForm.value.task_id)
          if (tIndex !== -1) {
            taskList[tIndex].progress_percent = logForm.value.progress_percent
            if (logForm.value.progress_percent === 100) {
              taskList[tIndex].status = '已完成'
            } else if (logForm.value.progress_percent > 0) {
              taskList[tIndex].status = '进行中'
            }
          }
          localStorage.setItem('fs_tasks', JSON.stringify(taskList))

          ElMessage.success('本地模拟进度汇报成功')
          logDialogVisible.value = false
          loadAllData()
        } else {
          try {
            const res = await taskLogApi.addTaskLog(logForm.value)
            if (res.success) {
              ElMessage.success('汇报提交成功')
              logDialogVisible.value = false
              loadAllData()
            }
          } catch (e) { console.error(e) }
        }
      })
    }

    // --- F. 总结中心管理 ---
    const summaryDialogVisible = ref(false)
    const summaryFormRef = ref(null)
    const summaryForm = ref({ project_id: '', task_id: null, summary_type: '阶段总结', content: '', created_by: null })
    const summaryFormRules = {
      project_id: [{ required: true, message: '请选择总结项目', trigger: 'change' }],
      summary_type: [{ required: true, message: '请选择总结类型', trigger: 'change' }],
      content: [{ required: true, message: '请输入总结的说明内容', trigger: 'blur' }]
    }

    const handleSummaryProjectChange = (val) => {
      summaryForm.value.task_id = null
      if (val) {
        currentProjectTasks.value = tasks.value.filter(t => t.project_id === val)
      } else {
        currentProjectTasks.value = []
      }
    }

    const openAddSummaryDialog = () => {
      summaryForm.value = {
        project_id: '',
        task_id: null,
        summary_type: '阶段总结',
        content: '',
        created_by: currentUser.value.id
      }
      currentProjectTasks.value = []
      summaryDialogVisible.value = true
    }

    const handleSaveSummary = () => {
      summaryFormRef.value.validate(async (valid) => {
        if (!valid) return
        
        if (isMockMode.value) {
          let sumList = JSON.parse(localStorage.getItem('fs_summaries') || '[]')
          const newId = sumList.length > 0 ? Math.max(...sumList.map(s => s.id)) + 1 : 1
          
          const now = new Date()
          const createTime = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')} ${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}:${String(now.getSeconds()).padStart(2, '0')}`

          sumList.push({
            id: newId,
            project_id: summaryForm.value.project_id,
            task_id: summaryForm.value.task_id,
            summary_type: summaryForm.value.summary_type,
            content: summaryForm.value.content,
            created_by: currentUser.value.id,
            create_time: createTime
          })
          localStorage.setItem('fs_summaries', JSON.stringify(sumList))
          
          ElMessage.success('本地模拟提交总结成功')
          summaryDialogVisible.value = false
          loadAllData()
        } else {
          try {
            const res = await summaryApi.addSummary(summaryForm.value)
            if (res.success) {
              ElMessage.success('提交项目总结成功')
              summaryDialogVisible.value = false
              loadAllData()
            }
          } catch (e) { console.error(e) }
        }
      })
    }

    // --- G. 修改密码与个人中心 ---
    const passwordFormRef = ref(null)
    const passwordForm = ref({ oldPassword: '', newPassword: '', confirmNewPassword: '' })
    
    const validateConfirmNewPass = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('请再次确认新密码'))
      } else if (value !== passwordForm.value.newPassword) {
        callback(new Error('新密码与确认新密码不一致，请核对'))
      } else {
        callback()
      }
    }

    const passwordRules = {
      oldPassword: [{ required: true, message: '请填写旧密码', trigger: 'blur' }],
      newPassword: [
        { required: true, message: '请设置新密码', trigger: 'blur' },
        { min: 6, message: '新密码长度必须大于等于 6 位', trigger: 'blur' }
      ],
      confirmNewPassword: [{ required: true, validator: validateConfirmNewPass, trigger: 'blur' }]
    }

    const handleChangePassword = () => {
      passwordFormRef.value.validate(async (valid) => {
        if (!valid) return
        
        if (isMockMode.value) {
          ElMessage.success('本地密码修改校验成功（明文模拟）')
          passwordForm.value = { oldPassword: '', newPassword: '', confirmNewPassword: '' }
        } else {
          try {
            const res = await authApi.updatePassword(passwordForm.value.oldPassword, passwordForm.value.newPassword)
            if (res.success) {
              ElMessage.success('更新密码成功！请牢记您的新密码。')
              passwordForm.value = { oldPassword: '', newPassword: '', confirmNewPassword: '' }
            }
          } catch (e) { console.error(e) }
        }
      })
    }

    // ==================== 4. 其他辅助操作（左侧菜单、高亮标签等） ====================
    const handleMenuSelect = (index) => {
      activeMenu.value = index
    }

    const handleLogout = () => {
      ElMessageBox.confirm('确定退出当前账号登录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }).then(() => {
        sessionStorage.clear()
        router.push('/login')
        ElMessage.info('已安全登出系统')
      }).catch(() => {})
    }

    const getStatusTag = (status) => {
      if (status === '已完成') return 'success'
      if (status === '进行中') return 'primary'
      return 'info'
    }

    const getPriorityTag = (priority) => {
      if (priority === '高') return 'danger'
      if (priority === '中') return 'warning'
      return 'info'
    }

    onMounted(() => {
      // 从 sessionStorage 加载当前用户
      const userStr = sessionStorage.getItem('user')
      if (userStr) {
        currentUser.value = JSON.parse(userStr)
      } else {
        router.push('/login')
      }
      
      // 全局加载数据
      loadAllData()
    })

    return {
      currentUser,
      activeMenu,
      loading,
      isMockMode,
      isLeader,
      menuTitles,
      currentDateStr,
      stats,
      projects,
      tasks,
      logs,
      summaries,
      users,
      myOngoingTasks,
      searchProjectQuery,
      filteredProjects,
      taskFilterProjectId,
      taskFilterStatus,
      filteredTasks,
      logFilterTaskId,
      filteredLogs,
      
      // 样式方法
      getStatusTag,
      getPriorityTag,
      handleMenuSelect,
      handleLogout,

      // 项目管理弹窗与事件
      projectDialogVisible,
      projectDialogTitle,
      projectFormRef,
      projectForm,
      projectFormRules,
      openAddProjectDialog,
      openEditProjectDialog,
      handleSaveProject,
      handleDeleteProject,

      // AI 任务拆解
      aiForm,
      aiFormRules,
      aiFormRef,
      aiGenerating,
      aiPlanResult,
      selectedAiItems,
      handleGenerateAiPlan,
      handleAiSelectionChange,
      handleImportAiTasks,

      // 任务管理
      taskDialogVisible,
      taskDialogTitle,
      taskFormRef,
      taskForm,
      taskFormRules,
      currentProjectTasks,
      handleTaskProjectChange,
      openAddTaskDialog,
      openEditTaskDialog,
      handleSaveTask,
      handleDeleteTask,

      // AI 建议
      aiDrawerVisible,
      aiSuggestionLoading,
      aiSuggestion,
      handleGetAiSuggestion,

      // 进度反馈
      logDialogVisible,
      logFormRef,
      logForm,
      logFormRules,
      openAddLogDialog,
      handleSaveLog,

      // 项目总结
      summaryDialogVisible,
      summaryFormRef,
      summaryForm,
      summaryFormRules,
      handleSummaryProjectChange,
      openAddSummaryDialog,
      handleSaveSummary,

      // 修改密码
      passwordFormRef,
      passwordForm,
      passwordRules,
      handleChangePassword
    }
  }
}
</script>

<style scoped>
.home-container {
  display: flex;
  height: 100vh;
  width: 100vw;
  overflow: hidden;
  background-color: var(--bg-page);
}

/* Sidebar Styling */
.sidebar {
  width: 260px;
  background-color: var(--bg-card);
  border-right: 1px solid var(--border-color);
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
}

.sidebar-brand {
  height: 64px;
  padding: 0 20px;
  display: flex;
  align-items: center;
  gap: 12px;
  border-bottom: 1px solid var(--border-color);
}

.sidebar-brand .logo {
  width: 32px;
  height: 32px;
  background-color: var(--google-blue);
  color: white;
  font-family: 'Outfit', sans-serif;
  font-weight: 700;
  font-size: 15px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
}

.sidebar-brand .brand-text {
  display: flex;
  flex-direction: column;
}

.sidebar-brand .brand-text .title {
  font-family: 'Outfit', sans-serif;
  font-weight: 600;
  font-size: 16px;
  color: var(--text-primary);
  line-height: 1.2;
}

.sidebar-brand .brand-text .subtitle {
  font-size: 10px;
  color: var(--text-secondary);
}

.user-profile-summary {
  padding: 16px 20px;
  display: flex;
  align-items: center;
  gap: 12px;
  border-bottom: 1px solid #f1f3f4;
}

.user-profile-summary .avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background-color: var(--google-blue-light);
  color: var(--google-blue);
  font-weight: bold;
  font-size: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid rgba(26, 115, 232, 0.2);
}

.user-profile-summary .info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.user-profile-summary .info .name {
  font-weight: 600;
  font-size: 14px;
  color: var(--text-primary);
}

.role-tag {
  font-size: 11px;
  padding: 1px 6px;
  border-radius: 10px;
  display: inline-block;
  font-weight: 500;
  width: fit-content;
}

.role-leader {
  background-color: var(--google-red-light);
  color: var(--google-red);
  border: 1px solid rgba(217, 48, 37, 0.15);
}

.role-member {
  background-color: var(--google-green-light);
  color: var(--google-green);
  border: 1px solid rgba(30, 142, 62, 0.15);
}

.sidebar-menu {
  flex: 1;
  overflow-y: auto;
  padding: 12px 10px 12px 0;
}

.menu-group-title {
  padding: 12px 24px 6px;
  font-size: 11px;
  text-transform: uppercase;
  letter-spacing: 0.8px;
  color: var(--text-disabled);
  font-weight: 600;
}

.sidebar-footer {
  padding: 15px 20px;
  border-top: 1px solid var(--border-color);
}

.btn-logout {
  width: 100%;
  height: 38px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

/* Main Content Styling */
.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.content-header {
  height: 64px;
  border-bottom: 1px solid var(--border-color);
  background-color: var(--bg-card);
  padding: 0 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-shrink: 0;
}

.page-title {
  font-size: 18px;
  font-weight: 500;
  color: var(--text-primary);
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 15px;
}

.current-date {
  color: var(--text-secondary);
  font-size: 13px;
}

.mock-tag {
  font-weight: 600;
  border: none;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0% { opacity: 0.85; }
  50% { opacity: 1; }
  100% { opacity: 0.85; }
}

.content-body {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
}

/* Overview Panels */
.stat-card {
  display: flex;
  align-items: center;
  padding: 10px 5px;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  margin-right: 15px;
}

.icon-blue { background-color: var(--google-blue-light); color: var(--google-blue); }
.icon-green { background-color: var(--google-green-light); color: var(--google-green); }
.icon-yellow { background-color: var(--google-yellow-light); color: var(--google-yellow); }
.icon-red { background-color: var(--google-red-light); color: var(--google-red); }

.stat-data .num {
  font-size: 26px;
  font-family: 'Outfit', sans-serif;
  font-weight: 600;
  color: var(--text-primary);
  line-height: 1.1;
}

.stat-data .label {
  color: var(--text-secondary);
  font-size: 12px;
  margin-top: 4px;
}

.overview-details {
  margin-top: 24px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-weight: 600;
  color: var(--text-primary);
}

.quick-links {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.quick-btn {
  width: 100%;
  height: 40px;
  margin-left: 0 !important;
  justify-content: flex-start;
  padding-left: 20px;
  font-weight: 500;
}

/* Actions in tables */
.table-actions {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
}

/* AI panel custom styles */
.ai-input-card {
  height: 100%;
}

.ai-result-section {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.ai-summary-box {
  background-color: var(--google-blue-light);
  border: 1px solid rgba(26, 115, 232, 0.15);
  border-radius: 8px;
  padding: 16px 20px;
}

.ai-summary-box h4 {
  color: var(--google-blue);
  font-size: 15px;
  margin-bottom: 6px;
  font-weight: 600;
}

.ai-summary-box p {
  color: var(--text-primary);
  font-size: 13px;
  line-height: 1.6;
}

.ai-items-box {
  background-color: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 8px;
  padding: 20px;
}

.items-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.items-header h4 {
  font-size: 15px;
  color: var(--text-primary);
  font-weight: 600;
}

.ai-empty-box {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: var(--bg-card);
  border: 1px dashed var(--border-color);
  border-radius: 8px;
  min-height: 400px;
}

/* Drawer styles */
.ai-drawer-content {
  padding: 10px 5px;
  display: flex;
  flex-direction: column;
  height: 100%;
}

.ai-badge {
  background-color: var(--google-yellow-light);
  color: var(--google-yellow-hover);
  border: 1px solid rgba(249, 171, 0, 0.2);
  padding: 6px 12px;
  border-radius: 4px;
  font-weight: bold;
  font-size: 13px;
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 20px;
}

.suggestion-text {
  font-size: 14px;
  color: var(--text-primary);
  line-height: 1.7;
  white-space: pre-line;
  background-color: #fafafa;
  padding: 15px;
  border-radius: 6px;
  border: 1px solid #f1f3f4;
}

.ai-disclaimer {
  margin-top: auto;
  font-size: 11px;
  color: var(--text-disabled);
  text-align: center;
  padding-top: 20px;
}
</style>
