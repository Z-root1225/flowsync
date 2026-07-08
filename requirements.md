FlowSync 小组任务协同管理系统
需求规格说明书
版本：V1.0

1. 引言
1.1 项目背景
FlowSync 是一个面向教学场景的小组任务协同管理系统。系统将「项目创建、任务分配、进度记录、总结输出」全流程串联，并接入阿里云千问大模型辅助任务拆分。项目核心理念是「流程完整、结构清楚、功能够用、学生能讲清楚」，适合作为高校 Web 工程与信息系统课程的教学实践项目。
1.2 项目目标
提供完整的项目-任务-进度-总结管理闭环，覆盖小组协同全流程。
接入 AI 大模型，辅助项目负责人自动拆解任务并推荐负责人。
通过角色区分实现权限控制，项目负责人全权管理，成员负责执行与汇报。
技术栈清晰、代码结构简洁，适合教学演示与学生扩展。
1.3 读者对象
本文档面向项目开发人员、课程指导教师及参与扩展开发的学生。

2. 技术栈
2.1 技术选型


| 层次 | 技术 | 版本 | 端口 |
|---|---|---|---|
| 前端 | Vue3 + Vue Router 4 | Vue 3.x | 8081 |
| 前端 UI | Element Plus | — | — |
| HTTP 客户端 | Axios | — | — |
| 后端框架 | Spring Boot | 3.3.5 | 8080 |
| ORM 框架 | MyBatis-Plus | 3.5.8 | — |
| 数据库 | MySQL | 8.x | 3306 |
| AI 模型 | 阿里云千问（DashScope SDK） | qwen-plus | — |
| API 文档 | SpringDoc OpenAPI | 2.1.0 | — |
| 构建工具 | Maven（后端）/ Vue CLI（前端） | — | — |



2.2 前后端通信机制
前端运行在 8081 端口，通过 Vue CLI 的 devServer proxy 将所有 /api 请求代理到后端 8080 端口，避免跨域问题。所有 API 响应统一使用 ApiResponse 封装，格式为：


| 字段 | 类型 | 说明 |
|---|---|---|
| success | boolean | 操作是否成功 |
| message | String | 提示信息 |
| data | T | 业务数据 |



2.3 认证机制
系统采用简化认证方案，不使用 JWT 或 Session。用户登录成功后，后端返回用户完整信息（含角色）和一个简单 token 字符串。前端将用户信息存储在 sessionStorage 中，通过 Axios 请求拦截器自动将 currentUserId 参数附加到所有请求上。该设计为教学简化方案，预留了后端权限校验的扩展空间。

3. 数据库设计
3.1 数据库概述
数据库名为 flowsync_simple，字符集 utf8mb4，共包含 5 张业务表。表之间通过外键关联，形成「用户-项目-任务-进度-总结」的完整数据链路。

3.2 ER 关系概述


| 关系 | 说明 |
|---|---|
| sys_user 1:N project_info | 一个用户可以作为多个项目的负责人（owner_id 关联） |
| sys_user 1:N task_info | 一个用户可以负责多个任务（assignee_id）和创建多个任务（creator_id） |
| project_info 1:N task_info | 一个项目包含多个任务（project_id 关联） |
| task_info 1:N task_info | 任务支持父子关系（parent_id 自关联），实现任务层级 |
| task_info 1:N task_log | 一个任务可以有多条进度记录（task_id 关联） |
| project_info 1:N task_summary | 一个项目可以有多条总结（project_id 关联） |
| task_info 1:N task_summary | 一个任务可以关联多条总结（task_id 可选关联） |



3.3 表结构详细设计
3.3.1 sys_user（用户表）
存储系统所有用户信息，包括用户名、密码、真实姓名和角色。角色字段决定用户在系统中的权限级别。


