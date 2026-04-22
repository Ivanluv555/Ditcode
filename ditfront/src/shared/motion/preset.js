/*
 * motion/preset.js
 * ----------------
 * 存放全局动效（动画）时间与缓动（easing）预设，便于在应用中统一使用，
 * 保持动画体验一致并方便做无障碍优化（例如缩短/禁用动画）。
 */
export const MOTION_DURATION = Object.freeze({
  fast: 0.18,
  normal: 0.32,
  slow: 0.56,
  intro: 0.65
});

export const MOTION_EASE = Object.freeze({
  enter: 'power3.out',
  exit: 'power2.in',
  subtleEnter: 'power1.out',
  subtleExit: 'power1.in',
  bounceEnter: 'back.out(1.2)'
});
