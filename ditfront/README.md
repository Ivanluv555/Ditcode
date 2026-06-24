# ditapp

一个基于 Vue 3 + Vite + Three.js 的前端原型项目，包含：

- 发现页（素材卡片流）
- 工作台（全景 WebGL 画布 + 编辑侧栏 + 任务悬浮舱）
- 任务状态模拟与 Cross-Fade 过渡演示

当前以前端交互验证为目标，业务数据改为由 Node.js mock 服务读写本地 JSON 文件持久化。

## 技术栈

- Vue 3（Composition API / script setup）
- Vite 5
- Pinia
- Vue Router
- Three.js
- @tweenjs/tween.js

## 环境要求
- Node.js 18+
- pnpm 8+
## 快速开始

```bash
pnpm install
pnpm dev
```

默认启动后访问控制台输出的本地地址（通常是 http://localhost:5173）。

`pnpm dev` 会同时启动 Vite 和 Node.js JSON mock 服务，前端通过 `/api` 代理访问后者。

如需单独运行 Node.js mock 服务（用于联调或独立调试），可额外执行：

```bash
pnpm mock:server
```

默认 mock 账号：`demo_user / demo123456`。

持久化数据文件位于：

- `mock-server/data/auth.json`
- `mock-server/data/workspace.json`

## 构建与预览

```bash
pnpm build
pnpm preview
```

## 路由说明

- `/`：Discovery 页面，展示素材卡片
- `/workshop/:id`：Workshop 页面，加载 WebGL 工作台

## 主要功能

### 1. Discovery 素材浏览

- 使用 mock 数据渲染素材卡片瀑布/网格
- 点击卡片跳转至对应工作台路由

### 2. Workshop 工作台

- Three.js 初始化球形内景场景（线框材质示意）
- 画布点击后执行射线拾取，输出 UV 映射坐标
- 触发 mock SAM 遮罩叠层（半透明球壳）

### 3. 编辑面板与任务舱

- 编辑面板创建任务并模拟进度增长
- 任务状态由 Pinia 管理并在左上悬浮舱展示
- 任务与作品历史会同步写入 `mock-server/data/workspace.json`
- 任务完成后派发 `cross-fade-trigger` 事件，驱动场景过渡
- 账号、会话与默认登录数据会同步写入 `mock-server/data/auth.json`

## 项目结构

```text
src/
	components/
		AssetCard.vue       # 发现页素材卡片
		EditorPanel.vue     # 右侧编辑面板（任务生成入口）
		TaskPod.vue         # 左上任务悬浮舱
	core/
		socket/
			index.js          # Mock Socket 实现
		webgl/
			Engine.js         # Three.js 引擎生命周期与过渡逻辑
			GC.js             # 资源释放
			Loader.js         # KTX2 纹理加载器封装（预留）
			Raycast.js        # 射线拾取与遮罩演示
	hooks/
		useResponsive.js    # 响应式断点工具
		useSAM.js           # SAM 请求调用封装（当前 mock）
	router/
		index.js            # 路由定义
	store/
		useSceneStore.js    # 场景状态
		useTaskStore.js     # 任务状态
	styles/
		global.css          # 全局样式
	views/
		Discovery/index.vue
		Workshop/index.vue
```

## 当前状态与边界

- 项目目前是前端原型，接口与实时链路尚未接入：
	- `useSAM` 为日志模拟
	- `core/socket` 为本地 MockSocket
- 编辑面板中的生成流程为前端定时器模拟，不代表真实推理耗时
- `Loader.js` 已预留 KTX2 纹理能力，但默认流程尚未接入真实资源

## 开发建议

- 如果要接入真实推理任务，建议将任务状态机统一为：`queued -> inferencing -> compositing -> success/failed`
- 如果要接入实时消息，建议把 `MockSocket` 抽象成统一事件总线适配层
- 如果要接入真实全景资源，优先打通 `Loader.js` 与引擎初始化流程

## 可用脚本

- `pnpm dev`：同时启动 Vite + Node.js JSON mock 服务
- `pnpm mock:server`：启动 Node.js mock 后端
- `pnpm build`：生产构建
- `pnpm preview`：本地预览构建产物

## 设计资源清单

- Icon 命名与资源检索清单见：`docs/icon-naming-list.md`
