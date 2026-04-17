# ditapp 项目架构说明

## 1. 目标
本次重组目标是将代码按“业务包（packages）+ 共享层（shared）+ 应用层（app）”拆分，提升以下能力：

- 可读性：同一业务的页面、状态、工具靠近放置。
- 可维护性：减少跨目录跳转和路径混乱。
- 可扩展性：新增业务模块时可按包独立演进。

## 2. 目录总览

```text
src/
  app/
    router/
      index.js              # 路由注册与页面映射

  packages/
    auth/
      pages/
        LoginPage.vue
        RegisterPage.vue
      store/
        useAuthStore.js
      utils/
        authValidation.js

    workspace/
      pages/
        HomePage.vue
        WorkshopPage.vue
      components/
        TaskPod.vue
        EditorPanel.vue
      store/
        useTaskStore.js
        useSceneStore.js

    discovery/
      pages/
        DiscoveryPage.vue

    content/
      pages/
        MyContentPage.vue

    settings/
      pages/
        SettingsPage.vue

  shared/
    components/
      AssetCard.vue         # 复用展示组件
    assets/
      AnimationIndex.json
    core/
      socket/
        index.js
      webgl/
        Engine.js
        GC.js
        Loader.js
        Raycast.js
    hooks/
      useResponsive.js
      useSAM.js
    styles/
      global.css

  App.vue                   # 应用壳布局与全局交互入口
  main.js                   # Vue 应用启动入口
```

## 3. 分层职责

### 3.1 app 层
- 定位：应用编排层。
- 内容：路由、初始化、全局装配。
- 约束：不承载具体业务实现逻辑，只做连接与分发。

### 3.2 packages 层
- 定位：业务域模块。
- 原则：按业务能力拆分，不按技术类型横切。
- 当前业务包：
  - `auth`：登录/注册、认证状态、认证校验工具。
  - `workspace`：创作工作流、任务状态、工作台组件。
  - `discovery`：社区发现页面。
  - `content`：我的内容页面。
  - `settings`：设置与帮助页面。

### 3.3 shared 层
- 定位：跨业务复用层。
- 内容：通用组件、全局样式、静态资源、WebGL 基础能力、通用 hooks。
- 约束：shared 不依赖具体业务包，避免反向耦合。

### 3.4 mock-server 持久化层
- 定位：开发期文件数据层。
- 内容：使用 Node.js 读写 `mock-server/data/*.json`，保存认证、会话、任务和作品历史。
- 约束：前端不直接写 JSON 文件，只通过 `/api/*` 调用 Node 服务。

## 4. 导入规范

项目已配置 Vite 别名：`@ -> src`。

推荐导入方式：

```js
import { useTaskStore } from '@/packages/workspace/store/useTaskStore';
import AssetCard from '@/shared/components/AssetCard.vue';
```

导入原则：
- 业务内部优先使用 `@/packages/<domain>/...`。
- 跨业务复用能力通过 `@/shared/...` 引入。
- 仅资源位于项目根目录（如 `icons/`）时，使用必要的相对路径。

## 5. 路由与页面映射

路由定义文件：`src/app/router/index.js`

- `/` -> `workspace/HomePage.vue`
- `/community` -> `discovery/DiscoveryPage.vue`
- `/my-content` -> `content/MyContentPage.vue`
- `/settings` -> `settings/SettingsPage.vue`
- `/workshop/:id` -> `workspace/WorkshopPage.vue`
- `/login` -> `auth/LoginPage.vue`
- `/register` -> `auth/RegisterPage.vue`

## 6. 状态管理（Pinia）

- `useAuthStore`：用户会话、登录注册、登出。
- `useTaskStore`：任务列表与资产历史，并通过 Node JSON API 持久化。
- `useSceneStore`：场景参数（当前预留）。

建议：
- 业务状态归属对应业务包。
- 共享状态仅在确实跨业务时抽离为 shared store（当前未引入）。

## 7. 后续扩展约定

新增业务模块时，按以下模板创建：

```text
src/packages/<new-domain>/
  pages/
  components/   # 可选
  store/        # 可选
  utils/        # 可选
```

评估是否放入 `shared` 的标准：
- 是否被 2 个及以上业务包复用。
- 是否不依赖具体业务语义。

## 8. 本次整理变更摘要

- 路由迁移到 `src/app/router`。
- 认证、工作区、发现、内容、设置全部拆分到 `src/packages/*`。
- 通用资源和底层能力迁移到 `src/shared/*`。
- 全量导入路径修复并统一支持 `@` 别名。
- 清理旧目录（`src/views`、`src/store` 等空目录）。

## 9. 校验结果

已执行：`pnpm build`

结果：构建通过。

备注：存在第三方依赖（lottie）`eval` 与 chunk 体积告警，为既有告警，非本次结构重组引入。
