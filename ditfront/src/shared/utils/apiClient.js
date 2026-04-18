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

