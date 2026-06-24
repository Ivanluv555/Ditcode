/*
 * defaultCover.js
 * ----------------
 * 提供应用默认封面图的全局常量，供多个组件或页面引用。
 * 将其挂载到 globalThis 上，是为了在不通过模块导入的场景下也能直接访问。
 */
const DEFAULT_COVER = '/assets/index.jpg';

globalThis.__DITAPP_DEFAULT_COVER__ = DEFAULT_COVER;


