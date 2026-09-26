<h1 align='center' style='font-size: 40px'>
  <a href="https://gitee.com/HanPulin/ditcode" style="text-decoration: none; color: grey;">Ditcode</a>
</h1>

<div align='center'>
  <img src="https://img.shields.io/badge/License-MIT-green">
  <img src="https://img.shields.io/badge/Version-0.1-blue">
  <img src="https://img.shields.io/badge/Java-21-orange">
  <img src="https://img.shields.io/badge/SpringBoot-4.0.5-brightgreen">
</div>

<div align='center'>
  <img src="https://img.shields.io/badge/Strategist-Nasuraya7-gold">
  <img src="https://img.shields.io/badge/Executor-Ivanluv555-aqua">
</div>

## 项目简介

Ditcode 是一个围绕**文本/图像驱动建模创作**构建的一体化系统，采用敏捷开发模式，提供从创作工作台到社区分享的完整链路。

### 核心功能

- **创作工作台**：支持档案管理、会话历史记录、首轮传图及后续文本交互
- **社区生态**：支持作品发布、浏览、Remix（二创）及计数统计
- **模型网关**：提供统一的 AI 模型转发接口，支持上游配置与降级策略
- **认证体系**：基于 Bearer Token 的用户认证与会话管理

## 技术架构

<div align='center'>

| 模块 | 技术栈 | 说明 |
|:---:|:---|:---|
| **前端** | Vue 3 + Pinia + Iconify | 位于 `ditfront` 目录 |
| **后端** | Spring Boot 4.0.5 + JPA + Spring Security | 位于 `src` 目录（ditserver） |
| **数据库** | MySQL / H2（开发环境） | 设计文档位于 `ditdbase` |
| **文档** | Markdown | 完整文档位于 `ditdocs` |

</div>

### 后端架构设计

- **core**：全局能力层（鉴权、配置、异常处理、通用工具）
- **module**：业务模块层（auth、workspace、community、model）
- **持久化**：JPA + Hibernate，采用 code-first 策略

### 数据模型

- `users` / `sessions`：用户与会话
- `archives` / `archive_messages` / `archive_tasks`：工作区档案与消息
- `archive_model_assets`：模型生成资产
- `community_publications`：社区发布
- `remix_events`：二创事件记录

## 快速开始

### 环境要求

- Java 21+
- Maven 3.6+
- MySQL 8.0+（可选，默认使用 H2）
- Node.js 18+（前端开发）

### 后端启动

```bash
# 使用 H2 内存数据库（默认）
./mvnw spring-boot:run

# 使用 MySQL（需配置环境变量）
export DB_URL=jdbc:mysql://localhost:3306/ditcode
export DB_USERNAME=root
export DB_PASSWORD=your_password
export DB_DRIVER=com.mysql.cj.jdbc.Driver
./mvnw spring-boot:run
```

后端服务启动后访问：
- API 基础路径：`http://localhost:8080/api`
- OpenAPI 文档：`http://localhost:8080/api/docs`
- Swagger UI：`http://localhost:8080/swagger-ui.html`
- 健康检查：`http://localhost:8080/api/health`

### 前端启动

```bash
cd ditfront
npm install
npm run dev
```

### 运行测试

```bash
./mvnw clean test
```

## API 接口

### 认证模块 `/api/auth/*`

- `POST /api/auth/register` - 用户注册
- `POST /api/auth/login` - 用户登录
- `POST /api/auth/logout` - 用户登出
- `GET /api/auth/session` - 获取当前会话（需 Bearer Token）
- `POST /api/auth/default-login` - 默认用户登录

### 工作区模块 `/api/workspace*`

- `GET /api/workspace` - 获取用户工作区（需认证）
- `PUT /api/workspace` - 更新工作区（需认证）
- `POST /api/workspace/reset` - 重置工作区（需认证）

### 社区模块 `/api/community*`

- `GET /api/community` - 获取社区列表
- `POST /api/community/publish` - 发布作品（需认证）
- `POST /api/community/unpublish` - 取消发布（需认证）
- `POST /api/community/remix` - Remix 作品（需认证）

### 模型网关 `/api/model/*`

- `POST /api/model/generate` - 模型生成接口

配置环境变量：
- `MODEL_UPSTREAM_URL` - 上游模型服务地址
- `MODEL_API_KEY` - API 密钥
- `MODEL_CONNECT_TIMEOUT_MS` - 连接超时（毫秒）
- `MODEL_READ_TIMEOUT_MS` - 读取超时（毫秒）

## 项目文档

完整的项目文档位于 `ditdocs` 目录，包含：

- **文档导航**：`00-文档导航.md`
- **项目演进**：`01-项目演进与里程碑.md`
- **技术选型**：`02-前端技术选型与实现.md`、`03-后端架构与模块设计.md`
- **数据库设计**：`04-数据库设计与规范.md`
- **接口契约**：`05-接口契约总表.md`
- **开发文档**：前端/后端/数据库各三个阶段的详细开发文档
- **后端说明**：`SpringBoot后端说明.md`

详细文档导航请参阅：[ditdocs/00-文档导航.md](ditdocs/00-文档导航.md)

## 开发约定

1. **图标**：前端统一使用 Iconify，后端不参与图标资源管理
2. **资源边界**：前端负责静态资源，后端仅负责动态资源地址与模型结果
3. **后端结构**：全局能力放 `core`，业务域放 `module`
4. **持久化**：统一使用 JPA，`spring.jpa.hibernate.ddl-auto=update`（code-first）
5. **安全**：密码使用 BCrypt 哈希，会话使用 Bearer Token
6. **权限**：仅所有者可发布/取消发布，登录用户可读写工作区

## 参与贡献

我们欢迎各种形式的贡献，包括但不限于：

1. Fork 本仓库
2. 创建特性分支（`git checkout -b feature/AmazingFeature`）
3. 提交更改（`git commit -m 'Add some AmazingFeature'`）
4. 推送到分支（`git push origin feature/AmazingFeature`）
5. 提交 Pull Request

请确保：
- 代码符合项目的编码规范
- 提交信息清晰明确
- 包含必要的测试用例
- 更新相关文档

## 许可证

本项目采用 MIT 许可证，详见 [LICENSE](LICENSE) 文件。

## 联系方式

- 问题反馈：通过 Gitee Issues 提交

---

<div align='center'>
  <sub>Built with ❤️ by Ditcode Team</sub>
</div>