| 字段名 | 类型 | 约束 | 说明 |
|---|---|---|---|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 主键 |
| username | VARCHAR(50) | NOT NULL, UNIQUE | 用户名，登录账号 |
| password | VARCHAR(100) | NOT NULL | 密码（明文存储，教学简化） |
| real_name | VARCHAR(50) | NOT NULL | 真实姓名 |
| role | VARCHAR(30) | NOT NULL | 角色（管理员/负责人/成员） |
| phone | VARCHAR(20) | 可空 | 联系电话 |
| email | VARCHAR(100) | 可空 | 电子邮箱 |
| create_time | DATETIME | DEFAULT CURRENT_TIMESTAMP | 创建时间 |



预置测试数据：


| 用户名 | 密码 | 真实姓名 | 角色 |
|---|---|---|---|
| leader | 123456 | 项目负责人 | 负责人 |
| member1 | 123456 | 张三 | 成员 |
| member2 | 123456 | 李四 | 成员 |



3.3.2 project_info（项目表）
存储项目基本信息，每个项目有一个负责人（owner_id），包含项目状态、优先级和时间范围。


| 字段名 | 类型 | 约束 | 说明 |
|---|---|---|---|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 主键 |
| name | VARCHAR(100) | NOT NULL | 项目名称 |
| description | VARCHAR(500) | 可空 | 项目说明 |
| status | VARCHAR(20) | NOT NULL | 项目状态（未开始/进行中/已完成） |
| priority | VARCHAR(20) | NOT NULL | 优先级（低/中/高） |
| owner_id | BIGINT | FOREIGN KEY -> sys_user(id) | 项目负责人 ID |
| start_date | DATE | 可空 | 开始日期 |
| end_date | DATE | 可空 | 结束日期 |
| create_time | DATETIME | DEFAULT CURRENT_TIMESTAMP | 创建时间 |



3.3.3 task_info（任务表）
存储任务信息，支持父子任务层级（parent_id 自关联）。每个任务有所属项目、负责人、创建人、状态、优先级、截止日期和 AI 建议字段。


| 字段名 | 类型 | 约束 | 说明 |
|---|---|---|---|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 主键 |
| project_id | BIGINT | NOT NULL, FOREIGN KEY -> project_info(id) | 所属项目 ID |
| parent_id | BIGINT | 可空，自关联 | 父任务 ID（支持任务层级） |
| title | VARCHAR(100) | NOT NULL | 任务标题 |
| description | VARCHAR(1000) | 可空 | 任务说明 |
| assignee_id | BIGINT | FOREIGN KEY -> sys_user(id) | 任务负责人 ID |
| creator_id | BIGINT | FOREIGN KEY -> sys_user(id) | 任务创建人 ID |
| status | VARCHAR(20) | NOT NULL | 任务状态（未开始/进行中/已完成） |
| priority | VARCHAR(20) | NOT NULL | 优先级（低/中/高） |
| due_date | DATE | 可空 | 截止日期 |
| ai_suggestion | TEXT | 可空 | 千问 AI 建议内容 |
| create_time | DATETIME | DEFAULT CURRENT_TIMESTAMP | 创建时间 |



3.3.4 task_log（任务进度记录表）
记录任务执行过程中的进度更新，包含进度百分比和文字说明。每个任务可以有多条进度记录，形成时间线。


| 字段名 | 类型 | 约束 | 说明 |
|---|---|---|---|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 主键 |
| task_id | BIGINT | NOT NULL, FOREIGN KEY -> task_info(id) | 关联任务 ID |
| progress_percent | INT | NOT NULL | 进度百分比（0-100） |
| content | VARCHAR(1000) | NOT NULL | 进度说明文字 |
| operator_id | BIGINT | FOREIGN KEY -> sys_user(id) | 记录人 ID |
| create_time | DATETIME | DEFAULT CURRENT_TIMESTAMP | 创建时间 |



3.3.5 task_summary（总结表）
存储项目阶段总结和最终总结，可关联到具体任务，也可仅关联项目。


