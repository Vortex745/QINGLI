# 青里 - 校园生活服务社区平台

<div align="center">

![Version](https://img.shields.io/badge/version-1.0.0-blue.svg)
![License](https://img.shields.io/badge/license-MIT-green.svg)
![Platform](https://img.shields.io/badge/platform-WeChat%20MiniProgram-brightgreen.svg)

**专为校园师生打造的一站式生活服务社区**

闲置交易 · 校园广场 · 兼职信息 · 失物招领

</div>

---

## 项目简介

**青里** 是一个面向高校师生的校园社区平台，旨在解决校园内信息碎片化、交易信任成本高等痛点。

### 核心价值
- **高效交易** - 便捷的闲置物品发布与搜索
- **真实社区** - 校友间的互动与信任
- **精准服务** - 兼职、失物招领等校园高频工具

---

## 功能特性

### 客户端 (微信小程序)
| 模块 | 功能 |
|------|------|
| 闲置交易 | 多图发布、瀑布流展示、关键词搜索、商品状态管理 |
| 校园广场 | 图文动态、匿名发布、点赞评论、可见范围设置 |
| 兼职信息 | 职位详情展示、直接联系发布者 |
| 失物招领 | 失物/招领分类、快速联系 |
| 用户中心 | 微信一键登录、发布管理、收藏夹、消息通知 |
| 实时聊天 | WebSocket 私信沟通 |

### 管理后台 (Web)
| 模块 | 功能 |
|------|------|
| 数据概览 | 用户/商品/帖子统计、数据可视化 |
| 用户管理 | 用户列表、状态管理 |
| 内容审核 | 商品、帖子、评论审核 |
| 系统通知 | 全员/定向通知发送 |

---

## 技术栈

### 客户端
| 层级 | 技术 |
|------|------|
| 前端 | 微信原生小程序 (WXML/WXSS/JS) |
| 后端 | Spring Boot 3.4.1 + MyBatis-Plus 3.5.7 |
| 数据库 | MySQL 8.0 |
| 实时通信 | WebSocket |

### 管理后台
| 层级 | 技术 |
|------|------|
| 前端 | Vue 3 + Vite + Element Plus + Pinia + TailwindCSS + ECharts |
| 后端 | Spring Boot 3.4.1 + MyBatis-Plus + JWT |

### 通用
- **语言**: Java 17, JavaScript
- **工具**: Lombok, FastJSON2, Apache Commons
- **文档**: Knife4j (Swagger 3)

---

## 项目结构

```
qingli/
├── native/                  # 小程序前端
│   ├── pages/               # 页面
│   ├── components/          # 组件
│   ├── custom-tab-bar/      # 自定义底部导航
│   ├── utils/               # 工具类
│   └── app.json             # 全局配置
│
├── backend/                 # 小程序后端
│   ├── src/main/java/       # Java 源码
│   │   └── com/jin/xianqu_backend/
│   │       ├── controller/  # 控制器
│   │       ├── service/     # 服务层
│   │       ├── mapper/      # 数据访问层
│   │       ├── model/       # 实体/DTO/VO
│   │       └── config/      # 配置类
│   └── src/main/resources/  # 配置文件
│
├── admin/                   # 管理后台前端
│   ├── src/
│   │   ├── views/           # 页面
│   │   ├── components/      # 组件
│   │   ├── stores/          # Pinia 状态管理
│   │   └── router/          # 路由
│   └── vite.config.js
│
├── admin_backend/           # 管理后台后端
│   └── src/main/java/
│       └── com/jin/xianqu_admin/
│
└── PRD.md                   # 产品需求文档
```

---

## 快速开始

### 环境要求
- JDK 17+
- Node.js 16+
- MySQL 8.0+
- 微信开发者工具

### 1. 数据库初始化
```sql
CREATE DATABASE qingli_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```
导入 `backend/sql/` 目录下的 SQL 脚本。

### 2. 启动后端

**小程序后端** (`backend`)
```bash
cd backend
# 修改 application.yml 数据库配置
mvn spring-boot:run
```
API 文档: `http://localhost:8080/doc.html`

**管理后台后端** (`admin_backend`)
```bash
cd admin_backend
mvn spring-boot:run
```

### 3. 启动前端

**小程序** (`native`)
1. 微信开发者工具导入 `native` 目录
2. 修改 `app.js` 中的 `baseUrl`
3. 填写 AppID 并编译

**管理后台** (`admin`)
```bash
cd admin
npm install
npm run dev
```

---

## 接口规范

统一响应格式：
```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

| 状态码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 参数错误 |
| 401 | 未登录 |
| 403 | 无权限 |
| 500 | 服务器错误 |

---

## 许可证

[MIT License](LICENSE)
