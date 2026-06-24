/*
 * fileToDataUrl.js
 * ----------------
 * 将用户本地选择的文件（File）转换为 data: URL（Base64），以便在 <img src="..."> 中直接展示。
 *
 * 为什么需要：
 * - 有时后端或本地预览需要以 data: URL 形式显示图片，例如在表单尚未上传到服务器时直接预览。
 *
 * 注意：
 * - 对大型文件（几 MB 以上）使用 data URL 会占用较多内存；在可能的情况下优先使用 URL.createObjectURL(file) 来生成 blob: URL。
 */
export const fileToDataUrl = (file) =>
  new Promise((resolve, reject) => {
    if (!file) {
      resolve('');
      return;
    }

    const reader = new FileReader();
    reader.onload = () => resolve(typeof reader.result === 'string' ? reader.result : '');
    reader.onerror = () => reject(reader.error || new Error('无法读取文件'));
    reader.readAsDataURL(file);
  });

