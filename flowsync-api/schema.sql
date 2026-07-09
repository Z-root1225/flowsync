CREATE DATABASE IF NOT EXISTS flowsync_simple DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE flowsync_simple;

-- 1. 用户表 (sys_user)
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '登录账号',
    password VARCHAR(100) NOT NULL COMMENT '密码(明文存储，教学简化)',
    real_name VARCHAR(50) NOT NULL COMMENT '真实姓名',
    role VARCHAR(30) NOT NULL COMMENT '角色(负责人/成员)',
    phone VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
    email VARCHAR(100) DEFAULT NULL COMMENT '电子邮箱',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 2. 项目表 (project_info)
CREATE TABLE IF NOT EXISTS project_info (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT '项目名称',
    description VARCHAR(500) DEFAULT NULL COMMENT '项目说明描述',
    status VARCHAR(20) NOT NULL COMMENT '项目状态(未开始/进行中/已完成)',
    priority VARCHAR(20) NOT NULL COMMENT '优先级(低/中/高)',
    owner_id BIGINT NOT NULL COMMENT '项目负责人 ID',
    start_date DATE DEFAULT NULL COMMENT '开始日期',
    end_date DATE DEFAULT NULL COMMENT '结束日期',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目基本信息表';

-- 3. 任务表 (task_info)
CREATE TABLE IF NOT EXISTS task_info (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL COMMENT '所属项目 ID',
    parent_id BIGINT DEFAULT NULL COMMENT '父任务 ID(自关联支持任务层级)',
    title VARCHAR(100) NOT NULL COMMENT '任务标题',
    description VARCHAR(1000) DEFAULT NULL COMMENT '任务具体说明',
    assignee_id BIGINT DEFAULT NULL COMMENT '任务负责人 ID',
    creator_id BIGINT NOT NULL COMMENT '任务创建人 ID',
    status VARCHAR(20) NOT NULL COMMENT '任务状态(未开始/进行中/已完成)',
    priority VARCHAR(20) NOT NULL COMMENT '优先级(低/中/高)',
    due_date DATE DEFAULT NULL COMMENT '截止日期',
    ai_suggestion TEXT DEFAULT NULL COMMENT '千问 AI 建议内容',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务表';

-- 4. 任务进度记录表 (task_log)
CREATE TABLE IF NOT EXISTS task_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_id BIGINT NOT NULL COMMENT '关联任务 ID',
    progress_percent INT NOT NULL COMMENT '进度百分比(0-100)',
    content VARCHAR(1000) NOT NULL COMMENT '进展文字描述',
    operator_id BIGINT NOT NULL COMMENT '记录人 ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务进度跟踪记录表';

-- 5. 总结表 (task_summary)
CREATE TABLE IF NOT EXISTS task_summary (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL COMMENT '所属项目 ID',
    task_id BIGINT DEFAULT NULL COMMENT '关联任务 ID(可选)',
    summary_type VARCHAR(30) NOT NULL COMMENT '总结类型(阶段总结/最终总结)',
    content VARCHAR(2000) NOT NULL COMMENT '总结文字说明',
    created_by BIGINT NOT NULL COMMENT '创建人 ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目总结表';

-- 预置测试数据
INSERT INTO sys_user (username, password, real_name, role, phone, email) VALUES
('admin', '123456', '系统管理员', '负责人', '13800000000', 'admin@flowsync.com'),
('leader', '123456', '项目负责人', '负责人', '13811111111', 'leader@flowsync.com'),
('member1', '123456', '张三', '成员', '13922222222', 'zhangsan@flowsync.com'),
('member2', '123456', '李四', '成员', '13933333333', 'lisi@flowsync.com')
ON DUPLICATE KEY UPDATE username=VALUES(username);

INSERT INTO project_info (id, name, description, status, priority, owner_id, start_date, end_date) VALUES
(1, 'FlowSync 小组协同系统', '面向教学场景的小组协同任务管理平台，集成阿里云千问大模型进行辅助任务拆分与建议。', '进行中', '高', 2, '2026-07-01', '2026-07-30')
ON DUPLICATE KEY UPDATE id=VALUES(id);

INSERT INTO task_info (id, project_id, parent_id, title, description, assignee_id, creator_id, status, priority, due_date) VALUES
(1, 1, NULL, '编写需求规格说明书', '梳理和编写系统的功能模块、ER关系设计、API详细说明书文档。', 2, 2, '已完成', '高', '2026-07-08'),
(2, 1, NULL, '前端登录与注册页面开发', '利用HTML/JS/Vue3以及Element Plus绘制美观的登录和注册页面，并且完成密码的一致性验证和路由跳转。', 3, 2, '进行中', '高', '2026-07-15'),
(3, 1, 2, '实现密码一致性校验逻辑', '子任务：在注册界面的前端表单增加自定义 validator 限制两次密码不匹配时报错。', 3, 2, '进行中', '中', '2026-07-12'),
(4, 1, NULL, '后端核心接口与数据库建表', '设计 sys_user, project_info, task_info, task_log, task_summary 等5张业务表并利用Spring Boot提供接口。', 4, 2, '进行中', '高', '2026-07-18')
ON DUPLICATE KEY UPDATE id=VALUES(id);

INSERT INTO task_log (id, task_id, progress_percent, content, operator_id) VALUES
(1, 1, 100, '需求规格说明书已完成评审并输出为 Markdown 规格文档。', 2),
(2, 2, 60, '已完成登录注册的视觉布局以及表单验证规则，接下来编写 API 对接代码。', 3)
ON DUPLICATE KEY UPDATE id=VALUES(id);

INSERT INTO task_summary (id, project_id, task_id, summary_type, content, created_by) VALUES
(1, 1, 1, '阶段总结', '第一阶段的需求梳理非常顺利，明确了项目、任务、进度、总结的核心流转闭环，为后续前后端并发编码打下了坚实基础。', 2)
ON DUPLICATE KEY UPDATE id=VALUES(id);