| 字段名 | 类型 | 约束 | 说明 |
|---|---|---|---|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 主键 |
| project_id | BIGINT | NOT NULL, FOREIGN KEY -> project_info(id) | 所属项目 ID |
| task_id | BIGINT | 可空, FOREIGN KEY -> task_info(id) | 关联任务 ID（可选） |
| summary_type | VARCHAR(30) | NOT NULL | 总结类型（阶段总结/最终总结） |
| content | VARCHAR(2000) | NOT NULL | 总结内容 |
| created_by | BIGINT | FOREIGN KEY -> sys_user(id) | 创建人 ID |
| create_time | DATETIME | DEFAULT CURRENT_TIMESTAMP | 创建时间 |



4. 系统整体流程
4.1 核心业务流程
系统的核心业务流程按以下顺序展开：


| 步骤 | 操作 | 执行角色 | 说明 |
|---|---|---|---|
| 1 | 创建项目 | 项目负责人 | 填写项目名称、说明、状态、优先级、时间范围，系统自动设置负责人为当前用户 |
| 2 | AI 任务拆解 | 项目负责人 | 选择项目并输入任务目标，系统调用千问大模型自动拆解任务并推荐负责人 |
| 3 | 导入任务 | 项目负责人 | 从 AI 拆解结果中勾选需要导入的任务，可调整负责人后批量导入任务表 |
| 4 | 手动创建/编辑任务 | 项目负责人 | 对未通过 AI 拆解的任务，手动创建并分配负责人、设置优先级和截止日期 |
| 5 | 更新任务状态 | 项目负责人/成员 | 负责人可修改任务全部字段；成员只能更新自己负责任务的状态 |
| 6 | 记录进度 | 所有用户 | 为任务添加进度记录，填写进度百分比和说明文字 |
| 7 | 撰写总结 | 所有用户 | 创建阶段总结或最终总结，关联项目和任务 |
| 8 | 查看总览 | 所有用户 | 在总览页面查看用户数、项目数、任务数、总结数等统计信息 |



4.2 前端页面结构
前端为单页面应用（SPA），所有功能集中在 HomeView.vue 中，通过左侧菜单切换面板。页面结构如下：


| 菜单分组 | 菜单项 | 功能说明 |
|---|---|---|
| 工作台 | 总览 | 展示系统统计信息（用户数、项目数、任务数、总结数） |
| 业务管理 | 项目管理 | 项目列表展示，负责人可新建/编辑/删除项目 |
| 业务管理 | 任务拆解 | AI 任务拆解功能，仅项目负责人可见此菜单 |
| 业务管理 | 任务管理 | 任务列表展示，负责人可新建/编辑/删除，成员可更新自己任务状态 |
| 业务管理 | 进度跟踪 | 进度记录列表，所有用户可新增记录 |
| 业务管理 | 总结中心 | 总结列表展示，所有用户可新增总结 |
| 系统信息 | 成员列表 | 查看系统中全部成员信息 |
| 系统信息 | 个人信息 | 查看个人资料并修改登录密码 |



4.3 后端架构分层
后端采用经典三层架构，包路径为 hgc.flowsyncapi：


| 层次 | 包路径 | 职责 | 包含的类 |
|---|---|---|---|
| Controller 层 | controller | 接收 HTTP 请求，调用 Service，返回 ApiResponse | AuthController, ProjectController, TaskController, TaskLogController, TaskSummaryController, OverviewController, UserController, AiController |
| Service 层 | service | 业务逻辑处理，数据组装 | AuthService, ProjectInfoService, TaskInfoService, TaskLogService, TaskSummaryService, OverviewService, UserService, QwenService |
| Mapper 层 | mapper | MyBatis-Plus 数据访问，对应 5 张表 | UserMapper, ProjectInfoMapper, TaskInfoMapper, TaskLogMapper, TaskSummaryMapper |
| Entity 层 | entity | 数据库实体映射 | User, ProjectInfo, TaskInfo, TaskLog, TaskSummary |
| DTO 层 | dto | 数据传输对象 | LoginRequest, PasswordUpdateRequest, AiTaskSuggestionRequest, AiTaskPlanRequest, AiTaskPlanResponse, AiTaskPlanItem, AiTaskPlanImportRequest |
| Common 层 | common | 通用工具类 | ApiResponse（统一响应封装） |



5. 权限设计
5.1 角色定义
系统包含三种角色，角色值存储在 sys_user 表的 role 字段中：


