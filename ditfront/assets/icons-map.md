# Icon & 静态图片资源清单

说明：
- 统一使用 `kebab-case` 命名，例如：`ic-settings-outline.svg`
- 推荐格式：`ic-<业务域>-<语义>-<风格>.svg`
- 风格后缀建议：`outline` / `filled` / `duotone`

## 现有资源映射

| 文件名 | 类型 | 作用说明 |
| --- | --- | --- |
| Login.svg | icon | 登录按钮图标 |
| My.svg | icon | 个人中心图标 |
| Register.svg | icon | 注册按钮图标 |
| Social.svg | icon | 社交相关图标 |
| admin.jpg | image | 管理员头像或后台图片 |
| delete.svg | icon | 删除操作图标 |
| favicon.svg | icon | 网站 favicon 图标 |
| icons.svg | icon | SVG 图标集合（Sprite） |
| animations/loading-spinner.svg | animation | 聊天生成中的加载动画（Home 气泡） |
| index.jpg | image | 首页展示图片 |
| jianzhu(black).json | icon数据 | 建筑相关黑色风格图标数据 |
| jianzhu(white).json | icon数据 | 建筑相关白色风格图标数据 |
| return.svg | icon | 返回按钮图标 |
| send.svg | icon | 发送按钮图标 |
| uMy.svg | icon | 用户中心图标（变体） |
| uShow.svg | icon | 展示/预览图标（变体） |
| uSocial.svg | icon | 社交相关图标（变体） |
| 见筑（横）.svg | icon | 建筑横向图标 |
| 见筑（竖）.svg | icon | 建筑竖向图标 |

> 如需补充说明，请在下方继续添加。

---

## 详细命名规范与推荐

- 统一使用 `kebab-case` 命名，例如：`ic-settings-outline.svg`
- 推荐格式：`ic-<业务域>-<语义>-<风格>.svg`
- 风格后缀建议：`outline` / `filled` / `duotone`

（以下为原有命名规范与推荐，供参考）

## 导航与布局

- `ic-nav-home-outline.svg`
- `ic-nav-home-filled.svg`
- `ic-nav-discovery-outline.svg`
- `ic-nav-discovery-filled.svg`
- `ic-nav-workshop-outline.svg`
- `ic-nav-workshop-filled.svg`
- `ic-nav-my-content-outline.svg`
- `ic-nav-my-content-filled.svg`
- `ic-nav-settings-outline.svg`
- `ic-nav-settings-filled.svg`
- `ic-nav-help-outline.svg`
- `ic-nav-help-filled.svg`
- `ic-nav-collapse-left-outline.svg`
- `ic-nav-collapse-right-outline.svg`
- `ic-nav-expand-outline.svg`

## 顶栏与用户

- `ic-top-back-outline.svg`
- `ic-top-community-outline.svg`
- `ic-top-model-drawer-outline.svg`
- `ic-user-avatar-outline.svg`
- `ic-user-login-outline.svg`
- `ic-user-logout-outline.svg`
- `ic-user-profile-outline.svg`

## 创作与工作流

- `ic-action-new-task-outline.svg`
- `ic-action-upload-image-outline.svg`
- `ic-action-send-outline.svg`
- `ic-action-remove-outline.svg`
- `ic-action-edit-outline.svg`
- `ic-action-save-outline.svg`
- `ic-action-download-outline.svg`
- `ic-action-share-outline.svg`
- `ic-action-remix-outline.svg`
- `ic-action-delete-outline.svg`

## 历史与状态

- `ic-history-outline.svg`
- `ic-history-recent-outline.svg`
- `ic-status-queued-filled.svg`
- `ic-status-inferencing-filled.svg`
- `ic-status-compositing-filled.svg`
- `ic-status-success-filled.svg`
- `ic-status-failed-filled.svg`
- `ic-status-warning-filled.svg`

## 设置与帮助

- `ic-settings-theme-outline.svg`
- `ic-settings-language-outline.svg`
- `ic-settings-notification-outline.svg`
- `ic-settings-privacy-outline.svg`
- `ic-settings-security-outline.svg`
- `ic-help-doc-outline.svg`
- `ic-help-faq-outline.svg`
- `ic-help-feedback-outline.svg`
- `ic-help-support-outline.svg`

## 文件与媒体

- `ic-file-image-outline.svg`
- `ic-file-model-outline.svg`
- `ic-file-folder-outline.svg`
- `ic-file-cloud-outline.svg`
- `ic-media-play-filled.svg`
- `ic-media-pause-filled.svg`
- `ic-media-stop-filled.svg`

## 资源来源建议

- Iconify（多图标库聚合）
- Lucide（线性风格）
- Material Symbols（完整覆盖）
- Remix Icon（产品常用语义齐全）

建议流程：
1. 先按上面语义清单检索资源
2. 按规范命名后落库到 `icons/`
3. 在组件中仅使用命名后的本地文件，避免 URL 直链
