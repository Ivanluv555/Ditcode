- POST /api/auth/default-login  
  - 作用：创建或返回 demo/default 用户并颁发 session token（用于无账号快速体验）。  
  - 鉴权：否  
  - 请求 body：无  
  - 响应示例：{ ok: true, token: string, user: { id, username, email, ... } }  
  - 要点：前端会把 token 存本地并用于后续请求 Authorization: Bearer <token>。
 - GET /api/auth/session  
  - 作用：校验 token，返回当前用户信息。  
  - 鉴权：是（通过 Authorization header）  
  - 响应示例：{ ok: true, user: { id, username, email, ... } } 或 401 错误。
 - POST /api/auth/login  
  - 作用：按 account/password 登录并返回 token。  
  - 鉴权：否  
  - 请求 body：{ account, password }  
  - 响应示例：{ ok: true, token, user } 或 400/404 错误。
 - POST /api/auth/register  
  - 作用：注册新用户并返回 token。  
  - 鉴权：否  
  - 请求 body：{ username, email, password }  
  - 响应示例：{ ok: true, token, user } 或 400（用户名/邮箱重复）。
 - POST /api/auth/logout  
  - 作用：登出，撤销 session token。  
  - 鉴权：是（可由后端从 header 移除 session）  
  - 响应示例：{ ok: true }

二、工作区 / 用户数据（workspace）

 - GET /api/workspace  
  - 作用：获取当前用户的 workspace（archives、tasks、messages、modelAsset、archiveOrder、activeArchiveId 等）。  
  - 鉴权：是  
  - 响应示例：{ ok: true, version:2, activeArchiveId, archiveOrder:[], archives: { [id]: { id, title, tasks:[], messages:[], modelAsset:{...}, 
ownerId,... } } }  
  - 要点：前端依赖 legacy projection（也接受不同字段排列）；返回中的 modelAsset.imagePreview 用于卡片/气泡封面。
 - PUT /api/workspace  
  - 作用：保存/同步用户 workspace（前端会将完整或部分 workspace PUT 到此端点）。  
  - 鉴权：是  
  - 请求 body：workspace 对象（可能包含 version, archives, tasks, assetHistory 等）  
  - 响应示例：{ ok:true, ...savedWorkspace }（返回保存后的 workspace）
 - POST /api/workspace/reset  
  - 作用：重置工作区（生成新的空 archive 并返回）。  
  - 鉴权：是  
  - 请求 body：可无  
  - 响应示例：{ ok:true, ...newWorkspace }

三、社区 / 资源（community / featured / resources）

 - GET /api/community  
  - 作用：列出公开的社区作品（archives 数组）。  
  - 鉴权：可匿名（mock 接受匿名）  
  - 响应示例：{ ok:true, archives: [ { id, title, ownerId, ownerName, modelAsset:{ imagePreview,... }, messages:[], remixCount,... } ] }  
  - 要点：前端展示时用 modelAsset.imagePreview 或 fallback '/assets/index.jpg'。
 - GET /api/featured  
  - 作用：返回“优秀案例 / 精选”集合（front-end Featured 页面使用）。  
  - 鉴权：可匿名  
  - 响应示例：{ ok:true, archives: [...] }（前端将其映射到 featuredArchives）
 - GET /api/resources  
  - 作用：返回资源灵感库（分类资源，用于 ResourceLibrary 页面）。  
  - 鉴权：可匿名  
  - 响应示例：{ ok:true, archives: [ { id, title, category, ownerId, ownerName, modelAsset:{...}, messages:[], remixCount } ] }
 - POST /api/community/remix  
  - 作用：登记某 community 存档被 remix（body: { communityId }），返回新的 remixCount。  
  - 鉴权：可视业务决定（mock-server 允许匿名）；前端会在 remix 后调用该接口以更新计数。  
  - 请求 body：{ communityId }  
  - 响应示例：{ ok:true, remixCount:number }
 - POST /api/community/publish  
  - 作用：将用户 workspace 的某个 archive 发布为 community 条目（body: { archiveId }）。  
  - 鉴权：是（必须为 archive 所有者）  
  - 请求 body：{ archiveId }  
  - 响应示例：{ ok:true, isPrivate:false }
 - POST /api/community/unpublish  
  - 作用：撤回发布（body: { archiveId }）。  
  - 鉴权：是（必须为 archive 所有者）  
  - 响应示例：{ ok:true, isPrivate:true }

四、模型生成 / 任务（model）

 - POST /api/model/generate  
  - 作用：提交生成任务（图像/asset），返回任务 id 和生成结果（或 asset 标识/可访问 URL/ base64 data）。前端根据返回的字段兼容多种格式。  
  - 鉴权：视业务；mock-server 未实现（前端调用了 requestModelApi，需真实后端或模型代理）。  
  - 请求 body（前端示例）：{ taskId, archiveId, prompt, imageName, imageBase64 }  
  - 期望响应字段（任意一种或多项）：
   - 标识： { taskId 或 id }（用于更新本地任务 id）  
   - 生成结果： { imagePreview: dataURL | imageBase64 | image | image_url | url | data | base64 } 或 返回 asset 名称字段（assetName / assetId / 
fileName / file_name / asset）  
   - mime 信息： imageMime / mime / contentType  
   - finishedAt / status / progress 可选
  - 要点/建议：
   - 推荐返回 assetName（例如 "asset_123.png"）或直接返回可访问的 URL；若返回 assetName，后端应实现 GET /api/assets/{name}（见下文）。
   - 前端会将返回的 preview 处理为 data:URL / blob URL / http URL 并把其写入 tasks + modelAsset + messages（以聊天气泡显示）。

五、静态资源 / 资产（建议）

 - GET /api/assets/{name}  
  - 作用：根据 asset 名称返回图片二进制（Content-Type: image/*），用于工作区气泡统一获取/缓存/鉴权。  
  - 鉴权：可选（取决于资源是否私有）  
  - 请求参数：可支持 ?width=<px> 等缩放参数以返回不同尺寸预览（前端样式限制宽度为容器的 50%）  
  - 响应示例：二进制图像，或 302 重定向到 CDN URL；推荐加 Cache-Control 头。  
  - 要点：前端现在（已改）会使用 /api/assets/{name} 当后端在 /api/model/generate 返回 assetName。

六、健康/诊断

 - GET /api/health  
  - 作用：服务自检（返回服务状态）。  
  - 鉴权：否  
  - 响应示例：{ ok:true, service: 'ditapp' }

七、错误和通用约定（前端期望）

 - 成功响应：通常 JSON 且可能包含 ok: true；具体字段按接口不同。  
 - 失败响应：HTTP 非 2xx 状态，响应体为 JSON，含 message 字段；前端 apiClient 会将 message 用作异常信息（throw new Error(data.message || 
'请求失败')）。  
 - Authorization：前端在有 token 时会自动在 apiRequest 中加入 Authorization: Bearer <token>（requestAuthApi / requestWorkspaceApi / 
requestCommunityApi / requestModelApi 都会这样做）。

八、前端具体依赖字段备注（关键字段）

 - archive object: { id, title, ownerId, ownerName, tasks:[{id,status,progress,prompt,imagePreview,...}], 
messages:[{role,text,imagePreview,createdAt}], modelAsset:{ id, prompt, imagePreview, localPreview?, createdAt, updatedAt }, isPrivate?, createdAt, 
updatedAt, category? }  
 - model/generate 返回：至少应包含任务 id 与一种可显示的图像字段（imagePreview 或 assetName 或 url / base64）