| 角色值 | 角色名称 | 说明 |
|---|---|---|
| 负责人 | 项目负责人 | 拥有最高业务权限，可创建/编辑/删除项目和任务，可使用 AI 拆解 |
| 成员 | 普通成员 | 可查看所有数据，可更新自己负责任务的状态，可新增进度记录和总结 |



5.2 权限控制策略
系统采用前端控制策略，后端接口不做权限校验，全部返回全量数据。前端通过判断 currentUser.role 控制菜单和按钮的显示与隐藏，实现权限隔离。
前端核心判断逻辑：
    isLeader = (role === '负责人')
isLeader 为 true 时显示全部菜单和操作按钮；为 false 时（即成员）按规则隐藏对应元素。该设计为学生留下了后端权限校验的扩展空间。

5.3 权限矩阵


| 功能模块 | 操作 | 负责人/管理员 | 成员 |
|---|---|---|---|
| 总览 | 查看统计信息 | 可以 | 可以 |
| 项目管理 | 查看项目列表 | 可以 | 可以 |
| 项目管理 | 新建项目 | 可以 | 不可以（按钮隐藏） |
| 项目管理 | 编辑项目 | 可以 | 不可以（按钮隐藏） |
| 项目管理 | 删除项目 | 可以 | 不可以（按钮隐藏） |
| 任务拆解 | 查看菜单 | 可以 | 不可以（菜单隐藏） |
| 任务拆解 | 生成 AI 拆解 | 可以 | 不可以 |
| 任务拆解 | 导入选中任务 | 可以 | 不可以 |
| 任务管理 | 查看任务列表 | 可以 | 可以 |
| 任务管理 | 新建任务 | 可以 | 不可以（按钮隐藏） |
| 任务管理 | 编辑任务（全部字段） | 可以 | 不可以 |
| 任务管理 | 更新自己任务的状态 | 可以 | 可以（仅自己负责的任务显示按钮） |
| 任务管理 | 删除任务 | 可以 | 不可以（按钮隐藏） |
| 进度跟踪 | 查看进度记录 | 可以 | 可以 |
| 进度跟踪 | 新增进度记录 | 可以 | 可以 |
| 总结中心 | 查看总结 | 可以 | 可以 |
| 总结中心 | 新增总结 | 可以 | 可以 |
| 成员列表 | 查看成员信息 | 可以 | 可以 |
| 个人信息 | 修改密码 | 可以 | 可以 |



5.4 成员任务编辑限制
成员编辑任务时，任务弹窗中除「状态」字段外，其余字段（所属项目、任务标题、任务说明、负责人、优先级、截止日期）均设为禁用状态（:disabled），确保成员只能修改任务状态。弹窗标题动态显示为「更新任务状态」。

6. AI 能力设计
6.1 AI 能力概述
系统接入阿里云千问大模型（qwen-plus），通过 DashScope SDK 实现两种 AI 能力。核心原则是「AI 只给建议，不替人做决定」——拆解结果由负责人确认后手动导入，负责人分配仍可调整。

6.2 单任务建议


| 项目 | 说明 |
|---|---|
| 接口 | POST /api/ai/task-suggestion |
| 功能 | 针对单个任务，给出子任务拆分建议、执行顺序和风险提醒 |
| 输入 | 项目名称、任务标题、任务说明 |
| 输出 | 纯文本建议（300 字以内） |
| System Prompt | 你是一个简单直接的项目任务助手。请用最容易理解的中文输出，给出：1. 建议拆分的子任务；2. 执行顺序；3. 风险提醒。控制在300字以内。 |
| 返回格式 | { "suggestion": "建议文本" } |



6.3 AI 任务拆解（核心能力）
6.3.1 功能说明
项目负责人选择项目并输入任务目标和补充说明后，系统调用千问大模型自动拆解任务，为每个任务推荐负责人，并生成可直接导入的任务草稿。
6.3.2 接口设计


