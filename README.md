# 养老院智慧管理系统 (Smart Elderly Care System)

![Version](https://img.shields.io/badge/version-1.0.0-blue.svg)
![Spring Boot](https://img.shields.io/badge/SpringBoot-2.7.18-green.svg)
![Vue](https://img.shields.io/badge/Vue.js-3.x-brightgreen.svg)
![Docker](https://img.shields.io/badge/Docker-Supported-blue.svg)

## 🌟 项目简介

本项目是一款专为中小型养老院设计的**全栈智慧管理系统**。通过数字化手段，实现对老人信息的精准管理、健康数据的实时记录以及异常状态的智能预警，旨在提升养老机构的服务质量与响应速度。

---

## ✨ 核心特性

- **🔐 统一身份认证**: 完善的登录与注册流程，保障数据访问安全。
- **👥 老人档案管理 (CRUD)**:
  - 实现老人基本信息的数字化建档。
  - 支持信息的快速检索、修改与注销。
- **📊 健康实时监测**:
  - 记录老人的关键生命体征（血压、体温）。
  - 提供结构化的历史监测记录，方便追踪健康趋势。
- **⚠️ 智能健康预警**:
  - **核心逻辑实现**: 当系统检测到录入体温超过 **37.3℃** 时，自动触发预警机制。
  - **视觉反馈**: 在管理后台自动标记为“异常数据”并通过红色警示色高亮展示，提醒管理人员及时处理。

---

## 🛠 技术栈

### 后端 (Backend)
- **核心框架**: Spring Boot 2.7.18
- **持久层**: MyBatis Plus 3.5.3.1 (实现 ORM 映射)
- **数据库**: MySQL 8.0
- **工具库**: Lombok (简化开发), Maven (包管理)

### 前端 (Frontend)
- **框架**: Vue.js 3.x
- **构建工具**: Vite
- **UI 组件库**: Element Plus (提供现代化的 UI 交互)
- **网络请求**: Axios (集成拦截器处理全局错误)

### 部署与运维 (DevOps)
- **容器化**: Docker & Docker Compose (实现一键环境部署)
- **静态资源服务器**: Nginx (前端镜像集成反向代理)

---

## 🚀 快速启动

### 1. 环境准备
- 确保您的开发环境已安装 **Docker** 和 **Docker Compose**。

### 2. 一键启动
在项目根目录（`207` 文件夹）下运行：

```bash
docker compose up --build
```

### 3. 访问系统
- **前端门户**: [http://localhost:3207](http://localhost:3207)
- **后端接口**: [http://localhost:8207](http://localhost:8207)
- **数据库管理**: `localhost:33207` (用户: `root` / 密码: `root`)

---

## 📂 项目结构

```text
207/
├── backend/            # 后端 SpringBoot 项目根目录
│   ├── src/            # Java 源码及核心业务逻辑
│   ├── Dockerfile      # 后端多阶段构建配置
│   └── pom.xml         # Maven 依赖管理
├── frontend/           # 前端 Vue3 项目根目录
│   ├── src/            # Vue 视图、组件及状态管理
│   ├── nginx.conf      # 前端 Nginx 反向代理配置
│   └── Dockerfile      # 前端生产环境镜像配置
├── sql/
│   └── init.sql        # 数据库初始化脚本（含表结构及演示数据）
└── docker-compose.yml  # 全栈容器编排配置文件
```

---

## 🛡️ 工程质量保证

1.  **代码规范**: 严格遵循 Java 命名规范与 RESTful API 设计原则。
2.  **安全性**: 前后端分离架构，后端接口通过跨域配置 (CORS) 与容器网络隔离。
3.  **健壮性**:
    - 前端集成 Axios 拦截器，对网络异常及业务错误进行统一 Toast 提示。
    - 后端通过 MyBatis Plus 防范 SQL 注入，逻辑层实现体温阈值自动计算。
4.  **现代化 UI**: 响应式布局设计，支持不同比例的屏幕访问。

---

## 📝 开发者说明
项目已预留多套环境变量接口，可根据实际生产环境快速调整数据库连接及 API 端口配置。
