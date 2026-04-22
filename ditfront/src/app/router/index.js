import { createRouter, createWebHistory } from 'vue-router';
import Home from '@/packages/workspace/pages/HomePage.vue';
import Discovery from '@/packages/discovery/pages/DiscoveryPage.vue';
import Workshop from '@/packages/workspace/pages/WorkshopPage.vue';
import Panorama from '@/packages/workspace/pages/PanoramaPage.vue';
import Viewer from '@/packages/workspace/pages/ViewerPage.vue';
import Login from '@/packages/auth/pages/LoginPage.vue';
import Register from '@/packages/auth/pages/RegisterPage.vue';
import MyContent from '@/packages/content/pages/MyContentPage.vue';
import Settings from '@/packages/settings/pages/SettingsPage.vue';
const routes = [
  { path: '/', component: Home },
  { path: '/community', component: Discovery, meta: { hideComposer: true } },
  { path: '/my-content', component: MyContent, meta: { hideWorkspace: true } },
  { path: '/settings', component: Settings },
  { path: '/panorama', component: Panorama },
  { path: '/viewer', component: Viewer },
  { path: '/workshop/:id', component: Workshop },
  { path: '/login', component: Login, meta: { standalone: true } },
  { path: '/register', component: Register, meta: { standalone: true } }
];
export const router = createRouter({
  history: createWebHistory(),
  routes
});