| 项目 | 说明 |
|---|---|
| 接口 | POST /api/ai/task-plan |
| 输入字段 | projectId（项目ID）、operatorId（操作人ID）、projectName（项目名称）、goal（任务目标）、description（补充说明） |
| 输出字段 | summary（总体概述）、items（任务列表，每个任务包含 title、description、priority、suggestedDays、assigneeId） |
| 导入接口 | POST /api/ai/task-plan/import，传入 projectId、creatorId、items 列表 |


6.3.3 处理流程


| 步骤 | 操作 | 说明 |
|---|---|---|
| 1 | 查询成员名单 | AiController 调用 UserService.listUsers() 获取全部成员（id + 姓名） |
| 2 | 构建 Prompt | QwenService 将项目名称、任务目标、补充说明和成员名单拼接为 user prompt |
| 3 | 调用千问 | 通过 DashScope SDK 调用 qwen-plus 模型，要求返回严格 JSON 格式 |
| 4 | 解析 JSON | 解析千问返回的 JSON，提取 summary 和 items 列表 |
| 5 | 校验负责人 ID | 遍历每个任务，检查 assigneeId 是否在成员名单中。不在名单中的 ID 兜底替换为名单第一个人，确保每个任务都有负责人 |
| 6 | 返回前端 | 返回 AiTaskPlanResponse，包含 summary 和 items |
| 7 | 前端展示 | 拆解结果表格展示，负责人列自动选中 AI 推荐值，用户可调整 |
| 8 | 导入任务 | 用户勾选任务并点击导入，前端校验每个选中任务都有负责人后，调用导入接口写入 task_info 表 |


6.3.4 System Prompt 设计
你是一个项目任务拆解助手。请把大任务拆成可以直接执行的小任务。我会给你可选的成员名单，请为每个任务推荐一个最合适的负责人，在 assigneeId 字段填写该成员的 id（必须是名单中已有的 id）。重要：每个任务都必须填写 assigneeId，不能为空；同一个人可以负责多个任务。只返回严格 JSON，不要解释，不要 markdown。
6.3.5 User Prompt 模板
项目名称：{projectName}
任务目标：{goal}
补充说明：{description}
可选成员名单（id - 姓名）：
1 - 系统管理员
2 - 项目负责人
3 - 张三
4 - 李四
6.3.6 JSON 返回格式
{"summary": "...", "items": [{"title": "...", "description": "...", "priority": "高", "suggestedDays": 3, "assigneeId": 1}]}
6.3.7 兜底机制
当千问调用失败或 JSON 解析失败时，系统自动调用 buildFallbackPlan 方法生成兜底方案，包含三个标准任务：准备资料、执行主体、检查总结。兜底方案不设置 assigneeId，由用户在导入前手动选择。
6.3.8 负责人推荐规则
AI 为每个任务独立推荐负责人，同一人可以负责多个任务。
任务数与负责人数不需要一一对应。
每个任务都必须有负责人，不能为空。
后端校验 AI 返回的 assigneeId 有效性，不在名单中的 ID 兜底为名单第一个人。
前端导入前再次校验，若有任务未选择负责人则提示用户。
用户可在导入前随意调整每个任务的负责人。

7. API 接口设计
7.1 接口列表


| 模块 | 接口 | 方法 | 说明 |
|---|---|---|---|
| 认证 | /api/auth/login | POST | 用户登录 |
| 项目 | /api/projects | GET | 获取项目列表 |
| 项目 | /api/projects | POST | 创建/编辑项目 |
| 项目 | /api/projects/{id} | DELETE | 删除项目 |
| 任务 | /api/tasks | GET | 获取任务列表（可按 projectId 筛选） |
| 任务 | /api/tasks | POST | 创建/编辑任务 |
| 任务 | /api/tasks/{id} | DELETE | 删除任务 |
| 进度 | /api/task-logs | GET | 获取进度记录列表（可按 taskId 筛选） |
| 进度 | /api/task-logs | POST | 新增进度记录 |
| 总结 | /api/summaries | GET | 获取总结列表 |
| 总结 | /api/summaries | POST | 新增总结 |
| 总览 | /api/overview | GET | 获取统计信息 |
| 用户 | /api/users | GET | 获取全部用户列表 |
| 用户 | /api/users/update-password | POST | 修改密码 |
| AI | /api/ai/task-suggestion | POST | 单任务 AI 建议 |
| AI | /api/ai/task-plan | POST | AI 任务拆解 |
| AI | /api/ai/task-plan/import | POST | 导入 AI 拆解任务 |



