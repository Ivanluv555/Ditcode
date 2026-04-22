/*
 * useSAM.js
 * ---------
 * 占位 Hook：用于提交 SAM（Segment Anything Model）相关参数的封装函数。
 * 当前实现为模拟占位（不向后端发送真实请求），便于后续替换为真实 API 调用。
 *
 * 返回值：
 * - submitSAMParams(x, y): 提交一个点位参数（目前为空实现）
 *
 * 使用场景：在图像交互需求中，可能需要把用户选择的点或框发送到分割模型。
 */
export function useSAM() {
  const submitSAMParams = (_pointX, _pointY) => {
    // 模拟请求占位：后续接入真实接口。
  };
  return { submitSAMParams };
}
