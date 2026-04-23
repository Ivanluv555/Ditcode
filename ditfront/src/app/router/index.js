/*
 * router/index.js
 * ----------------
 * 这个文件配置单页应用（SPA）的路由：它将 URL 路径映射到 Vue 组件。浏览器地址改变时，router 会在 <router-view> 中渲染对应页面。
 *
 * 初学者要点：
 * - 每一条 route 是一个对象 { path, component, meta? }
 * - path 是地址（例如 '/'、'/viewer'），component 是对应的 Vue 页面组件
 * - meta 字段可以携带额外信息（例如 hideComposer: true 表示该页面不展示底部 composer）
 * - 程序里通过 router.push({ path, query }) 做页面跳转
 *
 * 示例：router.push({ path: '/viewer', query: { image: url } }) 会把用户导航到 /viewer 页面并传入 image 参数用于展示。
 */
import { createRouter, createWebHistory } from 'vue-router';
import Home from '@/packages/workspace/pages/HomePage.vue';
import Discovery from '@/packages/discovery/pages/DiscoveryPage.vue';
import ResourceLibrary from '@/packages/discovery/pages/ResourceLibraryPage.vue';
import Viewer from '@/packages/workspace/pages/ViewerPage.vue';
import Login from '@/packages/auth/pages/LoginPage.vue';
import Register from '@/packages/auth/pages/RegisterPage.vue';
import MyContent from '@/packages/content/pages/MyContentPage.vue';
import Featured from '@/packages/content/pages/FeaturedPage.vue';
import Settings from '@/packages/settings/pages/SettingsPage.vue';
const routes = [
  { path: '/', component: Home },
  { path: '/community', component: Discovery, meta: { hideComposer: true } },
  { path: '/resources', component: ResourceLibrary, meta: { hideComposer: true } },
  { path: '/featured', component: Featured, meta: { hideWorkspace: true } },
  { path: '/my-content', component: MyContent, meta: { hideWorkspace: true } },
  { path: '/settings', component: Settings },
  { path: '/viewer', component: Viewer },
  //{ path: '/workshop/:id', component: Workshop },
  { path: '/login', component: Login, meta: { standalone: true } },
  { path: '/register', component: Register, meta: { standalone: true } }
];
export const router = createRouter({
  history: createWebHistory(),
  routes
});