7.2 公共参数说明
除登录和 AI 接口外，所有需要身份识别的接口通过 currentUserId 查询参数传递当前用户 ID。前端通过 Axios 请求拦截器自动从 sessionStorage 读取当前用户并附加此参数。

7.3 统一响应格式
所有接口返回 ApiResponse 封装结构：


| 场景 | success | message | data |
|---|---|---|---|
| 操作成功 | true | 操作成功 / 具体提示 | 业务数据 |
| 操作失败 | false | 错误原因描述 | null |



8. 功能模块详细说明
8.1 用户认证模块
登录：用户输入用户名和密码，后端通过 MyBatis-Plus 查询 sys_user 表，明文比对密码。
登录成功后返回用户完整信息（密码置空）和简单 token。
前端将用户信息存入 sessionStorage，刷新不丢失。
修改密码：验证旧密码后更新为新密码。
8.2 项目管理模块
创建项目：填写名称、说明、状态、优先级、负责人、时间范围。系统自动将 owner_id 设为当前用户。
编辑项目：可修改全部字段，包括更换负责人。
删除项目：直接删除，不级联清理任务数据（教学简化）。
项目列表：按 id 倒序展示全部项目。
8.3 任务管理模块
创建任务：选择所属项目，填写标题、说明、负责人、状态、优先级、截止日期。系统自动记录创建人。
编辑任务：负责人可修改全部字段；成员只能修改自己负责任务的状态字段。
删除任务：直接删除，不级联清理进度记录。
任务列表：按 id 倒序展示，可按 projectId 筛选。
任务支持父子层级关系（parent_id 字段）。
8.4 进度跟踪模块
新增进度记录：选择任务，填写进度百分比和说明文字。系统自动记录操作人和时间。
进度记录列表：按 id 倒序展示，可按 taskId 筛选。
所有用户均可新增进度记录。
8.5 总结模块
新增总结：选择项目和任务（可选），填写总结类型（阶段总结/最终总结）和内容。
总结列表：按 id 倒序展示全部总结。
所有用户均可新增总结。
8.6 总览统计模块
展示全局统计信息：用户数、项目数、任务数、总结数。
所有登录用户可见相同的统计数据。
8.7 成员管理模块
成员列表：展示全部用户信息（不含密码）。
所有登录用户均可查看。
8.8 个人信息模块
查看个人资料：展示当前登录用户信息。
修改密码：验证旧密码后更新。

9. 扩展空间
本项目为教学简化版，以下方面预留了扩展空间，适合学生作为课程扩展实践：


| 序号 | 扩展方向 | 当前状态 | 扩展建议 |
|---|---|---|---|
| 1 | 后端权限校验 | 后端不做权限校验，全部前端控制 | 在后端 Service 层加入 isProjectOwner 和 listVisibleProjectIds 校验 |
| 2 | 密码加密 | 明文存储和比对 | 引入 BCrypt 加密，登录时加密比对 |
| 3 | 认证机制 | 无 JWT/Session，currentUserId 通过参数传递 | 引入 JWT Token 认证，前端请求头携带 Authorization |
| 4 | 级联删除 | 删除项目/任务不清理子数据 | 删除项目时级联删除其下任务、进度记录和总结 |
| 5 | 分页查询 | 列表全量返回 | 引入 MyBatis-Plus 分页插件，支持分页查询 |
| 6 | API Key 管理 | 千问 Key 硬编码在配置文件默认值中 | 通过环境变量注入，不写入代码仓库 |
| 7 | 数据隔离 | 所有用户看到全部数据 | 成员只能看到自己所在项目的数据 |
| 8 | 操作日志 | 无操作审计 | 引入操作日志表，记录关键操作的执行人和时间 |


