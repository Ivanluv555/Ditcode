import { createApp } from 'vue';
import { createPinia } from 'pinia';
import App from './App.vue';
import { router } from './app/router/index.js';
import './shared/styles/global.css';
const app = createApp(App);
app.use(createPinia());
app.use(router);
app.mount('#app');
