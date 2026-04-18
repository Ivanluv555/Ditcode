# SpringBoot 后端说明（ditserver）

## 1. 实现范围

`ditserver` 已按前端当前调用契约实现以下模块：

- 认证：`/api/auth/*`
- 工作区：`/api/workspace*`
- 社区：`/api/community*`
- 健康检查：`/api/health`

后端采用关系型建模（JPA）拆分聚合：

- `users` / `sessions`
- `archives`
- `archive_messages`
- `archive_tasks`
- `archive_model_assets`
- `community_publications`
- `remix_events`

与 `ditdbase/数据库设计.md` 的 v1 模型保持一致，业务约束（权限、首轮图片、发布/取消发布、remix 计数）在服务层执行。

## 2. 接口契约（与前端一致）

### 认证

- `POST /api/auth/default-login`
- `GET /api/auth/session`（Bearer Token）
- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/auth/logout`

响应字段：

- 成功：`{ ok: true, ... }`
- 失败：`{ ok: false, message: "..." }`

### 工作区

- `GET /api/workspace`（Bearer Token）
- `PUT /api/workspace`（Bearer Token）
- `POST /api/workspace/reset`（Bearer Token）

工作区响应字段包含：

- `version`
- `activeArchiveId`
- `archiveOrder`
- `archives`（对象结构，key 为 archiveId）
- `tasks`（兼容前端 legacy 投影）
- `assetHistory`（兼容前端 legacy 投影）

### 社区

- `GET /api/community`
- `POST /api/community/publish`（Bearer Token）
- `POST /api/community/unpublish`（Bearer Token）
- `POST /api/community/remix`（Bearer Token）

## 3. 安全与认证

- 使用 Bearer Token（存储于 `sessions` 表）。
- 密码使用 `BCrypt` 哈希，不落明文。
- 权限控制：
  - 仅 owner 可发布/取消发布。
  - 仅登录用户可读写工作区。

## 4. 本地运行

`ditserver` 默认使用 H2 文件库（便于本地直接启动），也支持通过环境变量切换到 MySQL：

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `DB_DRIVER`

启动：

```bash
cd ditserver
sh mvnw spring-boot:run
```

测试：

```bash
cd ditserver
sh mvnw clean test
```

## 5. OpenAPI

- 文档 JSON：`/api/docs`
- Swagger UI：`/swagger-ui.html`
