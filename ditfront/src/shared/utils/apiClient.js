/*
 * apiClient.js
 * -------------
 * 简介：这个文件提供一个通用的 HTTP API 请求函数 apiRequest，用于向后端发送请求并处理响应。
 *
 * 设计目标（面向初学者）：
 * - 将后端地址统一通过 VITE_API_BASE 环境变量配置（例如：https://api.example.com）
 * - 自动处理常见请求头（例如 Content-Type: application/json）
 * - 对响应做 JSON 解析并在非 2xx 时抛出错误，简化调用代码逻辑
 *
 * 使用示例：
 *   const data = await apiRequest('/api/workspace', { method: 'GET' });
 *
 * 注意事项：
 * - 若传入的是 FormData，函数会避免自动覆盖 Content-Type，以便浏览器自己设置边界字符串
 * - 当服务器返回非 JSON（例如空响应）时，会返回空对象而不会抛出解析错误
 */
const API_BASE = (import.meta.env.VITE_API_BASE || '').replace(/\/$/, '');

const resolveApiUrl = (path) => {
  const normalizedPath = path.startsWith('/') ? path : `/${path}`;
  if (!API_BASE) return normalizedPath;
  return `${API_BASE}${normalizedPath}`;
};

export const apiRequest = async (path, options = {}) => {
  const hasBody = options.body !== undefined && options.body !== null;
  const isFormData = typeof FormData !== 'undefined' && options.body instanceof FormData;
  const headerEntries = Object.entries(options.headers || {});
  const hasContentTypeHeader = headerEntries.some(([key]) => key.toLowerCase() === 'content-type');

  const headers = {
    ...(options.headers || {})
  };

  if (hasBody && !isFormData && !hasContentTypeHeader) {
    headers['Content-Type'] = 'application/json';
  }

  const response = await fetch(resolveApiUrl(path), {
    ...options,
    headers
  });

  let data;
  try {
    data = await response.json();
  } catch {
    data = {};
  }

  if (!response.ok) {
    throw new Error(data.message || '请求失败');
  }

  return data;
};

